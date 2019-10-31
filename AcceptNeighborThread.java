import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Set;

public class AcceptNeighborThread implements Runnable{

    private Socket connectionSocket;
    private LinkedList<String> outgoingQueries;
    private LinkedList<String> incomingResponses;
    private Set<String> localFiles;
    private InetAddress fileTransferInetAddress;
    private int fileTransferPort;

    public AcceptNeighborThread(Socket connectionSocket, LinkedList<String> outgoingQueries,
                                LinkedList<String> incomingResponses, Set<String> localFiles,
                                InetAddress fileTransferInetAddress, int fileTransferPort){
        this.connectionSocket = connectionSocket;
        this.outgoingQueries = outgoingQueries;
        this.incomingResponses = incomingResponses;
        this.localFiles = localFiles;
        this.fileTransferInetAddress = fileTransferInetAddress;
        this.fileTransferPort = fileTransferPort;
    }

    @Override
    public void run() {
        try {
            while (true) {
                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

                DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

                // Wait on query format: "Q:(query ID);(file name)"
                String clientSentence = inFromClient.readLine();
                String queryData = clientSentence.split(":")[1];        //  "(query ID);(file name)"
                String queryID = queryData.split(";")[0];
                String filename = queryData.split(";")[1];

                // TODO store query ID to identify duplicates

                boolean onThisPeer = localFiles.contains(filename);

                if (onThisPeer) {
                    String ip = fileTransferInetAddress.getHostAddress();
                    String port = String.valueOf(fileTransferPort);
                    String response = "R:" + queryID + ";" + ip + ":" + port + ";" + filename;

                    // Response format: "R:(query id);(peer IP:port);(filename)"
                    outToClient.writeBytes(response + "\n");
                }
                else {
                    outgoingQueries.add(clientSentence);
                    outgoingQueries.notify();

                    if (incomingResponses.size() == 0) {
                        incomingResponses.wait();
                    }

                    String response = incomingResponses.pop();
                    outToClient.writeBytes(response + "\n");
                }

                inFromClient.close();
                outToClient.close();
            }
        }
        catch (Exception e){

        }
    }

}