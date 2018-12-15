package model;

import java.io.Serializable;
import java.util.ArrayList;

public class Team implements Serializable {

    private int id;
    private String name;
    private ArrayList<Researcher> researchers;

    public Team(int id, String name, ArrayList<Researcher> researchers) {
        this.id = id;
        this.name = name;
        this.researchers = researchers;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Researcher> getResearchers() {
        return researchers;
    }

    public void addResearcherToTeam(Researcher researcher) {
        researchers.add(researcher);
    }
}
