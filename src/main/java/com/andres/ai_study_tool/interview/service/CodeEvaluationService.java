package com.andres.ai_study_tool.interview.service;

import com.andres.ai_study_tool.common.exception.InvalidInterviewRequestException;
import com.andres.ai_study_tool.common.util.JsonGuard;
import com.andres.ai_study_tool.interview.dto.CodeResultResponse;
import com.andres.ai_study_tool.interview.dto.InterviewStartInternalResponse;
import com.andres.ai_study_tool.interview.store.InterviewSessionStore;
import com.andres.ai_study_tool.llm.LlmClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class CodeEvaluationService {

    private final InterviewSessionStore sessionStore;
    private final LlmClient llmClient;
    private final ObjectMapper objectMapper;

    public CodeEvaluationService(
            InterviewSessionStore sessionStore,
            LlmClient llmClient,
            ObjectMapper objectMapper
    ) {
        this.sessionStore = sessionStore;
        this.llmClient = llmClient;
        this.objectMapper = objectMapper;
    }

    public CodeResultResponse evaluate(String interviewId, String code) {

        InterviewStartInternalResponse session =
                sessionStore.get(interviewId);

        if (session == null) {
            throw new InvalidInterviewRequestException("Invalid interviewId");
        }

        String systemPrompt = """
You are a strict software engineering interviewer.

Evaluate the candidate's solution.

Rules:
- Do NOT execute code
- Judge correctness, edge cases, and style
- Be realistic and fair
- Return ONLY valid JSON

SCHEMA:
{
  "passed": boolean,
  "score": number,
  "feedback": "string"
}
""";

        String userPrompt = String.format("""
QUESTION:
%s

METHOD SIGNATURE:
%s

CANDIDATE CODE:
%s
""",
                session.getCodingQuestion().getPrompt(),
                session.getCodingQuestion().getMethodSignature(),
                code
        );

        try {
            String raw = llmClient.generate(systemPrompt, userPrompt);
            String json = JsonGuard.extractJsonObject(raw);
            return objectMapper.readValue(json, CodeResultResponse.class);

        } catch (Exception e) {
            throw new RuntimeException("Failed to evaluate code", e);
        }
    }
}

