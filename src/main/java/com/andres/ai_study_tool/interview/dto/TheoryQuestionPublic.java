package com.andres.ai_study_tool.interview.dto;

import java.util.List;

public class TheoryQuestionPublic {

    private int id;
    private String question;
    private List<String> options;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }
}

