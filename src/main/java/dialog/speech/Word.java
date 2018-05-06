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

package dialog.speech;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * This class represents one word of an utterance and provides a number of methods used for
 * further analysis of the word.
 */

public class Word {

    private String word;

    public Word(String word) {
        this.word = word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public boolean isEmptyWord() {
        return word.isEmpty();
    }

    public boolean isPositive() {
        Set<String> positiveAnswers = new HashSet<>();
        positiveAnswers.addAll(Arrays.asList("alright", "ok", "sure", "yes", "yeah"));

        return (positiveAnswers.contains(word));
    }

    public boolean isNumberLessOrEqualThanTen() {
        Set<String> numbers = new HashSet<>();
        numbers.addAll(Arrays.asList("oh", "zero", "one", "two", "three", "four", "five", "six", "seven", "eight",
                "nine", "ten"));

        return (numbers.contains(word));
    }

    public boolean isNumberBiggerThanTen() {
        Set<String> numbers = new HashSet<>();
        numbers.addAll(Arrays.asList("eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen",
                "eighteen", "nineteen", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety",
                "hundred", "thousand"));

        return (numbers.contains(word));
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        Word anotherWord = (Word) object;

        return word.equals(anotherWord.word);
    }

    @Override
    public int hashCode() {
        return word.hashCode();
    }

    @Override
    public String toString() {
        return word;
    }

}
