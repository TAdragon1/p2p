import java.net.*;
import java.util.*;

public class NeighborServer implements Runnable{

    private ServerSocket welcomeSocket;
    private Set<String> localFiles;
    private String peerIP;
    private String peerFileTransferIP;
    private String peerFileTransferPort;
    private PriorityQueue<Message> peerWideForwarding;
    private List<PriorityQueue<Message>> neighborOutgoingQueues;

    private static Comparator<Message> messageComparator = new Comparator<Message>() {
        @Override
        public int compare(Message m1, Message m2) {
            return m1.getPriority() - m2.getPriority();
        }
    };

    private static int INITIAL_CAPACITY = 10;

    public NeighborServer(ServerSocket welcomeSocket, Set<String> localFiles, String peerIP, String peerFileTransferIP,
                          String peerFileTransferPort, PriorityQueue<Message> peerWideForwarding,
                          List<PriorityQueue<Message>> neighborOutgoingQueues){
        this.welcomeSocket = welcomeSocket;
        this.localFiles = localFiles;
        this.peerIP = peerIP;
        this.peerFileTransferIP = peerFileTransferIP;
        this.peerFileTransferPort = peerFileTransferPort;
        this.peerWideForwarding = peerWideForwarding;
        this.neighborOutgoingQueues = neighborOutgoingQueues;
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
                ReaderThread neighborReader =
                        new ReaderThread(connectionSocket, neighborOutgoingMessages, heartbeatObject, localFiles,
                                peerIP, peerFileTransferIP, peerFileTransferPort, peerWideForwarding);
                Thread neighborReaderThread = new Thread(neighborReader);
                neighborReaderThread.start();

                neighborOutgoingQueues.add(neighborOutgoingMessages);
            }
        }
        catch (Exception e){

        }
    }

}     