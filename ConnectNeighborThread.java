import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

public class ConnectNeighborThread implements Runnable{

    private Socket connectionSocket;
    private LinkedList<String> outgoingQueries;
    private LinkedList<String> incomingResponses;
    private List<String> sentQueries;

    public ConnectNeighborThread(Socket connectionSocket, LinkedList<String> outgoingQueries,
                                 LinkedList<String> incomingResponses, List<String> sentQueries){
        this.connectionSocket = connectionSocket;
        this.outgoingQueries = outgoingQueries;
        this.incomingResponses = incomingResponses;
        this.sentQueries = sentQueries;
    }

    @Override
    public void run() {
        try {
            while (true) {
                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

                // Wait on outgoing queries
                if (outgoingQueries.size() == 0) {
                    outgoingQueries.wait();
                }

                outToClient.writeBytes(outgoingQueries.pop() + "\n");

                // Response format: "R:(query id);(peer IP:port);(filename)"
                String response = inFromClient.readLine();
                String responseData = response.substring(2);
                String queryID = responseData.split(";")[0];

                boolean atOriginPeer = sentQueries.contains(queryID);
                if (atOriginPeer) {
                    String ipAndPort = responseData.split(";")[1];
                    String ip = ipAndPort.split(":")[0];
                    String port = ipAndPort.split(":")[1];
                    String filename = responseData.split(";")[2];

                    String transfer = "T:" + filename;
                    ConnectTransferThread connectTransferThread = new ConnectTransferThread(ip, port, transfer);
                    Thread thread = new Thread(connectTransferThread);
                }
                else {
                    incomingResponses.add(response);
                    incomingResponses.notify();
                }

                inFromClient.close();
                outToClient.close();
            }
        }
        catch (Exception e){

        }
    }

}