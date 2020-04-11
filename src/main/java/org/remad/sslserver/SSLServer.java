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

public class SSLServer {

    private final static Logger logger = Logger.getLogger(SSLServer.class.getName());
    private static final int PORT = 8000;
    private static final int BACKLOG = 1000;
    private ServerSocket serverSocket;

    // ToDo refactor socket connection bring up and server, means createSSLSocket

    public static void main(String[] args) {


        SSLServerSocketFactory sslServerSocketFactory =
                (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();

        try {
            ServerSocket sslServerSocket =
                    sslServerSocketFactory.createServerSocket(PORT);
            System.out.println("SSL ServerSocket started");
            System.out.println(sslServerSocket.toString());

            Socket socket = sslServerSocket.accept();
            System.out.println("ServerSocket accepted");

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
                    .log(Level.SEVERE, null, ex);
        }
    }

    private Socket createServerSocket(int serverPort, String host) throws IOException {
        InetAddress ifAddress = InetAddress.getByName(host);
        SSLServerSocketFactory sslServerSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        ServerSocket sslServerSocket = sslServerSocketFactory.createServerSocket(serverPort, BACKLOG, ifAddress);
        Socket socket = sslServerSocket.accept();
        return socket;
    }
}
