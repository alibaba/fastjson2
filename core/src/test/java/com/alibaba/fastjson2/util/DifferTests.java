package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;

public class DifferTests {
    @Test
    public void test() {
        assertTrue(Differ.diff(1, 1));
        assertTrue(Differ.diff("a", "a"));
        assertFalse(Differ.diff("abc", "aac"));
        assertTrue(Differ.diff(new Object[]{"a", 1}, new Object[]{"a", 1}));
        assertTrue(
                Differ.diff(
                        Collections.singletonMap("a", "101"),
                        Collections.singletonMap("a", "101")
                )
        );
        assertFalse(
                Differ.diff(
                        Collections.singletonMap("a", "101"),
                        Collections.singletonMap("b", "101")
                )
        );
        assertTrue(
                Differ.diff(
                        Collections.singleton("a"),
                        Collections.singleton("a")
                )
        );
        assertTrue(
                Differ.diff(
                        Collections.singletonList("a"),
                        Collections.singletonList("a")
                )
        );

        Differ differ = new Differ(1, 2);
        assertFalse(differ.diff());

        differ.setLeftName("left");
        assertEquals("left", differ.getLeftName());

        differ.setRightName("right");
        assertEquals("right", differ.getRightName());

        differ.setReferenceDetect(true);
        assertTrue(differ.isReferenceDetect());

        differ.setSkipTransient(true);
        assertTrue(differ.isSkipTransient());

        differ.setOut(null);
        assertNull(differ.getOut());

        differ.setComparator(null);
        assertNull(differ.getComparator());
    }

    @Test
    public void map() {
        assertFalse(
                Differ.diff(
                        Collections.singletonMap("a", 101),
                        Collections.emptyMap()
                )
        );

        assertFalse(
                Differ.diff(
                        JSONObject.of("a", 101),
                        JSONObject.of()
                )
        );
        assertFalse(
                Differ.diff(
                        JSONObject.of("a", 101).keySet(),
                        JSONObject.of().keySet()
                )
        );

        {
            HashSet set0 = new HashSet();
            HashSet set1 = new HashSet();
            set1.add("a");

            assertFalse(
                    Differ.diff(
                            set0,
                            set1
                    )
            );
        }
    }

    @Test
    public void testComparor() {
        {
            Differ differ = new Differ(Integer.toString(123), Integer.toString(123));
            differ.setComparator((a, b) -> 0);
            assertTrue(
                    differ.diff()
            );
        }
        {
            Differ differ = new Differ(Integer.toString(123), Integer.toString(123));
            differ.setComparator((a, b) -> 1);
            assertFalse(
                    differ.diff()
            );
        }
        {
            Differ differ = new Differ(new Object[0], new Object[0]);
            differ.setComparator((a, b) -> 1);
            assertFalse(
                    differ.diff()
            );
        }
        {
            LinkedHashMap map0 = new LinkedHashMap();
            LinkedHashMap map1 = new LinkedHashMap();
            Differ differ = new Differ(map0, map1);
            differ.setComparator((a, b) -> 1);
            assertFalse(
                    differ.diff()
            );
        }
        {
            LinkedHashMap map0 = new LinkedHashMap();
            LinkedHashMap map1 = new LinkedHashMap();
            Differ differ = new Differ(map0, map1);
            differ.setComparator((a, b) -> 0);
            assertTrue(
                    differ.diff()
            );
        }
        {
            Differ differ = new Differ(new Bean(0), new Bean(0));
            differ.setComparator((a, b) -> 0);
            assertTrue(
                    differ.diff()
            );
        }
        {
            Differ differ = new Differ(new Bean(0), new Bean(0));
            differ.setComparator(new Comparator() {
                @Override
                public int compare(Object o1, Object o2) {
                    if (o1 instanceof Integer) {
                        return ((Integer) o1).compareTo((Integer) o2);
                    }
                    return 1;
                }
            });
            assertFalse(
                    differ.diff()
            );
        }
    }

    @Test
    public void test1() {
        assertTrue(Differ.diff(new Bean(1), new Bean(1)));
        assertFalse(Differ.diff(new Bean(1), new Bean(2)));
    }

    public static class Bean {
        public int id;

        public Bean(int id) {
            this.id = id;
        }
    }

    @Test
    public void test2() {
        assertTrue(Differ.diff(new Bean2("a"), new Bean2("a")));
        assertFalse(Differ.diff(new Bean2("a"), new Bean2("b")));
        assertFalse(Differ.diff(new Bean2("ab"), new Bean2("b")));
        assertFalse(Differ.diff(new Bean2("ab"), new Bean2("ac")));
        assertFalse(Differ.diff(new Bean2("abc"), new Bean2(null)));
        assertFalse(Differ.diff(new Bean2(null), new Bean2("abc")));
        assertTrue(Differ.diff(new Bean2(null), new Bean2(null)));
    }

    public static class Bean2 {
        public Object value;

        public Bean2(Object value) {
            this.value = value;
        }
    }
}
