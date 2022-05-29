package com.alibaba.fastjson2.v1issues.issue_1600;

class RegResponse<T> {
    private String retCode;
    private String retDesc;
    private T result;

    public String getRetCode() {
        return retCode;
    }

    public void setRetCode(String retCode) {
        this.retCode = retCode;
    }

    public String getRetDesc() {
        return retDesc;
    }

    public void setRetDesc(String retDesc) {
        this.retDesc = retDesc;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "RegResponse{" +
                "retCode='" + retCode + '\'' +
                ", retDesc='" + retDesc + '\'' +
                ", result=" + result +
                '}';
    }
}
