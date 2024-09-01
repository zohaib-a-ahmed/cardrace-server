package com.cardrace.cardrace_server.service;

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

@Service
public class GameService {
    @Autowired
    private final RedisGameRepository gameRepository;

    public GameService(RedisGameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    /**
     * Creates a new game with the given name and number of players.
     *
     * @param gameName   The name of the game
     * @param numPlayers The number of players for the game
     * @return A unique game ID for the created game
     */
    public String createGame(String gameName, Integer numPlayers) {
        String gameId = UUID.randomUUID().toString().substring(0, 6);

        Game newGame = new Game(gameName, numPlayers);
        gameRepository.save(gameId, newGame);
        return gameId;
    }

    /**
     * Adds a player to an existing game.
     *
     * @param gameId   The ID of the game to join
     * @param playerId The ID of the player joining the game
     * @throws PlayerLimitException If the game is full or not in waiting status
     */
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

    /**
     * Removes a player from a game. If the game is in progress, it will be terminated early.
     *
     * @param gameId   The ID of the game
     * @param playerId The ID of the player leaving the game
     */
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

    /**
     * Deletes a game from the repository.
     *
     * @param gameId The ID of the game to delete
     */
    public void deleteGame(String gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        gameRepository.delete(gameId);
    }

    /**
     * Applies a move to the game state.
     *
     * @param gameId The ID of the game
     * @param move   The move to apply
     * @throws IllegalMoveException If the move is not allowed
     */
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

    /**
     * Validates the structure of a move.
     *
     * @param move The move to validate
     * @throws InvalidMoveFormatException If the move structure is invalid
     */
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

    /**
     * Checks if a game is in the lobby (waiting) state.
     *
     * @param gameId The ID of the game to check
     * @return true if the game is in the lobby, false otherwise
     */
    public boolean inLobby(String gameId) {
        Optional<Game> game = gameRepository.findById(gameId);
        return game.isPresent() && game.get().getStatus() == Types.GameStatus.WAITING;
    }

    /**
     * Checks if a game has been terminated early.
     *
     * @param gameId The ID of the game to check
     * @return true if the game has been terminated, false otherwise
     */
    public boolean isTerminated(String gameId) {
        Optional<Game> game = gameRepository.findById(gameId);
        return game.isPresent() && game.get().getStatus() == Types.GameStatus.TERMINATED;
    }

    /**
     * Checks if a game has been completed normally.
     *
     * @param gameId The ID of the game to check
     * @return true if the game has been completed, false otherwise
     */
    public boolean hasCompleted(String gameId) {
        Optional<Game> game = gameRepository.findById(gameId);
        return game.isPresent() && game.get().getStatus() == Types.GameStatus.COMPLETE;
    }

    /**
     * Checks if a player exists in a game.
     *
     * @param gameId   The ID of the game
     * @param playerId The ID of the player to check
     * @return true if the player exists in the game, false otherwise
     */
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

    /**
     * Terminates a game early.
     *
     * @param gameId   The ID of the game to terminate
     * @param playerId The ID of the player causing the termination
     */
    public void earlyTerminate(String gameId, String playerId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        game.setStatus(Types.GameStatus.TERMINATED);
        game.setWinner(playerId);
        gameRepository.save(gameId, game);
    }

    /**
     * Retrieves the turn information for all players in a game.
     *
     * @param gameId The ID of the game
     * @return A map of player IDs to their turn counts
     */
    public Map<String, Integer> getPlayerTurnInformation(String gameId){
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        HashMap<String, Integer> turnInformation = new HashMap<>();
        for (String player : game.getPlayers()) {
            turnInformation.put(player, game.getPlayerTurns(player));
        }

        return turnInformation;
    }

    /**
     * Retrieves the game state specific to a player.
     *
     * @param gameId   The ID of the game
     * @param playerId The ID of the player
     * @return A DTO containing the game state specific to the player
     */
    public SpecificGameStateDTO getPlayerSpecificGameState(String gameId, String playerId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        return new SpecificGameStateDTO(game.gameName, game.getBoard(), game.getPlayers(), game.getPlayerColorMap(), game.getCurrentPlayerColor(), game.getLastCard(), game.getStatus(), game.getWinner(), playerId, game.getPlayerHand(playerId), game.getPlayerColor(playerId));
    }

    /**
     * Retrieves the game state for a game in the waiting state.
     *
     * @param gameId The ID of the game
     * @return A DTO containing the waiting game state
     */
    public WaitingGameStateDTO getWaitingGameState(String gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        return new WaitingGameStateDTO(game.getStatus(), game.gameName, game.getPlayers());
    }

    /**
     * Retrieves the game state for a terminated game.
     *
     * @param gameId The ID of the game
     * @return A DTO containing the terminated game state
     */
    public EarlyTerminationDTO getTerminatedGameState(String gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        return new EarlyTerminationDTO(game.getWinner(), game.getStatus());
    }

    /**
     * Checks if a game exists.
     *
     * @param gameId The ID of the game to check
     * @return true if the game exists, false otherwise
     */
    public boolean doesGameExist(String gameId) {
        Optional<Game> game = gameRepository.findById(gameId);
        return game.isPresent();
    }
}