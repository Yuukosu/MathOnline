package net.yuukosu.server;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;
import net.yuukosu.Game;

import java.io.IOException;

public class GameOption {

    @Setter
    @Getter
    private int maxProblems;
    @Setter
    @Getter
    private int maxResult;

    public GameOption(ServerConfig config) throws IOException {
        JsonNode node = Game.getJsonMapper().readTree(config.getConfig());
        this.maxProblems = node.get("GAME").get("MAX_PROBLEMS").asInt(15);
        this.maxResult = node.get("GAME").get("MAX_RESULT").asInt(10);
    }
}
