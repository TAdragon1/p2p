import java.net.Socket;
import java.util.TimerTask;

public class CloseSocketTask extends TimerTask {

    private Socket connectionSocket;
    private Object someObject;

    public CloseSocketTask(Socket connectionSocket, Object someObject){
        this.connectionSocket = connectionSocket;
        this.someObject = someObject;
    }

    @Override
    public void run() {
        try {
            Printer.print("Heartbeat timeout: Closing socket");
            connectionSocket.close();
            synchronized (someObject) {
                someObject.notify();
            }
        }
        catch (Exception e){
            System.out.println("Caught exception: " + e);
            e.printStackTrace();
        }
    }
}
