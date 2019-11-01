import java.io.DataOutputStream;
import java.net.Socket;
import java.util.HashSet;
import java.util.PriorityQueue;

public class WriterThread implements Runnable {

    private Socket connectionSocket;
    private PriorityQueue<Message> outgoingMessages;
    private HashSet<String> sentLog;

    private static String HEART = "Heart";
    private static String BEAT = "beat";

    public WriterThread(Socket connectionSocket, PriorityQueue<Message> outgoingMessages, HashSet<String> sentLog){
        this.connectionSocket = connectionSocket;
        this.outgoingMessages = outgoingMessages;
        this.sentLog = sentLog;
    }

    @Override
    public void run() {
        try {
            while (true) {
                DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

                if (outgoingMessages.isEmpty()){
                    outgoingMessages.wait();
                }

                Message message = outgoingMessages.remove();
                String messageData = message.getMessage();

                if (!sentLog.contains(messageData)) {
                    outToClient.writeBytes(message.getMessage() + "\n");

                    if (!messageData.equals(HEART) && !messageData.equals(BEAT)) {
                        sentLog.add(messageData);
                    }
                }
            }
        }
        catch (Exception e){

        }
    }
}
