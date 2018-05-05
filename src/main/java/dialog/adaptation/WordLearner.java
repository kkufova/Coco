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
