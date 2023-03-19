package edu.uob;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.nio.file.Files;


/** This class implements the DB server. */
public class DBServer {

    private static final char END_OF_TRANSMISSION = 4;
    private String storageFolderPath;
    static Process pr;


    public static void main(String args[]) throws IOException {

        //tokenize commands from user
        //String command = "UPDATE marks SET mark = 38, age = 7 WHERE name == 'Clive' AND age == 3;";
        //String command = "UPDATE marks SET mark = 55, pass = FALSE WHERE name == 'Dave' OR id == 3;";
        //String command = "DELETE FROM marks WHERE mark == 55;";
        //String command = "ALTER TABLE marks ADD percentage;";
        //String command = "ALTER TABLE marks DROP name;";
        //String command = "SELECT marks.id, marks.name, marks.mark FROM marks WHERE id > 1 AND id < 3;";
        //String command = "SELECT * FROM marks where name == 'steve';";
        //String command = "SELECT * FROM marks WHERE name == 'Dave' AND mark == 55;";
        //String command = "CREATE TABLE marks (                        hello.name, mark, pass);";
        //String command = "CREATE TABLE coursework (task, submission);";
        //String command = "CREATE table testing2;";
        //String command = "USE testing;";
        //String command = "DROP TABLE marks;";
        //String command = "Drop        database testing;";
        //String command = "JOIN coursework AND marks ON submission AND id;";
        //String command = "join marks and coursework on id and submission;";
        //String command = "INSERT INTO customers VALUES ('dave', 'dave@example.com', 222212233);";
        //String command = "insert into customers values('name', age, e93);";
//        String command = "Join orders and customers on customerId and id;";
//////
//        String valueReturn="";
//
//        try{
//            Tokenizer tokenCommands = new Tokenizer(command);
//            Parser p = new Parser();
//            DBCmd value = p.parse(tokenCommands);
//            System.out.println("went through parser");
//
//
//            pr = new Process(value);
//            valueReturn = pr.query();
//            System.out.println(valueReturn);
//
//        }catch(Exception e){
//            valueReturn="[ERROR]\n";
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


        String valueReturn="";

        try{
            Tokenizer tokenCommands = new Tokenizer(command);
            Parser p = new Parser();
            DBCmd value = p.parse(tokenCommands);

            pr = new Process(value);
            valueReturn = pr.query();

        }catch(Exception e){
            valueReturn="[ERROR]\n";
        }

        return valueReturn;


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
