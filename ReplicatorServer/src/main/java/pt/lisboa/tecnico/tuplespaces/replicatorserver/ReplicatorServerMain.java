package pt.ulisboa.tecnico.tuplespaces.replicatorserver;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;
import static pt.ulisboa.tecnico.tuplespaces.replicatorserver.Debug.debug_;
import io.grpc.ServerInterceptors;

public class ReplicatorServerMain {
  public static void main(String[] args) throws IOException, InterruptedException {

    System.out.println(ReplicatorServerMain.class.getSimpleName());

    // check arguments
    if (args.length < 1) {
      System.err.println("Argument(s) missing!");
      System.err.printf("Usage: java %s port%n", ReplicatorServerMain.class.getName());
      return;
    } else if (args.length == 2) {
      if (args[1].equals("debug")) {
        new Debug(true);
      }
    }
    // receive and print arguments
    debug_(String.format("Received %d arguments%n", args.length));
    for (int i = 0; i < args.length; i++) {
      debug_(String.format("arg[%d] = %s%n", i, args[i]));
    }

    final int port = Integer.parseInt(args[0]);
    final BindableService impl = new ServiceImpl();

    // Create a new server to listen on port
    Server server = ServerBuilder.forPort(port).addService(ServerInterceptors.intercept(impl, new HeaderServerInterceptor())).build();

    // Start the server
    server.start();

    // Server threads are running in the background.
    debug_("Server started\n");

    // Do not exit the main thread. Wait until server is terminated.
    server.awaitTermination();

  }
}
