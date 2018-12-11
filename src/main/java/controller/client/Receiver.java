package controller.client;

import java.io.*;
import java.net.Socket;

import static utils.GlobalConfig.SERVER_NAME;

public class Receiver extends Thread {

    private int portFrom;
    private String filename;
    private String filesDir;

    public Receiver(int portFrom, String filename, String filesDir) {
        this.portFrom = portFrom;
        this.filename = filename;
        this.filesDir = filesDir;
    }

    public void run() {
        Socket socket = null;
        InputStream in = null;
        OutputStream out = null;

        try {
            socket = new Socket(SERVER_NAME, portFrom);
        } catch (IOException ex) {
            System.out.println("Can't setup socket on this port number. ");
        }

        try {
            // send filename to controller.server
            ObjectOutputStream ooos = new ObjectOutputStream(socket.getOutputStream());
            ooos.flush();
            ooos.writeObject(filename);
        } catch (IOException ex){
            System.out.println("Can't send filename to peer. ");
        }

        try {
            in = socket.getInputStream();
        } catch (IOException ex) {
            System.out.println("Can't get socket input stream. ");
        }

        try {
            out = new FileOutputStream(filesDir + "//" + filename);
        } catch (FileNotFoundException ex) {
            System.out.println("File not found. ");
        }

        try {
            byte[] buffer = new byte[4096];

            int count;
            while ((count = in.read(buffer)) > 0) {
                System.out.println("\nReceiving... ");
                out.write(buffer, 0, count);
            }

            out.close();
            in.close();
            socket.close();

        } catch(IOException e) {
            System.out.println("Something went wrong. Can't receive file. ");
        }
    }
}
