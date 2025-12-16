import java.io.IOException;
import java.nio.file.*;
import java.util.List;

public class UserManager {

    private final Path USERS_FILE = Paths.get("data", "users.txt");

    public UserManager() {
        try {
            Files.createDirectories(USERS_FILE.getParent());
            if (!Files.exists(USERS_FILE)) {
                Files.write(USERS_FILE, List.of());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean authenticate(String username, String password) {
        try {
            List<String> lines = Files.readAllLines(USERS_FILE);
            username = username.trim();
            password = password.trim();
            for (String line : lines) {
                String[] parts = line.trim().split(",");
                if (parts.length == 2 && parts[0].trim().equals(username) && parts[1].trim().equals(password)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean userExists(String username) {
        try {
            List<String> lines = Files.readAllLines(USERS_FILE);
            for (String line : lines) {
                if (line.trim().split(",")[0].trim().equals(username)) return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void registerUser(String username, String password) {
        try {
            String line = username + "," + password;
            Files.write(USERS_FILE, List.of(line), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
