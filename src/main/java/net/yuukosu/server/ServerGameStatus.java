package net.yuukosu.server;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import net.yuukosu.Game;
import net.yuukosu.Utils;

import java.util.UUID;

public class ServerGameStatus {

    @Getter
    private final UUID uniqueId;
    @Getter
    private final ServerThread serverThread;
    @Getter
    private boolean started;
    @Getter
    private long startTime;
    @Setter
    @Getter
    private int maxProblems;
    @Setter
    @Getter
    private int maxResults;
    @Getter
    private int correctProblem;
    @Getter
    private int currentProblem;
    @Getter
    private int currentProblemAnswer;

    public ServerGameStatus(ServerThread serverThread) {
        this.uniqueId = UUID.randomUUID();
        this.serverThread = serverThread;
        this.maxProblems = 15;
        this.maxResults = 10;
        this.correctProblem = 0;
        this.currentProblem = 0;
        this.currentProblemAnswer = 0;

        GameOption option = serverThread.getGameOption();

        if (option != null) {
            this.maxProblems = option.getMaxProblems();
            this.maxResults = option.getMaxResult();
        }
    }

    public void start() {
        this.started = true;
        this.startTime = System.currentTimeMillis();
        this.correctProblem = 0;
        this.currentProblem = 0;
        this.currentProblemAnswer = 0;
        this.next();
    }

    public void next() {
        if (this.currentProblem >= this.maxProblems) {
            this.end();
            return;
        }

        if (this.started) {
            this.currentProblem += 1;
            this.nextProblem(this.maxResults);
        }
    }

    public void nextProblem(int maxResult) {
        int sub = Math.max(Game.getRandom().nextInt(maxResult), 1);
        int param1 = Math.min(Game.getRandom().nextInt(maxResult - sub) + 1, maxResult - sub);
        int param2 = Math.min(Game.getRandom().nextInt(sub) + 1, sub);
        this.currentProblemAnswer = param1 + param2;

        ObjectNode node = Utils.getTemplate("GAME", this.uniqueId.toString());
        node.put("A", "PROBLEM");
        node.put("B", this.currentProblem);
        node.put("C", param1);
        node.put("D", param2);

        this.serverThread.sendData(node.toString());
    }

    public void end() {
        this.started = false;

        String evaluation;
        long time = System.currentTimeMillis() - this.startTime;
        float rate = 100.0F / (this.correctProblem + this.getIncorrectProblems()) * this.correctProblem;

        if (time <= (1000 * 30) && rate >= 100.0F) {
            evaluation = "S";
        } else if (time <= (1000 * 60) && rate >= 75.0F) {
            evaluation = "A";
        } else if (time <= (1000 * 90) && rate >= 50.0F) {
            evaluation = "B";
        } else if (time <= (1000 * 120) && rate >= 30.0F) {
            evaluation = "C";
        } else if (time <= (1000 * 180) && rate >= 15.0F) {
            evaluation = "D";
        } else {
            evaluation = "E";
        }

        ObjectNode node = Utils.getTemplate("GAME", this.uniqueId.toString());
        node.put("A", "RESULT");
        node.put("B", evaluation);
        node.put("C", time);
        node.put("D", this.correctProblem);
        node.put("E", this.getIncorrectProblems());

        this.serverThread.sendData(node.toString());
    }

    public boolean checkAnswer(int answer) {
        boolean result = this.currentProblemAnswer == answer;

        if (result) {
            this.correctProblem += 1;
        }

        return result;
    }

    private int getIncorrectProblems() {
        return this.currentProblem - this.correctProblem;
    }
}
