/**
 * Created by Haylem on 25/08/2017.
 */
import jdk.nashorn.internal.runtime.ParserException;
import javax.rmi.CORBA.Util;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Haylem on 23/08/2017.
 */
public class ClassifierParser extends Parser {

    Map<Integer, String[]> transactions;
    List<Integer> toCheck;
    int idIndex;

    public ClassifierParser(String fileName, SizeChecker checker, int idIndex, List<Integer> toCheck) {
        super(fileName, checker);
        transactions = new HashMap<>();
        this.idIndex = idIndex;
        this.toCheck = toCheck;
    }
    protected  void readLine(String[] tokens, int lineNum) throws ParserException, IOException{
        if(idIndex < tokens.length){
            int index = Integer.parseInt(tokens[idIndex].trim());
            if(transactions.containsKey(index)){
                String[] oldTokens = transactions.get(index);
                boolean different = false;
                boolean valid = false;
                for(int i = 0; i < oldTokens.length; i++){
                    if (toCheck.contains(i)) {
                        if(oldTokens[i].equalsIgnoreCase(tokens[i]) == false){
                            valid = true;
                        }
                    } else {
                        different = oldTokens[i].equalsIgnoreCase(tokens[i]) != true;
                    }
                }

                if(different){
                    String existingLine = Utility.concantTokens(oldTokens);
                    existingLine = existingLine.concat(", false");
                    writer.write(existingLine);
                    writer.write("\n");
                }
            }
            transactions.put(index, tokens);
        }
    }

    /**
     * Signal to the parser that there are no more lines
     * to read.
     */
    public  void close() throws IOException{
        for(int id : transactions.keySet()){
            String[] tokens = transactions.get(id);
            writer.write(Utility.concantTokens(tokens)+", true");
            writer.write("\n");
        }
    }

    public  void createHeader(String[] header) throws IOException{
        writer.write(Utility.concantTokens(header)+", valid");
        writer.write("\n");
    }


}
