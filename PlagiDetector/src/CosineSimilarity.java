import java.util.HashMap;

public class CosineSimilarity {

    private String[] splitIntoWords(String text) {
        text = text.toLowerCase();

        text = text.replaceAll("[^a-z0-9]", " ").trim();

        if (text.isEmpty()) {
            return new String[0];
        }
        return text.split("\\s+");
    }

    public HashMap<String, Integer> buildFrequencyMap(String text) {
        String[] words = splitIntoWords(text);
        HashMap<String, Integer> freq = new HashMap<>();

        for (String word : words) {
            if (freq.containsKey(word)) {
                freq.put(word, freq.get(word) + 1);
            } else {
                freq.put(word, 1);
            }
        }
        return freq;
    }

    public double calculate(HashMap<String, Integer> freqA, HashMap<String, Integer> freqB) {
        if (freqA.isEmpty() || freqB.isEmpty()) {
            return 0.0;
        }

        HashMap<String, Integer> small = freqA.size() <= freqB.size() ? freqA : freqB;
        HashMap<String, Integer> large = (small == freqA) ? freqB : freqA;

        double dot = 0;
        for (String word : small.keySet()) {
            if (large.containsKey(word)) {
                dot += (double) small.get(word) * large.get(word);
            }
        }

        double normA = vectorLength(freqA);
        double normB = vectorLength(freqB);

        return dot / (normA * normB);
    }

    private double vectorLength(HashMap<String, Integer> freq) {
        double sum = 0;
        for (int count : freq.values()) {
            sum += (double) count * count;
        }
        return Math.sqrt(sum);
    }

    public double calculate(String text1, String text2) {
        return calculate(buildFrequencyMap(text1), buildFrequencyMap(text2));
    }

    public double calculatePercent(HashMap<String, Integer> freqA, HashMap<String, Integer> freqB) {
        return calculate(freqA, freqB) * 100;
    }
}