import jdk.nashorn.internal.runtime.ParserException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Haylem on 23/08/2017.
 */
public class ExtractParser extends Parser {

    List<Integer> extractFeatures;
    List<Integer> dateFeatures;

    boolean offset = false;

    public ExtractParser(String fileName, SizeChecker checker, List<Integer> targets){
        super(fileName,checker);
        extractFeatures = targets;
        this.dateFeatures = new ArrayList<>();
    }

    public ExtractParser(String fileName, SizeChecker checker,List<Integer> targets, List<Integer> dateFeatures) {
        super(fileName,checker);
        extractFeatures = targets;
        this.dateFeatures = dateFeatures;
    }

    public ExtractParser(String fileName, SizeChecker checker,List<Integer> targets, List<Integer> dateFeatures, boolean offset){
        super(fileName,checker);
        extractFeatures = targets;
        this.dateFeatures = dateFeatures;
    }

    public ExtractParser(String[] header, SizeChecker checker,String fileName){
        super(fileName,checker);
        extractFeatures = new ArrayList<>();
        this.dateFeatures = new ArrayList<>();
    }

    public void createHeader(String[] header) throws IOException{
        int writeNum = 0;
        for(int i = 0; i < header.length; i++){
            header[i] = header[i].trim();

            if(extractFeatures.contains(i)){

                if(i > 0 && writeNum > 0){
                    writer.write(", ");
                }

                writeNum++;

                if(dateFeatures.contains(i)){
                    String dateH = header[i]+"_date";
                    String timeH = header[i]+"_time";
                    writer.write(dateH+", ");
                    writer.write(timeH);
                } else {
                    writer.write(header[i]);
                }
            }
        }
        writer.write("\n");
        writer.flush();
    }

    public void readLine(String[] tokens, int lineNum) throws ParserException, IOException {
        for (int i = 0; i < extractFeatures.size(); i++) {
            if (extractFeatures.get(i) >= tokens.length) {
                throw new ParserException("Target feature of: " + i +
                        " but number of tokens to scan is length: " + tokens.length);
            }

            if (i > 0) {
                writer.write(", ");
            }

            if(dateFeatures.contains(extractFeatures.get(i))){
                // parse date feature
                float[] dateTime = Utility.parseDate(tokens[extractFeatures.get(i)], offset);
                if(dateTime == null){
                    writer.write(-1+", "+-1);
                } else {
                    writer.write(dateTime[0] + ", " + dateTime[1]);
                }
            } else {
                writer.write(tokens[extractFeatures.get(i)]);
            }
        }
        writer.write("\n");
        writer.flush();
    }

    public void close() {

    }

}
