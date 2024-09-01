package com.cardrace.cardrace_server.dto;

import com.cardrace.cardrace_server.model.game.Card;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MoveDTO {
    private final String username;
    private final Card card;
    private final Card substitute;

    @JsonDeserialize(using = DistancesDeserializer.class)
    private final Map<Integer, Integer> distances;

    private final boolean forfeit;

    @JsonCreator
    public MoveDTO(
            @JsonProperty("username") String username,
            @JsonProperty("card") Card card,
            @JsonProperty("substitute") Card substitute,
            @JsonProperty("distances") Map<Integer, Integer> distances,
            @JsonProperty("forfeit") boolean forfeit) {
        this.username = username;
        this.card = card;
        this.substitute = substitute;
        this.distances = distances;
        this.forfeit = forfeit;
    }

    // Getters
    public String getUsername() { return username; }
    public Card getCard() { return card; }
    public Card getSubstitute() { return substitute; }
    public Map<Integer, Integer> getDistances() { return distances; }
    public boolean isForfeit() { return forfeit; }

    // Custom deserializer for distances
    private static class DistancesDeserializer extends JsonDeserializer<Map<Integer, Integer>> {
        @Override
        public Map<Integer, Integer> deserialize(JsonParser p, DeserializationContext ctxt)
                throws IOException, JsonProcessingException {
            List<List<Integer>> list = p.readValueAs(List.class);
            Map<Integer, Integer> map = new HashMap<>();
            for (List<Integer> pair : list) {
                if (pair.size() == 2) {
                    map.put(pair.get(0), pair.get(1));
                }
            }
            return map;
        }
    }

    @Override
    public String toString() {
        return "MoveDTO{" +
                "username='" + username + '\'' +
                ", card=" + card +
                ", substitute=" + substitute +
                ", distances=" + distances +
                ", forfeit=" + forfeit +
                '}';
    }

}