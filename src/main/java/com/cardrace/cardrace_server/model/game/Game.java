package com.cardrace.cardrace_server.model.game;

import com.amazonaws.services.dynamodbv2.xspec.M;
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

        this.deck = new Deck(2);
        List<Types.Color> colors = new ArrayList<Types.Color>();

        for (int pos = 0; pos < players.size(); pos++) {
            String player = players.get(pos);
            Types.Color color = colorList.get(pos);

            playerColorMap.put(player, color);
            colorHandMap.put(color, deck.dealHand(handSize));
            colors.add(color);
        }

        this.board = new Board(colors);
        this.status = Types.GameStatus.IN_PROGRESS;
        this.currentPlayerIndex = 0;
        this.handSize--;
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

        if (players.size() == numPlayers) {
            initializeGame();
        }
    }

    /**
     * Check win condition via seeing if a given player's safeZone is filled. Return boolean win.
     *
     * @param username The player who we want to see if won.
     * @return Boolean of whether player has won.
     */
    public boolean hasWon(String username) {
        Types.Color playerColor = playerColorMap.get(username);
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

        Types.Color playerColor = playerColorMap.get(username);
        Hand playerHand = colorHandMap.get(playerColor);
        playerHand.removeCard(card);

        if (playerHand.getNumCards() == 0) {
            deck.dealHand(handSize);
            if (Objects.equals(username, players.get(numPlayers - 1))) { cycleHandSize(); }
        }
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
    public Types.GameStatus getStatus() { return status; }
    public void setStatus(Types.GameStatus status) { this.status = status; }
    public void setLastCard(Card lastCard) {
        this.lastCard = lastCard;
    }
    public Card getLastCard() {
        return lastCard;
    }
}
