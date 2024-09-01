package com.cardrace.cardrace_server.controller;

import com.cardrace.cardrace_server.dto.MoveDTO;
import com.cardrace.cardrace_server.exceptions.IllegalMoveException;
import com.cardrace.cardrace_server.exceptions.InvalidMoveFormatException;
import com.cardrace.cardrace_server.exceptions.PlayerLimitException;
import com.cardrace.cardrace_server.service.GameService;
import com.cardrace.cardrace_server.service.JwtService;
import com.cardrace.cardrace_server.service.UserService;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.corundumstudio.socketio.HandshakeData;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.Objects;

@Component
public class SocketIOEventHandler {

    private final SocketIOServer server;
    private final GameService gameService;
    private final JwtService jwtService;
    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(SocketIOEventHandler.class);

    @Autowired
    public SocketIOEventHandler(SocketIOServer server, GameService gameService, JwtService jwtService, UserService userService) {
        this.server = server;
        this.gameService = gameService;
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @PostConstruct
    public void init() {
        server.addConnectListener(onConnected());
        server.addDisconnectListener(onDisconnected());
        server.addEventListener("move", MoveDTO.class, onMakeMove());
    }

    private ConnectListener onConnected() {
        return (client) -> {
            HandshakeData handshakeData = client.getHandshakeData();
            String token = handshakeData.getSingleUrlParam("token");
            String gameId = handshakeData.getSingleUrlParam("gameId");

            if (token == null || token.isEmpty() || gameId == null || gameId.isEmpty()) {
                client.disconnect();
                return;
            }

            try {
                String username = jwtService.getUsernameFromToken(token);
                if (jwtService.validateToken(token, username)) {

                    client.set("username", username);
                    client.set("gameId", gameId);
                    client.joinRoom(gameId);
                    if (!gameService.doesPlayerExist(gameId, username)) {
                        try {
                            gameService.joinGame(gameId, username);
                            broadcastGameState(gameId);
                        } catch (PlayerLimitException e) {
                            client.disconnect();
                        }
                    }
                } else {
                    client.sendEvent("authenticationStatus", "failed");
                    client.disconnect();
                }
            } catch (Exception e) {
                logger.error("Error during authentication", e);
                client.sendEvent("authenticationStatus", "error");
                client.disconnect();
            }
        };
    }

    private DisconnectListener onDisconnected() {
        return (client) -> {
            HandshakeData handshakeData = client.getHandshakeData();
            String gameId = handshakeData.getSingleUrlParam("gameId");
            String username = client.get("username");

            logger.info("Client disconnected: " + client.getSessionId() + ", username: " + username);

            if (gameService.doesGameExist(gameId)) {
                gameService.leaveGame(gameId, username);
                broadcastGameState(gameId);
                if (gameService.isTerminated(gameId)) {
                    gameService.deleteGame(gameId);
                }
            }
            client.disconnect();
        };
    }

    private DataListener<MoveDTO> onMakeMove() {
        return (client, data, ackSender) -> {
            String username = client.get("username");
            String gameId = client.get("gameId");

            if (gameService.doesGameExist(gameId)) {
                if (!gameService.hasCompleted(gameId)) {
                    try {
                        gameService.isValidMoveStructure(data);
                    } catch (InvalidMoveFormatException e) {
                        client.sendEvent("moveResult", "Error processing move: " + e.getMessage());
                        return;
                    }

                    try {
                        gameService.applyMove(gameId, data);
                        broadcastGameState(gameId);
                        if (gameService.hasCompleted(gameId)) {
                            handlePlayerStatUpdates(gameId);
                            gameService.deleteGame(gameId);
                        }
                    } catch (IllegalMoveException e) {
                        logger.error("Error processing move", e);
                        client.sendEvent("moveResult", "Error processing move: " + e.getMessage());
                    }
                }
            }
        };
    }

    private void handlePlayerStatUpdates(String gameId) {
        Map<String, Integer> turnInformation = gameService.getPlayerTurnInformation(gameId);
        String winner = gameService.getGameWinner(gameId);

        for (Map.Entry<String, Integer> entry : turnInformation.entrySet()) {
            String player = entry.getKey();

            userService.incrementGamesPlayed(player);
            userService.incrementTurns(player, entry.getValue());
            if (Objects.equals(player, winner)) { userService.incrementWins(player); }
        }
    }

    private void broadcastGameState(String gameId) {
        for (SocketIOClient client : server.getRoomOperations(gameId).getClients()) {
            sendGameState(client);
        }
    }
    private void sendGameState(SocketIOClient client) {
        String username = client.get("username");
        String gameId = client.get("gameId");

        if (gameService.inLobby(gameId)) {
            client.sendEvent("gameState", gameService.getWaitingGameState(gameId));
        } else if (gameService.isTerminated(gameId)) {
            client.sendEvent("gameState", gameService.getTerminatedGameState(gameId));
        } else {
            client.sendEvent("gameState", gameService.getPlayerSpecificGameState(gameId, username));
        }
    }
}