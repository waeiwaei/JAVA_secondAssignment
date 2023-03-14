package edu.uob;

import java.util.ArrayList;

public class DBCmd {
    ArrayList<Condition> conditions;
    ArrayList<String> TableNames;
    ArrayList<String> colNames;
    String DBName;
    ArrayList<NameValue> nameValueList;
    ArrayList<String> values;
    String alterationType;
    String commandtype;

    ArrayList<Join> join;

    public DBCmd(){
    }
}
