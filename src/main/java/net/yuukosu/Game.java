package net.yuukosu;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.lalyos.jfiglet.FigletFont;
import lombok.Getter;
import net.yuukosu.client.GameClient;
import net.yuukosu.server.GameServer;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;

public class Game {

    @Getter
    public static final String version = "1.0";
    @Getter
    public static final String author = "YuukosuDev";
    @Getter
    private static final JsonMapper jsonMapper = new JsonMapper();
    @Getter
    private static final Random random = new Random();
    @Getter
    private static boolean debug = false;

    public static void main(String[] args) {
        boolean help = Arrays.asList(args).contains("--help") || Arrays.asList(args).contains("-h");
        Game.debug = Arrays.asList(args).contains("--debug") || Arrays.asList(args).contains("-d");
        boolean autoServer = Arrays.asList(args).contains("--server") || Arrays.asList(args).contains("-s");

        if (help) {
            Game.printHelp();
            return;
        }

        if (!Game.debug) {
            Game.printBanner();
        }

        if (autoServer) {
            GameServer server = new GameServer();
            server.start(11111);
            return;
        }

        String[] playOptions = {
                "サーバーを開始する",
                "サーバーに接続する",
                "終了する"
        };
        OptionSelector selector = new OptionSelector(playOptions);

        loop:
        while (true) {
            int play = selector.select(System.in, true);

            switch (play) {
                case 0: {
                    GameServer server = new GameServer();
                    server.start(11111);
                    break;
                }
                case 1: {
                    GameClient client = new GameClient();
                    String host = readHost();
                    client.start(host, 11111);
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

    public static void printLog(String message) {
        String format = new SimpleDateFormat("yy/MM/dd hh:mm:ss").format(new Date());
        System.out.printf("[%s] %s\n", format, message);
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

    private static void printHelp() {
        String[] help = {
                "---------- MathOnline Help ----------",
                "Usage: java -jar MathOnline.jar (Args)",
                " -h --help | Show Help.",
                " -s --server | Start The Server.",
                " -d --debug | Enable Debug Mode.",
                "-------------------------------------"
        };
        Arrays.asList(help).forEach(System.out::println);
    }

    private static void printBanner() {
        try {
            String art = FigletFont.convertOneLine("Math ONLINE") + " Math Online v" + Game.version + "\n" + " Author: " + Game.author + "\n";
            Game.print(art, 3000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
