import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class p2p {

    // Ports
    private static LinkedList<Integer> availablePortNums = readInPeer();

    // Client Sockets
    private static LinkedList<Socket> neighbors = new LinkedList<>();

    // Neighbor Hostnames
    private static List<String> neighborHostnames = readInNeighbors();

    // Files
    private static Set<String> localFiles = readInFiles();

    private static LinkedList<String> outgoingQueries = new LinkedList<>();
    private static LinkedList<String> incomingResponses = new LinkedList<>();

    private static String peer_hostname = "eecslab-10.case.edu";
    private static String peer_IP;
    private static AtomicInteger queryNum = new AtomicInteger(0);

    private static Comparator<Message> messageComparator = new Comparator<Message>() {
        @Override
        public int compare(Message m1, Message m2) {
            return m1.getPriority() - m2.getPriority();
        }
    };

    private static int INITIAL_CAPACITY = 10;
    private static PriorityQueue<Message> peerWideForwarding;

    public static void main(String[] args){
        Scanner scan;

        try {
            /* File Transfer Welcome Socket */
            ServerSocket fileTransferWelcomeSocket = new ServerSocket(nextPortNumber());
            FileTransferServer fileTransferServer = new FileTransferServer(fileTransferWelcomeSocket);
            Thread fileTransferServerThread = new Thread(fileTransferServer);

            InetAddress fileXferAddress;
            fileXferAddress = fileTransferWelcomeSocket.getInetAddress();
            int fileXferPort = fileTransferWelcomeSocket.getLocalPort();

            String peerFileTransferIP = fileXferAddress.getHostAddress();
            String peerFileTransferPort = String.valueOf(fileXferPort);

            /* Neighbor Welcome Socket */
            peer_IP = InetAddress.getByName(peer_hostname).getHostAddress();
            List<PriorityQueue<Message>> neighborOutgoingQueues = new LinkedList<>();

            ServerSocket neighborWelcomeSocket = new ServerSocket(nextPortNumber());
            NeighborServer neighborServer =
                    new NeighborServer(neighborWelcomeSocket, localFiles, peer_IP, peerFileTransferIP,
                            peerFileTransferPort, peerWideForwarding, neighborOutgoingQueues);
            Thread neighborTCPServerThread = new Thread(neighborServer);
            System.out.println("Peer started, peer ip = " + peer_IP);

            scan = new Scanner(System.in);
            while(true){
                String cmd = scan.nextLine();
                String[] strings = cmd.split(" ");
                String checker = strings[0].toLowerCase();

                switch(checker){
                    case "connect":
                        // Connect to each neighbor
                        for(String hostname : neighborHostnames){
                            System.out.println("Attempt to connect to neighbor: " + hostname);
                            try {
                                Socket connectionSocket = new Socket(hostname, nextPortNumber());
                                neighbors.add(connectionSocket);
                                System.out.println("Connection succeeded");
                            }
                            catch (Exception e) {
                                System.out.println("Connection failed");
                            }
                        }

                        for(Socket neighbor : neighbors){
                            PriorityQueue<Message> neighborOutgoingMessages =
                                    new PriorityQueue<>(INITIAL_CAPACITY, messageComparator);
                            HashSet<String> neighborSentLog = new HashSet<>();
                            WriterThread neighborWriter =
                                    new WriterThread(neighbor, neighborOutgoingMessages, neighborSentLog);
                            Thread neighborWriterThread = new Thread(neighborWriter);

                            Object heartbeatObject = new Object();
                            ReaderThread neighborReader =
                                    new ReaderThread(neighbor, neighborOutgoingMessages, heartbeatObject, localFiles,
                                            peer_IP, peerFileTransferIP, peerFileTransferPort, peerWideForwarding);
                            Thread neighborReaderThread = new Thread(neighborReader);

                            HeartbeatTimer neighborHeartbeatTimer =
                                    new HeartbeatTimer(neighbor, neighborOutgoingMessages, heartbeatObject);
                            Thread neighborHeartbeatTimerThread = new Thread(neighborHeartbeatTimer);

                            neighborOutgoingQueues.add(neighborOutgoingMessages);
                        }

                        peerWideForwarding = new PriorityQueue<>(INITIAL_CAPACITY, messageComparator);
                        PeerWideForwardingThread peerWideForwardingThread = new PeerWideForwardingThread(peerWideForwarding, neighborOutgoingQueues);
                        Thread pwfThread = new Thread(peerWideForwardingThread);
                        break;
                    case "get":
                        // Send query
                        String fileName = strings[1];
                        System.out.println("DEBUG: Get case " + fileName);

                        String queryID = peer_IP + "-" + String.valueOf(queryNum);
                        queryNum.incrementAndGet();

                        // query format: "Q:(query ID);(file name)"
                        outgoingQueries.add("Q:" + queryID + ";" + fileName);
                        outgoingQueries.add("Q:" + queryID + ";" + fileName);
                        outgoingQueries.notifyAll();

                        break;
                    case "leave":
                        // Close all connections with neighbors
                        System.out.println("DEBUG: Leave case");

                        for (Socket socket : neighbors) {
                            if (!socket.isClosed()){
                                socket.close();
                            }
                        }

                        break;
                    case "exit":
                        // Close all open connections and terminate
                        System.out.println("DEBUG: Exit case");

                        for (Socket socket : neighbors) {
                            if (!socket.isClosed()){
                                socket.close();
                            }
                        }

                        if (!neighborWelcomeSocket.isClosed()){
                            neighborWelcomeSocket.close();
                        }
                        if (!fileTransferWelcomeSocket.isClosed()){
                            fileTransferWelcomeSocket.close();
                        }

                        return;
                    default:
                        System.out.println("Debug: Default case");
                }
            }

        }
        catch (Exception e){
            System.out.println("Caught exception: " + e);
            e.printStackTrace();
        }

    }

    synchronized static private Integer nextPortNumber(){
        return availablePortNums.pop();
    }

    private static LinkedList<Integer> readInPeer() {
        LinkedList<Integer> portnums = new LinkedList<>();

        try {
            Scanner reader = new Scanner(new File("config_peer.txt"));

            while (reader.hasNext()){
                String line = reader.nextLine();
                Integer i = Integer.parseInt(line);
                portnums.add(i);
            }
        }
        catch (FileNotFoundException fileNotFoundException){
            System.out.println("File Not Found");
        }

        for (Integer item : portnums){
            System.out.println(item);
        }

        return portnums;
    }

    private static LinkedList<String> readInNeighbors() {
        LinkedList<String> hostnames = new LinkedList<>();

        try {
            Scanner reader = new Scanner(new File("config_neighbors.txt"));

            while (reader.hasNext()){
                String line = reader.nextLine();
                hostnames.add(line);
            }
        }
        catch (FileNotFoundException fileNotFoundException){
            System.out.println("File Not Found");
        }

        for (String item : hostnames){
            System.out.println(item);
        }

        return hostnames;
    }

    private static Set<String> readInFiles() {
        Set<String> files = new HashSet<>();

        try {
            Scanner reader = new Scanner(new File("config_sharing.txt"));

            while (reader.hasNext()){
                String line = reader.nextLine();
                files.add(line);
            }
        }
        catch (FileNotFoundException fileNotFoundException){
            System.out.println("File Not Found");
        }

        for (String item : files){
            System.out.println(item);
        }

        return files;
    }

}
