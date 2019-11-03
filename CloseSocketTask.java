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
            if (!connectionSocket.isClosed()) {
                connectionSocket.close();
                Printer.print("Heartbeat timeout: Closing socket");

                synchronized (heartbeatObject) {
                    heartbeatObject.notify();
                }
            }
        }
        catch (Exception e){

        }
    }
}
