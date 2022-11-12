package com.fasterxml.jackson.core.type;

public abstract class ResolvedType {
    public abstract Class<?> getRawClass();

    public abstract boolean isContainerType();
}
