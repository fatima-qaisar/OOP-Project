package utils;

import java.io.File;

public class FileUtils {

    public static boolean isSupported(String filePath) {
        String ext = getFileExtension(filePath);
        return ext.equalsIgnoreCase("txt") ||
               ext.equalsIgnoreCase("pdf") ||
               ext.equalsIgnoreCase("docx") ||
               ext.equalsIgnoreCase("ppt");
    }

    public static String getFileExtension(String filePath) {
        int idx = filePath.lastIndexOf('.');
        return idx > 0 ? filePath.substring(idx + 1) : "";
    }

    public static boolean fileExists(String filePath) {
        File f = new File(filePath);
        return f.exists() && f.isFile();
    }
}
