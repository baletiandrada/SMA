package com.example.booksapp;

import com.example.booksapp.dataModels.WordModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FullTextSearch {

    public static ArrayList<String> searchForText(String variable, ArrayList<String> texts_list){
        ArrayList<WordModel> word_objects = new ArrayList<>();
        ArrayList<String> list_of_words = new ArrayList<>();

        Character[] end_characters = {'.', ',', ';', '!', '?'};
        ArrayList<Character> end_characters_arraylist = new ArrayList<>();
        end_characters_arraylist.addAll(Arrays.asList(end_characters));
        for(String text:texts_list) {
            List<String> words = Arrays.asList(text.split("\\s+"));
            for (String word : words) {
                if (end_characters_arraylist.contains(word.charAt(word.length() - 1))) {
                    String substring = word.substring(0, word.length() - 1);
                    word = substring;
                }
                if (!listContains(word_objects, word.toLowerCase())) {
                    WordModel word_object = new WordModel(word, calculate_levenshtein(variable.toLowerCase(),word.toLowerCase()));
                    word_objects.add(word_object);
                }
            }
        }
        Collections.sort(word_objects);
        for(WordModel obj : word_objects){
            if(list_of_words.size()<5)
                list_of_words.add(obj.getWord());
        }
        return list_of_words;
    }

    public static boolean listContains(ArrayList<WordModel> list, String variable){
        for(WordModel item:list)
           if(item.getWord().equals(variable))
               return true;
        return false;
    }


    public static int calculate_levenshtein(String s1, String s2) {
        ArrayList<Character> s1_letters = new ArrayList<>();
        ArrayList<Character> s2_letters = new ArrayList<>();
        int i, j, difference_cnt=0;
        for(i=0;i<s1.length();i++)
            s1_letters.add(s1.charAt(i));
        for(i=0;i<s2.length();i++)
            s2_letters.add(s2.charAt(i));
        if(s1.length()<=s2.length()) {
            for(i=0;i<s1.length();i++) {
                if(!s1_letters.get(i).equals(s2_letters.get(i)))
                    difference_cnt++;
            }
        }
        else {
            for(i=0;i<s2.length();i++){
                if(!s1_letters.get(i).equals(s2_letters.get(i)))
                    difference_cnt++;
            }
        }
        difference_cnt += Math.abs(s1.length()-s2.length());
        return difference_cnt;
    }

    public static boolean wordsAreSimilar(String word, String variable){
        ArrayList<Character> word_letters = new ArrayList<>();
        ArrayList<Character> variable_letters = new ArrayList<>();
        int i, j;

        for(i=0;i<word.length();i++)
            word_letters.add(word.charAt(i));
        for(i=0;i<variable.length();i++)
            variable_letters.add(variable.charAt(i));

        Collections.sort(word_letters);
        Collections.sort(variable_letters);

        i=0; j=0;
        int counter=0;
            while(j<variable_letters.size() && i<word_letters.size()){
                if(word_letters.get(i).equals(variable_letters.get(j))){
                    i++; j++;
                    counter++;
                }
                else if(word_letters.get(i).compareTo(variable_letters.get(j))>0)
                    j++;
                else i++;
            }


        int diff = variable_letters.size() + word_letters.size() - 2 * counter;
        if(diff<3)
            return true;

        return false;
    }
}
