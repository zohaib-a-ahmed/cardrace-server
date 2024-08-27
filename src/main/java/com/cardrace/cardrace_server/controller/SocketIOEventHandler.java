package com.cardrace.cardrace_server.controller;

import com.cardrace.cardrace_server.dto.EarlyTerminationDTO;
import com.cardrace.cardrace_server.dto.MoveDTO;
import com.cardrace.cardrace_server.exceptions.IllegalMoveException;
import com.cardrace.cardrace_server.exceptions.InvalidMoveFormatException;
import com.cardrace.cardrace_server.exceptions.PlayerLimitException;
import com.cardrace.cardrace_server.security.RequestLoggingFilter;
import com.cardrace.cardrace_server.service.GameService;
import com.cardrace.cardrace_server.service.JwtService;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.corundumstudio.socketio.HandshakeData;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.apache.tomcat.util.json.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class SocketIOEventHandler {

    private final SocketIOServer server;
    private final GameService gameService;
    private final JwtService jwtService;
    private static final Logger logger = LoggerFactory.getLogger(SocketIOEventHandler.class);

    @Autowired
    public SocketIOEventHandler(SocketIOServer server, GameService gameService, JwtService jwtService) {
        this.server = server;
        this.gameService = gameService;
        this.jwtService = jwtService;
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
                    logger.info("User {} joined game {}", username, gameId);

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

            gameService.leaveGame(gameId, username);
            broadcastGameState(gameId);
            client.disconnect();
        };
    }

    private DataListener<MoveDTO> onMakeMove() {
        return (client, data, ackSender) -> {
            String username = client.get("username");
            String gameId = client.get("gameId");
            logger.info("Received move from user {} in game {}: {}", username, gameId, data.toString());

            try {
                gameService.isValidMoveStructure(data);
            } catch (InvalidMoveFormatException e) {
                client.sendEvent("moveResult", "Error processing move: " + e.getMessage());
            }

            try {
                logger.info("attempting move!");
                gameService.applyMove(gameId, data);
                logger.info("move applied! broadcasting new game state.");
                broadcastGameState(gameId);
            } catch (IllegalMoveException e) {
                logger.error("Error processing move", e);
                client.sendEvent("moveResult", "Error processing move: " + e.getMessage());
            }
        };
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