package edu.uob;

import java.io.IOException;
import java.util.ArrayList;

public class Parser {

    private DBCmd dbstate;

    int opening_brackets;

    int closing_brackets;


    public Parser(){
        dbstate = new DBCmd();
    }

    public DBCmd parse(Tokenizer tk) throws Exception {
        parseCommand(tk);
        return dbstate;
    }


    //<Command>         ::=  <CommandType> ";"
    private void parseCommand(Tokenizer tk) throws Exception {
        // Try to match <CommandType> rule
        parseCommandType(tk);

        String token = tk.getCurrentToken();

        if(token.equals(")")){
            token = tk.getCurrentToken();
        }

        // Match ";" token
        if(!token.equals(";")){
            throw new Exception("Parse failed! - parseCommand");
        }

        if(token.equals(";") && tk.hasMoreTokens()){
            throw new Exception("Parse failed! - only one command per line");
        }
    }

    //<CommandType>     ::=  <Use> | <Create> | <Drop> | <Alter> | <Insert> | <Select> | <Update> | <Delete> | <Join>
    public void parseCommandType(Tokenizer tk) throws Exception{

        switch(tk.nextToken().toLowerCase()){
            case "create":
                dbstate.commandtype = tk.getCurrentToken();
                parseCreate(tk);
                break;

            case "update":
                dbstate.commandtype = tk.getCurrentToken();
                parseUpdate(tk);
                break;

            case "select":
                dbstate.commandtype = tk.getCurrentToken();
                parseSelect(tk);
                break;

            case "alter":
                dbstate.commandtype = tk.getCurrentToken();
                parseAlteration(tk);
                break;

            case "insert":
                dbstate.commandtype = tk.getCurrentToken();
                parseInsert(tk);
                break;

            case "join":
                dbstate.commandtype = tk.getCurrentToken();
                parseJoin(tk);
                break;

            case "drop":
                dbstate.commandtype = tk.getCurrentToken();
                parseDrop(tk);
                break;

            case "use":
                dbstate.commandtype = tk.getCurrentToken();
                parseUse(tk);
                break;

            case "delete":
                dbstate.commandtype = tk.getCurrentToken();
                parseDelete(tk);
                break;

            default:
                throw new Exception("Invalid command typed :" + tk.nextToken());
        }

    }


    //<Create>          ::=  <CreateDatabase> | <CreateTable>
    private void parseCreate(Tokenizer tk) throws Exception {

        if(!tk.hasMoreTokens()){
            throw new Exception("Parse failed - Create");
        }

        String token = tk.nextToken();

        // <CreateDatabase>  ::=  "CREATE DATABASE " [DatabaseName]
        if (token.equalsIgnoreCase("database")) {
            dbstate.commandtype += " " + tk.getCurrentToken();
            parseDatabaseName(tk);

            // <CreateTable>     ::=  "CREATE TABLE " [TableName] | "CREATE TABLE " [TableName] "(" <AttributeList> ")"
        } else if (token.equalsIgnoreCase("table")) {
            dbstate.commandtype += " " + tk.getCurrentToken();

            parseTableName(tk);

            if (tk.contains("(")) {
                tk.setTokenIndex(tk.indexOf("("));
                dbstate.colNames = new ArrayList<String>();
                // Parse the attribute list
                parseAttributeList(tk);
                //tk.setTokenIndex(tk.getCurrent_token_index()+1);
                tk.nextToken();
            }
        }

        //tk.setTokenIndex(tk.getCurrent_token_index()+1);
        tk.nextToken();
    }

    // <Update>          ::=  "UPDATE " [TableName] " SET " <NameValueList> " WHERE " <Condition>
    public boolean parseUpdate(Tokenizer tk) throws Exception {

        if(!tk.hasMoreTokens()){
            throw new Exception("Parse Failed - parseUpdate");
        }

        parseTableName(tk);


        // Match "SET" token
        if (!tk.nextToken().equalsIgnoreCase("set")) {
            throw new Exception("Parse Failed - parseUpdate");
        }

        dbstate.nameValueList = new ArrayList<NameValue>();
        parseNameValueList(tk);

        // Match "WHERE" token
        if (!tk.nextToken().equalsIgnoreCase("where")) {
            throw new Exception("Parse Failed - parseUpdate");
        }

        // Match <Condition> rule
        dbstate.conditions = new ArrayList<Condition>();
        dbstate.conditions.add(parseCondition(tk));

        if(tk.getCurrentToken().equals(")")){
            tk.nextToken();
        }

        return true;
    }


    private void parseSelect(Tokenizer tk) throws Exception {

        parseWildAttributes(tk);

        if (!tk.nextToken().equalsIgnoreCase("From")) {
            throw new Exception("Parse Failed - parseSelect");
        }

        parseTableName(tk);

        if(tk.nextToken().equals(";")){
            return;
        }

        if (tk.getCurrentToken().equalsIgnoreCase("Where")) {
            dbstate.conditions = new ArrayList<Condition>();
            dbstate.conditions.add(parseCondition(tk));

        } else{
            throw new Exception("Parse Failed - parseSelect");
        }

        if(tk.getCurrentToken().equals(")")){
            tk.nextToken();
        }

        return;
    }

    //<Alter>           ::=  "ALTER TABLE " [TableName] " " [AlterationType] " " [AttributeName]
    private void parseAlteration(Tokenizer tk) throws Exception {

        if(!tk.hasMoreTokens()){
            throw new Exception("Parse Failed - parseAlteration");
        }

        if (!tk.nextToken().equalsIgnoreCase("TABLE")) {
            throw new Exception("Parse Failed - parseAlteration");
        }

        parseTableName(tk);
        parseAlterationType(tk.nextToken());


        dbstate.colNames = new ArrayList<String>();
        dbstate.colNames.add(parseAttributeName(tk));

        //tk.setTokenIndex(tk.getCurrent_token_index()+2);
        tk.nextToken();
    }


    private void parseInsert(Tokenizer tk) throws Exception{

        if(!tk.nextToken().equalsIgnoreCase("Into")){
            throw new Exception("Parser Fail - parseInsert");
        }

        parseTableName(tk);

        if(!tk.nextToken().equalsIgnoreCase("Values")){
            throw new Exception("Parser Fail - parseInsert");
        }


        if(tk.nextToken().equals("(")){

            if(tk.nextToken().equals("'")){
                tk.previousToken();
            }

            dbstate.values = new ArrayList<String>();
            parseValueList(tk);
        }

        if(!tk.getCurrentToken().equalsIgnoreCase(")")){
            throw new Exception("Parser Fail - parseInsert");
        }

        //tk.setTokenIndex(tk.getCurrent_token_index()+1);
        tk.nextToken();
    }



    //<Join>            ::=  "JOIN " [TableName] " AND " [TableName] " ON " [AttributeName] " AND " [AttributeName]
    private void parseJoin(Tokenizer tk) throws Exception {

        if(!tk.hasMoreTokens()){
            throw new Exception("Parse Failed - parseJoin");
        }

        dbstate.join = new ArrayList<Join>();

        parseTableName(tk);

        dbstate.join.add(new Join(dbstate.TableNames.get(0)));


        if (!tk.nextToken().equalsIgnoreCase("AND")){
            throw new Exception("Parse Failed - parseJoin");
        }

        parseTableName(tk);

        dbstate.join.add(new Join(dbstate.TableNames.get(0)));


        if (!tk.nextToken().equalsIgnoreCase("ON")){
            throw new Exception("Parse Failed - parseJoin");
        }

        //table 1
        dbstate.join.get(0).attributes = parseAttributeName(tk);

        //tk.setTokenIndex(tk.getCurrent_token_index()+1);
        tk.nextToken();

        if (!tk.nextToken().equalsIgnoreCase("AND")){
            throw new Exception("Parse Failed - parseJoin");
        }

        //table 2
        dbstate.join.get(1).attributes = parseAttributeName(tk);

        //tk.setTokenIndex(tk.getCurrent_token_index()+2);
        tk.nextToken();
        tk.nextToken();

    }





    //<Drop>            ::=  "DROP DATABASE " [DatabaseName] | "DROP TABLE " [TableName]
    private void parseDrop(Tokenizer tk) throws Exception {

        String token = tk.nextToken();

        if(token.equalsIgnoreCase("database")){
            dbstate.commandtype +=" " + tk.getCurrentToken();
            parseDatabaseName(tk);

        }else if(token.equalsIgnoreCase("table")){
            dbstate.commandtype += " " + tk.getCurrentToken();
            parseTableName(tk);

        }

        //tk.setTokenIndex(tk.getCurrent_token_index()+1);
        tk.nextToken();
    }


    private void parseUse(Tokenizer tk) throws Exception {

        parseDatabaseName(tk);
        //tk.setTokenIndex(tk.getCurrent_token_index() + 1);
        tk.nextToken();
    }


    private void parseDelete(Tokenizer tk) throws Exception {

        if(!tk.nextToken().equalsIgnoreCase("From")){
            throw new Exception("Parse Failed - parseDelete");
        }

        parseTableName(tk);

        if(!tk.nextToken().equalsIgnoreCase("Where")){
            throw new Exception("Parse Failed - parseDelete");
        }

        dbstate.conditions = new ArrayList<Condition>();
        dbstate.conditions.add(parseCondition(tk));

        if(tk.getCurrentToken().equals(")")){
            tk.nextToken();
        }

    }


    private void parseValueList(Tokenizer tk) throws Exception{
        // Check if there is at least one Value
        dbstate.values.add(parseValueLiteral(tk));

        if(tk.nextToken().equals(",")){
            if(tk.getCurrentToken().equals(")")){
                return;
            }
            //tk.setTokenIndex(tk.getCurrent_token_index()+1);

            if(tk.nextToken().equals("'")){
                tk.previousToken();
            }


            parseValueList(tk);

        }

    }



    private void parseWildAttributes(Tokenizer tk) throws Exception {
        dbstate.colNames = new ArrayList<String>();

        if (tk.nextToken().equals("*")) {
            // consume the "*" token
            dbstate.colNames.add("*");

        } else {
            //tk.setTokenIndex(tk.getCurrent_token_index() - 1);
            tk.previousToken();
            parseAttributeList(tk);
        }
    }


    //<AttributeList>   ::=  [AttributeName] | [AttributeName] "," <AttributeList>
    private void parseAttributeList(Tokenizer tk) throws Exception {

        dbstate.colNames.add(parseAttributeName(tk).toLowerCase());

        if(tk.nextToken().equals(",")){
            parseAttributeList(tk);
        }else{
            //tk.setTokenIndex(tk.getCurrent_token_index() - 1);
            tk.previousToken();
            return;
        }
    }


    // [AttributeName]   ::=  [PlainText] | [TableName] "." [PlainText]
    private String parseAttributeName(Tokenizer tk)throws Exception {
        String attributeName = "";

        // Check if the attribute name is a plain text or a table name and plain text
        String x = tk.nextToken();
        x = parsePlainText(x);
        attributeName += x;


        String nextToken = tk.nextToken();

        if (nextToken.equals(".")) {
            attributeName += ".";

            if(!tk.hasMoreTokens())
                throw new Exception("Parse failed!");

            attributeName+=parsePlainText(tk.nextToken());

        }else{

            if(dbstate.commandtype.equalsIgnoreCase("JOIN")){
                //tk.setTokenIndex(tk.getCurrent_token_index() - 2);
                tk.previousToken();
                tk.previousToken();
            }else{
                //tk.setTokenIndex(tk.getCurrent_token_index() - 1);
                tk.previousToken();
            }

        }

        return attributeName;
    }

    private void parseAlterationType(String token) throws Exception {

        if(token.equalsIgnoreCase("ADD") || token.equalsIgnoreCase("DROP")){
            dbstate.alterationType = new String();
            dbstate.alterationType = token;
        }else{
            throw new Exception("Parse Failed - parseAlterationType");
        }

    }

    //[PlainText]       ::=  [Letter] | [Digit] | [PlainText] [Letter] | [PlainText] [Digit]
    private String parsePlainText(String token) throws Exception {

        if (token == null) {
            throw new Exception("Parser Failed - parsePlainText");
        }

        for (int i = 0; i < token.length(); i++) {
            char c = token.charAt(i);
            if (!Character.isLetter(c) && !Character.isDigit(c)) {
                throw new Exception("Parse failed!");
            }
        }

        return token;
    }



    private Condition parseCondition(Tokenizer tk) throws Exception {
        Condition result = null;

        if (tk.hasMoreTokens()) {
            String token = tk.nextToken();
            if (token.equals("(")) {
                // parse sub-expression
                opening_brackets++;
                result = parseCondition(tk);

            } else {

                tk.previousToken();
                String attributeName = parseAttributeName(tk);
                String comparator = parseComparator(tk.nextToken());

                if(tk.nextToken().equals("'")){
                    tk.previousToken();
                }

                String value = parseValueLiteral(tk);

                Condition condition = new Condition();
                condition.attributeName = attributeName;
                condition.comparator = comparator;
                condition.value = value;

                if (tk.hasMoreTokens()) {
                    String boolOperator = parseBoolOperator(tk.nextToken());

                    if(boolOperator.isEmpty()){
                        result = condition;
                        return result;
                    }

                    Condition nextCondition = parseCondition(tk);

                    // combine expressions
                    Condition combinedCondition = new Condition();
                    combinedCondition.cnd1 = condition;
                    combinedCondition.cnd2 = nextCondition;
                    combinedCondition.boolOperator = boolOperator;

                    result = combinedCondition;
                } else {
                    result = condition;
                }
            }
        }

        if(tk.getCurrentToken().equals(")")){
            closing_brackets++;
        }

        if(tk.getCurrentToken().equals(";")){
            if(opening_brackets != closing_brackets){
                throw new Exception("Opening and closing Brackets missing");
            }
            return result;
        }

        String tok = tk.nextToken();
        if(tok.equalsIgnoreCase("AND") || tok.equalsIgnoreCase("OR")){
            Condition cond = new Condition();
            cond.cnd1 = result;
            cond.boolOperator = tk.getCurrentToken();
            cond.cnd2 = parseCondition(tk);
            result = cond;
        }

        if (result != null) {
            return result;
        } else {
            throw new Exception("Parse Failed - parseCondition: invalid syntax");
        }
    }


    private String parseComparator(String token) throws Exception{
        if(token.equalsIgnoreCase("==") || token.equalsIgnoreCase("!=")|| token.equalsIgnoreCase(">")|| token.equalsIgnoreCase("<")|| token.equalsIgnoreCase("<=")|| token.equalsIgnoreCase(">=")|| token.equalsIgnoreCase("LIKE")){
            return token;
        }else{
            throw new Exception("Parse failed - parseComparator");
        }
    }

    private String parseBoolOperator(String token) throws Exception{
        if(token.equalsIgnoreCase("AND") || token.equalsIgnoreCase("OR")){
            return token;
        }else{
            return "";
        }
    }


    //<NameValueList>   ::=  <NameValuePair> | <NameValuePair> "," <NameValueList>
    private boolean parseNameValueList(Tokenizer tk) throws Exception {

        // Parse the first NameValuePair
        if(!tk.hasMoreTokens()){
            throw new Exception("Parsed Fail - parseNameValueList");
        }

        dbstate.nameValueList.add(parseNameValuePair(tk));

        // Check if there are more NameValuePairs
        if (tk.nextToken().equals(",")) {

            // Parse the remaining NameValuePairs recursively
            return parseNameValueList(tk);
        }else{
            //tk.setTokenIndex(tk.getCurrent_token_index()-1);
            tk.previousToken();
        }

        // If there are no more NameValuePairs, the NameValueList is valid
        return true;
    }

    //<NameValuePair>   ::=  [AttributeName] "=" [Value]
    private NameValue parseNameValuePair(Tokenizer tk) throws Exception {

        NameValue nameVal = new NameValue();
        nameVal.Name = new String();
        nameVal.Value = new String();

        if(!tk.hasMoreTokens()){
            throw new Exception("Parse Failed - ParseNameValuePair");
        }

        nameVal.Name =parseAttributeName(tk);

        //tk.setTokenIndex(tk.getCurrent_token_index()+ 1);
        //tk.nextToken();

        if(!tk.nextToken().equals("=")){
            throw new Exception("Parse Failed - ParseNameValuePair");
        }

        if(tk.nextToken().equals("'")){
            tk.previousToken();
        }

        nameVal.Value = parseValueLiteral(tk);

        return nameVal;
    }

    // [Value]           ::=  "'" [StringLiteral] "'" | [BooleanLiteral] | [FloatLiteral] | [IntegerLiteral] | "NULL"
    private String parseValueLiteral(Tokenizer tk) throws Exception {

        int currentTokenIndex = tk.getCurrent_token_index();
        String current_token = "";

        if (tk.nextToken().equals("'")) {

            if(tk.nextToken().equals("'")){
                current_token = "''";
                return current_token;
            }else{

                tk.previousToken();
            }

            while(!tk.nextToken().equals("'") && tk.hasMoreTokens()) {
                current_token += tk.getCurrentToken();
                if(!tk.getCurrentToken().equals(".")){
                    current_token += " ";
                }
            }

            if(current_token.contains(".")){
                current_token=current_token.replaceAll("\\s+", "");
            }

            current_token = current_token.trim();

            if(!tk.hasMoreTokens()){
                throw new Exception("Parse Fail - paseValueLiteral");
            }

            return current_token;

        }else{
            tk.setTokenIndex(currentTokenIndex);
        }

        current_token = parseBooleanLiteral(tk.getCurrentToken());

        if(!current_token.isEmpty()){
            return current_token;
        }

        current_token = parseFloatLiteral(tk);

        if(!current_token.isEmpty()){
            return current_token;
        }

        current_token = parseIntegerLiteral(tk);
        if(!current_token.isEmpty()){
            return current_token;
        }

        if(current_token.isEmpty()){
            throw new Exception("Value provided does not conform to value grammar");
        }

        return current_token;
    }

    //[IntegerLiteral]  ::=  [DigitSequence] | "-" [DigitSequence] | "+" [DigitSequence]
    private String parseIntegerLiteral(Tokenizer tk) throws Exception {

        if(!tk.hasMoreTokens()){
            throw new Exception("Parsed Fail - parseFloatLiteral");
        }

        String integerLiteral = "";

        //int initialIndex = current_token_index;
        if ((tk.getCurrentToken().equals("+") || tk.getCurrentToken().equals("-"))) {
            integerLiteral += tk.getCurrentToken();
            //tk.setTokenIndex(tk.getCurrent_token_index()+1);
            tk.nextToken();
        }

        String subIntegerLiteral = "";

        subIntegerLiteral = parseDigitSequence(tk.getCurrentToken());

        if (subIntegerLiteral != null) {
            integerLiteral += subIntegerLiteral;
            return integerLiteral;

        }else{
            integerLiteral = "";
            return integerLiteral;
        }

    }

    // [FloatLiteral]    ::=  [DigitSequence] "." [DigitSequence] | "-" [DigitSequence] "." [DigitSequence] | "+" [DigitSequence] "." [DigitSequence]
    private String parseFloatLiteral(Tokenizer tk) throws Exception {

        if(!tk.hasMoreTokens()){
            throw new Exception("Parsed Fail - parseFloatLiteral");
        }

        String float_literal = "";
        String subfloat_literal = "";

        int initialIndex = tk.getCurrent_token_index();

        if (tk.getCurrentToken().equals("+") || tk.getCurrentToken().equals("-")) {
            float_literal += tk.getCurrentToken();
            //tk.setTokenIndex(tk.getCurrent_token_index()+1);
            tk.nextToken();
        }

        String first = parseDigitSequence(tk.getCurrentToken());

        if (first != null) {
            subfloat_literal = first;

            if (tk.nextToken().equals(".")) {
                subfloat_literal += tk.getCurrentToken();

                String second = parseDigitSequence(tk.nextToken());

                if (second != null) {
                    subfloat_literal += tk.getCurrentToken();
                }

            }else{
                tk.setTokenIndex(initialIndex);
                return "";
            }
        }else{
            return null;
        }

        float_literal += subfloat_literal;

        //we set the index back to the initial index as before
        //current_token_index = initialIndex;

        return float_literal;
    }

    //[DigitSequence]   ::=  [Digit] | [Digit] [DigitSequence]
    private String parseDigitSequence(String token) {

        try {
            Integer integerValue = Integer.parseInt(token);
            return token;
        }catch(NumberFormatException e){
            return "";
        }

    }

    //[BooleanLiteral]  ::=  "TRUE" | "FALSE"
    private String parseBooleanLiteral(String token) throws Exception {

        if(token.equalsIgnoreCase("TRUE") || token.equalsIgnoreCase("FALSE")){
            return token;
        }

        return "";
    }

    //String command = "UPDATE marks SET mark = 38 WHERE name == 'Clive';";


    // [TableName] ::= [PlainText]
    public void parseTableName(Tokenizer tk) throws Exception{

        if(!tk.hasMoreTokens()) {
            throw new Exception("Parse failed!");
        }

        String tblname = parsePlainText(tk.nextToken());
        dbstate.TableNames = new ArrayList<String>();
        dbstate.TableNames.add(tblname);
    }

    // [DatabaseName] ::= [PlainText]
    public void parseDatabaseName(Tokenizer tk) throws Exception{

        if(!tk.hasMoreTokens()){
            throw new Exception("Parse failed - DatabaseName");
        }

        dbstate.DBName= parsePlainText(tk.nextToken());

    }
}
