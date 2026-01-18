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

        int firstBrace = raw.indexOf('{');
        int lastBrace = raw.lastIndexOf('}');

        if (firstBrace == -1 || lastBrace == -1 || lastBrace <= firstBrace) {
            throw new IllegalStateException(
                    "LLM response does not contain valid JSON:\n" + raw
            );
        }

        return raw.substring(firstBrace, lastBrace + 1);
    }
}
