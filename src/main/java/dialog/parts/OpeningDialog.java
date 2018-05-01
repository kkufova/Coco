package dialog.parts;

import static dialog.constants.Speeches.*;

import dialog.DialogHelper;
import dialog.DialogPart;
import dialog.enumerations.Category;
import dialog.speech.Word;

public class OpeningDialog extends DialogPart {

    private static final String GRAMMAR = "opening-dialog";

    static String name = "";

    public OpeningDialog() {
        super();
    }

    @Override
    protected boolean setUseGrammar() {
        return true;
    }

    @Override
    protected String getDialogPartGrammar() {
        return GRAMMAR;
    }

    @Override
    public void actDialogPart() throws Exception {
        synthesizeSpeech(WELCOME);
        synthesizeSpeech(INTRODUCTION);
        synthesizeSpeech(EXCUSE_ME);
        synthesizeSpeech(SAY_YOUR_NAME);

        // Obtain the user name from the utterance and capitalize it:
        name = listenForString(Category.USERS);
        DialogHelper helper = new DialogHelper();
        name = helper.capitalizeName(name);

        synthesizeSpeech(HELLO + name + "!");

        synthesizeSpeech(ORDER_QUESTION);
        // Obtain the answer from the utterance:
        Word answer = listenForWord(Category.ANSWERS);

        if (!answer.isPositive()) {
            synthesizeSpeech(GOODBYE_ITEMS_NOT_CHOSEN);
            System.exit(0);
        }

        synthesizeSpeech(GREAT_ORDER_IS_HAPPENING);
    }

}
