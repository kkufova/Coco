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

package dialog;

import java.util.logging.Logger;

import dialog.adaptation.RulePruner;
import dialog.constants.Paths;

/**
 * The main class of the dialog system that runs each part of the dialog.
 *
 * After each dialog part, the mechanism responsible for pruning the grammar is initiated.
 */

public class Dialog {

    // Disable the INFO logging messages:
    static {
        System.setProperty("java.util.logging.config.file", Paths.LOGGING);
        Logger.getLogger(Dialog.class.getName());
    }

    public static String dialogPart;

    public static void main(String[] args) throws Exception {
        System.out.println("-------" + "\n"
                + "NOTICE:" + "\n"
                + "For a pleasant experience, make sure your microphone is turned on only if you are actually talking." + "\n"
                + "This will prevent the background noise from disrupting the speech recognition." + "\n"
                + "-------");

        DialogFactory factory = new DialogFactory();
        RulePruner rulePruner = new RulePruner();

        String pathToGrammars = Paths.GRAMMARS;

        DialogPart openingDialog = factory.getDialogPart(dialog.enumerations.DialogPart.OPENING);
        dialogPart = dialog.enumerations.DialogPart.OPENING.toString();
        openingDialog.actDialogPart();
        rulePruner.pruneGrammar(pathToGrammars + openingDialog.getDialogPartGrammar() + ".gram");

        DialogPart countingDialog = factory.getDialogPart(dialog.enumerations.DialogPart.COUNTING);
        dialogPart = dialog.enumerations.DialogPart.COUNTING.toString();
        countingDialog.actDialogPart();
        rulePruner.pruneGrammar(pathToGrammars + countingDialog.getDialogPartGrammar() + ".gram");

        DialogPart orderingDialog = factory.getDialogPart(dialog.enumerations.DialogPart.ORDERING);
        dialogPart = dialog.enumerations.DialogPart.ORDERING.toString();
        orderingDialog.actDialogPart();
        rulePruner.pruneGrammar(pathToGrammars + orderingDialog.getDialogPartGrammar() + ".gram");

        DialogPart shippingDialog = factory.getDialogPart(dialog.enumerations.DialogPart.SHIPPING);
        dialogPart = dialog.enumerations.DialogPart.SHIPPING.toString();
        shippingDialog.actDialogPart();
        rulePruner.pruneGrammar(pathToGrammars + shippingDialog.getDialogPartGrammar() + ".gram");

        DialogPart closingDialog = factory.getDialogPart(dialog.enumerations.DialogPart.CLOSING);
        dialogPart = dialog.enumerations.DialogPart.CLOSING.toString();
        closingDialog.actDialogPart();
        rulePruner.pruneGrammar(pathToGrammars + closingDialog.getDialogPartGrammar() + ".gram");
    }

}
