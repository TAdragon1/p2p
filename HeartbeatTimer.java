import java.net.Socket;
import java.util.PriorityQueue;
import java.util.Timer;

public class HeartbeatTimer implements Runnable {

    private Socket connectionSocket;
    private PriorityQueue<Message> outgoingMessages;
    private Object someObject;

    private static long NO_DELAY = 0;
    private static long FIVE_SECONDS = 5000;
    private static long SIXTY_SECONDS = 60000;

    public HeartbeatTimer(Socket connectionSocket, PriorityQueue<Message> outgoingMessages, Object someObject){
        this.connectionSocket = connectionSocket;
        this.outgoingMessages = outgoingMessages;
        this.someObject = someObject;
    }

    @Override
    public void run() {
        try{
            Timer timer = new Timer();
            timer.schedule(new HeartbeatTask(outgoingMessages), NO_DELAY);
            long start = System.currentTimeMillis();
            CloseSocketTask closeSocketTask = new CloseSocketTask(connectionSocket, someObject);
            timer.schedule(closeSocketTask, SIXTY_SECONDS);

            while (true) {
                someObject.wait();
                if (!connectionSocket.isClosed()){
                    closeSocketTask.cancel();

                    long end = System.currentTimeMillis();
                    long timePassed = end - start;
                    long delay;

                    if (timePassed < FIVE_SECONDS){
                        delay = FIVE_SECONDS - timePassed;
                    }
                    else{
                        delay = NO_DELAY;
                    }

                    timer.schedule(new HeartbeatTask(outgoingMessages), delay);
                    start = System.currentTimeMillis();
                    closeSocketTask = new CloseSocketTask(connectionSocket, someObject);
                    timer.schedule(closeSocketTask, SIXTY_SECONDS);
                }
                else{
                    // TODO Socket is closed. Cleanup?
                }
            }
        }
        catch (Exception e){

        }
    }
}
