import java.io.DataOutputStream;
import java.net.Socket;
import java.util.PriorityQueue;

public class WriterThread implements Runnable {

    private Socket connectionSocket;
    private PriorityQueue<Message> outgoingMessages;

    public WriterThread(Socket connectionSocket, PriorityQueue<Message> outgoingMessages){
        this.connectionSocket = connectionSocket;
        this.outgoingMessages = outgoingMessages;

    }

    @Override
    public void run() {
        try {
            while (true) {
                DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

                if (outgoingMessages.size() == 0){
                    outgoingMessages.wait();
                }

                Message message = outgoingMessages.remove();

                outToClient.writeBytes(message.getMessage() + "\n");
            }
        }
        catch (Exception e){

        }
    }
}
