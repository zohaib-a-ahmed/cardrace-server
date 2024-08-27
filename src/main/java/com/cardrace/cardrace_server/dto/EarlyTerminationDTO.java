package com.cardrace.cardrace_server.dto;

import com.cardrace.cardrace_server.model.game.Types;

public record EarlyTerminationDTO(
        String deserter,
        Types.GameStatus status
) {}
