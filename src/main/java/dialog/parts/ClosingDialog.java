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

/**
 * The final dialog part; the closing dialog.
 *
 * Coco informs the user about finalizing the order, asks for further requests, and concludes the whole experience.
 */

public class ClosingDialog extends DialogPart {

    private static final String GRAMMAR = "closing-dialog";

    public ClosingDialog() {
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
