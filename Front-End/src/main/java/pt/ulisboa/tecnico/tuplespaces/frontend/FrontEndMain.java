package pt.ulisboa.tecnico.tuplespaces.frontend;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static pt.ulisboa.tecnico.tuplespaces.frontend.Debug.debug_;

public class FrontEndMain {

  public static void main(String[] args) throws IOException, InterruptedException {
    String[] servers;
    System.out.println(FrontEndMain.class.getSimpleName());

    // check arguments
    if (args.length < 2) {
      System.err.println("Argument(s) missing!");
      System.err.println("Usage: mvn exec:java -Dexec.args=<host:port> <server:port>");
      return;
    } else if (args.length >= 5) {
      if (args[args.length - 1].equals("debug")) {
        new Debug(true);
      }
    }
    servers = new String[args.length - 2];
    debug_("FrontEnd Initialized\n");
    debug_(String.format("FrontEndPort: %s%n", args[0]));
    for (int i = 1; i < args.length - 1; i++) {
      servers[i - 1] = args[i];
      debug_(String.format("Added ServerPort: %s%n", args[i]));
    }

    final int frontEndPort = Integer.parseInt(args[0]);
    // final int serverPort = Integer.parseInt(args[1]);
    // final String serverPort = args[1];

    final BindableService impl = new FrontEndImpl(servers, 1);

    // Create a new server to listen on port
    Server server = ServerBuilder.forPort(frontEndPort)
        .addService(ServerInterceptors.intercept(impl, new HeaderServerInterceptor())).build();

    // Server server = ServerBuilder.forPort(frontEndPort).addService(impl).build();

    // Start the server
    server.start();

    // Server threads are running in the background.
    debug_("FrontEnd Server started\n");

    // Do not exit the main thread. Wait until server is terminated.
    server.awaitTermination();

  }
}
