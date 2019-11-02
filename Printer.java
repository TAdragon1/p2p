public class Printer {

    public synchronized static void print(String toPrint){
        System.out.println(toPrint);
    }

}
