package models;

public class PptDocument extends Document {

    public PptDocument(String filePath) {
        super(filePath);
    }

    @Override
    public String extractText() {
        // Placeholder: in future, real PPT extraction
        return "Text extracted from " + getFileName();
    }

    @Override
    public String getType() {
        return "PPT";
    }
}
