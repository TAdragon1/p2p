import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.PriorityQueue;
import java.util.Set;

public class ReaderThread implements Runnable {

    private Socket connectionSocket;
    private PriorityQueue<Message> outgoingMessages;
    private Object someObject;
    private Set<String> localFiles;
    private String localIP;
    private String fileTransferIP;
    private String fileTransferPort;
    private PriorityQueue<Message> peerWideForwarding;

    public ReaderThread(Socket connectionSocket, PriorityQueue<Message> outgoingMessages, Object someObject,
                        Set<String> localFiles, String localIP, String fileTransferIP, String fileTransferPort,
                        PriorityQueue<Message> peerWideForwarding){
        this.connectionSocket = connectionSocket;
        this.outgoingMessages = outgoingMessages;
        this.someObject = someObject;
        this.localFiles = localFiles;
        this.localIP = localIP;
        this.fileTransferIP = fileTransferIP;
        this.fileTransferPort = fileTransferPort;
        this.peerWideForwarding = peerWideForwarding;
    }

    @Override
    public void run() {
        try{
            while (true)
            {
                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                String message = inFromClient.readLine();

                Thread readerHelperThread = new Thread(new ReaderHelperThread(message, outgoingMessages, someObject,
                        localFiles, localIP, fileTransferIP, fileTransferPort, peerWideForwarding));
                readerHelperThread.start();
            }
        }
        catch (Exception e) {

        }
    }
}
