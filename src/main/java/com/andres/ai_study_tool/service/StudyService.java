package com.andres.ai_study_tool.service;

import com.andres.ai_study_tool.dto.StudyRequest;
import com.andres.ai_study_tool.dto.StudyResponse;
import com.andres.ai_study_tool.llm.LlmClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class StudyService {

    private final LlmClient llmClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public StudyService(LlmClient llmClient) {
        this.llmClient = llmClient;
    }

    @Cacheable(
            value = "study-cache",
            key = "#request.topic + '-' + #request.level + '-' + #request.language"
    )
    public StudyResponse generate(StudyRequest request) {


        String systemPrompt = """
If the topic is not a real, well-known concept in software engineering or computer science,
return the following JSON EXACTLY and do not fabricate content:

If the topic is not a real, well-known concept in software engineering or computer science,
return the following JSON EXACTLY and do not fabricate content:

{
  "definition": "Invalid topic",
  "whenToUse": [],
  "example": {
    "code": "",
    "explanation": ""
  },
  "complexity": {
    "average": "",
    "worst": ""
  },
  "commonMistakes": [],
  "interviewFollowUps": [],
  "quiz": []
}

But if the person writing the topic writes it a bit wrong then infer the topic a little bit.

Make sure to make it as good and detailed as possible.



You are an expert software engineering interviewer.

You MUST return ONLY valid JSON.
You MUST follow the EXACT schema provided.
You MUST NOT add, remove, or rename fields.
You MUST NOT include explanations or extra text.
You MUST NOT wrap the JSON in markdown or backticks.

If a field is not applicable, return an empty string or empty array.

SCHEMA (return EXACTLY this structure):

{
  "definition": "string",
  "whenToUse": ["string"],
  "example": {
    "code": "string",
    "explanation": "string"
  },
  "complexity": {
    "average": "string",
    "worst": "string"
  },
  "commonMistakes": ["string"],
  "interviewFollowUps": ["string"],
  "quiz": [
    {
      "question": "string",
      "options": ["string"],
      "correctAnswer": "string"
    }
  ]
}
""";


        String userPrompt = String.format("""
Generate study material for the following:

Topic: %s
Language: %s
Experience Level: %s

If the topic is ambiguous:
- Pick the MOST COMMON interpretation for interviews
- Briefly mention other possible meanings in the definition
- Then continue normally

Populate the schema fully with senior-level depth.
Return ONLY the JSON.
""",
                request.getTopic(),
                request.getLanguage(),
                request.getLevel()
        );


        try {
            String rawResponse = llmClient.generate(systemPrompt, userPrompt);
            System.out.println("===== RAW LLM RESPONSE =====");
            System.out.println(rawResponse);
            System.out.println("============================");
            return objectMapper.readValue(rawResponse, StudyResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate study material", e);
        }
    }
}
