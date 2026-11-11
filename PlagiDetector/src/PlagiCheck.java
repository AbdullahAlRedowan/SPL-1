import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Arrays;

public class PlagiCheck {

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

    static final int MIN_PHRASE = 5;

    static double strongScore(Result r) {
        return Math.max(r.lcsPercent, r.jaccard);
    }

//    static String verdict(Result r) {
////        double s = strongScore(r);
////        if (s >= 70) return "HIGH";
////        if (s >= 40) return "MEDIUM";
////        return "LOW";
//
//    }

    static String verdict(Result r) {
        if (r.phraseLen >= 50 || r.jaccard >= 20) return "HIGH";
        if (r.phraseLen >= 15 || r.jaccard >= 5)  return "MEDIUM";
        return "LOW";
    }

    static String shortName(String path) {
        return new File(path).getName();
    }

    static void printHeader(String targetPath, int targetWords,
                            int compared, int skipped, double seconds) {
        System.out.println("============================================");
        System.out.println("Target   : " + shortName(targetPath) + " (" + targetWords + " words)");
        System.out.println("Compared : " + compared + " files | Skipped: " + skipped);
        System.out.printf("Time     : %.2f s%n", seconds);
        System.out.println("============================================");
    }

    static void printSummaryTable(List<Result> results) {
        results.sort(Comparator.comparingInt((Result r) -> r.phraseLen).reversed());


        System.out.println();
        System.out.println("===== Summary (sorted by longest copied phrase and jaccard score) =====");
        System.out.printf("%-3s %-28s %8s %9s %8s  %s%n",
                "#", "File", "LCS%", "Jaccard%", "Cosine%", "Verdict");

        int limit = Math.min(3, results.size());
        for (int k = 0; k < limit; k++) {
            Result r = results.get(k);
            System.out.printf("%-3d %-28s %8.2f %9.2f %8.2f  %s%n",
                    k + 1, shortName(r.fileName),
                    r.lcsPercent, r.jaccard, r.cosine, verdict(r));
        }
    }

    static void printPhraseMatches(List<Result> results) {
        List<Result> matched = new ArrayList<>();
        for (Result r : results) {
            if (r.phraseLen >= MIN_PHRASE) {
                matched.add(r);
            }
        }

        System.out.println();
        System.out.println("===== Files sharing a phrase of " + MIN_PHRASE + "+ words ("
                + matched.size() + " files) =====");

        if (matched.isEmpty()) {
            System.out.println("None found.");
            return;
        }

        matched.sort(Comparator.comparingInt((Result r) -> r.phraseLen).reversed());

        for (Result r : matched) {
            System.out.println();
            System.out.println(shortName(r.fileName));
            System.out.printf("  Longest copied phrase (%d words): \"%s\"%n",
                    r.phraseLen, shorten(r.phrase));
        }
    }

    static String shorten(String phrase) {
        String[] w = phrase.split(" ");
        if (w.length <= 20) {
            return phrase;
        }
        return String.join(" ", Arrays.copyOfRange(w, 0, 12))
                + " ... "
                + String.join(" ", Arrays.copyOfRange(w, w.length - 6, w.length));
    }



    public static void main(String[] args) throws Exception {
        long startTime = System.nanoTime();

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

        String targetCanonical = targetFile.getCanonicalPath();
        List<Result> results = new ArrayList<>();
        int skipped = 0;

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

                List<String> phrase = LCS.longestCommonPhrase(tokensOfTarget, tokensOfComparedFile);

                results.add(new Result(textFiles.get(i).getPath(), common.size(),
                        lcsPercent, jaccardPercent, cosinePercent, phrase));

            } catch (Exception e) {
                skipped++;
                System.out.println("Skipped (cannot read): " + textFiles.get(i).getPath());
            }

            i++;
        }

        if (results.isEmpty()) {
            System.out.println("No file to compare.");
            return;
        }

        double seconds = (System.nanoTime() - startTime) / 1_000_000_000.0;

        printHeader(filePath, tokensOfTarget.size(), results.size(), skipped, seconds);
        printSummaryTable(results);
        printPhraseMatches(results);
    }
}