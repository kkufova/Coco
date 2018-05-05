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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dialog.enumerations.Category;

/**
 * This class represents one utterance of a speaker in the dialog
 * and contains a number of methods that allow you to further analyze the utterance.
 */

public class Utterance {

    private String utterance;

    private List<Word> words = new ArrayList<>();

    private static final String PATH_TO_CATEGORY_GRAMMARS = "src/main/resources/speech-recognition/grammars/category-grammars/";

    public Utterance(String utterance) {
        this.utterance = utterance;
    }

    public void setWords(List<Word> words) {
        this.words = words;
    }

    public List<Word> findWordsFromCategory(Category category) throws IOException {
        String categoryName = category.toString().toLowerCase();

        List<Word> wordsInGrammar = new ArrayList<>();
        List<Word> foundWordsFromCategory = new ArrayList<>();

        FileReader fileReader = new FileReader(PATH_TO_CATEGORY_GRAMMARS + "category-" + categoryName + ".gram");
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line;

        String ruleName = getRuleName(categoryName);
        Pattern pattern = Pattern.compile("public <" + ruleName + ">");

        while ((line = bufferedReader.readLine()) != null) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                wordsInGrammar = findWordsInGrammar(line, bufferedReader);
                break;
            }
        }

        for (Word word : words) {
            for (Word wordInGrammar : wordsInGrammar) {
                if (word.equals(wordInGrammar)) {
                    foundWordsFromCategory.add(word);
                }
            }
        }

        return foundWordsFromCategory;
    }

    public String getRuleName(String categoryName) {
        if (categoryName.endsWith("ies")) {
            return categoryName.replace("ies", "y");   // E.g. deliveries -> delivery
        } else {
            return categoryName.substring(0, categoryName.length() - 1); // E.g. users -> user
        }
    }

    private List<Word> findWordsInGrammar(String line, BufferedReader bufferedReader) throws IOException {
        List<Word> wordsInGrammar = new ArrayList<>();
        Pattern entryPattern = Pattern.compile("([=|])\\s(\\D+)");

        // Find the first word:
        Matcher entryMatcher = entryPattern.matcher(line);
        if (entryMatcher.find()) {
            wordsInGrammar.add(new Word(entryMatcher.group(2)));
        }
        // Find all other words:
        while ((line = bufferedReader.readLine()) != null) {
            entryMatcher = entryPattern.matcher(line);
            if (entryMatcher.find()) {
                wordsInGrammar.add(new Word(entryMatcher.group(2)));
            }
        }

        return wordsInGrammar;
    }

    // Use this method in cases where more that one word is expected
    // (such as a multiple-word name like Rosie Elizabeth).
    private String createStringFromListOfWords(List<Word> listOfWords) {
        String result = "";

        if (listOfWords.isEmpty()) {
            return "";
        } else {
            for (Word word : listOfWords) {
                result = result.concat(word + " ");
            }
        }
        return result.trim();
    }

    // Use this method in cases where only single-word answer is expected
    // (such as "yes" or "seven") and you want to call other methods available
    // only for single Words.
    private Word createWordFromListOfWords(List<Word> listOfWords) {
        if (listOfWords.size() == 1) {
            return listOfWords.get(0);
        } else {
            return new Word("");
        }
    }

    public String findWordsFromCategoryAndCreateString(Category category) throws IOException {
        List<Word> wordsFromCategory = findWordsFromCategory(category);
        return createStringFromListOfWords(wordsFromCategory);
    }

    public Word findWordsFromCategoryAndCreateWord(Category category) throws IOException {
        List<Word> wordsFromCategory = findWordsFromCategory(category);
        return createWordFromListOfWords(wordsFromCategory);
    }

    public boolean isUnknown() {
        return utterance.equals("<unk>");
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        Utterance anotherUtterance = (Utterance) object;

        return utterance.equals(anotherUtterance.utterance) && words.equals(anotherUtterance.words);
    }

    @Override
    public int hashCode() {
        int result = utterance.hashCode();
        result = 31 * result + words.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return utterance;
    }

}
