package net.yuukosu.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import net.yuukosu.ChoiceSelector;
import net.yuukosu.Game;
import net.yuukosu.Utils;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.UUID;

public class GameClient {

    @Getter
    private final Socket socket;
    @Getter
    private BufferedReader reader;
    @Getter
    private BufferedWriter writer;
    @Getter
    private ClientGameStatus gameStatus;
    private boolean closed;

    public GameClient() {
        this.socket = new Socket();
    }

    public void sendData(String data) {
        if (this.writer != null && !this.socket.isClosed()) {
            try {
                this.writer.write(data);
                this.writer.newLine();
                this.writer.flush();
            } catch (IOException ignored) {
            }
        }
    }

    private void receive(String data) {
        try {
            JsonNode node = Game.getJsonMapper().readTree(data);

            if (node.has("TYPE") && node.has("UUID")) {
                String type = node.get("TYPE").asText();
                String uuid = node.get("UUID").asText();

                if (this.gameStatus != null && !this.gameStatus.getUniqueId().toString().equals(uuid)) {
                    this.error();
                    return;
                }

                if (type.equals("SYSTEM")) {
                    String a = node.get("A").asText();

                    if (a.equals("INIT")) {
                        if (this.gameStatus != null) {
                            this.error();
                            return;
                        }

                        this.gameStatus = new ClientGameStatus(UUID.fromString(uuid), this);
                    }

                    if (a.equals("END")) {
                        this.close();
                    }

                    return;
                }

                if (type.equals("GAME")) {
                    String a = node.get("A").asText();

                    if (this.gameStatus.isStarted()) {
                        if (a.equals("PROBLEM")) {
                            int currentProb = node.get("B").asInt();
                            int param1 = node.get("C").asInt();
                            int param2 = node.get("D").asInt();
                            int answer = this.gameStatus.answer(currentProb, param1, param2);
                            ObjectNode answerNode = Utils.getTemplate("GAME", this.gameStatus.getUniqueId().toString());
                            answerNode.put("A", "ANSWER");
                            answerNode.put("B", answer);
                            this.sendData(answerNode.toString());
                        }

                        if (a.equals("JUDGE")) {
                            boolean b = node.get("B").asBoolean();
                            int c = node.get("C").asInt();
                            System.out.println((b ? "ﾋﾟﾝﾎﾟｰﾝ(●´з｀)b☆【正解】" : "🙅 不正解 🙅") + " 答えは " + c + " でした！");
                        }

                        if (a.equals("RESULT")) {
                            long b = node.get("B").asLong();
                            int c = node.get("C").asInt();
                            int d = node.get("D").asInt();
                            String bFormat = new SimpleDateFormat("mm:ss").format(b);
                            String cFormat = String.format("%,d", c);
                            String dFormat = String.format("%,d", d);

                            Game.print(
                                    "\n----- 結果 -----" +
                                    "\nタイム: " + bFormat +
                                    "\n正解した回数: " + cFormat +
                                    "\n間違えた回数: " + dFormat +
                                    "\n---------------\n",
                                    5000
                            );
                            this.gameStatus.end();
                        }
                    }

                    return;
                }
            }

            System.out.println("Dropped");
        } catch (JsonProcessingException e) {
            this.error();
        }
    }

    public void close() {
        if (this.isClosed() && this.gameStatus != null) {
            ObjectNode node = Utils.getTemplate("SYSTEM", this.gameStatus.getUniqueId().toString());
            node.put("A", "END");
            this.sendData(node.toString());
        }

        this.closed = true;

        try {
            if (this.writer != null) {
                this.writer.close();
            }

            if (this.reader != null) {
                this.reader.close();
            }

            if (!this.socket.isClosed()) {
                this.socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void error() {
        System.out.println("エラーが発生しました。");
        this.close();
    }

    public void start(String host, int port) {
        Runtime.getRuntime().addShutdownHook(new Thread(this::close));

        System.out.println("接続中...");

        try {
            this.socket.connect(new InetSocketAddress(host, port), 3000);
            this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.writer = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
        } catch (IOException e) {
            this.close();
            Game.print("サーバーへの接続に失敗しました。", 1000);
            return;
        }

        String[] choices = {
                "ゲームを開始",
                "切断する"
        };
        ChoiceSelector selector = new ChoiceSelector(choices);

        while (!this.isClosed()) {
            try {
                if (this.reader.ready()) {
                    String read = this.reader.readLine();
                    this.receive(read);
                }
            } catch (IOException e) {
                this.error();
                return;
            }

            if (this.gameStatus != null) {
                if (!this.gameStatus.isStarted()) {
                    int select = selector.select(System.in, true);
                    switch (select) {
                        case 0:
                            this.gameStatus.startGame();
                            break;
                        case 1:
                            this.close();
                            break;
                    }
                }
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Disconnected.");
    }

    public boolean isClosed() {
        return this.socket.isClosed() && this.closed;
    }
}
