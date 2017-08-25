/**
 * Created by Haylem on 23/08/2017.
 */

import jdk.nashorn.internal.runtime.ParserException;

import java.io.*;

public abstract class Parser {

    protected BufferedWriter writer;
    SizeChecker checker;

    public Parser(String fileName, SizeChecker checker){
        try{
            writer = new BufferedWriter(new FileWriter(fileName));
        } catch(IOException e){
            System.out.println(e);
        }
        this.checker = checker;
    }

    /**
     * Signal the parser to read a given line
     * @param line
     * @throws ParseException
     */
    public void readLine(String line, int lineNum) throws ParserException{
        try {
            checker.checkLine(line);

            readLine(line.split(","), lineNum);
        } catch(IOException e){
            System.out.println("Error on line: "+lineNum+"\n"+e);
        }
    }

    protected abstract void readLine(String[] tokens, int lineNum) throws ParserException, IOException;

    /**
     * Signal to the parser that there are no more lines
     * to read.
     */
    public abstract void close() throws IOException;

    public abstract void createHeader(String[] header) throws IOException;

}
