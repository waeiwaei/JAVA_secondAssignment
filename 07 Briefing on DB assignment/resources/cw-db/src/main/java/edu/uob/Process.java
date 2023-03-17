package edu.uob;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public class Process {

    //ArrayList<String> attributeList;
    static String database;
    static String fileSeparator = File.separator;
    static String path = (".."+fileSeparator+"cw-db"+fileSeparator+"databases"+fileSeparator);
    DBCmd dbcmd;

    public Process(DBCmd dbstate){
        this.dbcmd = dbstate;
    }

    public String query() throws Exception {
        String result = "";

        switch(dbcmd.commandtype.toLowerCase()){
            case "create table":
                result = createTableCMD();
                break;

            case "create database":
                result = createDatabaseCMD();
                break;

            case "update":
                result = updateCMD();
                break;

            case "select":
                result = selectCMD();
                break;

            case "alter":
                result = alterCMD();
                break;

            case "insert":
                result = insertCMD();
                break;

            case "join":
                result = joinCMD();
                break;

            case "drop table":
                result = dropTableCMD();
                break;

            case "drop database":
                result = dropDatabaseCMD();
                break;

            case "use":
                result = useCMD();
                break;

            case "delete":
                result = deleteCMD();
                break;

            default:
                throw new Exception("Invalid command typed :" + dbcmd.commandtype);
        }


        return result;
    }

    private String createTableCMD() throws Exception {

        //check table name with keyword - FROM, JOIN, DELETE, DROP, USE, SELECT, UPDATE, ALTER
        String filePath = path+database+fileSeparator+dbcmd.TableNames.get(0)+".tab";
        File f = new File(filePath);
        FileWriter fl = new FileWriter(f);
        BufferedWriter bw = new BufferedWriter(fl);

        if(dbcmd.colNames.size() == 0){

            if(!f.exists()){
                boolean create = f.createNewFile();
                if(create){
                    System.out.println("Table created");
                }else{
                    System.out.println("Table not created");
                }
            }else if (f.exists()){
                return "[Error - Table already exists]";
            }

        }else{

            bw.write("id");
            bw.write("\t");

            for(int i = 0; i < dbcmd.colNames.size(); i++){
                bw.write(dbcmd.colNames.get(i));
                bw.write("\t");
            }

            bw.close();
        }

        return "[OK]";
    }

    private String createDatabaseCMD() throws Exception {

        String folderPath = path+dbcmd.DBName;
        File folder = new File(folderPath);

        if(!folder.exists()){
            boolean create = folder.mkdirs();
            if(create){
                System.out.println("Database created");
            }else{
                System.out.println("Database not created");
            }

        }else if(folder.exists() && folder.isDirectory()){
            return "[ERROR - Database already exists]";
        }

        return "[OK]";
    }

    private String updateCMD() throws Exception{

        String filePath = path+database+fileSeparator+dbcmd.TableNames.get(0) + ".tab";
        File f = new File(filePath);
        FileInputStream fl = new FileInputStream(f);
        BufferedReader br = new BufferedReader(new InputStreamReader(fl));

        //extract table attribute values
        String attributes[] = br.readLine().split("\t");
        ArrayList<String> attributeList = new ArrayList<String>(Arrays.asList(attributes));

        ArrayList<String>entries = new ArrayList<String>();

        //extract all the entries
        String x = br.readLine();
        while(x != null){
            entries.add(x);
            x = br.readLine();
        }

        ArrayList<Integer> tracker = new ArrayList<Integer>();

        for(int i = 0; i < entries.size(); i++){
            if(conditionProcessList(dbcmd, entries.get(i),attributeList)){
                tracker.add(i);
            }
        }

        //get each individual entry within a 2D array
        String [][] updEntries = new String[entries.size()][attributeList.size()];
        int row = 0;
        int in = 0;


        while(in < entries.size() && entries.get(in) != null){

            String[] columnEntries = entries.get(in).split("\t");
            for(int col = 0; col <columnEntries.length; col++){
                updEntries[row][col] = columnEntries[col];
            }

            in++;
            row++;


        }

        //update the specific row index and column index
        ArrayList<String> updateName = new ArrayList<String>();
        ArrayList<String> updateVal = new ArrayList<String>();

        for(int i = 0; i < dbcmd.nameValueList.size(); i++){
            updateName.add(dbcmd.nameValueList.get(i).Name);
        }

        for(int i = 0; i < dbcmd.nameValueList.size(); i++){
            updateVal.add(dbcmd.nameValueList.get(i).Value);
        }

        ArrayList<Integer> indexCol = new ArrayList<Integer>();


        //get the column index to update
        for(int i = 0; i < updateName.size(); i++){
            indexCol.add(attributeList.indexOf(updateName.get(i)));
        }

        int i = 0;

        int rowChanges = tracker.size();
        int colChanges = indexCol.size();
        int valChanges = updateVal.size();

        //combine with rows found that match the condition
/*        while(counter != 0) {
            updEntries[tracker.get(i)][indexCol.get(i)] = updateVal.get(i);
            i++;
            counter--;
        }
        */

        for(int j = 0; j < colChanges; j++){
            for(int k = 0; k < rowChanges; k++){
                updEntries[tracker.get(k)][indexCol.get(j)] = updateVal.get(i);
            }
            i++;
        }

        //write back to the file and save
        //buffered writer
        f = new File(filePath);
        FileWriter fw = new FileWriter(f);
        BufferedWriter bw = new BufferedWriter(fw);

        for(int k = 0; k < attributeList.size(); k++){
            bw.write(attributeList.get(k));
            bw.write("\t");
        }

        bw.write("\n");

        //write entries back into the file
        for(int k = 0; k < entries.size(); k++){
            for(int y = 0; y < attributeList.size(); y++){
                bw.write(updEntries[k][y]);
                bw.write("\t");
            }
            bw.write("\n");
        }

        bw.close();

        return "[OK]";
    }

    private String selectCMD() throws Exception {

        ArrayList<String>  attributeList = new ArrayList<String>();

        //get the table path
        String filePath = path+database+fileSeparator+dbcmd.TableNames.get(0)+".tab";
        File f = new File(filePath);
        FileInputStream fl = new FileInputStream(f);
        BufferedReader br = new BufferedReader(new InputStreamReader(fl));

        //read line and store each column header in attributeList
        String attributes[] = br.readLine().split("\t");

        for(int i = 0; i < attributes.length; i++){
            attributeList.add(attributes[i]);
        }

        String result = "";


        //if no conditions in SELECT query
        if(dbcmd.conditions == null){

            if(dbcmd.colNames.get(0).equals("*")){
                result = String.join("\t", attributeList);
                String nextLine = br.readLine();
                while(nextLine!=null){
                    result+= "\n"+nextLine;
                    nextLine = br.readLine();
                }
            }else{
                Boolean arr[] = new Boolean[attributes.length];
                int i = 0;

                for(i = 0; i < dbcmd.colNames.size(); i++){
                    int j = 0;
                    for(j = 0; j < attributes.length; j++) {
                        if (dbcmd.colNames.get(i).equalsIgnoreCase(attributes[j])) {
                            arr[j] = true;
                            break;
                        }
                    }
                    if(j == attributes.length){
                        throw new Exception("Attribute " +dbcmd.colNames.get(i)+" not found");
                    }
                }

                for(int e = 0; e < dbcmd.colNames.size(); e++) {
                    result += dbcmd.colNames.get(e);
                    result+= "\t";
                }

                String nextLine = br.readLine();
                while(nextLine != null){
                    String arr1[] = nextLine.split("\t");
                    result+="\n";
                    for(int d = 0; d < arr.length;d++){
                        if(arr[d] != null) {
                            if (arr[d] == true) {
                                result += arr1[d] + "\t";
                            }
                        }
                    }

                    //result+=nextLine;
                    nextLine=br.readLine();
                }
            }
        }else{

            if(dbcmd.colNames.get(0).equals("*")){
                result = String.join("\t", attributeList);
                String nextLine = br.readLine();
                while(nextLine!=null){
                    if(conditionProcessList(dbcmd, nextLine, attributeList))
                    result+= "\n" + nextLine;
                    nextLine = br.readLine();
                }
            }else{
                Boolean arr[] = new Boolean[attributes.length];
                int i = 0;

                for(i = 0; i < dbcmd.colNames.size(); i++){
                    int j = 0;
                    for(j = 0; j < attributes.length; j++) {
                        if (dbcmd.colNames.get(i).equals(attributes[j])) {
                            arr[j] = true;
                            break;
                        }
                    }
                    if(j == attributes.length){
                        throw new Exception("Attribute " +dbcmd.colNames.get(i)+" not found");
                    }
                }

                for(int e = 0; e < dbcmd.colNames.size(); e++) {
                    result += dbcmd.colNames.get(e);
                    result+= "\t";
                }

                int trueCounter = 0;
                for(int d = 0; d < arr.length; d++){

                    if(arr[d].equals(true)){
                        trueCounter++;
                    }
                }

                String nextLine = br.readLine();
                while(nextLine != null){
                    String arr1[] = nextLine.split("\t");
                    result+="\n";
                    for(i = 0; i < arr.length;i++){
                        if(i < trueCounter) {
                            if (arr[i]) {
                                System.out.println("test");
                                if (!conditionProcessList(dbcmd, nextLine, attributeList)) {
                                    result += arr1[i] + "\t";
                                }
                            }
                        }else{break;}
                    }
                    //result+=nextLine;
                    nextLine=br.readLine();
                }
            }

        }

        return result;
    }

    private String alterCMD() throws Exception {

        String filePath = path + database + fileSeparator + dbcmd.TableNames.get(0) + ".tab";

        File f = new File(filePath);
        FileInputStream fl = new FileInputStream(f);
        BufferedReader br = new BufferedReader(new InputStreamReader(fl));

        ArrayList<String> attributeList = new ArrayList<String>();
        String attribute = br.readLine();
        String attlist[]  = attribute.split("\t");

        ArrayList<String>entries = new ArrayList<String>();

        while(attribute != null){
            attribute = br.readLine();
            entries.add(attribute);
        }


        for(int i = 0; i < attlist.length; i++){
            attributeList.add(attlist[i]);
        }
        br.close();


        File f1 = new File(filePath);
        if(!f1.exists()){
            throw new Exception("Error - table does not exist");
        }

        f1.delete();


        FileWriter fl1 = new FileWriter(f1);
        BufferedWriter bw = new BufferedWriter(fl1);

        //include column at the end - with the id attribute
        if(dbcmd.alterationType.equalsIgnoreCase("Add")){

            attributeList.add(dbcmd.colNames.get(0));

            //populate columns
            for(int ind = 0; ind < attributeList.size(); ind++){
                bw.write(attributeList.get(ind));
                bw.write("\t");
            }

            bw.write("\n");

            //populate entries
            for(int i = 0; i < entries.size(); i++){
                if(entries.get(i) == null){
                    break;
                }

                bw.write(entries.get(i));
                bw.write("\n");
            }

            bw.close();

        }else{

        //drop specific column attribute from that row
        //delimeted by "\t"
            String attributeDrop = dbcmd.colNames.get(0);
            int attDropIndex = attributeList.indexOf(attributeDrop);

            String [][] entriesDel = new String[entries.size()-1][attributeList.size()];
            int row = 0;
            int in = 0;


            while(entries.get(in) != null){

                String[] columnEntries = entries.get(in).split("\t");
                for(int col = 0; col <columnEntries.length; col++){
                    entriesDel[row][col] = columnEntries[col];
                }

                in++;
                row++;
            }

            String[][] newArray = new String[entries.size()-1][attributeList.size()-1];

            for (int i = 0; i < entries.size() - 1; i++) {
                int newColIdx = 0;
                for (int j = 0; j < attributeList.size(); j++) {
                    if (j != attDropIndex) { // skip the 2nd column (index 1)
                        newArray[i][newColIdx] = entriesDel[i][j];
                        newColIdx++;
                    }
                }
            }

            attributeList.remove(dbcmd.colNames.get(0));


            for(int i = 0; i < attributeList.size(); i++){
                bw.write(attributeList.get(i));
                bw.write("\t");
            }
            bw.write("\n");

            //write new array to file
            for(int i = 0; i < entries.size()-1; i++){
                for(int j = 0; j < attributeList.size(); j++){
                    bw.write(newArray[i][j]);
                    bw.write("\t");
                }
                bw.write("\n");
            }

            bw.close();
        }

        return "";
    }

    private String insertCMD() throws IOException {

        String filePath = path + database + fileSeparator + dbcmd.TableNames.get(0) + ".tab";

        String idIndex = String.valueOf(checkIdIndex(filePath));

        File f = new File(filePath);
        FileWriter fl = new FileWriter(f, true);
        BufferedWriter bw = new BufferedWriter(fl);

        if (dbcmd.values.size() != 0) {

            if (!f.exists()) {
                System.out.println("Table not created");
            } else if (f.exists()) {


                bw.newLine();
                bw.write(idIndex);
                bw.write("\t");

                for (int i = 0; i < dbcmd.values.size(); i++) {

                    bw.write(dbcmd.values.get(i));
                    bw.write("\t");
                }

            }
            bw.close();
            return "[OK]";
        }

        bw.close();
        return "[ERROR - insertCMD]";
    }

    private int checkIdIndex(String filepath) throws IOException {

        int result = 0;

        File f = new File(filepath);
        FileInputStream fl = new FileInputStream(f);
        BufferedReader br = new BufferedReader(new InputStreamReader(fl));

        String x = br.readLine();

        //get the latest index of the ID

        while(x != null){
            x = br.readLine();
            if(x != null){
                result = Integer.parseInt(x.split("\t")[0]);
            }
        }

        result += 1;

        br.close();

        return result;
    }


    private String joinCMD() throws Exception {

        String table1 = dbcmd.join.get(0).table;
        String table2 = dbcmd.join.get(1).table;

        //buffered reader
        String filePath1 = path+database+fileSeparator+table1+".tab";
        File f1 = new File(filePath1);
        FileInputStream fl1 = new FileInputStream(f1);
        BufferedReader br1 = new BufferedReader(new InputStreamReader(fl1));


        if(!f1.exists()){
            throw new Exception("Error JOIN");
        }


        String filePath2 = path+database+fileSeparator+table2+".tab";
        File f2 = new File(filePath2);
        FileInputStream fl2 = new FileInputStream(f2);
        BufferedReader br2 = new BufferedReader(new InputStreamReader(fl2));

        if(!f2.exists()){
            throw new Exception("Error JOIN");
        }

        //br1.readLine() -> Reads the lines of first table file
        //br2.readLine() -> Reads the lines of second table file

        String a1 [] = br1.readLine().split("\t");
        String a2 [] = br2.readLine().split("\t");


        //ArrayList<String> attrL1 -> Stores Attribute List of first Table
        ArrayList<String> attrL1 = new ArrayList<String>();

        for(int i = 0; i < a1.length; i++){
            attrL1.add(a1[i]);
        }

        //ArrayList<String> attrL2 -> Stores Attribute List of second Table
        ArrayList<String> attrL2 = new ArrayList<String>();

        for(int i = 0; i < a1.length; i++){
            attrL2.add(a2[i]);
        }

        //ArrayList<String> entry1 -> Stores each entry of first Table
        ArrayList<String> entry1 = new ArrayList<String>();

        String val1 = br1.readLine();

        while(val1 != null) {
            entry1.add(val1);
            val1 = br1.readLine();
        }

        //ArrayList<String> entry2 -> Stores each entry of second Table
        ArrayList<String> entry2 = new ArrayList<String>();

        String val2 = br2.readLine();

        while(val2 != null) {
            entry2.add(val2);
            val2 = br2.readLine();
        }

        //index of the keys for tables to join
        int indatt1 = find(dbcmd.join.get(0).attributes, attrL1);
        int indatt2 = find(dbcmd.join.get(1).attributes, attrL2);

        String io = "";

        io += "id\t";
        for(int i = 1; i < attrL1.size(); i++){

            if(i != indatt1){
                io += dbcmd.join.get(0).table + "." + attrL1.get(i) + "\t";
            }

        }

        for(int i = 1; i < attrL1.size(); i++){

            if(i != indatt2){
                io += dbcmd.join.get(1).table + "." + attrL2.get(i) + "\t";
            }

        }

        io+= "\n";

        String result = "";

        result += io;

        //drop the id and create a new id's
        for(int i = 0;i<entry1.size();i++){
            for(int j=0;j<entry2.size();j++){
                String arr1[] = entry1.get(i).split("\t");
                String arr2[] = entry2.get(j).split("\t");
                if(indatt1 < arr1.length && indatt2 < arr2.length) {
                    if (arr1[indatt1].equals(arr2[indatt2])) {

                        //creates a new arraylist object which initialised with the elements of arr
                        List<String> list1 = new ArrayList<String>(Arrays.asList(arr1));
                        List<String> list2 = new ArrayList<String>(Arrays.asList(arr2));

                        //removes specific entries from ArrayList
                        list1.remove(arr1[indatt1]);
                        list2.remove(arr2[indatt2]);

                        //converts List object to an array, in which this case returns a new array of type String[0]
                        arr1 = list1.toArray(new String[0]);
                        arr2 = list2.toArray(new String[0]);
                        result += String.join("\t", arr1) + "\t" + String.join("\t", arr2) + "\n";
                    }
                }else{
                    break;
                }
            }
        }


/*        int idCounter = 0;
        //replace the id with a new value
        String joinArray[] = result.split("\n");
        String updateIdArray[] = new String[joinArray.length];

        updateIdArray[0] = joinArray[0];

        for(int i = 1; i < joinArray.length; i++){
            joinArray[i].
        }*/


        return result;
    }

    private String dropTableCMD() {

        String filePath = path+database+fileSeparator+dbcmd.TableNames.get(0)+".tab";
        File fl = new File(filePath);

        if(fl.exists()){
            fl.delete();
        }else{
            return "[ERROR - Table does not exist]";
        }

        return "[OK]";
    }

    private String dropDatabaseCMD() {

        String folderPath = path+dbcmd.DBName;
        File folder = new File(folderPath);

        if(folder.exists()) {
            if(deleteSubdirectories(folder)){
                folder.delete();
            }
        }else {
            return "[ERROR - Database does not exist]";
        }

        return "[OK]";
    }

    public boolean deleteSubdirectories(File directory) {
        boolean success = true;

        // Get a list of all files and directories in the directory
        File[] files = directory.listFiles();

        // Delete all files and subdirectories in the directory
        for (File file : files) {

            // If the file is a file, delete it
            boolean deleted = file.delete();

            if (!deleted) {
                success = false;
            }
        }

        if(success == true){
            return true;
        }

        return false;
    }

        private String useCMD() {
        String folderPath = path+dbcmd.DBName;
        File folder = new File(folderPath);

        if(folder.exists()) {
            this.database = dbcmd.DBName;
        }else {
            return "[ERROR - Database does not exist]";
        }

        return "[OK]";
    }

    private String deleteCMD() throws Exception{

        String result = "";
        String table = dbcmd.TableNames.get(0);

        //buffered reader
        String filePath1 = path+database+fileSeparator+table+".tab";
        File f1 = new File(filePath1);

        if(!f1.exists()){
            throw new Exception("File does not exist");
        }

        FileInputStream fl1 = new FileInputStream(f1);
        BufferedReader br = new BufferedReader(new InputStreamReader(fl1));

        //extracts attribute list
        String attribute[] = br.readLine().split("\t");
        ArrayList<String> attributeList = new ArrayList<String>(Arrays.asList(attribute));


        ArrayList<String> entries = new ArrayList<String>();

        String val1 = br.readLine();
        while(val1 != null) {
            entries.add(val1);
            val1 = br.readLine();
        }

        br.close();
        f1.delete();

        //buffered writer
        File f = new File(filePath1);
        FileWriter fl = new FileWriter(f);
        BufferedWriter bw = new BufferedWriter(fl);

        ArrayList<String> updateEntries = new ArrayList<String>();

        for(int i=0;i<entries.size();i++){
            //if it returns false we save it;
            if(!conditionProcessList(dbcmd,entries.get(i), attributeList)) {
                updateEntries.add(entries.get(i));
            }
        }

        //write to the file
        for(int i = 0; i < attributeList.size(); i++){
            bw.write(attributeList.get(i) + "\t");
        }

        bw.write("\n");

        for(int i = 0; i < updateEntries.size(); i++){
            bw.write(updateEntries.get(i) + "\n");
        }

        bw.close();


        result = "[OK]";
        return result;
    }

    public Boolean conditionProcessList(DBCmd dbcmd, String nextLine, ArrayList<String> attributeList) throws Exception {
        Boolean f = true;
        for(int i = 0; i <dbcmd.conditions.size(); i++){
            f = f && conditionProcess(dbcmd.conditions.get(i), nextLine, attributeList);
        }
        return f;
    }

    public Boolean conditionProcess(Condition cnd, String nextLine, ArrayList<String> attributeList) throws Exception {

        boolean result = true;

        if (cnd.cnd1 != null && cnd.cnd2 != null && cnd.boolOperator != null){
            if(cnd.boolOperator.equalsIgnoreCase("AND")){
                boolean flag = false;
                if(conditionProcess(cnd.cnd1, nextLine, attributeList)){
                    flag = true;
                }

                if(flag == true && conditionProcess(cnd.cnd2, nextLine, attributeList)){
                    return true;
                }else{
                    return false;
                }

            }else if (cnd.boolOperator.equalsIgnoreCase("OR")){
                boolean flag = false;
                 if(conditionProcess(cnd.cnd1, nextLine, attributeList)) {
                     flag = true;
                 }

                 if(flag == true || conditionProcess(cnd.cnd2, nextLine, attributeList)){
                    return true;
                 }else{
                     return false;
                 }

            }
        }else{
            String attName = cnd.attributeName;
            String value = cnd.value;
            String comparator = cnd.comparator;

            System.out.println(attName + " " +value +" " +comparator);

            int index = find(attName, attributeList);

            if(index == -1){
                return true;
            }

            switch(comparator){
                case "==":
                    result = equalCom(index, nextLine, value);
                break;

                case ">":
                    result = moreThanCom(index, nextLine, value);
                    break;

                case "<":
                    result = lessThanCom(index, nextLine, value);
                    break;

                case ">=":
                    result = moreThanEqualCom(index, nextLine, value);
                    break;

                case "<=":
                    result = lessThanEqualCom(index, nextLine, value);
                    break;

                case "!=":
                    result = notEqualCom( index, nextLine, value);
                    break;

                case "LIKE":
                    result = likeCom(index, nextLine, value);
                    break;
            }

        }
        return result;
    }

    private boolean equalCom(int index, String nextLine, String value) throws Exception {

        String x = nextLine.split("\t")[index].trim();
        value  = value.trim();

        try {
            // Try to convert the string to an integer
            int intParseCol = Integer.parseInt(x);
            int intParseVal = Integer.parseInt(value);

            if(intParseVal == intParseCol){
                return true;
            }

        } catch (NumberFormatException e) {
            // Ignore the exception and move on to the next check
        }
        try {
            // Try to convert the string to a float
            float floatParseCol = Float.parseFloat(x);
            float floatParseVal = Float.parseFloat(value);

            if(floatParseCol == floatParseVal){
                return true;
            }

        } catch (NumberFormatException e) {
            // Ignore the exception and move on to the next check
        }

        try {

            if(x.equalsIgnoreCase(value)){
                return true;
            }

        } catch (NumberFormatException e) {
            // Ignore the exception and move on to the next check
        }

        return false;

    }


    private boolean moreThanCom(int index, String nextLine, String value) throws Exception {

        String x = nextLine.split("\t")[index].trim();
        value  = value.trim();

        try {
            // Try to convert the string to an integer
            int intParseCol = Integer.parseInt(x);
            int intParseVal = Integer.parseInt(value);

            if(intParseVal < intParseCol){
                return true;
            }

        } catch (NumberFormatException e) {
            // Ignore the exception and move on to the next check
        }
        try {
            // Try to convert the string to a float
            float floatParseCol = Float.parseFloat(x);
            float floatParseVal = Float.parseFloat(value);

            if(floatParseCol < floatParseVal){
                return true;
            }

        } catch (NumberFormatException e) {
            // Ignore the exception and move on to the next check
        }

        try {

            if(!x.equalsIgnoreCase(value)){
                return true;
            }

        } catch (NumberFormatException e) {
            // Ignore the exception and move on to the next check
        }


        return false;

    }


    private boolean lessThanCom(int index, String nextLine, String value) throws Exception {

        String x = nextLine.split("\t")[index].trim();
        value  = value.trim();

        System.out.println("Next line : " + x);

        try {
            // Try to convert the string to an integer
            int intParseCol = Integer.parseInt(x);
            int intParseVal = Integer.parseInt(value);

            System.out.println("Next line intParseCol: " + intParseCol);
            System.out.println("Next line intParseVal: " + intParseVal);

            if(intParseVal > intParseCol){
                return true;
            }

            return false;
        } catch (NumberFormatException e) {
            // Ignore the exception and move on to the next check
        }

        try {
            // Try to convert the string to a float
            float floatParseCol = Float.parseFloat(x);
            float floatParseVal = Float.parseFloat(value);

            System.out.println("Next line floatParseCol: " + floatParseCol);
            System.out.println("Next line floatParseVal: " + floatParseVal);

            if(floatParseCol > floatParseVal){
                return true;
            }

            return false;
        } catch (NumberFormatException e) {
            // Ignore the exception and move on to the next check
        }

        try {

            if(!x.equalsIgnoreCase(value)){
                return true;
            }

            return false;
        } catch (NumberFormatException e) {
            // Ignore the exception and move on to the next check
        }

        return false;
    }


    private boolean moreThanEqualCom(int index, String nextLine, String value) throws Exception {

        String x = nextLine.split("\t")[index].trim();
        value  = value.trim();

        try {
            // Try to convert the string to an integer
            int intParseCol = Integer.parseInt(x);
            int intParseVal = Integer.parseInt(value);

            if(intParseVal >= intParseCol){
                return true;
            }

        } catch (NumberFormatException e) {
            // Ignore the exception and move on to the next check
        }
        try {
            // Try to convert the string to a float
            float floatParseCol = Float.parseFloat(x);
            float floatParseVal = Float.parseFloat(value);

            if(floatParseCol >= floatParseVal){
                return true;
            }

        } catch (NumberFormatException e) {
            // Ignore the exception and move on to the next check
        }

        try {

            if(!x.equalsIgnoreCase(value)){
                return true;
            }

        } catch (NumberFormatException e) {
            // Ignore the exception and move on to the next check
        }


        return false;

    }

    private boolean lessThanEqualCom(int index, String nextLine, String value) throws Exception {

        String x = nextLine.split("\t")[index].trim();
        value  = value.trim();

        try {
            // Try to convert the string to an integer
            int intParseCol = Integer.parseInt(x);
            int intParseVal = Integer.parseInt(value);

            if(intParseVal <= intParseCol){
                return true;
            }

        } catch (NumberFormatException e) {
            // Ignore the exception and move on to the next check
        }
        try {
            // Try to convert the string to a float
            float floatParseCol = Float.parseFloat(x);
            float floatParseVal = Float.parseFloat(value);

            if(floatParseCol <= floatParseVal){
                return true;
            }

        } catch (NumberFormatException e) {
            // Ignore the exception and move on to the next check
        }

        try {

            if(!x.equalsIgnoreCase(value)){
                return true;
            }

        } catch (NumberFormatException e) {
            // Ignore the exception and move on to the next check
        }


        return false;

    }

    private boolean notEqualCom(int index, String nextLine, String value) throws Exception {

        String x = nextLine.split("\t")[index].trim();
        value  = value.trim();

        try {
            // Try to convert the string to an integer
            int intParseCol = Integer.parseInt(x);
            int intParseVal = Integer.parseInt(value);

            if(intParseVal != intParseCol){
                return true;
            }

        } catch (NumberFormatException e) {
            // Ignore the exception and move on to the next check
        }
        try {
            // Try to convert the string to a float
            float floatParseCol = Float.parseFloat(x);
            float floatParseVal = Float.parseFloat(value);

            if(floatParseCol != floatParseVal){
                return true;
            }

        } catch (NumberFormatException e) {
            // Ignore the exception and move on to the next check
        }

        try {

            if(!x.equalsIgnoreCase(value)){
                return true;
            }

        } catch (NumberFormatException e) {
            // Ignore the exception and move on to the next check
        }

        return false;
    }



    private boolean likeCom(int index, String nextLine, String value) throws Exception {

        String x = nextLine.split("\t")[index].trim();
        value  = value.trim();

        try {
            // Try to convert the string to an integer
            int intParseCol = Integer.parseInt(x);
            int intParseVal = Integer.parseInt(value);

            if(intParseVal != intParseCol){
                return true;
            }

        } catch (NumberFormatException e) {
            // Ignore the exception and move on to the next check
        }
        try {
            // Try to convert the string to a float
            float floatParseCol = Float.parseFloat(x);
            float floatParseVal = Float.parseFloat(value);

            if(floatParseCol != floatParseVal){
                return true;
            }

        } catch (NumberFormatException e) {
            // Ignore the exception and move on to the next check
        }

        try {

            if(!x.equalsIgnoreCase(value)){
                return true;
            }

        } catch (NumberFormatException e) {
            // Ignore the exception and move on to the next check
        }

        return false;
    }

    public Integer find(String attName, ArrayList<String> attributeList){
        return attributeList.indexOf(attName);
    }

}
