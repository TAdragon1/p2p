import java.net.*;

public class FileTransferServer implements Runnable{

    private ServerSocket welcomeSocket;

    public FileTransferServer(ServerSocket welcomeSocket){
        this.welcomeSocket = welcomeSocket;

    }

    @Override
    public void run() {
        try{
            while(true) {
                Socket connectionSocket = welcomeSocket.accept();

                AcceptTransferThread st =
                        new AcceptTransferThread(connectionSocket);
                Thread thread = new Thread(st);
            }
        }
        catch (Exception e){

        }
    }

}     