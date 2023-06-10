package com.alibaba.fastjson2.features;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReferenceDetectionTest {
    @Test
    public void test() {
        Path p = new Path("p");
        Path p1 = new Path("p1", p);
        p.next = p1;
        Path p2 = new Path("p2", p1);
        p2.root = p;
        p1.next = p2;
        String str = JSON.toJSONString(p, JSONWriter.Feature.ReferenceDetection);
        assertEquals(
                "{\"name\":\"p\",\"next\":{\"name\":\"p1\",\"next\":{\"name\":\"p2\",\"parent\":{\"$ref\":\"$.next\"},\"root\":{\"$ref\":\"$\"}},\"parent\":{\"$ref\":\"$\"}}}",
                str
        );

        Path px = JSON.parseObject(str, Path.class);
        assertEquals(str, JSON.toJSONString(px, JSONWriter.Feature.ReferenceDetection));
    }

    public static class Path {
        public String name;
        public Path root;
        public Path parent;
        public Path next;

        public Path() {
        }

        public Path(String name) {
            this.name = name;
        }

        public Path(String name, Path parent) {
            this.name = name;
            this.parent = parent;
        }
    }
}
