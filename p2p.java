import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class p2p {

    /* TODO's
        Add all print statements
        Add heartbeat threads (client/server)
        Handle duplicate responses for intermediate peers
    */

    // Ports
    private static LinkedList<Integer> availablePortNums = readInPeer();

    // Client Sockets
    private static LinkedList<Socket> neighbors = new LinkedList<>();

    // Neighbor IPs
    private static List<String> neighborHostnames = readInNeighbors();

    // Files
    private static Set<String> localFiles = readInFiles();

    private static LinkedList<String> outgoingQueries = new LinkedList<>();
    private static LinkedList<String> incomingResponses = new LinkedList<>();

    private static final String hostID = grabHostID();
    private static AtomicInteger queryNum = new AtomicInteger(0);

    private static List<String> sentQueries = new LinkedList<>();

    private static String grabHostID(){
        try{
            String hostname = InetAddress.getByName("eecslab-10.case.edu").getHostName();
            return hostname.substring(8, 10);
        }
        catch (Exception e){
            return "10";
        }
    }

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


            /* Neighbor Welcome Socket */
            ServerSocket neighborWelcomeSocket = new ServerSocket(nextPortNumber());
            NeighborServer neighborServer =
                    new NeighborServer(neighborWelcomeSocket, outgoingQueries, incomingResponses, localFiles, fileXferAddress, fileXferPort);
            Thread neighborTCPServerThread = new Thread(neighborServer);


            scan = new Scanner(System.in);
            while(true){
                String cmd = scan.nextLine().toLowerCase();
                String[] strings = cmd.split(" ");
                switch(strings[0]){
                    case "connect":
                        // Connect to each neighbor
                        System.out.println("DEBUG: Connect case");

                        for(String hostname : neighborHostnames){
                            Socket connectionSocket = new Socket(hostname, nextPortNumber());
                            neighbors.add(connectionSocket);
                        }

                        List<Thread> threads = new LinkedList<>();
                        for(Socket neighbor : neighbors){
                            ConnectNeighborThread connectNeighborThread = new ConnectNeighborThread(neighbor, outgoingQueries, incomingResponses, sentQueries);
                            Thread thread = new Thread(connectNeighborThread);
                            threads.add(thread);
                        }

                        break;
                    case "get":
                        // Send query
                        String fileName = strings[1];
                        System.out.println("DEBUG: Get case " + fileName);

                        String queryID = String.valueOf(hostID) + "-" + String.valueOf(queryNum);
                        sentQueries.add(queryID);
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
