# 使用Mixin注入Anntation定制序列化和反序列化

当你需要定制化序列化一些LIB里面的类，你无法修改这些类的代码，你可以使用FASTJSON2的Minxin功能注入Annotation。

比如：

## 1. 要序列化的类
```java
public static class Product {
    public String name;
}
```

## 2. 注入配置类
```java
public abstract class ProductMixin {
    @JSONField(name =  "productName")
    String name;
}
```

## 3. 注入配置 & 使用
```java
// 配置注入
JSON.mixIn(Product.class, ProductMixin.class);

// 使用
Product product = new Product();
product.name = "DataWorks";
assertEquals("{\"productName\":\"DataWorks\"}", JSON.toJSONString(product));

Product productParsed = JSON.parseObject("{\"productName\":\"DataWorks\"}", Product.class);
assertEquals("DataWorks", productParsed.name);
```
