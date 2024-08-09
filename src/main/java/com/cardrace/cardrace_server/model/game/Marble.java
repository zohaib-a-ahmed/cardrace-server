package com.cardrace.cardrace_server.model.game;

public class Marble {

    public enum Color {
        RED, GREEN, ORANGE, BLUE, PURPLE, YELLOW
    }

    public enum Type {
        A, B, C, D
    }

    public enum State {
        PROTECTED, UNPROTECTED
    }

    public final Color color;
    public final Type type;
    private State state;


    public Marble(Color color, Type type) {
        this.color = color;
        this.type = type;
        this.state = State.PROTECTED;
    }

    public Color getColor() {
        return color;
    }

    public Type getType() {
        return type;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "Marble [color=" + color +  ", type=" + type + "]";
    }

    @Override
    public boolean equals(Object obj) {
        // Check if the object is the same instance
        if (this == obj) {
            return true;
        }

        // Check if the object is an instance of Marble
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        // Cast the object to Marble
        Marble other = (Marble) obj;

        // Compare the attributes
        return color == other.color &&
                type == other.type &&
                state == other.state;
    }

    @Override
    public int hashCode() {
        int result = color != null ? color.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (state != null ? state.hashCode() : 0);
        return result;
    }


}
