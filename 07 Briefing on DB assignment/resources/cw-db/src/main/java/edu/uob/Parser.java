package edu.uob;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Parser {

    ArrayList<String> tokens;
    public int current_token_index;

    static String fileSeparator = File.separator;

    private int numberAttributes;

    static String currentDatabase = "testing";

    public Parser(ArrayList<String> token, Integer current_token_index){
        this.tokens = token;
        this.current_token_index = current_token_index;
    }

    public boolean parse() throws Exception {
        if(parseCommand()){
            System.out.print("Parsed Okay");
            return true;
        }else{
            System.out.print("Parse Fail");
        }
        return false;
    }


    //<Command>         ::=  <CommandType> ";"
    private boolean parseCommand() throws Exception {
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
    public boolean parseCommandType() throws Exception{

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
                current_token_index++;
                if(parseSelect()){
                    parse = true;
                }
                break;

            case "alter":
                current_token_index++;
                if(parseAlteration()){
                    parse= true;
                }
                break;

            case "insert":
                current_token_index++;
                if(parseInsert()){
                    parse= true;
                }
                break;

            case "join":
                current_token_index++;
                if(parseJoin()){
                    parse=true;
                }
                break;

            case "drop":
                current_token_index++;
                if(parseDrop()){
                    parse=true;
                }
                break;

            case "use":
                current_token_index++;
                if(parseUse()){
                    parse=true;
                }
                break;

            case "delete":
                current_token_index++;
                if(parseDelete()) {
                    parse=true;
                }
                break;
            default:
                throw new Exception("Invalid command typed :" + tokens.get(current_token_index));
        }

        if(parse == true){
            return true;
        }

        return false;
    }

    private boolean parseInsert() {

        if(!tokens.get(current_token_index).equalsIgnoreCase("Into")){
            return false;
        }

        current_token_index++;

        if(!parseTableName()){
            return false;
        }

        current_token_index++;

        if(!tokens.get(current_token_index).equalsIgnoreCase("Values")){
            return false;
        }

        current_token_index++;


        if(tokens.get(current_token_index).equalsIgnoreCase("(")){
            current_token_index++;
            if(!parseValueList()){
                return false;
            }

            if(tokens.get(current_token_index).equalsIgnoreCase(")")){
                current_token_index++;
            }
        }

        return true;
    }



    private boolean parseValueList() {
        // Check if there is at least one Value
        if (!parseValueLiteral()) {
            return false;
        }

        // Check if there are additional Values separated by commas
        while (tokens.get(current_token_index).equals(",")) {
            // Consume the comma token
            current_token_index++;

            // Check if there is another Value after the comma
            if (!parseValueLiteral()) {
                return false;
            }
        }

        // If we reached here, the ValueList is valid
        return true;
    }


    private boolean parseDelete() {

        if(!tokens.get(current_token_index).equalsIgnoreCase("From")){
            return false;
        }

        current_token_index++;

        if(!parseTableName()){
            return false;
        }

        current_token_index++;

        if(!tokens.get(current_token_index).equalsIgnoreCase("Where")){
            return false;
        }

        current_token_index++;

        if(!parseCondition()){
            return false;
        }

        return true;
    }

    private boolean parseUse() {

        if(parseDatabaseName()){

            /*INTERPRETER--------------------------*/
            currentDatabase = tokens.get(current_token_index);
            current_token_index++;
            /*-------------------------------------*/
            return true;
        }

        return false;

    }

    //<Drop>            ::=  "DROP DATABASE " [DatabaseName] | "DROP TABLE " [TableName]
    private boolean parseDrop() {

        if(tokens.get(current_token_index).equalsIgnoreCase("database")){
            current_token_index++;
            if(parseDatabaseName()){
                current_token_index++;
                if (tokens.get(current_token_index).equals(";")) {
                    return true;
                }
            }
        }else if(tokens.get(current_token_index).equalsIgnoreCase("table")){
            current_token_index++;
            if(parseTableName()){
                current_token_index++;
                if (tokens.get(current_token_index).equals(";")) {
                    return true;
                }
            }
        }

        return false;
    }

    //<Join>            ::=  "JOIN " [TableName] " AND " [TableName] " ON " [AttributeName] " AND " [AttributeName]
    private boolean parseJoin() {

        if(!parseTableName()){
            return false;
        }

        current_token_index++;

        if (!tokens.get(current_token_index).equals("AND")){
            return false;
        }

        current_token_index++;

        if (!parseTableName()){
            return false;
        }

        current_token_index++;

        if (!tokens.get(current_token_index).equals("ON")){
            return false;
        }

        current_token_index++;

        if(parseAttributeName() == null){
            return false;
        }

        if (!tokens.get(current_token_index).equals("AND")){
            return false;
        }

        current_token_index++;

        if(parseAttributeName() == null){
            return false;
        }

        // Check for semicolon at the end
        if (tokens.get(current_token_index).equals(";")) {
            return true;
        }


        return false;
    }

    //<Alter>           ::=  "ALTER TABLE " [TableName] " " [AlterationType] " " [AttributeName]
    private boolean parseAlteration() throws IOException {
        String val;
        if (!tokens.get(current_token_index).equalsIgnoreCase("TABLE")) {
            return false;
        }
        current_token_index++;

        if (!parseTableName()) {
            return false;
        }

        current_token_index++;

        if (!parseAlterationType(tokens.get(current_token_index))) {
            return false;
        }

        current_token_index++;
        val = parseAttributeName();

        if (val  == null) {
            return false;
        }

        // Check for semicolon at the end
        if (tokens.get(current_token_index).equals(";")) {
            return true;
        }

        return false;
    }

    private boolean parseSelect() {

        ArrayList<String> attributes = parseWildAttributes();
        if (attributes == null) {
            return false;
        }

        if (!tokens.get(current_token_index).equals("FROM")) {
            return false;
        }
        current_token_index++;

        if (!parseTableName()) {
            return false;
        }

        current_token_index++;

        if (tokens.size() > current_token_index && tokens.get(current_token_index).equals("WHERE")) {
            // optional WHERE clause
            current_token_index++;
            return parseCondition();
        }

        if(tokens.get(current_token_index).equals(";")){
            return true;
        }

        return false;
    }



    private ArrayList<String> parseWildAttributes() {
        if (tokens.get(current_token_index).equals("*")) {
            // consume the "*" token
            current_token_index++;
            ArrayList<String> attributes = new ArrayList<>();
            attributes.add("*");
            return attributes;
        } else {
            return parseAttributeList();
        }
    }


    //<Create>          ::=  <CreateDatabase> | <CreateTable>
    private boolean parseCreate() throws IOException {

        // <CreateDatabase>  ::=  "CREATE DATABASE " [DatabaseName]
        if (tokens.get(current_token_index).equalsIgnoreCase("database")) {
            current_token_index++;
            if(parseDatabaseName()){

                /*INTERPRETER-----------------------------------------------------------------------------------------*/
                File database_create=new File(".."+fileSeparator+"cw-db"+fileSeparator+"databases"+fileSeparator+tokens.get(current_token_index));

                if(!database_create.exists()){
                    database_create.mkdir();
                }else{
                    System.out.println("Database is already created");
                }

                current_token_index++;
                if(tokens.get(current_token_index).equals(";")){
                    return true;
                }
            }
            /*--------------------------------------------------------------------------------------------------------*/




            // <CreateTable>     ::=  "CREATE TABLE " [TableName] | "CREATE TABLE " [TableName] "(" <AttributeList> ")"
        } else if (tokens.get(current_token_index).equalsIgnoreCase("table")) {
            current_token_index++;

            /*INTERPRETER-----------------------------------------------------------------------------------------*/

            FileWriter createTable = null;

            /*----------------------------------------------------------------------------------------------------*/


            if (!parseTableName()) {
                return false;
            }

            /*INTERPRETER-----------------------------------------------------------------------------------------*/

            createTable = new FileWriter(".." + fileSeparator + "cw-db" + fileSeparator + "databases" + fileSeparator + currentDatabase + fileSeparator + tokens.get(current_token_index)+".tab");
            BufferedWriter bw = new BufferedWriter(createTable);

            /*----------------------------------------------------------------------------------------------------*/

            if (tokens.contains("(")) {
                current_token_index = tokens.indexOf("(") + 1;  // identify first attribute
                int endIndex = tokens.indexOf(")");

                // Parse the attribute list
                ArrayList<String> attributes = parseAttributeList();

                // Print the attributes
                if (attributes.isEmpty() != true) {
                    System.out.println("Attributes:");
                    for (String attribute : attributes) {
                        System.out.println(attribute);
                    }
                    current_token_index++;
                } else {
                    System.out.println("No attributes were found");
                }

                /*INTERPRETER---------------------------------------------------------------------------------------------*/

                if(attributes.size() > 0){

                    bw.write("id");
                    bw.write("\t");


                    for(int i = 0; i < attributes.size(); i++){
                        bw.write(attributes.get(i));
                        bw.write("\t");
                    }

                }
                /*--------------------------------------------------------------------------------------------------------*/

            }

            bw.close();

        }



        if(tokens.contains("(") == false){
            current_token_index++;
        }

        if(tokens.get(current_token_index).equals(";")){
            return true;
        }

        return false;
    }

    //<AttributeList>   ::=  [AttributeName] | [AttributeName] "," <AttributeList>
    private ArrayList<String> parseAttributeList() {
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

        numberAttributes = attributes.size();

        return attributes;
    }


    // [AttributeName]   ::=  [PlainText] | [TableName] "." [PlainText]
    private  String parseAttributeName() {
        String attributeName = "";

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
                //current_token_index++;
            }
        } else {
            return null;
        }

        return attributeName;
    }


    //[Space]           ::=  " "
    private boolean parseSpace(String token) {
        if(token.equals(" ")){
            return true;
        }
        return false;
    }

    //[Digit]           ::=  "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9"
    private boolean parseDigit(String token) {
        if(token.matches("[0-9]+")){
            return true;
        }
        return false;
    }

    //[Symbol]          ::=  "!" | "#" | "$" | "%" | "&" | "(" | ")" | "*" | "+" | "," | "-" | "." | "/" | ":" | ";" | ">" | "=" | "<" | "?" | "@" | "[" | "\" | "]" | "^" | "_" | "`" | "{" | "}" | "~"
    private boolean parseSymbol(String token) {
        String regex = "[!#$%&()*+,-./:;>=<?@\\[\\]\\^_`\\{\\}|~]*";
        return token.matches(regex);
    }

    //[Uppercase]       ::=  "A" | "B" | "C" | "D" | "E" | "F" | "G" | "H" | "I" | "J" | "K" | "L" | "M" | "N" | "O" | "P" | "Q" | "R" | "S" | "T" | "U" | "V" | "W" | "X" | "Y" | "Z"
    private boolean parseUppercase(String token) {
        return token.matches("[A-Z]");
    }

    //[Lowercase]       ::=  "a" | "b" | "c" | "d" | "e" | "f" | "g" | "h" | "i" | "j" | "k" | "l" | "m" | "n" | "o" | "p" | "q" | "r" | "s" | "t" | "u" | "v" | "w" | "x" | "y" | "z"
    private boolean parseLowercase(String token) {
        return token.matches("[a-z]");
    }

    //[Letter]          ::=  [Uppercase] | [Lowercase]
    private boolean parseLetter(String token) {
        return parseUppercase(token) || parseLowercase(token);
    }

    private static boolean parseAlterationType(String token) {
        if(token.equalsIgnoreCase("ADD") ||token.equalsIgnoreCase("DROP")){
            return true;
        }

        return false;
    }

    //[PlainText]       ::=  [Letter] | [Digit] | [PlainText] [Letter] | [PlainText] [Digit]
    private boolean parsePlainText(String token) {

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
    public boolean parseUpdate() throws Exception {
        // Match [TableName] rule
        if (!parseTableName()) {
            return false;
        }

        current_token_index++;

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
        if (!parseCondition()) {
            return false;
        }

        // Match ";" token
        if (!tokens.get(current_token_index).equals(";")) {
            return false;
        }

        return true;
    }

    //<Condition>       ::=  "(" <Condition> [BoolOperator] <Condition> ")" | <Condition> [BoolOperator] <Condition> | "(" [AttributeName] [Comparator] [Value] ")" | [AttributeName] [Comparator] [Value]
    private boolean parseCondition() {
        if (tokens.get(current_token_index).equals("(")) {
            // Condition enclosed in parentheses with nested conditions and boolean operators
            current_token_index++;
            boolean left_condition_valid = parseCondition();
            if (!left_condition_valid) {
                return false;
            }
            String bool_operator = tokens.get(current_token_index);
            if (!parseBoolOperator(bool_operator)) {
                return false;
            }
            current_token_index++;
            boolean right_condition_valid = parseCondition();
            if (!right_condition_valid) {
                return false;
            }
            if (!tokens.get(current_token_index).equals(")")) {
                return false;
            }
            current_token_index++;
            return true;
        } else if (parseAttributeName() != null) {
            // Condition with attribute name, comparator, and value
            if (!parseComparator(tokens.get(current_token_index))) {
                return false;
            }
            current_token_index++;
            if (!parseValueLiteral()) {
                return false;
            }
            return true;
        } else {
            return false;
        }
    }


    private boolean parseComparator(String token){
        if(token.equalsIgnoreCase("==") || token.equalsIgnoreCase("!=")|| token.equalsIgnoreCase(">")|| token.equalsIgnoreCase("<")|| token.equalsIgnoreCase("<=")|| token.equalsIgnoreCase(">=")|| token.equalsIgnoreCase("LIKE")){
            return true;
        }
        return false;
    }

    private boolean parseBoolOperator(String token){
        if(token.equalsIgnoreCase("AND") || token.equalsIgnoreCase("OR")){
            return true;
        }
        return false;
    }


    //<NameValueList>   ::=  <NameValuePair> | <NameValuePair> "," <NameValueList>
    private boolean parseNameValueList() {
        // Parse the first NameValuePair
        if (!parseNameValuePair()) {
            return false;
        }

        //current_token_index++;
        // Check if there are more NameValuePairs
        if (tokens.get(current_token_index).equals(",")) {
            // Parse the comma separator
            current_token_index++;

            // Parse the remaining NameValuePairs recursively
            return parseNameValueList();
        }

        // If there are no more NameValuePairs, the NameValueList is valid
        return true;
    }

    //<NameValuePair>   ::=  [AttributeName] "=" [Value]
    private boolean parseNameValuePair() {

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
    private boolean parseValueLiteral() {
        String current_token = tokens.get(current_token_index);

        if (current_token.equals("NULL")) {
            // If the current token is "NULL", the value literal is valid
            return true;
        }

        if (current_token.equals("'")) {
            // If the current token is a single quote, it's the start of a string literal
            current_token_index++;

            if(tokens.get(current_token_index).equals("'")){
                //System.out.print("Parse Val literal "+ current_token_index);
                return true;
            }

            current_token_index++;

            if(tokens.get(current_token_index).equals("'")) {
                // If the string literal is valid and we've reached the end single quote, the value literal is valid
                current_token_index++;
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
            current_token_index++;
            return true;
        }


        // If we didn't return true above, the value literal is not valid
        return false;
    }

    //[IntegerLiteral]  ::=  [DigitSequence] | "-" [DigitSequence] | "+" [DigitSequence]
    private boolean parseIntegerLiteral() {
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
    private boolean parseFloatLiteral() {
        int initialIndex = current_token_index;
        if (current_token_index < tokens.size() && (tokens.get(current_token_index).equals("+") || tokens.get(current_token_index).equals("-"))) {
            current_token_index++;
        }
        if (current_token_index < tokens.size() && parseDigitSequence()) {
            if (current_token_index < tokens.size() && tokens.get(current_token_index).equals(".")) {
                current_token_index++;
                if (current_token_index < tokens.size() && parseDigitSequence()) {
                    current_token_index--;
                    return true;
                }
            }
        }
        current_token_index = initialIndex;
        return false;
    }

    //[DigitSequence]   ::=  [Digit] | [Digit] [DigitSequence]
    private boolean parseDigitSequence() {
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
    private boolean parseBooleanLiteral() {
        if(tokens.get(current_token_index).equalsIgnoreCase("TRUE") || tokens.get(current_token_index).equalsIgnoreCase("FALSE")){
            return true;
        }

        return false;
    }

    //String command = "UPDATE marks SET mark = 38 WHERE name == 'Clive';";



    //[CharLiteral]     ::=  [Space] | [Letter] | [Symbol] | [Digit]
    private boolean parseCharLiteral() {

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
    public boolean parseTableName() {
        if (tokens.size() > current_token_index && parsePlainText(tokens.get(current_token_index))) {
            return true;
        }
        return false;
    }

    // [DatabaseName] ::= [PlainText]
    public boolean parseDatabaseName() {
        if (tokens.size() > current_token_index && parsePlainText(tokens.get(current_token_index))) {
            return true;
        }
        return false;
    }



}
