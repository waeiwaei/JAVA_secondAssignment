package edu.uob;

import java.util.ArrayList;

public class Tokenizer {

    public ArrayList<String> tokens;
    public int current_token_index = -1;

    public Tokenizer(String command){
        tokenize(command);
        current_token_index = - 1;
    }

    private void tokenize(String command) {
        String[] specialChar = {",", ";", "'", ")", "(", ".", "+"};

        // lookahead matches a position in the input string that is followed by one of the special characters, string array or a whitespace character
        // lookbehind matches a position in the input string that is preceded by one of the special characters, string array or a whitespace character.
        String regex = "(?=[" + String.join("", specialChar) + "\\s])|(?<=[" + String.join("", specialChar) + "\\s])";

        String[] tokensArray = command.split(regex);

        this.tokens = new ArrayList<String>();
        for (String token : tokensArray) {
            if (!token.trim().isEmpty()) {
                tokens.add(token.trim());
            }
        }
    }

    public String nextToken(){
        current_token_index++;
        return tokens.get(current_token_index);
    }

    public String previousToken(){
        current_token_index--;
        return tokens.get(current_token_index);
    }

    public int getCurrent_token_index(){
        return current_token_index;
    }

    public int indexOf(String input){

        return tokens.indexOf(input);
    }

    public String getCurrentToken(){
        return tokens.get(current_token_index);
    }

    public boolean hasMoreTokens(){
        return current_token_index<tokens.size()-1;
    }

    public boolean contains(String s){
        return tokens.contains(s);
    }

    public void setTokenIndex(int val){
        current_token_index = val;
    }

}
