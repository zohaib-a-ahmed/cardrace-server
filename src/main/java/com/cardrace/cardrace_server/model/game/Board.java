package com.cardrace.cardrace_server.model.game;

import com.cardrace.cardrace_server.exceptions.IllegalMoveException;

import java.util.*;

public class Board {

    public final Marble[] spaces;
    public final Map<Types.Color, Marble[]> safeZones;
    public final Map<Types.Color, List<Marble>> reserves;
    public final Map<Types.Color, Integer> startPositions;
    private final int boardSize;

    /**
     * Constructs a new Board with the given colors.
     * Initializes the board spaces, safe zones, reserves, and start positions.
     *
     * @param colors The list of colors in the game
     */
    public Board(List<Types.Color> colors) {
        this.boardSize = colors.size() * 16;
        this.spaces = new Marble[boardSize];
        this.safeZones = new EnumMap<>(Types.Color.class);
        this.reserves = new EnumMap<>(Types.Color.class);
        this.startPositions = new EnumMap<>(Types.Color.class);

        initializeBoard(colors);
    }

    /**
     * Initializes the board by setting up safe zones, reserves, and start positions for each color.
     *
     * @param colors The list of colors in the game
     */
    private void initializeBoard(List<Types.Color> colors) {
        int startPosition = 0;
        for (Types.Color color : colors) {
            createSafeZone(color);
            createReserve(color);

            startPositions.put(color, startPosition);
            startPosition += 16;
        }
    }

    /**
     * Creates a safe zone for the specified color.
     *
     * @param color The color for which to create a safe zone
     */
    private void createSafeZone(Types.Color color) {
        safeZones.put(color, new Marble[4]);
    }

    /**
     * Creates a reserve for the specified color, initializing it with four marbles.
     *
     * @param color The color for which to create a reserve
     */
    private void createReserve(Types.Color color) {
        Types.MarbleType[] types = {Types.MarbleType.A, Types.MarbleType.B, Types.MarbleType.C, Types.MarbleType.D};

        ArrayList<Marble> reserve = new ArrayList<>();
        for (Types.MarbleType type : types) {
            reserve.add(new Marble(color, type, Types.MarbleState.PROTECTED));
        }
        reserves.put(color, reserve);
    }

    /**
     * Activates a marble by moving it from the reserve to its start position on the board.
     * If the start position is occupied, the occupying marble is sent back to its reserve.
     *
     * @param marble The marble to activate
     * @throws NoSuchElementException if the marble is not in the reserve
     */
    public void activateMarble(Marble marble) {
        Types.Color marbleColor = marble.getColor();

        List<Marble> reserveMarbles = reserves.get(marbleColor);
        if (!reserveMarbles.contains(marble)) {
            throw new NoSuchElementException("Marble not in reserve!");
        }
        reserveMarbles.remove(marble);
        int startPos = startPositions.get(marbleColor);
        if (spaces[startPos] != null) {
            Marble tenant = spaces[startPos];
            sendToReserve(tenant);
        }
        spaces[startPos] = marble;
    }

    /**
     * Finds the position of a marble on the board.
     *
     * @param marble The marble to find
     * @return The index of the marble in the spaces array
     * @throws NoSuchElementException if the marble is not on the board
     */
    public int findMarble(Marble marble) throws NoSuchElementException {
        for (int i = 0; i < boardSize; i++) {
            if (spaces[i] == marble) {
                return i;
            }
        }
        throw new NoSuchElementException("Marble not in play!");
    }

    /**
     * Check if marble is in reserve
     *
     * @param marble Marble to be checked.
     */
    public boolean inReserve(Marble marble) {
        Types.Color marbleColor = marble.getColor();

        List<Marble> reserve = reserves.get(marbleColor);
        return reserve.contains(marble);
    }

    /**
     * Swaps the positions of two marbles on the board.
     *
     * @param source The first marble to swap
     * @param target The second marble to swap
     * @throws IllegalMoveException if either marble is not on the board
     */
    public void swapMarble(Marble source, Marble target) throws IllegalMoveException {
        int pos1 = findMarble(source);
        int pos2 = findMarble(target);

        spaces[pos1] = target;
        spaces[pos2] = source;
    }

    /**
     * Moves a marble the given distance, handling collisions, safeZones, and bully status.
     *
     * @param marble The marble to move
     * @param distance The distance to move the marble
     * @param bully Whether this is a bully move (e.g., for a 7 card)
     * @throws IllegalMoveException if the move is invalid (e.g., landing on a protected marble)
     */
    public void moveMarble(Marble marble, int distance, boolean bully) throws IllegalMoveException {
        int startPosition = findMarble(marble);
        int currentPosition = startPosition;
        int remainingDistance = Math.abs(distance);
        boolean movingBackwards = distance < 0;

        while (remainingDistance > 0) {
            if (movingBackwards) {
                currentPosition = (currentPosition - 1 + boardSize) % boardSize;
            } else {
                currentPosition = (currentPosition + 1) % boardSize;
            }
            remainingDistance--;

            if (!movingBackwards && currentPosition == startPositions.get(marble.getColor())) {
                int safeZonePosition = remainingDistance - 1;
                Marble[] safeZone = safeZones.get(marble.getColor());
                if (safeZonePosition < 4 && safeZone[safeZonePosition] == null) {
                    spaces[startPosition] = null;
                    safeZone[safeZonePosition] = marble;
                    return;
                }
            }

            Marble occupyingMarble = spaces[currentPosition];
            if (occupyingMarble != null) {
                if (bully && occupyingMarble.getState() != Types.MarbleState.PROTECTED) {
                    sendToReserve(occupyingMarble);
                    spaces[currentPosition] = null;
                }
            }
        }

        Marble landingMarble = spaces[currentPosition];
        if (landingMarble != null) {
            if (landingMarble.getState() == Types.MarbleState.PROTECTED) {
                throw new IllegalMoveException("Cannot land on a protected marble");
            } else {
                sendToReserve(landingMarble);
            }
        }

        spaces[startPosition] = null;
        spaces[currentPosition] = marble;
    }

    /**
     * Sends a marble back to its reserve.
     *
     * @param marble The marble to send to reserve
     */
    private void sendToReserve(Marble marble) {
        reserves.get(marble.getColor()).add(marble);
    }

    /**
     * Retrieve a safe zone contents.
     *
     * @param color The color of the zone.
     */
    public Marble[] getSafeZone(Types.Color color) {
        return safeZones.get(color);
    }

}

