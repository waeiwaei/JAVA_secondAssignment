package edu.uob;

import java.io.*;
import java.util.ArrayList;

public class Process {

    ArrayList<String> attributeList;
    String database = "testing";
    String fileSeparator = File.separator;
    String path = (".."+fileSeparator+"cw-db"+fileSeparator+"databases"+fileSeparator);
    DBCmd dbcmd;

    public Process(){
    }

    public void initialise(DBCmd dbstate){
        this.dbcmd = new DBCmd();
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

    private String updateCMD() {

        return "";
    }

    private String selectCMD() throws Exception {

        attributeList = new ArrayList<String>();

        //get the table path
        String filePath = path+database+fileSeparator+dbcmd.TableNames.get(0)+".tab";
        File f = new File(filePath);
        FileInputStream fl = new FileInputStream(f);
        BufferedReader br = new BufferedReader(new InputStreamReader(fl));

        //read line and store each column header in attributeList
        String attributes[];
        attributes = br.readLine().split("\t");

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
                    if(conditionProcessList(dbcmd, nextLine))
                    result+="\n"+nextLine;
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

                String nextLine = br.readLine();
                while(nextLine != null){
                    String arr1[] = nextLine.split("\t");
                    for(i = 0; i < arr.length;i++){
                        if(arr[i]){
                            nextLine+="\t"+arr1[i];
                        }
                    }
                    result+=nextLine;
                    nextLine=br.readLine();
                }
            }

        }

        return result;
    }

    private String alterCMD() {
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

    private String joinCMD() {
        return "";
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

    private String deleteCMD() {
        return "";
    }

    public Boolean conditionProcessList(DBCmd dbcmd, String nextLine) throws Exception {
        Boolean f = true;
        for(int i = 0; i <dbcmd.conditions.size(); i++){
            f = f && conditionProcess(dbcmd.conditions.get(i), nextLine);
        }
        return f;
    }

    public Boolean conditionProcess(Condition cnd, String nextLine) throws Exception {

        boolean result = true;

        if (cnd.cnd1 != null && cnd.cnd2 != null && cnd.boolOperator != null){
            if(cnd.boolOperator == "AND"){
                return conditionProcess(cnd.cnd1, nextLine) && conditionProcess(cnd.cnd2, nextLine);
            }else if (cnd.boolOperator == "OR"){
                return conditionProcess(cnd.cnd1, nextLine) || conditionProcess(cnd.cnd2, nextLine);
            }
        }else{
            String attName = cnd.attributeName;
            String value = cnd.value;
            String comparator = cnd.comparator;

            int index = find(attName);

            if(index == -1){
                return true;
            }

            //switch case between all the comparators
/*            if(nextLine.split("\t")[index] == value){
                return true;
            }else{
                return false;
            }*/

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

        String x = nextLine.split("\t")[index];

        try{

            int colValue = Integer.parseInt(x);
            int compVal = Integer.parseInt(value);

            if(colValue == compVal){
                return true;
            }

        }catch (NumberFormatException e){
            throw new Exception("Comparator - Invalid (type mismatch)");
        }

        return false;

    }


    private boolean moreThanCom(int index, String nextLine, String value) throws Exception {

        String x = nextLine.split("\t")[index];

        try{

            int colValue = Integer.parseInt(x);
            int compVal = Integer.parseInt(value);

            if(colValue > compVal){
                return true;
            }

        }catch (NumberFormatException e){
            throw new Exception("Comparator - Invalid (type mismatch)");
        }

        return false;

    }


    private boolean lessThanCom(int index, String nextLine, String value) throws Exception {

        String x = nextLine.split("\t")[index];

        try{

            int colValue = Integer.parseInt(x);
            int compVal = Integer.parseInt(value);

            if(colValue < compVal){
                return true;
            }

        }catch (NumberFormatException e){
            throw new Exception("Comparator - Invalid (type mismatch)");
        }

        return false;

    }


    private boolean moreThanEqualCom(int index, String nextLine, String value) throws Exception {

        String x = nextLine.split("\t")[index];

        try{

            int colValue = Integer.parseInt(x);
            int compVal = Integer.parseInt(value);

            if(colValue >= compVal){
                return true;
            }

        }catch (NumberFormatException e){
            throw new Exception("Comparator - Invalid (type mismatch)");
        }

        return false;

    }

    private boolean lessThanEqualCom(int index, String nextLine, String value) throws Exception {

        String x = nextLine.split("\t")[index];

        try{

            int colValue = Integer.parseInt(x);
            int compVal = Integer.parseInt(value);

            if(colValue <= compVal){
                return true;
            }

        }catch (NumberFormatException e){
            throw new Exception("Comparator - Invalid (type mismatch)");
        }

        return false;

    }

    private boolean notEqualCom(int index, String nextLine, String value) throws Exception {

        String x = nextLine.split("\t")[index];

        try{

           int colValue = Integer.parseInt(x);
           int compVal = Integer.parseInt(value);

           if(colValue != compVal){
               return true;
           }

        }catch (NumberFormatException e){
            throw new Exception("Comparator - Invalid (type mismatch)");
        }

        return false;
    }

    private boolean likeCom(int index, String nextLine, String value) throws Exception {

        String x = nextLine.split("\t")[index];

        try{
            int colValue = Integer.parseInt(x);
            int compVal = Integer.parseInt(value);

            if(x.matches("value")){
                return true;
            }

        }catch(NumberFormatException e){
            throw new Exception("Comparator - Invalid (type mismatch)");
        }

        return false;
    }

    public Integer find(String attName){
        return attributeList.indexOf(attName);
    }

}
