package org.remad.sslserver;

import org.apache.maven.shared.utils.StringUtils;
import org.remad.dto.Data;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * This handles a client socket connection in a thread.
 * @author Remy Meier
 */
public class Worker implements Runnable {

    private final Socket clientSocket; // Required.
    private final String name; // Required.
    private final ThreadPooledServerRunnable server; // Required
    private final InetAddress clientIP;
    private String login = null;
    private boolean isStopped = false;
    private PrintWriter outPrintWriter = null;
    private boolean isLogout;
    private int countLoginFail = 0;

    /**
     * Creates a new instance of WorkerRunnable.
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
        setLogout(false);
        try (BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] tokens = StringUtils.split(line);
                String command = tokens.length == 0 ? line : tokens[0];
                switch(command.toLowerCase()) {
                    case "<logout>": {
                        // Logs user out.
                        setLogout(true);
                        String message = getLogin() != null ? getLogin() + " offline." : getClientIP() + " offline.";
                        // ToDo refactor to send of protocol.
                        List<Worker> workers = server.getWorkers();
                        for(Worker worker : workers) {
                            if(worker != this) {
                                worker.send(message);
                            } else {
                                worker.send("Server: You logged off.");
                            }
                        }
                        break;
                    }
                    case "<login>": {
                        // Handles first login, ToDo refactor to own protocol.login() method
                        if(tokens.length > 2
                                && "remad".equals(tokens[1])
                                && "Password".equals(tokens[2])) {
                            // Logs user in and inform others
                            setLogin(tokens[1]);
                            String loginMessage = tokens[1] + " online";
                            List<Worker> workers = server.getWorkers();
                            for(Worker worker : workers) {
                                if(worker.getLogin() != null && worker.getLogin().equals(tokens[1])) {
                                    // Sends login message to logging in user.
                                    worker.send("Server: You successful logged in.");
                                } else if(worker.getLogin() != null) {
                                    // Sends online information to others
                                    worker.send(loginMessage);
                                }
                            }
                            System.out.println("[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SS")) + "] " + getClientIP().getHostAddress() + " logged in.");
                        } else if(tokens.length > 2
                                && "guest".equals(tokens[1])
                                && "Password2".equals(tokens[2])) {
                            // Logs user in and inform others
                            setLogin(tokens[1]);
                            String loginMessage = tokens[1] + " online";
                            List<Worker> workers = server.getWorkers();
                            for(Worker worker : workers) {
                                if(worker.getLogin() != null && worker.getLogin().equals(tokens[1])) {
                                    // Sends login message to logging in user.
                                    worker.send("Server: You successful logged in.");
                                } else if(worker.getLogin() != null) {
                                    // Sends online information to others
                                    worker.send(loginMessage);
                                }
                            }
                            System.out.println("[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SS")) + "] " + getClientIP().getHostAddress() + " logged in.");
                        } if (getCountLoginFail() == 3) {
                            // Three login failures set a Guest name.
                            countLoginFail = 0;
                            login = "Guest" + Instant.now().getEpochSecond();
                        } else {
                            // Increases login failures.
                            setCountLoginFail();
                        }
                        break;
                    }
                    case "<filetransfer>": {
                        Data data = null;
                        if(tokens.length == 2) {
                            // Transfers a requested file to client.

                        } else if(tokens.length > 2){
                            // Transfers a file from client to server.
                            try {
                                ObjectInputStream objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
                                data = (Data) objectInputStream.readObject();
                                FileOutputStream fileOutputStream = new FileOutputStream(data.getFullFileName());
                                fileOutputStream.write(data.getFile());
                                fileOutputStream.close();
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                        data = null;
                        break;
                    }
                    default: {
                        // Sends echo message back to client.
                        System.out.println("[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SS")) + "] " + getClientIP().getHostAddress() + " wrote: " + line);
                        send("You said: " + line);
                        break;
                    }
                }

                if(isLogout()) {
                    // Ends the while loop.
                    System.out.println("[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SS")) + "] " + getClientIP().getHostAddress() + " has logged out.");
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        stop();
    }

    /**
     * Sets login with username.
     * @param val The user's login.
     */
    public void setLogin(String val) {
        login = val;
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

    /**
     * @return The name of this worker, is socket content of this instance.
     */
    public String getName() {
        return name;
    }

    /**
     * @return In case th user logs out {@code true} or in case of user has not loged out {@code false}.
     */
    public boolean isLogout() {
        return isLogout;
    }

    /**
     * Sets logout to true.
     * @param logout n case of logout is {@code true}.
     */
    public void setLogout(boolean logout) {
        isLogout = logout;
    }

    /**
     * @return The counted login fails.
     */
    public int getCountLoginFail() {
        return countLoginFail;
    }

    /**
     * Increases login fail with +1.
     */
    public void setCountLoginFail() {
        this.countLoginFail++;
    }
}
