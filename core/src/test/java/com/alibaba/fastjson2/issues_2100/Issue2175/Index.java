package com.alibaba.fastjson2.issues_2100.Issue2175;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TypeReference;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author 张治保
 * @since 2024/1/11
 */
public class Index {
    @Getter
    @Setter
    @ToString
    @EqualsAndHashCode
    public static class TestER {
        private String name;
    }

    /**
     * issue #2175
     */
    @Test
    @SuppressWarnings("unchecked")
    void test() {
        TestER te = new TestER();
        te.setName("ER");
        List<TestER[]> list1 = new ArrayList<>();
        list1.add(new TestER[]{te, te});
        list1.add(new TestER[]{te, te});
        byte[] by1 = JSON.toJSONBytes(list1, JSONWriter.Feature.WriteClassName);
        System.out.println(new String(by1));
        List<TestER[]> result = JSON.parseObject(by1, list1.getClass(), JSONReader.autoTypeFilter(
                TestER.class.getName()
        ));
        assertEquals(result.size(), 2);
//
//        TestER[] first = result.get(0);
//        assertEquals(first.length, 2);
//        assertArrayEquals(result.get(0), new TestER[]{te, te});
//
//        TestER[] second = result.get(1);
//        assertEquals(second.length, 2);
//        assertArrayEquals(second, new TestER[]{te, te});
    }

    @Test
    public void toList() {
        TestER te = new TestER();
        te.setName("ER");
        List<TestER[]> list1 = new ArrayList<>();
        list1.add(new TestER[]{te, te});
        list1.add(new TestER[]{te, te});

        String ers1 = JSON.toJSONString(list1, JSONWriter.Feature.WriteClassName);
        //正确
        List<TestER[]> ers1_1 = JSON.parseObject(ers1, new TypeReference<List<TestER[]>>() {
        });
        //有问题 todo
        List<List<TestER>> ers1_2 = JSON.parseObject(ers1, new TypeReference<List<List<TestER>>>() {
        });
        //正确
        List<Object[]> ers1_3 = JSON.parseObject(ers1, new TypeReference<List<Object[]>>() {
        });

        List<List<TestER>> list2 = new ArrayList<>();
        list2.add(new ArrayList<TestER>() {{
                add(te);
                add(te);
                }
            }
        );
        list2.add(new ArrayList<TestER>() {{
                add(te);
                add(te);
                }
            }
        );

        String ers2 = JSON.toJSONString(list2, JSONWriter.Feature.WriteClassName);
        //正确
        List<TestER[]> ers2_1 = JSON.parseObject(ers2, new TypeReference<List<TestER[]>>() {
        });
        //正确
        List<List<TestER>> ers2_2 = JSON.parseObject(ers2, new TypeReference<List<List<TestER>>>() {
        });
        //正确
        List<Object[]> ers2_3 = JSON.parseObject(ers2, new TypeReference<List<Object[]>>() {
        });
        System.out.println(1);
    }

    /**
     * object array  multiple type
     */
    @Test
    @SuppressWarnings("unchecked")
    void testObjectArray() {
        TestER te = new TestER();
        te.setName("ER");
        List<Object[]> list1 = new ArrayList<>();
        list1.add(new Object[]{te, 0});
        list1.add(new Object[]{te, Boolean.FALSE});
        byte[] by1 = JSON.toJSONBytes(list1, JSONWriter.Feature.WriteClassName);
        System.out.println(new String(by1));
        List<Object[]> result = JSON.parseObject(by1, list1.getClass(), JSONReader.autoTypeFilter(
                Object.class.getName(),
                TestER.class.getName()
        ));
        assertEquals(result.size(), 2);
//
//        Object[] first = result.get(0);
//        assertEquals(first.length, 2);
//        assertArrayEquals(result.get(0), new Object[]{te, 0});
//
//        Object[] second = result.get(1);
//        assertEquals(second.length, 2);
//        assertArrayEquals(second, new Object[]{te, Boolean.FALSE});
    }

    @Getter
    @Setter
    @ToString
    @EqualsAndHashCode(callSuper = true)
    public static class Children
            extends TestER {
        private Integer age;
    }

    /**
     * polymorphism array
     */
    @Test
    @SuppressWarnings("unchecked")
    void testPolymorphism() {
        TestER te = new TestER();
        te.setName("parent");
        Children children = new Children();
        children.setName("child");
        children.setAge(1);

        List<TestER[]> list1 = new ArrayList<>();
        list1.add(new TestER[]{te, children});
        list1.add(new TestER[]{children, te});
        byte[] by1 = JSON.toJSONBytes(list1, JSONWriter.Feature.WriteClassName);
        System.out.println(new String(by1));
        List<TestER[]> result = JSON.parseObject(by1, list1.getClass(), JSONReader.autoTypeFilter(
                TestER.class.getName(),
                Children.class.getName()
        ));

        assertEquals(result.size(), 2);
//
//        Object[] first = result.get(0);
//        assertEquals(first.length, 2);
//        assertArrayEquals(result.get(0), new TestER[]{te, children});
//
//        Object[] second = result.get(1);
//        assertEquals(second.length, 2);
//        assertArrayEquals(second, new TestER[]{children, te});
    }

    /**
     * two dimensional array
     */
    @Test
    void testTwoDimensionalArray() {
        TestER te = new TestER();
        te.setName("parent");
        Children children = new Children();
        children.setName("child");
        children.setAge(1);

        TestER[][] list1 = new TestER[][]{
                {te, children},
                {children, te}
        };
        byte[] by1 = JSON.toJSONBytes(list1, JSONWriter.Feature.WriteClassName);
        System.out.println(new String(by1));
        TestER[][] result = JSON.parseObject(by1, list1.getClass(), JSONReader.autoTypeFilter(
                TestER.class.getName(),
                Children.class.getName()
        ));

        assertEquals(result.length, 2);

        TestER[] first = result[0];
        assertEquals(first.length, 2);
        assertArrayEquals(result[0], new TestER[]{te, children});

        TestER[] second = result[1];
        assertEquals(second.length, 2);
        assertArrayEquals(second, new TestER[]{children, te});
    }

    /**
     * multiple type array
     */
    @Test
    @SuppressWarnings("unchecked")
    void testListTwoDimensionalArray() {
        TestER te = new TestER();
        te.setName("parent");
        Children children = new Children();
        children.setName("child");
        children.setAge(1);

        List<Object[]> list1 = new ArrayList<>();
        list1.add(new Object[]{new TestER[]{te, children}, new TestER[]{children, te}});
        list1.add(new TestER[]{children, te});
        list1.add(new Object[]{1, false});

        byte[] by1 = JSON.toJSONBytes(list1, JSONWriter.Feature.WriteClassName);
        System.out.println(new String(by1));
        List<Object[]> result = JSON.parseObject(by1, list1.getClass(), JSONReader.autoTypeFilter(
                Object.class.getName(),
                TestER.class.getName(),
                Children.class.getName()
        ));
        assertEquals(result.size(), 3);
//
//        Object[] first = result.get(0);
//        assertEquals(first.length, 2);
//        assertArrayEquals(result.get(0), new Object[]{new TestER[]{te, children}, new TestER[]{children, te}});
//
//        Object[] second = result.get(1);
//        assertEquals(second.length, 2);
//        assertArrayEquals(second, new TestER[]{children, te});
//
//        Object[] third = result.get(2);
//        assertEquals(third.length, 2);
//        assertArrayEquals(third, new Object[]{1, false});
    }
}
