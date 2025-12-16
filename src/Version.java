class Version {
    private String name; // e.g., version_1.txt
    private String timestamp; // optional

    public Version(String name) {
        this.name = name;
    }
    public String getName() { return name; }
}