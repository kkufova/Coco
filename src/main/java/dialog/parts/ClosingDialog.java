package dialog.parts;

import static dialog.constants.Speeches.*;

import dialog.DialogPart;
import dialog.enumerations.Category;
import dialog.speech.Word;

public class ClosingDialog extends DialogPart {

    private static final String GRAMMAR = "closing-dialog";

    public ClosingDialog() {
        super();
    }

    @Override
    protected String getDialogPartGrammar() {
        return GRAMMAR;
    }

    @Override
    public void actDialogPart() throws Exception {
        synthesizeSpeech(THIS_IS_ALL_I_NEED);
        synthesizeSpeech(FURTHER_INFO);
        synthesizeSpeech(WILL_KEEP_YOU_INFORMED);
        synthesizeSpeech(ANYTHING_ELSE);

        Word answer = listenForWord(Category.ANSWERS);
        if (!answer.isPositive()) {
            synthesizeSpeech(OK);
        } else {
            synthesizeSpeech(WHAT_IS_IT);
            recognizeSpeech(recognizer, false);
            synthesizeSpeech(WILL_MAKE_IT_HAPPEN);
        }

        synthesizeSpeech(GOODBYE + OpeningDialog.name + "!");
        synthesizeSpeech(HOPE_TO_TALK_SOON);
    }

}
