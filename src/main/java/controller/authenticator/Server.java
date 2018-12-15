package controller.authenticator;

import model.Researcher;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

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
                socket = serverSocket.accept();
            } catch (IOException io) {
                System.out.println("Can't accept client connection. ");
            }
            new Authentication(socket).start();
        }
    }

}

class Authentication extends Thread{

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

                if (registerUser(firstname, lastname, username, password, team)) {
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

    private boolean registerUser(String firstname, String lastname, String username, String password, String team){
        Researcher researcher = new Researcher(getID(), firstname, lastname, username, password, team);
        return persistUser(researcher);
    }

    private boolean persistUser(Researcher researcher){
        boolean isUserPersisted = false;
        File directory = new File(CONFIG_FOLDER + "server");
        if (!directory.exists()){
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

    private int getID() {
        String[] userAttributes = null;
        File tableUsers = new File(CONFIG_FOLDER + SERVER_CONFIG_FOLDER + TABLE_USERS);
        BufferedReader reader = null;
        String csvSplitBy = ", ";
        String line = "";
        int lastID = 0;

        try {
            reader = new BufferedReader(new FileReader(tableUsers));

            while ((line = reader.readLine()) != null) {
                // use comma as separator
                userAttributes = line.split(csvSplitBy);

                lastID = Integer.parseInt(userAttributes[0]);
            }

        } catch (IOException e) {
            System.out.println("Couldn't find users table. Creating new one... ");
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
