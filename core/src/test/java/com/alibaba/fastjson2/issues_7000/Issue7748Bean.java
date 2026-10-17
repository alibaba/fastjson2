package com.alibaba.fastjson2.issues_7000;

/** Top-level so IsolatedClassLoader can define a second copy of this class. */
public class Issue7748Bean {
    private Generator generator;

    public Generator getGenerator() {
        return generator;
    }

    public void setGenerator(Generator generator) {
        this.generator = generator;
    }

    public static class Generator {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
