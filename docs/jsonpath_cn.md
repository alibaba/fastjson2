# FASTJSON2 JSONPath支持介绍

在FASTJSON2中，JSONPath是一等公民，支持通过JSONPath在不完整解析JSON Document的情况下，根据JSONPath读取内容；也支持用JSONPath对JavaBean求值，可以在Java框架中当做对象查询语言（OQL）来使用。

## 语法兼容标准
支持[SQL 2016](https://en.wikipedia.org/wiki/SQL:2016)的JSON Path的语法 [ISO/IEC 19075-6](https://www.iso.org/standard/78937.html)

## 1. 支持语法

| JSONPATH	                        | 描述                                                                                                                          |
|----------------------------------|-----------------------------------------------------------------------------------------------------------------------------|
| $	                               | 根对象，例如$.name                                                                                                                |
| [num]	                           | 数组访问，其中num是数字，可以是负数。例如$[0].leader.departments[-1].name 	                                                                    |
| [num0,num1,num2...]              | 数组多个元素访问，其中num是数字，可以是负数，返回数组中的多个元素。例如$[0,3,-2,5]                                                                            |
| [start:end]		                    | 数组范围访问，其中start和end是开始小表和结束下标，可以是负数，返回数组中的多个元素。例如$[0:5]                                                                      |
| [start:end :step]	               | 数组范围访问，其中start和end是开始小表和结束下标，可以是负数；step是步长，返回数组中的多个元素。例如$[0:5:2]                                                            |
| [?(@.key)]                       | 对象属性非空过滤，例如$.departs[?(@.name)]	                                                                                            |
| [?(@.key > 123)]		               | 数值类型对象属性比较过滤，例如$.departs[id >= 123]，比较操作符支持=,!=,>,>=,<,<=	                                                                  |
| [?(@.key = '123')]		             | 字符串类型对象属性比较过滤，例如$.departs[?(@..name = '123')]，比较操作符支持=,!=,>,>=,<,<=	                                                        |
| [?(@.key like 'aa%')]		          | 字符串类型like过滤， 例如$.departs[?(@..name like 'sz*')]，通配符只支持%支持not like	                                                          |
| [?(@.key rlike 'regexpr')]		     | 字符串类型正则匹配过滤，	                                                                                                               | 例如departs[name rlike 'aa(.)*']，正则语法为jdk的正则语法，支持not rlike 	|
| [?(@.key in ('v0', 'v1'))]		     | IN过滤, 支持字符串和数值类型	例如: $.departs[?(@.name in ('wenshao','Yako'))] $.departs[id not in (101,102)] 	                            |
| [?(@.key between 234 and 456)]		 | BETWEEN过滤, 支持数值类型，支持not between 例如: $.departs[?(@.id between 101 and 201)] <br/> $.departs[?(@.id not between 101 and 201)] |
| length() 或者 size()		             | 数组长度。例如$.values.size() 支持类型java.util.Map和java.util.Collection和数组                                                            |
| keySet()		                       | 获取Map的keySet或者对象的非空属性名称。例如$.val.keySet() 支持类型：Map和普通对象. 不支持：Collection和数组（返回null）                                           |
| .		                              | 属性访问，例如$.name 	                                                                                                             |
| ..		                             | deepScan属性访问，例如$..name 	                                                                                                    |
| *		                              | 对象的所有属性，例如$.leader.*                                                                                                        |
| ['key']		                        | 属性访问。例如$['name'] 	                                                                                                          |
| ['key0','key1']		                | 多个属性访问。例如$['id','name'] 	                                                                                                   |

以下两种写法的语义是相同的：
```java
$.store.book[0].title
```
和
```java
$['store']['book'][0]['title']
```

### 1.1 函数
| Function      | 返回类型     | Description      |
|---------------|----------|------------------|
| type          | string   | 返回对象的类型          |
| length/size   | integer  | 返回集合或者字符串的长度     |
| first         | Any      | 集合中第一个元素         |
| last          | Any      | 集合中最后一个元素        |
| keys / keySet | sequence | 返回Map类型的KeySet   |
| values        | sequence | Map类型的Values     |
| entries       | sequence | Map类型的EntrySet   |
| trim          | string   | 对字符串做trim后返回     |
| double        | double   | 将目标类型转换为double类型 |
| ceil          | number   | 对数值类型做ceil处理返回   |
| abs           | number   | 返回对数值类型的绝对值      |
| lower         | string   | 将字符串转换小写         |
| upper         | string   | 将字符串转换成大写        |

### 1.2 聚合函数
| Function | 返回类型   | Description |
|----------|--------|-------------|
| min      |        |             |
| max      |        |             |
| avg      | double |             |

### 1.3 Filter Operators
| Operator        | Description  |
|-----------------|--------------|
| =               | 相等           |
| !=  or <>       | 不等           |
| >               | 大于           |
| >=              | 大于等于         |
| <               | 小于           |
| <=              | 小于等于         |
| ~=              |              |
| like            | 类似SQL中LIKE语法 |
| not like        |              |
| rlike           |              |
| not rlike       |              |
| in              |              |
| not in          |              |
| between         |              |
| not between     |              |
| starts_with     |              |
| not starts_with |              |
| ends_with       |              |
| not ends_with   |              |
| contains        |              |
| not contains    |              |

## 2. 语法举例

| JSONPath   | 语义              |
| ---------- | ----------------- |
| $          | 根对象             |
| $[-1]      | 最后元素           |
| $[:-2]     | 第1个至倒数第2个    |
| $[1:]      | 第2个之后所有元素    |
| $[1,2,3]   | 集合中1,2,3个元素   |


# 3. API 示例

### 3.1 例1
```java
public void test_entity() throws Exception {
   Entity entity = new Entity(123, new Object());
   
  assertSame(entity.getValue(), JSONPath.eval(entity, "$.value")); 
  assertTrue(JSONPath.contains(entity, "$.value"));
  assertTrue(JSONPath.containsValue(entity, "$.id", 123));
  assertTrue(JSONPath.containsValue(entity, "$.value", entity.getValue())); 
  assertEquals(2, JSONPath.size(entity, "$"));
  assertEquals(0, JSONPath.size(new Object[], "$")); 
}

public static class Entity {
   private Integer id;
   private String name;
   private Object value;

   public Entity() {}
   public Entity(Integer id, Object value) { this.id = id; this.value = value; }
   public Entity(Integer id, String name) { this.id = id; this.name = name; }
   public Entity(String name) { this.name = name; }

   public Integer getId() { return id; }
   public Object getValue() { return value; }        
   public String getName() { return name; }
   
   public void setId(Integer id) { this.id = id; }
   public void setName(String name) { this.name = name; }
   public void setValue(Object value) { this.value = value; }
}
```

### 3.2 例2
读取集合多个元素的某个属性
```java
List<Entity> entities = new ArrayList<Entity>();
entities.add(new Entity("wenshao"));
entities.add(new Entity("ljw2083"));

List<String> names = (List<String>)JSONPath.eval(entities, "$.name"); // 返回enties的所有名称
assertSame(entities.get(0).getName(), names.get(0));
assertSame(entities.get(1).getName(), names.get(1));
```
### 3.3 例3
返回集合中多个元素
```java
List<Entity> entities = new ArrayList<Entity>();
entities.add(new Entity("wenshao"));
entities.add(new Entity("ljw2083"));
entities.add(new Entity("Yako"));

List<Entity> result = (List<Entity>)JSONPath.eval(entities, "[1,2]"); // 返回下标为1和2的元素
assertEquals(2, result.size());
assertSame(entities.get(1), result.get(0));
assertSame(entities.get(2), result.get(1));
```
### 3.4 例4
按范围返回集合的子集
```java
List<Entity> entities = new ArrayList<Entity>();
entities.add(new Entity("wenshao"));
entities.add(new Entity("ljw2083"));
entities.add(new Entity("Yako"));

List<Entity> result = (List<Entity>)JSONPath.eval(entities, "[0:2]"); // 返回下标从0到2的元素
assertEquals(3, result.size());
assertSame(entities.get(0), result.get(0));
assertSame(entities.get(1), result.get(1));
assertSame(entities.get(2), result.get(1));
```
### 3.5 例5
通过条件过滤，返回集合的子集
```java
List<Entity> entities = new ArrayList<Entity>();
entities.add(new Entity(1001, "ljw2083"));
entities.add(new Entity(1002, "wenshao"));
entities.add(new Entity(1003, "yakolee"));
entities.add(new Entity(1004, null));

List<Object> result = (List<Object>) JSONPath.eval(entities, "[?(@.id in (1001))]");
assertEquals(1, result.size());
assertSame(entities.get(0), result.get(0));
```
### 3.6 例6
根据属性值过滤条件判断是否返回对象，修改对象，数组属性添加元素
```java
Entity entity = new Entity(1001, "ljw2083");
assertSame(entity , JSONPath.eval(entity, "[?(@.id = 1001)]"));
assertNull(JSONPath.eval(entity, "[id = 1002]"));

JSONPath.set(entity, "id", 123456); //将id字段修改为123456
assertEquals(123456, entity.getId().intValue());

JSONPath.set(entity, "value", new int[0]); //将value字段赋值为长度为0的数组
JSONPath.arrayAdd(entity, "value", 1, 2, 3); //将value字段的数组添加元素1,2,3
```

### 3.7 例7
```java
Map root = Collections.singletonMap("company",
        Collections.singletonMap("departs",
                Arrays.asList(
                        Collections.singletonMap("id", 1001),
                        Collections.singletonMap("id", 1002),
                        Collections.singletonMap("id", 1003)
                )
        ));

List<Object> ids = (List<Object>) JSONPath.eval(root, "$..id");
assertEquals(3, ids.size());
assertEquals(1001, ids.get(0));
assertEquals(1002, ids.get(1));
assertEquals(1003, ids.get(2));
```

### 3.8 例8 keySet

使用keySet抽取对象的属性名，null值属性的名字并不包含在keySet结果中，使用时需要注意，详细可参考示例。

```java
Entity e = new Entity();
e.setId(null);
e.setName("hello");
Map<String, Entity> map = Collections.singletonMap("e", e);
Collection<String> result;

// id is null, excluded by keySet
result = (Collection<String>)JSONPath.eval(map, "$.e.keySet()");
assertEquals(1, result.size());
assertTrue(result.contains("name"));

e.setId(1L);
result = (Collection<String>)JSONPath.eval(map, "$.e.keySet()");
assertEquals(2, result.size());
.assertTrue(result.contains("id")); // included
assertTrue(result.contains("name"));

// Same result
assertEquals(result, JSONPath.keySet(map, "$.e"));
assertEquals(result, new JSONPath("$.e").keySet(map));
```
