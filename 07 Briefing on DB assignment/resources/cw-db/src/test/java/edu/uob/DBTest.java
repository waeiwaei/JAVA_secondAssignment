package edu.uob;


import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.Array;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;

public class DBTest {

    static String fileSeparator = File.separator;
    static String storageFolderPath;
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
        storageFolderPath = Paths.get("databases").toAbsolutePath().toString();

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

        response = sendCommandToServer("select * from Table1 a;");
        assertTrue(response.contains("[ERROR]"), "Grammar error - parenthesis matching error");

        response = sendCommandToServer("DELETE attribute1 FROM marks where (name == 'steve'));");
        assertTrue(response.contains("[ERROR]"), "Grammar error - parenthesis matching error");

        response = sendCommandToServer("DELETE attribute1 FROM marks where (((name == 'steve');");
        assertTrue(response.contains("[ERROR]"), "Grammar error - parenthesis matching error");

        response = sendCommandToServer("");
        assertTrue(response.contains("[ERROR]"), "Grammar error - cannot process empty query");



    }

    //test for Grammar where it parses correctly and carries out actions
    //create files, can check if folder, file directory exists
    //check if input values are as expected

    @Test
    public void testInvalidException(){
        storageFolderPath = Paths.get("databases").toAbsolutePath().toString();

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


        //insert to little values - many values
        response = sendCommandToServer("insert into testingtable ('bob')");
        assertTrue(response.contains("[ERROR]"), "Interpreter error - unable to add attribute name as keyword");

        response = sendCommandToServer("insert into testingtable ('bob',76, 'bob@email.com')");
        assertTrue(response.contains("[ERROR]"), "Interpreter error - unable to add attribute name as keyword");


    }





    @Test
    public void testDrop(){
        storageFolderPath = Paths.get("databases").toAbsolutePath().toString();

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

        //drop database that exists - with table
        sendCommandToServer("create table "+ tableName +";");
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
        storageFolderPath = Paths.get("databases").toAbsolutePath().toString();

        String databaseName = generateRandomName();

        //create database
        String response = sendCommandToServer("create database " + databaseName + ";");
        assertTrue(response.contains("[OK]"), "unable to create database");

        File f = new File(storageFolderPath+fileSeparator+databaseName);
        assertTrue(f.exists(), "Database was not created in the file directory properly");


        sendCommandToServer("use "+ databaseName + ";");

        //create table
        String customersTable = "customers";
        String ordersTable = "orders";

        response = sendCommandToServer("create table " + customersTable + " (name,email,phone);");
        assertTrue(response.contains("[OK]"), "unable to create table - marks");

        File f1 = new File(storageFolderPath+fileSeparator+databaseName+fileSeparator+customersTable + ".tab");
        assertTrue(f1.exists(), "Table was not created in the " + databaseName +" database directory properly");

        response = sendCommandToServer("create table " + ordersTable + "(customerId , delete, quantity, price);");
        assertTrue(response.contains("[ERROR]"), "unable to create table - due to attribute name being a keyword (delete)");

        File f2 = new File(storageFolderPath+fileSeparator+databaseName+fileSeparator+ordersTable +".tab");
        assertTrue(!f2.exists(), "Table was not created in the " + ordersTable +" database directory properly");



        //not allowed to make multiple tables or databases
        response = sendCommandToServer("create database " + databaseName +";");
        assertTrue(response.contains("[ERROR]"), "no duplicate database allowed to be created");



        //not allowed to make database or table name with key word
        response = sendCommandToServer("create database join;");
        assertTrue(response.contains("[ERROR]"), "database created however - not allowed to create database with keyword");

        response = sendCommandToServer("create table create (customerId , product, quantity, price);");
        assertTrue(response.contains("[ERROR]"), "table created however - not allowed to create database with keyword");

        sendCommandToServer("use "+ databaseName + ";");
        response = sendCommandToServer("create table tableExample (attribute1 , attribute2, attribute3, attribute1);");
        assertTrue(response.contains("[ERROR]"), "table cannot have 2 attributes with the same name");

    }


    @Test
    public void testUse(){
        storageFolderPath = Paths.get("databases").toAbsolutePath().toString();

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
        storageFolderPath = Paths.get("databases").toAbsolutePath().toString();

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
        response = sendCommandToServer("select * from customers;");

        String result [] = response.split("\n");
        ArrayList<String> entries = new ArrayList<String>();

        for(int i = 2; i < result.length; i++){
            entries.add(result[i]);
        }

        assertTrue(entries.size() == 4, "Insert query was supposed to add the following entries to the table orders");
        assertTrue(entries.get(0).contains("Alice"), "Insert query was supposed to add the following entries to the table orders");
        assertTrue(entries.get(1).contains("Bob"), "Insert query was supposed to add the following entries to the table orders");
        assertTrue(entries.get(2).contains("Charlie"), "Insert query was supposed to add the following entries to the table orders");
        assertTrue(entries.get(3).contains("dave"), "Insert query was supposed to add the following entries to the table orders");




        response = sendCommandToServer("INSERT INTO orders VALUES (1, 'Apple', 2, 3);");
        sendCommandToServer("INSERT INTO orders VALUES (2, 'Banana', 22, 4.00);");
        sendCommandToServer("INSERT INTO orders VALUES (3, 'Orange', 9, 7.00);");
        sendCommandToServer("INSERT INTO orders VALUES (4, 'Pineapple', 1, 10.00);");
        sendCommandToServer("INSERT INTO orders VALUES (5, 'Mango', 34, 11.00);");
        response = sendCommandToServer("select * from orders;");

        result = response.split("\n");
        entries = new ArrayList<String>();

        for(int i = 2; i < result.length; i++){
            entries.add(result[i]);
        }

        assertTrue(entries.size() == 5, "Insert query was supposed to add the following entries to the table orders");
        assertTrue(entries.get(0).contains("Apple"), "Insert query was supposed to add the following entries to the table orders");
        assertTrue(entries.get(1).contains("Banana"), "Insert query was supposed to add the following entries to the table orders");
        assertTrue(entries.get(2).contains("Orange"), "Insert query was supposed to add the following entries to the table orders");
        assertTrue(entries.get(3).contains("Pineapple"), "Insert query was supposed to add the following entries to the table orders");
        assertTrue(entries.get(4).contains("Mango"), "Insert query was supposed to add the following entries to the table orders");

        //insert null value and see if it registers


        //if they enter more values than attributes, return an error?
        response = sendCommandToServer("INSERT INTO customers VALUES ('Cucumber', 5, 9, 23.00, 'Location')");
        assertTrue(response.contains("[ERROR]"), "Was not supposed to allow users to insert if the number of attributes do not match");

        response = sendCommandToServer("INSERT INTO orders VALUES ('Strawberry');");
        assertTrue(response.contains("[ERROR]"), "Was not supposed to allow users to insert if the number of attributes do not match");

    }

    @Test
    public void testJoin(){
        storageFolderPath = Paths.get("databases").toAbsolutePath().toString();

        String databaseName = generateRandomName();

        //create database
        String response = sendCommandToServer("create database " + databaseName + ";");
        assertTrue(response.contains("[OK]"), "unable to create database");

        File f = new File(storageFolderPath+fileSeparator+databaseName);
        assertTrue(f.exists(), "Database was not created in the file directory properly");


        sendCommandToServer("use "+ databaseName + ";");

        //create table
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
        sendCommandToServer("INSERT INTO customers VALUES ('dave', 'dave@example.com', 323232300);");
        sendCommandToServer("INSERT INTO customers VALUES ('Tony', 'tony@example.com', 229392333);");



        sendCommandToServer("INSERT INTO orders VALUES (1, 'Apple', 2, 3);");
        sendCommandToServer("INSERT INTO orders VALUES (2, 'Banana', 22, 4.00);");
        sendCommandToServer("INSERT INTO orders VALUES (3, 'Orange', 9, 7.00);");
        sendCommandToServer("INSERT INTO orders VALUES (4, 'Pineapple', 1, 10.00);");
        sendCommandToServer("INSERT INTO orders VALUES (5, 'Mango', 34, 11.00);");


        sendCommandToServer("use "+databaseName + ";");
        response = sendCommandToServer("Join " + ordersTable + " and " + customersTable +" on customerid and id;");
        //not including the [OK]
        assertTrue(response.contains("[OK]"),"Valid query provided, however [OK] was not returned");

        String singleLineResponse [] = response.split("\n");

        String result [] = response.split("\n");
        ArrayList<String> entries = new ArrayList<String>();

        for(int i = 2; i < result.length; i++){
            entries.add(result[i]);
        }

        //check for the first line is equals
        assertTrue(entries.size() == 5, "4 entries were expected to be returned as a result of the JOIN");
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
        assertTrue(!response.contains("Charlie"), "Should not have a join as there is no customer id=3 entry in customers table");


        response = sendCommandToServer("create table marks (Name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Dave', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Clive', 20, FALSE);");


        response = sendCommandToServer("create table coursework (task, submission);");
        sendCommandToServer("INSERT INTO coursework VALUES ('OXO', 3);");
        sendCommandToServer("INSERT INTO coursework VALUES ('DB', 1);");
        sendCommandToServer("INSERT INTO coursework VALUES ('OXO', 4);");
        sendCommandToServer("INSERT INTO coursework VALUES ('STAG', 2);");

        response = sendCommandToServer("JOIN coursework AND marks ON submission AND id;");
        result = response.split("\n");
        entries = new ArrayList<String>();

        for(int i = 2; i < result.length; i++){
            entries.add(result[i]);
        }

        assertTrue(entries.size() == 4, "4 entries were expected to be returned as a result of the JOIN");

        //delete row
        sendCommandToServer("DELETE FROM marks where name == 'Dave';");
        response = sendCommandToServer("Select * from marks;");
        assertTrue(response.contains("[OK]"), "Valid query provided, however [OK] was not returned");
        assertTrue(!response.contains("Dave"), "customers table should not contain attribute Charlie after Delete query");


        //table should not contain corresponding "Charlie" row - (3	3	Orange	9	7.00)
        response = sendCommandToServer("JOIN coursework AND marks ON submission AND id;");
        assertTrue(!response.contains("STAG"), "Should not have a join as there is no customer id=3 entry in customers table");
        assertTrue(!response.contains("Dave"), "Should not have a join as there is no customer id=3 entry in customers table");


    }

    @Test
    public void testDelete() {
        storageFolderPath = Paths.get("databases").toAbsolutePath().toString();

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

        //delete with nested conditions
        response = sendCommandToServer("delete from " + customersTable + " where ((name == 'dave') AND id <= 4) OR name == 'Bob';");
        assertTrue(response.contains("[OK]"), "Response should return [OK] with valid query made");
        response = sendCommandToServer("Select name from "+customersTable+";");
        assertTrue(!response.contains("dave"), "Response should return [OK] although no entry was found");
        assertTrue(!response.contains("Bob"), "Response should return [OK] although no entry was found");


        //test with invalid nested conditions - bracket mismatch issue
        response = sendCommandToServer("delete from " + customersTable + " where ((name == 'dave') AND id <= 4) OR name == 'Bob');");
        assertTrue(response.contains("[ERROR]"), "Response should return [ERROR] with invalid query made");

        response = sendCommandToServer("delete from " + customersTable + " where (((name == 'dave') AND id <= 4) OR name == 'Bob';");
        assertTrue(response.contains("[ERROR]"), "Response should return [ERROR] with invalid query made");

    }

    @Test
    public void testSelect(){
        storageFolderPath = Paths.get("databases").toAbsolutePath().toString();
        String databaseName = generateRandomName();

        String response = sendCommandToServer("create database " + databaseName + ";");

        sendCommandToServer("use "+ databaseName +";");
        sendCommandToServer("create table customers(name, email, phone);");
        sendCommandToServer("INSERT INTO customers VALUES ('Alice', 'alice@example.com', 073849392);");
        sendCommandToServer("INSERT INTO customers VALUES ('Bob', 'bob@example.com', 389489322);");
        sendCommandToServer("INSERT INTO customers VALUES ('Charlie', 'charlie@example.com', 23232121);");
        sendCommandToServer("INSERT INTO customers VALUES ('dave', 'dave@example.com', 222212233);");

        //Select from table where no attribute name was identified in the table
        response = sendCommandToServer("select nonExistentAttribute from customers;");
        assertTrue(response.contains("[ERROR]"), "Was expecting an [ERROR] to be returned, however [OK] was returned");

        //select from table where no entry matches the condition
        response = sendCommandToServer("Select Name      from customers          where name == 'Steve';");
        assertTrue(response.contains("[OK]"), "Valid query was provided, however [OK] was not returned");

        String result [] = response.split("\n");
        ArrayList<String> entries = new ArrayList<String>();

        for(int i = 2; i < result.length; i++){
            entries.add(result[i]);
        }

        assertTrue(entries.size() == 0, "No entries were expected to be returned, as there are no entries which match the condition");

        //test with nested where conditions
        response = sendCommandToServer("Select name from customers where ((id > 1 AND name == 'Bob') AND id <= 4) OR id == 3;");
        assertTrue(response.contains("[OK]"), "Valid query was provided, however [OK] was not returned");
        assertTrue(response.contains("Bob"), "Select query should return Bob and Charlie only");
        assertTrue(response.contains("Charlie"), "Select query should return Bob and Charlie only");
        assertTrue(!response.contains("Alice"), "Select query should return Bob and Charlie only");
        assertTrue(!response.contains("dave"), "Select query should return Bob and Charlie only");


        response = sendCommandToServer("Select name from customers where ((phone like 7 AND name like 'A') AND id <= 4) OR id == 1;");
        assertTrue(response.contains("Alice"), "Select query should return Bob and Charlie only");
        assertTrue(!response.contains("Charlie"), "Select query should return Bob and Charlie only");
        assertTrue(!response.contains("Bob"), "Select query should return Bob and Charlie only");
        assertTrue(!response.contains("dave"), "Select query should return Bob and Charlie only");

        response = sendCommandToServer("Select name from customers where ((id == 5 AND name == 'Bob') AND id <= 4) OR id == 5;");
        assertTrue(response.contains("[OK]"), "Valid query was provided, however [OK] was not returned");

        result = response.split("\n");
        entries = new ArrayList<String>();

        for(int i = 2; i < result.length; i++){
            entries.add(result[i]);
        }

        assertTrue(entries.size() == 0, "No entries were expected to be returned, as there are no entries which match the condition");

        //test select with null condition
        //test with nested where conditions - fail
    }

    @Test
    public void testAlter(){
        storageFolderPath = Paths.get("databases").toAbsolutePath().toString();

        String databaseName = generateRandomName();

        //create database
        String response = sendCommandToServer("create database " + databaseName + ";");
        assertTrue(response.contains("[OK]"), "unable to create database");

        File f = new File(storageFolderPath+fileSeparator+databaseName);
        assertTrue(f.exists(), "Database was not created in the file directory properly");


        sendCommandToServer("use "+ databaseName + ";");

        //create table
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



        //test add
        sendCommandToServer("use "+ databaseName +";");
        response = sendCommandToServer("alter table "+customersTable+" add attribute1;");
        assertTrue(response.contains("[OK]"), "Valid query provided, however [OK] was not returned");

        response = sendCommandToServer("Select * from " +customersTable+";");
        assertTrue(response.contains("attribute1"), "alter - add should have added new column to the table");

        response = sendCommandToServer("alter table "+customersTable+" add attribute1;");
        assertTrue(response.contains("[ERROR]"), "Should return an error, not allowed to have 2 of the attributes with the same name");

        //alter table add - attname and delete attname (CAPS)
        response = sendCommandToServer("alter table "+customersTable+" add ATTRIBUTE2;");
        assertTrue(response.contains("[OK]"), "Should return an error, not allowed to have 2 of the attributes with the same name");
        response = sendCommandToServer("select attribute2 from " + customersTable + ";");
        assertTrue(response.contains("[OK]"), "Should return an error, not allowed to have 2 of the attributes with the same name");
        assertTrue(response.contains("ATTRIBUTE2"), "Should return an error, not allowed to have 2 of the attributes with the same name");



        //test drop
        response = sendCommandToServer("alter table "+customersTable+" drop attribute1;");
        assertTrue(response.contains("[OK]"), "Valid query provided, however [OK] was not returned");

        response = sendCommandToServer("Select * from " +customersTable+";");
        assertTrue(!response.contains("attribute1"), "alter - add should have added new column to the table");

        response = sendCommandToServer("alter table "+customersTable+" drop attribute2;");
        assertTrue(response.contains("[OK]"), "Valid query provided, however [OK] was not returned");

        response = sendCommandToServer("Select * from " +customersTable+";");
        assertTrue(!response.contains("ATTRIBUTE2"), "alter - add should have added new column to the table");


        //test drop
        response = sendCommandToServer("alter table "+customersTable+" drop name;");
        assertTrue(response.contains("[OK]"), "Valid query provided, however [OK] was not returned");

        //test drop fail - not allowed to remove id column
        response = sendCommandToServer("alter table "+customersTable+" drop id;");
        assertTrue(response.contains("[ERROR]"), "Not allowed to remove id columns");

        response = sendCommandToServer("Select * from " +customersTable+";");
        assertTrue(!response.contains("name"), "alter - add should have added new column to the table");
        assertTrue(response.contains("id"), "alter - add should have added new column to the table");

        //test add - attribute name as keyword (error)
        response = sendCommandToServer("alter table "+customersTable+" add join;");
        assertTrue(response.contains("[ERROR]"), "Invalid query provided, however [OK] was returned");

    }

    @Test
    public void testUpdate(){
        storageFolderPath = Paths.get("databases").toAbsolutePath().toString();

        String databaseName = generateRandomName();

        //create database*/
        String response = sendCommandToServer("create database " + databaseName + ";");
        assertTrue(response.contains("[OK]"), "unable to create database");

        File f = new File(storageFolderPath+fileSeparator+databaseName);
        assertTrue(f.exists(), "Database was not created in the file directory properly");


        sendCommandToServer("use "+ databaseName + ";");

        //create table
        String customersTable = "customers";

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

        response = sendCommandToServer("SELECT NAME from customers where ID == 1;");
        assertTrue(response.contains("[OK]"), "Valid query provided, however [OK] was not returned");

        results = response.split("\n");
        entriesResult = new ArrayList<String>();

        for(int i = 2; i < results.length; i++){
            entriesResult.add(results[i].trim());
        }

        assertTrue(entriesResult.get(0).equals("Bruce"), "Should obtain only one entry result - Alice");



        //test fail - update ID attribute
        response = sendCommandToServer("UPDATE customers Set ID = 45 where name == 'Bruce';");
        assertTrue(response.contains("[ERROR]"), "Valid query provided, users not allowed to change ID value");

        response = sendCommandToServer("UPDATE customers SET phone = 39292, ID = 45 where name == 'Bruce';");
        assertTrue(response.contains("[ERROR]"), "Valid query provided, users not allowed to change ID value");

        response = sendCommandToServer("UPDATE customers SET phone = 39292, ID = 45, email = 'new@email.com' where name == 'Bruce';");
        assertTrue(response.contains("[ERROR]"), "Valid query provided, users not allowed to change ID value");


        //update 2 fields
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




        //perform more updates with nested where conditions
        sendCommandToServer("use "+databaseName+";");
        sendCommandToServer("Create table orders (customerid, product, quantity, price);");
        sendCommandToServer("INSERT INTO orders VALUES (1, 'Apple', 2, 3);");
        sendCommandToServer("INSERT INTO orders VALUES (2, 'Banana', 22, 4.00);");
        sendCommandToServer("INSERT INTO orders VALUES (3, 'Orange', 9, 7.00);");
        sendCommandToServer("INSERT INTO orders VALUES (4, 'Pineapple', 1, 10.00);");
        sendCommandToServer("INSERT INTO orders VALUES (5, 'Mango', 34, 11.00);");


        response = sendCommandToServer("Update orders set price = 10000 where ((product like 'B') AND  quantity < 50) OR id == 5;");
        assertTrue(response.contains("[OK]"), "Valid query provided, however [OK] was not returned");
        response = sendCommandToServer("select product from orders where price != 10000;");
        assertTrue(!response.contains("Banana"), "Update query was meant to update the price of Banana and Mango");
        assertTrue(!response.contains("Mango"), "Update query was meant to update the price of Banana and Mango");


        response = sendCommandToServer("Update orders set price = 2323 where ((product == 'Mango') AND  quantity == 35) OR (id > 1 AND id < 3);");
        assertTrue(response.contains("[OK]"), "Valid query provided, however [OK] was not returned");
        response = sendCommandToServer("select product from orders where price == 2323;");
        assertTrue(response.contains("Banana"), "Update query was meant to update the price of Banana and Mango");

        response = sendCommandToServer("Update orders set quantity = 45 where ((product like 'a') AND  quantity < 30) AND ((id > 1 AND id <= 5) AND price == 2323);");
        assertTrue(response.contains("[OK]"), "Valid query provided, however [OK] was not returned");
        response = sendCommandToServer("select product from orders where quantity == 45;");
        assertTrue(response.contains("Banana"), "Update query was meant to update the price of Banana and Mango");


    }


    @Test
    public void testWhere(){
        storageFolderPath = Paths.get("databases").toAbsolutePath().toString();


        String databaseName = generateRandomName();

        sendCommandToServer("Create database "+databaseName+";");
        sendCommandToServer("use "+ databaseName +";");
        sendCommandToServer("Create table customers(name,email,phone);");
        sendCommandToServer("INSERT INTO customers VALUES ('Alice', 'alice@example.com', 073849392);");
        sendCommandToServer("INSERT INTO customers VALUES ('Bob', 'bob@example.com', 389489322);");
        sendCommandToServer("INSERT INTO customers VALUES ('Charlie', 'charlie@example.com', 23232121);");
        sendCommandToServer("INSERT INTO customers VALUES ('dave', 'dave@example.com', 222212233);");

        //update with one condition
        String response = sendCommandToServer("UPDATE customers SET name = 'Bryan' where id == 1;");
        assertTrue(response.contains("[OK]"), "Valid query was provided, however [OK] was not returned");
        response = sendCommandToServer("Select name from customers where id == 1;");

        String results [] = response.split("\n");

        ArrayList<String> entriesResult = new ArrayList<String>();
        for(int i = 2; i < results.length; i++){
            entriesResult.add(results[i].trim());
        }

        assertTrue(entriesResult.get(0).equals("Bryan"), "Select should only change the first record values");
        sendCommandToServer("UPDATE customers SET name = 'Alice' where name == 'Bryan';");

        //update specific fields based on condition - or nested conditions
        response = sendCommandToServer("UPDATE customers SET phone = 212 where name == 'Alice' AND email LIKE 'a';");
        assertTrue(response.contains("[OK]"), "Valid query was provided, however [OK] was not returned");

        response = sendCommandToServer("Select name from customers where phone == 212;");
        assertTrue(response.contains("[OK]"), "Valid query was provided, however [OK] was not returned");

        results = response.split("\n");

        entriesResult = new ArrayList<String>();
        for(int i = 2; i < results.length; i++){
            entriesResult.add(results[i].trim());
        }

        assertTrue(entriesResult.get(0).equals("Alice"), "Select should only change the first record values");

        response = sendCommandToServer("UPDATE customers SET phone = 456 where name == 'Alice' OR email LIKE '@';");
        assertTrue(response.contains("[OK]"), "Valid query was provided, however [OK] was not returned");
        response = sendCommandToServer("Select name from customers where phone == 456;");

        results = response.split("\n");

        entriesResult = new ArrayList<String>();
        for(int i = 2; i < results.length; i++){
            entriesResult.add(results[i].trim());
        }

        assertTrue(entriesResult.size() == 4, "The query should have changed all the values of the phone as the where condition is satisfied by all conditions");

        response = sendCommandToServer("UPDATE customers SET phone = 789 where id < 3 AND name LIKE 'B';");
        assertTrue(response.contains("[OK]"), "Valid query was provided, however [OK] was not returned");
        response = sendCommandToServer("Select name from customers where phone == 789;");

        results = response.split("\n");

        entriesResult = new ArrayList<String>();
        for(int i = 2; i < results.length; i++){
            entriesResult.add(results[i].trim());
        }


        assertTrue(entriesResult.size() == 1, "The query should have changed all the values of the phone as the where condition is satisfied by all conditions");
        assertTrue(entriesResult.get(0).equals("Bob"), "Query was supposed to only change the value for one field");

        response = sendCommandToServer("UPDATE customers SET phone = 11111 where id < 3;");
        response = sendCommandToServer("Select name from customers where phone == 11111;");

        results = response.split("\n");

        entriesResult = new ArrayList<String>();
        for(int i = 2; i < results.length; i++){
            entriesResult.add(results[i].trim());
        }

        assertTrue(entriesResult.size() == 2, "only two entries match the select condition");
        assertTrue(entriesResult.get(0).equals("Alice"), "(Alice, id == 1), was supposed to change to phone = 11111");
        assertTrue(entriesResult.get(1).equals("Bob"), "(Bob, id == 2), was supposed to change to phone = 11111");


        //use 2 conditions
        response = sendCommandToServer("UPDATE customers SET phone = 2293 where id < 3 and name == 'Bob';");
        response = sendCommandToServer("Select name from customers where phone == 2293;");

        results = response.split("\n");

        entriesResult = new ArrayList<String>();
        for(int i = 2; i < results.length; i++){
            entriesResult.add(results[i].trim());
        }

        assertTrue(entriesResult.size() == 1, "only one entry matches the select condition");
        assertTrue(entriesResult.get(0).equals("Bob"), "(Bob) first return name entry, with the phone == 2293 was not updated properly");



        response = sendCommandToServer("UPDATE customers SET phone = 8888 where id == 4 OR name == 'Alice';");
        assertTrue(response.contains("[OK]"), "Valid query was provided, however [OK] was not returned");
        response = sendCommandToServer("Select name from customers where phone == 8888;");

        results = response.split("\n");

        entriesResult = new ArrayList<String>();
        for(int i = 2; i < results.length; i++){
            entriesResult.add(results[i].trim());
        }

        assertTrue(entriesResult.size() == 2, "only two entries match the select condition");
        assertTrue(entriesResult.get(0).equals("Alice"), "(Alice) first return name entry, with the phone == 8888 was not updated properly");
        assertTrue(entriesResult.get(1).equals("dave"), "(dave) first return name entry, with the phone == 8888 was not updated properly");



        //use nested conditions
        response = sendCommandToServer("UPDATE customers SET phone = 2323 where (id < 3 AND name == 'Alice');");
        assertTrue(response.contains("[OK]"), "Valid query was provided, however [OK] was not returned");
        response = sendCommandToServer("Select name from customers where phone == 2323;");

        results = response.split("\n");

        entriesResult = new ArrayList<String>();
        for(int i = 2; i < results.length; i++){
            entriesResult.add(results[i].trim());
        }


        assertTrue(entriesResult.size() == 1, "only one entry match the select condition");
        assertTrue(entriesResult.get(0).equals("Alice"), "(Alice) return name entry, with the phone == 2323 was not updated properly");


        response = sendCommandToServer("UPDATE customers SET phone = 9090 where ((name LIKE 'a' AND id > 3) OR id == 1) ;");
        response = sendCommandToServer("Select name from customers where phone == 9090;");


        results = response.split("\n");

        entriesResult = new ArrayList<String>();
        for(int i = 2; i < results.length; i++){
            entriesResult.add(results[i].trim());
        }


        assertTrue(entriesResult.size() == 2, "only two entries match the select condition");
        assertTrue(entriesResult.get(0).equals("Alice"), "(Alice) first return name entry, with the phone == 9090 was not updated properly");
        assertTrue(entriesResult.get(1).equals("dave"), "(dave) first return name entry, with the phone == 9090 was not updated properly");


        response = sendCommandToServer("UPDATE customers SET phone = 1221 where (name LIKE 'A') AND (id == 1) ;");
        response = sendCommandToServer("Select name from customers where phone == 1221;");


        results = response.split("\n");

        entriesResult = new ArrayList<String>();
        for(int i = 2; i < results.length; i++){
            entriesResult.add(results[i].trim());
        }

        assertTrue(entriesResult.size() == 1, "only one entry matches the select condition");
        assertTrue(entriesResult.get(0).equals("Alice"), "Return should be 'Alice', with the update condition changing the value of the phone");




        //update with no attribute found
        response = sendCommandToServer("UPDATE customers SET phone = 4567 where occupation = 'doctor';");
        assertTrue(response.contains("[ERROR]"), "Was supposed to return an [ERROR], if attribute name is not found");
        response = sendCommandToServer("Select name from customers where phone == 4567;");

        results = response.split("\n");

        entriesResult = new ArrayList<String>();
        for(int i = 2; i < results.length; i++){
            entriesResult.add(results[i].trim());
        }

        assertTrue(entriesResult.size() == 0, "supposed to return no entries that match the select statement condition");

        response = sendCommandToServer("SELECT name from customers where ((name LIKE 'a' AND id < 4) OR id == 1);");
        assertTrue(response.contains("[OK]"), "Opening and closing brackets index error in query");
        results = response.split("\n");

        entriesResult = new ArrayList<String>();
        for(int i = 2; i < results.length; i++){
            entriesResult.add(results[i].trim());
        }

        assertTrue(entriesResult.size() == 2, "only one entry matches the select condition");
        assertTrue(entriesResult.get(0).equals("Alice"), "Only two eligible entries that match the condition was supposed to be returned - Alice and Charlie");
        assertTrue(entriesResult.get(1).equals("Charlie"), "Only two eligible entries that match the condition was supposed to be returned - Alice and Charlie");




        //test invalid statements - with brackets
        response = sendCommandToServer("UPDATE customers SET phone = 9090 where (((name LIKE 'a' AND id > 3) OR id == 1;");
        assertTrue(response.contains("[ERROR]"), "Opening and closing brackets index error in query");

        response = sendCommandToServer("UPDATE customers SET phone = 9090 where (((name LIKE 'a' AND id > 3))))) OR id == 1);");
        assertTrue(response.contains("[ERROR]"), "Opening and closing brackets index error in query");

        response = sendCommandToServer("UPDATE customers SET phone = 9090 where (name LIKE 'a' AND id > 3 OR id == 1)));");
        assertTrue(response.contains("[ERROR]"), "Opening and closing brackets index error in query");

        response = sendCommandToServer("UPDATE customers SET phone = 9090 where (name LIKE 'a' AND id > 3 OR id == 1)));");
        assertTrue(response.contains("[ERROR]"), "Opening and closing brackets index error in query");

    }

    @Test
    public void TestNull(){
        sendCommandToServer("create database test;");
        sendCommandToServer("use test;");
        sendCommandToServer("create table testTable;");
        String response = sendCommandToServer("Alter table testTable add name;");
        sendCommandToServer("insert into testTable values ('Bob');");
        sendCommandToServer("insert into testTable values ('Charles');");
        response = sendCommandToServer("select * from testTable;");

        assertTrue(response.contains("Charles"), "Table should have 2 entries - Bob & Charles");
        assertTrue(response.contains("Bob"), "Table should have 2 entries - Bob & Charles");

        response = sendCommandToServer("aLtEr TaBlE testTable add age;");
        sendCommandToServer("insert into testTable values ('Jerry', 25);");
        response = sendCommandToServer("select * from testTable;");
        String result [] = response.split("\n");
        ArrayList<String> entries = new ArrayList<String>();

        for(int i = 2; i < result.length; i++){
            entries.add(result[i]);
        }

        assertTrue(entries.size() == 3, "Table should have 3 entries - Bob, Charles & Jerry");

        response = sendCommandToServer("Select id from testTable where age == null;");

        result = response.split("\n");
        entries = new ArrayList<String>();

        for(int i = 2; i < result.length; i++){
            entries.add(result[i]);
        }
        assertTrue(entries.size() == 2, "Table should have 2 entries - Bob, Charles");
        assertTrue(response.contains("1"), "Table should have 2 entries - Bob, Charles");
        assertTrue(response.contains("2"), "Table should have 2 entries - Bob, Charles");
        assertTrue(!response.contains("3"), "Table should have 2 entries - Bob, Charles");



        response = sendCommandToServer("Select id from testTable where age != null;");
        result = response.split("\n");
        entries = new ArrayList<String>();

        for(int i = 2; i < result.length; i++){
            entries.add(result[i]);
        }
        assertTrue(entries.size() == 1, "Table should have 2 entries - Bob, Charles");
        assertTrue(response.contains("3"), "Table should have 2 entries - Bob, Charles");



        response = sendCommandToServer("Update testTable SET name = null WHERE name LIKE 'ry';");
        response = sendCommandToServer("Select id from testTable where name != null;");

        result = response.split("\n");
        entries = new ArrayList<String>();

        for(int i = 2; i < result.length; i++){
            entries.add(result[i]);
        }
        assertTrue(entries.size() == 2, "Table should have 2 entries - Bob, Charles");
        assertTrue(response.contains("1"), "Table should have 2 entries - Bob, Charles");
        assertTrue(response.contains("2"), "Table should have 2 entries - Bob, Charles");
        assertTrue(!response.contains("3"), "Table should have 2 entries - Bob, Charles");

        //check for positive and negative numbers
        //check for true/false values
    }

    @Test
    public void TestAttributeName(){
        String database = generateRandomName();

        //select - update - alter
        sendCommandToServer("create database "+ database + ";");
        sendCommandToServer("use "+database+ ";");

        String response = sendCommandToServer("create table marks (Name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Dave', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Clive', 20, FALSE);");


        response = sendCommandToServer("create table coursework (task, submission);");
        response = sendCommandToServer("INSERT INTO coursework VALUES ('OXO', 3);");
        sendCommandToServer("INSERT INTO coursework VALUES ('DB', 1);");
        sendCommandToServer("INSERT INTO coursework VALUES ('OXO', 4);");
        sendCommandToServer("INSERT INTO coursework VALUES ('STAG', 2);");

        //Select - w/o conditions
        response = sendCommandToServer("Select coursework.task from coursework;");

        String queryResult [] = response.split("\n");

        ArrayList<String> entries = new ArrayList<String>();
        for(int i = 2; i < queryResult.length; i++){
            entries.add(queryResult[i]);
        }

        assertTrue(response.contains("[OK]"), "");
        assertTrue(!queryResult[1].contains("coursework.task"), "");
        assertTrue(queryResult[1].contains("task"), "");

        assertTrue(entries.size() == 4, "");
        assertTrue(entries.get(0).contains("OXO"),  "");
        assertTrue(entries.get(1).contains("DB"), "");
        assertTrue(entries.get(2).contains("OXO"), "");
        assertTrue(entries.get(3).contains("STAG"), "");

        //Select - w conditions
        response = sendCommandToServer("Select coursework.task from coursework where coursework.id == 1;");
        queryResult = response.split("\n");

        entries = new ArrayList<String>();
        for(int i = 2; i < queryResult.length; i++){
            entries.add(queryResult[i]);
        }

        assertTrue(response.contains("[OK]"), "");
        assertTrue(entries.size() == 1, "");
        assertTrue(entries.get(0).contains("OXO"),  "");

        //error
        response = sendCommandToServer("Select marks.task from coursework where coursework.id == 1;");
        assertTrue(response.contains("[ERROR]"), "");

        response = sendCommandToServer("Select task from coursework where marks.id == 1;");
        assertTrue(response.contains("[ERROR]"), "");


        //Alter - add
        response = sendCommandToServer("Alter table coursework add coursework.subject;");
        assertTrue(response.contains("[OK]"), "");

        response = sendCommandToServer("select subject from coursework;");
        assertTrue(response.contains("[OK]"), "");
        assertTrue(!response.contains("coursework.subject"), "");
        assertTrue(response.contains("subject"), "");

        //Alter - add [ERROR]
        response = sendCommandToServer("Alter table coursework add marks.subject;");
        assertTrue(response.contains("[ERROR]"), "");

        //Alter - drop
        response = sendCommandToServer("Alter table coursework drop coursework.subject;");
        assertTrue(response.contains("[OK]"), "");

        response = sendCommandToServer("select subject from coursework;");
        assertTrue(response.contains("[ERROR]"), "");

        //Update
        response = sendCommandToServer("Update coursework set coursework.task = 'newAssignment' where coursework.id == 1;");
        assertTrue(response.contains("[OK]"), "");
        response = sendCommandToServer("SEleCt task from coursework where coursework.id == 1;");
        assertTrue(response.contains("[OK]"), "");
        assertTrue(response.contains("newAssignment"), "");

        //Update - [ERROR]
        response = sendCommandToServer("Update coursework set marks.task = 'newAssignment' where coursework.id == 1;");
        assertTrue(response.contains("[ERROR]"), "");

        response = sendCommandToServer("Update coursework set coursework.task = 'newAssignment' where marks.id == 1;");
        assertTrue(response.contains("[ERROR]"), "");

        //reset for JOIN
        response = sendCommandToServer("Update table coursework set marks.task = 'OXO' where coursework.id == 1;");


        //JOIN
        response = sendCommandToServer("JOIN coursework AND marks ON coursework.submission AND marks.id;");
        assertTrue(response.contains("[OK]"), "");

        String result [] = response.split("\n");
        entries = new ArrayList<String>();

        for(int i = 2; i < result.length; i++){
            entries.add(result[i]);
        }


        assertTrue(entries.size() == 4, "4 entries were expected to be returned as a result of the JOIN");

        //JOIN - [ERROR]
        response = sendCommandToServer("JOIN coursework AND marks ON tablerandom1.submission AND marks.id);");
        assertTrue(response.contains("[ERROR]"), "");


    }

}
