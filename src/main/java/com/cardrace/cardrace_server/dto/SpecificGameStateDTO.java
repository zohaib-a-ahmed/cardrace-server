package com.cardrace.cardrace_server.dto;

import com.cardrace.cardrace_server.model.game.Board;
import com.cardrace.cardrace_server.model.game.Card;
import com.cardrace.cardrace_server.model.game.Hand;
import com.cardrace.cardrace_server.model.game.Types;

import java.util.List;
import java.util.Map;

public record SpecificGameStateDTO (
        String gameName,
        Board board,
        List<String> players,
        Map<String, Types.Color> playerColorMap,
        Types.Color currentColor,
        Card lastCard,
        Types.GameStatus status,
        String winner,
        String player,
        Hand playerHand,
        Types.Color playerColor
) {}
