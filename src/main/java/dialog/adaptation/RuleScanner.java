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

        // References to other rules (uncomment based on your preference):
        //ruleBody = ruleBody.replaceAll("<[a-z]+?>", "");

        // Representatives (round brackets):
        rulePattern = Pattern.compile("\\(([a-z]+ ?).+?\\)");
        ruleMatcher = rulePattern.matcher(ruleBody);
        while (ruleMatcher.find()) {
            ruleBody = ruleMatcher.replaceFirst(ruleMatcher.group(1));
        }

        ruleBody = ruleBody.replaceAll(" {2,}", " ");

        // Representatives (square brackets):
        rulePattern = Pattern.compile("\\[([a-z]+ ?).+?]");
        ruleMatcher = rulePattern.matcher(ruleBody);
        while (ruleMatcher.find()) {
            ruleBody = ruleMatcher.replaceFirst(ruleMatcher.group(1));
        }

        ruleBody = ruleBody.replaceAll(" {2,}", " "); // The double space is caused by the previous group replacement.
        return ruleBody.split(" ");
    }

}
