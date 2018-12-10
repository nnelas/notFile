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
        System.out.println("1 - Search for a file");
        System.out.println("1.1 - Download a file");
        System.out.println("2 - ");
        System.out.println("3 - Info\n");

        scanner = new Scanner(System.in);
        int choice = scanner.nextInt();

        switch (choice) {
            case 1:
                System.out.println("\nEnter the file to be searched: ");
                scanner.nextLine();
                String fileToSearch = scanner.nextLine();
                searchFile(fileToSearch);
                break;
            case 2:

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

            // get peer server port and start server
            int serverPort = Integer.parseInt(properties.getProperty("peer" + peerID + ".serverport"));
            Server server = new Server(serverPort, filesDir);
            server.start();

            // get peer client port and start client
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
        String[] neighbours = properties.getProperty("peer" + peerID + ".next").split(","); 	//Creating a client thread for every neighbouring peer
        int TTL_Value = neighbours.length;

        ArrayList<Thread> thread = new ArrayList<Thread>();
        ArrayList<ConnectionThread> peers = new ArrayList<ConnectionThread>();		//To store all client threads

        for(String neighbour : neighbours) {
            int neighPort = Integer.parseInt(properties.getProperty("peer" + neighbour + ".port"));		// get neighbour port from config file
            int neighID = Integer.parseInt(neighbour);

            ConnectionThread cp = new ConnectionThread(neighPort, neighID, fileToSearch, msgID, peerID, TTL_Value);

            Thread t = new Thread(cp);
            t.start();
            thread.add(t);
            peers.add(cp);
        }

        for (int i = 0; i < thread.size(); i++) {
            try {
                //Wait until all the client threads are done executing
                ((Thread) thread.get(i)).join();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        int[] peerswithfiles; //part on how to send data from the ConnectingPeer

        System.out.println("\nPeers containing the file are: ");

        for(int i=0; i<peers.size(); i++) {

            //Reading the list of client threads which contains files
            peerswithfiles = ((ConnectionThread)peers.get(i)).getarray();

            for(int j=0; j < peerswithfiles.length; j++) {
                if(peerswithfiles[j] == 0) {
                    break;
                }

                System.out.println(peerswithfiles[j]);
            }
        }

        System.out.println("\nDo you wish to download this file? (y/N)");
        String ans = scanner.nextLine();
        if (ans.equalsIgnoreCase("y")) {
            System.out.println("Please selected a source: ");

            int peerFrom = scanner.nextInt();
            int portFrom = Integer.parseInt(properties.getProperty("peer" + peerFrom + ".serverport"));
            downloadFile(portFrom, fileToSearch, filesDir);
        } else if (ans.equalsIgnoreCase("n")){
            mainMenu();
        }
    }

    private void downloadFile (int portFrom, String filename, String filesDir){
        Receiver receiver = new Receiver(portFrom, filename, filesDir);
        receiver.run();

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
