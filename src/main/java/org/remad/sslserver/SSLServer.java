package org.remad.sslserver;

import javax.net.ssl.SSLServerSocketFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A simple kept SSL Server.
 */
public class SSLServer {

    private final static Logger logger = Logger.getLogger(SSLServer.class.getName());
    private static final int PORT = 8000;
    private static final int BACKLOG = 1;
    private Socket socket;

    // ToDo refactor socket connection bring up and server, means createSSLSocket

    public static void main(String[] args) {
        try {
            initKeystore();
            Socket socket = createServerSocket(PORT, 1000, "192.168.0.14");

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            try (BufferedReader bufferedReader =
                         new BufferedReader(
                                 new InputStreamReader(socket.getInputStream()))) {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    System.out.println(line);
                    out.println(line);
                }
            }
            System.out.println("Closed");
        } catch (IOException ex) {
            Logger.getLogger(SSLServer.class.getName())
                    .log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }

    public static Socket createServerSocket(int port, int backlog, String ip) throws IOException {
        Socket socket;
        try {
            InetAddress ifAddress = InetAddress.getByName(ip);
            SSLServerSocketFactory sslServerSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            ServerSocket serverSocket = sslServerSocketFactory.createServerSocket(port, backlog, ifAddress);
            socket = serverSocket.accept();
            System.out.println(socket.toString());
        } catch (IOException e) {
            throw new IOException(e);
        }
        return socket;
    }

    public static void initKeystore() {
        System.setProperty("javax.net.ssl.keyStore", "/home/rmeier/mykeystore/examplestore");
        System.setProperty("javax.net.ssl.keyStorePassword", "RemAd5619");
    }
}
