import java.util.Comparator;
import java.util.PriorityQueue;

public class ReaderHelperThread implements Runnable {

    private String message;
    private PriorityQueue<Message> outgoingMessages;

    public ReaderHelperThread(String message, PriorityQueue<String> outgoingMessages){

    }

    @Override
    public void run() {
        if (message.equals("heart")){
            outgoingMessages.add(new Message("beat", 0));
        }
        else if (message.equals("beat")){
            // TODO something
        }
        else if (message.charAt(0) == 'Q'){
            String queryData = message.split(":")[1];        //  "(query ID);(file name)"
            String queryID = queryData.split(";")[0];
            String filename = queryData.split(";")[1];

            boolean onThisPeer = localFiles.contains(filename);

            if (onThisPeer) {
                String ip = fileTransferInetAddress.getHostAddress();
                String port = String.valueOf(fileTransferPort);
                String response = "R:" + queryID + ";" + ip + ":" + port + ";" + filename;

                // Response format: "R:(query id);(peer IP:port);(filename)"
                outgoingMessages.add(new Message(response, 1));
            }
            else {
                // TODO add to outgoingMessages for other neighbor

            }
        }
        else if (message.charAt(0) == 'R'){
            String responseData = message.substring(2);
            String queryID = responseData.split(";")[0];

            boolean atOriginPeer = sentQueries.contains(queryID);

            if (atOriginPeer) {
                String ipAndPort = responseData.split(";")[1];
                String ip = ipAndPort.split(":")[0];
                String port = ipAndPort.split(":")[1];
                String filename = responseData.split(";")[2];

                String transfer = "T:" + filename;
                ConnectTransferThread connectTransferThread = new ConnectTransferThread(ip, port, transfer);
                Thread thread = new Thread(connectTransferThread);
            }
            else {
                // TODO add to outgoingMessages for other neighbor

            }
        }
    }

}
