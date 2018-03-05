package com.ys.mfc;

import java.util.List;

public class MkguQuestionXmlRoot {
    private String questionTitle;
    private List<MkguQuestionXmlIndicator> indicator;
    private String version;
    private String orderNumber;

    public MkguQuestionXmlRoot() {
    }

    public String getOrderNumber() {
        return this.orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getQuestionTitle() {
        return this.questionTitle;
    }

    public void setQuestionTitle(String questionTitle) {
        this.questionTitle = questionTitle;
    }

    public List<MkguQuestionXmlIndicator> getIndicator() {
        return this.indicator;
    }

    public void setIndicator(List<MkguQuestionXmlIndicator> indicator) {
        this.indicator = indicator;
    }
}
