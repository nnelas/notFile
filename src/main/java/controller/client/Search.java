package controller.client;

import controller.notFile.ConnectionThread;
import controller.notFile.notFile;

import utils.MessageFormat;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;

public class Search extends Thread {

    private Socket socket;
    private String FileDirectory;
    private int peer_id;
    private ArrayList<String> peerMessages;
    private ArrayList<Thread> thread = new ArrayList<Thread>();
    private ArrayList<ConnectionThread> peerswithfiles = new ArrayList<ConnectionThread>();
    private int[] peersArray_list = new int[20];
    private int countofpeers = 0;

    Search(Socket socket, String FileDirectory, int peer_id, ArrayList<String> peerMessages) {
        this.socket = socket;
        this.FileDirectory = FileDirectory;
        this.peer_id = peer_id;
        this.peerMessages = peerMessages;
    }

    public void run() {

        try {

            InputStream is = socket.getInputStream();
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
            } else {
                System.out.println("Duplicated Message");
            }

            String fname = MF.getFname();
            System.out.println("Searching for file: " + fname);

            if(!isMessageDuplicated) {
                File newfind;
                File directoryObj = new File(FileDirectory);
                String[] filesList = directoryObj.list();
                for (String s : filesList) {
                    newfind = new File(s);
                    if (newfind.getName().equals(fname)) {
                        System.out.println("You have this file");
                        peersArray_list[countofpeers++] = peer_id;
                        break;
                    }
                }

                Properties properties = new Properties();
                String propertiesFile = notFile.getPropertiesFile();
                is = new FileInputStream(propertiesFile);
                properties.load(is);

                String temp = properties.getProperty("peer" + peer_id + ".next");

                if(temp != null && MF.getTTL_value() > 0) {
                    //System.out.println("entered inside the loop");
                    String[] neighbours=temp.split(",");

                    for (String neighbour : neighbours) {

                        //creat controller.client thread for all neighbouring peers
                        if (MF.getFromPeerId() == Integer.parseInt(neighbour)) {
                            continue;
                        }

                        int connectingport = Integer.parseInt(properties.getProperty("peer" + neighbour + ".port"));
                        int neighbouringpeer = Integer.parseInt(neighbour);

                        int TTL_value = MF.getTTL_value();
                        System.out.println("You don't have this file. Sending the request to neighbour peer: " + neighbouringpeer);
                        ConnectionThread cp = new ConnectionThread(connectingport, neighbouringpeer, fname, MF.getMsgId(), peer_id, TTL_value--);
                        Thread t = new Thread(cp);
                        t.start();
                        thread.add(t);
                        peerswithfiles.add(cp);

                    }
                }
                //oos.writeObject(p);
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
            }

            oos.writeObject(peersArray_list);

        } catch(Exception e) {

            e.printStackTrace();

        }
    }
}
