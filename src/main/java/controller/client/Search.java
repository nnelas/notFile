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
    private ArrayList<ConnectionThread> listOfConnectionThreads = new ArrayList<ConnectionThread>();
    private ArrayList<DataSet> files;

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

            oos.writeObject(files);

        } catch(Exception e) {
            e.printStackTrace();
        }
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
                    listOfConnectionThreads.add(cp);

                }
            }

            for (Thread thread1 : thread) {
                ((Thread) thread1).join();
            }

            for (ConnectionThread peerswithfile : listOfConnectionThreads) {

                ArrayList<DataSet> a = ((ConnectionThread) peerswithfile).getarray();
                files.addAll(a);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void searchFileLocally (String query) {
        List<String> result = Arrays.asList(query.split("\\s*,\\s*"));

        for (String attribute : result) {
            if (attribute.toLowerCase().contains("name")) {
                String[] name = parseSearchRequest(attribute);
                checkIfAnyFileChecksSearch(name);
            } else if (attribute.toLowerCase().contains("duration")) {
                String[] duration = parseSearchRequest(attribute);
                checkIfAnyFileChecksSearch(duration);
            } else if (attribute.toLowerCase().contains("numparticipants")) {
                String[] numParticipants = parseSearchRequest(attribute);
                checkIfAnyFileChecksSearch(numParticipants);
            } else if (attribute.toLowerCase().contains("participantstype")) {
                String[] participantsType = parseSearchRequest(attribute);
                checkIfAnyFileChecksSearch(participantsType);
            } else if (attribute.toLowerCase().contains("numrecords")) {
                String[] numRecords = parseSearchRequest(attribute);
                checkIfAnyFileChecksSearch(numRecords);
            } else if (attribute.toLowerCase().contains("license")) {
                String[] license = parseSearchRequest(attribute);
                checkIfAnyFileChecksSearch(license);
            }
        }
    }

    private String[] parseSearchRequest (String attribute) {
        return attribute.split(String.format(WITH_DELIMITER, "(<|=|>)"));
    }

    private void checkIfAnyFileChecksSearch(String[] attributes) {
        files = new ArrayList<DataSet>();
        String[] userFiles = null;
        File configFile = new File(CONFIG_FOLDER + "Peer" + peer_id + "//" + USER_FILES);
        BufferedReader reader = null;
        String csvSplitBy = ", ";
        String line = "";

        try {
            reader = new BufferedReader(new FileReader(configFile));

            while ((line = reader.readLine()) != null) {
                // use comma as separator
                userFiles = line.split(csvSplitBy);

                DataSet userDataSet = new DataSet(userFiles[0], userFiles[1], Integer.parseInt(userFiles[2]), Integer.parseInt(userFiles[3]), userFiles[4], Integer.parseInt(userFiles[5]), userFiles[6]);

                if (attributes[0].equalsIgnoreCase("name")){
                    if (attributes[2].equalsIgnoreCase(userDataSet.getName())){
                        files.add(userDataSet);
                    }
                } else if (attributes[0].equalsIgnoreCase("duration")){
                    if (attributes[1].equals("<")){
                        if (userDataSet.getDuration() < Integer.parseInt(attributes[2])){
                            files.add(userDataSet);
                        }
                    } else if (attributes[1].equals("=")){
                        if (userDataSet.getDuration() == Integer.parseInt(attributes[2])){
                            files.add(userDataSet);
                        }
                    } else if (attributes[1].equals(">")){
                        if (userDataSet.getDuration() > Integer.parseInt(attributes[2])){
                            files.add(userDataSet);
                        }
                    }
                } else if (attributes[0].equalsIgnoreCase("numParticipants")){
                    if (attributes[1].equals("<")){
                        if (userDataSet.getNumParticipants() < Integer.parseInt(attributes[2])){
                            files.add(userDataSet);
                        }
                    } else if (attributes[1].equals("=")){
                        if (userDataSet.getNumParticipants() == Integer.parseInt(attributes[2])){
                            files.add(userDataSet);
                        }
                    } else if (attributes[1].equals(">")){
                        if (userDataSet.getNumParticipants() > Integer.parseInt(attributes[2])){
                            files.add(userDataSet);
                        }
                    }
                } else if (attributes[0].equalsIgnoreCase("participantsType")){
                    if (attributes[2].equalsIgnoreCase(userDataSet.getParticipantsType())){
                        files.add(userDataSet);
                    }
                } else if (attributes[0].equalsIgnoreCase("numRecords")){
                    if (attributes[1].equals("<")){
                        if (userDataSet.getNumRecords() < Integer.parseInt(attributes[2])){
                            files.add(userDataSet);
                        }
                    } else if (attributes[1].equals("=")){
                        if (userDataSet.getNumRecords() == Integer.parseInt(attributes[2])){
                            files.add(userDataSet);
                        }
                    } else if (attributes[1].equals(">")){
                        if (userDataSet.getNumRecords() > Integer.parseInt(attributes[2])){
                            files.add(userDataSet);
                        }
                    }
                } else if (attributes[0].equalsIgnoreCase("license")){
                    if (attributes[2].equalsIgnoreCase(userDataSet.getLicense())){
                        files.add(userDataSet);
                    }
                }
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
