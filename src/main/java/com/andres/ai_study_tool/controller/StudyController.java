package com.andres.ai_study_tool.controller;


import com.andres.ai_study_tool.common.exception.InvalidInterviewRequestException;
import com.andres.ai_study_tool.dto.StudyRequest;
import com.andres.ai_study_tool.dto.StudyResponse;
import com.andres.ai_study_tool.service.StudyService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/study")
public class StudyController {

    private final StudyService studyService;

    public StudyController(StudyService studyService) {
        this.studyService = studyService;
    }

    @PostMapping("/generate")
    public StudyResponse generate(@RequestBody StudyRequest request) {

        if (request == null) {
            throw new InvalidInterviewRequestException("Study request body must not be null");
        }

        if (request.getTopic() == null || request.getTopic().isBlank()) {
            throw new InvalidInterviewRequestException("Study topic must not be empty");
        }

        if (request.getLevel() == null || request.getLevel().isBlank()) {
            throw new InvalidInterviewRequestException("Study level must not be empty");
        }

        return studyService.generate(request);
    }

}
