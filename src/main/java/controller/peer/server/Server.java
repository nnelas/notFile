package controller.peer.server;

import java.io.*;
import java.net.*;

public class Server extends Thread {

    private int portno;
    private String FileDirectory;
    private ServerSocket serverSocket;
    private Socket socket;

    public Server(int portno, String FileDirectory) {
        this.portno = portno;
        this.FileDirectory = FileDirectory;
    }

    public void run() {

        try {
            serverSocket = new ServerSocket(portno);
        } catch(IOException io) {
            System.out.println("Can't setup controller.peer.server on this port number. ");
        }

        try {
            socket = serverSocket.accept();
        } catch(IOException io) {
            System.out.println("Can't accept controller.peer.client connection. ");
        }

        new Sender(socket, FileDirectory).start();
    }
}

