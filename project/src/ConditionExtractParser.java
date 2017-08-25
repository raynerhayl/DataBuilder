/**
 * Created by Haylem on 25/08/2017.
 */
import jdk.nashorn.internal.runtime.ParserException;

import javax.rmi.CORBA.Util;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Haylem on 23/08/2017.
 */
public class ConditionExtractParser extends Parser {

    int target;
    String condition;

    public ConditionExtractParser(String fileName, SizeChecker checker, int target, String condition) {
        super(fileName, checker);
        this.target = target;
        this.condition = condition;
    }
    protected  void readLine(String[] tokens, int lineNum) throws ParserException, IOException{
        if(target< tokens.length){
            String targetToken = tokens[target];
            if(targetToken.trim().equalsIgnoreCase(condition)){
                writer.write(Utility.concantTokens(tokens));
                writer.write("\n");
            }
        }
    }

    /**
     * Signal to the parser that there are no more lines
     * to read.
     */
    public  void close() throws IOException{

    }

    public  void createHeader(String[] header) throws IOException{
        writer.write(Utility.concantTokens(header));
        writer.write("\n");
    }


}
