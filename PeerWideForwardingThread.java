import java.util.List;
import java.util.PriorityQueue;

public class PeerWideForwardingThread implements Runnable {

    private PriorityQueue<Message> peerWideForwarding;
    private List<PriorityQueue<Message>> neighborOutgoingQueues;

    public PeerWideForwardingThread(PriorityQueue<Message> peerWideForwarding, List<PriorityQueue<Message>> neighborOutgoingQueues){
        this.peerWideForwarding = peerWideForwarding;
        this.neighborOutgoingQueues = neighborOutgoingQueues;
    }

    @Override
    public void run() {
        try{
            while (true){
                if (peerWideForwarding.isEmpty()){
                    Printer.print("Waiting");
                    peerWideForwarding.wait();
                }

                Message messageToBeForwarded = peerWideForwarding.remove();
                for (PriorityQueue<Message> priorityQueue : neighborOutgoingQueues){
                    priorityQueue.add(messageToBeForwarded);
                    priorityQueue.notify();
                    Printer.print("Message added and queue notified");
                }
            }
        }
        catch (Exception e){

        }
    }
}
