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
                Message messageToBeForwarded;
                synchronized (peerWideForwarding) {
                    if (peerWideForwarding.isEmpty()) {
                        peerWideForwarding.wait();
                    }

                    messageToBeForwarded = peerWideForwarding.remove();
                }
                for (PriorityQueue<Message> priorityQueue : neighborOutgoingQueues){
                    synchronized (priorityQueue) {
                        priorityQueue.add(messageToBeForwarded);
                        priorityQueue.notify();
                    }
                }
            }
        }
        catch (Exception e){

        }
    }
}
