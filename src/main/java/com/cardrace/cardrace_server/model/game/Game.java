package com.cardrace.cardrace_server.model.game;

import com.cardrace.cardrace_server.exceptions.IllegalMoveException;
import com.cardrace.cardrace_server.exceptions.PlayerLimitException;

import java.util.*;

public class Game {

    public final String gameName;
    public final int numPlayers;
    private Board board;
    private final Map<String, Types.Color> playerColorMap;
    private final List<String> players;
    private final Map<Types.Color, Hand> colorHandMap;
    private int handSize;
    private Deck deck;
    private int currentPlayerIndex;
    private Types.GameStatus status;
    private Card lastCard;
    private String winner;

    /**
     * Constructs a new Game with the specified name and number of players.
     *
     * @param gameName The name of the game
     * @param numPlayers The number of players in the game
     */
    public Game(String gameName, int numPlayers) {
        this.gameName = gameName;
        this.numPlayers = numPlayers;

        this.players = new ArrayList<>();
        this.playerColorMap = new HashMap<>();
        this.colorHandMap = new EnumMap<>(Types.Color.class);

        this.status = Types.GameStatus.WAITING;
        this.handSize = 6;
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

            playerColorMap.put(player, color);
            colors.add(color);
        }
        this.deck = new Deck(2);
        this.board = new Board(colors);
        this.status = Types.GameStatus.IN_PROGRESS;
        this.currentPlayerIndex = 0;
        this.handSize = 6;
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
     * Check win condition via seeing if a given player's safeZone is filled. Return boolean win.
     *
     * @param username The player who we want to see if won.
     * @return Boolean of whether player has won.
     */
    public boolean hasWon(String username) {
        Types.Color playerColor = getPlayerColor(username);
        Marble[] safeZone = board.getSafeZone(playerColor);

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
    public void applyMove(Card card, Card substitute, LinkedHashMap<Marble, Integer> distances) throws IllegalMoveException {
        Card actingCard = (card.cardValue == Types.CardValue.JOKER) ? substitute : card;
        List<Marble> marbleList = new ArrayList<>(distances.keySet());

        switch (actingCard.cardValue) {
            case JACK -> {
                board.swapMarble(marbleList.get(0), marbleList.get(1));
            }
            case ACE, KING -> {
                Marble marble = marbleList.get(0);
                if (board.inReserve(marble)) {
                    board.activateMarble(marble);
                } else {
                    board.moveMarble(marble, distances.get(marble), false);
                }
            }
            case SEVEN -> {
                for (Map.Entry<Marble, Integer> entry : distances.entrySet()) {
                    board.moveMarble(entry.getKey(), entry.getValue(), true);
                }
            }
            default -> {
                for (Map.Entry<Marble, Integer> entry : distances.entrySet()) {
                    board.moveMarble(entry.getKey(), entry.getValue(), false);
                }
            }
        }
        for (Marble marble : marbleList) {
            marble.setState(Types.MarbleState.UNPROTECTED);
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
     * Cycle handSize between 2 and 6
     */
    public void cycleHandSize() {
        handSize--;
        if (handSize < 2) {
            handSize = 6;
        }
    }

    /**
     * Getters/Setters
     */
    public Hand getPlayerHand(String username) { return colorHandMap.get(getPlayerColor(username)); }
    public Types.Color getPlayerColor(String username) { return playerColorMap.get(username); }
    public Board getBoard() { return board; }
    public List<String> getPlayers() { return players; }
    public Types.GameStatus getStatus() { return status; }
    public void setStatus(Types.GameStatus status) { this.status = status; }
    public void setLastCard(Card lastCard) { this.lastCard = lastCard; }
    public Card getLastCard() { return lastCard; }
    public Types.Color getCurrentPlayerColor() { return playerColorMap.get(players.get(currentPlayerIndex)); }
    public void setWinner(String winner) { this.winner = winner; }
    public String getWinner() { return winner; }
    public int getNumCurrPlayers() { return players.size(); }
    public Map<String, Types.Color> getPlayerColorMap() { return playerColorMap; }
}
