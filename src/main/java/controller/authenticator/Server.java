package controller.authenticator;

import model.Researcher;
import model.Team;
import utils.GlobalConfig;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import static utils.GlobalConfig.*;

public class Server {

    private ServerSocket serverSocket;
    private Socket socket;

    public static void main(String[] args) {
        System.out.println("\t> notFile Authenticator Server <");
        Server server = new Server();
        server.configServer();
    }

    private void configServer() {
        try {
            System.out.println("Creating socket...");
            serverSocket = new ServerSocket(SERVER_PORT);
        } catch (IOException io) {
            System.out.println("Can't setup server on this port number. ");
        }

        while (true) {
            try {
                System.out.println("Ready to accept clients...");
                new serverMenu().start();
                socket = serverSocket.accept();
            } catch (IOException io) {
                System.out.println("Can't accept client connection. ");
            }
            new Authentication(socket).start();
        }
    }


}

class serverMenu extends Thread {

    public void run (){
        serverMenu();
    }

    private void serverMenu() {
        String firstname;
        String lastname;
        String username;
        String password;
        String team;
        Persist persist = new Persist();

        GlobalConfig.clearConsole();

        System.out.println("\nChoose from these options: ");
        System.out.println("1 - Add User");
        System.out.println("2 - Add Team");
        System.out.println("3 - Add User to Team\n");
        System.out.println("0 - Quit\n");

        Scanner scanner = new Scanner(System.in);
        int choice = scanner.nextInt();

        switch (choice) {
            case 1:
                System.out.println("\nPlease enter your first name: ");
                scanner.nextLine();
                firstname = scanner.nextLine();
                System.out.println("\nPlease enter your last name: ");
                lastname = scanner.nextLine();
                System.out.println("\nPlease enter your username: ");
                username = scanner.nextLine();
                System.out.println("\nPlease enter your password: ");
                password = scanner.nextLine();
                System.out.println("\nPlease enter your team: ");
                team = scanner.nextLine();
                persist.registerUser(firstname, lastname, username, password, team);
                break;
            case 2:
                System.out.println("\nPlease enter team name: ");
                scanner.nextLine();
                team = scanner.nextLine();
                persist.persistTeam(new Team(persist.getID(TABLE_TEAMS), team));
                break;
            case 3:
                //System.out.println("\nPlease enter team id: ");
                //scanner.nextLine();
                //team = scanner.nextLine();
                //persist.persistTeam(new Team(persist.getID(TABLE_TEAMS), team));
                break;
            default:
                System.out.println("Invalid option. :(");
        }

        serverMenu();
    }
}

class Authentication extends Thread {

    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;

    private String option;

    Authentication(Socket socket) {
        this.socket = socket;
    }

    public void run(){
        System.out.println("New client at " + socket.getLocalAddress());

        try {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException ex) {
            System.out.println("Can't create stream with client. ");
        }

        try {
            option = input.readLine();
        } catch (IOException ex) {
            System.out.println("Couldn't receive option. ");
        }

        try {
            String username;
            String password;
            Persist persist = new Persist();

            if(option.equals(USER_LOGIN)){
                username = input.readLine();
                password = input.readLine();

                if(loginUser(username, password)){
                    output.println("Welcome, " + username);
                    System.out.println("User " + username + " authenticated. ");
                } else {
                    output.println("Login failed. ");
                    System.out.println("User " + username + " tried to authenticate. Failed. ");
                }

                output.flush();
                output.close();

            } else if(option.equals(USER_REGISTER)){
                String firstname = input.readLine();
                String lastname = input.readLine();
                username = input.readLine();
                password = input.readLine();
                String team = input.readLine();

                if (persist.registerUser(firstname, lastname, username, password, team)) {
                    output.println("User " + username + " successfully added to table! ");
                    System.out.println("User " + username + " added successfully to table! ");
                } else {
                    output.println("Register failed. ");
                    System.out.println("User " + username + " tried to authenticate. Failed. ");
                }

                output.flush();
                output.close();
            }
        } catch(IOException e) {
            System.out.println("Can't accept client connection. ");
        }
    }

    private boolean loginUser(String username, String password) {
        boolean isLoginValid = false;
        String[] userAttributes = null;
        File tableUsers = new File(CONFIG_FOLDER + SERVER_CONFIG_FOLDER + TABLE_USERS);
        BufferedReader reader = null;
        String csvSplitBy = ", ";
        String line = "";

        try {
            reader = new BufferedReader(new FileReader(tableUsers));

            while ((line = reader.readLine()) != null) {
                // use comma as separator
                userAttributes = line.split(csvSplitBy);

                Researcher researcher = new Researcher(Integer.parseInt(userAttributes[0]),
                                                                        userAttributes[1],
                                                                        userAttributes[2],
                                                                        userAttributes[3],
                                                                        userAttributes[4],
                                                                        userAttributes[5]);

                if (researcher.getUsername().equals(username) && researcher.getPassword().equals(password)){
                    isLoginValid = true;
                    break;
                }
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

        return isLoginValid;
    }
}

class Persist extends Thread {

    boolean registerUser(String firstname, String lastname, String username, String password, String team){
        Researcher researcher = new Researcher(getID(TABLE_USERS), firstname, lastname, username, password, team);
        return persistUser(researcher);
    }

    private boolean persistUser(Researcher researcher){
        boolean isUserPersisted = false;
        File directory = new File(CONFIG_FOLDER + "server");
        if (!directory.exists()) {
            directory.mkdir();
        }

        try {
            System.out.println("Adding new user to table... ");
            BufferedWriter writer = new BufferedWriter(new FileWriter(directory + "//" + TABLE_USERS, true));
            writer.append(researcher.getResearcher());
            writer.write(System.getProperty("line.separator"));
            writer.close();
            isUserPersisted = true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return isUserPersisted;
    }

    void persistTeam(Team team){
        File directory = new File(CONFIG_FOLDER + "server");
        if (!directory.exists()){
            directory.mkdir();
        }

        try {
            System.out.println("Adding new team to table... ");
            BufferedWriter writer = new BufferedWriter(new FileWriter(directory + "//" + TABLE_TEAMS, true));
            writer.append(team.getTeam());
            writer.write(System.getProperty("line.separator"));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    int getID(String option) {
        String[] attributes = null;
        File table = null;

        if (option.equalsIgnoreCase(TABLE_USERS)) {
            table = new File(CONFIG_FOLDER + SERVER_CONFIG_FOLDER + TABLE_USERS);
        } else if (option.equalsIgnoreCase(TABLE_TEAMS)) {
            table = new File(CONFIG_FOLDER + SERVER_CONFIG_FOLDER + TABLE_TEAMS);
        }
        BufferedReader reader = null;
        String csvSplitBy = ", ";
        String line = "";
        int lastID = 0;

        try {
            reader = new BufferedReader(new FileReader(table));

            while ((line = reader.readLine()) != null) {
                // use comma as separator
                attributes = line.split(csvSplitBy);

                lastID = Integer.parseInt(attributes[0]);
            }

        } catch (IOException e) {
            System.out.println("Couldn't find table. Creating new one... ");
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                System.out.println("Can't close buffer. ");
            }
        }
        return ++lastID;
    }
}
