package com.alibaba.fastjson2.internal.graalmeta;

public class TypeReachableMetadata {
    /**
     * Fully qualified class name of the class that must be reachable in order to register the class <name> for reflection
     */
    private String typeReachable;

    public String getTypeReachable() {
        return typeReachable;
    }

    public void setTypeReachable(String typeReachable) {
        this.typeReachable = typeReachable;
    }
}
