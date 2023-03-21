package edu.uob;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Locale;
import java.util.jar.Attributes;

public class Process {

    //static String database = "rhngugxcqc";
    static String database;
    static String fileSeparator = File.separator;
    static String storageFolderPath;
    DBCmd dbcmd;

    public Process(DBCmd dbstate, String folderpath){

        this.dbcmd = dbstate;
        Process.storageFolderPath = folderpath + fileSeparator;

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

        String keywords [] = new String[] {"insert", "create", "delete", "update", "alter", "select", "insert", "use", "join" };

        for(int i = 0; i < keywords.length; i++){
            if(keywords[i].equalsIgnoreCase(dbcmd.TableNames.get(0))){
                return "[ERROR] - cannot name table from keyword";
            }
        }

        dbcmd.TableNames.set(0, dbcmd.TableNames.get(0).toLowerCase());

        //check table name with keyword - FROM, JOIN, DELETE, DROP, USE, SELECT, UPDATE, ALTER
        String filePath = storageFolderPath+database+fileSeparator+dbcmd.TableNames.get(0)+".tab";
        File f = new File(filePath);


        if(dbcmd.colNames == null){

            if(!f.exists()){
                boolean create = f.createNewFile();
                if(create){
                    System.out.println("Table created");
                }else{
                    System.out.println("Table not created");
                }

            }else if (f.exists()){
                return "[ERROR] - Table already exists";
            }

        }else{

            for(int i = 0; i < keywords.length; i++){
                for(int j = 0; j < dbcmd.colNames.size(); j++) {
                    if (keywords[i].equalsIgnoreCase(dbcmd.colNames.get(j))) {
                        return "[ERROR] - cannot have attributes with keyword names";
                    }
                }
            }

            if(!f.exists()){
                boolean create = f.createNewFile();
                if(create){
                    System.out.println("Table created");
                }else{
                    System.out.println("Table not created");
                }

            }else if (f.exists()){
                return "[ERROR] - Table already exists";
            }

            FileWriter fl = new FileWriter(f);
            BufferedWriter bw = new BufferedWriter(fl);

            bw.write("id");
            bw.write("\t");

            for(int i = 0; i < dbcmd.colNames.size(); i++){
                bw.write(dbcmd.colNames.get(i).toLowerCase());
                bw.write("\t");
            }

            bw.close();
        }

        return "[OK]\n";
    }

    private String createDatabaseCMD() throws Exception {

        String keywords [] = new String[] {"insert", "create", "delete", "update", "alter", "select", "insert", "use", "join" };

        for(int i = 0; i < keywords.length; i++){
            if (keywords[i].equalsIgnoreCase(dbcmd.DBName)) {
                return "[ERROR] - cannot database from keyword";
            }
        }

        dbcmd.DBName = dbcmd.DBName.toLowerCase();

        String folderPath = storageFolderPath+dbcmd.DBName;
        File folder = new File(folderPath);

        if(!folder.exists()){
            boolean create = folder.mkdirs();

            if(create){
                System.out.println("Database created");
            }

        }else if(folder.exists() && folder.isDirectory()){
            return "[ERROR] - Database already exists";
        }

        return "[OK]\n";
    }

    private String updateCMD() throws Exception{

        String filePath = storageFolderPath+database+fileSeparator+dbcmd.TableNames.get(0) + ".tab";
        File f = new File(filePath);
        FileInputStream fl = new FileInputStream(f);
        BufferedReader br = new BufferedReader(new InputStreamReader(fl));

        //extract table attribute values
        String attributes[] = br.readLine().split("\t");
        ArrayList<String> attributeList = new ArrayList<String>(Arrays.asList(attributes));

//        ArrayList<String>entries = new ArrayList<String>();
//
//        //extract all the entries
//        String x = br.readLine();
//        while(x != null){
//            entries.add(x);
//            x = br.readLine();
//        }

        ArrayList<String> entries = readEntries(br);

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
            updateName.add(dbcmd.nameValueList.get(i).Name.toLowerCase());
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
                if(updEntries[k][y] == null){
                    break;
                }

                bw.write(updEntries[k][y]);
                bw.write("\t");
            }
            bw.write("\n");
        }

        bw.close();

        return "[OK]\n";
    }

    private String selectCMD() throws Exception {

        ArrayList<String> attributeList = new ArrayList<String>();

        //get the table path
        String filePath = storageFolderPath+database+fileSeparator+dbcmd.TableNames.get(0)+".tab";
        File f = new File(filePath);
        if(!f.exists())
            return "[ERROR]";
        FileInputStream fl = new FileInputStream(f);
        BufferedReader br = new BufferedReader(new InputStreamReader(fl));

        //read line and store each column header in attributeList
        String attributes[] = br.readLine().split("\t");

        for(int i = 0; i < attributes.length; i++){
            attributeList.add(attributes[i].toLowerCase());
        }



        String result = "[OK]\n";

        //if no conditions in SELECT query
        if(dbcmd.conditions == null){

            if(dbcmd.colNames.get(0).equals("*")){
                result+= String.join("\t", attributeList);
                String nextLine = br.readLine();
                while(nextLine!=null){
                    result+= "\n"+nextLine;
                    nextLine = br.readLine();
                }
            }else{
                boolean arr[] = new boolean[attributes.length];
                int i = 0;

                //removes '.' character
                char targetChar = '.';
                for (int j = 0; j < dbcmd.colNames.size(); j++) {
                    String str = dbcmd.colNames.get(j);
                    int index = str.indexOf(targetChar);
                    if (index != -1) {
                        String modifiedStr = str.substring(index + 1);
                        dbcmd.colNames.set(j, modifiedStr);
                    }
                }

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
                        if (arr[d] == true) {
                            result += arr1[d] + "\t";
                        }
                    }

                    nextLine=br.readLine();
                }
            }
        }else {

            if (dbcmd.colNames.get(0).equals("*")) {
                result += String.join("\t", attributeList);
                String nextLine = br.readLine();
                while (nextLine != null && !nextLine.isEmpty()) {
                    if (conditionProcessList(dbcmd, nextLine, attributeList))
                        result += "\n" + nextLine;
                    nextLine = br.readLine();
                }
            } else {
                boolean arr[] = new boolean[attributes.length];
                int i = 0;


                //removes '.' character
                char targetChar = '.';
                for (int j = 0; j < dbcmd.colNames.size(); j++) {
                    String str = dbcmd.colNames.get(j);
                    int index = str.indexOf(targetChar);
                    if (index != -1) {
                        String modifiedStr = str.substring(index + 1);
                        dbcmd.colNames.set(j, modifiedStr);
                    }
                }


                for (i = 0; i < dbcmd.colNames.size(); i++) {
                    int j = 0;
                    for (j = 0; j < attributes.length; j++) {
                        if (dbcmd.colNames.get(i).equalsIgnoreCase(attributes[j])) {
                            arr[j] = true;
                            break;
                        }
                    }
                    if (j == attributes.length) {
                        System.out.println("Entering here!");
                        throw new Exception("Attribute " + dbcmd.colNames.get(i) + " not found");
                    }
                }

                for (int e = 0; e < dbcmd.colNames.size(); e++) {
                    result += dbcmd.colNames.get(e);
                    result += "\t";
                }
                result += "\n";

                int trueCounter = 0;
                for (int d = 0; d < arr.length; d++) {

                    if (arr[d] == true) {
                        trueCounter++;
                    }
                }

                List<Integer> trueIndexes = new ArrayList<Integer>();
                for (int in = 0; in < arr.length; in++) {
                    if (arr[in]) {
                        trueIndexes.add(in);
                    }
                }

                String nextLine = br.readLine();
                while(nextLine != null && !nextLine.isEmpty()){

                    boolean flag = true;
                    String arr1[] = nextLine.split("\t");

                    for(int u = 0; u < trueIndexes.size() ;u++){
                        if(u < trueCounter && conditionProcessList(dbcmd, nextLine, attributeList)) {
                            flag = false;
                            result += arr1[trueIndexes.get(u)] + "\t";
                        }else{break;}
                    }

                    nextLine=br.readLine();

                    if(nextLine!=null && flag == false)
                        result+="\n";
                }

            }
        }

        return result;
    }

    private String alterCMD() throws Exception {

        String filePath = storageFolderPath + database + fileSeparator + dbcmd.TableNames.get(0) + ".tab";

        File f = new File(filePath);
        FileInputStream fl = new FileInputStream(f);
        BufferedReader br = new BufferedReader(new InputStreamReader(fl));

        String attribute = br.readLine();
        ArrayList<String> attributeList = new ArrayList<String>();
        ArrayList<String> entries = new ArrayList<String>();

        if(attribute != null){

            String attlist[] = attribute.split("\t");
            attributeList = new ArrayList<String>(Arrays.asList(attlist));

            entries = readEntries(br);

            br.close();
        }

        File f1 = new File(filePath);
        if(!f1.exists()){
            throw new Exception("Error - table does not exist");
        }

        f1.delete();


        FileWriter fl1 = new FileWriter(f1);
        BufferedWriter bw = new BufferedWriter(fl1);

        //include column at the end - with the id attribute
        if(dbcmd.alterationType.equalsIgnoreCase("Add")){

            String keywords [] = new String[] {"insert", "create", "delete", "update", "alter", "select", "insert", "use", "join" };

            //for each row, we want to create a new string with the original values and with the + ("\t"+"")
            //result += String.join("\t", arr1) + "\t" + String.join("\t", arr2) + "\n";


            for(int i = 0; i < keywords.length; i++){
                if(keywords[i].equalsIgnoreCase(dbcmd.colNames.get(0))){

                    //populate columns
                    for(int ind = 0; ind < attributeList.size(); ind++){
                        bw.write(attributeList.get(ind));
                        bw.write("\t");
                    }

                    bw.write("\n");

                    //populate entries
                    for(int d = 0; d < entries.size(); d++){
                        if(entries.get(d) == null){
                            break;
                        }

                        bw.write(entries.get(d));
                        bw.write("\n");
                    }

                    bw.close();
                    return "[ERROR] - attribute name cannot be a keyword";
                }
            }

            //persumes table is not empty
            if(attributeList.contains("id")){
                attributeList.add(dbcmd.colNames.get(0).toLowerCase());
            }else{
                //persumes table is empty - when adding column, must add id then new attribute
                attributeList.add("id");
                attributeList.add(dbcmd.colNames.get(0).toLowerCase());
            }

            //populate columns
            for(int ind = 0; ind < attributeList.size(); ind++){
                bw.write(attributeList.get(ind));
                bw.write("\t");
            }

            //populate entries
            for(int i = 0; i < entries.size(); i++){
                if(entries.get(i) == null){
                    break;
                }
                bw.write("\n");
                bw.write(String.join("\t", entries.get(i) + " "));
                //bw.write("\n");
            }

            bw.close();

        }else{

        //drop specific column attribute from that row
        //delimeted by "\t"
            String attributeDrop = dbcmd.colNames.get(0);
            int attDropIndex = attributeList.indexOf(attributeDrop);

            String [][] entriesDel = new String[entries.size()][attributeList.size()];
            int row = 0;
            int in = 0;


            while(in < entries.size()){

                String[] columnEntries = entries.get(in).split("\t");
                for(int col = 0; col <columnEntries.length; col++){
                    entriesDel[in][col] = columnEntries[col];
                }

                in++;
            }

            String[][] newArray = new String[entries.size()][attributeList.size()-1];

            for (int i = 0; i < entries.size(); i++) {
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
            for(int i = 0; i < newArray.length; i++){
                for(int j = 0; j < attributeList.size(); j++){
                    bw.write(newArray[i][j]);
                    bw.write("\t");
                }
                bw.write("\n");
            }

            bw.close();
        }

        return "[OK]\n";
    }

    private String insertCMD() throws Exception {

        String filePath = storageFolderPath + database + fileSeparator + dbcmd.TableNames.get(0) + ".tab";
        String idIndex = String.valueOf(checkIdIndex(filePath));

        File f1 = new File(filePath);
        if(!f1.exists()){
            throw new Exception("Table does not exist");
        }

        FileReader fr = new FileReader(f1);
        BufferedReader br = new BufferedReader(fr);

        String attributeList [] = br.readLine().split("\t");

        ArrayList<String> entries = readEntries(br);
        int numberAttributes = attributeList.length - 1;


        br.close();

        File f = new File(filePath);
        FileWriter fl = new FileWriter(f);
        BufferedWriter bw = new BufferedWriter(fl);

        if (dbcmd.values.size() != 0 && dbcmd.values.size() == numberAttributes) {

            if (!f.exists()) {
                System.out.println("Table not created");
            } else if (f.exists()) {
                  String newInsert = idIndex + "\t";

                for (int i = 0; i < dbcmd.values.size(); i++) {

                    //find if there is a "+" then we remove it
                    if(dbcmd.values.get(i).contains("+")){
                        dbcmd.values.set(i, dbcmd.values.get(i).replace("+" ,""));
                    }

                    newInsert += dbcmd.values.get(i) + "\t";
                }

                entries.add(newInsert);

                //write attribute list
                for(int i = 0; i < attributeList.length; i++){
                    bw.write(attributeList[i]);
                    bw.write("\t");
                }

                bw.write("\n");

                //write entries back to file
                for(int i = 0; i < entries.size(); i++){
                    bw.write(entries.get(i));
                    bw.write("\n");
                }

//                bw.newLine();
//
//                bw.write(idIndex);
//                bw.write("\t");
//
//                for (int i = 0; i < dbcmd.values.size(); i++) {
//
//                    //find if there is a "+" then we remove it
//                    if(dbcmd.values.get(i).contains("+")){
//                        dbcmd.values.set(i, dbcmd.values.get(i).replace("+" ,""));
//                    }
//
//                    bw.write(dbcmd.values.get(i));
//                    bw.write("\t");
//                }

            }
            bw.close();
            return "[OK]\n";
        }else{
            bw.close();
        }

        return "[ERROR] - number of values to insert does not match the number of attributes \n";
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
            if(x == null) {
                break;
            }
            result = Integer.parseInt(x.split("\t")[0]);
        }

        result += 1;

        br.close();

        return result;
    }


    private String joinCMD() throws Exception {

        String table1 = dbcmd.join.get(0).table;
        String table2 = dbcmd.join.get(1).table;

        //buffered reader
        String filePath1 = storageFolderPath+database+fileSeparator+table1+".tab";
        File f1 = new File(filePath1);

        if(!f1.exists()){
            return "[ERROR]\n";
        }

        FileInputStream fl1 = new FileInputStream(f1);
        BufferedReader br1 = new BufferedReader(new InputStreamReader(fl1));

        String filePath2 = storageFolderPath+database+fileSeparator+table2+".tab";
        File f2 = new File(filePath2);

        if(!f2.exists()){
            return "[ERROR]\n";
        }


        FileInputStream fl2 = new FileInputStream(f2);
        BufferedReader br2 = new BufferedReader(new InputStreamReader(fl2));

        if(!f2.exists()){
            throw new Exception("Error - unable to find file");
        }

        //br1.readLine() -> Reads the lines of first table file
        //br2.readLine() -> Reads the lines of second table file

        String a1 [] = br1.readLine().split("\t");
        String a2 [] = br2.readLine().split("\t");


        //ArrayList<String> attrL1 -> Stores Attribute List of first Table
        ArrayList<String> attrL1 = new ArrayList<String>(Arrays.asList(a1));
//
//        for(int i = 0; i < a1.length; i++){
//            attrL1.add(a1[i]);
//        }


        //ArrayList<String> attrL2 -> Stores Attribute List of second Table
        ArrayList<String> attrL2 = new ArrayList<String>(Arrays.asList(a2));

//        for(int i = 0; i < a2.length; i++){
//            attrL2.add(a2[i]);
//        }

        //ArrayList<String> entry1 -> Stores each entry of first Table
        //ArrayList<String> entry1 = new ArrayList<String>();

//        String val1 = br1.readLine();
//
//        while(val1 != null) {
//            entry1.add(val1);
//            val1 = br1.readLine();
//        }
//
        ArrayList<String> entry1 = readEntries(br1);

        //ArrayList<String> entry2 -> Stores each entry of second Table
//        ArrayList<String> entry2 = new ArrayList<String>();
//
//        String val2 = br2.readLine();
//
//        while(val2 != null) {
//            entry2.add(val2);
//            val2 = br2.readLine();
//        }

        ArrayList<String> entry2 = readEntries(br2);


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

        for(int i = 1; i < attrL2.size(); i++){
            if(i != indatt2){
                io += dbcmd.join.get(1).table + "." + attrL2.get(i) + "\t";
            }
        }

        io+= "\n";

        String result = "[OK]\n";

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


        String t1 = "";

        //replace the id with a new ID values
        String joinArray[] = result.split("\n");
        String updateIdArray[] = new String[joinArray.length];
        int idCounter = 1;

        //we wanna split up based on tabs
        for(int i = 2; i < joinArray.length; i++){
            String tempArray [] = joinArray[i].split("\t");
            tempArray[0] = Integer.toString(idCounter);

            for(int j = 0; j < tempArray.length; j++){
                t1+= tempArray[j] + "\t";
            }

            t1+= "\n";

            idCounter++;
        }


        result = "[OK]\n" + io + t1;
        return result;
    }

    private String dropTableCMD() {

        String filePath = storageFolderPath+database+fileSeparator+dbcmd.TableNames.get(0)+".tab";
        File fl = new File(filePath);

        if(fl.exists()){
            fl.delete();
        }else{
            return "[ERROR] - Table does not exist";
        }

        return "[OK]\n";
    }

    private String dropDatabaseCMD() {

        String folderPath = storageFolderPath+dbcmd.DBName;
        File folder = new File(folderPath);

        if(folder.exists()) {
            if(deleteSubdirectories(folder)){
                folder.delete();
            }
        }else {
            return "[ERROR] - Database does not exist";
        }

        return "[OK]\n";
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
        String folderPath = storageFolderPath+dbcmd.DBName;
        File folder = new File(folderPath);

        if(folder.exists()) {
            Process.database = dbcmd.DBName;
        }else {
            return "[ERROR] - Database does not exist";
        }

        return "[OK]\n";
    }

    private String deleteCMD() throws Exception{

        String result = "";
        String table = dbcmd.TableNames.get(0);

        //buffered reader
        String filePath1 = storageFolderPath+database+fileSeparator+table+".tab";
        File f1 = new File(filePath1);

        if(!f1.exists()){
            return "[ERROR] - table does not exist\n";
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
            bw.write(updateEntries.get(i));

            if(i != updateEntries.size() - 1){
                bw.write("\n");
            }

        }



        bw.close();

        return "[OK]\n";
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

            }else{
                throw new Exception("Exception - not passed a valid BoolOperator");
            }
        }else{
            String attName = cnd.attributeName;
            String value = cnd.value;
            String comparator = cnd.comparator;

            int index = find(attName, attributeList);
            //throw exception if attribute name is not found
            if(index == -1){
                return false;
            }

            switch(comparator.toUpperCase()){
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
        value = value.trim();

        try {
            // Try to convert the string to an integer
            int intParseCol = Integer.parseInt(x);
            int intParseVal = Integer.parseInt(value);

            if(intParseVal == intParseCol){
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

            if(floatParseCol == floatParseVal){
                return true;
            }

            return false;

        } catch (NumberFormatException e) {
            // Ignore the exception and move on to the next check
        }

        try {

            if(x.equals(value)){
                return true;
            }

            return false;

        } catch (NumberFormatException e) {
            // Ignore the exception and move on to the next check
        }


        try{

            if(x.equalsIgnoreCase(" ") && value == null){
                return true;
            }

            return false;
        }catch(NullPointerException e){

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

            return false;

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

            return false;

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


        return false;
    }


    private boolean moreThanEqualCom(int index, String nextLine, String value) throws Exception {

        String x = nextLine.split("\t")[index].trim();
        value  = value.trim();

        try {
            // Try to convert the string to an integer
            int intParseCol = Integer.parseInt(x);
            int intParseVal = Integer.parseInt(value);

            if(intParseVal <= intParseCol){
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

            if(floatParseVal <= floatParseCol){
                return true;
            }

            return false;

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

            if(intParseVal >= intParseCol){
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

            if(floatParseVal <= floatParseCol){
                return true;
            }

            return false;

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

            return false;

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

            return false;
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

            return false;

        } catch (NumberFormatException e) {
            // Ignore the exception and move on to the next check
        }

        try {

            if(x.contains(value)){
                return true;
            }


            return false;

        } catch (NumberFormatException e) {
            // Ignore the exception and move on to the next check
        }

        return false;
    }

    public Integer find(String attName, ArrayList<String> attributeList){



        return attributeList.indexOf(attName.toLowerCase());
    }

    public ArrayList<String> readEntries (BufferedReader br) throws IOException {
        ArrayList<String> attributelist = new ArrayList<String>();

        String val1 = br.readLine();

        while(val1 != null) {
            attributelist.add(val1);
            val1 = br.readLine();
        }

        return attributelist;
    }

    public boolean checkKeywords(String input){


        return true;
    }


}
