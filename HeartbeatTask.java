import java.util.PriorityQueue;
import java.util.TimerTask;

public class HeartbeatTask extends TimerTask {

    private PriorityQueue<Message> outgoingMessages;
    private static int HIGHEST_PRIORITY = 0;
    private static String HEART = "Heart";

    public HeartbeatTask(PriorityQueue<Message> outgoingMessages){
        this.outgoingMessages = outgoingMessages;
    }

    @Override
    public void run() {
        try {
            Message message = new Message(HEART, HIGHEST_PRIORITY);
            outgoingMessages.add(message);
            outgoingMessages.notify();
        }
        catch (Exception e){

        }
    }
}