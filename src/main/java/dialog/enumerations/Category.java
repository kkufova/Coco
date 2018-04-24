package dialog.enumerations;

public enum Category {
    ANSWERS(""),
    COLORS("color of the item"),
    DELIVERIES("delivery"),
    GROUPS("category of the item"),
    ITEMS("item type"),
    NUMBERS("number"),
    SIZES("size"),
    USERS("your name");

    private String about;

    Category(String about) {
        this.about = about;
    }

    public String getAbout() {
        return about;
    }

}
