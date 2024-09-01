package com.cardrace.cardrace_server.dto;

import com.cardrace.cardrace_server.model.game.Types;

import java.util.List;

public record WaitingGameStateDTO(
        Types.GameStatus status,
        String gameName,
        List<String> players
) {}
