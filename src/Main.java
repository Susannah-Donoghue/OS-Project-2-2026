public class Main {

    public static void main(String[] args) {

        //TASK 3
        if (args.length != 2 || !args[0].equals("-S") || !args[1].equals("3")){
            System.err.println("Invalid arguments. Usage: -S 3");
            return;
        }

        CapabilityLists system = new CapabilityLists();

        system.runSimulation();
    }
}
