import java.io.*;
import java.sql.Time;
import java.text.ParseException;
import java.util.*;
import java.io.FileReader;

import jdk.nashorn.internal.runtime.ParserException;
import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;

/**
 * Created by Haylem on 5/12/2017.
 */
public class CSVReader {

    /* Default Format
     *
     * 0: Audit_Time,           10: Duration,
     * 1: Audit_Action,         11: Break_Duration,
     * 2: time_segment_key,     12: Approved,
     * 3: myemployee_key,       13: is_deleted,
     * 4: time_date,            14: myPay_Key,
     * 5: Company,              15: Time_Requires_Approval,
     * 6: Work,                 16: Week_Starts_On,
     * 7: Work_Category,        17: Pay_Period_Start,
     * 8: Job,                  18: Pay_Period_End,
     * 9: Start_Time,           19: Pay_Frequency,
     * 10: End_Time,            20: Pay_Created_Date
     *
     * Added Features
     *
     * 21: Start Day
     * 22: Start Time #In seconds#
     */
    BufferedReader reader;
    Map<Integer, String> idToTransaction;
    String fileName;
    String originalFileName;
    String extendedFilName;

    int lineCount = 0;
    int numLines = -1;
    int maxKey = -1;

    public CSVReader(BufferedReader reader, String fileName) {
        this.reader = reader;
        this.originalFileName = fileName;
        this.fileName = String.valueOf(fileName.subSequence(0, fileName.length() - 4));
        // removes .csv
        idToTransaction = new HashMap<>();
    }

    public void executeParser(Parser parser) throws ParserException {
        int lineNum = 0;
        int printedNum = 0;
        try {
            String line = reader.readLine();
            while(line != null){
                lineNum++;
                if(line.trim().length() <= 0){
                    continue;
                }
                printedNum++;
                if(printedNum == 1){
                    parser.createHeader(line.split(","));
                } else {
                    parser.readLine(line, lineNum);
                }
                line = reader.readLine();
            }
            parser.close();
            System.out.println("Done applying parser");
        } catch(Exception e){
            System.out.println(e);
        }
    }

    private void calculateMax(int keyPlacement) {
        try {

            BufferedReader scanner = new BufferedReader(new FileReader(originalFileName));

            String line = scanner.readLine();

            while (line != null) {
                if (numLines % 10000 == 0) {
                    System.out.println("Line: " + numLines);
                    int key = -1;
                    try {
                        key = Integer.valueOf(line.split(",")[keyPlacement]);
                    } catch (NumberFormatException e) {
                        System.out.println("Did not find key at index: " + keyPlacement + " on line: " + numLines);
                        line = scanner.readLine();
                        continue;
                    }
                    if (key > maxKey) {
                        maxKey = key;
                    }
                }
                line = scanner.readLine();
                numLines++;
            }
            System.out.println("Num Lines: " + numLines);
            System.out.println("Max Key: " + maxKey);

        } catch (IOException e) {
            System.out.println("Failed to read file: " + originalFileName);
            System.out.println(e);
        }
    }

    public void parse() {

    }

    public void binaryClassificationParse(int[] featuresToCheck) {

    }

    public String modificationHisto(String trainFileName) {
        String file = String.valueOf(trainFileName.subSequence(0, trainFileName.length() - 4));
        String trainTo = file + "_time_dif.csv";

        /*if(numLines < 0 && maxKey < 0) {
            calculateMax(2);
        }*/

        //String[] segmentArray = new String[maxKey*2];
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(trainTo));
            BufferedReader reader = new BufferedReader(new FileReader(trainFileName));
            try {
                Map<Integer, Long> dates = new HashMap<>();

                String line = reader.readLine();
                line = reader.readLine();

                int line_num = 0;

                while (line != null) {
                    String[] tokens = line.split(",");
                    // if in map
                    // check date
                    // else
                    // put id and date in map
                    int seg_id = Integer.parseInt(tokens[2]);
                    String date_string = tokens[0];
                    long time = get_time_date(date_string);
                    if (dates.containsKey(seg_id)) {
                        long time_dif = Math.abs(time - dates.get(seg_id));
                        writer.write(String.valueOf(seg_id + " " + time_dif));
                        writer.write("\n");
                        writer.flush();
                    }
                    dates.put(seg_id, time);

                    if (line_num % 100000 == 0) {
                        System.out.println(line_num);
                    }
                    line_num++;
                    line = reader.readLine();
                }
            } catch (Exception e) {
                System.out.println(e);
                writer.close();
                reader.close();
            }
        } catch (IOException e) {
            System.out.println("Error reading file" + e);
        }
        return trainTo;
    }

    /**
     * Classify an existing instance according to a new given instance. I.e look for differences
     * between the new instance and the existing one.
     *
     * @param writer        used to output target classification
     * @param instance
     * @param existingEntry
     * @param multiple      when true, each feature is compared and output as the targer, written to the writer
     * @param binary        when true, each feature is classified as either similar or not, when false the difference is written
     */
    private void classifyInstance(BufferedWriter writer, String instance, String existingEntry, boolean multiple, boolean binary) {
        try {
            String[] tokens = instance.split(",");
            String[] existingTokens = instance.split(",");

            //System.out.println("1) "+instance);
            //System.out.println("2)" +existingEntry);

            boolean invariant = true;
            //if (multiple) {
            for (int i = 3; i < tokens.length && i < existingTokens.length; i++) {
                double feature = Double.valueOf(tokens[i]);
                double existingFeature = Double.valueOf(existingTokens[i]);

                String newFeature = "null";
                if (binary) {
                    newFeature = (feature != existingFeature) ? "1" : "0";
                    if (newFeature.equalsIgnoreCase("1")) {
                        invariant = false;
                    }
                } else {
                    newFeature = String.valueOf(existingFeature - feature);
                    invariant = false;
                }
                existingEntry = existingEntry.concat(", " + newFeature);
            }

            //if (!invariant) {
            writer.write(existingEntry);
            //System.out.println(existingEntry);
            //}

            writer.flush();
            //}

            // for single output classification
            /*else {
                // timeSegment already exists.
                writer.write(timeSegments.get(timeSegmentKey) + ",wrong");
                //writer.write(segmentArray[timeSegmentKey] + ",wrong");
                printed = true;
                //System.out.println(line);
            }*/
        } catch (IOException e) {
            System.out.println("Error compairing instances: ");
            System.out.println("Existing: " + existingEntry);
            System.out.println("Novel instance: " + instance);
        }
    }

    public void checkMemory(long thresh, String[] tokens, Map<Integer, String> timeSegments,
                            long flush_time, BufferedWriter writer, Set<Integer> classified,
                            int keyIndex, boolean keepFeatures) {
        //System.out.println("Clearing memory");
        int prev_size = timeSegments.size();
        long current_time = get_time_date(tokens[0]);
        clear_time_segs(timeSegments, writer, flush_time, thresh, classified, keyIndex, keepFeatures);
        flush_time = current_time;
        //System.out.println("Cleared: "+(timeSegments.size()-prev_size)+" from memory");
    }

    public boolean compare_string(String s_one, String s_two) {
        try {
            long l_one = Long.parseLong(s_one);
            long l_two = Long.parseLong(s_two);

            return l_one == l_two;
        } catch (NumberFormatException e) {
            return s_one.equalsIgnoreCase(s_two);
        }
    }

    public String createTrainSet2(String trainFileName, List<Integer> to_compare, List<Integer> to_check, int time_index, int key_index, boolean header) {
        long time_thresh = (long) 2e10;

        String file = String.valueOf(trainFileName.subSequence(0, trainFileName.length() - 4));
        String trainTo = file + "_classified.csv";

        if (numLines < 0 && maxKey < 0) {
            calculateMax(key_index);
        }

        try {
            Map<Integer, String> time_segments = new HashMap<Integer, String>();
            Set<Integer> classified = new HashSet<Integer>();

            BufferedWriter writer = new BufferedWriter(new FileWriter(trainTo));

            reader = new BufferedReader(new FileReader(trainFileName));

            if (header) {
                writer.write(reader.readLine() + ", time_dif");
                writer.newLine();

            } else {
                reader.readLine();
            }
            String line = reader.readLine();

            long flush_time = 0;
            Set<Integer> classified_set = new HashSet<>();
            while (line != null) {

                String[] tokens = line.split(",");
                int transaction_id = Integer.parseInt(tokens[key_index]);
                String existing_string = time_segments.get(transaction_id);


                if (existing_string == null) {
                    time_segments.put(transaction_id, line);
                } else {

                    String[] existing_tokens = existing_string.split(",");
                    if (classified_set.contains(transaction_id) == false && line.trim().length() > 0) {
                        long new_time = get_time_date(tokens[time_index]);
                        long existing_time = get_time_date(existing_tokens[time_index]);
                        long time = new_time - flush_time;


                        if (time > time_thresh) {
                            /*clear_time_segs(time_segments, writer, time_thresh, flush_time,
                                    classified_set, to_compare.get(0), true);*/

                            time_segments.remove(transaction_id);
                            flush_time = new_time;
                        } else {

                            boolean is_valid = false; // is this transaction a valid one
                            // according to features at to_check
                            String to_print = existing_string;

                            for (Integer index : to_compare) {
                                String to_append = (compare_string(tokens[index], existing_tokens[index]) == false) ? "1" : "0";
                                to_append = to_compare.indexOf(index) >= to_compare.size() - 2 ? to_append : to_append.concat(",");

                                to_print = to_print.concat(to_append);
                                if (to_check.contains(index) && (compare_string(tokens[index], existing_tokens[index]) == false)) {
                                    is_valid = true;
                                }
                            }

                            to_print = to_print.concat(String.valueOf(new_time - existing_time));

                            if (!is_valid) {
                                time_segments.put(transaction_id, line);
                            } else {
                                classified_set.add(transaction_id);
                                time_segments.remove(transaction_id);
                            }

                            writer.write(to_print + "\n");
                            writer.flush();
                        }

                    }
                }
                if (lineCount % (numLines / 100) == 0) {
                    System.out.println(lineCount / (numLines / 100) + "%");
                }
                lineCount++;

                line = reader.readLine();
            }
        } catch (IOException e) {

        }

        return trainTo;
    }

    /**
     * Classifies a set assuming the following features.
     * An instance is classified as 'wrong' when there is
     * a following transaction on the same time segment key.
     * 'correct' when there are no following transactions.
     * <p>
     * 0: Audit_Action
     * 1: time_segment_key
     * 2: myemployee_key
     * 3: Duration
     * 4: start_day
     * 5: start_time
     *
     * @return
     */
    public String createTrainSet(String trainFileName, boolean header, boolean multiple, boolean binary, boolean target, boolean time, int key_placement) {
        String file = String.valueOf(trainFileName.subSequence(0, trainFileName.length() - 4));
        String trainTo = file + "_classified.csv";

        if (numLines < 0 && maxKey < 0) {
            calculateMax(key_placement);
        }

        //String[] segmentArray = new String[maxKey*2];

        try {
            Map<Integer, String> timeSegments = new HashMap<Integer, String>();
            Set<Integer> classified = new HashSet<Integer>();

            BufferedWriter writer = new BufferedWriter(new FileWriter(trainTo));

            reader = new BufferedReader(new FileReader(trainFileName));
            if (header) {
                writer.write(reader.readLine() + ", time_dif");
                writer.newLine();

            } else {
                reader.readLine();
            }
            String line = reader.readLine();

            long flush_time = 0;
            lineCount = 0;
            long thresh = (long) 2e10;
            while (line != null) {
                String[] tokens = null;

                boolean printed = false;
                if (line.trim().length() > 0) {
                    tokens = line.split(",");
                    int timeSegmentKey = Integer.parseInt(tokens[key_placement]);
                    //System.out.println(line);
                    long line_time = get_time_date(tokens[0]);

                    if (Math.abs(line_time - flush_time) > (long) 4e10) {
                        //System.out.println("Long on line: "+lineCount);
                        checkMemory(thresh, tokens, timeSegments, flush_time, writer, classified, key_placement + 2, !target);
                        flush_time = line_time;
                    }

                    if (timeSegments.keySet().contains(timeSegmentKey)) {
                        String existingEntry = "";
                        String[] existingTokens = null;
                        try {

                            existingEntry = timeSegments.get(timeSegmentKey);

                            existingTokens = existingEntry.split(",");
                        } catch (OutOfMemoryError e) {
                            System.out.println("Line: " + line);
                            System.out.println("Line Count: " + lineCount);
                            System.out.println("Memory: " + timeSegments.size());
                            System.out.println(e);

                            if (tokens != null) {

                            }
                        }

                        if (tokens[1].equalsIgnoreCase("delete")) {
                            timeSegments.remove(timeSegmentKey);
                            continue;
                        }

                        if (target) {
                            existingEntry = existingTokens[0] + "," + existingTokens[1] + "," + existingTokens[2] + "," + existingTokens[3];
                        }
                        // check entries of each element
                        boolean invariant = true;
                        // for removing elements that are unchanged

                        String[] newTokens = new String[tokens.length];

                        if (multiple) {
                            for (int i = 0; i < tokens.length && i < existingTokens.length; i++) {

                                newTokens[i] = tokens[i];

                                if (i >= key_placement + 2) {


                                    String feature = tokens[i];
                                    String existingFeature = existingTokens[i];
                                    String newFeature = "null";


                                    try {
                                        double feature_double = Double.valueOf(tokens[i]);
                                        double existingFeature_double = Double.valueOf(existingTokens[i]);

                                        if (binary) {
                                            newFeature = (feature_double != existingFeature_double) ? "1" : "0";
                                            if (newFeature.equalsIgnoreCase("1")) {
                                                invariant = false;
                                            }
                                        } else {
                                            newFeature = String.valueOf(existingFeature_double - feature_double);
                                            invariant = false;
                                        }
                                    } catch (NumberFormatException e) {
                                        //NOTE: This is just for binary, multiple target classification
                                        newFeature = (!feature.equals(existingFeature) ? "1" : "0");
                                        if (newFeature.equalsIgnoreCase("1")) {
                                            invariant = false;
                                        }
                                    }
                                    existingEntry = existingEntry.concat(", " + newFeature);
                                }
                            }

                            long existing_time = get_time_date(existingTokens[0]);

                            existingEntry = existingEntry.concat("," + String.valueOf(Math.abs(existing_time - line_time)));

                            if (Math.abs(line_time - flush_time) > 1e10) {
                                //clear map
                            }

                            if (!invariant) {
                                writer.write(existingEntry);
                                printed = true;


                            }
                        } else {
                            writer.write(timeSegments.get(timeSegmentKey) + ",wrong");
                            printed = true;
                        }

                        if (printed) {
                            writer.newLine();
                            writer.flush();
                        }

                        line = joinLine(existingTokens);
                        timeSegments.put(timeSegmentKey, line);
                    } else if (line.split(",")[1].equalsIgnoreCase("ADD")) {
                        timeSegments.put(timeSegmentKey, line);
                    }
                }
                line = reader.readLine();

                if (lineCount % (numLines / 100) == 0) {
                    System.out.println(lineCount / (numLines / 100) + "%");
                }
                lineCount++;
            }

            System.out.println("Classifying unchanged instances");
            int instances = timeSegments.keySet().size();
            //int instances = segmentArray.length;
            lineCount = 0;
            // all segments remaining are correct
            for (int timeSegmentKey : timeSegments.keySet()) {
                //for(int i = 0; i < instances; i++){
                String existingLine = timeSegments.get(timeSegmentKey);
                String[] existingTokens = existingLine.split(",");
                /*if(segmentArray[i] == null){
                    continue;
                }
                String[] existingTokens = segmentArray[i].split(",");*/
                String existingEntry = existingTokens[0] + "," + existingTokens[1] + "," + existingTokens[2];
                if (!target) {
                    existingEntry = existingLine;
                }

                for (int j = 4; j < existingTokens.length && j < existingTokens.length; j++) {
                    existingEntry = existingEntry.concat(", 0");
                }

                writer.newLine();
                writer.flush();

                if (lineCount % (maxKey / 100) == 0) {
                    System.out.println(lineCount / (numLines / 100) + "%");
                }

                lineCount++;
            }

            writer.close();
            reader.close();
        } catch (IOException e) {

        }
        return trainTo;
    }

    private String joinLine(String[] line) {
        String lineString = line[0];
        for (int i = 1; i < line.length; i++) {
            lineString = lineString.concat("," + line[i]);
        }
        return lineString;
    }

    private void clear_time_segs(Map<Integer, String> segments, BufferedWriter writer, long flush_time,
                                 long time_thresh, Set<Integer> classified, int compareStart, boolean keepFeatures) {
        for (int key : segments.keySet()) {
            String entry = segments.get(key);
            long seg_time = get_time_date(entry.split(",")[0]);
            if (Math.abs(seg_time - flush_time) >= time_thresh) {
                // remove, count as unmodified
                String[] tokens = entry.split(",");
                String to_write = tokens[0] + "," + tokens[1] + "," + tokens[2] + "," + tokens[3];
                if (keepFeatures) {
                    to_write = entry;
                }
                for (int i = compareStart; i < tokens.length; i++) {
                    to_write = to_write.concat(",0");
                }
                to_write = to_write.concat(",0");
                try {
                    writer.write(to_write + "\n");
                    writer.flush();
                } catch (IOException e) {
                    System.out.println(e);
                }
                classified.add(key);
            }
        }

        for (Integer key : classified) {
            segments.remove(key);
        }
    }

    public long get_time_date(String line_date) {
        if (line_date.equals("NULL")) {
            return 0;
        }
        String seg_date = line_date.split(" ")[0];
        String seg_time = line_date.split(" ")[1];

        int year_int = Integer.parseInt(seg_date.split("-")[0]);
        int month_int = Integer.parseInt(seg_date.split("-")[2]);
        int date_int = Integer.parseInt(seg_date.split("-")[1]);

        Calendar cal = Calendar.getInstance();
        cal.set(year_int, month_int, date_int);

        // convert 1-7 date into cyclic representation.

        int startHour = Integer.valueOf(seg_time.split(":")[0]);
        int startMinute = Integer.valueOf(seg_time.split(":")[1]);
        float startSeconds = Float.valueOf(seg_time.split(":")[2]);

        int startTimeSeconds = startHour * 12 * 60 + startMinute * 60 + (int) startSeconds;

        return cal.getTimeInMillis() + startTimeSeconds;
    }

    public String addedFeatures(String line) {
        line = line.trim();
        try {
            String[] splitLine = splitLine(line);
            String startTimeString = splitLine[9];
            if (startTimeString.equals("NULL")) {
                return null;
            }
            String startDate = startTimeString.split(" ")[0];
            String startTime = startTimeString.split(" ")[1];

            return "," + String.valueOf(getStartDate(startDate)) + "," + String.valueOf(getStartTime(startTime));
        } catch (Exception e) {
            System.out.println("Exception at: " + line);
            System.out.println(e);
        }
        return null;
    }

    public float getStartDate(String startDate) {
        int yearInt = Integer.parseInt(startDate.split("-")[0]);
        int monthInt = Integer.parseInt(startDate.split("-")[2]);
        int dateInt = Integer.parseInt(startDate.split("-")[1]);

        Calendar cal = Calendar.getInstance();
        cal.set(yearInt, monthInt, dateInt);

        int day = cal.get(Calendar.DAY_OF_WEEK);

        return (float) Math.sin(((2.0 * Math.PI) / (6.0)) * ((float) day - 1.0));
    }

    public float getStartTime(String startTime) {
        int startHour = Integer.valueOf(startTime.split(":")[0]);
        int startMinute = Integer.valueOf(startTime.split(":")[1]);
        float startSeconds = Float.valueOf(startTime.split(":")[2]);

        int startTimeSeconds = startHour * 12 * 60  + startMinute * 60 + (int) startSeconds;
        // start time in seconds
        float secondsInDay = 24 * 12 * 60;

        return (float) Math.sin(((2.0 * Math.PI) / (secondsInDay)) * ((float) startTimeSeconds));
    }

    public String createAddedFeatures() {
        String filToWrite = this.fileName + "_extended.csv";

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filToWrite));
            String line = reader.readLine();
            line = reader.readLine();
            while (line != null) {
                if (line.trim().length() > 0) {
                    String addedFeatures = addedFeatures(line);
                    writer.write(line);
                    writer.write(addedFeatures);
                    writer.newLine();
                    writer.flush();
                }
                line = reader.readLine();
            }
        } catch (IOException e) {
            System.out.println(e);
        }
        return filToWrite;
    }

    /**
     * Constricts data set with addedFeatures:
     * <p>
     * 0: Audit_Action
     * 1: time_segment_key
     * 2: myemployee_key
     * 3: Duration
     * 4: start_day
     * 5: start_time
     *
     * @return
     */
    public String createNewDataSet(List<Integer> to_keep) {
        String filToWrite = this.fileName + "_contracted.csv";
        calculateMax(2);
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filToWrite));

            String line = reader.readLine();
            if (!to_keep.isEmpty()) {
                writer.write(line + ",periodic_date, periodic_time");
            } else {
                writer.write("Audit_Action,time_segment_key,myemployee_key,Duration,start_day,start_time");
            }

            writer.newLine();
            writer.flush();
            lineCount = 0;
            while (line != null) {
                try {
                    //System.out.println("READING:");
                    lineCount++;

                    if (line.trim().length() > 0) {
                        String[] tokens = splitLine(line);
                        String addedFeatureString = addedFeatures(line);
                        if (addedFeatureString == null) {
                            line = reader.readLine();
                            continue;
                        }
                        String[] addedFeatures = addedFeatureString.split(",");

                        String[] toSeperate;

                        if (!to_keep.isEmpty()) {
                            toSeperate = new String[to_keep.size() + 2];
                            for (int i = 0; i < tokens.length; i++) {
                                if (to_keep.contains(i)) {
                                    toSeperate[i] = tokens[i];
                                }
                            }
                            toSeperate[toSeperate.length - 2] = addedFeatures[1];
                            toSeperate[toSeperate.length - 1] = addedFeatures[2];
                        } else {
                            toSeperate = new String[]{tokens[1], tokens[2], tokens[3], tokens[11], addedFeatures[1], addedFeatures[2]};
                        }

                        //String[] toSeperate = new String[]{tokens[2],tokens[3],tokens[11], addedFeatures[1],addedFeatures[2]};
                        // the features which will be kept in the new data set
                        writer.write(seperateColumns(toSeperate));
                        writer.newLine();
                        writer.flush();
                    }

                    if (lineCount % (numLines / 100) == 0) {
                        System.out.println(lineCount / (numLines / 100) + "%");
                    }

                } catch (Exception e) {
                    System.out.println("Error on line: " + line);
                    System.out.println(line);
                    System.out.println(e);
                }
                line = reader.readLine();
            }
        } catch (IOException e) {
            System.out.println(e);
        }
        return extendedFilName;
    }

    public String seperateColumns(String[] tokens) {
        String seperated = "";
        for (int i = 0; i < tokens.length - 1; i++) {
            seperated = seperated.concat(tokens[i] + ",");
        }
        return seperated.concat(tokens[tokens.length - 1]);
    }

    /**
     * Parse a CSV line
     *
     * @param line
     */
    public String[] splitLine(String line) {
        return line.split(",");
    }

}
