package com.andres.ai_study_tool.interview.dto;

public class CodingQuestion {

    private String prompt;
    private String methodSignature;

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getMethodSignature() {
        return methodSignature;
    }

    public void setMethodSignature(String methodSignature) {
        this.methodSignature = methodSignature;
    }
}
