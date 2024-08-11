package com.cardrace.cardrace_server.model.game;

import java.util.Objects;

public class Marble {

    private Types.MarbleState state;
    private final Types.Color color;
    private final Types.MarbleType type;

    public Marble (Types.Color color, Types.MarbleType type, Types.MarbleState state) {
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Marble other = (Marble) obj;

        return this.color.equals(other.color) &&
                this.type.equals(other.type) &&
                this.state.equals(other.state);
    }

    public int hashCode() {
        return Objects.hash(color, type, state);
    }

}
