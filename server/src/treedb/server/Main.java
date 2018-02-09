package treedb.server;

import java.io.IOException;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        String ip = args[0];
        int port = Integer.valueOf(args[1]);
        String[] additionalArgs = Arrays.copyOfRange(args, 2, args.length);

        try {
			Server server = new Server(ip, port, additionalArgs);
			new Thread(server).start();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}