package com.alibaba.fastjson2.internal.processor.eishay;

import org.junit.jupiter.api.Test;

public class TypeTest {
    @Test
    public void test() {
        String fieldType = "java.util.List<java.lang.String>";
        String[] listTypes = {"java.util.List", "java.util.ArrayList"};

        String listType = null, itemType = null;
        for (String item : listTypes) {
            if (fieldType.startsWith(item)) {
                if (fieldType.charAt(item.length()) == '<') {
                    int lastIndex = fieldType.lastIndexOf('>');
                    String temp = fieldType.substring(item.length() + 1, lastIndex);
                    if (temp.indexOf(',') == -1) {
                        listType = item;
                        itemType = temp;
                    }
                    break;
                }
            }
        }

        if (itemType != null && itemType.startsWith("java.lang.")) {
            itemType = itemType.substring("java.lang.".length());
        }

        System.out.println(listType);
        System.out.println(itemType);
    }
}
