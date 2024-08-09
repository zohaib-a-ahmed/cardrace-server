package com.cardrace.cardrace_server.model.game;
import java.util.Stack;
import java.util.Collections;
import java.util.Random;

public class Deck {

    private static final PlayingCard.Suit[] SUITS = {
            PlayingCard.Suit.HEARTS,
            PlayingCard.Suit.DIAMONDS,
            PlayingCard.Suit.CLUBS,
            PlayingCard.Suit.SPADES
    };

    private static final PlayingCard.Value[] VALUES = {
            PlayingCard.Value.TWO,
            PlayingCard.Value.THREE,
            PlayingCard.Value.FOUR,
            PlayingCard.Value.FIVE,
            PlayingCard.Value.SIX,
            PlayingCard.Value.SEVEN,
            PlayingCard.Value.EIGHT,
            PlayingCard.Value.NINE,
            PlayingCard.Value.TEN,
            PlayingCard.Value.JACK,
            PlayingCard.Value.QUEEN,
            PlayingCard.Value.KING,
            PlayingCard.Value.ACE
    };

    private int numDecks;
    private Stack<PlayingCard> playingDeck;
    private Random random;

    public Deck(int numDecks) {
        this.numDecks = numDecks;
        this.playingDeck = new Stack<>();
        this.random = new Random();
        shuffle();
    }

    public void shuffle() {
        playingDeck.clear();

        for (int i = 0; i < numDecks; i++) {
            for (PlayingCard.Suit suit : SUITS) {
                for (PlayingCard.Value value : VALUES) {
                    playingDeck.push(new PlayingCard(value, suit));
                }
            }
        }

        Collections.shuffle(playingDeck, random);
    }

    public PlayingHand dealHand(int handSize) {
        PlayingHand hand = new PlayingHand(handSize);
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

        return hand;
    }
}
