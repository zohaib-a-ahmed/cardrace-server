package com.cardrace.cardrace_server.model.game;

public class Card {
    public final Types.CardValue cardValue;
    public final Types.CardSuit cardSuit;

    public Card(Types.CardValue value, Types.CardSuit suit) {
        this.cardValue = value;
        this.cardSuit = suit;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Card that = (Card) obj;
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