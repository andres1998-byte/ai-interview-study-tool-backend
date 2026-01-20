package com.andres.ai_study_tool.interview.controller;

import com.andres.ai_study_tool.common.exception.InvalidInterviewRequestException;
import com.andres.ai_study_tool.interview.dto.*;
import com.andres.ai_study_tool.interview.service.CodeEvaluationService;
import com.andres.ai_study_tool.interview.service.InterviewService;
import com.andres.ai_study_tool.interview.service.TheoryScoringService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/interview")
public class InterviewController {

    private final InterviewService interviewService;
    private final TheoryScoringService theoryScoringService;
    private final CodeEvaluationService codeEvaluationService;

    public InterviewController(
            InterviewService interviewService,
            TheoryScoringService theoryScoringService,
            CodeEvaluationService codeEvaluationService) {
        this.interviewService = interviewService;
        this.theoryScoringService = theoryScoringService;
        this.codeEvaluationService = codeEvaluationService;
    }

    @PostMapping("/start")
    public InterviewStartResponse startInterview(
            @RequestBody InterviewStartRequest request
    ) {
        if (request == null) {
            throw new InvalidInterviewRequestException("Interview start request body must not be null");
        }

        if (isBlank(request.getTopic())) {
            throw new InvalidInterviewRequestException("Interview topic must not be empty");
        }

        if (isBlank(request.getLevel())) {
            throw new InvalidInterviewRequestException("Interview level must not be empty");
        }

        if (isBlank(request.getLanguage())) {
            throw new InvalidInterviewRequestException("Interview language must not be empty");
        }

        return interviewService.startInterview(
                request.getTopic().trim(),
                request.getLevel().trim(),
                request.getLanguage().trim()
        );
    }

    @PostMapping("/submit-theory")
    public ResponseEntity<TheoryResultResponse> submitTheory(
            @RequestBody TheoryAnswerSubmission submission
    ) {
        if (submission == null) {
            throw new InvalidInterviewRequestException("Theory submission body must not be null");
        }

        if (isBlank(submission.getInterviewId())) {
            throw new InvalidInterviewRequestException("Interview ID must not be empty");
        }

        if (submission.getAnswers() == null || submission.getAnswers().isEmpty()) {
            throw new InvalidInterviewRequestException("Theory answers must not be empty");
        }

        return ResponseEntity.ok(
                theoryScoringService.score(submission)
        );
    }

    @PostMapping("/submit-code")
    public ResponseEntity<CodeResultResponse> submitCode(
            @RequestBody CodeSubmissionRequest request
    ) {
        if (request == null) {
            throw new InvalidInterviewRequestException("Code submission body must not be null");
        }

        if (isBlank(request.getInterviewId())) {
            throw new InvalidInterviewRequestException("Interview ID must not be empty");
        }

        if (isBlank(request.getCode())) {
            throw new InvalidInterviewRequestException("Submitted code must not be empty");
        }

        return ResponseEntity.ok(
                codeEvaluationService.evaluate(
                        request.getInterviewId(),
                        request.getCode()
                )
        );
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
