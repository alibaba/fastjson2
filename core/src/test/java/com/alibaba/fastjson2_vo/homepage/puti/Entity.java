package com.alibaba.fastjson2_vo.homepage.puti;

public abstract class Entity {
    protected Object mView;

    public Object getView() {
        return mView;
    }

    void setView(Object view) {
        this.mView = view;
    }
}
