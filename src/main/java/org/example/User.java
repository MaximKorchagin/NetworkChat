package org.example;

import java.io.PrintWriter;
import java.net.Socket;

public class User {
    private final Socket clientSocket;
    private final PrintWriter outMsg;
    private String name;

    public User(Socket clientSocket, PrintWriter outMsg) {
        this.clientSocket = clientSocket;
        this.outMsg = outMsg;
    }

    public void sendMsg(String msg) {
        outMsg.println(msg);
        outMsg.flush();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}