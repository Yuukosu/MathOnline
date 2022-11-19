package net.yuukosu;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.lalyos.jfiglet.FigletFont;
import lombok.Getter;
import net.yuukosu.client.GameClient;
import net.yuukosu.server.GameServer;

import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

public class Game {

    @Getter
    public static final String version = "1.0";
    @Getter
    private static final JsonMapper jsonMapper = new JsonMapper();
    @Getter
    private static final Random random = new Random();

    public static void main(String[] args) {
        Game.showLogo();

        String[] playChoices = {
                "サーバーを開始する",
                "ゲームで遊ぶ",
                "終了する"
        };
        ChoiceSelector playSelector = new ChoiceSelector(playChoices);

        loop:
        while (true) {
            int play = playSelector.select(System.in, true);

            switch (play) {
                case 0: {
                    GameServer server = new GameServer();
                    server.start(18080);
                    break;
                }
                case 1: {
                    GameClient client = new GameClient();
                    String host = readHost();
                    client.start(host, 18080);
                    break;
                }
                case 2: {
                    print("ゲーム終了", 500);
                    break loop;
                }
            }
        }
    }

    public static void print(String message, int wait) {
        System.out.println(message);

        try {
            Thread.sleep(wait);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static String readHost() {
        System.out.println("### 接続先 IP を入力してください。 ###");

        String host = null;
        while (host == null || (!host.matches("^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$") && !host.matches("^([A-Za-z]+.)[A-Za-z]+.[A-Za-z]+$"))) {
            System.out.print(" > ");
            Scanner scanner = new Scanner(System.in);
            host = scanner.nextLine();
        }

        return host;
    }

    private static void showLogo() {
        try {
            String art = FigletFont.convertOneLine("Math ONLINE") + " Math Online v" + Game.version + "\n";
            Game.print(art, 3000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
