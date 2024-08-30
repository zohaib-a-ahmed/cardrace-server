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
            logger.info("Attempting to increment games played for user: {}", username);
            Optional<User> userOptional = findByUsername(username);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                logger.info("Found user. Current games played: {}", user.getGamesPlayed());
                user.setGamesPlayed(user.getGamesPlayed() + 1);
                dynamoDBMapper.save(user);
                logger.info("Games played incremented. New value: {}", user.getGamesPlayed());
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
            logger.info("Attempting to increment wins for user: {}", username);
            Optional<User> userOptional = findByUsername(username);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                logger.info("Found user. Current wins: {}", user.getWins());
                user.setWins(user.getWins() + 1);
                dynamoDBMapper.save(user);
                logger.info("Wins incremented. New value: {}", user.getWins());
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
            logger.info("Attempting to increment turns for user: {}", username);
            Optional<User> userOptional = findByUsername(username);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                logger.info("Found user. Current turns: {}", user.getTurns());
                user.setTurns(user.getTurns() + turnsTaken);
                dynamoDBMapper.save(user);
                logger.info("Turns incremented. New value: {}", user.getTurns());
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