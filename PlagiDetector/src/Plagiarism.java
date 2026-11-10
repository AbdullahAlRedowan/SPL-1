import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Plagiarism {
    static ArrayList<File> getTextFiles(String folderPath){
        ArrayList<File> result = new ArrayList<>();

        File folder = new File(folderPath);
        File[] all = folder.listFiles();
        if(all == null) {
            return result;
        }
        for(File f : all) {
            if(f.isFile() && f.getName().endsWith(".txt")) {
                result.add(f);
            }
        }
        return result;
    }

    public static void main(String[] args) throws Exception{
        if(args.length < 2){
            System.out.println("Enter <target> <folder>");
            return;
        }

        String filePath = args[0];
        String folderPath = args[1];

        String textA = Files.readString(Paths.get(filePath));
        List<String> tokensOfTarget = LCS.tokenize(textA);

        if (tokensOfTarget.isEmpty()) {
            System.out.println("Target file is empty.");
            return;
        }

        ArrayList<File> textFiles = getTextFiles(folderPath);

        if(textFiles.isEmpty()){
            System.out.println("No .txt files found in folder");
        }
        System.out.println("Target file is " + args[0]);

        int i = 0;
        while(i < textFiles.size()){

            File targetFile = new File(filePath);
            if(textFiles.get(i).getCanonicalPath().equals(targetFile.getCanonicalPath())){
                i++;
                continue;
            }

            List<String> tokensOfComparedFile = LCS.tokenize(Files.readString(textFiles.get(i).toPath()));
            int[][] dp = LCS.buildTable(tokensOfTarget, tokensOfComparedFile);
            List<String> common = LCS.backtrack(dp, tokensOfTarget, tokensOfComparedFile);

            int copied = common.size();
            double plagiarised = copied * 100.0 / tokensOfTarget.size();
            double unique = 100.0 - plagiarised;

            System.out.println("Compared file is : " + textFiles.get(i));
            System.out.println("Copied word (LCS)  : "+copied);
            System.out.printf("Plagiarised        :  %.2f%%%n", plagiarised);
            System.out.printf("Unique             : %.2f%%%n", unique);
            System.out.println("Copied text        : " + String.join(" ", common));

            i++ ;
        }
    }
}