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
    private static int FIRST_INDEX = 0;
    private static int SECOND_INDEX = 1;

    private static PriorityQueue<Message> peerWideForwarding;

    private static List<Thread> allThreads = new LinkedList<>();

    public static void main(String[] args){
        Scanner scan;
        peerWideForwarding = new PriorityQueue<>(INITIAL_CAPACITY, messageComparator);

        try {
            /* File Transfer Welcome Socket */
            ServerSocket fileTransferWelcomeSocket = new ServerSocket(nextPortNumber());
            FileTransferServer fileTransferServer = new FileTransferServer(fileTransferWelcomeSocket);
            Thread fileTransferServerThread = new Thread(fileTransferServer);
            fileTransferServerThread.start();

            int fileXferPort = fileTransferWelcomeSocket.getLocalPort();

            String peerFileTransferPort = String.valueOf(fileXferPort);

            /* Neighbor Welcome Socket */
            peerIP = InetAddress.getByName(peerHostname).getHostAddress();
            List<PriorityQueue<Message>> neighborOutgoingQueues = new LinkedList<>();

            ServerSocket neighborWelcomeSocket = new ServerSocket(nextPortNumber());
            NeighborServer neighborServer =
                    new NeighborServer(neighborWelcomeSocket, localFiles, peerIP,
                            peerFileTransferPort, peerWideForwarding, neighborOutgoingQueues);
            Thread neighborTCPServerThread = new Thread(neighborServer);
            neighborTCPServerThread.start();
            Printer.print("Peer started, peer ip = " + peerIP);

            int serverPortNum = neighborWelcomeSocket.getLocalPort();

            scan = new Scanner(System.in);
            while(true){
                String cmd = scan.nextLine();
                String[] strings = cmd.split(" ");
                String checker = strings[FIRST_INDEX].toLowerCase();

                switch(checker){
                    case "connect":
                        // Connect to each neighbor
                        for(String hostname : neighborHostnames){
                            Printer.print("Attempt to connect to neighbor: " + hostname);
                            try {
                                Socket connectionSocket = new Socket(hostname, serverPortNum);
                                neighbors.add(connectionSocket);
                                Printer.print("Connection succeeded");
                            }
                            catch (Exception e) {
                                Printer.print("Connection failed");
                            }
                        }

                        for(Socket neighbor : neighbors){
                            PriorityQueue<Message> neighborOutgoingMessages =
                                    new PriorityQueue<>(INITIAL_CAPACITY, messageComparator);
                            HashSet<String> neighborSentLog = new HashSet<>();
                            WriterThread neighborWriter =
                                    new WriterThread(neighbor, neighborOutgoingMessages, neighborSentLog);
                            Thread neighborWriterThread = new Thread(neighborWriter);
                            neighborWriterThread.start();

                            Object heartbeatObject = new Object();
                            HashSet<String> neighborReceivedLog = new HashSet<>();
                            ReaderThread neighborReader =
                                    new ReaderThread(neighbor, neighborReceivedLog, neighborOutgoingMessages,
                                            heartbeatObject, localFiles, peerIP,
                                            peerFileTransferPort, peerWideForwarding);
                            Thread neighborReaderThread = new Thread(neighborReader);
                            neighborReaderThread.start();

                            HeartbeatTimer neighborHeartbeatTimer =
                                    new HeartbeatTimer(neighbor, neighborOutgoingMessages, heartbeatObject);
                            Thread neighborHeartbeatTimerThread = new Thread(neighborHeartbeatTimer);
                            neighborHeartbeatTimerThread.start();

                            neighborOutgoingQueues.add(neighborOutgoingMessages);
                        }

                        PeerWideForwardingThread peerWideForwardingThread =
                                new PeerWideForwardingThread(peerWideForwarding, neighborOutgoingQueues);
                        Thread pwfThread = new Thread(peerWideForwardingThread);
                        pwfThread.start();
                        break;
                    case "get":
                        // Send query
                        String fileName = strings[SECOND_INDEX];
                        Printer.print("Getting " + fileName);

                        String queryID = peerIP + "-" + String.valueOf(queryNum);
                        queryNum.incrementAndGet();

                        // query format: "Q:(query ID);(file name)"
                        String query = makeQuery(queryID, fileName);
                        Message messageQ = new Message(query, DEFAULT_PRIORITY);

                        AddToPWF addToPWF = new AddToPWF(messageQ, peerWideForwarding);
                        Thread addToPWFThread = new Thread(addToPWF);
                        addToPWFThread.start();
                        break;
                    case "leave":
                        // Close all connections with neighbors
                        Printer.print("Leaving");

                        for (Socket socket : neighbors) {
                            try {
                                if (!socket.isClosed()) {
                                    socket.close();
                                }
                            }
                            catch (Exception e){
//                                Printer.print("Caught exception: " + e);
//                                e.printStackTrace();
                            }
                        }

                        break;
                    case "exit":
                        // Close all open connections and terminate
                        Printer.print("Exiting");

                        for (Socket socket : neighbors) {
                            try {
                                if (!socket.isClosed()) {
                                    socket.close();
                                }
                            }
                            catch (Exception e){
//                                Printer.print("Caught exception: " + e);
//                                e.printStackTrace();
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
                        Printer.print("The four command options are: Connect, Get FilenameWithExtension, Leave, or Exit");
                }
            }

        }
        catch (Exception e){
//            Printer.print("Caught exception: " + e);
//            e.printStackTrace();
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
            Printer.print("File Not Found");
        }

//        for (Integer item : portnums){
//            Printer.print(String.valueOf(item));
//        }

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
            Printer.print("File Not Found");
        }

//        for (String item : hostnames){
//            Printer.print(item);
//        }

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
            Printer.print("File Not Found");
        }

//        for (String item : files){
//            Printer.print(item);
//        }

        return files;
    }

    private static String makeQuery(String queryID, String fileName){
        return "Q:" + queryID + ";" + fileName;
    }

}
