package com.andres.ai_study_tool.interview.dto;

import java.util.Map;

public class TheoryAnswerSubmission {

    private String interviewId;

    // questionId -> selected option
    private Map<Integer, String> answers;

    public String getInterviewId() {
        return interviewId;
    }

    public void setInterviewId(String interviewId) {
        this.interviewId = interviewId;
    }

    public Map<Integer, String> getAnswers() {
        return answers;
    }

    public void setAnswers(Map<Integer, String> answers) {
        this.answers = answers;
    }
}

