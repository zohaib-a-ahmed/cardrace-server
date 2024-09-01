package com.cardrace.cardrace_server.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.cardrace.cardrace_server.controller.SocketIOEventHandler;
import com.cardrace.cardrace_server.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Repository
public class UserRepository {

    private final DynamoDBMapper dynamoDBMapper;
    private static final Logger logger = LoggerFactory.getLogger(SocketIOEventHandler.class);

    @Autowired
    public UserRepository(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
    }

    public User save(User user) {
        dynamoDBMapper.save(user);
        return user;
    }

    public Optional<User> findById(String id) {
        User user = dynamoDBMapper.load(User.class, id);
        return Optional.ofNullable(user);
    }

    public Optional<User> findByUsername(String username) {
        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":val1", new AttributeValue().withS(username));

        DynamoDBQueryExpression<User> queryExpression = new DynamoDBQueryExpression<User>()
                .withIndexName("UsernameIndex")
                .withConsistentRead(false)
                .withKeyConditionExpression("username = :val1")
                .withExpressionAttributeValues(eav);

        List<User> users = dynamoDBMapper.query(User.class, queryExpression);

        if (users.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(users.get(0));
    }
    public void incrementGamesPlayed(String username) {
        try {
            Optional<User> userOptional = findByUsername(username);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                user.setGamesPlayed(user.getGamesPlayed() + 1);
                dynamoDBMapper.save(user);
            } else {
                logger.warn("User not found for username: {}", username);
            }
        } catch (Exception e) {
            logger.error("Error incrementing games played: {}", e.getMessage(), e);
            throw e;
        }
    }

    public void incrementWins(String username) {
        try {
            Optional<User> userOptional = findByUsername(username);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                user.setWins(user.getWins() + 1);
                dynamoDBMapper.save(user);
            } else {
                logger.warn("User not found for username: {}", username);
            }
        } catch (Exception e) {
            logger.error("Error incrementing wins: {}", e.getMessage(), e);
            throw e;
        }
    }

    public void incrementTurns(String username, int turnsTaken) {
        try {
            Optional<User> userOptional = findByUsername(username);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                user.setTurns(user.getTurns() + turnsTaken);
                dynamoDBMapper.save(user);
            } else {
                logger.warn("User not found for username: {}", username);
            }
        } catch (Exception e) {
            logger.error("Error incrementing turns: {}", e.getMessage(), e);
            throw e;
        }
    }

    public void delete(User user) {
        dynamoDBMapper.delete(user);
    }

    public List<User> findAll() {
        return dynamoDBMapper.scan(User.class, new DynamoDBScanExpression());
    }
}