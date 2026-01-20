package com.andres.ai_study_tool.interview.store;

import com.andres.ai_study_tool.interview.dto.InterviewStartInternalResponse;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InterviewSessionStore {

    private final Map<String, InterviewStartInternalResponse> sessions =
            new ConcurrentHashMap<>();

    public void save(String interviewId, InterviewStartInternalResponse data) {

        if (interviewId == null || interviewId.trim().isEmpty()) {
            throw new IllegalArgumentException("Interview ID must not be null or empty");
        }

        if (data == null) {
            throw new IllegalArgumentException("Interview session data must not be null");
        }

        InterviewStartInternalResponse existing =
                sessions.putIfAbsent(interviewId, data);

        if (existing != null) {
            throw new IllegalStateException(
                    "Interview session already exists for ID: " + interviewId
            );
        }
    }

    public InterviewStartInternalResponse get(String interviewId) {

        if (interviewId == null || interviewId.trim().isEmpty()) {
            throw new IllegalArgumentException("Interview ID must not be null or empty");
        }

        InterviewStartInternalResponse data = sessions.get(interviewId);

        if (data == null) {
            throw new IllegalStateException(
                    "No interview session found for ID: " + interviewId
            );
        }

        return data;
    }

    public void remove(String interviewId) {

        if (interviewId == null || interviewId.trim().isEmpty()) {
            throw new IllegalArgumentException("Interview ID must not be null or empty");
        }

        sessions.remove(interviewId);
    }
}
