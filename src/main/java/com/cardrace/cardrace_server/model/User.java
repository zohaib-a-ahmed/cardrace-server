package com.cardrace.cardrace_server.model;

import com.amazonaws.services.dynamodbv2.datamodeling.*;

@DynamoDBTable(tableName = "Users")
public class User {

    @DynamoDBHashKey
    @DynamoDBAttribute
    private String id;

    @DynamoDBAttribute
    @DynamoDBIndexHashKey(globalSecondaryIndexName = "UsernameIndex")
    private String username;

    @DynamoDBAttribute
    private String password;

    @DynamoDBAttribute
    private int gamesPlayed;

    @DynamoDBAttribute
    private int wins;

    @DynamoDBAttribute
    private int turns;

    // Default constructor
    public User() {}

    // Constructor with fields
    public User(String id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.gamesPlayed = 0;
        this.wins = 0;
        this.turns = 0;
    }

    // Getters and Setters
    @DynamoDBHashKey
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @DynamoDBAttribute
    @DynamoDBIndexHashKey(globalSecondaryIndexName = "UsernameIndex")
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @DynamoDBAttribute
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @DynamoDBAttribute
    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    @DynamoDBAttribute
    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    @DynamoDBAttribute
    public int getTurns() {
        return turns;
    }

    public void setTurns(int turns) {
        this.turns = turns;
    }
}