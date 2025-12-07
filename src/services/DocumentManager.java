package services;

import models.*;
import utils.FileUtils;

import java.util.HashMap;
import java.util.Scanner;

public class DocumentManager {

    private HashMap<String, Document> documents = new HashMap<>();
    private VersionControlService versionService = new VersionControlService();
    private DiffService diffService = new DiffService();

    public DocumentManager() { }

    // -------------------- CREATE DOCUMENT --------------------
    public Document createDocument(String type, String filePath) {
        if (!FileUtils.isSupported(filePath)) {
            System.out.println("Unsupported document type!");
            return null;
        }

        Document doc = null;

        switch (type.toUpperCase()) {
            case "TXT":
                doc = new TextDocument(filePath);
                break;
            case "PDF":
                doc = new PdfDocument(filePath);
                break;
            case "DOCX":
                doc = new WordDocument(filePath);
                break;
            case "PPT":
                doc = new PptDocument(filePath);
                break;
        }

        if (doc == null) return null;

        doc.initializeVersion(); // Version 1 automatically
        documents.put(filePath, doc);

        // Save first version to disk if TXT
        if (doc instanceof TextDocument) ((TextDocument) doc).saveToDisk();

        System.out.println("Document added: " + filePath + " | Type: " + type);
        return doc;
    }

    // Overloaded: create document with initial content
    public Document createDocument(String type, String filePath, String initialContent) {
        Document doc = createDocument(type, filePath);
        if (doc != null && initialContent != null && !initialContent.isEmpty()) {
            doc.addVersion(initialContent); // override placeholder
            if (doc instanceof TextDocument) ((TextDocument) doc).saveToDisk();
        }
        return doc;
    }

    // -------------------- GET DOCUMENT --------------------
    public Document getDocument(String filePath) {
        return documents.get(filePath);
    }

    // -------------------- LIST DOCUMENTS --------------------
    public void listDocuments() {
        if (documents.isEmpty()) {
            System.out.println("No documents available.");
            return;
        }
        System.out.println("\nDocuments:");
        for (Document doc : documents.values()) {
            System.out.println("- " + doc.getFileName() + " (" + doc.getType() + ")");
        }
    }

    // -------------------- EDIT DOCUMENT --------------------
    public void editDocument(String filePath) {
        Document doc = getDocument(filePath);
        if (doc == null) {
            System.out.println("Document not found!");
            return;
        }

        System.out.println("\nCurrent content (latest version):");
        String latest = doc.getLatestContent();
        System.out.println(latest);


        Scanner sc = new Scanner(System.in);
        System.out.println("\nEnter new content:");
        String newContent = sc.nextLine();

        editDocument(filePath, newContent);
    }


    // Overloaded: edit using provided content
    public void editDocument(String filePath, String newContent) {
        Document doc = getDocument(filePath);
        if (doc == null) {
            System.out.println("Document not found!");
            return;
        }

        doc.addVersion(newContent);

        // Save to disk automatically if TXT
        if (doc instanceof TextDocument) {
            ((TextDocument) doc).saveToDisk();
        }

        System.out.println("New version created! Total versions: " + doc.getVersions().size());
    }

    // -------------------- VERSION HISTORY --------------------
    public void viewVersionHistory(String filePath) {
        Document doc = getDocument(filePath);
        if (doc == null) {
            System.out.println("Document not found!");
            return;
        }

        System.out.println("\nVersion history for: " + doc.getFileName());
        for (Version v : doc.getVersions()) {
            System.out.println("Version " + v.getVersionNumber() +
                               " | Timestamp: " + v.getTimestamp() +
                               " | Hash: " + v.getHash());
        }
    }

    // -------------------- RESTORE VERSION --------------------
    public void restoreVersion(String filePath, int versionNumber) {
        Document doc = getDocument(filePath);
        if (doc == null) {
            System.out.println("Document not found!");
            return;
        }

        versionService.restoreVersion(doc, versionNumber);

        // Save restored version if TXT
        if (doc instanceof TextDocument) {
            ((TextDocument) doc).saveToDisk();
        }
    }

    // -------------------- SHOW DIFF --------------------
    public void showDiff(String filePath, int v1, int v2) {
        Document doc = getDocument(filePath);
        if (doc == null) {
            System.out.println("Document not found!");
            return;
        }

        diffService.showDiff(doc, v1, v2);
    }

    // Overloaded: show diff between two strings
    public void showDiff(String oldContent, String newContent) {
        diffService.showDiff(oldContent, newContent);
    }
}
