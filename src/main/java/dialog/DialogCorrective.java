package dialog;

import java.io.IOException;
import java.util.Scanner;

import dialog.constants.Speeches;
import dialog.enumerations.Category;
import dialog.recognition.SpeechRecognizer;
import dialog.speech.Utterance;
import dialog.speech.Word;

class DialogCorrective implements DialogSetup {

    private static final String CORRECTIVE_DIALOG_GRAMMAR = "";
    private boolean useGrammar = false;

    private SpeechRecognizer correctiveRecognizer;

    DialogCorrective() {
        try {
            correctiveRecognizer = new SpeechRecognizer(configureRecognizer(useGrammar, CORRECTIVE_DIALOG_GRAMMAR));
        } catch (IOException exception) {
            System.out.println("A problem with speech recognition has occurred.");
        }
    }

    Word getWordFromCorrectiveDialog(Category category) throws Exception {
        synthesizeSpeech(Speeches.DO_NOT_UNDERSTAND);
        synthesizeSpeech(Speeches.PLEASE_REPEAT);

        Utterance utterance = recognizeSpeech(correctiveRecognizer, true);
        Word word = utterance.findWordsFromCategoryAndCreateWord(category);

        while (word.isEmptyWord()) {
            if (!category.equals(Category.ANSWERS)) {
                synthesizeSpeech(Speeches.NOT_ANSWER + " " + Speeches.I_HAVE_ASKED + category.getAbout() + ".");
            } else {
                synthesizeSpeech(Speeches.NOT_ANSWER);
            }
            synthesizeSpeech(Speeches.PLEASE_RESPOND);
            utterance = recognizeSpeech(correctiveRecognizer, true);
            word = utterance.findWordsFromCategoryAndCreateWord(category);
        }
        synthesizeSpeech(Speeches.THANK_YOU_FOR);

        // ask if this is really what the user said

        // add new grammar rule

        // let's continue with our dialog!

        return word;
    }

    String getStringFromCorrectiveDialog(Category category) throws Exception {
        if (category.equals(Category.USERS)) {
            synthesizeSpeech(Speeches.NOT_IN_DATABASE);
            synthesizeSpeech(Speeches.I_WILL_REMEMBER);
            // add the name to the users list
            return getNewUserName();
        }

        return "poo";
    }

    private String getNewUserName() {
        Scanner reader = new Scanner(System.in);
        System.out.print("Your name: ");
        return reader.nextLine().toLowerCase();
    }

}
