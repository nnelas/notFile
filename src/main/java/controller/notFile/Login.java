package controller.notFile;

import org.apache.commons.codec.digest.DigestUtils;
import utils.GlobalConfig;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

import static utils.GlobalConfig.*;

class Login {

    private Socket socket;
    private PrintWriter output;
    private BufferedReader read;

    void loginMenu() {
        String firstname;
        String lastname;
        String username;
        String password;
        String team;

        GlobalConfig.clearConsole();

        System.out.println("*****************");
        System.out.println("**** notFile ****");
        System.out.println("*****************");
        System.out.println("\nChoose from these options: ");
        System.out.println("1 - Login");
        System.out.println("2 - Register\n");
        System.out.println("0 - Quit\n");

        Scanner scanner = new Scanner(System.in);
        int choice = scanner.nextInt();

        switch (choice) {
            case 1:
                if(connectToServer()){
                    System.out.println("\nPlease enter your username: ");
                    scanner.nextLine();
                    username = scanner.nextLine();
                    System.out.println("\nPlease enter your password: ");
                    password = scanner.nextLine();
                    login(username, password);
                }
                break;
            case 2:
                if(connectToServer()){
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
                    register(firstname, lastname, username, password, team);
                }
                break;
            default:
                System.out.println("Invalid option. :(");
                loginMenu();
        }
    }

    private boolean connectToServer(){
        boolean isConnected;
        try {
            socket = new Socket(SERVER_NAME, AUTH_SERVER_PORT);
            output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            read = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            isConnected = true;
        } catch (IOException ex){
            System.out.println("Couldn't create socket. ");
            isConnected = false;
        }
        return isConnected;
    }

    private void login(String username, String password) {
        System.out.println("Trying to authenticate... ");

        output.println(USER_LOGIN);
        output.println(username);
        output.println(hashPasswordString(password));
        output.flush();

        waitForServerAns();
    }

    private void register(String firstname, String lastname, String username, String password, String team){
        output.println(USER_REGISTER);

        output.println(firstname);
        output.println(lastname);
        output.println(username);
        output.println(hashPasswordString(password));
        output.println(team);
        output.flush();

        waitForServerAns();
    }

    private String hashPasswordString (String password){
        return DigestUtils.sha512Hex(password);

    }

    private void waitForServerAns (){
        try {
            //read response from server
            String response = read.readLine();

            if (!response.contains("failed")){
                System.out.println(response + "\n");
            } else {
                System.out.println(response + "\n");
                loginMenu();
            }
        } catch (IOException ex) {
            System.out.println("Couldn't create stream. ");
        } finally {
            try {
                socket.close();
                output.close();
                read.close();
            } catch (IOException ignored) {

            }
        }
    }
}
