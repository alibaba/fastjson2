package com.alibaba.fastjson2.features;

/**
 * Plain bean loaded twice (parent loader and an isolated child loader) to witness
 * that ObjectWriterProvider.cleanup(ClassLoader) fully releases the child loader.
 */
public class LeakBean {
    public int a = 1;
    public int z = 2;
}
