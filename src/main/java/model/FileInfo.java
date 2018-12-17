package model;

import java.io.Serializable;

public class FileInfo implements Serializable {

    private String fileName;
    private int portNumber;
    private int totalNumberOfPeers;
    private byte[] fileData;
    private int myPart;

    public FileInfo(String fileName, int portNumber, int totalNumberOfPeers, int myPart){
        this.fileName=fileName;
        this.portNumber = portNumber;
        this.totalNumberOfPeers=totalNumberOfPeers;
        this.myPart=myPart;
    }

    public String getFileName() {
        return fileName;
    }

    public int getPortNumber() {
        return portNumber;
    }

    public int getTotalNumberOfPeers() {
        return totalNumberOfPeers;
    }

    public byte[] getFileData() {
        return fileData;
    }

    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }

    public int getMyPart() {
        return myPart;
    }

}
