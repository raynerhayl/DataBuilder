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
    int idIndex;

    public ClassifierParser(String fileName, SizeChecker checker, int idIndex) {
        super(fileName, checker);
        transactions = new HashMap<>();
        this.idIndex = idIndex;
    }
    protected  void readLine(String[] tokens, int lineNum) throws ParserException, IOException{
        if(idIndex < tokens.length){
            int index = Integer.parseInt(tokens[idIndex].trim());
            if(transactions.containsKey(index)){
                String[] oldTokens = transactions.get(index);
                boolean different = false;
                for(int i = 0; i < oldTokens.length; i++){
                    /*
                    TODO: Use the pay key as an indicator for whether it's valid, can always
                    TODO: transaction in memory to check if it is in-fact changed in future
                     */
                    different = oldTokens[i].trim().equalsIgnoreCase(tokens[i].trim()) != true;
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
