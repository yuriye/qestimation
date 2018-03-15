package com.ys.mfc;

import java.util.EventObject;

public class AnswerButtonPressedEvent extends EventObject {

    private String message;

    public AnswerButtonPressedEvent(Object source, String message) {
        super(source);
        this.message = message;
    }

    public AnswerButtonPressedEvent(Object source){
        this(source, "");
    }

    public AnswerButtonPressedEvent(String s){
        this(null, s);
    }

    public String getMessage(){
        return message;
    }

    @Override
    public String toString(){
        return getClass().getName() + "[source = " + getSource() + ", message = " + message + "]";
    }
}
