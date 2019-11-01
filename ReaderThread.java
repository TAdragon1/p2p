import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

public class ReaderThread implements Runnable {

    private Socket connectionSocket;

    public ReaderThread(){

    }

    @Override
    public void run() {
        try{
            while (true)
            {
                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

                String message = inFromClient.readLine();

                // TODO create reader helper thread that will handle the message
            }
        }
        catch (Exception e) {

        }
    }
}
