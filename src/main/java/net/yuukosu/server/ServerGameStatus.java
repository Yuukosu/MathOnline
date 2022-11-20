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
    @Getter
    private int correctProblem;
    @Getter
    private int incorrectProblem;
    @Getter
    private int currentProblem;
    @Getter
    private int currentProblemAnswer;

    public ServerGameStatus(ServerThread serverThread) {
        this.uniqueId = UUID.randomUUID();
        this.serverThread = serverThread;
        this.maxProblems = 15;
        this.correctProblem = 0;
        this.incorrectProblem = 0;
        this.currentProblem = 0;
        this.currentProblemAnswer = 0;
    }

    public void start() {
        this.started = true;
        this.startTime = System.currentTimeMillis();
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
            this.serverThread.sendData(this.generateProblem(10));
        }
    }

    public void end() {
        this.started = false;

        String evaluation;
        long time = System.currentTimeMillis() - this.startTime;
        float rate = 100.0F / (this.correctProblem + this.incorrectProblem) * this.correctProblem;

        if (time <= (1000 * 60) && rate >= 100.0F) {
            evaluation = "S";
        } else if (time <= (1000 * 60) && rate >= 50.0F) {
            evaluation = "A";
        } else if (time <= (1000 * 90) && rate >= 50.0F) {
            evaluation = "B";
        } else if (time <= (1000 * 120) && rate >= 30.0F) {
            evaluation = "C";
        } else if (time <= (1000 * 180) && rate >= 30.0F) {
            evaluation = "D";
        } else {
            evaluation = "E";
        }

        ObjectNode node = Utils.getTemplate("GAME", this.uniqueId.toString());
        node.put("A", "RESULT");
        node.put("B", evaluation);
        node.put("C", time);
        node.put("D", this.correctProblem);
        node.put("E", this.incorrectProblem);

        this.serverThread.sendData(node.toString());
    }

    public String generateProblem(int max) {
        int sub = Game.getRandom().nextInt(max);
        int param1 = Math.min(Game.getRandom().nextInt(Math.max(max - sub, 1)) + 1, max);
        int param2 = Math.min(Game.getRandom().nextInt(Math.max(sub, 1)) + 1, max);
        this.currentProblemAnswer = param1 + param2;

        ObjectNode node = Utils.getTemplate("GAME", this.uniqueId.toString());
        node.put("A", "PROBLEM");
        node.put("B", this.currentProblem);
        node.put("C", param1);
        node.put("D", param2);

        return node.toString();
    }

    public boolean checkAnswer(int answer) {
        boolean result = this.currentProblemAnswer == answer;

        if (result) {
            this.correctProblem += 1;
        } else {
            this.incorrectProblem += 1;
        }

        return result;
    }
}
