public class Message {

    private String message;
    private int priority;

    public Message(String message, int priority){
        this.message = message;
        this.priority = priority;
    }

    public String getMessage(){
        return message;
    }

    public int getPriority(){
        return priority;
    }

}
