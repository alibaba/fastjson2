# ValueFilter
`FASTJSON`提供了一个序列化时对字段名做处理的扩展叫做`ValueFilter`，接口如下
```java
package com.alibaba.fastjson2.filter;

public interface ValueFilter
        extends Filter {
    Object apply(Object object, String name, Object value);
}
```

ValueFilter允许你在输出的时候自定义输出的值，比如：
```java
public static class Bean {
    public int id;
}

@Test
public void test_valuefilterCompose() {
    ValueFilter filter0 = (source, name, value) -> {
        if (name.equals("id")) {
            return ((Integer) value).intValue() + 1;
        }
        return value;
    };

    ValueFilter filter1 = (source, name, value) -> {
        if (name.equals("id")) {
            return ((Integer) value).intValue() + 10;
        }
        return value;
    };

    Bean bean = new Bean();
    bean.id = 100;
    String str = JSON.toJSONString(bean, ValueFilter.compose(filter0, filter1));
    assertEquals("{\"id\":111}", str);
}
```
