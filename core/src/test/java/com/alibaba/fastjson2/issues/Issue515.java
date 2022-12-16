package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue515 {
    @Test
    public void test() {
        Element child0 = new Element();
        child0.setChildren(Collections.emptyList());
        Element child1 = new Element();
        child1.setChildren(Collections.emptyList());

        List<Element> children = new ArrayList<>();
        children.add(child0);
        children.add(child1);
        Element parent = new Element();
        parent.setChildren(children);

        List<Element> elements = new ArrayList<>();
        elements.add(parent);

        assertEquals("[{\"children\":[{\"children\":[]},{\"children\":[]}]}]", JSONArray.toJSONString(elements, JSONWriter.Feature.ReferenceDetection));
    }

    public static class Element {
        private List<Element> children;

        public List<Element> getChildren() {
            return this.children;
        }

        public void setChildren(List<Element> children) {
            this.children = children;
        }
    }
}
