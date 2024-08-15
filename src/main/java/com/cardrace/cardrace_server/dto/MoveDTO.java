package com.cardrace.cardrace_server.dto;

import com.cardrace.cardrace_server.model.game.Card;
import com.cardrace.cardrace_server.model.game.Marble;

import java.util.LinkedHashMap;

public record MoveDTO(String username, Card card, Card substitute, LinkedHashMap<Marble, Integer> distances, boolean forfeit) {}