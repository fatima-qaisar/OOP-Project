package services;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileStorageService {

    public static String readFile(String filePath) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line).append("\n");
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
            sb.append("Error reading file.");
        }
        return sb.toString().trim();
    }

    public static void writeFile(String filePath, String content) {
        try {
            Files.write(Paths.get(filePath), content.getBytes());
        } catch (IOException e) {
            System.out.println("Error writing file: " + e.getMessage());
        }
    }
}
