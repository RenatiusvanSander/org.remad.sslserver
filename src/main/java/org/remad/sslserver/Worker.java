package org.remad.sslserver;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

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
                String[] tokens = org.apache.maven.shared.utils.StringUtils.split(line);
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
                    case "<FileTransfer>": {
                        FileInputStream fis = null;
                        BufferedInputStream bis = null;
                        OutputStream os = null;
                        FileOutputStream fos = null;
                        BufferedOutputStream bos = null;
                        InputStream is = null;
                        // Transfers a file.
                        if(tokens.length == 2) {
                            // Transfers a requested file to client
                            try {
                                String fullFileName = tokens[1];
                                File file = new File(fullFileName);
                                byte[] allocatedByteBuffer = new byte[(int)file.length()];
                                fis = new FileInputStream(file);
                                bis = new BufferedInputStream(fis);
                                bis.read(allocatedByteBuffer,0, allocatedByteBuffer.length);
                                os = clientSocket.getOutputStream();
                                // System.out.println("Sending " + fullFileName + "(" + allocatedByteBuffer.length + " bytes)");
                                os.write(allocatedByteBuffer,0,allocatedByteBuffer.length);
                                os.flush();
                                // System.out.println("Done.");
                            } finally {
                                if(fis != null && bis != null && os != null) {
                                    // Whatever comes, Close streams when they are not null.
                                    fis.close();
                                    bis.close();
                                    os.close();
                                }
                            }
                        } else if(tokens.length > 2){
                            try {
                                String fullQualifiedFileName = tokens[1];
                                int fileByteSize = Integer.parseInt(tokens[2]);
                                // receive file
                                byte [] allocatedByteBuffer  = new byte [fileByteSize];
                                is = clientSocket.getInputStream();
                                fos = new FileOutputStream(fullQualifiedFileName);
                                bos = new BufferedOutputStream(fos);
                                int bytesRead = is.read(allocatedByteBuffer,0,allocatedByteBuffer.length);
                                int current = bytesRead;

                                do {
                                    bytesRead =
                                            is.read(allocatedByteBuffer, current, (allocatedByteBuffer.length - current));
                                    if(bytesRead >= 0) current += bytesRead;
                                } while(bytesRead > -1);

                                bos.write(allocatedByteBuffer, 0 , current);
                                bos.flush();
                            } finally {
                                if(is != null && fos != null && bos != null) {
                                    is.close();
                                    fos.close();
                                    bos.close();
                                }
                            }
                        }
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
