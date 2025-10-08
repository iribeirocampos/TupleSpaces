package pt.ulisboa.tecnico.tuplespaces.client;

import pt.ulisboa.tecnico.tuplespaces.client.grpc.ClientService;
import static pt.ulisboa.tecnico.tuplespaces.client.Debug.debug_;

public class ClientMain {

    public static void main(String[] args) {
        System.out.println(ClientMain.class.getSimpleName());

        // check arguments
        if (args.length < 2) {
            System.err.println("Argument(s) missing!");
            System.err.println("Usage: mvn exec:java -Dexec.args=<host:port> <client_id>");
            return;
        } else if (args.length == 3) {
            if (args[2].equals("debug")) {
                new Debug(true);
            }
        }
        // receive and print arguments
        for (int i = 0; i < args.length; i++) {
            debug_(String.format("arg[%d] = %s%n", i, args[i]));
        }

        // get the host and the port of the server or front-end
        final String host_port = args[0];
        final int client_id = Integer.parseInt(args[1]);

        CommandProcessor parser = new CommandProcessor(new ClientService(host_port, client_id));
        parser.parseInput();
        

    }
}
