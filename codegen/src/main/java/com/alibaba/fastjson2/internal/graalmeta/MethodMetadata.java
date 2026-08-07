package com.alibaba.fastjson2.internal.graalmeta;

public class MethodMetadata {
    /**
     * Method name that should be registered for this class when for methods
     * Method name that are queried for this class when for queriedMethods
     */
    private String name;
    /**
     * List of types for the parameters of the this method when for methods
     * List of methods to register for this class that are only looked up but not invoked when for queriedMethods
     */
    private String[] parameterTypes;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(String[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }
}
