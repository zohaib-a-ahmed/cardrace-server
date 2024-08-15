package com.cardrace.cardrace_server.service;

import com.cardrace.cardrace_server.dto.GameStateDTO;
import com.cardrace.cardrace_server.dto.MoveDTO;
import com.cardrace.cardrace_server.dto.PlayerStateDTO;
import com.cardrace.cardrace_server.exceptions.IllegalMoveException;
import com.cardrace.cardrace_server.exceptions.PlayerLimitException;
import com.cardrace.cardrace_server.model.game.Board;
import com.cardrace.cardrace_server.model.game.Game;
import com.cardrace.cardrace_server.model.game.Types;
import com.cardrace.cardrace_server.repository.InMemoryGameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Service
public class GameService {
    @Autowired
    private final InMemoryGameRepository gameRepository;

    public GameService(InMemoryGameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public String createGame(String gameName, Integer numPlayers) {
        String gameId = UUID.randomUUID().toString().substring(0, 6);

        Game newGame = new Game(gameName, numPlayers);
        gameRepository.save(gameId, newGame);
        return gameId;
    }

    public Game joinGame(String gameId, String playerId) throws PlayerLimitException {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        if (game.getStatus() == Types.GameStatus.WAITING) {
            game.addPlayer(playerId);
            if (game.getNumCurrPlayers() == game.numPlayers) {
                game.initializeGame();
            }
        } else {
            throw new PlayerLimitException("Game in progress or complete.");
        }
        return gameRepository.save(gameId, game);
    }

    public Game applyMove(String gameId, MoveDTO move) throws IllegalMoveException {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        if (!move.forfeit()) {
            game.applyMove(move.card(), move.substitute(), move.distances());
            if (game.hasWon(move.username())) {
                game.setStatus(Types.GameStatus.COMPLETE);
                game.setWinner(move.username());
                return gameRepository.save(gameId, game);
            }
            game.setLastCard(move.card());
            game.updatePlayerHand(move.username(), move.card());
            if (game.timeToDeal()) {
                game.dealOut();
            }
        } else {
            game.clearHand(move.username());
        }

        game.nextTurn();
        return gameRepository.save(gameId, game);
    }

    public boolean checkAvailability(String gameId) {
        Optional<Game> game = gameRepository.findById(gameId);
        return game.isPresent() && game.get().getStatus() == Types.GameStatus.WAITING;
    }
    public GameStateDTO getGameState(String gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        return new GameStateDTO(game.getBoard(), game.getPlayers(), game.getCurrentPlayerColor(), game.getLastCard(), game.getStatus(), game.getWinner());
    }

    public PlayerStateDTO getPlayerState(String gameId, String playerId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        return new PlayerStateDTO(playerId, game.getPlayerColor(playerId), game.getPlayerHand(playerId));
    }
}