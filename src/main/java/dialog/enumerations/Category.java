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

package dialog.enumerations;

public enum Category {
    ANSWERS(""),
    COLORS("color of the item"),
    DELIVERIES("delivery"),
    GROUPS("category of the item"),
    ITEMS("item type"),
    NUMBERS("number"),
    SIZES("size"),
    USERS("your name");

    private String about;

    Category(String about) {
        this.about = about;
    }

    public String getAbout() {
        return about;
    }

}
