package com.alibaba.fastjson2.diff.mock;

public class MetaData {

    private Object expect;


    private Object actual;

    public Object getExpect() {
        return expect;
    }

    public void setExpect(Object expect) {
        this.expect = expect;
    }

    public Object getActual() {
        return actual;
    }

    public void setActual(Object actual) {
        this.actual = actual;
    }
}
