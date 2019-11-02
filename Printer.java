public class Printer {

    public static void print(String toPrint){
        synchronized (System.out) {
            System.out.println(toPrint);
        }
    }

}
