package com.ys.mfc;

import java.util.List;

public class MkguQuestionXmlIndicator {
    private String questionTitle;
    private String descriptionTitle;
    private String indicatorId;
    private List<MkguQuestionXmlQuestions> indicator;

    public MkguQuestionXmlIndicator() {
    }

    public String getQuestionTitle() {
        return this.questionTitle;
    }

    public void setQuestionTitle(String questionTitle) {
        this.questionTitle = questionTitle;
    }

    public String getDescriptionTitle() {
        return this.descriptionTitle;
    }

    public void setDescriptionTitle(String descriptionTitle) {
        this.descriptionTitle = descriptionTitle;
    }

    public String getIndicatorId() {
        return this.indicatorId;
    }

    public void setIndicatorId(String indicatorId) {
        this.indicatorId = indicatorId;
    }

    public List<MkguQuestionXmlQuestions> getIndicator() {
        return this.indicator;
    }

    public void setIndicator(List<MkguQuestionXmlQuestions> indicator) {
        this.indicator = indicator;
    }
}
