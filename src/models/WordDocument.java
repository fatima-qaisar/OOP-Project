package models;

public class WordDocument extends Document {

    public WordDocument(String filePath) {
        super(filePath);
    }

    @Override
    public String extractText() {
        // Placeholder: in future, real DOCX extraction
        return "Text extracted from " + getFileName();
    }

    @Override
    public String getType() {
        return "DOCX";
    }
}
