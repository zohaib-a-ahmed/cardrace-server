package com.cardrace.cardrace_server.repository;

import com.cardrace.cardrace_server.model.game.Game;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Repository
public class RedisGameRepository implements GameRepository {

    private final RedisTemplate<String, Game> redisTemplate;
    private static final String KEY_PREFIX = "game:";
    private static final long DEFAULT_EXPIRATION = 24 * 60 * 60; // 24 hours in seconds

    public RedisGameRepository(RedisTemplate<String, Game> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Game save(String uuid, Game game) {
        String key = KEY_PREFIX + uuid;
        redisTemplate.opsForValue().set(key, game, DEFAULT_EXPIRATION, TimeUnit.SECONDS);
        return game;
    }

    @Override
    public Optional<Game> findById(String gameId) {
        String key = KEY_PREFIX + gameId;
        Game game = redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(game);
    }

    @Override
    public void delete(String gameId) {
        String key = KEY_PREFIX + gameId;
        redisTemplate.delete(key);
    }
}