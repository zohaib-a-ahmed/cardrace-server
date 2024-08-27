package com.cardrace.cardrace_server.model.game;

import java.util.Objects;

public class Marble {

    private Types.MarbleState state;
    private final Types.Color color;
    private final Types.MarbleType type;
    public final int id;

    public Marble (int id, Types.Color color, Types.MarbleType type, Types.MarbleState state) {
        this.color = color;
        this.type = type;
        this.state = state;
        this.id = id;
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
