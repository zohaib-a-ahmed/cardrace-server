package com.cardrace.cardrace_server.model.game;

public class HandState {

    private PlayingHand hand;
    public final Marble.Color color;

    public HandState(Marble.Color color) { this.color = color; }

    public PlayingHand getHand() {
        return hand;
    }

    public void setHand(PlayingHand hand) {
        this.hand = hand;
    }
}
