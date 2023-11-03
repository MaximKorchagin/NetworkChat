package org.example;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Date;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class Client {
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());
    private static final String EXIT = "/exit";
    private static Socket clientSocket = null;
    private static BufferedReader inMsg;
    private static PrintWriter outMsg;
    private static Scanner scannerConsole;
    private static final AtomicBoolean flag = new AtomicBoolean(true);
    private static final Date date = new Date();

    public static void main(String[] args) throws IOException {
        FileHandler fileHandler = new FileHandler(Client.class.getCanonicalName() + "Log.txt", true);
        LOGGER.addHandler(fileHandler);
        int port = 0;
        Properties properties = new Properties();

        try (FileInputStream fis = new FileInputStream("src/main/resources/settings.txt")) {
            properties.load(fis);
            port = Integer.parseInt(properties.getProperty("port"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        clientSocket = new Socket(InetAddress.getLocalHost(), port);
        outMsg = new PrintWriter(clientSocket.getOutputStream(), true);
        inMsg = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        scannerConsole = new Scanner(System.in);
        startInputThread();
        startOutputThread();
    }

    public static void startInputThread() {
        new Thread(() -> {
            try {
                while (true) {
                    if (!flag.get()) {
                        inMsg.close();
                        clientSocket.close();
                        break;
                    }
                    if (inMsg.ready()) {
                        String msgFormServer = inMsg.readLine();
                        LOGGER.info(date + ": " + msgFormServer);
                        System.out.println(msgFormServer);
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    public static void startOutputThread() {
        new Thread(() -> {
            while (true) {
                if (scannerConsole.hasNext()) {
                    String msg = scannerConsole.nextLine();
                    if (msg.equalsIgnoreCase(EXIT)) {
                        outMsg.println(msg);
                        scannerConsole.close();
                        outMsg.close();
                        flag.set(false);
                        break;
                    }
                    LOGGER.info(date + ": " + msg);
                    outMsg.println(msg);
                }
            }
        }).start();
    }
}
