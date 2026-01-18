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

        InterviewStartInternalResponse interview =
                sessionStore.get(submission.getInterviewId());

        if (interview == null) {
            throw new InvalidInterviewRequestException("Interview session not found");
        }

        Map<Integer, String> submittedAnswers = submission.getAnswers();

        // 🔍 DEBUG: verify answers BEFORE scoring
        interview.getTheoryQuestions().forEach(q -> {
            System.out.println(
                    "Q" + q.getId()
                            + " | Correct: " + q.getCorrectAnswer()
                            + " | Submitted: " + submission.getAnswers().get(q.getId())
            );
        });

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
            qr.setQuestionText(q.getQuestion());          // ✅ question text
            qr.setUserAnswer(submitted);                  // ✅ what user chose
            qr.setCorrectAnswer(q.getCorrectAnswer());    // ✅ expected answer
            qr.setCorrect(correct);

            results.add(qr);


            // 🔍 Debug
            System.out.println(
                    "Q" + q.getId()
                            + " | Correct: [" + q.getCorrectAnswer() + "]"
                            + " | Submitted: [" + submitted + "]"
                            + " | Match = " + correct
            );
        }


        TheoryResultResponse response = new TheoryResultResponse();
        response.setTotalQuestions(total);
        response.setCorrectAnswers(correctCount);
        response.setScorePercentage(
                (int) Math.round((correctCount * 100.0) / total)
        );

        response.setResults(results);

        System.out.println("===== THEORY SCORING SUMMARY =====");
        System.out.println("Interview ID: " + submission.getInterviewId());
        System.out.println("Total questions: " + total);
        System.out.println("Correct answers: " + correctCount);

        double rawScore = (correctCount * 100.0) / total;
        System.out.println("Raw percentage (double): " + rawScore);

        int finalScore = (int) Math.round(rawScore);
        System.out.println("Final rounded score: " + finalScore);
        System.out.println("=================================");


        return response;
    }
}
