package hasoffer.core.test;

import hasoffer.core.analysis.CJKAnalyzerGetWord;
import hasoffer.core.analysis.IGetWord;
import hasoffer.core.analysis.StandardAnalyzerGetWord;

public class WordTest {
    public static void main(String[] args) throws Exception {
        String text = " APPLE iPhone 5S 64GB | GOLD | iOS9 IMPORTED ' UNLOCKED ";

        IGetWord getWord = new StandardAnalyzerGetWord();
        String[] words = getWord.getWords(text);
        showWords(words);

//        getWord = new StopAnalyzerGetWord();
//        words = getWord.getWords(text);
//        showWords(words);

        getWord = new CJKAnalyzerGetWord();
        words = getWord.getWords(text);
        showWords(words);
    }

    private static void showWords(String[] words) {
        for (String word : words) {
            System.out.print(word + "\t,\t");
        }
        System.out.println();
    }
}