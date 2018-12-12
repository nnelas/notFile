package controller.notFile;

import utils.MessageFormat;

import java.net.Socket;
import java.io.IOException;
import java.io.*;

import static utils.GlobalConfig.SERVER_NAME;

public class ConnectionThread extends Thread {

    private int portofconnection;
    private int peertoconnect;
    private String query;
    private int[] peersArray;
    private String msgid;
    private int frompeer_id;
    private int TTL_value;

    public ConnectionThread(int portofconnection, int peertoconnect, String query, String msgid, int frompeer_id, int TTL_value) {
        this.portofconnection = portofconnection;
        this.peertoconnect = peertoconnect;
        this.query = query;
        this.msgid = msgid;
        this.frompeer_id = frompeer_id;
        this.TTL_value = TTL_value;
    }

    public void run() {

        try {
            Socket socket = new Socket(SERVER_NAME, portofconnection);

            OutputStream os = socket.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(os);
            InputStream is = socket.getInputStream();
            ObjectInputStream ois = new ObjectInputStream(is);

            //writing the data to be serialized and send to the controller.server thread
            MessageFormat MF = new MessageFormat(query, msgid, frompeer_id, TTL_value);
            oos.writeObject(MF);

            peersArray = (int[])ois.readObject();

        } catch(IOException io) {
            io.printStackTrace();
        } catch(ClassNotFoundException cp) {
            cp.printStackTrace();
        }
    }

    public int[] getarray() {
        return peersArray;
    }
}

