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

You are generating a Java interview session.

Topic: "{topic}"
Level: "{level}"

CRITICAL REQUIREMENTS:
0) HUMAN INPUT TOLERANCE (VERY IMPORTANT):

- The topic may contain:
  - pluralization (e.g. "lists", "trees", "li isssts")
  - repeated letters (e.g. "listsss", "haashmap", "hash  maP")
  - minor spelling mistakes (e.g. "araylist", "hasmap")
  - casing differences (e.g. "LIST", "list")

- You MUST infer the most likely intended technical topic
  and generate questions about that intended topic.

- Example:
  - Input topic: "listsss"
  - Intended topic: "List"
  - You must generate questions about "List", NOT about the literal string "listsss".

- Example:
  - Input topic: "araylistssssss"
  - Intended topic: "ArrayList"
  - You must generate questions about "ArrayList".

1) ALL 5 theory questions MUST be strictly and directly about the topic "{topic}".
2) Do NOT include generic Java, OOP, JVM, GC, threading, SQL, or REST questions
   unless they are directly and specifically required to understand "{topic}".
3) Each theory question must clearly reference or depend on "{topic}" or a core
   concept of "{topic}".
4) If you cannot generate 5 valid topic-specific theory questions about "{topic}",
   you MUST instead return:

   {
     "error": "INVALID_TOPIC",
     "message": "Cannot generate enough topic-specific interview questions."
   }

5) Before returning your JSON, internally verify:
   - At least 4 of the 5 theory questions are clearly and directly about "{topic}".
   - If not, regenerate them.

STRUCTURE RULES:

- Generate EXACTLY 5 multiple-choice theory questions
- Generate EXACTLY 1 coding question
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
        // Include schema again (important) so the retry doesn't drift.
        String retrySystemPrompt = """
Your previous response was INVALID.

STRICT CORRECTION REQUIRED.

Topic: "{topic}"
Level: "{level}"

FAILURE REASON:
- The generated questions were not sufficiently related to the topic.

CRITICAL REQUIREMENTS:

1) ALL 5 theory questions MUST be strictly and directly about the topic "{topic}".
2) Do NOT include any generic Java, OOP, JVM, GC, threading, SQL, or REST questions.
3) Each theory question must explicitly reference "{topic}" or a core concept
   of "{topic}".
4) If you cannot generate 5 valid topic-specific theory questions about "{topic}",
   you MUST return:

   {
     "error": "INVALID_TOPIC",
     "message": "Cannot generate enough topic-specific interview questions."
   }

5) Before returning your JSON, internally verify:
   - At least 4 of the 5 theory questions are clearly and directly about "{topic}".
   - If not, regenerate them.

STRUCTURE RULES:

- Generate EXACTLY 5 multiple-choice theory questions
- Generate EXACTLY 1 coding question
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


        String safeTopic = topic == null ? "" : topic.trim();
        String safeLevel = level == null ? "" : level.trim();
        String safeLanguage = language == null ? "" : language.trim();

        String userPrompt = String.format("""
Topic: %s
Language: %s
Level: %s
""", safeTopic, safeLanguage, safeLevel);

        try {
            InterviewStartInternalResponse internal =
                    generateAndValidate(systemPrompt, userPrompt, safeTopic);

            return toPublicResponse(internal);

        } catch (InvalidInterviewRequestException firstFailure) {

            InterviewStartInternalResponse retryInternal =
                    generateAndValidate(retrySystemPrompt, userPrompt, safeTopic);

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

            // JsonGuard throws IllegalStateException -> treat as invalid output (retryable)
            String json = JsonGuard.extractJsonObject(raw);

            InterviewStartInternalResponse internal =
                    objectMapper.readValue(json, InterviewStartInternalResponse.class);

            // Sanity validator throws InvalidInterviewRequestException -> retryable
            InterviewSanityValidator.validateAll(topic, internal);

            // Extra guard to prevent NPEs later
            if (internal.getTheoryQuestions() == null || internal.getTheoryQuestions().isEmpty()) {
                throw new InvalidInterviewRequestException("LLM returned no theory questions");
            }
            if (internal.getCodingQuestion() == null) {
                throw new InvalidInterviewRequestException("LLM returned no coding question");
            }

            return internal;

        } catch (InvalidInterviewRequestException e) {
            throw e;

        } catch (IllegalStateException e) {
            // JsonGuard / missing JSON etc -> retryable
            throw new InvalidInterviewRequestException(e.getMessage());

        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            // Parsing failed -> retryable
            throw new InvalidInterviewRequestException("LLM returned invalid JSON");

        } catch (Exception e) {
            // True unexpected failures (network, etc.)
            throw new RuntimeException("LLM generation failed", e);
        }
    }


    // 🔐 Strip answers before returning to client
    private InterviewStartResponse toPublicResponse(InterviewStartInternalResponse internal) {

        if (internal == null) {
            throw new IllegalStateException("Interview generation returned null internal response");
        }
        if (internal.getTheoryQuestions() == null || internal.getTheoryQuestions().isEmpty()) {
            throw new IllegalStateException("Interview generation returned no theory questions");
        }
        if (internal.getCodingQuestion() == null) {
            throw new IllegalStateException("Interview generation returned no coding question");
        }

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
