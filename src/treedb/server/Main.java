package treedb.server;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        String ip = args[0];
        int port = Integer.valueOf(args[1]);

        try {
			Server server = new Server(ip, port);
			new Thread(server).start();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}