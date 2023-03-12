package edu.uob;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class Tokenizer {

    String command;


    public Tokenizer(String command){
        this.command = command;
    }

    public ArrayList<String> tokenize(){
        String[] specialChar = {",", ";", "'", ")", "(", ".", "+"};

        // lookahead matches a position in the input string that is followed by one of the special characters, string array or a whitespace character
        // lookbehind matches a position in the input string that is preceded by one of the special characters, string array or a whitespace character.
        String regex = "(?=[" + String.join("", specialChar) + "\\s])|(?<=[" + String.join("", specialChar) + "\\s])";

        String[] tokensArray = command.split(regex);

        ArrayList<String> tokens = new ArrayList<String>();
        for (String token : tokensArray) {
            if (!token.trim().isEmpty()) {
                tokens.add(token.trim());
            }
        }

        return tokens;

    }

}
