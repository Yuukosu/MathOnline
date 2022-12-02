package net.yuukosu.server;

import lombok.Getter;

import java.io.File;

public class ServerConfig {

    @Getter
    private final File config;

    public ServerConfig(String fileName) {
        this.config = new File(fileName);
    }

    public boolean isExists() {
        return this.config.exists();
    }
}
