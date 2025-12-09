import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

class VersionManager {
    private final Path DATA_ROOT = Paths.get("data");
    private final Path DOCS = DATA_ROOT.resolve("documents");
    private final Path VERS = DATA_ROOT.resolve("versions");

    VersionManager() {
        try {
            Files.createDirectories(DOCS);
            Files.createDirectories(VERS);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Adds a file (copies it into versions as version_1 and into documents/current_{name})
    public void addFile(File source) throws IOException {
        String base = sanitize(source.getName());
        Path docFolder = VERS.resolve(base);
        Files.createDirectories(docFolder);

        // determine next available version number (should be 1)
        int next = 1;
        Path versionFile = docFolder.resolve("version_" + next + ".txt");

        // copy file contents
        byte[] bytes = Files.readAllBytes(source.toPath());
        Files.write(versionFile, bytes);

        // copy to documents as current
        Path current = DOCS.resolve(base + "_current.txt");
        Files.write(current, bytes);
    }

    public void saveNewVersion(String originalName, String content) throws IOException {
        String base = sanitize(originalName);
        Path docFolder = VERS.resolve(base);
        if (!Files.exists(docFolder)) throw new IOException("Document not found in version store.");

        int next = nextVersionNumber(docFolder);
        Path versionFile = docFolder.resolve("version_" + next + ".txt");
        Files.writeString(versionFile, content, StandardCharsets.UTF_8);

        // update current
        Path current = DOCS.resolve(base + "_current.txt");
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

    public List<String> listVersions(String originalName) {
        String base = sanitize(originalName);
        Path docFolder = VERS.resolve(base);
        if (!Files.exists(docFolder)) return Collections.emptyList();

        try {
            return Files.list(docFolder)
                    .map(p -> p.getFileName().toString())
                    .filter(n -> n.startsWith("version_") && n.endsWith(".txt"))
                    .sorted(Comparator.naturalOrder())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public String getVersionContent(String originalName, String versionName) throws IOException {
        String base = sanitize(originalName);
        Path docFolder = VERS.resolve(base);
        Path file = docFolder.resolve(versionName);
        return Files.readString(file);
    }

    public String getLatestVersionContent(String originalName) throws IOException {
        String base = sanitize(originalName);
        Path docFolder = VERS.resolve(base);
        List<String> versions = listVersions(originalName);
        if (versions.isEmpty()) return "";
        String last = versions.get(versions.size() - 1);
        return getVersionContent(originalName, last);
    }

    public void revertToVersion(String originalName, String versionName) throws IOException {
        String base = sanitize(originalName);
        Path docFolder = VERS.resolve(base);
        Path selected = docFolder.resolve(versionName);
        if (!Files.exists(selected)) throw new IOException("Selected version not found.");

        String content = Files.readString(selected);
        // Create a new version with this content (so revert creates a new current version)
        int next = nextVersionNumber(docFolder);
        Path newVersion = docFolder.resolve("version_" + next + ".txt");
        Files.writeString(newVersion, content, StandardCharsets.UTF_8);

        // update current
        Path current = DOCS.resolve(base + "_current.txt");
        Files.writeString(current, content, StandardCharsets.UTF_8);
    }

    // return previous version name (null if none)
    public String getPreviousVersionName(String originalName, String versionName) throws IOException {
        List<String> list = listVersions(originalName);
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equals(versionName)) {
                if (i - 1 >= 0) return list.get(i - 1);
                else return null;
            }
        }
        return null;
    }

    private String sanitize(String name) {
        // return a folder-friendly base name (remove spaces and colons)
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
