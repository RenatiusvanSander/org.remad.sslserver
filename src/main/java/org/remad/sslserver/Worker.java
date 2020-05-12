package org.remad.sslserver;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.StringTokenizer;

/**
 * This handles a client socket connection in a thread.
 */
public class Worker implements Runnable {

    private final Socket clientSocket; // Required.
    private final String name; // Required.
    private final ThreadPooledServerRunnable server; // Required
    private final InetAddress clientIP;
    private String login = null;
    private boolean isStopped = false;
    private PrintWriter outPrintWriter = null;

    /**
     * Creates a new instance of WorkerRunnable.
     *
     * @param clientSocket The client socket
     * @param name         The name of this WorkerRunnable
     * @param server       The server instance
     */
    public Worker(Socket clientSocket, String name, ThreadPooledServerRunnable server) {
        this.clientSocket = clientSocket;
        this.name = name;
        this.server = server;
        clientIP = clientSocket.getInetAddress();
    }

    /**
     * Runs a worker.
     */
    @Override
    public void run() {
        try (BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                boolean isLogout = false;
                String[] tokens = org.apache.maven.shared.utils.StringUtils.split(line);
                String command = tokens[0];
                switch(command.toLowerCase()) {
                    case "<logout>": {
                        send("Server: logged you off.");
                        isLogout = true;
                        break;
                    }
                    case "<login>": {
                        break;
                    }
                    default: {
                        send("You said: " + line);
                        break;
                    }
                }

                if(isLogout) {
                    System.out.println("[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SS")) + "] " + getClientIP().getHostAddress() + " has logged out.");
                    break;
                }

                System.out.println("[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SS")) + "] " + getClientIP().getHostAddress() + " wrote: " + line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        stop();
    }

    /**
     * Stops this Worker instance.
     */
    public void stop() {
        isStopped = true;
        try {
            clientSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing client socket.", e);
        } finally {
            server.getWorkers().remove(this);
        }
    }

    /**
     * Sends message to client via output stream.
     *
     * @param message The message addressed to client
     */
    public void send(String message) {
        if (outPrintWriter == null) {
            // Initializes outPrintWriter.
            try {
                outPrintWriter = new PrintWriter(clientSocket.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        outPrintWriter.println(message);
    }

    /**
     * @return Returns in case not stopped false and in case of stopped true.
     */
    private boolean isStopped() {
        return isStopped;
    }

    /**
     * @return The user's login name.
     */
    public String getLogin() {
        return login;
    }

    /**
     * @return The IP belonging to this worker.
     */
    public InetAddress getClientIP() {
        return clientIP;
    }
}
