package proxy;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import proxy.DBHandler;

import java.io.*;
import java.util.Base64;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.Arrays;
import java.lang.Integer;

public class EdgeHandler {
    public String[] server;
    public String status;
    public PrintWriter outToServer;
    public Scanner inFromServer;
    public Socket clientSocket;

    public EdgeHandler(String server) {

        DBHandler db = new DBHandler();

        this.server = db.getServerData(server).split(":");
        this.connect();
    }

    public void connect() {
        try {
            this.clientSocket = new Socket(this.server[0], Integer.parseInt(this.server[1]));
            this.outToServer = new PrintWriter(clientSocket.getOutputStream(), true);
            this.inFromServer = new Scanner(clientSocket.getInputStream());
            this.outToServer.println("HELLO");
            this.status = "SERVER_UP";

        } catch (Exception e) {
            System.out.println(e);
            this.status = "SERVER_DOWN";
        }
    }

    public void disconnect() {
        try {
            this.clientSocket.close();

        } catch (Exception e) {
            System.out.println(e);
            this.status = "SERVER_DOWN";
        }
    }

    public Boolean fileExists(String file) {

        this.outToServer.println("ls");
        String inText;
        Boolean flag = false;
        inText = "";

        if (this.status == "SERVER_DOWN") {
            return false;
        }

        while (!inText.equals("END")) {
            inText = inFromServer.nextLine();

            if (!inText.equals("END")) {
                if (inText.equals(file)) {
                    System.out.println(file + " " + inText);
                    flag = true;
                }
            }

        }
        return flag;

    }
}