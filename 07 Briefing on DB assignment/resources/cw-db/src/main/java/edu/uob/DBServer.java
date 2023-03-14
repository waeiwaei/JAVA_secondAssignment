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
    static String fileSeparator = File.separator;
    static String currentDatabase;
    static String secondDatabase;
    static String currentTable;
    static String secondaryTable;
    static int numberOfColumns1;


    public static void main(String args[]) throws Exception {

        //tokenize commands from user
        //String command = "UPDATE marks SET mark = 38, age = 7 WHERE name == 'Clive';";
        String command = "UPDATE marks SET mark = 38 WHERE name == 'Clive' AND age == 23;";
        //String command = "DELETE FROM table1 WHERE age == 29;";
        //String command = "ALTER TABLE marks ADD percentage;";
        //String command = "SELECT * FROM marks;";
        //String command = "SELECT * FROM marks WHERE name != 'Dave';";
        //String command = "CREATE TABLE marks (                        name, mark, pass);";
        //String command = "CREATE TABLE marks;";
        //String command = "CREATE DATABASE testing;";
        //String command = "USE marks;";
        //String command = "DROP TABLE coursework;";
        //String command = "Drop        database marks;";
        //String command = "JOIN coursework AND marks ON submission AND id;";
        //String command = "INSERT INTO coursework VALUES('name', 20, 30);";


        Tokenizer tokenCommands = new Tokenizer(command);
        Parser p = new Parser();
        DBCmd value = p.parse(tokenCommands);

        System.out.println("Command type - "+ value.commandtype);
        System.out.println("DB Name - " + value.DBName);
        System.out.println("Table names - " + value.TableNames);
        System.out.println("Col names - " + value.colNames);
        System.out.println("Values list - " + value.values);
        System.out.println("Alteration Type - "+ value.alterationType);
        System.out.println("Name Values - "+ value.nameValueList);
        System.out.println("Conditions - "+ value.conditions);

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


        return "";
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
