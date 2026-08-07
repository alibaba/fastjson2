在fastjson中，可以使用Annotation JSONType#seeAlso配置，类似于JAXB中的XmlSeeAlso。用法如下：

### JavaBean Config
```java
@JSONType(seeAlso={Dog.class, Cat.class})
public static class Animal {
}

@JSONType(typeName = "dog")
public static class Dog extends Animal {
    public String dogName;
}

@JSONType(typeName = "cat")
public static class Cat extends Animal {
    public String catName;
}
```

#### Usage
```java
Dog dog = new Dog();
dog.dogName = "dog1001";

String text = JSON.toJSONString(dog, SerializerFeature.WriteClassName);
Assert.assertEquals("{\"@type\":\"dog\",\"dogName\":\"dog1001\"}", text);

Dog dog2 = (Dog) JSON.parseObject(text, Animal.class);

Assert.assertEquals(dog.dogName, dog2.dogName);
```

```java
Cat cat = new Cat();
cat.catName = "cat2001";

String text = JSON.toJSONString(cat, SerializerFeature.WriteClassName);
Assert.assertEquals("{\"@type\":\"cat\",\"catName\":\"cat2001\"}", text);

Cat cat2 = (Cat) JSON.parseObject(text, Animal.class);

Assert.assertEquals(cat.catName, cat2.catName);
```
