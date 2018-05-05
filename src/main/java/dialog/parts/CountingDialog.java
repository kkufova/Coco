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
    protected boolean setUseGrammar() {
        return true;
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
