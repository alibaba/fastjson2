# ValueFilter
`FASTJSON` provides an extension for processing field values during serialization called `Value-Filter`. The interface is as follows:
```java
package com.alibaba.fastjson2.filter;

public interface ValueFilter
extends Filter {
Object apply(Object object, String name, Object value);
}
```

ValueFilter allows you to customize the output value during serialization, for example:
```java
public static class Bean {
    public int id;
}

@Test
public void test_valuefilterCompose() {
    ValueFilter filter0 = (source, name, value) -> {
        if ("id".equals(name)) {
            return ((Integer) value).intValue() + 1;
        }
        return value;
    };

    ValueFilter filter1 = (source, name, value) -> {
        if ("id".equals(name)) {
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
