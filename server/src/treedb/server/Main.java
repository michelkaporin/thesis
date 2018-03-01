package treedb.server;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import treedb.server.utils.Utility;

public class Main {
    public static void main(String[] args) throws SecurityException, IOException {
        logStartup();

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

    private static void logStartup() throws SecurityException, IOException {
        String startup = Utility.logString(new UUID(0L, 0L), "n/a", 0, 0);
        Logger l = Logger.getLogger("TreeDB Performance");
        l.setLevel(Level.INFO);
        l.setUseParentHandlers(false);
        Handler handler = new FileHandler("performance.log");
        handler.setFormatter(new LogFormatter());
        l.addHandler(handler);
        l.info("Time\tCurrent Memory Consumption\tStream ID\tRequest Type\tOperation Elapsed Time\tChunk Count");
        l.info(startup);
	}

	private static class LogFormatter extends Formatter {
        @Override
        public String format(LogRecord record) {
            StringBuffer sb = new StringBuffer();
            sb.append(record.getMessage() + "\n");
            return sb.toString();
        }
    }
}