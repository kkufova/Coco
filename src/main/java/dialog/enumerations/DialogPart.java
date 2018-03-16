package dialog.enumerations;

public enum DialogPart {
    OPENING("opening"),
    COUNTING("counting"),
    ORDERING("ordering"),
    SHIPPING("shipping"),
    CLOSING("closing");

    private String name;

    DialogPart(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

}
