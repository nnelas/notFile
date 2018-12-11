package model;

public class DataSet {

    private String name;
    private int duration;
    private int numParticipants;
    private String participantsType;
    private int numRecords;
    private String license;

    public DataSet(String name, int duration, int numParticipants, String participantsType, int numRecords, String license) {
        this.name = name;
        this.duration = duration;
        this.numParticipants = numParticipants;
        this.participantsType = participantsType;
        this.numRecords = numRecords;
        this.license = license;
    }

    public String getName() {
        return name;
    }

    public int getDuration() {
        return duration;
    }

    public int getNumParticipants() {
        return numParticipants;
    }

    public String getParticipantsType() {
        return participantsType;
    }

    public int getNumRecords() {
        return numRecords;
    }

    public String getLicense() {
        return license;
    }
}
