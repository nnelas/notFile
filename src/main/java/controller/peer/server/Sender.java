package controller.peer.server;

import java.io.*;
import java.net.Socket;

class Sender extends Thread {

    private String sharedDirectory;
    private Socket socket;
    private String filename;

    Sender(Socket socket, String FileDir) {
        this.socket = socket;
        this.sharedDirectory = FileDir;
    }

    public void run() {

        InputStream is = null;
        ObjectInputStream ois = null;
        InputStream in = null;
        OutputStream out = null;

        try {
            is = socket.getInputStream();            //Connecting controller.peer.client.Client acting as a controller.peer.server to the file requesting controller.peer.client.Client
            ois = new ObjectInputStream(is);
            filename = (String) ois.readObject();                    //Filename to be downloaded
        } catch (Exception ex){
            System.out.println("Can't receive filename. ");
        }

        try {
            System.out.println("Preparing to send " + filename);
            File file = new File(sharedDirectory + "//" + filename);
            // Get the size of the file
            byte[] buffer = new byte[4096];
            in = new FileInputStream(file);
            out = socket.getOutputStream();

            int count;
            while ((count = in.read(buffer)) > 0) {
                System.out.println("\nSending... ");
                out.write(buffer, 0, count);
            }

            out.close();
            in.close();
            socket.close();

            System.out.println("File was successfully sent. ");

        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
