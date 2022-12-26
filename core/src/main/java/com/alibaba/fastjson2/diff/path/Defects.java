package com.alibaba.fastjson2.diff.path;


public class Defects {

    private Object expect;

    private Object actual;

    private String indexPath;

    private String illustrate;


    public Defects setExpect(Object expect) {
        this.expect = expect;
        return this;
    }

    public Defects setActual(Object actual) {
        this.actual = actual;
        return this;
    }

    public Defects setIndexPath(String indexPath) {
        this.indexPath = indexPath;
        return this;
    }

    public Defects setIllustrate(String illustrate) {
        this.illustrate = illustrate;
        return this;
    }

    public Object getExpect() {
        return expect;
    }

    public Object getActual() {
        return actual;
    }

    public String getIndexPath() {
        return indexPath;
    }

    public String getIllustrate() {
        return illustrate;
    }
}
