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

## 4. 结合JSONCreator & JSONField(value=true)的例子
如下的Address类，没有缺省构造函数，只有一个字段，我希望序列化结果为address字段的字符串。
```java
public static class Address {
    private final String address;

    public Address(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }
}
```

注入使用方式如下：
```java
static class AddressMixin {
    // 通过JSONCreator指定构造函数
    @JSONCreator
    public AddressMixin(@JSONField(value = true) String address) {
    }

    // 配置输出使用此字段
    @JSONField(value = true)
    public String getAddress() {
        return null;
    }
}

@Test
public void test() {
    JSON.mixIn(Address.class, AddressMixin.class); // 通过mixin注入Annotation

    Address address = new Address("HangZhou");

    // 序列化输出结果为address字段
    String str = JSON.toJSONString(address); 
    assertEquals("\"HangZhou\"", str); 

    // 通过结果可以看出使用了JSONCreator指定的构造函数
    Address address1 = JSON.parseObject(str, Address.class);
    assertEquals(address.getAddress(), address1.getAddress()); 
}
```
