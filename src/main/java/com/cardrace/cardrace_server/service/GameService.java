package com.cardrace.cardrace_server.service;

import com.cardrace.cardrace_server.controller.SocketIOEventHandler;
import com.cardrace.cardrace_server.dto.*;
import com.cardrace.cardrace_server.exceptions.IllegalMoveException;
import com.cardrace.cardrace_server.exceptions.InvalidMoveFormatException;
import com.cardrace.cardrace_server.exceptions.PlayerLimitException;
import com.cardrace.cardrace_server.model.game.Card;
import com.cardrace.cardrace_server.model.game.Game;
import com.cardrace.cardrace_server.model.game.Types;
import com.cardrace.cardrace_server.repository.RedisGameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class GameService {
    @Autowired
    private final RedisGameRepository gameRepository;
    private static final Logger logger = LoggerFactory.getLogger(SocketIOEventHandler.class);

    public GameService(RedisGameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public String createGame(String gameName, Integer numPlayers) {
        String gameId = UUID.randomUUID().toString().substring(0, 6);

        Game newGame = new Game(gameName, numPlayers);
        gameRepository.save(gameId, newGame);
        return gameId;
    }

    public void joinGame(String gameId, String playerId) throws PlayerLimitException {
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
        gameRepository.save(gameId, game);
    }

    public void leaveGame(String gameId, String playerId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        if (game.getStatus() == Types.GameStatus.IN_PROGRESS) {
            earlyTerminate(gameId, playerId);
        } else if (game.getStatus() == Types.GameStatus.WAITING && doesPlayerExist(gameId, playerId)) {
            game.removePlayer(playerId);
            if (game.getPlayers().isEmpty()) {
                earlyTerminate(gameId, playerId);
            } else { gameRepository.save(gameId, game); }
        }
    }

    public void deleteGame(String gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        gameRepository.delete(gameId);
    }

    public void applyMove(String gameId, MoveDTO move) throws IllegalMoveException {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        if (!move.isForfeit()) {
            if (game.getPlayerColor(move.getUsername()) == game.getCurrentPlayerColor()) {
                game.applyMove(move.getCard(), move.getSubstitute(), move.getDistances());
                game.incrementPlayerTurns(move.getUsername());
                if (game.hasWon(move.getUsername())) {
                    game.setStatus(Types.GameStatus.COMPLETE);
                    game.setWinner(move.getUsername());
                }
                game.setLastCard(move.getCard());
                game.updatePlayerHand(move.getUsername(), move.getCard());
            } else throw new IllegalMoveException("Not player's turn!");
        } else {
            game.clearHand(move.getUsername());
            game.setLastCard(null);
        }
        if (game.timeToDeal()) {
            game.dealOut();
        }
        game.nextTurn();
        gameRepository.save(gameId, game);
    }

    public void isValidMoveStructure(MoveDTO move) throws InvalidMoveFormatException {

        Card actingCard;

        if (move.isForfeit()) { return; }
        if (!Objects.nonNull(move.getCard()) || !Objects.nonNull(move.getUsername()) || !Objects.nonNull(move.getDistances())) {
            throw new InvalidMoveFormatException("Move is missing required data!");
        }
        if (move.getCard().cardValue == Types.CardValue.JOKER) {
            if (!Objects.nonNull(move.getSubstitute())) {
                throw new InvalidMoveFormatException("Joker Card requires substitute!");
            }
            actingCard = move.getSubstitute();
        } else {
            actingCard = move.getCard();
        }

        Map<Integer, Integer> distances = move.getDistances();

        switch (actingCard.cardValue) {
            case JACK -> {
                if (distances.size() != 2) {
                    throw new InvalidMoveFormatException("Jack move must involve exactly two marbles!");
                }
            }
            case SEVEN -> {
                if (distances.isEmpty()) {
                    throw new InvalidMoveFormatException("Seven move must involve at least one marble!");
                }
                int sum = distances.values().stream().mapToInt(Integer::intValue).sum();
                if (sum != 7) {
                    throw new InvalidMoveFormatException("Seven move distances must sum to 7!");
                }
            }
            default -> {
                if (distances.size() != 1) {
                    throw new InvalidMoveFormatException("Move must involve exactly one marble!");
                }
                int distance = distances.values().iterator().next();
                if (!Types.isValidCardValue(actingCard.cardValue, distance)) {
                    throw new InvalidMoveFormatException("Invalid distance for the given card!");
                }
            }
        }
    }

    public boolean inLobby(String gameId) {
        Optional<Game> game = gameRepository.findById(gameId);
        return game.isPresent() && game.get().getStatus() == Types.GameStatus.WAITING;
    }

    public boolean isTerminated(String gameId) {
        Optional<Game> game = gameRepository.findById(gameId);
        return game.isPresent() && game.get().getStatus() == Types.GameStatus.TERMINATED;
    }

    public boolean hasCompleted(String gameId) {
        Optional<Game> game = gameRepository.findById(gameId);
        return game.isPresent() && game.get().getStatus() == Types.GameStatus.COMPLETE;
    }

    public boolean doesPlayerExist(String gameId, String playerId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        return game.getPlayers().stream()
                .anyMatch(player -> player.equals(playerId));
    }

    public String getGameWinner(String gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));
        return game.getWinner();
    }

    public void earlyTerminate(String gameId, String playerId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        game.setStatus(Types.GameStatus.TERMINATED);
        game.setWinner(playerId);
        gameRepository.save(gameId, game);
    }

    public Map<String, Integer> getPlayerTurnInformation(String gameId){
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        HashMap<String, Integer> turnInformation = new HashMap<>();
        for (String player : game.getPlayers()) {
            turnInformation.put(player, game.getPlayerTurns(player));
        }

        return turnInformation;
    }

    public SpecificGameStateDTO getPlayerSpecificGameState(String gameId, String playerId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        return new SpecificGameStateDTO(game.gameName, game.getBoard(), game.getPlayers(), game.getPlayerColorMap(), game.getCurrentPlayerColor(), game.getLastCard(), game.getStatus(), game.getWinner(), playerId, game.getPlayerHand(playerId), game.getPlayerColor(playerId));
    }

    public WaitingGameStateDTO getWaitingGameState(String gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        return new WaitingGameStateDTO(game.getStatus(), game.gameName, game.getPlayers());
    }

    public EarlyTerminationDTO getTerminatedGameState(String gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        return new EarlyTerminationDTO(game.getWinner(), game.getStatus());
    }

    public boolean doesGameExist(String gameId) {
        Optional<Game> game = gameRepository.findById(gameId);
        return game.isPresent();
    }
}