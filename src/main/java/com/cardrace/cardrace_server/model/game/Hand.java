package com.cardrace.cardrace_server.model.game;

import com.cardrace.cardrace_server.controller.SocketIOEventHandler;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class Hand {

    private static final Logger logger = LoggerFactory.getLogger(SocketIOEventHandler.class);

    @JsonProperty
    private final List<Card> cards;

    @JsonCreator
    public Hand(@JsonProperty("cards") List<Card> cards) {
        this.cards = cards != null ? cards : new ArrayList<>();
    }

    public Hand(int size) {
        this.cards = new ArrayList<>(size);
    }

    public void addCard(Card card) {
        cards.add(card);
    }

    public void removeCard(Card card) {
        if (!cards.remove(card)) {
            throw new NoSuchElementException("Card not found in hand");
        }
    }

    public void forfeitCards() {
        cards.clear();
    }

    @JsonIgnore
    public int getNumCards() {
        return cards.size();
    }

    public List<Card> getCards() {
        return new ArrayList<>(cards);
    }

    @Override
    public String toString() {
        return "Hand{cards=" + cards + ", numCards=" + getNumCards() + '}';
    }
}