package com.cardrace.cardrace_server.model.game;

import com.cardrace.cardrace_server.exceptions.IllegalMoveException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


import java.util.*;

public class Board {
    @JsonProperty
    public final Map<Integer, Marble> marbles;
    @JsonProperty
    public final Integer[] spaces;
    @JsonProperty
    public final Map<Types.Color, Integer[]> safeZones;
    @JsonProperty
    public final Map<Types.Color, List<Integer>> reserves;
    @JsonProperty
    public final Map<Types.Color, Integer> startPositions;
    @JsonProperty
    private final int boardSize;

    public Board(List<Types.Color> colors) {
        this.boardSize = colors.size() * 16;
        this.spaces = new Integer[boardSize];
        this.safeZones = new EnumMap<>(Types.Color.class);
        this.reserves = new EnumMap<>(Types.Color.class);
        this.startPositions = new EnumMap<>(Types.Color.class);
        this.marbles = new HashMap<>();

        initializeBoard(colors);
    }

    @JsonCreator
    public Board(
            @JsonProperty("marbles") Map<Integer, Marble> marbles,
            @JsonProperty("spaces") Integer[] spaces,
            @JsonProperty("safeZones") Map<Types.Color, Integer[]> safeZones,
            @JsonProperty("reserves") Map<Types.Color, List<Integer>> reserves,
            @JsonProperty("startPositions") Map<Types.Color, Integer> startPositions,
            @JsonProperty("boardSize") int boardSize
    ) {
        this.marbles = marbles;
        this.spaces = spaces;
        this.safeZones = safeZones;
        this.reserves = reserves;
        this.startPositions = startPositions;
        this.boardSize = boardSize;
    }

    private void initializeBoard(List<Types.Color> colors) {
        int startPosition = 0;
        int marbleId = 0;
        for (Types.Color color : colors) {
            createSafeZone(color);
            createReserve(color, marbleId);
            marbleId += 4;

            startPositions.put(color, startPosition);
            startPosition += 16;
        }
    }

    private void createSafeZone(Types.Color color) {
        safeZones.put(color, new Integer[4]);
    }

    private void createReserve(Types.Color color, int startId) {
        Types.MarbleType[] types = {Types.MarbleType.A, Types.MarbleType.B, Types.MarbleType.C, Types.MarbleType.D};

        ArrayList<Integer> reserve = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            int id = startId + i;
            Marble marble = new Marble(id, color, types[i], Types.MarbleState.PROTECTED);
            marbles.put(id, marble);
            reserve.add(id);
        }
        reserves.put(color, reserve);
    }

    public void activateMarble(int marbleId) throws IllegalMoveException {
        Marble marble = marbles.get(marbleId);
        marble.setState(Types.MarbleState.PROTECTED);
        Types.Color marbleColor = marble.getColor();

        List<Integer> reserveMarbles = reserves.get(marbleColor);
        if (!reserveMarbles.contains(marbleId)) {
            throw new NoSuchElementException("Marble not in reserve!");
        }
        reserveMarbles.remove(Integer.valueOf(marbleId));
        int startPos = startPositions.get(marbleColor);
        if (spaces[startPos] != null) {
            Integer tenantId = spaces[startPos];
            Marble tenantMarble = marbles.get(tenantId);
            if (tenantMarble.getColor() == marbleColor && tenantMarble.getState() == Types.MarbleState.PROTECTED) {
                throw new IllegalMoveException("Cannot activate a new marble onto a protected one!");
            } else { sendToReserve(tenantId); }
        }
        spaces[startPos] = marbleId;
    }

    public int findMarble(int marbleId) throws NoSuchElementException {
        for (int i = 0; i < boardSize; i++) {
            if (spaces[i] != null && spaces[i].equals(marbleId)) {
                return i;
            }
        }
        throw new NoSuchElementException("Marble not in play!");
    }

    public boolean inReserve(int marbleId) {
        Marble marble = marbles.get(marbleId);
        Types.Color marbleColor = marble.getColor();

        List<Integer> reserve = reserves.get(marbleColor);
        return reserve.contains(marbleId);
    }

    public void swapMarble(int sourceId, int targetId) throws IllegalMoveException {
        int pos1 = findMarble(sourceId);
        int pos2 = findMarble(targetId);

        spaces[pos1] = targetId;
        spaces[pos2] = sourceId;
    }

    public void moveMarble(int marbleId, int distance, boolean bully) throws IllegalMoveException {

        int startPosition = findMarble(marbleId);
        int currentPosition = startPosition;
        int remainingDistance = Math.abs(distance);
        boolean movingBackwards = distance < 0;
        Marble marble = marbles.get(marbleId);

        while (remainingDistance > 0) {

            if (!movingBackwards && currentPosition == startPositions.get(marble.getColor()) && marble.getState() != Types.MarbleState.PROTECTED) {
                int safeZonePosition = remainingDistance - 1;
                Integer[] safeZone = safeZones.get(marble.getColor());
                if (safeZonePosition < 4 && safeZone[safeZonePosition] == null) {
                    spaces[startPosition] = null;
                    safeZone[safeZonePosition] = marbleId;
                    return;
                }
            }

            if (movingBackwards) {
                currentPosition = (currentPosition - 1 + boardSize) % boardSize;
            } else {
                currentPosition = (currentPosition + 1) % boardSize;
            }
            remainingDistance--;

            // Check for any occupying marbles
            Integer occupyingMarbleId = spaces[currentPosition];
            if (occupyingMarbleId != null) {
                Marble occupyingMarble = marbles.get(occupyingMarbleId);
                if (bully && occupyingMarble.getState() != Types.MarbleState.PROTECTED) {
                    sendToReserve(occupyingMarbleId);
                    spaces[currentPosition] = null;
                }
            }
        }
        Integer landingMarbleId = spaces[currentPosition];
        if (landingMarbleId != null) {
            Marble landingMarble = marbles.get(landingMarbleId);
            if (landingMarble.getState() == Types.MarbleState.PROTECTED) {
                throw new IllegalMoveException("Cannot land on a protected marble");
            } else {
                sendToReserve(landingMarbleId);
            }
        }
        spaces[startPosition] = null;
        spaces[currentPosition] = marbleId;
    }

    private void sendToReserve(int marbleId) {
        Marble marble = marbles.get(marbleId);
        reserves.get(marble.getColor()).add(marbleId);
    }

    public Integer[] getSafeZone(Types.Color color) {
        return safeZones.get(color);
    }

    public Map<Integer, Marble> getMarbles() {
        return marbles;
    }
}