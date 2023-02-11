package com.alibaba.fastjson2.diff.path;

public class Defects {
    private Object expect;

    private Object actual;

    private String indexPath;

    private String illustrate;

    public Object getExpect() {
        return expect;
    }

    public Defects setExpect(Object expect) {
        this.expect = expect;
        return this;
    }

    public Object getActual() {
        return actual;
    }

    public Defects setActual(Object actual) {
        this.actual = actual;
        return this;
    }

    public String getIndexPath() {
        return indexPath;
    }

    public Defects setIndexPath(String indexPath) {
        this.indexPath = indexPath;
        return this;
    }

    public String getIllustrate() {
        return illustrate;
    }

    public Defects setIllustrate(String illustrate) {
        this.illustrate = illustrate;
        return this;
    }
}
