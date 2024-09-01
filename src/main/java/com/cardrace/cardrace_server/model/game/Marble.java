package com.cardrace.cardrace_server.model.game;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class Marble {
    @JsonProperty
    private Types.MarbleState state;
    @JsonProperty
    private final Types.Color color;
    @JsonProperty
    private final Types.MarbleType type;
    public final int id;

    @JsonCreator
    public Marble(
            @JsonProperty("id") int id,
            @JsonProperty("color") Types.Color color,
            @JsonProperty("type") Types.MarbleType type,
            @JsonProperty("state") Types.MarbleState state) {
        this.id = id;
        this.color = color;
        this.type = type;
        this.state = state;
    }

    public Types.Color getColor() {
        return color;
    }

    public Types.MarbleState getState() {
        return state;
    }

    public Types.MarbleType getType() {
        return type;
    }

    public void setState(Types.MarbleState state) {
        this.state = state;
    }
}
