package dialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.sound.sampled.AudioInputStream;

import dialog.recognition.SpeechRecognizer;
import dialog.speech.Utterance;
import dialog.speech.Word;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.result.WordResult;

import marytts.LocalMaryInterface;
import marytts.MaryInterface;
import marytts.datatypes.MaryData;
import marytts.datatypes.MaryDataType;
import marytts.datatypes.MaryXML;
import marytts.util.data.audio.AudioPlayer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public interface DialogSetup {

    String ACOUSTIC_MODEL = "src/main/resources/speech-recognition/acoustic-model";
    String DICTIONARY = "src/main/resources/speech-recognition/dictionary/cmudict-en-us.dict";
    String GRAMMAR = "src/main/resources/speech-recognition/grammars";
    String LANGUAGE_MODEL = "src/main/resources/speech-recognition/language-model/en-70k-0.2-pruned.lm";

    default Configuration configureRecognizer(boolean useGrammar, String grammarName) {
        Configuration configuration = new Configuration();

        configuration.setAcousticModelPath(ACOUSTIC_MODEL);
        configuration.setDictionaryPath(DICTIONARY);

        if (useGrammar) {
            configuration.setUseGrammar(true);
            configuration.setGrammarPath(GRAMMAR);
            configuration.setGrammarName(grammarName);
        } else {
            configuration.setLanguageModelPath(LANGUAGE_MODEL);
        }

        return configuration;
    }

    default Utterance recognizeSpeech(SpeechRecognizer recognizer, boolean clearData) throws Exception {
        recognizer.startRecognition(clearData);
        SpeechResult result = recognizer.getResult();
        recognizer.stopRecognition();

        Utterance utterance = new Utterance(result.getHypothesis());

        // Get a list of single words in an utterance:
        List<WordResult> wordResultList = result.getWords();
        List<Word> words = new ArrayList<>();
        for (WordResult wordResult : wordResultList) {
            words.add(new Word(wordResult.getWord().toString()));
        }
        utterance.setWords(words);

        if (utterance.isUnknown()) {
            System.out.println("--- An unknown utterance has been recognized! ---");
            return utterance;
        }

        printUtterance(utterance);
        return utterance;
    }

    default void printUtterance(Utterance utterance) {
        // Format the utterance:
        String utteranceToPrint = utterance.toString().substring(0, 1).toUpperCase() + utterance.toString().substring(1);
        utteranceToPrint = utteranceToPrint.concat(".");
        utteranceToPrint = utteranceToPrint.replace(" i ", " I ");

        System.out.println("You: " + utteranceToPrint);
    }

    default void synthesizeSpeech(String text) throws Exception {
        System.out.println("Coco: " + text);

        MaryInterface maryTTS = new LocalMaryInterface();
        maryTTS.setVoice("dfki-prudence");
        maryTTS.setInputType("RAWMARYXML");

        Document document = MaryXML.newDocument();

        Element maryXML = document.getDocumentElement();
        maryXML.setAttribute("xml:lang", "en-GB");

        Element paragraph = MaryXML.appendChildElement(maryXML, MaryXML.PARAGRAPH);
        Element sentence = MaryXML.appendChildElement(paragraph, MaryXML.SENTENCE);
        Element boundary = MaryXML.appendChildElement(sentence, MaryXML.BOUNDARY);
        boundary.setAttribute("duration", "1000");
        Element token = MaryXML.appendChildElement(sentence, MaryXML.TOKEN);
        token.setTextContent(text);

        MaryData maryData = new MaryData(MaryDataType.PHONEMES, Locale.ENGLISH, false);
        maryData.setDocument(document);

        AudioInputStream audio = maryTTS.generateAudio(document);
        AudioPlayer player = new AudioPlayer(audio);
        player.start();
        player.join();
    }

}
