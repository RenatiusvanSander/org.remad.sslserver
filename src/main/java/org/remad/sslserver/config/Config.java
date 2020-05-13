package org.remad.sslserver.config;

/**
 * Data Object for the server configuration.
 * @author Remy Meier
 */
public class Config {

    public Config() {
    }

    /**
     * @return The full qualified path for config file.
     */
    public String getConfigFileFullPath() {
        return configFileFullPath;
    }

    /**
     * @return Returns the backlog, number of allowed worker threads.
     */
    public int getBacklog() {
        return backlog;
    }

    /**
     * @return The full qualified path to chatlog file.
     */
    public int getChatLogFileFullPath() {
        return chatLogFileFullPath;
    }

    /**
     * @return The listener port of this server.
     */
    public int getListenerPort() {
        return listenerPort;
    }

    /**
     * @return The join message of this server.
     */
    public String getServerJoinMessage() {
        return serverJoinMessage;
    }

    /**
     * @return The name of this server.
     */
    public String getServerName() {
        return serverName;
    }

    /**
     * @return The InetAddress where the server socket binds to.
     */
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
