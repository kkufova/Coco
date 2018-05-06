/*
 * Coco the Dialogue System
 * https://github.com/kkufova/Coco
 *
 * Copyright 2018 Klara Kufova
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dialog.adaptation;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A class that is responsible for the correct formatting of each newly created rule.
 *
 * Removes inappropriate spaces and duplicate words, merges brackets, and contains
 * logic for managing incorrectly added constructs, such as disrupted links to rules
 * from imported grammars.
 */

class RuleFormatter {

    String performFormatting(String inputRule) {
        inputRule = inputRule.trim().replaceAll(" +", " ");

        inputRule = mergeBrackets(inputRule);
        inputRule = adjustIncorrectBrackets(inputRule);
        inputRule = mergeBrackets(inputRule);
        inputRule = removeDuplicates(inputRule);

        return inputRule;
    }

    private String mergeBrackets(String inputRule) {
        // The basic "or" case:
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

    private String adjustIncorrectBrackets(String inputRule) {
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

        // Disrupted category word links to other rules:
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
