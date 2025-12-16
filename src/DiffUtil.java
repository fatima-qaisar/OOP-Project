import javafx.scene.text.Text;
import java.util.*;

public class DiffUtil {
    public static List<Text> generateTextNodes(String a, String b) {
        String[] A = a.split("\\s+");
        String[] B = b.split("\\s+");

        int n = A.length, m = B.length;
        int[][] dp = new int[n+1][m+1];
        for (int i = n-1; i >= 0; i--) {
            for (int j = m-1; j >= 0; j--) {
                if (A[i].equals(B[j])) dp[i][j] = 1 + dp[i+1][j+1];
                else dp[i][j] = Math.max(dp[i+1][j], dp[i][j+1]);
            }
        }

        List<Text> out = new ArrayList<>();
        int i=0, j=0;
        while (i<n || j<m) {
            if (i<n && j<m && A[i].equals(B[j])) {
                out.add(new Text(A[i] + " "));
                i++; j++;
            } else if (j<m && (i==n || dp[i][j+1] >= dp[i+1][j])) {
                Text t = new Text(B[j] + " ");
                t.setStyle("-fx-fill: green; -fx-font-weight: bold;");
                out.add(t);
                j++;
            } else if (i<n && (j==m || dp[i][j+1] < dp[i+1][j])) {
                Text t = new Text(A[i] + " ");
                t.setStyle("-fx-fill: red; -fx-strikethrough: true;");
                out.add(t);
                i++;
            } else break;
        }
        return out;
    }
}
