package dialog.adaptation;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class RuleFormatter {

    String performFormatting(String inputRule) {
        inputRule = inputRule.trim().replaceAll(" +", " ");

        inputRule = mergeBrackets(inputRule);
        inputRule = fixNonsensicalBrackets(inputRule);
        inputRule = mergeBrackets(inputRule);
        inputRule = removeDuplicates(inputRule);

        return inputRule;
    }

    private String mergeBrackets(String inputRule) {
        // Basic case:
        inputRule = inputRule.replaceAll("\\) \\(", " | ");
        inputRule = inputRule.replaceAll("] \\[", " | ");

        // Inappropriate spaces:
        inputRule = inputRule.replaceAll("\\( \\[", "([");
        inputRule = inputRule.replaceAll("\\[ \\(", "[(");
        inputRule = inputRule.replaceAll("\\) ]", ")]");
        inputRule = inputRule.replaceAll("] \\)", "])");

        inputRule = inputRule.replaceAll("\\( ", "\\(");
        inputRule = inputRule.replaceAll("\\[ ", "\\[");
        inputRule = inputRule.replaceAll(" \\)", "\\)");
        inputRule = inputRule.replaceAll(" ]", "]");

        return inputRule;
    }

    private String fixNonsensicalBrackets(String inputRule) {
        // Single words in round brackets:
        Pattern pattern = Pattern.compile("\\(([a-z]+?)\\)");
        Matcher matcher = pattern.matcher(inputRule);
        while (matcher.find()) {
            inputRule = matcher.replaceFirst("[" + matcher.group(1) + "]");
            matcher = pattern.matcher(inputRule);
        }

        // Single words in square brackets inside round brackets:
        pattern = Pattern.compile("\\(([^)]*)( \\| )*(\\[[a-z]+?])( \\| )*([^(]*)\\)");
        matcher = pattern.matcher(inputRule);
        while (matcher.find()) {
            inputRule = inputRule.replace(matcher.group(3), matcher.group(3).replace("[", "").replace("]", ""));
            matcher = pattern.matcher(inputRule);
        }

        // Single words in square brackets inside square brackets:
        pattern = Pattern.compile("\\[([^)\\]]*)( \\| )*(\\[[a-z]+?])( \\| )*([^(\\[]*)]");
        matcher = pattern.matcher(inputRule);
        while (matcher.find()) {
            inputRule = inputRule.replace(matcher.group(3), matcher.group(3).replace("[", "").replace("]", ""));
            matcher = pattern.matcher(inputRule);
        }

        // Disrupting category word links to other rules:
        pattern = Pattern.compile("<.*(<[a-z]+?>).*>");
        matcher = pattern.matcher(inputRule);
        while (matcher.find()) {
            inputRule = inputRule.replace(matcher.group(0), matcher.group(1));
            matcher = pattern.matcher(inputRule);
        }

        return inputRule;
    }

    private String removeDuplicates(String inputRule) {
        // Duplicates inside round brackets:
        Pattern pattern = Pattern.compile("\\(([\\D |]+?)\\)");
        Matcher matcher = pattern.matcher(inputRule);
        while (matcher.find()) {
            inputRule = inputRule.replace(matcher.group(1), Arrays.stream(matcher
                    .group(1)
                    .split(" \\| "))
                    .distinct()
                    .collect(Collectors.joining(" | ")));
        }

        pattern = Pattern.compile("\\(([a-z ]+?)\\)");
        matcher = pattern.matcher(inputRule);
        while (matcher.find()) {
            inputRule = inputRule.replace(matcher.group(1), Arrays.stream(matcher
                    .group(1)
                    .split(" "))
                    .distinct()
                    .collect(Collectors.joining(" ")));
        }

        // Duplicates inside square brackets:
        pattern = Pattern.compile("\\[([\\D |]+?)]");
        matcher = pattern.matcher(inputRule);
        while (matcher.find()) {
            inputRule = inputRule.replace(matcher.group(1), Arrays.stream(matcher
                    .group(1)
                    .split(" \\| "))
                    .distinct()
                    .collect(Collectors.joining(" | ")));
        }

        pattern = Pattern.compile("\\[([a-z ]+?)]");
        matcher = pattern.matcher(inputRule);
        while (matcher.find()) {
            inputRule = inputRule.replace(matcher.group(1), Arrays.stream(matcher
                    .group(1)
                    .split(" "))
                    .distinct()
                    .collect(Collectors.joining(" ")));
        }

        return inputRule;
    }

}
