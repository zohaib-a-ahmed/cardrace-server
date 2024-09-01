package com.cardrace.cardrace_server.controller;

import com.cardrace.cardrace_server.service.GameService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequestMapping("api/games")
@Controller
public class GameController {

    private final GameService gameService;
    private static final Logger logger = LoggerFactory.getLogger(GameController.class);

    @Autowired
    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostConstruct
    public void init() {
        logger.info("GameController initialized with mappings:");
        logger.info("/api/games/create (POST)");
        logger.info("/api/games/available/{gameId} (GET)");
    }

    @PostMapping("/create")
    public ResponseEntity<String> createGame(@RequestParam String gameName, @RequestParam Integer numPlayers) {
        logger.info("attempting to create game!");
        String gameId = gameService.createGame(gameName, numPlayers);
        logger.info("Game created with UUID: {}", gameId);
        return ResponseEntity.ok(gameId);
    }

    @GetMapping("/available/{gameId}")
    public ResponseEntity<Boolean> checkGameAvailability(@PathVariable String gameId) {
        boolean isAvailable = gameService.inLobby(gameId);
        logger.info("Check availability for gameId: {}", gameId);
        return ResponseEntity.ok(isAvailable);
    }
}