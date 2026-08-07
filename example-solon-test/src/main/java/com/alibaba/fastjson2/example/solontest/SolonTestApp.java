package com.alibaba.fastjson2.example.solontest;

import org.noear.solon.Solon;
import org.noear.solon.annotation.SolonMain;

@SolonMain
public class SolonTestApp {
    public static void main(String[] args) {
        Solon.start(SolonTestApp.class, args);
    }
}
