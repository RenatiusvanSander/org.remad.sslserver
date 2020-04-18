package org.remad.sslserver;

import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Map;

/**
 * As a factory this handles initializing of a server socket.
 */
public class ServerSocketFactory {

    /**
     * Initializes the server socket.
     * @param socketSettings The server socket settings
     * @return The created server socket
     * @throws IOException In case of bind errors.
     */
    public static ServerSocket initServerSocket(Map<String, String> socketSettings) throws IOException {
        initKeystore();
        return createServerSocket(Integer.parseInt(socketSettings.get("port")),
                Integer.parseInt(socketSettings.get("backlog")), socketSettings.get("ip"));
    }

    /**
     * Initializes the key store for TLS.
     * @throws RuntimeException In case cannot process keystore.
     */
    private static void initKeystore() throws RuntimeException {
        System.setProperty("javax.net.ssl.keyStore", "/home/rmeier/mykeystore/examplestore");
        System.setProperty("javax.net.ssl.keyStorePassword", "RemAd5619");
    }

    /**
     * Creates SSL Socket of server.
     * @param port    The port of network interface to listen.
     * @param backlog The number of connections, called by ServerSocket backlog.
     * @param ip      The Inet Address of network interface to bind to.
     * @return The bound server socket.
     * @throws IOException In case of subscription to port and ip fails.
     */
    private static ServerSocket createServerSocket(int port, int backlog, String ip) throws IOException {
        ServerSocket serverSocket;
        try {
            InetAddress ifAddress = InetAddress.getByName(ip);
            SSLServerSocketFactory sslServerSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            serverSocket = sslServerSocketFactory.createServerSocket(port, backlog, ifAddress);
        } catch (IOException e) {
            throw new IOException("Server Socket Error: Cannot bind to " + ip + ".", e);
        }
        return serverSocket;
    }
}
