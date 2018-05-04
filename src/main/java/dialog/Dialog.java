package dialog;

import java.util.logging.Logger;

import dialog.adaptation.RulePruner;

/**
 * The main class of the Coco dialog system.
 * Runs each part of the dialog.
 */

public class Dialog {

    // Disable the INFO logging messages:
    static {
        System.setProperty("java.util.logging.config.file", "src/main/resources/logging/logging.properties");
        Logger.getLogger(Dialog.class.getName());
    }

    public static String dialogPart;

    public static void main(String[] args) throws Exception {
        DialogFactory factory = new DialogFactory();
        RulePruner rulePruner = new RulePruner();

        String pathToGrammars = "src/main/resources/speech-recognition/grammars/";

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
