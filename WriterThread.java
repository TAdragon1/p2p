import java.io.DataOutputStream;
import java.net.Socket;
import java.util.PriorityQueue;

public class WriterThread implements Runnable {

    private Socket connectionSocket;
    private PriorityQueue<String> outgoingMessages;


    public WriterThread(Socket connectionSocket, PriorityQueue<String> outgoingMessages){
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

                String message = outgoingMessages.remove();

                outToClient.writeBytes(message + "\n");
            }
        }
        catch (Exception e){

        }
    }
}
