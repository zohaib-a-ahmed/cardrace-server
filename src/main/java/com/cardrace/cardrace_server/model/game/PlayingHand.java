package com.cardrace.cardrace_server.model.game;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class PlayingHand {

    public final List<PlayingCard> cards;

    public PlayingHand(Integer size) {
        this.cards = new ArrayList<>(size);
    }

    public void addCard(PlayingCard card) {
        cards.add(card);
    }

    public void removeCard(PlayingCard card) {
        if (cards.contains(card)){
            cards.remove(card);
        }
        throw new NoSuchElementException();
    }

    @Override
    public String toString() {
        return "PlayingHand{" +
                "cards=" + cards +
                '}';
    }
}

