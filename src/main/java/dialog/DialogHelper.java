package dialog;

import java.util.HashMap;

import dialog.speech.Word;

import org.apache.commons.lang.WordUtils;

/**
 * This class contains helper methods needed by the dialog.
 */

public class DialogHelper {

    public String capitalizeName(String name) {
        if (name != null) {
            return WordUtils.capitalize(name);
        }
        return null;
    }

    public int convertNumberToInt(Word number) {
        HashMap<Word, Integer> keys = new HashMap<>();

        keys.put(new Word("oh"), 0);
        keys.put(new Word("zero"), 0);
        keys.put(new Word("one"), 1);
        keys.put(new Word("two"), 2);
        keys.put(new Word("three"), 3);
        keys.put(new Word("four"), 4);
        keys.put(new Word("five"), 5);
        keys.put(new Word("six"), 6);
        keys.put(new Word("seven"), 7);
        keys.put(new Word("eight"), 8);
        keys.put(new Word("nine"), 9);
        keys.put(new Word("ten"), 10);
        keys.put(new Word("eleven"), 11);
        keys.put(new Word("twelve"), 12);
        keys.put(new Word("thirteen"), 13);
        keys.put(new Word("fourteen"), 14);
        keys.put(new Word("fifteen"), 15);
        keys.put(new Word("sixteen"), 16);
        keys.put(new Word("seventeen"), 17);
        keys.put(new Word("eighteen"), 18);
        keys.put(new Word("nineteen"), 19);
        keys.put(new Word("twenty"), 20);
        keys.put(new Word("thirty"), 30);
        keys.put(new Word("forty"), 40);
        keys.put(new Word("fifty"), 50);
        keys.put(new Word("sixty"), 60);
        keys.put(new Word("seventy"), 70);
        keys.put(new Word("eighty"), 80);
        keys.put(new Word("ninety"), 90);
        keys.put(new Word("hundred"), 100);
        keys.put(new Word("thousand"), 1000);

        return keys.get(number);
    }

    public Word convertCardinalToOrdinal(int cardinal) {
        HashMap<Integer, Word> keys = new HashMap<>();

        keys.put(1, new Word("first"));
        keys.put(2, new Word("second"));
        keys.put(3, new Word("third"));
        keys.put(4, new Word("fourth"));
        keys.put(5, new Word("fifth"));
        keys.put(6, new Word("sixth"));
        keys.put(7, new Word("seventh"));
        keys.put(8, new Word("eighth"));
        keys.put(9, new Word("ninth"));
        keys.put(10, new Word("tenth"));

        return keys.get(cardinal);
    }

}