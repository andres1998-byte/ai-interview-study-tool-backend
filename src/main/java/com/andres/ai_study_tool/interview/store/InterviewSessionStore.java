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
        sessions.put(interviewId, data);
    }

    public InterviewStartInternalResponse get(String interviewId) {
        return sessions.get(interviewId);
    }

    public void remove(String interviewId) {
        sessions.remove(interviewId);
    }
}
