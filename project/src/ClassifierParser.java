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
    List<Integer> validTransactions;
    List<Integer> toCheck;
    List<Integer> toValidate;
    int idIndex;

    public ClassifierParser(String fileName, SizeChecker checker, int idIndex, List<Integer> toCheck, List<Integer> toValidate) {
        super(fileName, checker);
        transactions = new HashMap<>();
        validTransactions = new ArrayList<>();
        this.idIndex = idIndex;
        this.toCheck = toCheck;
        this.toValidate = toValidate;
    }
    protected  void readLine(String[] tokens, int lineNum) throws ParserException, IOException{
        if(idIndex < tokens.length){
            int index = Integer.parseInt(tokens[idIndex].trim());
            boolean valid = false;
            boolean different = false;
            boolean existing = false;

            if(transactions.containsKey(index)){
                existing = true;
                String[] oldTokens = transactions.get(index);
                for(int i = 0; i < oldTokens.length; i++){
                    if(toValidate.contains(i)){
                        if(!oldTokens[i].equalsIgnoreCase(tokens[i])){
                            valid = true;
                            // change made to feature that indicates validity
                        }
                    }

                    if(toCheck.contains(i)){
                        if(!different) {
                            different = !oldTokens[i].equalsIgnoreCase(tokens[i]);
                        }
                    }
                }

                String toWrite = ", false";

                if(valid){
                    toWrite = ", true";
                }

                if(different || (valid && !validTransactions.contains(index))){
                    String existingLine = Utility.concantTokens(oldTokens);
                    existingLine = existingLine.concat(toWrite);
                    writer.write(existingLine);
                    writer.write("\n");
                }

                if(valid){
                    validTransactions.add(index);
                }
            }
            if((!valid && different)||!existing) {
                transactions.put(index, tokens);
            }
        }
    }

    /**
     * Signal to the parser that there are no more lines
     * to read.
     */
    public  void close() throws IOException{
//        for(int id : transactions.keySet()){
//            String[] tokens = transactions.get(id);
//            writer.write(Utility.concantTokens(tokens)+", true");
//            writer.write("\n");
//        }
    }

    public  void createHeader(String[] header) throws IOException{
        writer.write(Utility.concantTokens(header)+", valid");
        writer.write("\n");
    }


}
