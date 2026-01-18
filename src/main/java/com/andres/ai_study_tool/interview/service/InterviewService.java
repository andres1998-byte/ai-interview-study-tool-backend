package com.andres.ai_study_tool.interview.service;

import com.andres.ai_study_tool.common.exception.InvalidInterviewRequestException;
import com.andres.ai_study_tool.common.util.JsonGuard;
import com.andres.ai_study_tool.interview.dto.*;
import com.andres.ai_study_tool.interview.store.InterviewSessionStore;
import com.andres.ai_study_tool.interview.validation.InterviewSanityValidator;
import com.andres.ai_study_tool.llm.LlmClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class InterviewService {

    private final LlmClient llmClient;
    private final ObjectMapper objectMapper;

    private final InterviewSessionStore sessionStore;

    public InterviewService(LlmClient llmClient, ObjectMapper objectMapper, InterviewSessionStore sessionStore) {
        this.llmClient = llmClient;
        this.objectMapper = objectMapper;
        this.sessionStore = sessionStore;
    }

    public InterviewStartResponse startInterview(String topic, String level, String language) {

        String systemPrompt = """
You are a strict software engineering interviewer.

Generate:
- EXACTLY 5 multiple-choice theory questions
- EXACTLY 1 coding question

Rules:
- Questions must match the given topic and experience level
- Each theory question must have exactly 4 options
- Only ONE option is correct
- Coding question must be realistic for interviews
- Return ONLY valid JSON
- No explanations outside JSON

SCHEMA:
{
  "theoryQuestions": [
    {
      "id": number,
      "question": "string",
      "options": ["string"],
      "correctAnswer": "string"
    }
  ],
  "codingQuestion": {
    "prompt": "string",
    "methodSignature": "string"
  }
}
""";

        String retrySystemPrompt = """
Your previous response was INVALID.

STRICT CORRECTION REQUIRED:
- ALL questions must be DIRECTLY about the topic
- Do NOT generalize
- Do NOT invent unrelated concepts
- Follow schema EXACTLY
- Return ONLY valid JSON
""";

        String userPrompt = String.format("""
Topic: %s
Language: %s
Level: %s
""", topic, language, level);

        try {
            // ---- FIRST ATTEMPT
            InterviewStartInternalResponse internal =
                    generateAndValidate(systemPrompt, userPrompt, topic);

            return toPublicResponse(internal);

        } catch (InvalidInterviewRequestException firstFailure) {

            // ---- ONE RETRY
            InterviewStartInternalResponse retryInternal =
                    generateAndValidate(retrySystemPrompt, userPrompt, topic);

            return toPublicResponse(retryInternal);
        }
    }

    // 🔁 Core generation + validation
    private InterviewStartInternalResponse generateAndValidate(
            String systemPrompt,
            String userPrompt,
            String topic
    ) {
        try {
            String raw = llmClient.generate(systemPrompt, userPrompt);
            String json = JsonGuard.extractJsonObject(raw);

            InterviewStartInternalResponse internal =
                    objectMapper.readValue(json, InterviewStartInternalResponse.class);

            InterviewSanityValidator.validateAll(topic, internal);

            return internal;

        } catch (InvalidInterviewRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("LLM generation failed", e);
        }
    }

    // 🔐 Strip answers before returning to client
    private InterviewStartResponse toPublicResponse(InterviewStartInternalResponse internal) {

        InterviewStartResponse response = new InterviewStartResponse();
        response.setInterviewId(UUID.randomUUID().toString());

        response.setTheoryQuestions(
                internal.getTheoryQuestions().stream()
                        .map(q -> {
                            TheoryQuestionPublic pub = new TheoryQuestionPublic();
                            pub.setId(q.getId());
                            pub.setQuestion(q.getQuestion());
                            pub.setOptions(q.getOptions());
                            return pub;
                        })
                        .toList()
        );

        response.setCodingQuestion(internal.getCodingQuestion());
        sessionStore.save(response.getInterviewId(), internal);
        return response;
    }
}
