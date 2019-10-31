import java.io.*;
import java.net.Socket;

public class AcceptTransferThread implements Runnable {

    private Socket connectionSocket;

    public AcceptTransferThread(Socket connectionSocket){
        this.connectionSocket = connectionSocket;
    }

    @Override
    public void run() {
        try {
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

            DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

            String transfer = inFromClient.readLine();
            // Transfer format: "T:(filename)"
            String filename = transfer.split(":")[1];

            BufferedReader fileToWrite = new BufferedReader(new FileReader(new File("shared//" + filename)));

            while (fileToWrite.ready()) {
                outToClient.writeBytes(fileToWrite.readLine());
            }

            fileToWrite.close();
            inFromClient.close();
            outToClient.close();
            connectionSocket.close();
        }
        catch (Exception e){

        }
    }
}
