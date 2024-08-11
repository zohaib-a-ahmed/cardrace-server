package com.cardrace.cardrace_server.model.game;

public class Player {

    private final String username;
    private final Integer id;
    private final Types.Color color;

    public Player (Integer id, String username, Types.Color color) {
        this.id = id;
        this.username = username;
        this.color = color;
    }

    public Types.Color getColor() {
        return color;
    }

    public String getUsername() {
        return username;
    }

    public Integer getId() {
        return id;
    }
}
