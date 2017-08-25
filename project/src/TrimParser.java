/**
 * Created by Haylem on 24/08/2017.
 */

import jdk.nashorn.internal.runtime.ParserException;
import java.io.IOException;

public class TrimParser extends Parser{

    int lineCount = 0;

    public TrimParser(String fileName, SizeChecker checker){
        super(fileName, checker);
    }

    protected void readLine(String[] tokens, int lineNum) throws ParserException, IOException{
        if(tokens.length == lineCount){
            for(int i = 0; i < tokens.length; i++){
                writer.write(tokens[i]);
                if(i < tokens.length-1){
                    writer.write(", ");
                }
            }
            writer.write("\n");
            writer.flush();
        }
    }

    public void createHeader(String[] header) throws IOException{
        lineCount = header.length;
        for(int i = 0; i < header.length; i++){
            writer.write(header[i]);
            if(i < header.length-1){
                writer.write(", ");
            }
        }
        writer.write("\n");
        writer.flush();
    }

    public void close(){

    }
}