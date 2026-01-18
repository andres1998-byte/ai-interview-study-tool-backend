package com.andres.ai_study_tool.interview.validation;

import com.andres.ai_study_tool.common.exception.InvalidInterviewRequestException;
import com.andres.ai_study_tool.interview.dto.InterviewStartInternalResponse;
import com.andres.ai_study_tool.interview.dto.TheoryQuestion;

public final class InterviewStructureValidator {

    private InterviewStructureValidator() {
        // utility class
    }

    public static void validate(InterviewStartInternalResponse interview) {

        if (interview == null) {
            throw new InvalidInterviewRequestException("Interview payload is null");
        }

        // 🔹 Theory questions
        if (interview.getTheoryQuestions() == null ||
                interview.getTheoryQuestions().size() != 5) {

            throw new InvalidInterviewRequestException(
                    "Interview must contain exactly 5 theory questions"
            );
        }

        for (TheoryQuestion q : interview.getTheoryQuestions()) {
            validateTheoryQuestion(q);
        }

        // 🔹 Coding question
        if (interview.getCodingQuestion() == null) {
            throw new InvalidInterviewRequestException(
                    "Interview must contain exactly 1 coding question"
            );
        }

        if (isBlank(interview.getCodingQuestion().getPrompt()) ||
                isBlank(interview.getCodingQuestion().getMethodSignature())) {

            throw new InvalidInterviewRequestException(
                    "Coding question is missing prompt or method signature"
            );
        }
    }

    private static void validateTheoryQuestion(TheoryQuestion q) {

        if (q.getId() <= 0) {
            throw new InvalidInterviewRequestException(
                    "Theory question has invalid id"
            );
        }


        if (isBlank(q.getQuestion())) {
            throw new InvalidInterviewRequestException(
                    "Theory question text is missing"
            );
        }

        if (q.getOptions() == null || q.getOptions().size() != 4) {
            throw new InvalidInterviewRequestException(
                    "Each theory question must have exactly 4 options"
            );
        }

        if (isBlank(q.getCorrectAnswer())) {
            throw new InvalidInterviewRequestException(
                    "Theory question missing correctAnswer"
            );
        }

        boolean answerExists = q.getOptions()
                .stream()
                .anyMatch(opt -> opt.equals(q.getCorrectAnswer()));

        if (!answerExists) {
            throw new InvalidInterviewRequestException(
                    "correctAnswer must match one of the options"
            );
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
