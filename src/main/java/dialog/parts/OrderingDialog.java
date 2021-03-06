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

import java.util.ArrayList;
import java.util.List;

import dialog.DialogHelper;
import dialog.DialogPart;
import dialog.enumerations.Category;
import dialog.speech.Utterance;
import dialog.speech.Word;

/**
 * The most significant dialog part; the actual specification of the selected items.
 *
 * Each item is represented by its category, type, size, and a number. At the end of the dialog part, the list of
 * all the ordered items is displayed, and it is possible to review the details and reorder any item in case of
 * mistakes that may have been made during the ordering process by either the user or the system.
 */

public class OrderingDialog extends DialogPart {

    private static final String GRAMMAR = "ordering-dialog";

    private Utterance utterance;

    private List<Item> orderedItems = new ArrayList<>();
    private DialogHelper helper = new DialogHelper();

    public OrderingDialog() {
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
        int totalAmount = helper.convertNumberToInt(CountingDialog.amountOfItems);

        for (int itemOrder = 1; itemOrder <= totalAmount; itemOrder++) {
            Item item = orderItem(itemOrder);
            orderedItems.add(item);
        }

        confirmOrder(orderedItems);
        informAboutPrice();
    }

    private Item orderItem(int itemOrder) throws Exception {
        Word ordinal = helper.convertCardinalToOrdinal(itemOrder);

        synthesizeSpeech(INTRODUCE_ITEM + ordinal + " " + ITEM + ".");

        // Get item group:
        synthesizeSpeech("Is the " + ordinal + SPECIFY_ITEM_CATEGORY);
        Word itemGroup = listenForWord(Category.GROUPS);

        // Get item type:
        synthesizeSpeech(SPECIFY_ITEM_TYPE);
        Word itemType = listenForWord(Category.ITEMS);

        // Get item size:
        synthesizeSpeech(SPECIFY_ITEM_SIZE + itemType + TO_BE + "?");
        Word itemSize = listenForWord(Category.SIZES);
        itemSize = helper.convertSize(itemSize);

        // Get item number:
        synthesizeSpeech(SPECIFY_ITEM_NUMBER);
        utterance = recognizeSpeech(recognizer, false);
        int itemNumber = getItemNumber();

        synthesizeSpeech(THANK_YOU);

        return new Item(itemGroup, itemType, itemSize, itemNumber);
    }

    private int getItemNumber() throws Exception {
        List<Word> numbers = utterance.findWordsFromCategory(Category.NUMBERS);

        while ((numbers.size() != 4) || (containsNumberBiggerThanTen(numbers))) {
            synthesizeSpeech(REPEAT_ITEM_NUMBER);
            utterance = recognizeSpeech(recognizer, false);
            numbers = utterance.findWordsFromCategory(Category.NUMBERS);
        }

        String itemNumber = utterance.findWordsFromCategoryAndCreateString(Category.NUMBERS);
        Word answer = new Word("no");
        boolean isFirstIteration = true;

        while (!answer.isPositive() || (numbers.size() != 4) || (containsNumberBiggerThanTen(numbers))) {
            if (!isFirstIteration) {
                synthesizeSpeech(REPEAT_ITEM_NUMBER);
                utterance = recognizeSpeech(recognizer, false);
                itemNumber = utterance.findWordsFromCategoryAndCreateString(Category.NUMBERS);
                numbers = utterance.findWordsFromCategory(Category.NUMBERS);
            }
            isFirstIteration = false;

            if ((numbers.size() == 4) && (!containsNumberBiggerThanTen(numbers))) {
                synthesizeSpeech(CONFIRM_ITEM_NUMBER + itemNumber + "?");
                answer = listenForWord(Category.ANSWERS);
            }
        }

        StringBuilder builder = new StringBuilder();
        for (Word number : numbers) {
            builder.append(helper.convertNumberToInt(number));
        }
        return Integer.parseInt(builder.toString());
    }

    private boolean containsNumberBiggerThanTen(List<Word> numbers) {
        for (Word number : numbers) {
            if (number.isNumberBiggerThanTen()) {
                return true;
            }
        }
        return false;
    }

    private void confirmOrder(List<Item> orderedItems) throws Exception {
        synthesizeSpeech(PLEASE_REVIEW_ITEMS);

        printTableOfOrderedItems(orderedItems);

        // Read the list of ordered items aloud if requested:
        synthesizeSpeech(SHOULD_READ_ALOUD_LIST);
        Word answer = listenForWord(Category.ANSWERS);
        int i = 1;
        if (answer.isPositive()) {
            synthesizeSpeech(ALL_RIGHT);
            for (Item item : orderedItems) {
                String ordinal = helper.convertCardinalToOrdinal(i).toString();
                synthesizeSpeech("The " + ordinal + " item is from " + item.toString());
                i++;
            }
        }

        synthesizeSpeech(EVERYTHING_CORRECT);
        answer = listenForWord(Category.ANSWERS);
        if (answer.isPositive()) {
            synthesizeSpeech(GREAT);
            synthesizeSpeech(PROCEED_TO_CHECKOUT);
        } else {
            correctItem(orderedItems);
        }
    }

    private void correctItem(List<Item> orderedItems) throws Exception {
        synthesizeSpeech(I_AM_SORRY);
        synthesizeSpeech(WHICH_ITEM_INCORRECT);
        Word numberOfIncorrectItem = listenForWord(Category.NUMBERS);

        // Remove the item from the table:
        int numberOfIncorrectItemInt = helper.convertNumberToInt(numberOfIncorrectItem);
        Item item = orderedItems.get(numberOfIncorrectItemInt - 1);
        orderedItems.remove(item);

        // Order the item again:
        synthesizeSpeech(ORDER_ITEM_AGAIN);
        Item correctedItem = orderItem(numberOfIncorrectItemInt);
        orderedItems.add(correctedItem);
        confirmOrder(orderedItems);
    }

    private void printTableOfOrderedItems(List<Item> orderedItems) {
        final Object[][] table = new String[orderedItems.size() + 1][];
        table[0] = new String[]{"ITEM NO.", "CATEGORY", "TYPE", "SIZE", "NUMBER"};

        int i = 1;

        for (Item item : orderedItems) {
            table[i] = new String[]{i + ".", item.itemGroup.toString(), item.itemType.toString(), item.itemSize.toString(), item.itemNumber + ""};
            i++;
        }
        for (final Object[] row : table) {
            System.out.format("%-15s%-15s%-15s%-15s%-15s\n", row);
        }
    }

    private void informAboutPrice() throws Exception {
        synthesizeSpeech(TOTAL_AMOUNT);
        Word answer = listenForWord(Category.ANSWERS);
        if (answer.isPositive()) {
            synthesizeSpeech(GREAT);
        } else {
            synthesizeSpeech(UNFORTUNATE);
            synthesizeSpeech(GOODBYE_WRONG_PRICE);
            System.exit(0);
        }
    }

    class Item {

        private Word itemGroup;
        private Word itemType;
        private Word itemSize;
        private int itemNumber;

        Item(Word itemGroup, Word itemType, Word itemSize, int itemNumber) {
            this.itemGroup = itemGroup;
            this.itemType = itemType;
            this.itemSize = itemSize;
            this.itemNumber = itemNumber;
        }

        @Override
        public String toString() {
            return ("group " + itemGroup + ", of type " + itemType + ", in size " + itemSize + ", and its number is " + itemNumber + ".");
        }

    }

}
