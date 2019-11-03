import java.net.Socket;
import java.util.TimerTask;

public class CloseSocketTask extends TimerTask {

    private Socket connectionSocket;
    private Object heartbeatObject;

    public CloseSocketTask(Socket connectionSocket, Object heartbeatObject){
        this.connectionSocket = connectionSocket;
        this.heartbeatObject = heartbeatObject;
    }

    @Override
    public void run() {
        try {
            if (connectionSocket.isConnected()) {
                Printer.print("Heartbeat timeout: Closing socket");
                connectionSocket.close();

                synchronized (heartbeatObject) {
                    heartbeatObject.notify();
                }
            }
        }
        catch (Exception e){
            System.out.println("Caught exception: " + e);
            e.printStackTrace();
        }
    }
}
