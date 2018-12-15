package controller.peer.client;

import model.DataSet;

import java.io.*;

import static utils.GlobalConfig.CONFIG_FOLDER;
import static utils.GlobalConfig.USER_FILES;


public class InsertFile extends Thread {

    private DataSet dataSet;
    private int peerID;

    public InsertFile(int peerID, DataSet dataSet) {
        this.peerID = peerID;
        this.dataSet = dataSet;
    }

    public void run() {
        FileWriter writer = null;

        File directory = new File(CONFIG_FOLDER + "Peer" + peerID);
        if (!directory.exists()){
            directory.mkdir();
        }

        try {
            writer = new FileWriter(directory + "//" + USER_FILES,true); //the true will append the new data
            writer.write(dataSet.getOwner() + ", " + dataSet.getName() + ", " + dataSet.getDuration() + ", " +
                    dataSet.getNumParticipants() + ", " + dataSet.getParticipantsType()  + ", " +
                    dataSet.getNumRecords() + ", " + dataSet.getLicense());//appends the string to the file
            writer.write(System.getProperty("line.separator"));
        } catch (IOException ex) {
            System.out.println("Can't write files list. ");
        } finally {
            try {writer.close();} catch (Exception ex) {/*ignore*/}
        }
    }
}
