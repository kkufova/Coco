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

import static dialog.constants.Speeches.PLEASE_ANSWER;

import java.io.IOException;

import dialog.enumerations.Category;
import dialog.recognition.SpeechRecognizer;
import dialog.speech.Utterance;
import dialog.speech.Word;

/**
 * An abstract parent class of each dialog part.
 *
 * The class is responsible for finding the category keywords and strings in the spoken utterances,
 * for initiating the "please repeat" dialogs in case the required information was not included in the utterance,
 * and for initiating corrective dialogs.
 */

public abstract class DialogPart implements DialogSetup {

    protected SpeechRecognizer recognizer;

    protected DialogPart() {
        try {
            recognizer = new SpeechRecognizer(configureRecognizer(setUseGrammar(), getDialogPartGrammar()));
        } catch (IOException exception) {
            System.out.println("A problem with speech recognition has occurred.");
        }
    }

    protected abstract boolean setUseGrammar();

    protected abstract String getDialogPartGrammar();

    protected abstract void actDialogPart() throws Exception;

    protected Word listenForWord(Category category) throws Exception {
        Utterance utterance = recognizeSpeech(recognizer, true); // Always clear the cached microphone data. Use "false" otherwise.

        Word word = utterance.findWordsFromCategoryAndCreateWord(category);

        while (utterance.isUnknown() || word.isEmptyWord()) {
            if (utterance.isUnknown()) {
                DialogCorrective dialogCorrective = new DialogCorrective(); // Initiate a new corrective dialog.
                word = dialogCorrective.getWordFromCorrectiveDialog(category);
                // It is necessary to set the grammar-based recognition again:
                recognizer = new SpeechRecognizer(configureRecognizer(setUseGrammar(), getDialogPartGrammar()));
                break;
            }
            if (word.isEmptyWord()) {
                synthesizeSpeech(PLEASE_ANSWER);
                utterance = recognizeSpeech(recognizer, true);
                word = utterance.findWordsFromCategoryAndCreateWord(category);
            }
        }

        return word;
    }

    protected String listenForString(Category category) throws Exception {
        Utterance utterance = recognizeSpeech(recognizer, true); // Always clear the cached microphone data. Use "false" otherwise.

        String string = utterance.findWordsFromCategoryAndCreateString(category);

        while (utterance.isUnknown() || string.isEmpty()) {
            if (utterance.isUnknown()) {
                DialogCorrective dialogCorrective = new DialogCorrective(); // Initiate a new corrective dialog.
                string = dialogCorrective.getStringFromCorrectiveDialog(category);
                // It is necessary to set the grammar-based recognition again:
                recognizer = new SpeechRecognizer(configureRecognizer(setUseGrammar(), getDialogPartGrammar()));
                break;
            }
            if (string.isEmpty()) {
                synthesizeSpeech(PLEASE_ANSWER);
                utterance = recognizeSpeech(recognizer, true);
                string = utterance.findWordsFromCategoryAndCreateString(category);
            }
        }

        return string;
    }

}
