package controller.notFile;

import controller.client.Client;
import controller.client.InsertFile;
import controller.client.Receiver;
import controller.server.Server;
import model.DataSet;
import utils.GlobalConfig;

import java.util.Properties;
import java.io.*;
import java.util.*;

public class notFile {

    private static String propertiesFile;
    private Properties properties;
    private int count = 0;
    private int peerID;
    private String filesDir;
    private Scanner scanner;

    public static void main(String[] args)  {

        notFile notFile = new notFile();

        propertiesFile = args[0];
        notFile.peerID = Integer.parseInt(args[1]);
        notFile.filesDir = args[2];

        System.out.println("Peer " + notFile.peerID + " started with shared directory " + notFile.filesDir);

        notFile.configurePeer(notFile.peerID, propertiesFile, notFile.filesDir);
        notFile.mainMenu();
    }

    private void mainMenu (){

        GlobalConfig.clearConsole();

        System.out.println("*****************");
        System.out.println("**** notFile ****");
        System.out.println("*****************");
        System.out.println("");
        System.out.println("Choose from these choices: ");
        System.out.println("1 - Search for a File");
        System.out.println("1.1 - Download a File");
        System.out.println("2 - Insert a File");
        System.out.println("3 - Info\n");

        scanner = new Scanner(System.in);
        int choice = scanner.nextInt();

        switch (choice) {
            case 1:
                System.out.println("\nEnter the DataSet to be searched: ");
                scanner.nextLine();
                String fileToSearch = scanner.nextLine();
                searchFile(fileToSearch);
                break;
            case 2:
                //TODO: files can be everywhere. change insert file dir
                System.out.println("\nEnter the DataSet to be inserted (inside files dir): ");
                scanner.nextLine();
                String newFile = scanner.nextLine();

                File f = new File(filesDir + "//" + newFile);
                if(f.isFile()) {

                    System.out.println("\nInsert DataSet duration: ");
                    int dataSetDuration = scanner.nextInt();

                    System.out.println("\nInsert number of participants: ");
                    int numParticipants = scanner.nextInt();

                    System.out.println("\nInsert participants type (humans, trucks, taxis, ...): ");
                    scanner.nextLine();
                    String participantsType = scanner.nextLine();

                    System.out.println("\nInsert number of records: ");
                    int numRecords = scanner.nextInt();

                    System.out.println("\nInsert license (GPL, ...): ");
                    scanner.nextLine();
                    String license = scanner.nextLine();

                    insertFile(peerID, newFile, dataSetDuration, numParticipants, participantsType, numRecords, license);
                } else {
                    System.out.println("Can't find file. You must write filename exactly including extension. ");
                    mainMenu();
                }
                break;
            case 3:
                systemInfo();
                break;
            case 0:
                // Perform "quit" case.
                break;
            default:

        }
    }

    private void configurePeer(int peerID, String propertiesFile, String filesDir){
        try {
            //Properties class to read the configuration file
            properties = new Properties();
            System.out.println("Selected the " + propertiesFile);
            InputStream is = new FileInputStream(propertiesFile);
            properties.load(is);

            // get peer controller.server port and start controller.server
            int serverPort = Integer.parseInt(properties.getProperty("peer" + peerID + ".serverport"));
            Server server = new Server(serverPort, filesDir);
            server.start();

            // get peer controller.client port and start controller.client
            int clientPort = Integer.parseInt(properties.getProperty("peer" + peerID + ".port"));
            Client client = new Client(clientPort, filesDir, peerID);
            client.start();

        } catch(IOException io) {
            io.printStackTrace();
        }
    }

    private void searchFile (String fileToSearch){
        ++count;
        String msgID = peerID + "." + count;
        String[] neighbours = properties.getProperty("peer" + peerID + ".next").split(","); 	//Creating a controller.client thread for every neighbouring peer
        int TTL_Value = neighbours.length;

        ArrayList<Thread> thread = new ArrayList<Thread>();
        ArrayList<ConnectionThread> peers = new ArrayList<ConnectionThread>();		//To store all controller.client threads

        for(String neighbour : neighbours) {
            int neighPort = Integer.parseInt(properties.getProperty("peer" + neighbour + ".port"));		// get neighbour port from config file
            int neighID = Integer.parseInt(neighbour);

            ConnectionThread cp = new ConnectionThread(neighPort, neighID, fileToSearch, msgID, peerID, TTL_Value);

            Thread t = new Thread(cp);
            t.start();
            thread.add(t);
            peers.add(cp);
        }

        for (Thread thread1 : thread) {
            try {
                //Wait until all the controller.client threads are done executing
                ((Thread) thread1).join();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        int[] peerswithfiles; //part on how to send data from the ConnectingPeer
        List<Integer> allPeersWithFile = new ArrayList<Integer>();

        for (ConnectionThread peer : peers) {

            //Reading the list of controller.client threads which contains files
            peerswithfiles = ((ConnectionThread) peer).getarray();

            if (peerswithfiles.length > 0) {
                for (int availablePeer : peerswithfiles) {
                    if (availablePeer != 0) {
                        allPeersWithFile.add(availablePeer);
                    }
                }
            }
        }

        if (!allPeersWithFile.isEmpty()) {
            System.out.println("\nPeers containing the file are: ");
            for (int availablePeers : allPeersWithFile) {
                System.out.println("> " + availablePeers);
            }
            downloadFile(fileToSearch);
        } else {
            System.out.println("No one has this file. Sorry. ");
            mainMenu();
        }
    }

    private void downloadFile (String filename){
        System.out.println("\nDo you wish to download this file? (y/N)");
        String ans = scanner.nextLine();
        if (ans.equalsIgnoreCase("y")) {
            System.out.println("Please selected a source: ");

            int peerFrom = scanner.nextInt();
            int portFrom = Integer.parseInt(properties.getProperty("peer" + peerFrom + ".serverport"));

            Receiver receiver = new Receiver(portFrom, filename, filesDir);
            receiver.run();
        }

        mainMenu();
    }

    private void insertFile (int peerID, String filename, int dataSetDuration, int numParticipants, String participantsType, int numRecords, String license){
        // TODO: check if all arguments are valid before creating new dataset

        DataSet dataSet = new DataSet(filename, dataSetDuration, numParticipants, participantsType, numRecords, license);
        InsertFile insertFile = new InsertFile(peerID, dataSet);
        insertFile.run();

        mainMenu();
    }

    private void systemInfo (){
        System.out.println("\nPeer " + peerID + " started with shared directory " + filesDir);
        System.out.println("Selected the " + propertiesFile);
        mainMenu();
    }

    public static String getPropertiesFile () {
        return propertiesFile;
    }

}
