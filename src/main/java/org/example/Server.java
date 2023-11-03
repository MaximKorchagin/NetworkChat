package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class Server {
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());
    private static final Map<Integer, User> USERS = new HashMap<>();

    public static void main(String[] args) throws IOException {
        FileHandler fileHandler = new FileHandler(Server.class.getCanonicalName() + "Log.txt", true);
        LOGGER.addHandler(fileHandler);

        LOGGER.info("Server started");
        int port = 0;
        Properties properties = new Properties();

        try (FileInputStream fis = new FileInputStream("src/main/resources/settings.txt")) {
            properties.load(fis);
            port = Integer.parseInt(properties.getProperty("port"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    startServer(clientSocket);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void startServer(Socket clientSocket) {
        new Thread(() -> {
            try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true); BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
                User user = new User(clientSocket, out);
                USERS.put(clientSocket.getPort(), user);
                out.println("Enter you nickname:");
                String name = in.readLine();
                user.setName(name);
                out.println("Welcome to the network chat, " + name + "!");
                sendMsgToAll(name + " connected.");
                waitMsgAndSend(clientSocket);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                    LOGGER.warning("Connection lost: " + clientSocket.getInetAddress().getHostAddress() + " " + USERS.get(clientSocket.getPort()).getName());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static synchronized void sendMsgToAll(String msg) {
        for (Map.Entry<Integer, User> entry : USERS.entrySet()) {
            entry.getValue().sendMsg(msg);
        }
    }

    public static void waitMsgAndSend(Socket clientSocket) {
        try (Scanner inMsg = new Scanner(clientSocket.getInputStream())) {
            while (true) {
                if (inMsg.hasNext()) {
                    String msg = inMsg.nextLine();
                    switch (msg) {
                        default:
                            Date date = new Date();
                            LOGGER.info(date + ": " + USERS.get(clientSocket.getPort()).getName() + " " + msg);
                            sendMsgToAll(USERS.get(clientSocket.getPort()).getName() + ": " + msg);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}