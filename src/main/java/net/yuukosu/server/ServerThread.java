package net.yuukosu.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import net.yuukosu.Game;
import net.yuukosu.Utils;

import java.io.*;
import java.net.Socket;

public class ServerThread implements Runnable {

    @Getter
    private final Socket socket;
    @Getter
    private final BufferedReader reader;
    @Getter
    private final BufferedWriter writer;
    @Getter
    private final ServerGameStatus gameStatus;
    @Getter
    private boolean closed;

    public ServerThread(Socket socket) throws IOException {
        this.socket = socket;
        this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        this.writer = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
        this.gameStatus = new ServerGameStatus(this);
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

    private void init() {
        ObjectNode node = Utils.getTemplate("SYSTEM", this.gameStatus.getUniqueId().toString());
        node.put("A", "INIT");
        this.sendData(node.toString());
    }

    private void receive(String data) {
        try {
            JsonNode node = Game.getJsonMapper().readTree(data);

            if (node.has("TYPE") && node.has("UUID")) {
                String type = node.get("TYPE").asText();
                String uuid = node.get("UUID").asText();

                if (!this.gameStatus.getUniqueId().toString().equals(uuid)) {
                    this.error();
                    return;
                }

                if (type.equals("SYSTEM")) {
                    String a = node.get("A").asText();

                    if (a.equals("END")) {
                        this.close();
                    }

                    return;
                }

                if (type.equals("GAME")) {
                    String a = node.get("A").asText();

                    if (a.equals("START")) {
                        if (this.gameStatus.isStarted()) {
                            return;
                        }

                        this.gameStatus.start();
                    }

                    if (a.equals("ANSWER")) {
                        int b = node.get("B").asInt();
                        ObjectNode judgeNode = Utils.getTemplate("GAME", this.gameStatus.getUniqueId().toString());
                        judgeNode.put("A", "JUDGE");
                        judgeNode.put("B", this.gameStatus.checkAnswer(b));
                        judgeNode.put("C", this.gameStatus.getCurrentProblemAnswer());
                        this.sendData(judgeNode.toString());

                        try {
                            Thread.sleep(100);
                            this.gameStatus.next();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    return;
                }
            }

            System.out.println("Dropped.");
        } catch (JsonProcessingException e) {
            this.error();
        }
    }

    public void close() {
        if (!this.socket.isClosed() && !this.closed) {
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

    public void error() {
        System.out.println("エラーが発生しました。");
        this.close();
    }

    @Override
    public void run() {
        this.init();

        while (!this.closed && !this.socket.isClosed()) {
            try {
                if (this.reader.ready()) {
                    String read = this.reader.readLine();
                    this.receive(read);
                }
            } catch (IOException e) {
                this.error();
                break;
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Disconnected.");
    }
}
