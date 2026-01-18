package com.andres.ai_study_tool.interview.dto;

public class CodeSubmissionRequest {

    private String interviewId;
    private String code;

    public String getInterviewId() {
        return interviewId;
    }

    public void setInterviewId(String interviewId) {
        this.interviewId = interviewId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
