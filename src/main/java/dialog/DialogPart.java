package dialog;

import static dialog.constants.Speeches.PLEASE_REPEAT;

import java.io.IOException;

import dialog.enumerations.Category;
import dialog.speech.Utterance;
import dialog.speech.Word;

import edu.cmu.sphinx.api.LiveSpeechRecognizer;

public abstract class DialogPart implements DialogSetup {

    protected LiveSpeechRecognizer recognizer;

    protected DialogPart() {
        try {
            recognizer = new LiveSpeechRecognizer(configureRecognizer(true, getDialogPartGrammar()));
        } catch (IOException exception) {
            System.out.println("A problem with speech recognition has occurred.");
        }
    }

    protected abstract String getDialogPartGrammar();

    protected abstract void actDialogPart() throws Exception;

    protected Word listenForWord(Category category) throws Exception {
        Utterance utterance = recognizeSpeech(recognizer, true); // Always clear the cached microphone data. Use "false" otherwise.

        Word word = utterance.findWordsFromCategoryAndCreateWord(category);

        while (word.isEmptyWord()) {
            synthesizeSpeech(PLEASE_REPEAT);
            word = recognizeSpeech(recognizer, false).findWordsFromCategoryAndCreateWord(category);
        }

        return word;
    }

    protected String listenForString(Category category) throws Exception {
        Utterance utterance = recognizeSpeech(recognizer, true); // Always clear the cached microphone data. Use "false" otherwise.

        String string = utterance.findWordsFromCategoryAndCreateString(category);

        while (string.isEmpty()) {
            synthesizeSpeech(PLEASE_REPEAT);
            string = recognizeSpeech(recognizer, false).findWordsFromCategoryAndCreateString(category);
        }

        return string;
    }

}
