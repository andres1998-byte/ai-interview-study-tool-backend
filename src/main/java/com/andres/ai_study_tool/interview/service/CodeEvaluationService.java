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
You are a strict but fair software engineering interviewer.

You are evaluating a candidate's Java method implementation.

CRITICAL EVALUATION RULES:

1) Do NOT execute code. Reason about it statically.

2) Primary goal: correctness.
   - If the logic is fundamentally incorrect, the solution MUST fail.
   - If the logic is mostly correct but misses minor edge cases,
     the solution may still pass with a lower score.

3) Edge cases:
   - Consider null inputs, empty inputs, boundary values, and off-by-one errors
     if they are relevant to the problem.
   - Missing important edge cases should lower the score.

4) Style and code quality:
   - Judge readability, naming, structure, and simplicity.
   - Style issues should only slightly affect the score.
   - Style alone must NEVER cause a correct solution to fail.

5) Passing criteria:
   - passed = true ONLY if the solution is logically correct
     for the core problem.
   - passed = false if the core logic is wrong or incomplete.

6) Scoring rules (0–100):
   - Start from 100 points.
   - Subtract points for:
     - incorrect logic
     - missing edge cases
     - inefficiencies
     - poor readability or structure
   - A perfect, clean solution should score 90–100.
   - A mostly correct solution with minor issues: 70–89.
   - A partially correct solution: 40–69.
   - A fundamentally incorrect solution: 0–39.

7) Feedback rules:
   - Feedback must be concise, specific, and actionable.
   - Mention:
     - whether the logic is correct or incorrect
     - which edge cases are missing (if any)
     - any major style or structure issues
   - Do NOT praise excessively.
   - Do NOT be harsh or insulting.

OUTPUT RULES:

- Return ONLY valid JSON.
- No explanations outside JSON.
- Use the exact schema below.

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

