package models;

import java.util.ArrayList;
import java.util.List;

public abstract class Document {
    protected String filePath;
    protected String fileName;
    protected String content;

    protected List<Version> versions = new ArrayList<>();

    public Document(String filePath) {
        this.filePath = filePath;
        this.fileName = extractFileName(filePath);
    }

    private String extractFileName(String filePath) {
        int index = filePath.lastIndexOf('/');
        if (index == -1) index = filePath.lastIndexOf('\\');
        return filePath.substring(index + 1);
    }

    // Abstract methods for subclasses to implement
    public abstract String extractText(); // How text is extracted per type
    public abstract String getType();     // "PDF", "DOCX", "TXT", "PPT"

    // Version management
    public void initializeVersion() {
        this.content = extractText();
        addVersion(content);
    }

    public void addVersion(String newContent) {
        int newVersionNumber = versions.size() + 1;
        Version version = new Version(newVersionNumber, newContent);
        versions.add(version);
    }

    public List<Version> getVersions() {
        return versions;
    }

    public Version getVersion(int versionNumber) {
        if (versionNumber <= 0 || versionNumber > versions.size()) return null;
        return versions.get(versionNumber - 1);
    }

    public void restoreVersion(int versionNumber) {
        Version v = getVersion(versionNumber);
        if (v != null) {
            this.content = v.getContent();
            addVersion(this.content); // restoring creates new version
        }
    }

    public String getContent() {
        return content;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFilePath() {
        return filePath;
    }
    public String getLatestContent() {
    return versions.get(versions.size() - 1).getContent();
}

}
