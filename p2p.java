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

    private static String peerHostname;
    private static String peerIP;
    private static AtomicInteger queryNum = new AtomicInteger(0);

    private static Comparator<Message> messageComparator = new Comparator<Message>() {
        @Override
        public int compare(Message m1, Message m2) {
            return m1.getPriority() - m2.getPriority();
        }
    };

    private static int INITIAL_CAPACITY = 10;
    private static int DEFAULT_PRIORITY = 1;
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
            peerIP = InetAddress.getByName(peerHostname).getHostAddress();
            List<PriorityQueue<Message>> neighborOutgoingQueues = new LinkedList<>();

            ServerSocket neighborWelcomeSocket = new ServerSocket(nextPortNumber());
            NeighborServer neighborServer =
                    new NeighborServer(neighborWelcomeSocket, localFiles, peerIP, peerFileTransferIP,
                            peerFileTransferPort, peerWideForwarding, neighborOutgoingQueues);
            Thread neighborTCPServerThread = new Thread(neighborServer);
            System.out.println("Peer started, peer ip = " + peerIP);

            int serverPortNum = neighborWelcomeSocket.getLocalPort();

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
                                Socket connectionSocket = new Socket(hostname, serverPortNum);
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
                                            peerIP, peerFileTransferIP, peerFileTransferPort, peerWideForwarding);
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

                        String queryID = peerIP + "-" + String.valueOf(queryNum);
                        queryNum.incrementAndGet();

                        // query format: "Q:(query ID);(file name)"
                        String query = "Q:" + queryID + ";" + fileName;
                        Message messageQ = new Message(query, DEFAULT_PRIORITY);
                        peerWideForwarding.add(messageQ);
                        peerWideForwarding.notify();

                        break;
                    case "leave":
                        // Close all connections with neighbors
                        System.out.println("DEBUG: Leave case");

                        for (Socket socket : neighbors) {
                            try {
                                if (!socket.isClosed()) {
                                    socket.close();
                                }
                            }
                            catch (Exception e){

                            }
                        }

                        break;
                    case "exit":
                        // Close all open connections and terminate
                        System.out.println("DEBUG: Exit case");

                        for (Socket socket : neighbors) {
                            try {
                                if (!socket.isClosed()) {
                                    socket.close();
                                }
                            }
                            catch (Exception e){

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

            peerHostname = reader.nextLine();

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
