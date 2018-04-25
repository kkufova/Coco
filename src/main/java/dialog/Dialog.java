package dialog;

import java.util.logging.Logger;

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

    public static void main(String[] args) throws Exception {
        DialogFactory factory = new DialogFactory();

        DialogPart openingDialog = factory.getDialogPart(dialog.enumerations.DialogPart.OPENING);
        openingDialog.actDialogPart();

        DialogPart countingDialog = factory.getDialogPart(dialog.enumerations.DialogPart.COUNTING);
        countingDialog.actDialogPart();

        DialogPart orderingDialog = factory.getDialogPart(dialog.enumerations.DialogPart.ORDERING);
        orderingDialog.actDialogPart();

        DialogPart shippingDialog = factory.getDialogPart(dialog.enumerations.DialogPart.SHIPPING);
        shippingDialog.actDialogPart();

        DialogPart closingDialog = factory.getDialogPart(dialog.enumerations.DialogPart.CLOSING);
        closingDialog.actDialogPart();
    }

}
