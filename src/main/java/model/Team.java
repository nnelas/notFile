package model;

import java.io.Serializable;

public class Team implements Serializable {

    private int id;
    private String name;

    public Team(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTeam(){
        return id + ", " + name;
    }
}
