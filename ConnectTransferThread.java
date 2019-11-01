import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.LinkedList;

public class ConnectTransferThread implements Runnable{

    private String ip;
    private String port;
    private String transfer;

    public ConnectTransferThread(String ip, String port, String transfer){
        this.ip = ip;
        this.port = port;
        this.transfer = transfer;
    }

    @Override
    public void run() {
        try {
            Socket connectionSocket = new Socket(ip, Integer.parseInt(port));
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

            System.out.println("Requesting a file transfer");
            outToClient.writeBytes(transfer + "\n");
            String filename = transfer.split(":")[1];

            // Response is file content
            FileOutputStream obtainedFile = new FileOutputStream("obtained//" + filename);
            while (inFromClient.ready()) {
                String line = inFromClient.readLine();
                obtainedFile.write(line.getBytes());
            }
            System.out.println("File transmission complete");

            inFromClient.close();
            outToClient.close();
            connectionSocket.close();
        }
        catch (Exception e){

        }
    }

}