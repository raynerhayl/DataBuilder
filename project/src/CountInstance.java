/**
 * Created by Haylem on 25/08/2017.
 */
public class CountInstance implements Comparable<CountInstance> {

    int count;
    String label;

    public CountInstance(int count, String label){
        this.count = count;
        this.label = label;
    }

    public int compareTo(CountInstance o){
        return o.count - count;
    }

}
