package com.cardrace.cardrace_server.model.game;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.IOException;
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


    @JsonDeserialize(using = ColorDeserializer.class)
    public enum Color {
        RED, BLUE, GREEN, YELLOW, PURPLE, ORANGE;

        @JsonCreator
        public static Color fromValue(String value) {
            return valueOf(value.toUpperCase());
        }
    }

    private static class ColorDeserializer extends JsonDeserializer<Color> {
        @Override
        public Color deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String value = p.getValueAsString();
            if (value == null) {
                return null;
            }
            return Color.fromValue(value);
        }
    }


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
        return switch (numPlayers) {
            case 2, 3 -> 12;
            case 4, 5, 6 -> 6;
            default -> 8;
        };
    }

}
