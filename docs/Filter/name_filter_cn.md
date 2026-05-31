# NameFilter
`FASTJSON`提供了一个序列化时对字段名做处理的扩展叫做`NameFilter`，接口如下
```java
package com.alibaba.fastjson2.filter;

public interface NameFilter
        extends Filter {
    String process(Object object, String name, Object value);
}
```

`NameFilter`通过of方法提供了常见需求的实现，比如：
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
