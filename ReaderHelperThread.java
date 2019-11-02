import java.util.PriorityQueue;
import java.util.Set;

public class ReaderHelperThread implements Runnable {

    private String message;
    private PriorityQueue<Message> outgoingMessages;
    private Object someObject;
    private Set<String> localFiles;
    private String localIP;
    private String fileTransferIP;
    private String fileTransferPort;
    private PriorityQueue<Message> peerWideForwarding;

    private static String HEART = "Heart";
    private static String BEAT = "beat";
    private static char Q = 'Q';
    private static char R = 'R';

    private static int HIGHEST_PRIORITY = 0;
    private static int DEFAULT_PRIORITY = 1;

    private static int FIRST_INDEX = 0;

    public ReaderHelperThread(String message, PriorityQueue<Message> outgoingMessages, Object someObject,
                              Set<String> localFiles, String localIP, String fileTransferIP, String fileTransferPort,
                              PriorityQueue<Message> peerWideForwarding){
        this.message = message;
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
        if (message.equals(HEART)){
            synchronized (outgoingMessages) {
                outgoingMessages.add(new Message(BEAT, HIGHEST_PRIORITY));
                outgoingMessages.notify();
            }
        }
        else if (message.equals(BEAT)){
            synchronized (someObject) {
                someObject.notify();
            }
        }
        else if (firstCharOf(message) == Q){
            Printer.print("Query received: " + message);

            String queryData = message.split(":")[1];        //  "(query ID);(file name)"
            String queryID = queryData.split(";")[0];
            String filename = queryData.split(";")[1];

            boolean onThisPeer = localFiles.contains(filename);

            if (onThisPeer) {
                Printer.print("This peer has the requested file");

                String ip = fileTransferIP;
                String port = fileTransferPort;

                // Response format: "R:(query id);(peer IP:port);(filename)"
                String response = "R:" + queryID + ";" + ip + ":" + port + ";" + filename;
                synchronized (outgoingMessages) {
                    outgoingMessages.add(new Message(response, DEFAULT_PRIORITY));
                    outgoingMessages.notify();
                }
            }
            else {
                Printer.print("This peer doesn't have the requested file");
                synchronized (peerWideForwarding) {
                    peerWideForwarding.add(new Message(message, DEFAULT_PRIORITY));
                    peerWideForwarding.notify();
                }
            }
        }
        else if (firstCharOf(message) == R){
            String responseData = message.substring(2);             // "(query id);(peer IP:port);(filename)"
            String queryID = responseData.split(";")[0];
            String filename = responseData.split(";")[2];

            String originIP = queryID.split("-")[0];
            boolean atOriginPeer = originIP.equals(localIP);

            if (atOriginPeer) {
                String ipAndPort = responseData.split(";")[1];
                String ip = ipAndPort.split(":")[0];
                String port = ipAndPort.split(":")[1];

                String transfer = "T:" + filename;
                ConnectTransferThread connectTransferThread = new ConnectTransferThread(ip, port, transfer);
                Thread thread = new Thread(connectTransferThread);
                thread.start();
            }
            else {
                synchronized (outgoingMessages) {
                    outgoingMessages.add(new Message(message, DEFAULT_PRIORITY));
                    outgoingMessages.notify();
                }
            }
        }
    }

    private static char firstCharOf(String message){
        return message.charAt(FIRST_INDEX);
    }

}
