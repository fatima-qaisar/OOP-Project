package models;

import utils.HashUtils;
import java.time.LocalDateTime;

public class Version {

    private int versionNumber;
    private String content;
    private String hash;
    private LocalDateTime timestamp;

    public Version(int versionNumber, String content) {
        this.versionNumber = versionNumber;
        this.content = content;
        this.hash = HashUtils.sha256(content);
        this.timestamp = LocalDateTime.now();
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public String getContent() {
        return content;
    }

    public String getHash() {
        return hash;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
