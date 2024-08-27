package com.cardrace.cardrace_server.model.game;

import java.util.HashMap;
import java.util.Map;

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

    public static boolean isValidCardValue(CardValue cardValue, int value) {
        switch (cardValue) {
            case TWO:
                return value == 2;
            case THREE:
                return value == 3;
            case FOUR:
                return value == 4 || value == -4;
            case FIVE:
                return value == 5;
            case SIX:
                return value == 6;
            case SEVEN:
                return value == 7;
            case EIGHT:
                return value == 8;
            case NINE:
                return value == 9;
            case TEN:
                return value == 10;
            case QUEEN:
                return value == 12;
            case KING:
                return value == 13;
            case ACE:
                return value == 1 || value == 11;
            default:
                return false;
        }
    }

    public static int getHandSize(int numPlayers) {
        switch (numPlayers) {
            case 2, 3:
                return 12;
            case 4, 5, 6:
                return 6;
            default:
                return 8;
        }
    }

}
