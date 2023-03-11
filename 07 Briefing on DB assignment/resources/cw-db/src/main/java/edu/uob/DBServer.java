package edu.uob;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.ArrayList;


/** This class implements the DB server. */
public class DBServer {

    private static final char END_OF_TRANSMISSION = 4;
    private String storageFolderPath;

    private static ArrayList<String> tokens;

    private static int current_token_index;

    public static void main(String args[]) throws Exception {

        //tokenize commands from user
        //String command = "UPDATE marks SET mark = 38 WHERE name == 'Clive';";
        String command = "CREATE TABLE marks (name, mark, pass);";
        //String command = "CREATE DATABASE marks;";

        tokenizer(command);
        System.out.println(tokens);

        current_token_index = 0;

        //Parse tokens According to Grammar (BNF.txt)
        if(parseCommand()){
            System.out.println("Parsed okay");
        }else{
            System.out.println("Fail Parse");
        }


        /*READ AND WRITE TO FILES IN THE RELATIVE FILE PATH*/
//        String fileSeparator = File.separator;
//        //example command to write to file
//        //Structure to store tables
//        ArrayList <ArrayList<String>> tables = new ArrayList<ArrayList<String>>();
//        String command = "CREATE TABLE marks (name, mark, pass);";
//
//        //store individual tokens in String object array
//        String[] token;
//        token = command.split("\s");
//
//        //identify the number of columns to create
//        int counter = 0;
//        for (int i = 0; i < command.length(); i++){
//            char c = command.charAt(i);
//            if(c == '('){
//                while(c != ')'){
//                    c=command.charAt(i);
//                    if(c == ','){
//                        counter++;
//                    }
//                    i++;
//                }
//                counter++;
//            }
//        }
//
//        //add columns - based on comma delimiter for columns
//        tables.add(new ArrayList<String>());
//        for(int i = 0; i < counter; i++) {
//            tables.get(0).add(null);
//        }
//
//        tables.get(0).set(0, token[3].replaceAll("[,.();]",""));
//        tables.get(0).set(1, token[4].replaceAll("[,.();]",""));
//        tables.get(0).set(2, token[5].replaceAll("[,.();]",""));
//
//        //input to a new file - write to it
//        FileWriter f2 = new FileWriter(".."+fileSeparator+"cw-db"+fileSeparator+"databases"+fileSeparator+ token[2]+".tab");
//        BufferedWriter bw = new BufferedWriter(f2);
//
//        bw.write("id");
//        bw.write("\t");
//        bw.write(tables.get(0).get(0));
//        bw.write("\t");
//        bw.write(tables.get(0).get(1));
//        bw.write("\t");
//        bw.write(tables.get(0).get(2));
//
//        bw.close();
//
//
//
//
//
//
//        // Read from a file and store within 2D array
//        File f = new File(".."+fileSeparator+"people.tab");
//        FileInputStream fiStream = new FileInputStream(f);
//        BufferedReader br = new BufferedReader(new InputStreamReader(fiStream));
//
//
//
//        String[] columnHeader;
//        //read the first row - column header
//        String s = br.readLine();
//        ArrayList<ArrayList<String>> read_file = new ArrayList<ArrayList<String>>();
//        read_file.add(new ArrayList<>());
//        read_file.get(0).add(null);
////        columnHeader = s.split("\t");
////
////        for(int i = 0; i < columnHeader.length;i++){
////            System.out.println(columnHeader[i]);
////        }
//
//        int i = 0;
//        while(s != null){
//            s = br.readLine();
//            if(s.isEmpty()){
//                break;
//            }
//
//            if(i < counter) {
//                for (int j = 0; j <= 3; j++) {
//                    read_file.get(i).set(j, Arrays.toString(s.split("\t")));
//                }
//                i++;
//            }
//        }


        DBServer server = new DBServer();
        server.blockingListenOn(8888);

    }

    /**
    * KEEP this signature otherwise we won't be able to mark your submission correctly.
    */
    public DBServer() {
        storageFolderPath = Paths.get("databases").toAbsolutePath().toString();
        try {
            // Create the database storage folder if it doesn't already exist !
            Files.createDirectories(Paths.get(storageFolderPath));
        } catch(IOException ioe) {
            System.out.println("Can't seem to create database storage folder " + storageFolderPath);
        }
    }

    /**
    * KEEP this signature (i.e. {@code edu.uob.DBServer.handleCommand(String)}) otherwise we won't be
    * able to mark your submission correctly.
    *
    * <p>This method handles all incoming DB commands and carries out the required actions.
    */
    public String handleCommand(String command) {
        // TODO implement your server logic here

        // Handle incomming commands - split into tokens (Store within ArrayList<String>)

        // Parse against grammar - BNF document to check grammar conformity (Within the parsing, come up with )


        return "";
    }


    public static void tokenizer(String command){

        String[] specialChar = {",", ";", "'", ")", "(", ".", "+"};

        // lookahead matches a position in the input string that is followed by one of the special characters, string array or a whitespace character
        // lookbehind matches a position in the input string that is preceded by one of the special characters, string array or a whitespace character.
        String regex = "(?=[" + String.join("", specialChar) + "\\s])|(?<=[" + String.join("", specialChar) + "\\s])";

        String[] tokensArray = command.split(regex);

        tokens = new ArrayList<String>();
        for (String token : tokensArray) {
            if (!token.trim().isEmpty()) {
                tokens.add(token.trim());
            }
        }
    }

    //<Command>         ::=  <CommandType> ";"
    private static boolean parseCommand() throws Exception {
        // Try to match <CommandType> rule
        if (!parseCommandType()) {
            return false;
        }

        // Match ";" token
        if(!tokens.get(current_token_index).equals(";")){
            return false;
        }

        return true;
    }

    //<CommandType>     ::=  <Use> | <Create> | <Drop> | <Alter> | <Insert> | <Select> | <Update> | <Delete> | <Join>
    public static boolean parseCommandType() throws Exception{

        boolean parse = false;

        switch(tokens.get(current_token_index).toLowerCase()){
            case "create":
                current_token_index++;
                if(parseCreate()){
                    parse = true;
                }
                break;

            case "update":
                current_token_index++;
                if(parseUpdate()){
                    parse = true;
                }
                break;

            case "select":
                //parseSelect();
                break;

            case "alter":
                //parseAlter();
                break;

            case "insert":
                //parseInsert();
                break;

            case "join":
                //parseJoin();
                break;

            case "drop":
                //parseDrop();
                break;

            case "delete":
                //parseDelete();
                break;
            default:
                throw new Exception("Invalid command typed :" + tokens.get(current_token_index));
        }

        if(parse == true){
            return true;
        }

        return false;
    }

    //<Create>          ::=  <CreateDatabase> | <CreateTable>
    private static boolean parseCreate() {

        // <CreateDatabase>  ::=  "CREATE DATABASE " [DatabaseName]
        if (tokens.get(current_token_index).equalsIgnoreCase("database")) {
            if (tokens.size() <= 4) { // identify if the command is 3 values
                //parse okay
                current_token_index = current_token_index + 2;
            }

        // <CreateTable>     ::=  "CREATE TABLE " [TableName] | "CREATE TABLE " [TableName] "(" <AttributeList> ")"
        } else if (tokens.get(current_token_index).equalsIgnoreCase("table")) {
            if (tokens.size() <= 4) {
                //return error
                return false;
            }

            if (tokens.contains("(")) {
                current_token_index = tokens.indexOf("(") + 1;  // identify first attribute
                int endIndex = tokens.indexOf(")");

                // Parse the attribute list
                ArrayList<String> attributes = parseAttributeList();

                // Print the attributes
                if(attributes.isEmpty() != true) {
                    System.out.println("Attributes:");
                    for (String attribute : attributes) {
                        System.out.println(attribute);
                    }
                    current_token_index++;
                }else{
                    System.out.println("No attributes were found");
                }
            }
        }

        if(tokens.get(current_token_index).equals(";")){
            return true;
        }

        return false;
    }

    //<AttributeList>   ::=  [AttributeName] | [AttributeName] "," <AttributeList>
    private static ArrayList<String> parseAttributeList() {
        ArrayList<String> attributes = new ArrayList<>();

        while (current_token_index < tokens.indexOf(")")) {
            String attributeName = parseAttributeName();
            if (attributeName == null) {
                return null;
            }
            attributes.add(attributeName);

            // Check if there are more attributes to parse
            if (current_token_index < tokens.indexOf(")") && tokens.get(current_token_index).equals(",")) {
                current_token_index++;
                ArrayList<String> remainingAttributes = parseAttributeList();
                if (remainingAttributes == null) {
                    return null;
                }
                attributes.addAll(remainingAttributes);
                break;
            }
        }

        return attributes;
    }


    // [AttributeName]   ::=  [PlainText] | [TableName] "." [PlainText]
    private static String parseAttributeName() {
        String attributeName = "";

//        if (current_token_index >= tokens.indexOf(")")) {
//            return null;
//        }

        // Check if the attribute name is a plain text or a table name and plain text
        if (parsePlainText(tokens.get(current_token_index))) {
            attributeName += tokens.get(current_token_index);
            current_token_index++;
            if (current_token_index < tokens.indexOf(")") && tokens.get(current_token_index).equals(".")) {
                attributeName += ".";
                current_token_index++;
                if (current_token_index >= tokens.indexOf(")") || !parsePlainText(tokens.get(current_token_index))) {
                    return null;
                }
                attributeName += tokens.get(current_token_index);
                current_token_index++;
            }
        } else {
            return null;
        }

        return attributeName;
    }

//    private static boolean parseTableName(String token) {
//        return parsePlainText(token);
//    }

    //[Space]           ::=  " "
    private static boolean parseSpace(String token) {
        if(token.equals(" ")){
            return true;
        }
        return false;
    }

    //[Digit]           ::=  "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9"
    private static boolean parseDigit(String token) {

        if(token.matches("[0-9]")){
            return true;
        }

        return false;
    }

    //[Symbol]          ::=  "!" | "#" | "$" | "%" | "&" | "(" | ")" | "*" | "+" | "," | "-" | "." | "/" | ":" | ";" | ">" | "=" | "<" | "?" | "@" | "[" | "\" | "]" | "^" | "_" | "`" | "{" | "}" | "~"
    private static boolean parseSymbol(String token) {
        String regex = "[!#$%&()*+,-./:;>=<?@\\[\\]\\^_`\\{\\}|~]*";
        return token.matches(regex);
    }

    //[Uppercase]       ::=  "A" | "B" | "C" | "D" | "E" | "F" | "G" | "H" | "I" | "J" | "K" | "L" | "M" | "N" | "O" | "P" | "Q" | "R" | "S" | "T" | "U" | "V" | "W" | "X" | "Y" | "Z"
    private static boolean parseUppercase(String token) {
        return token.matches("[A-Z]");
    }

    //[Lowercase]       ::=  "a" | "b" | "c" | "d" | "e" | "f" | "g" | "h" | "i" | "j" | "k" | "l" | "m" | "n" | "o" | "p" | "q" | "r" | "s" | "t" | "u" | "v" | "w" | "x" | "y" | "z"
    private static boolean parseLowercase(String token) {
        return token.matches("[a-z]");
    }

    //[Letter]          ::=  [Uppercase] | [Lowercase]
    private static boolean parseLetter(String token) {
        return parseUppercase(token) || parseLowercase(token);
    }

    //[PlainText]       ::=  [Letter] | [Digit] | [PlainText] [Letter] | [PlainText] [Digit]
    private static boolean parsePlainText(String token) {

        if (token == null) {
            return false;
        }
        if (token.length() == 1) {
            return parseLetter(token) || parseDigit(token);
        }
        for (int i = 0; i < token.length(); i++) {
            char c = token.charAt(i);
            if (!parseLetter(Character.toString(c)) && !parseDigit(Character.toString(c))) {
                return false;
            }
        }

        return true;
    }


    // <Update>          ::=  "UPDATE " [TableName] " SET " <NameValueList> " WHERE " <Condition>
    public static boolean parseUpdate() throws Exception {
        // Match [TableName] rule
        if (!parseTableName()) {
            return false;
        }

        // Match "SET" token
        if (!tokens.get(current_token_index).equalsIgnoreCase("set")) {
            return false;
        }
        current_token_index++;

        // Match <NameValueList> rule
        if (!parseNameValueList()) {
            return false;
        }

        // Match "WHERE" token
        if (!tokens.get(current_token_index).equalsIgnoreCase("where")) {
            return false;
        }
        current_token_index++;

        // Match <Condition> rule
        if (!ConditionParser()) {
            return false;
        }

        // Match ";" token
        if (!tokens.get(current_token_index).equals(";")) {
            return false;
        }

        return true;
    }

    //<Condition>       ::=  "(" <Condition> [BoolOperator] <Condition> ")" | <Condition> [BoolOperator] <Condition> | "(" [AttributeName] [Comparator] [Value] ")" | [AttributeName] [Comparator] [Value]
    private static boolean ConditionParser() {
        return false;
    }

    //<NameValueList>   ::=  <NameValuePair> | <NameValuePair> "," <NameValueList>
    private static boolean parseNameValueList() {
        //check name-value pair
        if(!parseNameValuePair()){

        }
        return false;
    }

    //<NameValuePair>   ::=  [AttributeName] "=" [Value]
    private static boolean parseNameValuePair() {

        if(parseAttributeName().isEmpty()){
            return false;
        }

        if(!tokens.get(current_token_index).equals("=")){
            return false;
        }

        current_token_index++;

        if(parseValueLiteral()){
            return true;
        }

        return false;
    }
    
    // [Value]           ::=  "'" [StringLiteral] "'" | [BooleanLiteral] | [FloatLiteral] | [IntegerLiteral] | "NULL"
    private static boolean parseValueLiteral() {
        String current_token = tokens.get(current_token_index);

        if (current_token.equals("NULL")) {
            // If the current token is "NULL", the value literal is valid
            return true;
        }

        if (current_token.equals("'")) {
            // If the current token is a single quote, it's the start of a string literal
            current_token_index++;

            // Parse the string literal
            boolean string_literal_valid = parseStringLiteral();

            if(string_literal_valid && tokens.get(current_token_index).equals("'")) {
                // If the string literal is valid and we've reached the end single quote, the value literal is valid
                return true;
            }
        }

        if(parseBooleanLiteral()){
            return true;
        }

        if(parseFloatLiteral()){
            return true;
        }

        if(parseIntegerLiteral()){
            return true;
        }


        // If we didn't return true above, the value literal is not valid
        return false;
    }

    //[IntegerLiteral]  ::=  [DigitSequence] | "-" [DigitSequence] | "+" [DigitSequence]
    private static boolean parseIntegerLiteral() {
        int initialIndex = current_token_index;
        if (current_token_index < tokens.size() && (tokens.get(current_token_index).equals("+") || tokens.get(current_token_index).equals("-"))) {
            current_token_index++;
        }
        if (current_token_index < tokens.size() && parseDigitSequence()) {
            current_token_index = initialIndex;
            return true;
        }
        current_token_index = initialIndex;
        return false;
    }

    // [FloatLiteral]    ::=  [DigitSequence] "." [DigitSequence] | "-" [DigitSequence] "." [DigitSequence] | "+" [DigitSequence] "." [DigitSequence]
    private static boolean parseFloatLiteral() {
        int initialIndex = current_token_index;
        if (current_token_index < tokens.size() && (tokens.get(current_token_index).equals("+") || tokens.get(current_token_index).equals("-"))) {
            current_token_index++;
        }
        if (current_token_index < tokens.size() && parseDigitSequence()) {
            if (current_token_index < tokens.size() && tokens.get(current_token_index).equals(".")) {
                current_token_index++;
                if (current_token_index < tokens.size() && parseDigitSequence()) {
                    return true;
                }
            }
        }
        current_token_index = initialIndex;
        return false;
    }

    //[DigitSequence]   ::=  [Digit] | [Digit] [DigitSequence]
    private static boolean parseDigitSequence() {
        if (parseDigit(tokens.get(current_token_index))) {
            current_token_index++;
            if (current_token_index < tokens.size() && parseDigitSequence()) {
                return true;
            }
            return true;
        }
        return false;
    }

    //[BooleanLiteral]  ::=  "TRUE" | "FALSE"
    private static boolean parseBooleanLiteral() {
        if(tokens.get(current_token_index).equalsIgnoreCase("TRUE") || tokens.get(current_token_index).equalsIgnoreCase("FALSE")){
            return true;
        }

        return false;
    }

    //[StringLiteral]   ::=  "" | [CharLiteral] | [StringLiteral] [CharLiteral]
    private static boolean parseStringLiteral() {
        if (tokens.get(current_token_index).equals("")) {
            // Empty string literal is valid
            return true;
        }

        if (tokens.get(current_token_index).equals("'")) {
            // Move to the next token
            current_token_index++;

            // Loop through each character in the string literal
            while (current_token_index < tokens.size() && !tokens.get(current_token_index).equals("'")) {
                // Check if the current token is a valid character literal or a nested string literal
                boolean char_or_string_literal_valid = parseCharLiteral() || parseStringLiteral();

                if (!char_or_string_literal_valid) {
                    // If the current token is not a valid character or string literal, the string literal is not valid
                    return false;
                }

                // Move to the next token
                current_token_index++;
            }

            // If the string literal ends with a single quote, it's valid
            if (current_token_index < tokens.size() && tokens.get(current_token_index).equals("'")) {
                return true;
            }
        }

        // If we haven't returned true by this point, the string literal is not valid
        return false;
    }

    //[CharLiteral]     ::=  [Space] | [Letter] | [Symbol] | [Digit]
    private static boolean parseCharLiteral() {

        if(parseSpace(tokens.get(current_token_index))){
            return true;
        }

        if(parseLetter(tokens.get(current_token_index))){
            return true;
        }

        if(parseDigit(tokens.get(current_token_index))){
            return true;
        }

        if(parseSymbol(tokens.get(current_token_index))){
            return true;
        }

        return false;
    }


    // [TableName] ::= [PlainText]
    public static boolean parseTableName() {
        if (tokens.size() > current_token_index && parsePlainText(tokens.get(current_token_index))) {
            current_token_index++;
            return true;
        }
        return false;
    }




    //  === Methods below handle networking aspects of the project - you will not need to change these ! ===

    public void blockingListenOn(int portNumber) throws IOException {
        try (ServerSocket s = new ServerSocket(portNumber)) {
            System.out.println("Server listening on port " + portNumber);
            while (!Thread.interrupted()) {
                try {
                    blockingHandleConnection(s);
                } catch (IOException e) {
                    System.err.println("Server encountered a non-fatal IO error:");
                    e.printStackTrace();
                    System.err.println("Continuing...");
                }
            }
        }
    }

    private void blockingHandleConnection(ServerSocket serverSocket) throws IOException {
        try (Socket s = serverSocket.accept();
        BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()))) {

            System.out.println("Connection established: " + serverSocket.getInetAddress());
            while (!Thread.interrupted()) {
                String incomingCommand = reader.readLine();
                System.out.println("Received message: " + incomingCommand);
                String result = handleCommand(incomingCommand);
                writer.write(result);
                writer.write("\n" + END_OF_TRANSMISSION + "\n");
                writer.flush();
            }
        }
    }
}
