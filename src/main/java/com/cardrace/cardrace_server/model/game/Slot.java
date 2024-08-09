package com.cardrace.cardrace_server.model.game;

public class Slot {
    private Marble marble;

    public Slot() {
        this.marble = null;
    }

    public Marble getMarble() {
        return marble;
    }

    public void setMarble(Marble marble) {
        this.marble = marble;
    }

    public boolean hasMarble() {
        return marble != null;
    }

    @Override
    public String toString() {
        return "Slot{" +
                ", marble=" + marble +
                '}';
    }
}
