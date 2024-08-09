package com.cardrace.cardrace_server.model.game;

public class Player {

    final public String username;

    final public Marble.Color color;

    public Player (String username, Marble.Color color) {
        this.username = username;
        this.color = color;
    }
}
