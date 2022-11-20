package net.yuukosu.client;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import net.yuukosu.Utils;

import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.UUID;

public class ClientGameStatus {

    @Getter
    private final UUID uniqueId;
    @Getter
    private final GameClient gameClient;
    @Getter
    private GameDebugger gameDebugger;
    @Getter
    private boolean started;

    public ClientGameStatus(UUID uniqueId, GameClient gameClient) {
        this.uniqueId = uniqueId;
        this.gameClient = gameClient;
    }

    public void startGame() {
        if (!this.started) {
            this.started = true;
            ObjectNode node = Utils.getTemplate("GAME", this.getUniqueId().toString());
            node.put("A", "START");
            this.gameClient.sendData(node.toString());
        }
    }

    public void end() {
        this.started = false;
    }

    public void allowDebug() {
        this.gameDebugger = new GameDebugger();
    }

    public int answer(int problem, int param1, int param2, boolean autoAnswer) {
        System.out.println("題" + problem + "問");
        int answer;

        while (true) {
            System.out.print(param1 + " + " + param2 + " = ");

            if (autoAnswer) {
                answer = param1 + param2;
                System.out.println(answer);
                break;
            }

            try {
                Scanner scanner = new Scanner(System.in);
                answer = scanner.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("数字のみ入力可能です。");
                continue;
            }

            break;
        }

        return answer;
    }
}
