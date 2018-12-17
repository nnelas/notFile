package model;

import java.io.Serializable;
import java.util.Objects;

public class DataSet implements Serializable {

    private String owner;
    private String name;
    private int duration;
    private int numParticipants;
    private String participantsType;
    private int numRecords;
    private String license;

    public DataSet(String owner, String name, int duration, int numParticipants, String participantsType, int numRecords, String license) {
        this.owner = owner;
        this.name = name;
        this.duration = duration;
        this.numParticipants = numParticipants;
        this.participantsType = participantsType;
        this.numRecords = numRecords;
        this.license = license;
    }

    public String getOwner() {
        return owner;
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

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + ((name!=null) ? name.hashCode() : 0);
        hash = 31 * hash + ((participantsType!=null) ? participantsType.hashCode() : 0);
        hash = 31 * hash + ((license!=null) ? license.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof DataSet)) {
            return false;
        }
        return Objects.equals(duration, ((DataSet) obj).getDuration()) &&
                Objects.equals(numParticipants, ((DataSet) obj).getNumParticipants()) &&
                Objects.equals(participantsType, ((DataSet) obj).getParticipantsType()) &&
                Objects.equals(numRecords, ((DataSet) obj).getNumRecords()) &&
                Objects.equals(license, ((DataSet) obj).getLicense());
    }
}
