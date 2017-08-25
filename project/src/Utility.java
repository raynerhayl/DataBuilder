import java.util.*;

import com.sun.corba.se.impl.io.TypeMismatchException;


/**
 * Created by Haylem on 23/08/2017.
 */
public class Utility {

    /**
     * Convert a time string of the form HH:MM:SS.MS to an integer value.
     * @param timeString
     * @return Number of seconds which the time represents
     */
    public static float timeOfDay(String timeString){
        if(timeString.split(":").length < 3){
            return -1;
        }
        String[] timeSeg = timeString.split(":");
        int hours, minutes;
        float seconds;
        try {
            hours = Integer.valueOf(timeSeg[0]);
            minutes = Integer.valueOf(timeSeg[1]);
            seconds = Float.valueOf(timeSeg[2]);
        } catch(TypeMismatchException e){
            return -1;
        }
        float time = hours * 60 * 60 + minutes * 60 + (int) seconds;
        time = time/(60*60);

        return time;
    }

    /**
     * Convert a date of form YYYY-MM-DD to a day of the week, where month
     * ranges between 1-12.
     *
     * @param dateString
     * @return day of the week 1-7 for Sunday-Saturday
     */
    public static int dayOfWeek(String dateString){
        return getCalendar(dateString).get(Calendar.DAY_OF_WEEK);
    }

    public static Calendar getCalendar(String dateString){
        if(dateString.split(" ")[0].split("-").length < 3){
            return null;
        }

        String[] segDate = dateString.split(" ")[0].split("-");

        int year_int,month_int,date_int;
        try {
            year_int = Integer.parseInt(segDate[0]);
            // note that month starts at 0
            month_int = Integer.parseInt(segDate[1]) - 1;
            date_int = Integer.parseInt(segDate[2]);
        } catch(TypeMismatchException e){
            return null;
        }

        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(year_int, month_int, date_int);
        return cal;
    }

    /**
     * Parse a date string, format YYYY:MM:DD HH:MM:SS to an array of the the
     * day of the week and the time.
     *
     * @param line String value of the date and time
     * @param offset Whether to offset the date by the start time of the given day
     * @return array of floats, first value the day of the week (actually an int)
     * second value the time of day.
     */
    public static float[] parseDate(String line, boolean offset){
        if (line.equals("NULL")) {
            return null;
        }

        if(line.split(" ").length <2){
            return null;
        }

        String dateString = line.split(" ")[0];
        String timeString = line.split(" ")[1];

        //int day = dayOfWeek(dateString);
        Calendar cal = getCalendar(dateString);
        float timeOfDay = timeOfDay(timeString);
        float time = offset ? (int)(cal.getTimeInMillis() / 1000.0) + timeOfDay : timeOfDay;
        return new float[]{cal.get(Calendar.DAY_OF_WEEK), time};
    }

    public static String concantTokens(String[] tokens){
        String line = "";
        for(int i = 0; i < tokens.length; i++){
            line = line.concat(tokens[i]);
            if(i < tokens.length-1){
                line = line.concat(", ");
            }
        }
        return line;
    }

}
