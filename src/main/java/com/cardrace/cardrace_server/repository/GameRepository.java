package com.cardrace.cardrace_server.repository;

import com.cardrace.cardrace_server.model.game.Game;

import java.util.Optional;

public interface GameRepository {
    Game save(String uuid, Game game);
    Optional<Game> findById(String gameId);
    void delete(String gameId);
}
