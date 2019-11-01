import java.io.DataOutputStream;
import java.net.Socket;
import java.util.HashSet;
import java.util.PriorityQueue;

public class WriterThread implements Runnable {

    private Socket connectionSocket;
    private PriorityQueue<Message> outgoingMessages;
    private HashSet<String> sentLog;

    public WriterThread(Socket connectionSocket, PriorityQueue<Message> outgoingMessages){
        this.connectionSocket = connectionSocket;
        this.outgoingMessages = outgoingMessages;
        this.sentLog = new HashSet<>();
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
                    sentLog.add(messageData);
                }
            }
        }
        catch (Exception e){

        }
    }
}
