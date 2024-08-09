package com.cardrace.cardrace_server.model.game;

import java.util.NoSuchElementException;

public class Board {

    private final Slot[] slots;

    public Board(int numberOfPositions) {
        slots = new Slot[numberOfPositions];
        for (int i = 0; i < numberOfPositions; i++) {
            slots[i] = new Slot();
        }
    }

    public Slot[] getSlots() {
        return slots;
    }

    public Slot getSlot(int position) {
        if (position < 0 || position >= slots.length) {
            throw new IndexOutOfBoundsException("Invalid slot position.");
        }
        return slots[position];
    }

    public Integer findPosition(Marble target) {
        for (int i = 0; i < slots.length; i++) {
            if (slots[i].hasMarble() && slots[i].getMarble() == target) {
                return i;
            }
        }
        throw new NoSuchElementException("Marble is not on game board.");
    }

    public void printBoard() {
        for (Slot slot : slots) {
            System.out.println(slot);
        }
    }
}

