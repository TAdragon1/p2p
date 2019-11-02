import java.util.PriorityQueue;

public class AddToPWF implements Runnable {

    private Message m;
    private PriorityQueue<Message> peerWideForwarding;

    public AddToPWF(Message m, PriorityQueue<Message> peerWideForwarding){
        this.m = m;
        this.peerWideForwarding = peerWideForwarding;
    }

    @Override
    public void run() {
        synchronized (peerWideForwarding) {
            peerWideForwarding.add(m);
            peerWideForwarding.notify();
        }
    }
}
