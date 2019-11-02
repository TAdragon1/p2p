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
                    if (messageData.charAt(FIRST_INDEX) == Q) {
                        InetAddress remoteAddress = connectionSocket.getInetAddress();
                        String remoteIP = remoteAddress.getHostAddress();
                        String messageContent = messageData.substring(THIRD_INDEX);
                        String messageIP = messageContent.split("-")[0];
                        if (remoteIP.equals(messageIP)){
                            // Then receiver of this query is query originator, don't send
                        }
                        else{
                            outToClient.writeBytes(messageData + "\n");
                            Printer.print("Query sent");
                        }
                    }
                    else if (messageData.equals(HEART)){
                        Printer.print("Sending Heart");
                        outToClient.writeBytes(messageData + "\n");
                    }
                    else if (messageData.equals(BEAT)){
                        Printer.print("Sending beat");
                        outToClient.writeBytes(messageData + "\n");
                    }
                    else{
                        Printer.print("Sending " + messageData);
                        outToClient.writeBytes(messageData + "\n");
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
