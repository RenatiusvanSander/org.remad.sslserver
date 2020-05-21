package org.remad.sslserver;

import org.remad.sslserver.config.Config;
import org.remad.sslserver.config.ConfigReader;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A simple kept SSL Server in Thread Pooled Pattern.
 * @author Remy Meier
 */
public class ThreadPooledSSLServer {

    /**
     * App of thread pooled ssl server.
     * @param args The arguments of shell to process.
     */
    public static void main(String[] args) {
        Config serverConfig;
        ServerSocket serverSocket;
        try {
            serverConfig = new ConfigReader().getConfig();
            serverSocket = ServerSocketFactory.initServerSocket(ConfigReader.getServerSocketSettings(serverConfig));
        } catch (IOException e) {
            Logger.getLogger(ThreadPooledSSLServer.class.getName())
                    .log(Level.SEVERE, e.getLocalizedMessage(), e);
            return;
        }

        runServer(serverSocket);
    }

    /**
     * Runs the Thread Pooled Server.
     * @param serverSocket The server socket of this pc.
     */
    private static void runServer(ServerSocket serverSocket) {
        ThreadPooledServerRunnable server = new ThreadPooledServerRunnable(serverSocket, 100);
        Thread serverThread = new Thread(server);
        serverThread.start();

        if (server.isStopped()) {
            server.stop();
        }
    }
}
