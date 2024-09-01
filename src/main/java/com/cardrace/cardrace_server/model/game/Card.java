package com.cardrace.cardrace_server.model.game;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Card {
    @JsonProperty
    public final Types.CardValue cardValue;
    @JsonProperty
    public final Types.CardSuit cardSuit;

    @JsonCreator
    public Card(
            @JsonProperty("cardValue") Types.CardValue cardValue,
            @JsonProperty("cardSuit") Types.CardSuit cardSuit) {
        this.cardValue = cardValue;
        this.cardSuit = cardSuit;
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