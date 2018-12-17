package controller.peer.client;

import controller.notFile.DownloadCallable;
import model.FileInfo;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Receiver extends Thread {

    private List<Integer> portsFrom;
    private String filename;
    private String filesDir;

    public Receiver(List<Integer> portsFrom, String filename, String filesDir) {
        this.portsFrom = portsFrom;
        this.filename = filename;
        this.filesDir = filesDir;
    }

    public void run() {
        OutputStream out = null;
        InputStream in = null;
        byte[] fileStream = null;

        List<Future> allFutures = new ArrayList<>();
        for(int i = 0; i < portsFrom.size(); i++){
            Future<FileInfo> future = service.submit(new DownloadCallable(new FileInfo(filename, portsFrom.get(i), portsFrom.size(), i)));
            allFutures.add(future);
        }

        //Esperar que todos os future acabem e juntar o conteudo para uma inputSteam
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        for(int i = 0; i < portsFrom.size(); i++){
            Future<FileInfo> future = allFutures.get(i);
            try {
                outputStream.write(future.get().getFileData());
            } catch (InterruptedException | ExecutionException | IOException e) {
                e.printStackTrace();
            }
        }
        fileStream = outputStream.toByteArray();
        in = new ByteArrayInputStream(fileStream);

        //escreve o in para o ficheiro
        try {
            out = new FileOutputStream(filesDir + "//" + filename);
        } catch (FileNotFoundException ex) {
            System.out.println("File not found. ");
        }

        try {

            System.out.println("\nReceiving... ");
            out.write(fileStream);

            out.close();
            in.close();
            outputStream.close();

            System.out.println("File was successfully received. ");

        } catch(IOException e) {
            System.out.println("Something went wrong. Can't receive file. ");
        }
    }
}
