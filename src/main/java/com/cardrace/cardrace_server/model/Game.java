package com.cardrace.cardrace_server.model;

import com.cardrace.cardrace_server.model.game.GameState;
import com.cardrace.cardrace_server.model.game.HandState;

import java.util.ArrayList;
import java.util.HashMap;

public class Game {

    private GameState state;
    private HashMap<String, HandState> handStateMap;
    private String gameName;
    private ArrayList<String> players;
    private final Integer numPlayers;

    public Game (String gameName, Integer numPlayers) {

        this.state = new GameState(gameName, numPlayers);
        this.gameName = gameName;
        this.numPlayers = numPlayers;
    }

    public Integer getNumPlayers() {
        return numPlayers;
    }

    public ArrayList<String> getPlayers() {
        return players;
    }

    public GameState getState() {
        return state;
    }

    public void setState(GameState state) {
        this.state = state;
    }
}
