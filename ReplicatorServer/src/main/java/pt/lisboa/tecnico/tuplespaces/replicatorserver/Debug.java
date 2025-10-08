package pt.ulisboa.tecnico.tuplespaces.replicatorserver;

public class Debug {
    private static boolean DEBUG_FLAG = false;

    public Debug(boolean flag) {
        if (flag) {
            System.out.println("[WARNING] Debug mode enabled");
        }
        DEBUG_FLAG = flag;
    }

    public static void debug_(String debugMessage) {
        if (DEBUG_FLAG)
            System.err.print("[DEBUG]" + debugMessage);
    }
}
