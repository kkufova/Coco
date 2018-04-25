package dialog;

import static dialog.constants.Speeches.PLEASE_ANSWER;

import java.io.IOException;

import dialog.enumerations.Category;
import dialog.recognition.SpeechRecognizer;
import dialog.speech.Utterance;
import dialog.speech.Word;

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

        if (utterance.isUnknown()) {
            DialogCorrective dialogCorrective = new DialogCorrective(); // Initiate a new corrective dialog.
            word = dialogCorrective.getWordFromCorrectiveDialog(category);
        }

        while (word.isEmptyWord()) {
            synthesizeSpeech(PLEASE_ANSWER);
            word = recognizeSpeech(recognizer, false).findWordsFromCategoryAndCreateWord(category);
        }

        return word;
    }

    protected String listenForString(Category category) throws Exception {
        Utterance utterance = recognizeSpeech(recognizer, true); // Always clear the cached microphone data. Use "false" otherwise.

        String string = utterance.findWordsFromCategoryAndCreateString(category);

        if (utterance.isUnknown()) {
            DialogCorrective dialogCorrective = new DialogCorrective(); // Initiate a new corrective dialog.
            string = dialogCorrective.getStringFromCorrectiveDialog(category);
        }

        while (string.isEmpty()) {
            synthesizeSpeech(PLEASE_ANSWER);
            string = recognizeSpeech(recognizer, false).findWordsFromCategoryAndCreateString(category);
        }

        return string;
    }

}
