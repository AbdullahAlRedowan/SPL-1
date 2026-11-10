import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LCS {

    static ArrayList<String> readWords(File file) throws Exception {

        String text = new String(Files.readAllBytes(file.toPath()));
        text = text.toLowerCase().replaceAll("[^a-z0-9\\s]"," ");
        String[] words = text.trim().split("\\s+");

        return new ArrayList<>(Arrays.asList(words)) ;
    }

    static List<String> tokenize(String text) {
        String cleaned = text.toLowerCase().replaceAll("[^a-z0-9\\s]", " ").trim().replaceAll("\\s+", " ");
        List<String> tokens = new ArrayList<>();
        if (!cleaned.isEmpty()) {
            Collections.addAll(tokens, cleaned.split(" "));
        }
        return tokens;
    }

    static int[][] buildTable(List<String> a, List<String> b) {
        int n = a.size();
        int m = b.size();
        int[][] dp = new int[n + 1][m + 1];

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                if (a.get(i - 1).equals(b.get(j - 1))) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }
        return dp;
    }

    static List<String> backtrack(int[][] dp, List<String> a, List<String> b) {
        List<String> common = new ArrayList<>();
        int i = a.size();
        int j = b.size();

        while (i > 0 && j > 0) {
            if (a.get(i - 1).equals(b.get(j - 1))) {
                common.add(a.get(i - 1));
                i--;
                j--;
            } else if (dp[i - 1][j] >= dp[i][j - 1]) {
                i--;
            } else {
                j--;
            }
        }
        Collections.reverse(common);
        return common;
    }
}