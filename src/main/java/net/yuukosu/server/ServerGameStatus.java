package net.yuukosu.server;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
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
        if (this.currentProblem >= 15) {
            this.end();
            return;
        }

        if (this.started) {
            this.currentProblem += 1;
            this.serverThread.sendData(this.generateProblem());
        }
    }

    public void end() {
        this.started = false;
        long time = System.currentTimeMillis() - this.startTime;
        ObjectNode node = Utils.getTemplate("GAME", this.uniqueId.toString());
        node.put("A", "RESULT");
        node.put("B", time);
        node.put("C", this.correctProblem);
        node.put("D", this.incorrectProblem);

        this.serverThread.sendData(node.toString());
    }

    public String generateProblem() {
        int param1 = Game.getRandom().nextInt(50) + 1;
        int param2 = Game.getRandom().nextInt(50) + 1;
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
