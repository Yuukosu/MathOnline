package net.yuukosu.server;

import lombok.Getter;
import net.yuukosu.Game;

import javax.net.ServerSocketFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class GameServer {

    @Getter
    private ServerSocket socket;
    @Getter
    private final List<ServerThread> serverThreads;

    public GameServer() {
        try {
            this.socket = ServerSocketFactory.getDefault().createServerSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.serverThreads = new ArrayList<>();
    }

    public void start(int port) {
        Runtime.getRuntime().addShutdownHook(new Thread(this::close));

        Game.printLog("Starting Game Server...");

        try {
            this.socket = ServerSocketFactory.getDefault().createServerSocket(port);
        } catch (IOException e) {
            this.close();
            Game.printLog("サーバーの起動に失敗しました。");
            return;
        }

        Game.printLog("Waiting For Connection...");

        while (!this.socket.isClosed()) {
            try {
                Socket client = this.socket.accept();
                ServerThread serverThread = new ServerThread(client);

                Thread thread = new Thread(serverThread);
                thread.start();

                this.serverThreads.add(serverThread);

                Game.printLog("New Connection -> " + this.socket.getInetAddress().getHostAddress());
            } catch (IOException e) {
                this.close();
                return;
            }
        }
    }

    public void close() {
        this.serverThreads.forEach(ServerThread::close);

        if (!this.socket.isClosed()) {
            try {
                this.socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
