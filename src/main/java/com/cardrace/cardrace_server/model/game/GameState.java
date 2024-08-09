package com.cardrace.cardrace_server.model.game;

import java.util.ArrayList;

public class GameState {

    private Board board;
    private ArrayList<Player> players;
    private Marble.Color turn;
    private PlayingCard lastCard;
    private Integer numPlayers;
    private String gameName;

    public enum Status {
        WAITING, IN_PROGRESS, COMPLETE
    }
    private Status status;

    public GameState(String gameName, Integer numPlayers) {

        this.board = new Board(numPlayers);
        this.status = Status.WAITING;
        this.lastCard = null;
        this.turn = null;
        this.players = new ArrayList<Player>();
        this.numPlayers = numPlayers;
        this.gameName = gameName;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
    public Board getBoard() {
        return board;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public Marble.Color getCurrentTurn() {
        return turn;
    }

    public PlayingCard getLastCard() {
        return lastCard;
    }

    public void setLastCard(PlayingCard lastCard) {
        this.lastCard = lastCard;
    }

    public void setTurn(Marble.Color turn) {
        this.turn = turn;
    }

    public void setNumPlayers(Integer numPlayers) {
        this.numPlayers = numPlayers;
    }

    public Integer getNumPlayers() {
        return numPlayers;
    }

    public void setPlayers(ArrayList<Player> players) {
        this.players = players;
    }

    public void setBoard(Board board) {
        this.board = board;
    }
}
