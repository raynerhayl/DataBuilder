/**
 * Created by Haylem on 25/08/2017.
 */
import jdk.nashorn.internal.runtime.ParserException;
import javax.rmi.CORBA.Util;
import java.io.IOException;
import java.util.*;

/**
 * Created by Haylem on 23/08/2017.
 */
public class ClassifierParser extends Parser {

    Map<Integer, Stack<String[]>> transactions;
    List<Integer> validTransactions;
    List<Integer> toCheck;
    List<Integer> toValidate;
    int idIndex;
    int lineLength;

    boolean singleValue;

    public ClassifierParser(String fileName, SizeChecker checker, int idIndex, List<Integer> toCheck, List<Integer> toValidate, boolean singleValue) {
        super(fileName, checker);
        transactions = new HashMap<>();
        validTransactions = new ArrayList<>();
        this.idIndex = idIndex;
        this.toCheck = toCheck;
        this.toValidate = toValidate;
        this.singleValue = singleValue;
    }
    protected  void readLine(String[] tokens, int lineNum) throws ParserException, IOException{
        lineLength = tokens.length;
        if(idIndex < tokens.length){
            int index = Integer.parseInt(tokens[idIndex].trim());
            boolean valid = false;
            boolean different = false;
            boolean existing = false;

            int classIndex = tokens.length;

            if(transactions.containsKey(index)){
                existing = true;
                // top of stack is latest transaction
                String[] oldTokens = transactions.get(index).peek();

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

                        // this should contain the tokens from the old transaction
                        if(!oldTokens[i].equalsIgnoreCase(tokens[i])){
                            oldTokens[classIndex] = "false";
                        } else{
                            oldTokens[classIndex] = "true";
                        }
                        classIndex++;
                    }
                }

                if(valid){
                    for(int i =  tokens.length; i < oldTokens.length; i++){
                        oldTokens[i] = "true";
                    }
                    validTransactions.add(index);
                }
            }

            String[] toWrite = new String[tokens.length+toCheck.size()];
            for(int i = 0; i < toWrite.length; i++){
                if(i < tokens.length) {
                    toWrite[i] = tokens[i];
                } else {
                    toWrite[i] = "SURPRISE";
                }
            }

            if((!valid && different)||!existing) {
                if(transactions.containsKey(index)){
                    transactions.get(index).push(toWrite);
                } else {
                    Stack newTransactions = new Stack();
                    newTransactions.push(toWrite);
                    transactions.put(index, newTransactions);
                }
            }
        }
    }

    /**
     * Signal to the parser that there are no more lines
     * to read.
     */
    public  void close() throws IOException{
        System.out.println("Printing results...");
        int numPrinted = 0;
        for(int id : transactions.keySet()){
            if(validTransactions.contains(id)){
                // transaction was correct
                // write history of transaction
                Stack<String[]> history = transactions.get(id);

                if(history != null && history.size()<1){
                    continue;
                } else{
                    // top element is valid
                    //+",true"
                    String[] firstEntry = history.pop();

                    if(!singleValue) {
                        for (int i = lineLength; i < firstEntry.length; i++) {
                            firstEntry[i] = "true";
                        }
                        writer.write(Utility.concantTokens(firstEntry));
                    } else {

                        String[] toWrite = new String[lineLength+1];
                        for(int i = 0; i < lineLength; i++){
                            toWrite[i] = firstEntry[i];
                        }
                        toWrite[toWrite.length-1] = "true";
                        writer.write(Utility.concantTokens(toWrite));
                    }
                    writer.write("\n");

                    // other transactions are in-valid
                    while(!history.isEmpty()){
                        //+",false"
                        String[] transactionHistory = history.pop();
                        if(!singleValue) {
                            writer.write(Utility.concantTokens(history.pop()));
                        } else{
                            boolean valid = true;
                            String[] toWrite = new String[lineLength+1];
                            for(int i = 0; i < transactionHistory.length; i++){
                                if(i>=lineLength) {
                                    if (!Boolean.parseBoolean(transactionHistory[i])) {
                                        valid = false;
                                    }
                                } else{
                                    toWrite[i]=transactionHistory[i];
                                }
                            }
                            toWrite[toWrite.length-1]=String.valueOf(valid);
                            writer.write(Utility.concantTokens(toWrite));
                        }
                        writer.write("\n");
                    }
                }
                writer.flush();
            }
        }
        numPrinted++;
        if(((float)transactions.size()/numPrinted%0.1f==0)){
            System.out.println("Progress~"+transactions.size()/numPrinted);
        }
    }

    public  void createHeader(String[] header) throws IOException{
        writer.write(Utility.concantTokens(header)+",valid");
        writer.write("\n");
    }


}
