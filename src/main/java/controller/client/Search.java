package controller.client;

import controller.notFile.ConnectionThread;
import controller.notFile.notFile;

import model.DataSet;
import utils.MessageFormat;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static utils.GlobalConfig.*;

public class Search extends Thread {

    private Socket socket;
    private String FileDirectory;
    private int peer_id;
    private ArrayList<String> peerMessages;
    private ArrayList<Thread> thread = new ArrayList<Thread>();
    private ArrayList<ConnectionThread> peerswithfiles = new ArrayList<ConnectionThread>();
    private int[] peersArray_list = new int[20];
    private int countofpeers = 0;

    private InputStream is = null;


    Search(Socket socket, String FileDirectory, int peer_id, ArrayList<String> peerMessages) {
        this.socket = socket;
        this.FileDirectory = FileDirectory;
        this.peer_id = peer_id;
        this.peerMessages = peerMessages;
    }

    public void run() {

        try {
            is = socket.getInputStream();
            ObjectInputStream ois = new ObjectInputStream(is);
            OutputStream os = socket.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(os);

            boolean isMessageDuplicated;

            //reading the serialized PeerMessageID class
            MessageFormat MF = (MessageFormat) ois.readObject();

            System.out.println("Got request from peer: " + MF.getFromPeerId());

            // check if message is duplicate by ID
            isMessageDuplicated = this.peerMessages.contains(MF.getMsgId());


            if(!isMessageDuplicated) {
                this.peerMessages.add(MF.getMsgId());

                String query = MF.getQuery();
                System.out.println("Searching with query: " + query);

                searchFileLocally(query);
                sendSearchRequestToNeighbours(MF, query);
            } else {
                System.out.println("Duplicated Message");
            }

            oos.writeObject(peersArray_list);

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void searchFileLocally (String query) {
        File newfind;
        File directoryObj = new File(FileDirectory);
        String[] filesList = directoryObj.list();
        for (String s : filesList) {
            newfind = new File(s);
            if (newfind.getName().equals(query)) {
                System.out.println("You have this file");
                peersArray_list[countofpeers++] = peer_id;
                break;
            }
        }

        parseSearchRequest(query);
    }

    private void sendSearchRequestToNeighbours (MessageFormat MF, String query) {
        try {
            Properties properties = new Properties();
            String propertiesFile = notFile.getPropertiesFile();
            is = new FileInputStream(propertiesFile);
            properties.load(is);

            String temp = properties.getProperty("peer" + peer_id + ".next");

            if (temp != null && MF.getTTL_value() > 0) {
                String[] neighbours = temp.split(",");

                for (String neighbour : neighbours) {

                    //create controller.client thread for all neighbouring peers
                    if (MF.getFromPeerId() == Integer.parseInt(neighbour)) {
                        continue;
                    }

                    int connectingport = Integer.parseInt(properties.getProperty("peer" + neighbour + ".port"));
                    int neighbouringpeer = Integer.parseInt(neighbour);

                    int TTL_value = MF.getTTL_value();
                    System.out.println("You don't have this file. Sending the request to neighbour peer: " + neighbouringpeer);
                    ConnectionThread cp = new ConnectionThread(connectingport, neighbouringpeer, query, MF.getMsgId(), peer_id, TTL_value--);
                    Thread t = new Thread(cp);
                    t.start();
                    thread.add(t);
                    peerswithfiles.add(cp);

                }
            }

            for (Thread thread1 : thread) {
                ((Thread) thread1).join();
            }

            for (ConnectionThread peerswithfile : peerswithfiles) {

                int[] a = ((ConnectionThread) peerswithfile).getarray();
                for (int i : a) {
                    if (i == 0)
                        break;

                    peersArray_list[countofpeers++] = i;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseSearchRequest (String query) {
        List<String> result = Arrays.asList(query.split("\\s*,\\s*"));

        for (String attribute : result) {
            if (attribute.contains("name")) {
                String[] name = parseEachAttribute(attribute);
                checkIfAnyFilesChecksSearch(name);
            } else if (attribute.contains("duration")) {
                String[] duration = parseEachAttribute(attribute);
                checkIfAnyFilesChecksSearch(duration);
            } else if (attribute.contains("numParticipants")) {
                String[] numParticipants = parseEachAttribute(attribute);
                checkIfAnyFilesChecksSearch(numParticipants);
            } else if (attribute.contains("participantsType")) {
                String[] participantsType = parseEachAttribute(attribute);
                checkIfAnyFilesChecksSearch(participantsType);
            } else if (attribute.contains("numRecords")) {
                String[] numRecords = parseEachAttribute(attribute);
                checkIfAnyFilesChecksSearch(numRecords);
            } else if (attribute.contains("license")) {
                String[] license = parseEachAttribute(attribute);
                checkIfAnyFilesChecksSearch(license);
            }
        }
    }

    private String[] parseEachAttribute (String attribute) {
        String[] parts = attribute.split(String.format(WITH_DELIMITER, "(<|=|>)"));
        System.out.println("Attr: " + parts[0]);
        System.out.println("Op: " + parts[1]);
        System.out.println("Opr: " + parts[2]);

        return parts;
    }

    private void checkIfAnyFilesChecksSearch(String[] attributes) {
        String[] files = null;
        File configFile = new File(CONFIG_FOLDER + "Peer" + peer_id + "//" + USER_FILES);
        BufferedReader reader = null;
        String csvSplitBy = ",";
        String line = "";

        try {
            reader = new BufferedReader(new FileReader(configFile));

            while ((line = reader.readLine()) != null) {
                // use comma as separator
                files = line.split(csvSplitBy);

                DataSet userDataSet = new DataSet(files[0], Integer.parseInt(files[1]), Integer.parseInt(files[2]), files[3], Integer.parseInt(files[4]), files[5]);

                //TODO: compare user's query with files that neighbours have
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                System.out.println("Can't close buffer. ");
            }
        }
    }
}
