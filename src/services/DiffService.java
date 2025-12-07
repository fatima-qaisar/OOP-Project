package services;

import models.Document;
import models.Version;
import utils.DiffUtility;

public class DiffService {

    public void showDiff(Document doc, int v1, int v2) {
        Version version1 = doc.getVersion(v1);
        Version version2 = doc.getVersion(v2);

        if (version1 == null || version2 == null) {
            System.out.println("One or both versions do not exist!");
            return;
        }

        System.out.println("Showing diff between version " + v1 + " and version " + v2);
        DiffUtility.showDiff(version1.getContent(), version2.getContent());
    }

    // Optional: overload to compare two strings directly
    public void showDiff(String oldContent, String newContent) {
        DiffUtility.showDiff(oldContent, newContent);
    }
}
