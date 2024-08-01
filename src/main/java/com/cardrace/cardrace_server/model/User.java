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

    // Default constructor
    public User() {}

    // Constructor with fields
    public User(String id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
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
}