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

    private Utterance utterance;
    private Word requiredWord;
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
            int numberOfAttempts = 3; // Set the number of attempts to suit your application's needs.
            for (int i = 0; i < numberOfAttempts; i++) {
                checkIfCorrectlyRecognized();
                if (answer.isPositive()) {
                    askUserForRequiredWord(category);
                } else {
                    askForRepeat(category);
                }
                if (!requiredWord.isEmptyWord()) {
                    break;
                }
            }
        }

        if (requiredWord.isEmptyWord()) {
            synthesizeSpeech(Speeches.IT_LOOKS_LIKE);
            utterance = getWrittenUtterance();
            requiredWord = utterance.findWordsFromCategoryAndCreateWord(category);
        }

        RuleLearner ruleLearner = new RuleLearner();
        ruleLearner.addRuleToGrammar(utterance);

        synthesizeSpeech(Speeches.THANK_YOU_FOR);
        synthesizeSpeech(Speeches.LETS_CONTINUE);

        return requiredWord;
    }

    private void checkIfCorrectlyRecognized() throws Exception {
        // We need another recognizer:
        grammarRecognizer = new SpeechRecognizer(configureRecognizer(true, CORRECTIVE_DIALOG_GRAMMAR)); // TODO try to transfer it back

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

    private void askForRepeat(Category category) throws Exception {
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

        // TODO

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
