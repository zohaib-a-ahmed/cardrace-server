package com.cardrace.cardrace_server.service;

import com.cardrace.cardrace_server.model.User;
import com.cardrace.cardrace_server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public Optional<User> findUserById(String id) {
        return userRepository.findById(id);
    }

    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public void incrementGamesPlayed(String userId) {
        userRepository.incrementGamesPlayed(userId);
    }

    public void incrementWins(String userId) {
        userRepository.incrementWins(userId);
    }

    public void incrementTurns(String userId, int turnsTaken) {
        userRepository.incrementTurns(userId, turnsTaken);
    }

    public User getUserProfile(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        return user.orElse(null);
    }
}