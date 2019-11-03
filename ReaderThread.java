import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

public class ReaderThread implements Runnable {

    private Socket connectionSocket;
    private HashSet<String> receivedLog;
    private PriorityQueue<Message> outgoingMessages;
    private Object heartbeatObject;
    private Set<String> localFiles;
    private String localIP;
    private String fileTransferPort;
    private PriorityQueue<Message> peerWideForwarding;

    private static String HEART = "Heart";
    private static String BEAT = "beat";

    public ReaderThread(Socket connectionSocket, HashSet<String> receivedLog, PriorityQueue<Message> outgoingMessages,
                        Object heartbeatObject, Set<String> localFiles, String localIP,
                        String fileTransferPort, PriorityQueue<Message> peerWideForwarding){
        this.connectionSocket = connectionSocket;
        this.receivedLog = receivedLog;
        this.outgoingMessages = outgoingMessages;
        this.heartbeatObject = heartbeatObject;
        this.localFiles = localFiles;
        this.localIP = localIP;
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
                Printer.print("Reader received: " + message);

                synchronized (receivedLog) {
                    if (!receivedLog.contains(message)) {        // New Incoming Message
                        if (!message.equals(HEART) && !message.equals(BEAT)) {
                            receivedLog.add(message);
                        }

                        Thread readerHelperThread = new Thread(new ReaderHelperThread(message, outgoingMessages, heartbeatObject,
                                localFiles, localIP, fileTransferPort, peerWideForwarding));
                        readerHelperThread.start();
                    }
                    // Else do nothing, we've already handled this before.
                }
            }
        }
        catch (Exception e) {

        }
    }
}
