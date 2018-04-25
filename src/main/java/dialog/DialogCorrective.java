package dialog;

import java.io.IOException;
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
        synthesizeSpeech(Speeches.PLEASE_REPEAT);

        Utterance utterance = recognizeSpeech(lmRecognizer, true);
        Word word = utterance.findWordsFromCategoryAndCreateWord(category);

        while (word.isEmptyWord()) {
            if (!category.equals(Category.ANSWERS)) {
                synthesizeSpeech(Speeches.NOT_ANSWER + " " + Speeches.I_HAVE_ASKED + category.getAbout() + ".");
            } else {
                synthesizeSpeech(Speeches.NOT_ANSWER);
            }
            synthesizeSpeech(Speeches.PLEASE_RESPOND);
            utterance = recognizeSpeech(lmRecognizer, true);
            word = utterance.findWordsFromCategoryAndCreateWord(category);
        }

        Word answer = new Word("no");
        while (!answer.isPositive()) {
            synthesizeSpeech(Speeches.DID_I_RECOGNIZE);
            answer = recognizeSpeech(grammarRecognizer, true).findWordsFromCategoryAndCreateWord(Category.ANSWERS);
        }

        // add new grammar rule
        RuleLearner ruleLearner = new RuleLearner();

        synthesizeSpeech(Speeches.THANK_YOU_FOR);
        synthesizeSpeech(Speeches.LETS_CONTINUE);

        return word;
    }

    String getStringFromCorrectiveDialog(Category category) throws Exception {
        if (category.equals(Category.USERS)) {
            return actUnknownUserDialog();
        }

        //TODO

        return "poo";
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
