import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class VersionManager {

    private final Path DATA_ROOT = Paths.get("data");
    private final Path DOCS = DATA_ROOT.resolve("documents");
    private final Path VERS = DATA_ROOT.resolve("versions");
    private final String currentUser;

    public VersionManager(String username) {
        this.currentUser = username;
        try {
            Files.createDirectories(getUserDocs());
            Files.createDirectories(getUserVersions());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ---------------- Paths ----------------
    public Path getUserDocs() { return DOCS.resolve(currentUser); }
    public Path getUserVersions() { return VERS.resolve(currentUser); }

    // ---------------- Add / Save ----------------
    public void addFile(File source) throws IOException {
        String base = sanitize(source.getName());
        Path docFolder = getUserVersions().resolve(base);
        Files.createDirectories(docFolder);

        // Save version 1
        Path versionFile = docFolder.resolve("version_1.txt");
        byte[] bytes = Files.readAllBytes(source.toPath());
        Files.write(versionFile, bytes);

        // Save current
        Path current = getUserDocs().resolve(base + "_current.txt");
        Files.write(current, bytes);
    }

    public void saveNewVersion(String originalName, String content) throws IOException {
        String base = sanitize(originalName);
        Path docFolder = getUserVersions().resolve(base);
        if (!Files.exists(docFolder)) throw new IOException("Document not found in version store.");

        int next = nextVersionNumber(docFolder);
        Path versionFile = docFolder.resolve("version_" + next + ".txt");
        Files.writeString(versionFile, content, StandardCharsets.UTF_8);

        Path current = getUserDocs().resolve(base + "_current.txt");
        Files.writeString(current, content, StandardCharsets.UTF_8);
    }

    private int nextVersionNumber(Path docFolder) throws IOException {
        if (!Files.exists(docFolder)) return 1;
        OptionalInt max = Files.list(docFolder)
                .map(p -> p.getFileName().toString())
                .filter(n -> n.startsWith("version_") && n.endsWith(".txt"))
                .mapToInt(n -> Integer.parseInt(n.replace("version_", "").replace(".txt", "")))
                .max();
        return max.isPresent() ? max.getAsInt() + 1 : 1;
    }

    // ---------------- List / Retrieve ----------------
    public List<String> listVersions(String originalName) {
        String base = sanitize(originalName);
        Path docFolder = getUserVersions().resolve(base);
        if (!Files.exists(docFolder)) return Collections.emptyList();

        try {
            return Files.list(docFolder)
                    .map(p -> p.getFileName().toString())
                    .filter(n -> n.startsWith("version_") && n.endsWith(".txt"))
                    .sorted(Comparator.comparingInt(n -> Integer.parseInt(
                            n.replace("version_", "").replace(".txt", "")
                    )))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public String getVersionContent(String originalName, String versionName) throws IOException {
        Path file = getUserVersions().resolve(sanitize(originalName)).resolve(versionName);
        if (!Files.exists(file)) throw new FileNotFoundException("Version not found: " + versionName);
        return Files.readString(file, StandardCharsets.UTF_8);
    }

    public String getLatestVersionContent(String originalName) throws IOException {
        List<String> versions = listVersions(originalName);
        if (versions.isEmpty()) return "";
        return getVersionContent(originalName, versions.get(versions.size() - 1));
    }

    public void revertToVersion(String originalName, String versionName) throws IOException {
        String content = getVersionContent(originalName, versionName);
        saveNewVersion(originalName, content);
    }

    public String getPreviousVersionName(String originalName, String versionName) {
        List<String> versions = listVersions(originalName);
        for (int i = 0; i < versions.size(); i++) {
            if (versions.get(i).equals(versionName)) {
                return i - 1 >= 0 ? versions.get(i - 1) : null;
            }
        }
        return null;
    }

    private String sanitize(String name) {
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
