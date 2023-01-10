package org.trotiletre.common;

public enum AnswerTag {
    ANSWER(0), NOTIFICATION(1);
    public int tag;
    AnswerTag(int i){
        this.tag = i;
    }
}
