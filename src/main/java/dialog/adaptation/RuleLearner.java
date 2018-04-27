package dialog.adaptation;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import dialog.Dialog;
import dialog.speech.Utterance;

public class RuleLearner {

    private static final String PATH_TO_GRAMMARS = "src/main/resources/speech-recognition/grammars/";

    private static int count = 0;

    public void addRuleToGrammar(Utterance utterance) {
        String grammarFile = PATH_TO_GRAMMARS + Dialog.dialogPart + "-dialog.gram";

        count++;
        String ruleDeclaration = "public <generatedRule-" + count + "> = ";

        try {
            Path path = Paths.get(grammarFile);
            Charset charset = StandardCharsets.UTF_8;

            String content = new String(Files.readAllBytes(path), charset);
            content = content.concat(ruleDeclaration + utterance.toString() + ";\n");
            Files.write(path, content.getBytes(charset));
        } catch (IOException exception) {
            System.err.println("An error occurred during the creation of the new rule.");
        }
    }

}
