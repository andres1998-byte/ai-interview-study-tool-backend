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

        if (interviewId == null || interviewId.trim().isEmpty()) {
            throw new InvalidInterviewRequestException("Interview ID must not be empty");
        }

        if (code == null || code.trim().isEmpty()) {
            throw new InvalidInterviewRequestException("Submitted code must not be empty");
        }

        InterviewStartInternalResponse session =
                sessionStore.get(interviewId);

        if (session.getCodingQuestion() == null) {
            throw new IllegalStateException("Interview session has no coding question");
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
                code.trim()
        );

        try {
            String raw = llmClient.generate(systemPrompt, userPrompt);

            String json = JsonGuard.extractJsonObject(raw);

            CodeResultResponse result =
                    objectMapper.readValue(json, CodeResultResponse.class);

            // ---- Defensive sanity checks on LLM output
            if (result.getFeedback() == null || result.getFeedback().isBlank()) {
                throw new InvalidInterviewRequestException("LLM returned empty feedback");
            }

            if (result.getScore() < 0 || result.getScore() > 100) {
                throw new InvalidInterviewRequestException(
                        "LLM returned invalid score: " + result.getScore()
                );
            }

            return result;

        } catch (InvalidInterviewRequestException e) {
            throw e;

        } catch (IllegalStateException e) {
            // JsonGuard failure, missing JSON, etc → deterministic client error
            throw new InvalidInterviewRequestException(e.getMessage());

        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new InvalidInterviewRequestException("LLM returned invalid JSON");

        } catch (Exception e) {
            throw new RuntimeException("Failed to evaluate code", e);
        }
    }

}

