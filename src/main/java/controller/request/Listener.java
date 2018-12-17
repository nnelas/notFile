package controller.request;

import controller.request.interfaces.MessageListener;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static utils.GlobalConfig.*;

public class Listener implements MessageListener {

    public Object parseMessage(int _id, Object obj) {
        //System.out.println("(Direct Message Received) " + obj);
        saveLog(_id, obj);
        return "success";
    }

    private void saveLog (int peerID, Object obj) {
        FileWriter writer = null;
        File directory;

        Date date=new Date();
        System.out.println();

        if (peerID == 0 || peerID == 1000){
            directory = new File(CONFIG_FOLDER + SERVER_CONFIG_FOLDER);
        } else {
            directory = new File(CONFIG_FOLDER + "Peer" + peerID);
        }
        if (!directory.exists()){
            directory.mkdir();
        }

        try {
            writer = new FileWriter(directory + "//" + PUBSUB_LOG,true); //the true will append the new data
            writer.write(new SimpleDateFormat("yyyyMMdd  HH:mm:ss").format(date) + ": " + (String) obj);//appends the string to the file
            writer.write(System.getProperty("line.separator"));
        } catch (IOException ex) {
            System.out.println("Can't write log. ");
        } finally {
            try {writer.close();} catch (Exception ex) {/*ignore*/}
        }
    }
}
