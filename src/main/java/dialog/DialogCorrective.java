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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import dialog.adaptation.RuleLearner;
import dialog.adaptation.WordLearner;
import dialog.constants.Speeches;
import dialog.enumerations.Category;
import dialog.recognition.SpeechRecognizer;
import dialog.speech.Utterance;
import dialog.speech.Word;

class DialogCorrective implements DialogSetup {

    private static final String CORRECTIVE_DIALOG_GRAMMAR = "corrective-dialog";

    private SpeechRecognizer lmRecognizer;
    private SpeechRecognizer grammarRecognizer;

    private Utterance utterance;
    private Word requiredWord;
    private String requiredString;
    private Word answer;

    DialogCorrective() {
        try {
            lmRecognizer = new SpeechRecognizer(configureRecognizer(false, null));
            grammarRecognizer = new SpeechRecognizer(configureRecognizer(true, CORRECTIVE_DIALOG_GRAMMAR));
        } catch (IOException exception) {
            System.out.println("A problem with initializing the speech recognizers has occurred.");
        }
    }

    Word getWordFromCorrectiveDialog(Category category) throws Exception {
        synthesizeSpeech(Speeches.DO_NOT_UNDERSTAND);
        synthesizeSpeech(Speeches.PLEASE_REPEAT_ANSWER);

        utterance = recognizeSpeech(lmRecognizer, true);
        requiredWord = utterance.findWordsFromCategoryAndCreateWord(category);

        if (requiredWord.isEmptyWord()) {
            int numberOfAttempts = 1; // Set the number of attempts to suit your application's needs.
            for (int i = 0; i < numberOfAttempts; i++) {
                checkIfCorrectlyRecognized();
                if (answer.isPositive()) {
                    askUserForRequiredWord(category);
                } else {
                    askForRepeatingWord(category);
                }
                if (!requiredWord.isEmptyWord()) {
                    break;
                }
            }
        }

        while (requiredWord.isEmptyWord()) {
            synthesizeSpeech(Speeches.IT_LOOKS_LIKE);
            utterance = getWrittenUtterance();

            // As this is not a recognized speech, it is necessary to set the words manually:
            String[] wordsInUtterance = utterance.toString().split(" ");
            List<Word> words = new ArrayList<>();
            for (String wordInUtterance : wordsInUtterance) {
                words.add(new Word(wordInUtterance));
            }
            utterance.setWords(words);
            requiredWord = utterance.findWordsFromCategoryAndCreateWord(category);
            if (requiredWord.isEmptyWord()) {
                askUserForRequiredWord(category);
            }
        }

        RuleLearner ruleLearner = new RuleLearner();
        ruleLearner.addRuleToGrammar(utterance, requiredWord, category);

        synthesizeSpeech(Speeches.THANK_YOU_FOR);
        synthesizeSpeech(Speeches.LETS_CONTINUE);

        return requiredWord;
    }

    private void checkIfCorrectlyRecognized() throws Exception {
        // We need another recognizer:
        grammarRecognizer = new SpeechRecognizer(configureRecognizer(true, CORRECTIVE_DIALOG_GRAMMAR));

        synthesizeSpeech(Speeches.DID_I_HEAR);
        answer = recognizeSpeech(grammarRecognizer, true).findWordsFromCategoryAndCreateWord(Category.ANSWERS);
    }

    private void askUserForRequiredWord(Category category) throws Exception {
        lmRecognizer = new SpeechRecognizer(configureRecognizer(false, null));

        if (!category.equals(Category.ANSWERS)) {
            synthesizeSpeech(Speeches.NOT_ANSWER + " " + Speeches.I_HAVE_ASKED + category.getAbout() + ".");
        } else {
            synthesizeSpeech(Speeches.NOT_ANSWER);
        }

        synthesizeSpeech(Speeches.PLEASE_RESPOND);
        utterance = recognizeSpeech(lmRecognizer, true);
        requiredWord = utterance.findWordsFromCategoryAndCreateWord(category);
    }

    private void askForRepeatingWord(Category category) throws Exception {
        lmRecognizer = new SpeechRecognizer(configureRecognizer(false, null));

        synthesizeSpeech(Speeches.PLEASE_REPEAT_UTTERANCE);
        utterance = recognizeSpeech(lmRecognizer, true);
        requiredWord = utterance.findWordsFromCategoryAndCreateWord(category);
    }

    private Utterance getWrittenUtterance() {
        Scanner reader = new Scanner(System.in);
        System.out.print("You: ");
        return new Utterance(reader.nextLine().toLowerCase());
    }

    String getStringFromCorrectiveDialog(Category category) throws Exception {
        if (category.equals(Category.USERS)) {
            return actUnknownUserDialog();
        }

        synthesizeSpeech(Speeches.DO_NOT_UNDERSTAND);
        synthesizeSpeech(Speeches.PLEASE_REPEAT_ANSWER);

        utterance = recognizeSpeech(lmRecognizer, true);
        requiredString = utterance.findWordsFromCategoryAndCreateString(category);

        if (requiredString.isEmpty()) {
            int numberOfAttempts = 1; // Set the number of attempts to suit your application's needs.
            for (int i = 0; i < numberOfAttempts; i++) {
                checkIfCorrectlyRecognized();
                if (answer.isPositive()) {
                    askUserForRequiredString(category);
                } else {
                    askForRepeatingString(category);
                }
                if (!requiredString.isEmpty()) {
                    break;
                }
            }
        }

        while (requiredString.isEmpty()) {
            synthesizeSpeech(Speeches.IT_LOOKS_LIKE);
            utterance = getWrittenUtterance();

            // As this is not a recognized speech, it is necessary to set the word manually:
            String[] wordsInUtterance = utterance.toString().split(" ");
            List<Word> words = new ArrayList<>();
            for (String wordInUtterance : wordsInUtterance) {
                words.add(new Word(wordInUtterance));
            }
            utterance.setWords(words);
            requiredString = utterance.findWordsFromCategoryAndCreateString(category);
            if (requiredString.isEmpty()) {
                askUserForRequiredString(category);
            }
        }

        RuleLearner ruleLearner = new RuleLearner();
        ruleLearner.addRuleToGrammar(utterance, new Word(requiredString), category); // TODO: this should be further improved.

        synthesizeSpeech(Speeches.THANK_YOU_FOR);
        synthesizeSpeech(Speeches.LETS_CONTINUE);

        return requiredString;
    }

    private void askUserForRequiredString(Category category) throws Exception {
        lmRecognizer = new SpeechRecognizer(configureRecognizer(false, null));

        if (!category.equals(Category.ANSWERS)) {
            synthesizeSpeech(Speeches.NOT_ANSWER + " " + Speeches.I_HAVE_ASKED + category.getAbout() + ".");
        } else {
            synthesizeSpeech(Speeches.NOT_ANSWER);
        }

        synthesizeSpeech(Speeches.PLEASE_RESPOND);
        utterance = recognizeSpeech(lmRecognizer, true);
        requiredString = utterance.findWordsFromCategoryAndCreateString(category);
    }

    private void askForRepeatingString(Category category) throws Exception {
        lmRecognizer = new SpeechRecognizer(configureRecognizer(false, null));

        synthesizeSpeech(Speeches.PLEASE_REPEAT_UTTERANCE);
        utterance = recognizeSpeech(lmRecognizer, true);
        requiredString = utterance.findWordsFromCategoryAndCreateString(category);
    }

    private String actUnknownUserDialog() throws Exception {
        synthesizeSpeech(Speeches.NOT_IN_DATABASE);
        synthesizeSpeech(Speeches.I_WILL_REMEMBER);

        String newUserName = getNewUserName();
        String[] listOfNames = newUserName.split(" ");
        WordLearner wordLearner = new WordLearner();

        for (String name : listOfNames) {
            Word nameWord = new Word(name);
            wordLearner.addWordToCategoryGrammar(nameWord, Category.USERS);
        }

        return newUserName;
    }

    private String getNewUserName() {
        Scanner reader = new Scanner(System.in);
        System.out.print("Your name: ");
        return reader.nextLine().toLowerCase();
    }

}
