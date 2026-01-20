package com.andres.ai_study_tool.common.util;

public final class JsonGuard {

    private JsonGuard() {
        // utility class
    }

    /**
     * Extracts the first valid JSON object from a raw LLM response.
     * Protects against markdown, explanations, or accidental text.
     */
    public static String extractJsonObject(String raw) {

        if (raw == null || raw.isBlank()) {
            throw new IllegalStateException("LLM returned empty response");
        }

        int braceCount = 0;
        int startIndex = -1;

        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);

            if (c == '{') {
                if (braceCount == 0) {
                    startIndex = i;
                }
                braceCount++;
            } else if (c == '}') {
                braceCount--;
                if (braceCount == 0 && startIndex != -1) {
                    return raw.substring(startIndex, i + 1);
                }
            }
        }

        throw new IllegalStateException(
                "LLM response does not contain a complete JSON object:\n" + raw
        );
    }

}

