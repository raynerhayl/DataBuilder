import java.io.File;

/**
 * Created by Haylem on 24/08/2017.
 */
public class SizeChecker {

    File file;
    long size;
    long checked = 0;

    float increment = 0;
    float incrementChecked = 0;

    public SizeChecker(File file){
        this.file = file;
        size = file.length();
        increment = (float)size * 0.01f;
        System.out.println("File size of: "+size);
    }

    public float checkLine(String line){
        float percentage = -1;

        if(checked - incrementChecked >= increment){
            incrementChecked+= increment;
            percentage = incrementChecked/increment;
            System.out.println("Read: "+percentage+"%");
        }

        long bytes = line.getBytes().length;
        checked += bytes;

        return percentage;
    }

    public void reset(){
        checked = 0;
    }

}
