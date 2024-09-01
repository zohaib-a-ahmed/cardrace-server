package com.cardrace.cardrace_server.repository;

import com.cardrace.cardrace_server.model.game.Game;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryGameRepository implements GameRepository{

    private final Map<String, Game> gameStore = new ConcurrentHashMap<>();

    @Override
    public Game save(String uuid, Game game) {

        gameStore.put(uuid, game);
        return game;
    }

    @Override
    public Optional<Game> findById(String gameId) {
        return Optional.ofNullable(gameStore.get(gameId));
    }

    @Override
    public void delete(String gameId) {
        gameStore.remove(gameId);
    }
}
