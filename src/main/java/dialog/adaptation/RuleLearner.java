package dialog.adaptation;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dialog.Dialog;
import dialog.speech.Utterance;

public class RuleLearner {

    private static final String PATH_TO_GRAMMARS = "src/main/resources/speech-recognition/grammars/";
    private String grammarFile = PATH_TO_GRAMMARS + Dialog.dialogPart + "-dialog.gram";

    private HashMap<String, String[]> rules = new HashMap<>(); // <actual rule body, list of words in a rule>
    private String ruleBodyToBeModified;

    public void addRuleToGrammar(Utterance utterance) throws IOException {
        RuleScanner ruleScanner = new RuleScanner();
        rules = ruleScanner.getAllRulesMap(grammarFile);

        String newRuleBody = utterance.toString();
        String[] newRuleBodyWords = newRuleBody.split(" ");

        if (canBeAddedToExistingRule(newRuleBodyWords)) {
            modifyExistingRule(newRuleBody);
        } else {
            addNewRule(newRuleBody + ";");
        }
    }

    private boolean canBeAddedToExistingRule(String[] newRuleBodyWords) {
        for (String ruleBody : rules.keySet()) {
            String[] ruleBodyWords = rules.get(ruleBody);

            // Change the following parameters to suit your application's needs:
            int allowedDifferencesLeft = 3;
            int allowedDifferencesRight = 3;

            if (Arrays.equals(ruleBodyWords, newRuleBodyWords)) {
                return true;
            }
            for (String word : ruleBodyWords) {
                if (!Arrays.asList(newRuleBodyWords).contains(word)) {
                    allowedDifferencesLeft--;
                }
            }
            for (String word : newRuleBodyWords) {
                if (!Arrays.asList(ruleBodyWords).contains(word)) {
                    allowedDifferencesRight--;
                }
            }
            if (allowedDifferencesLeft < 0 || allowedDifferencesRight < 0) {
                continue;
            }
            ruleBodyToBeModified = ruleBody;
            return true;
        }

        return false;
    }

    private void modifyExistingRule(String newRuleBody) {
        if (ruleBodyToBeModified.equals(newRuleBody)) { // This is not very likely, but it can happen if the initial grammar-based recognition fails.
            return;
        }

        LinkedList<DiffMatchPatch.Diff> diff = computeDiffMatchPatch(newRuleBody);

        for (int i = 0; i < diff.size(); i++) {
            if (diff.get(i).text.equals("")) {
                diff.remove(i);
            }
        }

        System.out.println(diff); // TODO remove

        for (int i = 0; i < diff.size(); i++) {

            if (diff.get(i).operation.equals(DiffMatchPatch.Operation.INSERT)) { // INSERT
                if (i == diff.size() - 1) {
                    String previousWord = diff.get(i - 1).text.trim();
                    if (diff.get(i - 1).operation.equals(DiffMatchPatch.Operation.DELETE)) { // DELETE INSERT
                        appendTextToText(diff.get(i).text, previousWord, "(", ")", false);
                    } else { // EQUAL INSERT
                        appendTextToText(diff.get(i).text, previousWord, "[", "]", false);
                    }
                } else {
                    String followingWord = diff.get(i + 1).text.trim();
                    if (diff.get(i + 1).operation.equals(DiffMatchPatch.Operation.DELETE)) { // INSERT DELETE
                        appendTextToText(diff.get(i).text, followingWord, "(", ")", true);
                    } else { // INSERT EQUAL
                        if (i != 0) {
                            if (diff.get(i - 1).operation.equals(DiffMatchPatch.Operation.DELETE)) { // DELETE INSERT EQUAL
                                appendTextToText(diff.get(i).text, followingWord, "(", ")", true);
                            } else {
                                appendTextToText(diff.get(i).text, followingWord, "[", "]", true);
                            }
                        } else {
                            appendTextToText(diff.get(i).text, followingWord, "[", "]", true);
                        }
                    }
                }
            } else if (diff.get(i).operation.equals(DiffMatchPatch.Operation.DELETE)) { // DELETE
                if (i == diff.size() - 1) {
                    if (diff.get(i - 1).operation.equals(DiffMatchPatch.Operation.INSERT)) { // INSERT DELETE
                        putTextIntoBrackets(diff.get(i).text, "(", ")", false, true);
                    } else { // EQUAL DELETE
                        putTextIntoBrackets(diff.get(i).text, "[", "]", false, true);
                    }
                } else {
                    if (diff.get(i + 1).operation.equals(DiffMatchPatch.Operation.INSERT)) { // DELETE INSERT
                        putTextIntoBrackets(diff.get(i).text, "(", ")", true, false); // may not be first, but it does not matter
                    } else { // DELETE EQUAL
                        if (i != 0) {
                            if (diff.get(i - 1).operation.equals(DiffMatchPatch.Operation.INSERT)) { // INSERT DELETE EQUAL
                                putTextIntoBrackets(diff.get(i).text, "(", ")", false, false);
                            } else {
                                putTextIntoBrackets(diff.get(i).text, "[", "]", false, false);
                            }
                        } else {
                            putTextIntoBrackets(diff.get(i).text, "[", "]", true, false);
                        }
                    }
                }
            }
        }

        // TODO
        // remove duplicates
        // reprezentanti zavorek nefunguji s vnorenyma zavorkama
        // predelat category words na <neco>
        // odstran stare ruleBody z gramatiky

        ruleBodyToBeModified = ruleBodyToBeModified.trim().replaceAll(" +", " ");

        RuleScanner ruleScanner = new RuleScanner();
        ruleBodyToBeModified = ruleScanner.mergeBrackets(ruleBodyToBeModified);
        ruleBodyToBeModified = ruleScanner.fixNonsensicalBrackets(ruleBodyToBeModified);
        ruleBodyToBeModified = ruleScanner.removeDuplicates(ruleBodyToBeModified);

        addNewRule(ruleBodyToBeModified + ";");
    }

    private void appendTextToText(String newText, String oldText, String leftBracket, String rightBracket, boolean putBefore) {
        newText = newText.trim();

        String[] array = oldText.split(" ");
        Pattern pattern;
        Matcher matcher;

        ruleBodyToBeModified = " " + ruleBodyToBeModified + " ";

        if (putBefore) {
            oldText = array[0];
            if (array.length > 1) {
                pattern = Pattern.compile(oldText + "( .*)" + array[1]); // Sometimes we need more context.
            } else {
                pattern = Pattern.compile(oldText);
            }
            matcher = pattern.matcher(ruleBodyToBeModified);
        } else {
            oldText = array[array.length - 1];
            if (array.length > 1) {
                pattern = Pattern.compile(array[array.length - 2] + "(.* )" + oldText);
            } else {
                pattern = Pattern.compile(oldText);
            }
            matcher = pattern.matcher(ruleBodyToBeModified);
        }

        while (matcher.find()) {
            if (putBefore) {
                ruleBodyToBeModified = new StringBuilder(ruleBodyToBeModified)
                        .insert(matcher.start() - 1, " " + leftBracket + newText + rightBracket + " ")
                        .toString();
            } else {
                ruleBodyToBeModified = new StringBuilder(ruleBodyToBeModified)
                        .insert(matcher.start() + oldText.length() + 1, " " + leftBracket + newText + rightBracket + " ")
                        .toString();
            }
        }

        ruleBodyToBeModified = ruleBodyToBeModified.trim();
    }

    private void putTextIntoBrackets(String text, String leftBracket, String rightBracket, boolean isFirst, boolean isLast) {
        text = text.trim();
        Pattern pattern;

        if (isFirst) {
            pattern = Pattern.compile(text + "\\b");
        } else if (isLast) {
            pattern = Pattern.compile("\\b" + text);
        } else {
            pattern = Pattern.compile("\\b" + text + "\\b");
        }

        Matcher matcher = pattern.matcher(ruleBodyToBeModified);

        if (matcher.find()) {
            ruleBodyToBeModified = matcher.replaceAll(" " + leftBracket + text + rightBracket + " ");
        }
    }

    private LinkedList<DiffMatchPatch.Diff> computeDiffMatchPatch(String newRuleBody) {
        DiffMatchPatch dmp = new DiffMatchPatch();
        DiffMatchPatch.LinesToCharsResult result = dmp.diff_linesToWords(getPlainRuleBody(ruleBodyToBeModified), newRuleBody);
        String input1 = result.chars1;
        String input2 = result.chars2;
        List<String> array = result.lineArray;

        LinkedList<DiffMatchPatch.Diff> diff = dmp.diff_main(input1, input2, false);
        dmp.diff_charsToLines(diff, array);
        dmp.diff_cleanupSemantic(diff);

        return diff;
    }

    private String getPlainRuleBody(String ruleBody) {
        RuleScanner ruleScanner = new RuleScanner();
        String[] ruleBodyWords = ruleScanner.getRuleBodyWords(" = " + ruleBody + ";"); // Make sure the matcher finds a match!
        String result = "";

        for (String ruleBodyWord : ruleBodyWords) {
            result = result.concat(ruleBodyWord + " ");
        }

        return result.trim();
    }

    private void addNewRule(String newRuleBody) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new java.util.Date());
        String ruleDeclaration = "public <generated-rule-" + timestamp + "> = ";

        try {
            Path path = Paths.get(grammarFile);
            Charset charset = StandardCharsets.UTF_8;

            String content = new String(Files.readAllBytes(path), charset);
            content = content.concat(ruleDeclaration + newRuleBody + "\n");
            Files.write(path, content.getBytes(charset));
        } catch (IOException exception) {
            System.err.println("An error occurred during the creation of the new rule.");
        }
    }

}
