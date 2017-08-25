/**
 * Created by Haylem on 24/08/2017.
 */

import jdk.nashorn.internal.runtime.ParserException;
import java.io.IOException;
import java.util.*;


public class CountCompanyParser extends Parser{

    Map<String, Integer> counts;
    int companyIndex;

    public CountCompanyParser(String fileName, SizeChecker checker, int index){
        super(fileName+"_trim.csv", checker);
        counts = new HashMap<String, Integer>();
        companyIndex = index;
    }

    protected void readLine(String[] tokens, int lineNum) throws ParserException, IOException{
        if(counts.containsKey(tokens[companyIndex])){
            counts.put(tokens[companyIndex], counts.get(tokens[companyIndex]) + 1);
        } else {
            counts.put(tokens[companyIndex], 1);
        }
    }

    public void createHeader(String[] header) throws IOException{

    }

    public void close() throws IOException{
        Queue<CountInstance> queue = new PriorityQueue<>();
        for(String key: counts.keySet()){
            CountInstance instance = new CountInstance(counts.get(key), key);
            queue.add(instance);
        }

        while(queue.isEmpty() == false){
            CountInstance instance = queue.poll();
            writer.write(instance.label+", "+instance.count);
            writer.write("\n");
        }
    }

}
