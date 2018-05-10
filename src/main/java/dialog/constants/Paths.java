package dialog.constants;

public final class Paths {

    private Paths() {
    }

    private static final String CURRENT_DIRECTORY = System.getProperty("user.dir");
    private static final String COCO_DIRECTORY = "/coco-resources/";

    public static final String LOGGING = CURRENT_DIRECTORY + COCO_DIRECTORY + "logging/logging.properties";
    public static final String ACOUSTIC_MODEL = CURRENT_DIRECTORY + COCO_DIRECTORY + "speech-recognition/acoustic-model";
    public static final String DICTIONARY = CURRENT_DIRECTORY + COCO_DIRECTORY + "speech-recognition/dictionary/cmudict-en-us.dict";
    public static final String GRAMMARS = CURRENT_DIRECTORY + COCO_DIRECTORY + "speech-recognition/grammars/";
    public static final String CATEGORY_GRAMMARS = CURRENT_DIRECTORY + COCO_DIRECTORY + "speech-recognition/grammars/category-grammars/";
    public static final String LANGUAGE_MODEL = CURRENT_DIRECTORY + COCO_DIRECTORY + "speech-recognition/language-model/en-70k-0.2-pruned.lm";

}
