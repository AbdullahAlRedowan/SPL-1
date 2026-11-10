// holds the scores of ONE compared file
public class Result {

    String fileName;
    int copied;          // LCS copied words
    double lcsPercent;   // LCS plagiarised %
    double jaccard;      // Jaccard %
    double cosine;       // Cosine %

    // constructor: fill everything in one step
    public Result(String fileName, int copied, double lcsPercent,
                  double jaccard, double cosine) {
        this.fileName = fileName;
        this.copied = copied;
        this.lcsPercent = lcsPercent;
        this.jaccard = jaccard;
        this.cosine = cosine;
    }
}