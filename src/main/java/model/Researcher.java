package model;

import java.io.Serializable;
public class Researcher implements Serializable {

    private int id;
    private String firstname;
    private String lastname;
    private String username;
    private String password;
    private String team;

    public Researcher(int id, String firstname, String lastname, String username, String password, String team) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.username = username;
        this.password = password;
        this.team = team;
    }

    public int getId() {
        return id;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getTeam() {
        return team;
    }

    public String getResearcher(){
        return id + ", " + firstname + ", " + lastname + ", " + username + ", " + password + ", " + team;
    }
}
