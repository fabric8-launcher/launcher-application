/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.fabric8.forge.generator.quickstart;

import static java.lang.Character.isDigit;
import static java.lang.Character.isLetter;
import static java.lang.Character.isLowerCase;

/**
 */
public class ProjectNameValidator {
    /**
     * Returns null if the project name is valid or a validation error message
     */
    public static String validProjectName(String name) {
        int length = name.length();
        if (length > 0) {
            char ch = name.charAt(0);
            if (!isLowerCaseLetter(ch)) {
                return "The project name must start with a lower case letter";
            }
            char last = name.charAt(length - 1);
            if (!isLowerCaseLetter(last) && !isDigit(last)) {
                return "The project name must end with a lowe case letter or digit";
            }
            for (int i = 1; i < length - 1; i++) {
                char c =  name.charAt(i);
                if (!isLowerCaseLetter(c) && !isDigit(c) && !isValidNameSeparator(c)) {
                    return "The project name must contain lower case letters, digits and dashes only";
                }
            }
        }
        return null;
    }

    public static boolean isLowerCaseLetter(char ch) {
        return isLetter(ch) && isLowerCase(ch);
    }

    public static boolean isValidNameSeparator(char ch) {
        return ch == '-';
    }
}
