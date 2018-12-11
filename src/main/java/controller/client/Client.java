package controller.client;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Client extends Thread {

    private String FilesDir;
    private int port_no;
    private ServerSocket serverSocket = null;
    private int peer_id;
    private static ArrayList<String> msg;

    public Client (int port, String FilesDir, int peer_id) {
        port_no = port;
        this.FilesDir = FilesDir;
        this.peer_id = peer_id;
        msg = new ArrayList<String>();
    }

    public void run() {
        try {
            serverSocket = new ServerSocket(port_no);
        } catch(IOException ie) {
            ie.printStackTrace();
        }

        //Accept() to create controller.server socket for every request
        while(true) {
            try {
                Socket socket = serverSocket.accept();
                System.out.println("Connected to controller.client at " + socket.getRemoteSocketAddress() + " with peer " + peer_id);
                new Search(socket, FilesDir, peer_id, msg).start();
            } catch(IOException io) {
                io.printStackTrace();
            }
        }
    }
}


