package dialog.recognition;

import java.io.IOException;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;

public class SpeechRecognizer extends LiveSpeechRecognizer {

    public SpeechRecognizer(Configuration configuration) throws IOException {
        super(configuration);

        context.setLocalProperty("flatLinguist->outOfGrammarProbability", "0.5");
        context.setLocalProperty("flatLinguist->phoneInsertionProbability", "0.01");
    }

}
