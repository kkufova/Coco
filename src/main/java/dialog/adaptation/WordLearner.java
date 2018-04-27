package dialog.adaptation;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import dialog.enumerations.Category;
import dialog.speech.Word;

public class WordLearner {

    private static final String PATH_TO_CATEGORY_GRAMMARS = "src/main/resources/speech-recognition/grammars/category-grammars/";

    public void addWordToCategoryGrammar(Word word, Category category) {
        String grammarFile = PATH_TO_CATEGORY_GRAMMARS + "category-" + category.toString().toLowerCase() + ".gram";

        String stringToInsert = "     | " + word.toString() + "\n              ;";

        try {
            Path path = Paths.get(grammarFile);
            Charset charset = StandardCharsets.UTF_8;

            String content = new String(Files.readAllBytes(path), charset);
            content = content.replaceAll(" {5};", stringToInsert);
            Files.write(path, content.getBytes(charset));
        } catch (IOException exception) {
            System.err.println("An error occurred during grammar update.");
        }
    }

}
