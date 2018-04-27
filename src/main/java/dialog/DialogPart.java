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

        while (utterance.isUnknown() || word.isEmptyWord()) {
            if (utterance.isUnknown()) {
                DialogCorrective dialogCorrective = new DialogCorrective(); // Initiate a new corrective dialog.
                word = dialogCorrective.getWordFromCorrectiveDialog(category);
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
