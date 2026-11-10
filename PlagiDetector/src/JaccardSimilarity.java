import java.util.HashSet;
import java.util.Set;

public class JaccardSimilarity {

    private int n;

    public JaccardSimilarity(int n) {
        if (n < 1) {
            throw new IllegalArgumentException("n must be at least 1");
        }
        this.n = n;
    }

    private String[] splitIntoWords(String text) {
        text = text.toLowerCase();
        text = text.replaceAll("[^a-z0-9]", " ").trim();

        if (text.isEmpty()) {
            return new String[0];
        }
        return text.split("\\s+");
    }

    public Set<String> buildNGrams(String text) {
        String[] words = splitIntoWords(text);
        Set<String> ngrams = new HashSet<>();

        if (words.length == 0) {
            return ngrams;
        }

        if (words.length < n) {
            ngrams.add(String.join(" ", words));
            return ngrams;
        }

        for (int i = 0; i + n <= words.length; i++) {
            StringBuilder gram = new StringBuilder();
            for (int j = i; j < i + n; j++) {
                gram.append(words[j]);
                if (j < i + n - 1) {
                    gram.append(" ");
                }
            }
            ngrams.add(gram.toString());
        }
        return ngrams;
    }

    public double calculate(Set<String> setA, Set<String> setB) {
        if (setA.isEmpty() && setB.isEmpty()) {
            return 0.0;
        }

        Set<String> small = setA.size() <= setB.size() ? setA : setB;
        Set<String> large = (small == setA) ? setB : setA;

        int common = 0;
        for (String gram : small) {
            if (large.contains(gram)) {
                common++;
            }
        }

        int union = setA.size() + setB.size() - common;
        return (double) common / union;
    }

    public double calculate(String text1, String text2) {
        return calculate(buildNGrams(text1), buildNGrams(text2));
    }

    public double calculatePercent(Set<String> setA, Set<String> setB) {
        return calculate(setA, setB) * 100;
    }
}