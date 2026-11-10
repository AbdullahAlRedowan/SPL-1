import java.io.File;
import java.util.ArrayList;

public class FileCollector {

    public static void collectTxtFiles(File folder, ArrayList<File> result) {
        File[] items = folder.listFiles();

        if (items == null) {
            return;
        }

        for (File item : items) {
            if (item.isDirectory()) {
                collectTxtFiles(item, result);
            } else if (item.getName().endsWith(".txt")) {
                result.add(item);
            }
        }
    }
}