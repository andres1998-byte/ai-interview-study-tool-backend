package com.andres.ai_study_tool.interview.dto;

import java.util.List;

public class InterviewStartInternalResponse {
    private List<TheoryQuestion> theoryQuestions;
    private CodingQuestion codingQuestion;

    public List<TheoryQuestion> getTheoryQuestions() {
        return theoryQuestions;
    }

    public void setTheoryQuestions(List<TheoryQuestion> theoryQuestions) {
        this.theoryQuestions = theoryQuestions;
    }

    public CodingQuestion getCodingQuestion() {
        return codingQuestion;
    }

    public void setCodingQuestion(CodingQuestion codingQuestion) {
        this.codingQuestion = codingQuestion;
    }
}

