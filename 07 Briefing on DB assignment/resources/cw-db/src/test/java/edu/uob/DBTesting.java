package edu.uob;


import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;

public class DBTesting {

    static String fileSeparator = File.separator;
    static String storageFolderPath = (".."+fileSeparator+"cw-db"+fileSeparator+"databases"+fileSeparator);
    private DBServer server;


    // Create a new server _before_ every @Test
    @BeforeEach
    public void setup() {
        server = new DBServer();
    }

    // Random name generator - useful for testing "bare earth" queries (i.e. where tables don't previously exist)
    private String generateRandomName()
    {
        String randomName = "";
        for(int i=0; i<10 ;i++) randomName += (char)( 97 + (Math.random() * 25.0));
        return randomName;
    }

    private String sendCommandToServer(String command) {
        // Try to send a command to the server - this call will timeout if it takes too long (in case the server enters an infinite loop)
        return assertTimeoutPreemptively(Duration.ofMillis(1000), () -> { return server.handleCommand(command);},
                "Server took too long to respond (probably stuck in an infinite loop)");
    }


    //Test grammar - [ERROR] tag is returned for code that causes error
    @Test
    public void testGrammarError(){
        String response = sendCommandToServer("Create database test");
        assertTrue(response.contains("[ERROR]"), "Parse error - missing semicolon");

        response = sendCommandToServer("Create table;");
        assertTrue(response.contains("[ERROR]"), "Grammar error - table name not provided");

        response = sendCommandToServer("Select col1, col2, from tables;;");
        assertTrue(response.contains("[ERROR]"), "Grammar error - extra semicolon");

        response = sendCommandToServer("Include database test");
        assertTrue(response.contains("[ERROR]"), "Grammar error - not a keyword");

        response = sendCommandToServer("Delete from t## where A == B");
        assertTrue(response.contains("[ERROR]"), "Grammar error - table name must be [PlainText] ");

        response = sendCommandToServer("UPDATE name = bruce2 from table WHERE A == 2");
        assertTrue(response.contains("[ERROR]"), "Grammar error - value must be either stringliteral, boolean, float, integer or null ");

        response = sendCommandToServer("ALTER TABLE table1 DROP attribute1;JOIN table1 AND table2 on att1 and att 2;");
        assertTrue(response.contains("[ERROR]"), "Grammar error - multiple expressions");

        response = sendCommandToServer("UPDATE name = bruce2 from table WHERE A = 2");
        assertTrue(response.contains("[ERROR]"), "Grammar error - Comparator not detected in after Attribute");

        response = sendCommandToServer("DELETE attribute1 FROM marks where name == steve;");
        assertTrue(response.contains("[ERROR]"), "Grammar error - value (steve) should be a string literal ('steve')");

        response = sendCommandToServer("");
        assertTrue(response.contains("[ERROR]"), "Grammar error - cannot process empty query");

    }

    //test for Grammar where it parses correctly and carries out actions
    //create files, can check if folder, file directory exists
    //check if input values are as expected

    @Test
    public void testInvalidException(){

        String databaseName = generateRandomName();
        String databaseName1 = "Create";
        String attributeName = "Join";
        String tableName = "Delete";

        sendCommandToServer("Create database " + databaseName + ";");
        sendCommandToServer("use" + databaseName + ";");

        String response = sendCommandToServer("Create table testTable Values(name, "+attributeName+");");
        assertTrue(response.contains("[ERROR]"), "Interpreter error - unable to create table with attribute name as keyword");

        File f1 = new File(storageFolderPath+databaseName+fileSeparator+"testTable");
        assertFalse(f1.exists(), "Interpreter error - table name cannot be a keyword");


        response = sendCommandToServer("Create database "+ databaseName1 +";");
        assertTrue(response.contains("[ERROR]"), "Interpreter error - unable to create database with keyword");

        File f2 = new File(storageFolderPath+databaseName1);
        assertFalse(f2.exists(), "Interpreter error - database name cannot be a keyword");


        response = sendCommandToServer("Create table "+ tableName +";");
        assertTrue(response.contains("[ERROR]"), "Interpreter error - unable to create table name as keyword");

        File f3 = new File(storageFolderPath+databaseName+fileSeparator+tableName);
        assertFalse(f3.exists(), "Interpreter error - database name cannot be a keyword");


        //create a table and add attribute - keyword
        sendCommandToServer("use " + databaseName + ";");
        sendCommandToServer("Create table testingtable (name,age);");
        response = sendCommandToServer("ALTER TABLE testingtable ADD " + attributeName + ";");
        assertTrue(response.contains("[ERROR]"), "Interpreter error - unable to add attribute name as keyword");

        sendCommandToServer("drop database "+databaseName+";");

    }





    @Test
    public void testDrop(){
        String databaseName = generateRandomName();

        //drop table that exists
        String tableName = "testingtable";

        sendCommandToServer("create database "+databaseName+";");
        sendCommandToServer("use " + databaseName + ";");
        sendCommandToServer("create table "+ tableName +";");

        String response = sendCommandToServer("drop table " + tableName +";");
        assertTrue(response.contains("[OK]"), "Table was not deleted successfully");

        File f = new File(storageFolderPath+fileSeparator+databaseName+fileSeparator+tableName);
        assertTrue(!f.exists(), "Table was not dropped in the " + databaseName +" database directory properly");

        //drop database that exists
        response = sendCommandToServer("drop database " + databaseName +";");
        assertTrue(response.contains("[OK]"), "database not deleted successfully");

        File f1 = new File(storageFolderPath+fileSeparator+databaseName);
        assertTrue(!f1.exists(), "Table was not dropped in the " + databaseName +" database directory properly");

        //drop database - does not exist
        response = sendCommandToServer("drop table "+tableName+";");
        assertTrue(response.contains("[ERROR]"), "table does not exists and was deleted successfully");

        //drop table - does not exist
        response = sendCommandToServer("drop database "+databaseName+";");
        assertTrue(response.contains("[ERROR]"), "database does not exists and was deleted successfully");

    }



    @Test
    public void testCreate(){

        String databaseName = generateRandomName();

        /*        //create database*/
        String response = sendCommandToServer("create database " + databaseName + ";");
        assertTrue(response.contains("[OK]"), "unable to create database");

        File f = new File(storageFolderPath+fileSeparator+databaseName);
        assertTrue(f.exists(), "Database was not created in the file directory properly");


        sendCommandToServer("use "+ databaseName + ";");

        /*        //create table*/
        String customersTable = "customers";
        String ordersTable = "orders";

        response = sendCommandToServer("create table " + customersTable + " (name,email,phone);");
        assertTrue(response.contains("[OK]"), "unable to create table - marks");

        File f1 = new File(storageFolderPath+fileSeparator+databaseName+fileSeparator+customersTable + ".tab");
        assertTrue(f1.exists(), "Table was not created in the " + databaseName +" database directory properly");

        response = sendCommandToServer("create table " + ordersTable + "(customerId , product, quantity, price);");
        assertTrue(response.contains("[OK]"), "unable to create table - ordersTable");

        File f2 = new File(storageFolderPath+fileSeparator+databaseName+fileSeparator+ordersTable +".tab");
        assertTrue(f2.exists(), "Table was not created in the " + ordersTable +" database directory properly");



        //not allowed to make multiple tables or databases
        response = sendCommandToServer("create database " + databaseName + "(customerId , product, quantity, price);");
        assertTrue(response.contains("[ERROR]"), "no duplicate database allowed to be created");

        response = sendCommandToServer("create table " + ordersTable + "(customerId , product, quantity, price);");
        assertTrue(response.contains("[ERROR]"), "no duplicate tables allowed to be created");
    }


    @Test
    public void testUse(){

        String databaseName = generateRandomName();

        //use database that exists
        sendCommandToServer("Create database " + databaseName + ";");
        String response = sendCommandToServer("use " + databaseName + ";");
        assertTrue(response.contains("[OK]"), "database exists and used successfully");

        response = sendCommandToServer("Create table testingtable (name,age);");
        assertTrue(response.contains("[OK]"), "table created in the correct database");

        //use database - does not exist
        response = sendCommandToServer("use nonexistentdatabase;");
        assertTrue(response.contains("[ERROR]"), "unable to use database that doesnt exist");

        sendCommandToServer("Drop database " + databaseName +";");

    }




    @Test
    public void testInsert(){
        String databaseName = generateRandomName();

        /*        //create database*/
        String response = sendCommandToServer("create database " + databaseName + ";");
        assertTrue(response.contains("[OK]"), "unable to create database");

        File f = new File(storageFolderPath+fileSeparator+databaseName);
        assertTrue(f.exists(), "Database was not created in the file directory properly");


        sendCommandToServer("use "+ databaseName + ";");

        /*        //create table*/
        String customersTable = "customers";
        String ordersTable = "orders";

        response = sendCommandToServer("create table " + customersTable + " (name,email,phone);");
        assertTrue(response.contains("[OK]"), "unable to create table - marks");

        File f1 = new File(storageFolderPath+fileSeparator+databaseName+fileSeparator+customersTable + ".tab");
        assertTrue(f1.exists(), "Table was not created in the " + databaseName +" database directory properly");

        response = sendCommandToServer("create table " + ordersTable + "(customerId , product, quantity, price);");
        assertTrue(response.contains("[OK]"), "unable to create table - ordersTable");

        File f2 = new File(storageFolderPath+fileSeparator+databaseName+fileSeparator+ordersTable +".tab");
        assertTrue(f2.exists(), "Table was not created in the " + ordersTable +" database directory properly");





        sendCommandToServer("use "+ databaseName +";");
        sendCommandToServer("INSERT INTO customers VALUES ('Alice', 'alice@example.com', 073849392);");
        sendCommandToServer("INSERT INTO customers VALUES ('Bob', 'bob@example.com', 389489322);");
        sendCommandToServer("INSERT INTO customers VALUES ('Charlie', 'charlie@example.com', 23232121);");
        sendCommandToServer("INSERT INTO customers VALUES ('dave', 'dave@example.com', 222212233);");



        response = sendCommandToServer("INSERT INTO orders VALUES (1, 'Apple', 2, 3);");
        sendCommandToServer("INSERT INTO orders VALUES (2, 'Banana', 22, 4.00);");
        sendCommandToServer("INSERT INTO orders VALUES (3, 'Orange', 9, 7.00);");
        sendCommandToServer("INSERT INTO orders VALUES (4, 'Pineapple', 1, 10.00);");
        sendCommandToServer("INSERT INTO orders VALUES (5, 'Mango', 34, 11.00);");

        //if they enter more values than attributes, return an error?
        response = sendCommandToServer("INSERT INTO customers VALUES ('Cucumber', 5, 9, 23.00, 'Location')");
        assertTrue(response.contains("[ERROR]"), "Was not supposed to allow users to insert if the number of attributes do not match");

        response = sendCommandToServer("INSERT INTO orders VALUES ('Strawberry');");
        assertTrue(response.contains("[ERROR]"), "Was not supposed to allow users to insert if the number of attributes do not match");

    }

    @Test
    public void testJoin(){
        String databaseName = generateRandomName();

        /*        //create database*/
        String response = sendCommandToServer("create database " + databaseName + ";");
        assertTrue(response.contains("[OK]"), "unable to create database");

        File f = new File(storageFolderPath+fileSeparator+databaseName);
        assertTrue(f.exists(), "Database was not created in the file directory properly");


        sendCommandToServer("use "+ databaseName + ";");

        /*        //create table*/
        String customersTable = "customers";
        String ordersTable = "orders";

        response = sendCommandToServer("create table " + customersTable + " (name,email,phone);");
        assertTrue(response.contains("[OK]"), "unable to create table - marks");

        File f1 = new File(storageFolderPath+fileSeparator+databaseName+fileSeparator+customersTable + ".tab");
        assertTrue(f1.exists(), "Table was not created in the " + databaseName +" database directory properly");

        response = sendCommandToServer("create table " + ordersTable + "(customerId , product, quantity, price);");
        assertTrue(response.contains("[OK]"), "unable to create table - ordersTable");

        File f2 = new File(storageFolderPath+fileSeparator+databaseName+fileSeparator+ordersTable +".tab");
        assertTrue(f2.exists(), "Table was not created in the " + ordersTable +" database directory properly");





        sendCommandToServer("use "+ databaseName +";");
        sendCommandToServer("INSERT INTO customers VALUES ('Alice', 'alice@example.com', 073849392);");
        sendCommandToServer("INSERT INTO customers VALUES ('Bob', 'bob@example.com', 389489322);");
        sendCommandToServer("INSERT INTO customers VALUES ('Charlie', 'charlie@example.com', 23232121);");
        sendCommandToServer("INSERT INTO customers VALUES ('dave', 'dave@example.com', 222212233);");



        response = sendCommandToServer("INSERT INTO orders VALUES (1, 'Apple', 2, 3);");
        sendCommandToServer("INSERT INTO orders VALUES (2, 'Banana', 22, 4.00);");
        sendCommandToServer("INSERT INTO orders VALUES (3, 'Orange', 9, 7.00);");
        sendCommandToServer("INSERT INTO orders VALUES (4, 'Pineapple', 1, 10.00);");
        sendCommandToServer("INSERT INTO orders VALUES (5, 'Mango', 34, 11.00);");


        sendCommandToServer("use "+databaseName + ";");
        response = sendCommandToServer("Join " + ordersTable + " and " + customersTable +" on customerid and id;");
        //not including the [OK]
        assertTrue(response.contains("[OK]"),"Valid query provided, however [OK] was not returned");

        String singleLineResponse [] = response.split("\n");

        //check for the first line is equals
        assertTrue(singleLineResponse[1].equals("id\torders.product\torders.quantity\torders.price\tcustomers.name\tcustomers.email\tcustomers.phone\t"), "First row should have the column headers with table.attribute");
        assertTrue(singleLineResponse[2].equals("1\tApple\t2\t3\tAlice\talice@example.com\t073849392\t"), "Second row should have the joined row entries of both tables");

        //delete row
        sendCommandToServer("DELETE FROM " + customersTable + " where name == 'Charlie';");
        response = sendCommandToServer("Select * from "+ customersTable +";");
        assertTrue(response.contains("[OK]"), "Valid query provided, however [OK] was not returned");
        assertTrue(!response.contains("Charlie"), "customers table should not contain attribute Charlie after Delete query");


        //table should not contain corresponding "Charlie" row - (3	3	Orange	9	7.00)
        response = sendCommandToServer("Join " + ordersTable + " and " + customersTable +" on customerId and id;");
        assertTrue(!response.contains("Orange"), "Should not have a join as there is no customer id=3 entry in customers table");


    }

    @Test
    public void testDelete() {

        String databaseName = generateRandomName();

        /*        //create database*/
        String response = sendCommandToServer("create database " + databaseName + ";");
        assertTrue(response.contains("[OK]"), "unable to create database");

        File f = new File(storageFolderPath+fileSeparator+databaseName);
        assertTrue(f.exists(), "Database was not created in the file directory properly");


        sendCommandToServer("use "+ databaseName + ";");

        /*        //create table*/
        String customersTable = "customers";
        String ordersTable = "orders";

        response = sendCommandToServer("create table " + customersTable + " (name,email,phone);");
        assertTrue(response.contains("[OK]"), "unable to create table - marks");

        File f1 = new File(storageFolderPath+fileSeparator+databaseName+fileSeparator+customersTable + ".tab");
        assertTrue(f1.exists(), "Table was not created in the " + databaseName +" database directory properly");


        sendCommandToServer("use "+ databaseName +";");
        sendCommandToServer("INSERT INTO customers VALUES ('Alice', 'alice@example.com', 073849392);");
        sendCommandToServer("INSERT INTO customers VALUES ('Bob', 'bob@example.com', 389489322);");
        sendCommandToServer("INSERT INTO customers VALUES ('Charlie', 'charlie@example.com', 23232121);");
        sendCommandToServer("INSERT INTO customers VALUES ('dave', 'dave@example.com', 222212233);");


        sendCommandToServer("use "+databaseName+";");
        response = sendCommandToServer("Select * from customers;");
        assertTrue(response.contains("dave"), "Valid query was provided, but [OK] was not returned");

        sendCommandToServer("delete from " + customersTable + " where name == 'dave';");
        response = sendCommandToServer("select * from "+customersTable+";");
        assertTrue(!response.contains("dave"), "value should exist in the table after running Delete query");

        response = sendCommandToServer("delete from " + customersTable + " where name == 'dave';");
        assertTrue(response.contains("[OK]"), "Response should return [OK] although no entry was found");

        //set back to original entries
        sendCommandToServer("INSERT INTO customers VALUES ('dave', 'dave@example.com', 222212233);");

    }



    @Test
    public void testAlter(){
        String databaseName = generateRandomName();

        /*        //create database*/
        String response = sendCommandToServer("create database " + databaseName + ";");
        assertTrue(response.contains("[OK]"), "unable to create database");

        File f = new File(storageFolderPath+fileSeparator+databaseName);
        assertTrue(f.exists(), "Database was not created in the file directory properly");


        sendCommandToServer("use "+ databaseName + ";");

        /*        //create table*/
        String customersTable = "customers";
        String ordersTable = "orders";

        response = sendCommandToServer("create table " + customersTable + " (name,email,phone);");
        assertTrue(response.contains("[OK]"), "unable to create table - marks");

        File f1 = new File(storageFolderPath+fileSeparator+databaseName+fileSeparator+customersTable + ".tab");
        assertTrue(f1.exists(), "Table was not created in the " + databaseName +" database directory properly");

        response = sendCommandToServer("create table " + ordersTable + "(customerId , product, quantity, price);");
        assertTrue(response.contains("[OK]"), "unable to create table - ordersTable");

        File f2 = new File(storageFolderPath+fileSeparator+databaseName+fileSeparator+ordersTable +".tab");
        assertTrue(f2.exists(), "Table was not created in the " + ordersTable +" database directory properly");





        sendCommandToServer("use "+ databaseName +";");
        sendCommandToServer("INSERT INTO customers VALUES ('Alice', 'alice@example.com', 073849392);");
        sendCommandToServer("INSERT INTO customers VALUES ('Bob', 'bob@example.com', 389489322);");
        sendCommandToServer("INSERT INTO customers VALUES ('Charlie', 'charlie@example.com', 23232121);");
        sendCommandToServer("INSERT INTO customers VALUES ('dave', 'dave@example.com', 222212233);");



        response = sendCommandToServer("INSERT INTO orders VALUES (1, 'Apple', 2, 3);");
        sendCommandToServer("INSERT INTO orders VALUES (2, 'Banana', 22, 4.00);");
        sendCommandToServer("INSERT INTO orders VALUES (3, 'Orange', 9, 7.00);");
        sendCommandToServer("INSERT INTO orders VALUES (4, 'Pineapple', 1, 10.00);");
        sendCommandToServer("INSERT INTO orders VALUES (5, 'Mango', 34, 11.00);");



        //test add
        sendCommandToServer("use "+ databaseName +";");
        response = sendCommandToServer("alter table "+customersTable+" add attribute1;");
        assertTrue(response.contains("[OK]"), "Valid query provided, however [OK] was not returned");

        response = sendCommandToServer("Select * from " +customersTable+";");
        assertTrue(response.contains("attribute1"), "alter - add should have added new column to the table");


        //test drop
        response = sendCommandToServer("alter table "+customersTable+" drop attribute1;");
        assertTrue(response.contains("[OK]"), "Valid query provided, however [OK] was not returned");

        response = sendCommandToServer("Select * from " +customersTable+";");
        assertTrue(!response.contains("attribute1"), "alter - add should have added new column to the table");

        //test add - attribute name as keyword (error)
        response = sendCommandToServer("alter table "+customersTable+" add join;");
        assertTrue(response.contains("[ERROR]"), "Invalid query provided, however [OK] was returned");

    }

    @Test
    public void testUpdate(){
        String databaseName = generateRandomName();

        /*        //create database*/
        String response = sendCommandToServer("create database " + databaseName + ";");
        assertTrue(response.contains("[OK]"), "unable to create database");

        File f = new File(storageFolderPath+fileSeparator+databaseName);
        assertTrue(f.exists(), "Database was not created in the file directory properly");


        sendCommandToServer("use "+ databaseName + ";");

        /*        //create table*/
        String customersTable = "customers";
        String ordersTable = "orders";

        response = sendCommandToServer("create table " + customersTable + " (name,email,phone);");
        assertTrue(response.contains("[OK]"), "unable to create table - marks");

        File f1 = new File(storageFolderPath+fileSeparator+databaseName+fileSeparator+customersTable + ".tab");
        assertTrue(f1.exists(), "Table was not created in the " + databaseName +" database directory properly");


        sendCommandToServer("use "+ databaseName +";");
        sendCommandToServer("INSERT INTO customers VALUES ('Alice', 'alice@example.com', 073849392);");
        sendCommandToServer("INSERT INTO customers VALUES ('Bob', 'bob@example.com', 389489322);");
        sendCommandToServer("INSERT INTO customers VALUES ('Charlie', 'charlie@example.com', 23232121);");
        sendCommandToServer("INSERT INTO customers VALUES ('dave', 'dave@example.com', 222212233);");


        sendCommandToServer("use "+ databaseName +";");
        response = sendCommandToServer("SELECT NAME from customers where id == 1;");

        //should return 3 lines - [OK], attributename, result
        String results [] = response.split("\n");
        ArrayList<String> entriesResult = new ArrayList<String>();

        //extract all entries of the return result - after [OK] and attributeName
        for(int i = 2; i < results.length; i++){
            entriesResult.add(results[i].trim());
        }

        assertTrue(entriesResult.get(0).equals("Alice"), "Should obtain only one entry result - Alice");

        response = sendCommandToServer("UPDATE customers Set NAME = 'Bruce' where id == 1;");
        assertTrue(response.contains("[OK]"), "Valid query provided, however [OK] was not returned");

        response = sendCommandToServer("SELECT NAME from customers where id == 1;");
        assertTrue(response.contains("[OK]"), "Valid query provided, however [OK] was not returned");

        results = response.split("\n");
        entriesResult = new ArrayList<String>();

        for(int i = 2; i < results.length; i++){
            entriesResult.add(results[i].trim());
        }

        assertTrue(entriesResult.get(0).equals("Bruce"), "Should obtain only one entry result - Alice");



        //update conditions with 2 conditions
        response = sendCommandToServer("Select name,phone from customers where id <= 3;");
        results = response.split("\n");

        entriesResult = new ArrayList<String>();
        for(int i = 2; i < results.length; i++){
            entriesResult.add(results[i].trim());
        }

        assertTrue(entriesResult.get(0).equals("Bruce\t073849392"), "Should obtain only two entries - name & phone");
        assertTrue(entriesResult.get(1).equals("Bob\t389489322"), "Should obtain only two entries - name & phone");
        assertTrue(entriesResult.get(2).equals("Charlie\t23232121"), "Should obtain only two entries - name & phone");

        response = sendCommandToServer("UPDATE customers Set NAME = 'Bruce',phone = 122 where id <= 3;");
        assertTrue(response.contains("[OK]"), "Valid query provided, however [OK] was not returned");

        response = sendCommandToServer("Select name,phone from customers where id <= 3;");
        results = response.split("\n");

        entriesResult = new ArrayList<String>();
        for(int i = 2; i < results.length; i++){
            entriesResult.add(results[i].trim());
        }

        assertTrue(entriesResult.get(0).equals("Bruce\t122"), "Should obtain only two entries - name & phone");
        assertTrue(entriesResult.get(1).equals("Bruce\t122"), "Should obtain only two entries - name & phone");
        assertTrue(entriesResult.get(2).equals("Bruce\t122"), "Should obtain only two entries - name & phone");


    }


//    @Test
//    public void testWhere(){
//
//    }
//
//


}
