import java.net.*;
import java.util.*;

public class NeighborServer implements Runnable{

    private ServerSocket welcomeSocket;
    private Set<String> localFiles;
    private String peerIP;
    private String peerFileTransferPort;
    private PriorityQueue<Message> peerWideForwarding;
    private List<PriorityQueue<Message>> neighborOutgoingQueues;
    private HashSet<String> peerWideFileTransferRequestSet;

    private static Comparator<Message> messageComparator = new Comparator<Message>() {
        @Override
        public int compare(Message m1, Message m2) {
            return m1.getPriority() - m2.getPriority();
        }
    };

    private static int INITIAL_CAPACITY = 10;

    public NeighborServer(ServerSocket welcomeSocket, Set<String> localFiles, String peerIP,
                          String peerFileTransferPort, PriorityQueue<Message> peerWideForwarding,
                          List<PriorityQueue<Message>> neighborOutgoingQueues,
                          HashSet<String> peerWideFileTransferRequestSet){
        this.welcomeSocket = welcomeSocket;
        this.localFiles = localFiles;
        this.peerIP = peerIP;
        this.peerFileTransferPort = peerFileTransferPort;
        this.peerWideForwarding = peerWideForwarding;
        this.neighborOutgoingQueues = neighborOutgoingQueues;
        this.peerWideFileTransferRequestSet = peerWideFileTransferRequestSet;
    }

    @Override
    public void run() {
        try{
            while(true) {
                Socket connectionSocket = welcomeSocket.accept();
                Printer.print("Accepted connection from another peer");

                PriorityQueue<Message> neighborOutgoingMessages =
                        new PriorityQueue<>(INITIAL_CAPACITY, messageComparator);
                HashSet<String> neighborSentLog = new HashSet<>();
                WriterThread neighborWriter =
                        new WriterThread(connectionSocket, neighborOutgoingMessages, neighborSentLog);
                Thread neighborWriterThread = new Thread(neighborWriter);
                neighborWriterThread.start();

                Object heartbeatObject = new Object();
                HashSet<String> neighborReceivedLog = new HashSet<>();
                ReaderThread neighborReader =
                        new ReaderThread(connectionSocket, neighborReceivedLog, neighborOutgoingMessages, heartbeatObject, localFiles,
                                peerIP, peerFileTransferPort, peerWideForwarding, peerWideFileTransferRequestSet);
                Thread neighborReaderThread = new Thread(neighborReader);
                neighborReaderThread.start();

                neighborOutgoingQueues.add(neighborOutgoingMessages);
            }
        }
        catch (Exception e){

        }
    }

}     