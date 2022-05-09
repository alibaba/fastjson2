package com.alibaba.fastjson2.filter;

public class PascalNameFilter implements NameFilter {
    @Override
    public String process(Object source, String name, Object value) {
        if (name == null || name.length() == 0) {
            return name;
        }

        char[] chars = name.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);

        return new String(chars);
    }
}
