package com.cardrace.cardrace_server.model.game;
import java.util.Stack;
import java.util.Collections;
import java.util.Random;

import com.cardrace.cardrace_server.controller.SocketIOEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Deck {

    private static final Logger logger = LoggerFactory.getLogger(SocketIOEventHandler.class);

    private static final Types.CardSuit[] SUITS = {
            Types.CardSuit.HEARTS,
            Types.CardSuit.DIAMONDS,
            Types.CardSuit.CLUBS,
            Types.CardSuit.SPADES
    };

    private static final Types.CardValue[] VALUES = {
            Types.CardValue.TWO,
            Types.CardValue.THREE,
            Types.CardValue.FOUR,
            Types.CardValue.FIVE,
            Types.CardValue.SIX,
            Types.CardValue.SEVEN,
            Types.CardValue.EIGHT,
            Types.CardValue.NINE,
            Types.CardValue.TEN,
            Types.CardValue.JACK,
            Types.CardValue.QUEEN,
            Types.CardValue.KING,
            Types.CardValue.ACE
    };

    private int numDecks;
    private Stack<Card> playingDeck;
    private Random random;

    /**
     * Constructs a new Deck with the specified number of standard decks.
     * Initializes and shuffles the playing deck.
     *
     * @param numDecks The number of standard 52-card decks to include
     */
    public Deck(int numDecks) {
        this.numDecks = numDecks;
        this.playingDeck = new Stack<>();
        this.random = new Random();
        shuffle();
    }

    /**
     * Shuffles the deck by clearing it, adding all cards (including Jokers),
     * and then randomly shuffling the order.
     */
    public void shuffle() {
        playingDeck.clear();

        for (int i = 0; i < numDecks; i++) {
            for (Types.CardSuit suit : SUITS) {
                for (Types.CardValue value : VALUES) {
                    playingDeck.push(new Card(value, suit));
                }
            }
        }
        playingDeck.push(new Card(Types.CardValue.JOKER, Types.CardSuit.JOKER));
        playingDeck.push(new Card(Types.CardValue.JOKER, Types.CardSuit.JOKER));

        Collections.shuffle(playingDeck, random);
    }

    /**
     * Deals a hand of cards from the deck.
     * If the deck is empty, it reshuffles before dealing.
     *
     * @param handSize The number of cards to deal
     * @return A new Hand object containing the dealt cards
     */
    public Hand dealHand(int handSize) {
        Hand hand = new Hand(handSize);
        if (playingDeck.isEmpty()) {
            shuffle();
        }

        for (int i = 0; i < handSize; i++) {
            if (playingDeck.isEmpty()) {
                shuffle();
            }
            if (!playingDeck.isEmpty()) {
                hand.addCard(playingDeck.pop());
            }
        }
        logger.info(playingDeck.toString());
        return hand;
    }
}
