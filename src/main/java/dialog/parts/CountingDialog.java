package dialog.parts;

import static dialog.constants.Speeches.*;

import dialog.DialogPart;
import dialog.enumerations.Category;
import dialog.speech.Word;

public class CountingDialog extends DialogPart {

    private static final String GRAMMAR = "counting-dialog";

    static Word amountOfItems;

    public CountingDialog() {
        super();
    }

    @Override
    protected String getDialogPartGrammar() {
        return GRAMMAR;
    }

    @Override
    public void actDialogPart() throws Exception {
        synthesizeSpeech(HOW_MANY_ITEMS);
        // Obtain the amount of items from the utterance:
        amountOfItems = listenForWord(Category.NUMBERS);

        if (!amountOfItems.isNumberLessOrEqualThanTen()) {
            if (userWantsToOrderTenItems()) {
                return;
            }
        }

        boolean isFirstIteration = true;
        Word answer = new Word("no");

        while (!answer.isPositive()) {
            if (!isFirstIteration) {
                synthesizeSpeech(REPEAT_AMOUNT);
                amountOfItems = listenForWord(Category.NUMBERS);
            }
            isFirstIteration = false;

            synthesizeSpeech(CONFIRM_AMOUNT + amountOfItems + "?");
            answer = listenForWord(Category.ANSWERS);
        }

        if (!amountOfItems.isNumberLessOrEqualThanTen()) {
            if (userWantsToOrderTenItems()) {
                return;
            }
        }

        synthesizeSpeech(GREAT);
        if (amountOfItems.toString().equals("one")) {
            synthesizeSpeech(LETS_ORDER + ITEM + ".");
        } else {
            synthesizeSpeech(LETS_ORDER + amountOfItems + " " + ITEMS + ".");
        }
    }

    // Returns true if the user wants to order only ten of the chosen items.
    private boolean userWantsToOrderTenItems() throws Exception {
        synthesizeSpeech(MAX_TEN_ITEMS);
        synthesizeSpeech(ORDER_TEN_QUESTION);
        Word answer = listenForWord(Category.ANSWERS);

        if (answer.isPositive()) {
            synthesizeSpeech(GREAT);
            amountOfItems.setWord("ten");
            synthesizeSpeech(LETS_ORDER + amountOfItems + " " + ITEMS + ".");
            return true;
        } else {
            synthesizeSpeech(GOODBYE_UNFORTUNATE);
            System.exit(0);
        }

        return false;
    }

}
