package com.campusfind.lostfound.util;

import java.util.*;

public class SimilarityUtil {

    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "a", "an", "the", "and", "or", "in", "on", "at", "to", "for", "with",
            "is", "of", "it", "my", "this", "that", "near", "by", "from", "around"
    ));

    /**
     * Compute Levenshtein similarity between two strings (0.0 to 1.0)
     */
    public static double levenshteinSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) return 0.0;
        s1 = s1.trim().toLowerCase();
        s2 = s2.trim().toLowerCase();
        if (s1.equals(s2)) return 1.0;
        if (s1.isEmpty() || s2.isEmpty()) return 0.0;

        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= s2.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }

        int maxLen = Math.max(s1.length(), s2.length());
        int distance = dp[s1.length()][s2.length()];
        return Math.max(0.0, 1.0 - ((double) distance / maxLen));
    }

    /**
     * Extract normalized tokens ignoring punctuation and stop words
     */
    public static Set<String> tokenize(String text) {
        Set<String> tokens = new HashSet<>();
        if (text == null || text.trim().isEmpty()) return tokens;

        String[] rawTokens = text.toLowerCase()
                .replaceAll("[^a-zA-Z0-9\\s]", " ")
                .split("\\s+");

        for (String token : rawTokens) {
            String trimmed = token.trim();
            if (trimmed.length() > 1 && !STOP_WORDS.contains(trimmed)) {
                tokens.add(trimmed);
            }
        }
        return tokens;
    }

    /**
     * Compute Jaccard token similarity (0.0 to 1.0)
     */
    public static double jaccardSimilarity(String s1, String s2) {
        Set<String> set1 = tokenize(s1);
        Set<String> set2 = tokenize(s2);

        if (set1.isEmpty() || set2.isEmpty()) return 0.0;

        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        if (union.isEmpty()) return 0.0;
        return (double) intersection.size() / union.size();
    }

    /**
     * Combined name similarity using Levenshtein distance + Jaccard index
     */
    public static double nameSimilarity(String name1, String name2) {
        if (name1 == null || name2 == null) return 0.0;
        String n1 = name1.toLowerCase().trim();
        String n2 = name2.toLowerCase().trim();

        // Exact match or substring inclusion boost
        if (n1.equals(n2)) return 1.0;
        if (n1.contains(n2) || n2.contains(n1)) {
            return 0.90;
        }

        double lev = levenshteinSimilarity(n1, n2);
        double jac = jaccardSimilarity(n1, n2);

        return Math.max(lev, (lev * 0.4 + jac * 0.6));
    }

    /**
     * Location proximity similarity (0.0 to 1.0)
     */
    public static double locationSimilarity(String loc1, String loc2) {
        if (loc1 == null || loc2 == null) return 0.0;
        String l1 = loc1.toLowerCase().trim();
        String l2 = loc2.toLowerCase().trim();

        if (l1.equals(l2)) return 1.0;
        if (l1.contains(l2) || l2.contains(l1)) return 0.85;

        // Check common campus landmarks/keywords
        Set<String> tokens1 = tokenize(l1);
        Set<String> tokens2 = tokenize(l2);

        Set<String> common = new HashSet<>(tokens1);
        common.retainAll(tokens2);

        if (!common.isEmpty()) {
            return 0.70;
        }

        return jaccardSimilarity(l1, l2);
    }
}
