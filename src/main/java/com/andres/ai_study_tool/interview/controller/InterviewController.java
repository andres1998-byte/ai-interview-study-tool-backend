package com.andres.ai_study_tool.interview.controller;

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
        return interviewService.startInterview(
                request.getTopic(),
                request.getLevel(),
                request.getLanguage()
        );
    }

    @PostMapping("/submit-theory")
    public ResponseEntity<TheoryResultResponse> submitTheory(
            @RequestBody TheoryAnswerSubmission submission
    ) {
        return ResponseEntity.ok(
                theoryScoringService.score(submission)
        );
    }

    @PostMapping("/submit-code")
    public ResponseEntity<CodeResultResponse> submitCode(
            @RequestBody CodeSubmissionRequest request
    ) {
        return ResponseEntity.ok(
                codeEvaluationService.evaluate(
                        request.getInterviewId(),
                        request.getCode()
                )
        );
    }


}

