package com.cardrace.cardrace_server.model.game;

import com.cardrace.cardrace_server.controller.SocketIOEventHandler;
import com.cardrace.cardrace_server.exceptions.IllegalMoveException;
import com.cardrace.cardrace_server.exceptions.PlayerLimitException;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class Game {

    @JsonProperty
    public final String gameName;

    @JsonProperty
    public final int numPlayers;

    @JsonProperty
    private Board board;

    @JsonProperty
    private final Map<String, Types.Color> playerColorMap;

    @JsonProperty
    private final List<String> players;

    @JsonProperty
    private final Map<String, Integer> playerTurnCounter;

    @JsonProperty
    private final Map<Types.Color, Hand> colorHandMap;

    @JsonProperty
    private int handSize;

    @JsonProperty
    private final int maxHandSize;

    @JsonProperty
    private Deck deck;

    @JsonProperty
    private int currentPlayerIndex;

    @JsonProperty
    private Types.GameStatus status;

    @JsonProperty
    private Card lastCard;

    @JsonProperty
    private String winner;

    /**
     * Constructs a new Game with the specified name and number of players.
     *
     * @param gameName The name of the game
     * @param numPlayers The number of players in the game
     */
    @JsonCreator
    public Game(@JsonProperty("gameName") String gameName,
                @JsonProperty("numPlayers") int numPlayers) {
        this.gameName = gameName;
        this.numPlayers = numPlayers;

        this.players = new ArrayList<>();
        this.playerColorMap = new HashMap<>();
        this.colorHandMap = new EnumMap<>(Types.Color.class);
        this.playerTurnCounter = new HashMap<>();

        this.status = Types.GameStatus.WAITING;
        this.maxHandSize = Types.getHandSize(numPlayers);
    }

    /**
     * Initializes the game by randomly assigning colors, setting up the deck, dealing hands, creating the board, and setting game status.
     * This method should be called once all players have joined the game.
     */
    public void initializeGame() {

        List<Types.Color> colorList = Arrays.asList(
                Types.Color.RED,
                Types.Color.BLUE,
                Types.Color.GREEN,
                Types.Color.YELLOW,
                Types.Color.PURPLE,
                Types.Color.ORANGE
        );
        Collections.shuffle(colorList);


        List<Types.Color> colors = new ArrayList<Types.Color>();

        for (int pos = 0; pos < players.size(); pos++) {
            String player = players.get(pos);
            Types.Color color = colorList.get(pos);

            playerTurnCounter.put(player, 0);
            playerColorMap.put(player, color);
            colors.add(color);
        }
        this.deck = new Deck(2);
        this.board = new Board(colors);
        this.status = Types.GameStatus.IN_PROGRESS;
        this.currentPlayerIndex = 0;
        this.handSize = maxHandSize;
        dealOut();
    }

    /**
     * Adds a new player to the game.
     * Initializes the game if the player limit is reached after adding the new player.
     *
     * @param username The player to add to the game
     * @throws PlayerLimitException if the maximum number of players has already been reached
     */
    public void addPlayer(String username) throws PlayerLimitException {
        if (players.size() == numPlayers) {
            throw new PlayerLimitException("Player limit reached");
        }

        players.add(username);
    }

    /**
     * Remove player from game.
     */
    public void removePlayer(String username) {
        players.remove(username);
    }

    /**
     * Check win condition via seeing if a given player's safeZone is filled. Return boolean win.
     *
     * @param username The player who we want to see if won.
     * @return Boolean of whether player has won.
     */
    public boolean hasWon(String username) {
        Types.Color playerColor = getPlayerColor(username);
        Integer[] safeZone = board.getSafeZone(playerColor);

        return Arrays.stream(safeZone).noneMatch(Objects::isNull);
    }
    /**
     * Update player hand after playing a card. Re-deal if necessary and cycle handSize after all players are dealt.
     *
     * @param username Player who has used a card.
     * @param card Card used.
     */
    public void updatePlayerHand(String username, Card card) {
        Hand playerHand = getPlayerHand(username);
        playerHand.removeCard(card);
    }

    /**
     * Check if all player hands are empty and deal if so.
     */
    public boolean timeToDeal() {
        return players.stream()
                .map(playerColorMap::get)
                .map(colorHandMap::get)
                .allMatch(hand -> hand.getNumCards() == 0);
    }

    /**
     * Deal out hands to each player of size handSize.
     */
    public void dealOut() {
        for (String player : players) {
            Types.Color playerColor = getPlayerColor(player);
            colorHandMap.put(playerColor, deck.dealHand(handSize));
        }
        cycleHandSize();
    }

    /**
     * Apply a new move to the game post card-specific format-validation.
     *
     * @param card Primary card used.
     * @param substitute Subbed card if Joker is used.
     * @param distances Ordered marble to distance mapping.
     */
    public void applyMove(Card card, Card substitute, Map<Integer, Integer> distances) throws IllegalMoveException {
        Card actingCard = (card.cardValue == Types.CardValue.JOKER) ? substitute : card;
        List<Integer> marbleList = new ArrayList<>(distances.keySet());
        boolean protect;

        try {
            switch (actingCard.cardValue) {
                case JACK -> {
                    board.swapMarble(marbleList.get(0), marbleList.get(1));
                    protect = false;
                }
                case ACE, KING -> {
                    int marbleId = marbleList.get(0);
                    if (board.inReserve(marbleId)) {
                        board.activateMarble(marbleId);
                        protect = true;
                    } else {
                        board.moveMarble(marbleId, distances.get(marbleId), false);
                        protect = false;
                    }
                }
                case SEVEN -> {
                    for (Map.Entry<Integer, Integer> entry : distances.entrySet()) {
                        board.moveMarble(entry.getKey(), entry.getValue(), true);
                    }
                    protect = false;
                }
                default -> {
                    for (Map.Entry<Integer, Integer> entry : distances.entrySet()) {
                        board.moveMarble(entry.getKey(), entry.getValue(), false);
                    }
                    protect = false;
                }
            }
            if (!protect) {
                for (int marbleId : marbleList) {
                    board.getMarbles().get(marbleId).setState(Types.MarbleState.UNPROTECTED);
                }
            }
        } catch (Exception e) {
            throw new IllegalMoveException(e.getMessage());
        }

    }

    /**
     * Forfeit cards of hand
     *
     * @param username Player who forfeits cards.
     */
    public void clearHand(String username) {
        Hand playerHand = getPlayerHand(username);
        playerHand.forfeitCards();

    }

    /**
     * Update player turn by order of join.
     */
    public void nextTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % numPlayers;
    }

    /**
     * Cycle handSize between 2 and maximum hand size dependent on number of players.
     */
    public void cycleHandSize() {
        if (maxHandSize == 6) {
            handSize--;
        } else {
            handSize -= 2;
        }
        if (handSize < 2) {
            handSize = maxHandSize;
        }
    }

    /**
     * Getters/Setters
     */
    public Hand getPlayerHand(String username) {
        Types.Color color = getPlayerColor(username);
        if (color == null) {
            return null;
        }
        return colorHandMap.get(color);
    }
    public Types.Color getPlayerColor(String username) { return playerColorMap.get(username); }
    public Board getBoard() { return board; }
    public List<String> getPlayers() { return players; }
    public Types.GameStatus getStatus() { return status; }
    public void setStatus(Types.GameStatus status) { this.status = status; }
    public void setLastCard(Card lastCard) { this.lastCard = lastCard; }
    public Card getLastCard() { return lastCard; }
    @JsonIgnore
    public Types.Color getCurrentPlayerColor() {
        if (players.isEmpty() || currentPlayerIndex >= players.size()) {
            return null;
        }
        String currentPlayer = players.get(currentPlayerIndex);
        return playerColorMap.get(currentPlayer);
    }
    public void setWinner(String winner) { this.winner = winner; }
    public String getWinner() { return winner; }
    @JsonIgnore
    public int getNumCurrPlayers() { return players.size(); }
    public Map<String, Types.Color> getPlayerColorMap() { return playerColorMap; }
    public void incrementPlayerTurns(String username) {
        int turns = playerTurnCounter.get(username);
        playerTurnCounter.put(username, turns + 1);
    }
    public int getPlayerTurns(String username) { return playerTurnCounter.get(username); }
}
