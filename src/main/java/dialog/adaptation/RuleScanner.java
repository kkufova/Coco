package dialog.adaptation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class RuleScanner {

    HashMap<String, String[]> getAllRulesMap(String grammarFile) throws IOException {
        HashMap<String, String[]> rules = new HashMap<>();

        FileReader fileReader = new FileReader(grammarFile);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line;

        Pattern pattern = Pattern.compile("public <generated-rule-" + "\\d{4}-\\d{2}-\\d{2}-\\d{2}-\\d{2}-\\d{2}" + "> = ");

        while ((line = bufferedReader.readLine()) != null) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                rules.put(getRuleBody(line), getRuleBodyWords(line));
            }
        }

        return rules;
    }

    private String getRuleBody(String line) {
        Pattern rulePattern = Pattern.compile(" = (.+?;)");
        Matcher ruleMatcher = rulePattern.matcher(line);
        if (ruleMatcher.find()) {
            return ruleMatcher.group(1).replace(";", "");
        }
        return "";
    }

    String[] getRuleBodyWords(String line) {
        String ruleBody;
        Pattern rulePattern = Pattern.compile(" = (.+?;)");
        Matcher ruleMatcher = rulePattern.matcher(line);
        if (ruleMatcher.find()) {
            ruleBody = ruleMatcher.group(1);
        } else {
            ruleBody = "";
        }

        // All the available special symbols in a rule body:
        ruleBody = ruleBody.replace(";", "");
        ruleBody = ruleBody.replace("*", "");
        ruleBody = ruleBody.replace("+", "");
        ruleBody = ruleBody.replace("|", "");

        ruleBody = ruleBody.replaceAll("<[a-z]+?>", "");

        // Round brackets:
        rulePattern = Pattern.compile("\\(([a-z]+ ?).+?\\)"); // TODO fix najdeme vsechno co je v kulatych a z nich odstranime jine kulate nebo hranate
        ruleMatcher = rulePattern.matcher(ruleBody);
        while (ruleMatcher.find()) {
            ruleBody = ruleMatcher.replaceFirst(ruleMatcher.group(1));
        }

        // Square brackets:
        rulePattern = Pattern.compile("\\[([a-z]+ ?).+?]"); // TODO fix
        ruleMatcher = rulePattern.matcher(ruleBody);
        while (ruleMatcher.find()) {
            ruleBody = ruleMatcher.replaceFirst(ruleMatcher.group(1));
        }

        ruleBody = ruleBody.replace("  ", " "); // The double space is caused by the previous group replacement.
        return ruleBody.split(" ");
    }

    String mergeBrackets(String inputRule) {
        inputRule = inputRule.replaceAll("\\) \\(", " | ");
        inputRule = inputRule.replaceAll("] \\[", " | ");

        // Inappropriate spaces:
        inputRule = inputRule.replaceAll("\\[ \\(", "[(");
        inputRule = inputRule.replaceAll("\\( \\[", "([");
        inputRule = inputRule.replaceAll("\\) ]", ")]");
        inputRule = inputRule.replaceAll("] \\)", "])");

        return inputRule;
    }

    String fixNonsensicalBrackets(String inputRule) {
        Pattern pattern = Pattern.compile("\\(([a-z]+?)\\)");
        Matcher matcher = pattern.matcher(inputRule);
        while (matcher.find()) {
            inputRule = matcher.replaceFirst("[" + matcher.group(1) + "]");
            matcher = pattern.matcher(inputRule);
        }

        return inputRule;
    }

    String removeDuplicates(String inputRule) {

        return inputRule;
    }

}
