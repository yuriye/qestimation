package com.ys.mfc;

public class MkguQuestionXmlQuestions {
    private String questionValue;
    private String questionText;
    private String altTitle;

    public MkguQuestionXmlQuestions(String questionValue, String questionText, String altTitle) {
        this.questionValue = questionValue;
        this.questionText = questionText;
        this.altTitle = altTitle;
    }

    public String getQuestionValue() {
        return this.questionValue;
    }

    public void setQuestionValue(String questionValue) {
        this.questionValue = questionValue;
    }

    public String getQuestionText() {
        return this.questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getAltTitle() {
        return this.altTitle;
    }

    public void setAltTitle(String altTitle) {
        this.altTitle = altTitle;
    }

    @Override
    public String toString() {
        return "MkguQuestionXmlQuestions{" +
                "questionValue='" + questionValue + '\'' +
                ", questionText='" + questionText + '\'' +
                ", altTitle='" + altTitle + '\'' +
                '}';
    }
}
