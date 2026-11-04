package com.alibaba.fastjson2.issues_7000;

import java.util.List;

/** Only a List field, so fieldClass isExternalClass does not already disable JIT. */
public class Issue7748ListBean {
    private List<Issue7748Bean.Generator> generators;

    public List<Issue7748Bean.Generator> getGenerators() {
        return generators;
    }

    public void setGenerators(List<Issue7748Bean.Generator> generators) {
        this.generators = generators;
    }
}
