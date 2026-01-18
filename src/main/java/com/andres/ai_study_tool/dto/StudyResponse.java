package com.andres.ai_study_tool.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StudyResponse {

    private String definition;
    private List<String> whenToUse;
    private CodeExample example;
    private Complexity complexity;
    private List<String> commonMistakes;
    private List<String> interviewFollowUps;
    private List<QuizQuestion> quiz;

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public List<String> getWhenToUse() {
        return whenToUse;
    }

    public void setWhenToUse(List<String> whenToUse) {
        this.whenToUse = whenToUse;
    }

    public CodeExample getExample() {
        return example;
    }

    public void setExample(CodeExample example) {
        this.example = example;
    }

    public Complexity getComplexity() {
        return complexity;
    }

    public void setComplexity(Complexity complexity) {
        this.complexity = complexity;
    }

    public List<String> getCommonMistakes() {
        return commonMistakes;
    }

    public void setCommonMistakes(List<String> commonMistakes) {
        this.commonMistakes = commonMistakes;
    }

    public List<String> getInterviewFollowUps() {
        return interviewFollowUps;
    }

    public void setInterviewFollowUps(List<String> interviewFollowUps) {
        this.interviewFollowUps = interviewFollowUps;
    }

    public List<QuizQuestion> getQuiz() {
        return quiz;
    }

    public void setQuiz(List<QuizQuestion> quiz) {
        this.quiz = quiz;
    }
}

