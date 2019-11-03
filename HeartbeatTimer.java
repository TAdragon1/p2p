import java.net.Socket;
import java.util.PriorityQueue;
import java.util.Timer;

public class HeartbeatTimer implements Runnable {

    private Socket connectionSocket;
    private PriorityQueue<Message> outgoingMessages;
    private Object heartbeatObject;

    private static long NO_DELAY = 0;
    private static long DELAY = 10000;          // 10 seconds
    private static long TIMEOUT = 60000;        // 1 min

    public HeartbeatTimer(Socket connectionSocket, PriorityQueue<Message> outgoingMessages, Object heartbeatObject){
        this.connectionSocket = connectionSocket;
        this.outgoingMessages = outgoingMessages;
        this.heartbeatObject = heartbeatObject;
    }

    @Override
    public void run() {
        try{
            Timer timer = new Timer();
            timer.schedule(new HeartbeatTask(outgoingMessages), NO_DELAY);
            long start = System.currentTimeMillis();
            CloseSocketTask closeSocketTask = new CloseSocketTask(connectionSocket, heartbeatObject);
            timer.schedule(closeSocketTask, TIMEOUT);

            while (true) {
                synchronized (heartbeatObject) {
                    heartbeatObject.wait();
                }
                if (!connectionSocket.isClosed()){
                    closeSocketTask.cancel();

                    long end = System.currentTimeMillis();
                    long timePassed = end - start;
                    long delay;

                    if (timePassed < DELAY){
                        delay = DELAY - timePassed;
                    }
                    else{
                        delay = NO_DELAY;
                    }

                    timer.schedule(new HeartbeatTask(outgoingMessages), delay);
                    start = System.currentTimeMillis();
                    closeSocketTask = new CloseSocketTask(connectionSocket, heartbeatObject);
                    timer.schedule(closeSocketTask, TIMEOUT);
                }
                else{
                    break;
                }
            }
        }
        catch (Exception e){

        }
    }
}
