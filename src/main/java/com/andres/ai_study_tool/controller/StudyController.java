package com.andres.ai_study_tool.controller;


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
        return studyService.generate(request);
    }
}
