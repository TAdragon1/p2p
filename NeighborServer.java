import java.net.*;
import java.util.LinkedList;
import java.util.Set;

public class NeighborServer implements Runnable{

    private ServerSocket welcomeSocket;
    private LinkedList<String> outgoingQueries;
    private LinkedList<String> incomingResponses;
    private InetAddress fileTransferInetAddress;
    private int fileTransferPort;
    private Set<String> localFiles;

    public NeighborServer(ServerSocket welcomeSocket, LinkedList<String> outgoingQueries,
                          LinkedList<String> incomingResponses, Set<String> localFiles,
                          InetAddress fileTransferInetAddress, int fileTransferPort){
        this.welcomeSocket = welcomeSocket;
        this.outgoingQueries = outgoingQueries;
        this.incomingResponses = incomingResponses;
        this.localFiles = localFiles;
        this.fileTransferInetAddress = fileTransferInetAddress;
        this.fileTransferPort = fileTransferPort;
    }

    @Override
    public void run() {
        try{
            while(true) {
                Socket connectionSocket = welcomeSocket.accept();
                System.out.println("Accepted connection from another peer");

                // TODO create threads per neighbor

            }
        }
        catch (Exception e){

        }
    }

}     