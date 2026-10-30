package com.ixtom.uds.utils;

public class StringUtils {
    public static Integer parseIntInRange(String num, int low, int high) {
        if (num == null) return null;
        try {
            int val = Integer.parseInt(num);

            return val >= low && val <= high ? val : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static boolean stringLengthInRange(String s, int min, int max) {
        return s != null && s.length() >= min && s.length() <= max;
    }

    public static boolean isValidUsername(String s) {
        String regex = "^[a-z0-9_]{2,25}$";
        return s != null && s.matches(regex);
    }

    public static boolean isEmail(String s) {
        // https://emailregex.com/
        String regex = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";
        return s != null && s.matches(regex);
    }

    public static boolean isGoodPassword(String s) {
        // one upper & lower char, one number, one non alphanumeric and at least 8 chars
        return s != null && s.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[^a-zA-Z0-9]).{8,}$");
    }

    public static String toTitleCase(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }

        String[] words = s.toLowerCase().split("\\s+");

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            words[i] =
                Character.toUpperCase(word.charAt(0)) + word.substring(1);
        }

        return String.join(" ", words);
    }
}
