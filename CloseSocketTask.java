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
            connectionSocket.close();
            someObject.notify();
        }
        catch (Exception e){

        }
    }
}
