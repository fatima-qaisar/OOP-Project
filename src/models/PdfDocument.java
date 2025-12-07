package models;

public class PdfDocument extends Document {

    public PdfDocument(String filePath) {
        super(filePath);
    }

    @Override
    public String extractText() {
        // Placeholder: in future, real PDF extraction
        return "Text extracted from " + getFileName();
    }

    @Override
    public String getType() {
        return "PDF";
    }
}
