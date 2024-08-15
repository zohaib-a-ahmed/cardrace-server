package com.cardrace.cardrace_server.dto;

import com.cardrace.cardrace_server.model.game.Hand;
import com.cardrace.cardrace_server.model.game.Types;

public record PlayerStateDTO(String username, Types.Color color, Hand hand) {}
