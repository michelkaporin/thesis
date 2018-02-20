package treedb.server.storage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileSystem implements Storage {

    public String indexPath;

    public FileSystem() {
        indexPath = System.getProperty("user.dir")
            .concat(File.separator)
            .concat("storage")
            .concat(File.separator)
            .concat("tree_index");
    }

    public boolean store(String streamID, String key, byte[] bytes) {
        String dir = indexPath.concat(File.separator)
            .concat(streamID)
            .concat(File.separator);
        createDirsIfNeeded(dir);
        
        try {
			Files.write(Paths.get(dir.concat(key)), bytes);
		} catch (IOException e) {
			e.printStackTrace(); // TODO: move to more serious logging solution (e.g. log4j)
			return false;
        }
        System.out.println("Created " + dir.concat(key));
        return true;
    }

    public byte[] get(String streamID, String key) {
        String dir = indexPath.concat(File.separator)
            .concat(streamID)
            .concat(File.separator);

        byte[] data = null;
        try {
			data = Files.readAllBytes(Paths.get(dir.concat(key)));
		} catch (IOException e) {
			e.printStackTrace();
        }
        
        return data;
    }

    private void createDirsIfNeeded(String pathStr) {
        Path path = Paths.get(pathStr);
        if (!Files.exists(path)) {
            try {
				Files.createDirectories(path);
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
    }
}