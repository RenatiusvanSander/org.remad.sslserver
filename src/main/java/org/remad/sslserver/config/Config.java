package org.remad.sslserver.config;

/**
 * Holds te server configuration.
 */
public class Config {

    public Config() {
    }

    public String getConfigFileFullPath() {
        return configFileFullPath;
    }

    public int getBacklog() {
        return backlog;
    }

    public int getChatLogFileFullPath() {
        return chatLogFileFullPath;
    }

    public int getListenerPort() {
        return listenerPort;
    }

    public String getServerJoinMessage() {
        return serverJoinMessage;
    }

    public String getServerName() {
        return serverName;
    }

    public String getServerSocketIP() {
        return serverSocketIP;
    }

    private String configFileFullPath;
    private int backlog;
    private int chatLogFileFullPath;
    private int listenerPort;
    private String serverJoinMessage;
    private String serverName;
    private String serverSocketIP;
}
