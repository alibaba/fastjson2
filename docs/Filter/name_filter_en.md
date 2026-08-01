# NameFilter
`FASTJSON` provides an extension for processing field names during serialization called `NameFilter`. The interface is as follows:
```java
package com.alibaba.fastjson2.filter;

public interface NameFilter
        extends Filter {
    String process(Object object, String name, Object value);
}
```

`NameFilter` provides implementations for common requirements through the `of` method, for example:
```java
public class Bean {
    public int userId;
}

@Test
public void test() {
    Bean bean = new Bean();
    bean.userId = 101;

    NameFilter pascalNameFilter = NameFilter.of(PropertyNamingStrategy.PascalCase);
    String str = JSON.toJSONString(bean, pascalNameFilter);
    assertEquals("{\"UserId\":101}", str);
    JSONObject object = JSON.parseObject(str);
    assertEquals(101, object.get("UserId"));
}
```
