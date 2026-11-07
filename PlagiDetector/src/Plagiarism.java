import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Plagiarism {

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

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Usage: java Plagiarism <targetFile> <otherFile>");
            return;
        }

        String textA = Files.readString(Paths.get(args[0]));
        String textB = Files.readString(Paths.get(args[1]));

        List<String> a = tokenize(textA);
        List<String> b = tokenize(textB);

        if (a.isEmpty()) {
            System.out.println("Target file is empty.");
            return;
        }

        int[][] dp = buildTable(a, b);
        List<String> common = backtrack(dp, a, b);

        int copied = common.size();
        double plagiarised = copied * 100.0 / a.size();
        double unique = 100.0 - plagiarised;

        System.out.println("Target total words : " + a.size());
        System.out.println("Other file words   : " + b.size());
        System.out.println("Copied words (LCS) : " + copied);
        System.out.printf("Plagiarised        : %.2f%%%n", plagiarised);
        System.out.printf("Unique             : %.2f%%%n", unique);
        System.out.println("Copied text        : " + String.join(" ", common));
    }
}