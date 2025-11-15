# Using Mixin to Inject Annotations for Custom Serialization and Deserialization

When you need to customize the serialization of classes from a library whose code you cannot modify, you can use FASTJSON2's Mixin feature to inject annotations.

For example:

## 1. The Class to be Serialized
```java
public static class Product {
    public String name;
}
```

## 2. The Mixin Configuration Class
```java
public abstract class ProductMixin {
    @JSONField(name =  "productName")
    String name;
}
```

## 3. Injecting the Configuration & Usage
```java
// Configure injection
JSON.mixIn(Product.class, ProductMixin.class);

// Usage
Product product = new Product();
product.name = "DataWorks";
assertEquals("{\"productName\":\"DataWorks\"}", JSON.toJSONString(product));

Product productParsed = JSON.parseObject("{\"productName\":\"DataWorks\"}", Product.class);
assertEquals("DataWorks", productParsed.name);
```

## 4. Example with JSONCreator & JSONField(value=true)
Consider the following `Address` class, which has no default constructor and only one field. We want the serialization result to be the string value of the `address` field.
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

The injection and usage are as follows:
```java
static class AddressMixin {
    // Specify the constructor with @JSONCreator
    @JSONCreator
    public AddressMixin(@JSONField(value = true) String address) {
    }

    // Configure the output to use this field
    @JSONField(value = true)
    public String getAddress() {
        return null;
    }
}

@Test
public void test() {
    JSON.mixIn(Address.class, AddressMixin.class); // Inject annotations via mixin

    Address address = new Address("HangZhou");

    // The serialization output is the value of the address field
    String str = JSON.toJSONString(address); 
    assertEquals("\"HangZhou\"", str); 

    // The result shows that the constructor specified by @JSONCreator was used
    Address address1 = JSON.parseObject(str, Address.class);
    assertEquals(address.getAddress(), address1.getAddress()); 
}
```
