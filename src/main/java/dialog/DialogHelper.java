/*
 * Coco the Dialogue System
 * https://github.com/kkufova/Coco
 *
 * Copyright 2018 Klara Kufova
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dialog;

import java.util.HashMap;

import dialog.speech.Word;

import org.apache.commons.lang.WordUtils;

/**
 * This class contains various helper methods needed by the dialog, such as miscellaneous conversions,
 * methods used for string capitalization, and other.
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

    public Word convertSize(Word size) {
        HashMap<Word, Word> keys = new HashMap<>();

        keys.put(new Word("universal"), new Word("Universal"));
        keys.put(new Word("xs"), new Word("XS"));
        keys.put(new Word("s"), new Word("S"));
        keys.put(new Word("m"), new Word("M"));
        keys.put(new Word("l"), new Word("L"));
        keys.put(new Word("xl"), new Word("XL"));
        keys.put(new Word("xxl"), new Word("XXL"));
        keys.put(new Word("two"), new Word("2"));
        keys.put(new Word("three"), new Word("3"));
        keys.put(new Word("four"), new Word("4"));
        keys.put(new Word("five"), new Word("5"));
        keys.put(new Word("six"), new Word("6"));
        keys.put(new Word("seven"), new Word("7"));
        keys.put(new Word("eight"), new Word("8"));
        keys.put(new Word("nine"), new Word("9"));

        return keys.get(size);
    }

}
