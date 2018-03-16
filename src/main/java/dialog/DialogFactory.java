package dialog;

import dialog.parts.*;

class DialogFactory {

    DialogPart getDialogPart(dialog.enumerations.DialogPart dialogPart) {
        if (dialogPart == null) {
            return null;
        }

        switch (dialogPart) {
            case OPENING:
                return new OpeningDialog();
            case COUNTING:
                return new CountingDialog();
            case ORDERING:
                return new OrderingDialog();
            case SHIPPING:
                return new ShippingDialog();
            case CLOSING:
                return new ClosingDialog();
        }

        return null;
    }

}
