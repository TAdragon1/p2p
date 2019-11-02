import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
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
    private static int THIRD_INDEX = 2;

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
                Message message;
                synchronized (outgoingMessages) {
                    if (outgoingMessages.isEmpty()) {
                        outgoingMessages.wait();
                    }


                    message = outgoingMessages.remove();
                }
                String messageData = message.getMessage();

                if (!sentLog.contains(messageData)) {
                    if (message.getMessage().charAt(FIRST_INDEX) == Q) {
                        InetAddress remoteAddress = connectionSocket.getInetAddress();
                        String remoteIP = remoteAddress.getHostAddress();
                        String messageContent = messageData.substring(THIRD_INDEX);
                        String messageIP = messageContent.split("-")[0];
                        if (remoteIP.equals(messageIP)){
                            // Then receiver of this query is query originator, don't send
                        }
                        else{
                            outToClient.writeBytes(message.getMessage() + "\n");
                            Printer.print("Query sent");
                        }

                    }
                    else if (message.getMessage().equals(HEART)){
                        Printer.print("Sending Heart");
                        outToClient.writeBytes(message.getMessage() + "\n");
                    }
                    else if (message.getMessage().equals(BEAT)){
                        Printer.print("Sending beat");
                        outToClient.writeBytes(message.getMessage() + "\n");
                    }

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
