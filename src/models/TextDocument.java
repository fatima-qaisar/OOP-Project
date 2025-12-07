package models;

import services.FileStorageService;

import java.io.File;

public class TextDocument extends Document {

    private final String dataFolder = "data"; // root folder for all documents

    public TextDocument(String filePath) {
        super(filePath);
    }

    @Override
    public String extractText() {
        return FileStorageService.readFile(filePath);
    }

    @Override
    public String getType() {
        return "TXT";
    }

    // Save current version to disk in data/<DocumentName>/vX.txt
    public void saveToDisk() {
        try {
            String docFolderName = dataFolder + File.separator + getFileNameWithoutExtension();
            File folder = new File(docFolderName);
            if (!folder.exists()) folder.mkdirs(); // create folder if it doesn't exist

            int versionNumber = getVersions().size();
            String versionFile = docFolderName + File.separator + "v" + versionNumber + ".txt";

            FileStorageService.writeFile(versionFile, content);
        } catch (Exception e) {
            System.out.println("Error saving to disk: " + e.getMessage());
        }
    }

    // Helper to get filename without extension
    private String getFileNameWithoutExtension() {
        String name = getFileName();
        int idx = name.lastIndexOf('.');
        return (idx > 0) ? name.substring(0, idx) : name;
    }
}
