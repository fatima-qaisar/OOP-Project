package utils;

import java.util.Arrays;
import java.util.List;

public class DiffUtility {

    public static void showDiff(String oldContent, String newContent) {
        List<String> oldLines = Arrays.asList(oldContent.split("\n"));
        List<String> newLines = Arrays.asList(newContent.split("\n"));

        int maxLines = Math.max(oldLines.size(), newLines.size());

        System.out.println("\n--- Diff ---");
        for (int i = 0; i < maxLines; i++) {
            String oldLine = i < oldLines.size() ? oldLines.get(i) : "";
            String newLine = i < newLines.size() ? newLines.get(i) : "";

            if (!oldLine.equals(newLine)) {
                if (!oldLine.isEmpty()) System.out.println("- " + oldLine);
                if (!newLine.isEmpty()) System.out.println("+ " + newLine);
            } else {
                System.out.println("  " + oldLine);
            }
        }
        System.out.println("--- End Diff ---\n");
    }
}
