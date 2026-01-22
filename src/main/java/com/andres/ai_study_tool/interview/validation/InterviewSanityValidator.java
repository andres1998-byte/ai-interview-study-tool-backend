package com.andres.ai_study_tool.interview.validation;

import com.andres.ai_study_tool.common.exception.InvalidInterviewRequestException;
import com.andres.ai_study_tool.interview.dto.InterviewStartInternalResponse;
import com.andres.ai_study_tool.interview.dto.TheoryQuestion;

import java.util.List;

public final class InterviewSanityValidator {

    private InterviewSanityValidator() {
        // utility class
    }

    /**
     * Master validation entry point
     */
    public static void validateAll(
            String topic,
            InterviewStartInternalResponse internal
    ) {
        if (internal == null) {
            throw new InvalidInterviewRequestException("Empty interview response");
        }

        validateCodingQuestion(internal);
    }

    /**
     * Validates theory questions structure + relevance
     */
    private static void validateTheoryQuestions(
            String topic,
            List<TheoryQuestion> questions
    ) {
        if (topic == null || topic.isBlank()) {
            throw new InvalidInterviewRequestException("Interview topic must not be empty");
        }

        if (questions == null || questions.size() != 5) {
            throw new InvalidInterviewRequestException(
                    "Invalid number of theory questions for topic: " + topic
            );
        }

        String topicLower = topic.toLowerCase();
        long relevantCount = 0;

        java.util.Set<Integer> seenIds = new java.util.HashSet<>();

        for (TheoryQuestion q : questions) {

            if (q == null) {
                throw new InvalidInterviewRequestException(
                        "Theory question entry is null"
                );
            }

            if (q.getId() <= 0) {
                throw new InvalidInterviewRequestException(
                        "Theory question has invalid id"
                );
            }

            if (!seenIds.add(q.getId())) {
                throw new InvalidInterviewRequestException(
                        "Duplicate theory question id: " + q.getId()
                );
            }

            if (q.getQuestion() == null || q.getQuestion().isBlank()) {
                throw new InvalidInterviewRequestException(
                        "Theory question text is empty"
                );
            }

            if (q.getOptions() == null || q.getOptions().size() != 4) {
                throw new InvalidInterviewRequestException(
                        "Theory question does not have exactly 4 options"
                );
            }

            for (String opt : q.getOptions()) {
                if (opt == null || opt.isBlank()) {
                    throw new InvalidInterviewRequestException(
                            "Theory question has empty option text"
                    );
                }
            }

            if (q.getCorrectAnswer() == null || q.getCorrectAnswer().isBlank()) {
                throw new InvalidInterviewRequestException(
                        "Theory question missing correct answer"
                );
            }

            boolean correctInOptions = q.getOptions().stream()
                    .anyMatch(o -> o.equalsIgnoreCase(q.getCorrectAnswer().trim()));

            if (!correctInOptions) {
                throw new InvalidInterviewRequestException(
                        "Correct answer is not one of the provided options"
                );
            }

            if (q.getQuestion().toLowerCase().contains(topicLower)) {
                relevantCount++;
            }
        }

        // Require at least 2 questions clearly tied to topic
        if (relevantCount < 2) {
            throw new InvalidInterviewRequestException(
                    "Interview questions are not related to topic: " + topic
            );
        }
    }

    /**
     * Validates coding question existence
     */
    private static void validateCodingQuestion(
            InterviewStartInternalResponse internal
    ) {
        if (internal.getCodingQuestion() == null) {
            throw new InvalidInterviewRequestException(
                    "Missing coding question"
            );
        }

        if (internal.getCodingQuestion().getPrompt() == null ||
                internal.getCodingQuestion().getPrompt().isBlank()) {
            throw new InvalidInterviewRequestException(
                    "Coding question prompt is empty"
            );
        }

        if (internal.getCodingQuestion().getMethodSignature() == null ||
                internal.getCodingQuestion().getMethodSignature().isBlank()) {
            throw new InvalidInterviewRequestException(
                    "Coding question method signature is empty"
            );
        }
    }
}
