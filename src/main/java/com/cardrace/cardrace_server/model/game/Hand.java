package com.cardrace.cardrace_server.model.game;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class Hand {

    public final List<Card> cards;

    /**
     * Constructs a new Hand with the specified initial size.
     *
     * @param size The initial size of the hand
     */
    public Hand(Integer size) {
        this.cards = new ArrayList<>(size);
    }

    /**
     * Adds a card to the hand.
     *
     * @param card The card to add to the hand
     */
    public void addCard(Card card) {
        cards.add(card);
    }

    /**
     * Removes a specific card from the hand.
     *
     * @param card The card to remove from the hand
     * @throws NoSuchElementException if the card is not in the hand
     */
    public void removeCard(Card card) {
        if (cards.contains(card)){
            cards.remove(card);
        } else {
            throw new NoSuchElementException("Card not found in hand");
        }
    }

    /**
     * Clear hand if cards are forfeited.
     */
    public void forfeitCards() {
        for(int i = 0; i < getNumCards(); i++) {
            cards.remove(i);
        }
    }

    /**
     * Check number of cards within hand.
     *
     * @return Amount of cards within hand.
     */
    public int getNumCards() {
        return cards.size();
    }

    @Override
    public String toString() {
        return "PlayingHand{" +
                "cards=" + cards +
                '}';
    }
}

