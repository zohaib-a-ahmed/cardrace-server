package com.cardrace.cardrace_server.dto;

import jakarta.validation.constraints.NotBlank;
public class GameCreationRequest {
    @NotBlank
    private String gameName;
    @NotBlank
    private Integer numPlayers;

    public GameCreationRequest (String name, Integer players) {
        this.gameName = name;
        this.numPlayers = players;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String name) {
        this.gameName = name;
    }

    public Integer getNumPlayers() {
        return numPlayers;
    }

    public void setPassword(Integer num) {
        this.numPlayers = num;
    }
}
