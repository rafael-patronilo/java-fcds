import java.util.Random;
public class Main{

    static int data = 0;
    static boolean producerFlag = false;
    static boolean consumerFlag = false;

    static int rounds = 0;
    static int produced = 0;
    static int consumed = 0;
    static int producerWaits = 0;
    static int consumerWaits = 0;


    public static boolean dataAvailable(){
        return producerFlag ^ consumerFlag;
    }

    public static void producer(){
        if(!dataAvailable()){
            System.out.println("Producer: Producing");
            data++;
            producerFlag = !producerFlag;
            produced++;
        }
        else {
            System.out.println("Producer: Nothing to produce, waiting...");
            producerWaits++;
        }
    }
    

    public static void consumer(){
        if(dataAvailable()){
            System.out.println("Consumer: Consuming");
            data--;
            consumerFlag = !consumerFlag;
            consumed++;
        }
        else {
            System.out.println("Consumer: Nothing to consume, waiting...");
            consumerWaits++;
        }
    }

    public static String boolToStr(boolean value) {return value ? "true" : "false";}

    public static void main(String[] args) throws Exception{
        Random random = new Random();
        
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                    System.out.println("\n\n");
                    System.out.println(
                        "Results:\n"+
                        "  rounds        = " + rounds + "\n" +
                        "  consumed      = " + consumed + "\n" +
                        "  produced      = " + produced + "\n" +
                        "  consumerWaits = " + consumerWaits + "\n" +
                        "  producerWaits = " + producerWaits
                    );
                }
            }
        );
        while(true){
            System.out.println(rounds++);
            if(random.nextBoolean()){
                System.out.println("Simulator: invoking producer");
                producer();
            } else{
                System.out.println("Simulator: invoking consumer");
                consumer();
            }
            System.out.printf("Simulator: \tdata = %d,\n" + 
            "\tavailable = %s,\n \tflags (cons/prod) = (%s/%s)\n", 
                data, boolToStr(dataAvailable()), boolToStr(consumerFlag), boolToStr(producerFlag));
            if(data != 0 && data != 1){
                System.err.println("Simulator: Invalid state detected!");
                break;
            }
            System.out.println();
            if(args.length > 0){
                Thread.sleep(Integer.parseInt(args[0])*1000);
            }
            if(args.length > 1 && rounds >= Integer.parseInt(args[1])){
                break;
            }
        }
    }
}