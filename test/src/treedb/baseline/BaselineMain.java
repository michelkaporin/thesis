package treedb.baseline;

import java.io.IOException;

public class BaselineMain {
    
    public static void main(String[] args) {
        String ip = args[0];
        int port = Integer.valueOf(args[1]);

        try {
            BaselineServer server = new BaselineServer(ip, port);
            new Thread(server).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}