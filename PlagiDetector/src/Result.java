import java.util.List;

public class Result {

    String fileName;
    int copied;
    double lcsPercent;
    double jaccard;
    double cosine;
    int phraseLen;
    String phrase;
    public Result(String fileName, int copied, double lcsPercent,
                  double jaccard, double cosine, List<String> phraseWords) {
        this.fileName = fileName;
        this.copied = copied;
        this.lcsPercent = lcsPercent;
        this.jaccard = jaccard;
        this.cosine = cosine;
        this.phraseLen = phraseWords.size();
        this.phrase = String.join(" ", phraseWords);
    }
}