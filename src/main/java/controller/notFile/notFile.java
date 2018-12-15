package controller.notFile;

import controller.peer.client.Client;
import controller.peer.client.InsertFile;
import controller.peer.client.Receiver;
import controller.peer.server.Server;
import model.DataSet;
import utils.GlobalConfig;

import java.util.Properties;
import java.io.*;
import java.util.*;

import static utils.GlobalConfig.CONFIG_FOLDER;
import static utils.GlobalConfig.USER_FILES;

public class notFile {

    private static String propertiesFile;
    private Properties properties;
    private int count = 0;
    private int peerID;
    private String filesDir;
    private Scanner scanner;

    public static void main(String[] args) throws IOException {

        notFile notFile = new notFile();

        Login login = new Login();
        login.loginMenu();

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
        System.out.println("\nChoose from these options: ");
        System.out.println("1 - Search for a File");
        System.out.println("1.1 - Download a File");
        System.out.println("2 - Insert a File");
        System.out.println("3 - List all my files");
        System.out.println("4 - Info\n");
        System.out.println("0 - Quit\n");

        scanner = new Scanner(System.in);
        int choice = scanner.nextInt();

        switch (choice) {
            case 1:
                System.out.println("\nYou can search a DataSet by: ");
                System.out.println("> name, ");
                System.out.println("> duration, ");
                System.out.println("> number of participants (numParticipants), ");
                System.out.println("> participants type (participantsType), ");
                System.out.println("> number of records (numRecords), ");
                System.out.println("> license");
                System.out.println("\n\nFor searching by one of these attributes, please query your input in this format: ");
                System.out.println("\n\tduration>10 or participantsType=Taxis or numRecords<9");
                System.out.println("\nAny attribute that is not represented, will not be considered. ");
                System.out.println("Search engine can only consider one attribute at a time. ");
                System.out.println("White-spaces or tabs are not allowed. ");
                scanner.nextLine();
                String query = scanner.nextLine();
                searchFile(query);
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
                seeAllMyFiles();
                break;
            case 4:
                systemInfo();
                break;
            case 0:
                System.exit(0);
                break;
            default:
                System.out.println("Invalid option. :(");
                mainMenu();

        }
    }

    private void configurePeer(int peerID, String propertiesFile, String filesDir){
        try {
            //Properties class to read the configuration file
            properties = new Properties();
            System.out.println("Selected the " + propertiesFile);
            InputStream is = new FileInputStream(propertiesFile);
            properties.load(is);

            // get peer controller.peer.server port and start controller.peer.server
            int serverPort = Integer.parseInt(properties.getProperty("peer" + peerID + ".serverport"));
            Server server = new Server(serverPort, filesDir);
            server.start();

            // get peer controller.peer.client port and start controller.peer.client
            int clientPort = Integer.parseInt(properties.getProperty("peer" + peerID + ".port"));
            Client client = new Client(clientPort, filesDir, peerID);
            client.start();

        } catch(IOException io) {
            io.printStackTrace();
        }
    }


    private void searchFile (String query){
        ++count;
        String msgID = peerID + "." + count;
        String[] neighbours = properties.getProperty("peer" + peerID + ".next").split(","); 	//Creating a controller.peer.client thread for every neighbouring peer
        int TTL_Value = neighbours.length;
        System.out.println(query);

        ArrayList<Thread> thread = new ArrayList<Thread>();
        ArrayList<ConnectionThread> peers = new ArrayList<ConnectionThread>();		//To store all controller.peer.client threads

        for(String neighbour : neighbours) {
            int neighPort = Integer.parseInt(properties.getProperty("peer" + neighbour + ".port"));		// get neighbour port from config file
            int neighID = Integer.parseInt(neighbour);

                ConnectionThread cp = new ConnectionThread(neighPort, neighID, query, msgID, peerID, TTL_Value);

            Thread t = new Thread(cp);
            t.start();
            thread.add(t);
            peers.add(cp);
        }

        for (Thread thread1 : thread) {
            try {
                //Wait until all the controller.peer.client threads are done executing
                ((Thread) thread1).join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        ArrayList<DataSet> peersFiles; //part on how to send data from the ConnectingPeer
        ArrayList<DataSet> allPeersWithFile = new ArrayList<DataSet>();

        for (ConnectionThread peer : peers) {

            //Reading the list of controller.peer.client threads which contains files
            peersFiles = ((ConnectionThread) peer).getarray();

            if (!peersFiles.isEmpty()) {
                for (DataSet dataSet : peersFiles) {
                    if (!dataSet.getOwner().equals("Peer" + peerID)) {
                        allPeersWithFile.add(dataSet);
                    }
                }
            }
        }

        if (!allPeersWithFile.isEmpty()) {
            System.out.println("\nFound this: ");
            for (DataSet dataSet : allPeersWithFile) {
                System.out.println("> From " + dataSet.getOwner());
                System.out.println("\t> name: " + dataSet.getName());
                System.out.println("\t> duration: " + dataSet.getDuration());
                System.out.println("\t> number of participants (numParticipants): " + dataSet.getNumParticipants());
                System.out.println("\t> participants type (participantsType): " + dataSet.getParticipantsType());
                System.out.println("\t> number of records (numRecords): " + dataSet.getNumRecords());
                System.out.println("\t> license: " + dataSet.getLicense());
            }

            for (DataSet dataSet : allPeersWithFile) {
                System.out.println("\n> From " + dataSet.getOwner());
                System.out.println("\t> name: " + dataSet.getName());
                System.out.println("Do you wish to download this DataSet? (y/N/q)");
                String ans = scanner.nextLine();

                if (ans.equalsIgnoreCase("y")) {
                    downloadFile(dataSet);
                }

                if(ans.equals("q")) {
                    break;
                }
            }
        } else {
            System.out.println("No one has this type of DataSet. Sorry. ");
        }

        mainMenu();
    }

    private void downloadFile (DataSet dataSet){
        int portFrom = Integer.parseInt(properties.getProperty("peer" + Integer.parseInt(dataSet.getOwner().replaceAll("Peer", "")) + ".serverport"));

        Receiver receiver = new Receiver(portFrom, dataSet.getName(), filesDir);
        receiver.run();

        // after completed download, it will add new file to list
        insertFile(peerID, dataSet.getName(), dataSet.getDuration(), dataSet.getNumParticipants(), dataSet.getParticipantsType(), dataSet.getNumRecords(), dataSet.getLicense());
    }

    private void insertFile (int peerID, String filename, int dataSetDuration, int numParticipants, String participantsType, int numRecords, String license){
        // TODO: check if all arguments are valid before creating new dataset

        DataSet dataSet = new DataSet("Peer" + peerID, filename, dataSetDuration, numParticipants, participantsType, numRecords, license);
        InsertFile insertFile = new InsertFile(peerID, dataSet);
        insertFile.run();

        mainMenu();
    }

    private void seeAllMyFiles (){
        String[] files = null;
        File file = new File(CONFIG_FOLDER + "Peer" + peerID + "//" + USER_FILES);
        BufferedReader reader = null;
        String csvSplitBy = ",";
        String line = "";

        try {
            reader = new BufferedReader(new FileReader(file));

            while ((line = reader.readLine()) != null) {
                // use comma as separator
                files = line.split(csvSplitBy);

                System.out.println("File [name = " + files[1] +
                        ", duration = " + files[2] +
                        ", numParticipants = " + files[3] +
                        ", participantsType = " + files[4] +
                        ", numRecords = " + files[5] +
                        ", license = " + files[6] +"]");
            }

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
