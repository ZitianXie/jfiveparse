/**
 * Copyright (C) 2015 digitalfondue (info@digitalfondue.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.digitalfondue.jfiveparse;

import java.util.Arrays;

/**
 * Resizable char buffer
 * <ul>
 * <li>only chars can be appended (avoid confusion with
 * StringBuilder.append(int))
 * <li>2 CharBuilders can be compared for equality: StringBuilder does not
 * provide an equality method
 * </ul>
 */
class ResizableCharBuilder {

    char[] buff = new char[16];
    int pos = 0;

    ResizableCharBuilder() {
    }

    ResizableCharBuilder(String s) {
        for (char c : s.toCharArray()) {
            append(c);
        }
    }

    void append(char c) {
        if (pos < buff.length) {
            buff[pos] = c;
            pos++;
        } else {
            buff = Arrays.copyOf(buff, buff.length * 2 + 2);
            buff[pos] = c;
            pos++;
        }
    }

    char charAt(int idx) {
        return buff[idx];
    }

    String asString() {
        return new String(buff, 0, pos);
    }

    char[] toCharArray() {
        return Arrays.copyOf(buff, pos);
    }

    @Override
    public String toString() {
        return asString();
    }

    int length() {
        return pos;
    }

    boolean same(ResizableCharBuilder cb) {
        return pos == cb.pos && Arrays.equals(buff, cb.buff);
    }
}