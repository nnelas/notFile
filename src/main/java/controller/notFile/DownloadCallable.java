package controller.notFile;

import model.FileInfo;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.Callable;

import static utils.GlobalConfig.SERVER_NAME;

/**
 * @author pedro.martins
 */
public class DownloadCallable implements Callable<FileInfo> {

    private FileInfo fileInfo;

    public DownloadCallable(FileInfo fileInfo) {
        this.fileInfo=fileInfo;
    }

    @Override
    public FileInfo call() throws Exception {

        Socket socket = null;
        InputStream in = null;
        ObjectInputStream ois = null;
        ObjectOutputStream ooos = null;

        //Abre uma ligação com o peer
        try {
            socket = new Socket(SERVER_NAME, fileInfo.getPortNumber());
        } catch (IOException ex) {
            System.out.println("Can't setup socket on this port number. ");
        }

        //Envia para o Sender o nome do ficheiro pretendido
        try {
            // send filename to controller.server
            ooos = new ObjectOutputStream(socket.getOutputStream());
            ooos.flush();
            ooos.writeObject(fileInfo);
        } catch (IOException ex){
            System.out.println("Can't send filename to peer. ");
        }

        try {
            in = socket.getInputStream();            //Connecting controller.client.Client acting as a controller.server to the file requesting controller.client.Client
            ois = new ObjectInputStream(in);
            fileInfo = (FileInfo) ois.readObject();
        } catch (IOException ex) {
            System.out.println("Can't get socket input stream. ");
        }finally {
            ooos.close();
            socket.close();
            in.close();
            ois.close();
        }

        return fileInfo;
    }
}
