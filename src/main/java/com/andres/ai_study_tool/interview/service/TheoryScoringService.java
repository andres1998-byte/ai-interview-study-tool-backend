package com.andres.ai_study_tool.interview.service;

import com.andres.ai_study_tool.common.exception.InvalidInterviewRequestException;
import com.andres.ai_study_tool.interview.dto.*;
import com.andres.ai_study_tool.interview.store.InterviewSessionStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class TheoryScoringService {

    private final InterviewSessionStore sessionStore;

    public TheoryScoringService(InterviewSessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    public TheoryResultResponse score(TheoryAnswerSubmission submission) {

        if (submission == null) {
            throw new InvalidInterviewRequestException("Theory submission body must not be null");
        }

        if (submission.getInterviewId() == null || submission.getInterviewId().trim().isEmpty()) {
            throw new InvalidInterviewRequestException("Interview ID must not be empty");
        }

        if (submission.getAnswers() == null || submission.getAnswers().isEmpty()) {
            throw new InvalidInterviewRequestException("Theory answers must not be empty");
        }

        InterviewStartInternalResponse interview =
                sessionStore.get(submission.getInterviewId());

        if (interview.getTheoryQuestions() == null || interview.getTheoryQuestions().isEmpty()) {
            throw new IllegalStateException("Interview contains no theory questions");
        }

        Map<Integer, String> submittedAnswers = submission.getAnswers();

        int total = interview.getTheoryQuestions().size();
        int correctCount = 0;

        List<TheoryResultResponse.QuestionResult> results = new ArrayList<>();

        for (TheoryQuestion q : interview.getTheoryQuestions()) {

            String submitted = submittedAnswers.get(q.getId());

            boolean correct = submitted != null &&
                    submitted.trim().equalsIgnoreCase(q.getCorrectAnswer().trim());

            if (correct) {
                correctCount++;
            }

            TheoryResultResponse.QuestionResult qr =
                    new TheoryResultResponse.QuestionResult();

            qr.setQuestionId(q.getId());
            qr.setQuestionText(q.getQuestion());
            qr.setUserAnswer(submitted);
            qr.setCorrectAnswer(q.getCorrectAnswer());
            qr.setCorrect(correct);

            results.add(qr);
        }

        TheoryResultResponse response = new TheoryResultResponse();
        response.setTotalQuestions(total);
        response.setCorrectAnswers(correctCount);
        response.setScorePercentage(
                (int) Math.round((correctCount * 100.0) / total)
        );

        response.setResults(results);

        return response;
    }

}
