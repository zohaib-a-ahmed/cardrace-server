package com.cardrace.cardrace_server.dto;

import com.cardrace.cardrace_server.model.game.Board;
import com.cardrace.cardrace_server.model.game.Card;
import com.cardrace.cardrace_server.model.game.Types;

import java.util.List;

public record GameStateDTO (Board board, List<String> players, Types.Color currentColor, Card lastCard, Types.GameStatus status, String winner) {}
