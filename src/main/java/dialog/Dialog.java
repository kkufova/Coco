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

    public static String dialogPart;

    public static void main(String[] args) throws Exception {
        DialogFactory factory = new DialogFactory();

        DialogPart openingDialog = factory.getDialogPart(dialog.enumerations.DialogPart.OPENING);
        dialogPart = dialog.enumerations.DialogPart.OPENING.toString();
        openingDialog.actDialogPart();

        DialogPart countingDialog = factory.getDialogPart(dialog.enumerations.DialogPart.COUNTING);
        dialogPart = dialog.enumerations.DialogPart.COUNTING.toString();
        countingDialog.actDialogPart();

        DialogPart orderingDialog = factory.getDialogPart(dialog.enumerations.DialogPart.ORDERING);
        dialogPart = dialog.enumerations.DialogPart.ORDERING.toString();
        orderingDialog.actDialogPart();

        DialogPart shippingDialog = factory.getDialogPart(dialog.enumerations.DialogPart.SHIPPING);
        dialogPart = dialog.enumerations.DialogPart.SHIPPING.toString();
        shippingDialog.actDialogPart();

        DialogPart closingDialog = factory.getDialogPart(dialog.enumerations.DialogPart.CLOSING);
        dialogPart = dialog.enumerations.DialogPart.CLOSING.toString();
        closingDialog.actDialogPart();
    }

}
