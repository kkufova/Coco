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

import dialog.DialogHelper;
import dialog.DialogPart;
import dialog.enumerations.Category;
import dialog.speech.Word;

/**
 * The very first dialog part that is responsible for welcoming the user, introducing Coco,
 * and obtaining the user's name.
 *
 * If the user does not have the order ready, the dialog is concluded.
 */

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
