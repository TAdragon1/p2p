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

    private static char Q = 'Q';

    private static int FIRST_INDEX = 0;

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
                    if (message.getMessage().charAt(FIRST_INDEX) == Q) {
                        System.out.println("Query sent");
                    }
                    else if (message.getMessage().equals(HEART)){
                        System.out.println("Sending heartbeat");
                    }
                    else if (message.getMessage().equals(BEAT)){
                        System.out.println("Receiving heartbeat");
                    }

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
