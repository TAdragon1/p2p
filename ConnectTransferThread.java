import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashSet;

public class ConnectTransferThread implements Runnable{

    private String ip;
    private String port;
    private String transfer;
    private HashSet<String> peerWideFileTransferRequestSet;

    public ConnectTransferThread(String ip, String port, String transfer, HashSet<String> peerWideFileTransferRequestSet){
        this.ip = ip;
        this.port = port;
        this.transfer = transfer;
        this.peerWideFileTransferRequestSet = peerWideFileTransferRequestSet;
    }

    @Override
    public void run() {
        try {
            String filename = transfer.split(":")[1];
            synchronized (peerWideFileTransferRequestSet) {
                if (!peerWideFileTransferRequestSet.contains(filename)) {
                    Socket connectionSocket = new Socket(ip, Integer.parseInt(port));
                    BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                    DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

                    Printer.print("Requesting a file transfer");
                    outToClient.writeBytes(transfer + "\n");

                    peerWideFileTransferRequestSet.add(filename);       // So as to not request file multiple times

                    // Response is file content
                    FileOutputStream obtainedFile = new FileOutputStream("obtained//" + filename);

                    String line = inFromClient.readLine();
                    obtainedFile.write(line.getBytes());

                    while (inFromClient.ready()) {
                        line = inFromClient.readLine();
                        obtainedFile.write(line.getBytes());
                    }
                    Printer.print("File transmission complete");

                    inFromClient.close();
                    outToClient.close();
                    connectionSocket.close();
                }
            }
        }
        catch (Exception e){
            System.out.println("Caught exception: " + e);
            e.printStackTrace();
        }
    }

}