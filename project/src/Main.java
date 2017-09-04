import jdk.nashorn.internal.runtime.ParserException;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        new Main();
    }

    Scanner userInput;
    String fileName = "";
    File file;
    SizeChecker checker;

    public Main(){
        userInput = new Scanner(System.in); // attach system console

        String today = "2017-08-26 10:46:00";
        float[] result = Utility.parseDate(today, true);
        System.out.println(result[0]+" "+result[1]);

        System.out.print("Custom file? ");
        boolean customFile = askBoolean();
        if(customFile){
            System.out.println("Enter file of type .csv");
            fileName = askStringExtension("csv");
        } else {
            fileName = "companyVDV_small.csv";
            //fileName = "test.txt";
        }

        BufferedReader scanner = loadFile(fileName);
        checker = new SizeChecker(new File(fileName));

        while(userInput.hasNext()){
            if(!parseInput(scanner)){
                break;
            }
        }
    }

    /**
     * Parse input commands from user
     *
     * @param scanner BufferedReader attatched to CSV file
     * @return
     */
    public boolean parseInput(BufferedReader scanner){
        String token = askString();
        if(token.equalsIgnoreCase("summary")){
            printAll(scanner);
        } else if(token.equalsIgnoreCase("c")) {
            return false;
        } else if(token.equalsIgnoreCase("parse")) {
            parseFile(scanner);
        } else if(token.equalsIgnoreCase("classify")) {
            classifyFile(scanner);
        } else if(token.equalsIgnoreCase("time")){
            timeFile(scanner);
        } else if(token.equalsIgnoreCase("dim")){
            dim(scanner);
        } else if(token.equalsIgnoreCase("split")){
            split(scanner);
        } else if(token.equalsIgnoreCase("extract")){
            extract(scanner);
        }  else if(token.equalsIgnoreCase("trim")){
            trim(scanner);
        } else if(token.equalsIgnoreCase("count")){
            count(scanner);
        } else if(token.equalsIgnoreCase("extractCondition")) {
            extractCondition(scanner);
        } else if(token.equalsIgnoreCase("parseClassify")){
            parseClassify(scanner);
        } else{
            System.out.println("Command not recognized: "+token);
        }
        return true;
    }

    public void parseClassify(BufferedReader reader){
        int target = askNumbers("Index of id").get(0);
        System.out.println("File to print to");
        String fileToPrint = askString();
        List<Integer> toCheck = askNumbers("Indicies to check");
        Parser parser = new ClassifierParser(fileToPrint, checker, target, toCheck);
        CSVReader csvReader = new CSVReader(reader, fileToPrint);
        try{
            csvReader.executeParser(parser);
        } catch(ParserException e) {
            System.out.println("Error executing classifier parser: "+e);
        }
    }

    public void extractCondition(BufferedReader reader){
        int target = askNumbers("Index to select from").get(0);
        System.out.println("Required value for index: "+target);
        String condition = askString();
        System.out.println("File to print to (csv)");
        String fileToPrint = askString();
        Parser parser = new ConditionExtractParser(fileToPrint, checker, target, condition);
        CSVReader csvReader = new CSVReader(reader, fileToPrint);
        try{
            csvReader.executeParser(parser);
        } catch(ParserException e){
            System.out.println("Error executing extract condition parser: "+e);
        }
    }

    public void count(BufferedReader reader){
        System.out.println("File to print to (csv)");
        String fileToPrint = askString();
        int targetToCount = askNumbers("Index of coloumn to count?").get(0);
        Parser countParser = new CountCompanyParser(fileToPrint, checker, targetToCount);
        CSVReader csvReader = new CSVReader(reader, fileToPrint);
        try{
            csvReader.executeParser(countParser);
        } catch(ParserException e){
            System.out.println("Error executing count parser: "+e);
        }
    }

    public void trim(BufferedReader reader){
        Parser trimParser = new TrimParser(fileName, checker);
        System.out.println("File to print to (csv)");
        String fileToPrint = askString();
        CSVReader csvReader = new CSVReader(reader, fileToPrint);
        try{
            csvReader.executeParser(trimParser);
        } catch(ParserException e){
            System.out.println(e);
        }
    }

    public void extract(BufferedReader reader){
        List<Integer> targets = askNumbers("Enter feature indicies to extract.");
        Parser extractParser;
        System.out.println("Parse date/time features?");
        if(askBoolean()){
            List<Integer> dateTimeFeatures = askNumbers("Features must be contained in previous set of numbers");
            System.out.println("Add offset of date?");
            boolean offset = askBoolean();
            extractParser = new ExtractParser("testExtract1.csv", checker,targets, dateTimeFeatures, offset);
        } else {
            extractParser = new ExtractParser("testExtract1.csv",checker, targets);
        }

        CSVReader csvReader = new CSVReader(reader, "file1.txt");
        try {
            csvReader.executeParser(extractParser);
        } catch(ParserException e){
            System.out.println(e);
        }
    }

    public void split(BufferedReader reader){
        List<Integer> cols = askNumbers("Enter coloumn indicies to remove, zero indexed");
        String toPrint = "split.csv";
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(toPrint));
            String line = reader.readLine();
            int row = 0;
            while (line != null) {
                String[] tokens = line.split(",");

                if(line.trim().length() < 0 || tokens.length < cols.size()){
                    line = reader.readLine();
                    continue;
                }

                String to_write = "";
                int matching = 0;

                for(int i = 0; i < tokens.length; i++){
                    if(cols.contains(i)){
                        matching ++;
                        to_write = to_write.concat(tokens[i]);
                        if(i < tokens.length-1 && cols.indexOf(i) < cols.size()-1){
                            to_write = to_write.concat(",");
                        }
                    }
                }
                if(matching == cols.size()) {
                    writer.write(to_write+"\n");
                }
                line = reader.readLine();

                if(row % 100000 == 0){
                    System.out.println("row: "+row);
                }
                row++;
            }
        } catch(IOException e){
            System.out.println(e);
        }

        System.out.println("Printed to: "+toPrint);
    }

    public void dim(BufferedReader scanner){
        try {
            String line = scanner.readLine();
            String oldLine = line;
            int cols = line.split(",").length;
            int row = 1;
            while (line != null && line.trim().length()> 0) {

                if(Math.abs(row - 12088) < 5){
                    System.out.println("Line: "+line);
                }

                oldLine = line;
                line = scanner.readLine();
                int current_cols = -1;
                try {
                    current_cols = line.split(",").length;
                }catch(Exception e){
                    System.out.println("Line: "+line+":");
                    System.out.println(row);
                    return;
                }
                if (current_cols != cols) {
                    System.out.println("Miss step, row: " + row + " expected: " + cols + " found: " + current_cols);
                    System.out.println(oldLine);
                    System.out.println(line);
                }

                cols = current_cols;
                row++;

            }
            System.out.println("Cols: " + cols + " Rows: " + row);
        } catch(IOException e){
            System.out.println(e);
        }
    }

    public void timeFile(BufferedReader scanner){
        CSVReader csvReader = new CSVReader(scanner, fileName);
        System.out.println("Running time difference calculation");

        System.out.println("Done, printed to: "+csvReader.modificationHisto(fileName));
    }

    public void classifyFile(BufferedReader scanner){
        CSVReader csvReader = new CSVReader(scanner, fileName);
        String trainFileName = fileName;//"companyVDV_small_contracted.csv";

/*
        System.out.println("Classify with multiple target features?");
        boolean multiple = askBoolean();
        boolean target = false;
        if(multiple){
            System.out.println("Print only target features?");
            target = askBoolean();
        }
        System.out.println("Classify with binary target feature[s]?");
        boolean binary = askBoolean();*/

        System.out.println("Index of key?");
        int key_placement = Integer.parseInt(askString());
        key_placement=2;

        System.out.println("Classifying file: "+trainFileName);
        //String fileTo = csvReader.createTrainSet(trainFileName, multiple, binary, target, key_placement);
        String fileTo = csvReader.createTrainSet(trainFileName, false,true, true,false, true, key_placement);
        List<Integer> to_compare = Arrays.asList(5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23);
        List<Integer> to_check = Arrays.asList(13,15);
        //String fileTo = csvReader.createTrainSet2(trainFileName, to_compare, to_check, 0, key_placement, true);
        System.out.println("Done classifying, printed to: "+fileTo);
        // call classify.
    }

    public void parseFile(BufferedReader scanner){
        CSVReader csvReader = new CSVReader(scanner, fileName);
        System.out.println("Adding features to default csv file: "+fileName);

        System.out.println("Keep all features?");
        List<Integer> to_keep = new ArrayList<>();
        if(!askBoolean()){
            to_keep = askNumbers("Enter features to keep (starts at 0)");
        } else {
            for(int i = 0; i < 50; i++){
                to_keep.add(i);
            }
        }

        String extendedFileName = csvReader.createNewDataSet(to_keep);
        System.out.println("Written altered data set to: "+ extendedFileName);
    }



    public void printAll(BufferedReader scanner){
        try {
            System.out.println("Printing file: " + fileName);
            String line = scanner.readLine();
            while (line != null) {
                System.out.println(line);
                line = scanner.readLine();
            }
            System.out.println("Done printing: " + fileName);
        } catch (IOException e){
            System.out.println(e);
        }
    }

    public BufferedReader loadFile(String fileName){
        try{
            System.out.println("Loading file: "+fileName);
            BufferedReader scanner = new BufferedReader(new FileReader(fileName));
            System.out.println("Successfully loaded file: "+fileName);
            return scanner;
        } catch(IOException e){
            System.out.println(e);
        }
        System.out.println("Error reading: "+fileName);
        return null;
    }

    public String askStringExtension(String extension){
        String token = askString();
        while(!token.contains(extension) && !token.equalsIgnoreCase("c")){
            System.out.println("Invalid file type: "+token+" expected extension: "+extension);
            System.out.println("Re-enter correct file type  or 'c' to cancel");
            token = askString();
        }

        if(token.equalsIgnoreCase("c")){
            return null;
        } else {
            return token;
        }
    }

    public List<Integer> askNumbers(String prompt){
        boolean success = false;
        List<Integer> nums = new ArrayList<Integer>();

        while(success == false) {
            System.out.println(prompt);
            String line = askString();
            nums.clear();
            try {
                String[] tokens = line.split(" ");
                for (int i = 0; i < tokens.length; i++) {
                    nums.add(Integer.parseInt(tokens[i]));
                }
            } catch(NumberFormatException e){
                System.out.println("Please enter integers separated by spaces only.");
                continue;
            }
            success = true;
        }
        return nums;
    }

    public String askString(){
        if(userInput == null){
            System.out.println("No console input!");
            return null;
        } else {
            return userInput.nextLine();
        }
    }

    public boolean askBoolean(){
        System.out.println("Y/N");
        String token = askString();
        while(true){
            if(token.equalsIgnoreCase("true") || token.equalsIgnoreCase("t") || token.equalsIgnoreCase("y")){
                return true;
            }

            if(token.equalsIgnoreCase("false") || token.equalsIgnoreCase("f") || token.equalsIgnoreCase("n")){
                return false;
            }

            System.out.println("Invalid response: "+token+" Enter (y/n), (t/f), (true/false)");

            token = System.console().readLine();
        }
    }
}
