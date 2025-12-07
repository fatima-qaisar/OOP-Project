package services;

import models.Document;
import models.Version;

public class VersionControlService {

    public void restoreVersion(Document doc, int versionNumber) {
        Version v = doc.getVersion(versionNumber);
        if (v == null) {
            System.out.println("Version " + versionNumber + " does not exist!");
            return;
        }
        doc.restoreVersion(versionNumber);
        System.out.println("Version " + versionNumber + " restored as new version " + doc.getVersions().size());
    }
}
