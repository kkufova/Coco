/*
 * Diff Match and Patch
 * Copyright 2018 The diff-match-patch Authors.
 * https://github.com/google/diff-match-patch
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dialog.adaptation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Pattern;

class DiffMatchPatch {

    private final float Diff_Timeout = 1.0f;

    static class LinesToCharsResult {

        final String chars1;
        final String chars2;
        final List<String> lineArray;

        LinesToCharsResult(String chars1, String chars2, List<String> lineArray) {
            this.chars1 = chars1;
            this.chars2 = chars2;
            this.lineArray = lineArray;
        }
    }

    public enum Operation {
        DELETE, INSERT, EQUAL
    }

    LinkedList<Diff> diff_main(String text1, String text2, boolean checkLines) {
        long deadline;
        if (Diff_Timeout <= 0) {
            deadline = Long.MAX_VALUE;
        } else {
            deadline = System.currentTimeMillis() + (long) (Diff_Timeout * 1000);
        }
        return diff_main(text1, text2, checkLines, deadline);
    }

    private LinkedList<Diff> diff_main(String text1, String text2, boolean checkLines, long deadline) {
        if (text1 == null || text2 == null) {
            throw new IllegalArgumentException("Null inputs. (diff_main)");
        }

        LinkedList<Diff> diffs;
        if (text1.equals(text2)) {
            diffs = new LinkedList<>();
            if (text1.length() != 0) {
                diffs.add(new Diff(Operation.EQUAL, text1));
            }
            return diffs;
        }

        int commonLength = diff_commonPrefix(text1, text2);
        String commonPrefix = text1.substring(0, commonLength);
        text1 = text1.substring(commonLength);
        text2 = text2.substring(commonLength);

        commonLength = diff_commonSuffix(text1, text2);
        String commonSuffix = text1.substring(text1.length() - commonLength);
        text1 = text1.substring(0, text1.length() - commonLength);
        text2 = text2.substring(0, text2.length() - commonLength);

        diffs = diff_compute(text1, text2, checkLines, deadline);

        if (commonPrefix.length() != 0) {
            diffs.addFirst(new Diff(Operation.EQUAL, commonPrefix));
        }
        if (commonSuffix.length() != 0) {
            diffs.addLast(new Diff(Operation.EQUAL, commonSuffix));
        }

        diff_cleanupMerge(diffs);
        return diffs;
    }

    private LinkedList<Diff> diff_compute(String text1, String text2, boolean checkLines, long deadline) {
        LinkedList<Diff> diffs = new LinkedList<>();

        if (text1.length() == 0) {
            diffs.add(new Diff(Operation.INSERT, text2));
            return diffs;
        }

        if (text2.length() == 0) {
            diffs.add(new Diff(Operation.DELETE, text1));
            return diffs;
        }

        String longText = text1.length() > text2.length() ? text1 : text2;
        String shortText = text1.length() > text2.length() ? text2 : text1;
        int i = longText.indexOf(shortText);
        if (i != -1) {
            Operation op = (text1.length() > text2.length()) ? Operation.DELETE : Operation.INSERT;
            diffs.add(new Diff(op, longText.substring(0, i)));
            diffs.add(new Diff(Operation.EQUAL, shortText));
            diffs.add(new Diff(op, longText.substring(i + shortText.length())));
            return diffs;
        }

        if (shortText.length() == 1) {
            diffs.add(new Diff(Operation.DELETE, text1));
            diffs.add(new Diff(Operation.INSERT, text2));
            return diffs;
        }

        String[] hm = diff_halfMatch(text1, text2);
        if (hm != null) {
            String text1_a = hm[0];
            String text1_b = hm[1];
            String text2_a = hm[2];
            String text2_b = hm[3];
            String mid_common = hm[4];
            LinkedList<Diff> diffs_a = diff_main(text1_a, text2_a, checkLines, deadline);
            LinkedList<Diff> diffs_b = diff_main(text1_b, text2_b, checkLines, deadline);

            diffs = diffs_a;
            diffs.add(new Diff(Operation.EQUAL, mid_common));
            diffs.addAll(diffs_b);

            return diffs;
        }

        if (checkLines && text1.length() > 100 && text2.length() > 100) {
            return diff_lineMode(text1, text2, deadline);
        }

        return diff_bisect(text1, text2, deadline);
    }

    private LinkedList<Diff> diff_lineMode(String text1, String text2, long deadline) {
        LinesToCharsResult b = diff_linesToChars(text1, text2);
        text1 = b.chars1;
        text2 = b.chars2;
        List<String> lineArray = b.lineArray;

        LinkedList<Diff> diffs = diff_main(text1, text2, false, deadline);

        diff_charsToLines(diffs, lineArray);
        diff_cleanupSemantic(diffs);

        diffs.add(new Diff(Operation.EQUAL, ""));
        int count_delete = 0;
        int count_insert = 0;
        String text_delete = "";
        String text_insert = "";
        ListIterator<Diff> pointer = diffs.listIterator();
        Diff thisDiff = pointer.next();
        while (thisDiff != null) {
            switch (thisDiff.operation) {
                case INSERT:
                    count_insert++;
                    text_insert += thisDiff.text;
                    break;
                case DELETE:
                    count_delete++;
                    text_delete += thisDiff.text;
                    break;
                case EQUAL:
                    if (count_delete >= 1 && count_insert >= 1) {
                        pointer.previous();
                        for (int j = 0; j < count_delete + count_insert; j++) {
                            pointer.previous();
                            pointer.remove();
                        }
                        for (Diff newDiff : diff_main(text_delete, text_insert, false, deadline)) {
                            pointer.add(newDiff);
                        }
                    }
                    count_insert = 0;
                    count_delete = 0;
                    text_delete = "";
                    text_insert = "";
                    break;
            }
            thisDiff = pointer.hasNext() ? pointer.next() : null;
        }
        diffs.removeLast();

        return diffs;
    }

    private LinkedList<Diff> diff_bisect(String text1, String text2, long deadline) {
        int text1_length = text1.length();
        int text2_length = text2.length();
        int max_d = (text1_length + text2_length + 1) / 2;
        int v_offset = max_d;
        int v_length = 2 * max_d;
        int[] v1 = new int[v_length];
        int[] v2 = new int[v_length];

        for (int x = 0; x < v_length; x++) {
            v1[x] = -1;
            v2[x] = -1;
        }

        v1[v_offset + 1] = 0;
        v2[v_offset + 1] = 0;
        int delta = text1_length - text2_length;
        boolean front = (delta % 2 != 0);
        int k1start = 0;
        int k1end = 0;
        int k2start = 0;
        int k2end = 0;
        for (int d = 0; d < max_d; d++) {
            if (System.currentTimeMillis() > deadline) {
                break;
            }

            for (int k1 = -d + k1start; k1 <= d - k1end; k1 += 2) {
                int k1_offset = v_offset + k1;
                int x1;
                if (k1 == -d || (k1 != d && v1[k1_offset - 1] < v1[k1_offset + 1])) {
                    x1 = v1[k1_offset + 1];
                } else {
                    x1 = v1[k1_offset - 1] + 1;
                }
                int y1 = x1 - k1;
                while (x1 < text1_length && y1 < text2_length && text1.charAt(x1) == text2.charAt(y1)) {
                    x1++;
                    y1++;
                }
                v1[k1_offset] = x1;
                if (x1 > text1_length) {
                    k1end += 2;
                } else if (y1 > text2_length) {
                    k1start += 2;
                } else if (front) {
                    int k2_offset = v_offset + delta - k1;
                    if (k2_offset >= 0 && k2_offset < v_length && v2[k2_offset] != -1) {
                        int x2 = text1_length - v2[k2_offset];
                        if (x1 >= x2) {
                            return diff_bisectSplit(text1, text2, x1, y1, deadline);
                        }
                    }
                }
            }

            for (int k2 = -d + k2start; k2 <= d - k2end; k2 += 2) {
                int k2_offset = v_offset + k2;
                int x2;
                if (k2 == -d || (k2 != d && v2[k2_offset - 1] < v2[k2_offset + 1])) {
                    x2 = v2[k2_offset + 1];
                } else {
                    x2 = v2[k2_offset - 1] + 1;
                }
                int y2 = x2 - k2;
                while (x2 < text1_length && y2 < text2_length && text1.charAt(text1_length - x2 - 1) == text2.charAt(text2_length - y2 - 1)) {
                    x2++;
                    y2++;
                }
                v2[k2_offset] = x2;
                if (x2 > text1_length) {
                    k2end += 2;
                } else if (y2 > text2_length) {
                    k2start += 2;
                } else if (!front) {
                    int k1_offset = v_offset + delta - k2;
                    if (k1_offset >= 0 && k1_offset < v_length && v1[k1_offset] != -1) {
                        int x1 = v1[k1_offset];
                        int y1 = v_offset + x1 - k1_offset;
                        x2 = text1_length - x2;
                        if (x1 >= x2) {
                            return diff_bisectSplit(text1, text2, x1, y1, deadline);
                        }
                    }
                }
            }
        }

        LinkedList<Diff> diffs = new LinkedList<>();
        diffs.add(new Diff(Operation.DELETE, text1));
        diffs.add(new Diff(Operation.INSERT, text2));
        return diffs;
    }

    private LinkedList<Diff> diff_bisectSplit(String text1, String text2, int x, int y, long deadline) {
        String text1a = text1.substring(0, x);
        String text2a = text2.substring(0, y);
        String text1b = text1.substring(x);
        String text2b = text2.substring(y);

        LinkedList<Diff> diffs = diff_main(text1a, text2a, false, deadline);
        LinkedList<Diff> diffs2 = diff_main(text1b, text2b, false, deadline);

        diffs.addAll(diffs2);
        return diffs;
    }

    private LinesToCharsResult diff_linesToChars(String text1, String text2) {
        List<String> lineArray = new ArrayList<>();
        Map<String, Integer> lineHash = new HashMap<>();
        lineArray.add("");

        String chars1 = diff_linesToCharsMunge(text1, lineArray, lineHash);
        String chars2 = diff_linesToCharsMunge(text2, lineArray, lineHash);
        return new LinesToCharsResult(chars1, chars2, lineArray);
    }

    LinesToCharsResult diff_linesToWords(String text1, String text2) {
        List<String> lineArray = new ArrayList<>();
        Map<String, Integer> lineHash = new HashMap<>();
        lineArray.add("");

        String chars1 = diff_linesToWordsMunge(text1, lineArray, lineHash);
        String chars2 = diff_linesToWordsMunge(text2, lineArray, lineHash);
        return new LinesToCharsResult(chars1, chars2, lineArray);
    }

    private String diff_linesToWordsMunge(String text, List<String> lineArray, Map<String, Integer> lineHash) {
        int lineStart = 0;
        int lineEnd = -1;
        String line;
        StringBuilder chars = new StringBuilder();
        while (lineEnd < text.length() - 1) {
            lineEnd = text.indexOf(' ', lineStart);
            if (lineEnd == -1) {
                lineEnd = text.length() - 1;
            }
            line = text.substring(lineStart, lineEnd + 1);
            lineStart = lineEnd + 1;

            if (lineHash.containsKey(line)) {
                chars.append(String.valueOf((char) (int) lineHash.get(line)));
            } else {
                lineArray.add(line);
                lineHash.put(line, lineArray.size() - 1);
                chars.append(String.valueOf((char) (lineArray.size() - 1)));
            }
        }
        return chars.toString();
    }

    private String diff_linesToCharsMunge(String text, List<String> lineArray, Map<String, Integer> lineHash) {
        int lineStart = 0;
        int lineEnd = -1;
        String line;
        StringBuilder chars = new StringBuilder();

        while (lineEnd < text.length() - 1) {
            lineEnd = text.indexOf('\n', lineStart);
            if (lineEnd == -1) {
                lineEnd = text.length() - 1;
            }
            line = text.substring(lineStart, lineEnd + 1);
            lineStart = lineEnd + 1;

            if (lineHash.containsKey(line)) {
                chars.append(String.valueOf((char) (int) lineHash.get(line)));
            } else {
                lineArray.add(line);
                lineHash.put(line, lineArray.size() - 1);
                chars.append(String.valueOf((char) (lineArray.size() - 1)));
            }
        }
        return chars.toString();
    }

    void diff_charsToLines(LinkedList<Diff> diffs, List<String> lineArray) {
        StringBuilder text;
        for (Diff diff : diffs) {
            text = new StringBuilder();
            for (int y = 0; y < diff.text.length(); y++) {
                text.append(lineArray.get(diff.text.charAt(y)));
            }
            diff.text = text.toString();
        }
    }

    private int diff_commonPrefix(String text1, String text2) {
        int n = Math.min(text1.length(), text2.length());
        for (int i = 0; i < n; i++) {
            if (text1.charAt(i) != text2.charAt(i)) {
                return i;
            }
        }
        return n;
    }

    private int diff_commonSuffix(String text1, String text2) {
        int text1_length = text1.length();
        int text2_length = text2.length();
        int n = Math.min(text1_length, text2_length);
        for (int i = 1; i <= n; i++) {
            if (text1.charAt(text1_length - i) != text2.charAt(text2_length - i)) {
                return i - 1;
            }
        }
        return n;
    }

    private int diff_commonOverlap(String text1, String text2) {
        int text1_length = text1.length();
        int text2_length = text2.length();
        if (text1_length == 0 || text2_length == 0) {
            return 0;
        }
        if (text1_length > text2_length) {
            text1 = text1.substring(text1_length - text2_length);
        } else if (text1_length < text2_length) {
            text2 = text2.substring(0, text1_length);
        }
        int text_length = Math.min(text1_length, text2_length);
        if (text1.equals(text2)) {
            return text_length;
        }

        int best = 0;
        int length = 1;
        while (true) {
            String pattern = text1.substring(text_length - length);
            int found = text2.indexOf(pattern);
            if (found == -1) {
                return best;
            }
            length += found;
            if (found == 0 || text1.substring(text_length - length).equals(text2.substring(0, length))) {
                best = length;
                length++;
            }
        }
    }

    private String[] diff_halfMatch(String text1, String text2) {
        if (Diff_Timeout <= 0) {
            return null;
        }
        String longText = text1.length() > text2.length() ? text1 : text2;
        String shortText = text1.length() > text2.length() ? text2 : text1;
        if (longText.length() < 4 || shortText.length() * 2 < longText.length()) {
            return null;
        }

        String[] hm1 = diff_halfMatchI(longText, shortText, (longText.length() + 3) / 4);
        String[] hm2 = diff_halfMatchI(longText, shortText, (longText.length() + 1) / 2);
        String[] hm;
        if (hm1 == null && hm2 == null) {
            return null;
        } else if (hm2 == null) {
            hm = hm1;
        } else if (hm1 == null) {
            hm = hm2;
        } else {
            hm = hm1[4].length() > hm2[4].length() ? hm1 : hm2;
        }

        if (text1.length() > text2.length()) {
            return hm;
        } else {
            return new String[]{hm[2], hm[3], hm[0], hm[1], hm[4]};
        }
    }

    private String[] diff_halfMatchI(String longtext, String shortText, int i) {
        String seed = longtext.substring(i, i + longtext.length() / 4);
        int j = -1;
        String best_common = "";
        String best_longText_a = "", best_longtext_b = "";
        String best_shortText_a = "", best_shortText_b = "";

        while ((j = shortText.indexOf(seed, j + 1)) != -1) {
            int prefixLength = diff_commonPrefix(longtext.substring(i), shortText.substring(j));
            int suffixLength = diff_commonSuffix(longtext.substring(0, i), shortText.substring(0, j));
            if (best_common.length() < suffixLength + prefixLength) {
                best_common = shortText.substring(j - suffixLength, j) + shortText.substring(j, j + prefixLength);
                best_longText_a = longtext.substring(0, i - suffixLength);
                best_longtext_b = longtext.substring(i + prefixLength);
                best_shortText_a = shortText.substring(0, j - suffixLength);
                best_shortText_b = shortText.substring(j + prefixLength);
            }
        }

        if (best_common.length() * 2 >= longtext.length()) {
            return new String[]{best_longText_a, best_longtext_b,
                    best_shortText_a, best_shortText_b, best_common};
        } else {
            return null;
        }
    }

    void diff_cleanupSemantic(LinkedList<Diff> diffs) {
        if (diffs.isEmpty()) {
            return;
        }
        boolean changes = false;
        Stack<Diff> equalities = new Stack<>();
        String lastEquality = null;
        ListIterator<Diff> pointer = diffs.listIterator();
        int length_insertions1 = 0;
        int length_deletions1 = 0;
        int length_insertions2 = 0;
        int length_deletions2 = 0;
        Diff thisDiff = pointer.next();
        while (thisDiff != null) {
            if (thisDiff.operation == Operation.EQUAL) {
                equalities.push(thisDiff);
                length_insertions1 = length_insertions2;
                length_deletions1 = length_deletions2;
                length_insertions2 = 0;
                length_deletions2 = 0;
                lastEquality = thisDiff.text;
            } else {
                if (thisDiff.operation == Operation.INSERT) {
                    length_insertions2 += thisDiff.text.length();
                } else {
                    length_deletions2 += thisDiff.text.length();
                }
                if (lastEquality != null && (lastEquality.length()
                        <= Math.max(length_insertions1, length_deletions1))
                        && (lastEquality.length()
                        <= Math.max(length_insertions2, length_deletions2))) {
                    while (thisDiff != equalities.lastElement()) {
                        thisDiff = pointer.previous();
                    }
                    pointer.next();

                    pointer.set(new Diff(Operation.DELETE, lastEquality));
                    pointer.add(new Diff(Operation.INSERT, lastEquality));

                    equalities.pop();
                    if (!equalities.empty()) {
                        equalities.pop();
                    }
                    if (equalities.empty()) {
                        while (pointer.hasPrevious()) {
                            pointer.previous();
                        }
                    } else {
                        thisDiff = equalities.lastElement();
                        while (thisDiff != pointer.previous()) {
                            // Intentionally empty loop.
                        }
                    }

                    length_insertions1 = 0;
                    length_insertions2 = 0;
                    length_deletions1 = 0;
                    length_deletions2 = 0;
                    lastEquality = null;
                    changes = true;
                }
            }
            thisDiff = pointer.hasNext() ? pointer.next() : null;
        }

        if (changes) {
            diff_cleanupMerge(diffs);
        }
        diff_cleanupSemanticLossless(diffs);

        pointer = diffs.listIterator();
        Diff prevDiff = null;
        thisDiff = null;
        if (pointer.hasNext()) {
            prevDiff = pointer.next();
            if (pointer.hasNext()) {
                thisDiff = pointer.next();
            }
        }
        while (thisDiff != null) {
            if (prevDiff.operation == Operation.DELETE && thisDiff.operation == Operation.INSERT) {
                String deletion = prevDiff.text;
                String insertion = thisDiff.text;
                int overlap_length1 = this.diff_commonOverlap(deletion, insertion);
                int overlap_length2 = this.diff_commonOverlap(insertion, deletion);
                if (overlap_length1 >= overlap_length2) {
                    if (overlap_length1 >= deletion.length() / 2.0 || overlap_length1 >= insertion.length() / 2.0) {
                        pointer.previous();
                        pointer.add(new Diff(Operation.EQUAL, insertion.substring(0, overlap_length1)));
                        prevDiff.text = deletion.substring(0, deletion.length() - overlap_length1);
                        thisDiff.text = insertion.substring(overlap_length1);
                    }
                } else {
                    if (overlap_length2 >= deletion.length() / 2.0 || overlap_length2 >= insertion.length() / 2.0) {
                        pointer.previous();
                        pointer.add(new Diff(Operation.EQUAL, deletion.substring(0, overlap_length2)));
                        prevDiff.operation = Operation.INSERT;
                        prevDiff.text = insertion.substring(0, insertion.length() - overlap_length2);
                        thisDiff.operation = Operation.DELETE;
                        thisDiff.text = deletion.substring(overlap_length2);
                    }
                }
                thisDiff = pointer.hasNext() ? pointer.next() : null;
            }
            prevDiff = thisDiff;
            thisDiff = pointer.hasNext() ? pointer.next() : null;
        }
    }

    private void diff_cleanupSemanticLossless(LinkedList<Diff> diffs) {
        String equality1, edit, equality2;
        String commonString;
        int commonOffset;
        int score, bestScore;
        String bestEquality1, bestEdit, bestEquality2;
        ListIterator<Diff> pointer = diffs.listIterator();
        Diff prevDiff = pointer.hasNext() ? pointer.next() : null;
        Diff thisDiff = pointer.hasNext() ? pointer.next() : null;
        Diff nextDiff = pointer.hasNext() ? pointer.next() : null;
        while (nextDiff != null) {
            if (prevDiff.operation == Operation.EQUAL && nextDiff.operation == Operation.EQUAL) {
                equality1 = prevDiff.text;
                edit = thisDiff.text;
                equality2 = nextDiff.text;

                commonOffset = diff_commonSuffix(equality1, edit);
                if (commonOffset != 0) {
                    commonString = edit.substring(edit.length() - commonOffset);
                    equality1 = equality1.substring(0, equality1.length() - commonOffset);
                    edit = commonString + edit.substring(0, edit.length() - commonOffset);
                    equality2 = commonString + equality2;
                }

                bestEquality1 = equality1;
                bestEdit = edit;
                bestEquality2 = equality2;
                bestScore = diff_cleanupSemanticScore(equality1, edit) + diff_cleanupSemanticScore(edit, equality2);
                while (edit.length() != 0 && equality2.length() != 0 && edit.charAt(0) == equality2.charAt(0)) {
                    equality1 += edit.charAt(0);
                    edit = edit.substring(1) + equality2.charAt(0);
                    equality2 = equality2.substring(1);
                    score = diff_cleanupSemanticScore(equality1, edit) + diff_cleanupSemanticScore(edit, equality2);
                    if (score >= bestScore) {
                        bestScore = score;
                        bestEquality1 = equality1;
                        bestEdit = edit;
                        bestEquality2 = equality2;
                    }
                }

                if (!prevDiff.text.equals(bestEquality1)) {
                    if (bestEquality1.length() != 0) {
                        prevDiff.text = bestEquality1;
                    } else {
                        pointer.previous();
                        pointer.previous();
                        pointer.previous();
                        pointer.remove();
                        pointer.next();
                        pointer.next();
                    }
                    thisDiff.text = bestEdit;
                    if (bestEquality2.length() != 0) {
                        nextDiff.text = bestEquality2;
                    } else {
                        pointer.remove();
                        nextDiff = thisDiff;
                        thisDiff = prevDiff;
                    }
                }
            }
            prevDiff = thisDiff;
            thisDiff = nextDiff;
            nextDiff = pointer.hasNext() ? pointer.next() : null;
        }
    }

    private int diff_cleanupSemanticScore(String one, String two) {
        if (one.length() == 0 || two.length() == 0) {
            return 6;
        }

        char char1 = one.charAt(one.length() - 1);
        char char2 = two.charAt(0);
        boolean nonAlphaNumeric1 = !Character.isLetterOrDigit(char1);
        boolean nonAlphaNumeric2 = !Character.isLetterOrDigit(char2);
        boolean whitespace1 = nonAlphaNumeric1 && Character.isWhitespace(char1);
        boolean whitespace2 = nonAlphaNumeric2 && Character.isWhitespace(char2);
        boolean lineBreak1 = whitespace1 && Character.getType(char1) == Character.CONTROL;
        boolean lineBreak2 = whitespace2 && Character.getType(char2) == Character.CONTROL;
        boolean blankLine1 = lineBreak1 && BLANK_LINE_END.matcher(one).find();
        boolean blankLine2 = lineBreak2 && BLANK_LINE_START.matcher(two).find();

        if (blankLine1 || blankLine2) {
            return 5;
        } else if (lineBreak1 || lineBreak2) {
            return 4;
        } else if (nonAlphaNumeric1 && !whitespace1 && whitespace2) {
            return 3;
        } else if (whitespace1 || whitespace2) {
            return 2;
        } else if (nonAlphaNumeric1 || nonAlphaNumeric2) {
            return 1;
        }
        return 0;
    }

    private final Pattern BLANK_LINE_END = Pattern.compile("\\n\\r?\\n\\Z", Pattern.DOTALL);
    private final Pattern BLANK_LINE_START = Pattern.compile("\\A\\r?\\n\\r?\\n", Pattern.DOTALL);

    private void diff_cleanupMerge(LinkedList<Diff> diffs) {
        diffs.add(new Diff(Operation.EQUAL, ""));
        ListIterator<Diff> pointer = diffs.listIterator();
        int count_delete = 0;
        int count_insert = 0;
        String text_delete = "";
        String text_insert = "";
        Diff thisDiff = pointer.next();
        Diff prevEqual = null;
        int commonLength;
        while (thisDiff != null) {
            switch (thisDiff.operation) {
                case INSERT:
                    count_insert++;
                    text_insert += thisDiff.text;
                    prevEqual = null;
                    break;
                case DELETE:
                    count_delete++;
                    text_delete += thisDiff.text;
                    prevEqual = null;
                    break;
                case EQUAL:
                    if (count_delete + count_insert > 1) {
                        boolean both_types = count_delete != 0 && count_insert != 0;
                        pointer.previous();
                        while (count_delete-- > 0) {
                            pointer.previous();
                            pointer.remove();
                        }
                        while (count_insert-- > 0) {
                            pointer.previous();
                            pointer.remove();
                        }
                        if (both_types) {
                            commonLength = diff_commonPrefix(text_insert, text_delete);
                            if (commonLength != 0) {
                                if (pointer.hasPrevious()) {
                                    thisDiff = pointer.previous();
                                    assert thisDiff.operation == Operation.EQUAL : "Previous diff should have been an equality.";
                                    thisDiff.text += text_insert.substring(0, commonLength);
                                    pointer.next();
                                } else {
                                    pointer.add(new Diff(Operation.EQUAL, text_insert.substring(0, commonLength)));
                                }
                                text_insert = text_insert.substring(commonLength);
                                text_delete = text_delete.substring(commonLength);
                            }
                            commonLength = diff_commonSuffix(text_insert, text_delete);
                            if (commonLength != 0) {
                                thisDiff = pointer.next();
                                thisDiff.text = text_insert.substring(text_insert.length() - commonLength) + thisDiff.text;
                                text_insert = text_insert.substring(0, text_insert.length() - commonLength);
                                text_delete = text_delete.substring(0, text_delete.length() - commonLength);
                                pointer.previous();
                            }
                        }
                        if (text_delete.length() != 0) {
                            pointer.add(new Diff(Operation.DELETE, text_delete));
                        }
                        if (text_insert.length() != 0) {
                            pointer.add(new Diff(Operation.INSERT, text_insert));
                        }
                        thisDiff = pointer.hasNext() ? pointer.next() : null;
                    } else if (prevEqual != null) {
                        prevEqual.text += thisDiff.text;
                        pointer.remove();
                        thisDiff = pointer.previous();
                        pointer.next();
                    }
                    count_insert = 0;
                    count_delete = 0;
                    text_delete = "";
                    text_insert = "";
                    prevEqual = thisDiff;
                    break;
            }
            thisDiff = pointer.hasNext() ? pointer.next() : null;
        }
        if (diffs.getLast().text.length() == 0) {
            diffs.removeLast();
        }

        boolean changes = false;

        pointer = diffs.listIterator();
        Diff prevDiff = pointer.hasNext() ? pointer.next() : null;
        thisDiff = pointer.hasNext() ? pointer.next() : null;
        Diff nextDiff = pointer.hasNext() ? pointer.next() : null;

        while (nextDiff != null) {
            if (prevDiff.operation == Operation.EQUAL &&
                    nextDiff.operation == Operation.EQUAL) {
                if (thisDiff.text.endsWith(prevDiff.text)) {
                    thisDiff.text = prevDiff.text
                            + thisDiff.text.substring(0, thisDiff.text.length()
                            - prevDiff.text.length());
                    nextDiff.text = prevDiff.text + nextDiff.text;
                    pointer.previous();
                    pointer.previous();
                    pointer.previous();
                    pointer.remove();
                    pointer.next();
                    thisDiff = pointer.next();
                    nextDiff = pointer.hasNext() ? pointer.next() : null;
                    changes = true;
                } else if (thisDiff.text.startsWith(nextDiff.text)) {
                    prevDiff.text += nextDiff.text;
                    thisDiff.text = thisDiff.text.substring(nextDiff.text.length()) + nextDiff.text;
                    pointer.remove();
                    nextDiff = pointer.hasNext() ? pointer.next() : null;
                    changes = true;
                }
            }
            prevDiff = thisDiff;
            thisDiff = nextDiff;
            nextDiff = pointer.hasNext() ? pointer.next() : null;
        }
        if (changes) {
            diff_cleanupMerge(diffs);
        }
    }

    public static class Diff {

        Operation operation;
        String text;

        Diff(Operation operation, String text) {
            this.operation = operation;
            this.text = text;
        }

        public String toString() {
            String prettyText = this.text.replace('\n', '\u00b6');
            return "Diff(" + this.operation + ",\"" + prettyText + "\")";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = (operation == null) ? 0 : operation.hashCode();
            result += prime * ((text == null) ? 0 : text.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Diff other = (Diff) obj;
            if (operation != other.operation) {
                return false;
            }
            if (text == null) {
                if (other.text != null) {
                    return false;
                }
            } else if (!text.equals(other.text)) {
                return false;
            }
            return true;
        }
    }

}
