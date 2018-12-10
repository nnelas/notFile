import utils.MessageFormat;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Properties;

import static utils.GlobalConfig.SERVER_NAME;

public class Client extends Thread {

    private String FilesDir;
    private int port_no;
    private ServerSocket serverSocket = null;
    private Socket socket = null;
    private int peer_id;
    private static ArrayList<String> msg;

    Client (int port, String FilesDir, int peer_id) {
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

        //Accept() to create server socket for every request
        while(true) {
            try {
                socket = serverSocket.accept();
                System.out.println("Connected to client at " + socket.getRemoteSocketAddress() + " with peer " + peer_id);
                new Search(socket, FilesDir, peer_id, msg).start();
            } catch(IOException io) {
                io.printStackTrace();
            }
        }
    }
}


class Search extends Thread {

    protected Socket socket;
    private String FileDirectory;
    private String fname;
    private int peer_id;
    private ArrayList<String> peerMessages;
    private ArrayList<Thread> thread = new ArrayList<Thread>();
    private ArrayList<ConnectionThread> peerswithfiles = new ArrayList<ConnectionThread>();
    private int[] peersArray_list = new int[20];
    private int[] a = new int[20];
    private int countofpeers = 0;
    private MessageFormat MF = new MessageFormat();

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

            MF = (MessageFormat)ois.readObject();					//reading the serialized PeerMessageID class

            System.out.println("Got request from peer: " + MF.fromPeerId);

            // check if message is duplicate by ID
            isMessageDuplicated = this.peerMessages.contains(MF.msgId);

            if(isMessageDuplicated == false) {
                this.peerMessages.add(MF.msgId);
            } else {
                System.out.println("Duplicated Message");
            }

            fname = MF.fname;
            System.out.println("Searching for file: " + fname);

            if(!isMessageDuplicated) {
                File newfind;
                File directoryObj = new File(FileDirectory);
                String[] filesList = directoryObj.list();
                for (int j = 0; j < filesList.length; j++) {
                    newfind = new File(filesList[j]);
                    if(newfind.getName().equals(fname)) {
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

                if(temp!=null && MF.TTL_value>0) {
                    //System.out.println("entered inside the loop");
                    String[] neighbours=temp.split(",");

                    for(int i=0;i<neighbours.length;i++) {

                        //creat client thread for all neighbouring peers
                        if(MF.fromPeerId == Integer.parseInt(neighbours[i])) {
                            continue;
                        }

                        int connectingport = Integer.parseInt(properties.getProperty("peer"+neighbours[i]+".port"));
                        int neighbouringpeer = Integer.parseInt(neighbours[i]);

                        System.out.println("You don't have this file. Sending the request to neighbour peer: " + neighbouringpeer);
                        ConnectionThread cp = new ConnectionThread(connectingport,neighbouringpeer,fname,MF.msgId,peer_id,MF.TTL_value--);
                        Thread t = new Thread(cp);
                        t.start();
                        thread.add(t);
                        peerswithfiles.add(cp);

                    }
                }
                //oos.writeObject(p);
                for(int i=0;i<thread.size();i++) {
                    ((Thread) thread.get(i)).join();
                }

                for(int i=0;i<peerswithfiles.size();i++) {

                    a=((ConnectionThread)peerswithfiles.get(i)).getarray();
                    for(int j=0;j<a.length;j++) {
                        if(a[j]==0)
                            break;

                        peersArray_list[countofpeers++] = a[j];
                    }
                }
            }

            oos.writeObject(peersArray_list);

        } catch(Exception e) {

            e.printStackTrace();

        }
    }
}

class Receiver extends Thread {

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
            // send filename to server
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