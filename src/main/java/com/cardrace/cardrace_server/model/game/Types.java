package com.cardrace.cardrace_server.model.game;

public final class Types {

    public enum CardSuit {
        HEARTS, DIAMONDS, CLUBS, SPADES, JOKER
    }

    public enum CardValue { TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING, ACE, JOKER }


    public enum MarbleState {
        PROTECTED, UNPROTECTED
    }

    public enum MarbleType {
        A, B, C, D
    }

    public enum Color { RED, BLUE, GREEN, YELLOW, PURPLE, ORANGE }

    public enum GameStatus {
        WAITING, IN_PROGRESS, COMPLETE, TERMINATED
    }

}
