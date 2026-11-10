import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class MainMerge {

    static void printTop(String algorithm, List<Result> results) {

        if (algorithm.equals("LCS")) {
            Collections.sort(results, Comparator.comparingDouble((Result r) -> r.lcsPercent).reversed());
        } else if (algorithm.equals("JACCARD")) {
            Collections.sort(results, Comparator.comparingDouble((Result r) -> r.jaccard).reversed());
        } else {
            Collections.sort(results, Comparator.comparingDouble((Result r) -> r.cosine).reversed());
        }

        System.out.println();
        System.out.println("===== Top 5 by " + algorithm + " =====");

        int limit = Math.min(5, results.size());
        for (int k = 0; k < limit; k++) {
            Result r = results.get(k);

            double score;
            if (algorithm.equals("LCS")) {
                score = r.lcsPercent;
            } else if (algorithm.equals("JACCARD")) {
                score = r.jaccard;
            } else {
                score = r.cosine;
            }

            System.out.printf("%d. %s -> %.2f%%%n", k + 1, r.fileName, score);
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Enter <target> <file or folder>");
            return;
        }

        String filePath = args[0];
        String secondPath = args[1];

        File targetFile = new File(filePath);
        File second = new File(secondPath);

        if (!targetFile.isFile()) {
            System.out.println("Target must be an existing file: " + filePath);
            return;
        }
        if (!second.exists()) {
            System.out.println("Path not found: " + secondPath);
            return;
        }

        String textA = Files.readString(Paths.get(filePath));

        List<String> tokensOfTarget = LCS.tokenize(textA);
        if (tokensOfTarget.isEmpty()) {
            System.out.println("Target file is empty.");
            return;
        }

        JaccardSimilarity jaccard = new JaccardSimilarity(3);
        CosineSimilarity cosine = new CosineSimilarity();
        Set<String> targetGrams = jaccard.buildNGrams(textA);
        HashMap<String, Integer> targetFreq = cosine.buildFrequencyMap(textA);

        ArrayList<File> textFiles = new ArrayList<>();
        if (second.isDirectory()) {
            FileCollector.collectTxtFiles(second, textFiles);
        } else {
            textFiles.add(second);
        }

        if (textFiles.isEmpty()) {
            System.out.println("No .txt files found in folder");
            return;
        }
        System.out.println("Target file is " + args[0]);

        String targetCanonical = targetFile.getCanonicalPath();
        List<Result> results = new ArrayList<>();

        int i = 0;
        while (i < textFiles.size()) {

            if (textFiles.get(i).getCanonicalPath().equals(targetCanonical)) {
                i++;
                continue;
            }

            try {
                String textB = Files.readString(textFiles.get(i).toPath());

                List<String> tokensOfComparedFile = LCS.tokenize(textB);
                int[][] dp = LCS.buildTable(tokensOfTarget, tokensOfComparedFile);
                List<String> common = LCS.backtrack(dp, tokensOfTarget, tokensOfComparedFile);

                double lcsPercent = common.size() * 100.0 / tokensOfTarget.size();
                double jaccardPercent = jaccard.calculatePercent(targetGrams, jaccard.buildNGrams(textB));
                double cosinePercent = cosine.calculatePercent(targetFreq, cosine.buildFrequencyMap(textB));

                results.add(new Result(textFiles.get(i).getPath(), common.size(),
                        lcsPercent, jaccardPercent, cosinePercent));

            } catch (Exception e) {
                System.out.println("Skipped (cannot read): " + textFiles.get(i).getPath());
            }

            i++;
        }

        if (results.isEmpty()) {
            System.out.println("No file to compare.");
            return;
        }

        printTop("LCS", results);
        printTop("JACCARD", results);
        printTop("COSINE", results);
    }
}