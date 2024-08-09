package com.cardrace.cardrace_server.model.game;

public class PlayingCard {

    public enum Suit {
        HEARTS, DIAMONDS, CLUBS, SPADES
    }

    public enum Value {
        TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING, ACE
    }

    public final Value cardValue;
    public final Suit cardSuit;

    public PlayingCard(Value value, Suit suit) {
        this.cardValue = value;
        this.cardSuit = suit;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PlayingCard that = (PlayingCard) obj;
        return cardValue.equals(that.cardValue) && cardSuit.equals(that.cardSuit);
    }

    @Override
    public int hashCode() {
        return 31 * cardValue.hashCode() + cardSuit.hashCode();
    }

    @Override
    public String toString() {
        return cardValue + " of " + cardSuit;
    }
}

