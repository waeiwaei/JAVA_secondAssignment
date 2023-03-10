package edu.uob;

import java.io.*;
import java.lang.reflect.Array;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.ArrayList;


/** This class implements the DB server. */
public class DBServer {

    private static final char END_OF_TRANSMISSION = 4;
    private String storageFolderPath;


    public static void main(String args[]) throws IOException {

        String fileSeparator = File.separator;

        //example command to write to file
        //Structure to store tables
        ArrayList <ArrayList<String>> tables = new ArrayList<ArrayList<String>>();
        String command = "CREATE TABLE marks (name, mark, pass);";

        //store individual tokens in String object array
        String[] token;
        token = command.split("\s");

        //identify the number of columns to create
        int counter = 0;
        for (int i = 0; i < command.length(); i++){
            char c = command.charAt(i);
            if(c == '('){
                while(c != ')'){
                    c=command.charAt(i);
                    if(c == ','){
                        counter++;
                    }
                    i++;
                }
                counter++;
            }
        }

        //add columns - based on comma delimiter for columns
        tables.add(new ArrayList<String>());
        for(int i = 0; i < counter; i++) {
            tables.get(0).add(null);
        }


        //input to a new file - write to it
        FileWriter f2 = new FileWriter(".."+fileSeparator+"cw-db"+fileSeparator+"databases"+fileSeparator+ token[2]);
        BufferedWriter bw = new BufferedWriter(f2);

        bw.write("id");
        bw.write("\t");
        bw.write(token[3].replaceAll("[,.();]",""));
        bw.write("\t");
        bw.write(token[4].replaceAll("[,.();]",""));
        bw.write("\t");
        bw.write(token[5].replaceAll("[,.();]",""));


        bw.close();


        // Read from a file
        File f = new File(".."+fileSeparator+"people.tab");
        FileInputStream fiStream = new FileInputStream(f);
        BufferedReader br = new BufferedReader(new InputStreamReader(fiStream));

        String[] columnHeader;
        //read the first row - column header
        String s = br.readLine();
        columnHeader = s.split("\t");

        for(int i = 0; i < columnHeader.length;i++){
            System.out.println(columnHeader[i]);
        }

        while(s != null){
            s = br.readLine();
            if(s.isEmpty()){
                break;
            }
            String [] sr = s.split("\t");
            System.out.println(sr[2]);
        }



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
