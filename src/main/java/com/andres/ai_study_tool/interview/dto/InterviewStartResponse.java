package com.andres.ai_study_tool.interview.dto;

import java.util.List;

public class InterviewStartResponse {

    private String interviewId;
    private List<TheoryQuestionPublic> theoryQuestions;
    private CodingQuestion codingQuestion;

    public String getInterviewId() {
        return interviewId;
    }

    public void setInterviewId(String interviewId) {
        this.interviewId = interviewId;
    }

    public List<TheoryQuestionPublic> getTheoryQuestions() {
        return theoryQuestions;
    }

    public void setTheoryQuestions(List<TheoryQuestionPublic> theoryQuestions) {
        this.theoryQuestions = theoryQuestions;
    }

    public CodingQuestion getCodingQuestion() {
        return codingQuestion;
    }

    public void setCodingQuestion(CodingQuestion codingQuestion) {
        this.codingQuestion = codingQuestion;
    }
}

