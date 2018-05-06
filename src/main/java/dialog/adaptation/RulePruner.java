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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A mechanism used for removing unused rules in grammars.
 *
 * Currently, an approach based on removing old rules is available. In the future, deleting rules based on their actual
 * usage will be implemented as well.
 */

public class RulePruner {

    public void pruneGrammar(String grammarFile) {
        // Based on the application requirements, use one or both of the following options:
        pruneOldRules(grammarFile);
        pruneUnusedRules(grammarFile);
    }

    private void pruneOldRules(String grammarFile) {
        try {
            File file = new File(grammarFile);
            FileReader fileReader = new FileReader(grammarFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;

            Pattern pattern = Pattern.compile("public <generated-rule-" + "(\\d{4}-\\d{2}-\\d{2}-\\d{2}-\\d{2}-\\d{2})" + "> = ");

            while ((line = bufferedReader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    String ruleTimestamp = matcher.group(1);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
                    LocalDateTime ruleDateAndTime = LocalDateTime.parse(ruleTimestamp, formatter);

                    if (ruleDateAndTime.isBefore(LocalDateTime.now().minusMonths(1))) { // Set the number of month to suite your application's needs.
                        List<String> out = Files.lines(file.toPath())
                                .filter(rule -> !rule.contains(ruleTimestamp))
                                .collect(Collectors.toList());
                        Files.write(file.toPath(), out, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
                    }
                }
            }

        } catch (IOException exception) {
            System.err.println("An error occurred during the deletion of the old rule.");
        }
    }

    private void pruneUnusedRules(String grammarFile) {
        // TODO: implement rule pruning based on the actual usage of each rule.
    }

}
