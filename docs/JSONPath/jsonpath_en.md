# Introduction to FASTJSON2 JSONPath Support

In FASTJSON2, JSONPath is a first-class citizen. It supports reading content via JSONPath without fully parsing the entire JSON Document. It also supports evaluating JSONPath against JavaBeans, allowing it to be used as an Object Query Language (OQL) within Java frameworks.

## Syntax Compatibility Standard
Compatible with the JSON Path syntax of [SQL 2016](https://en.wikipedia.org/wiki/SQL:2016) [ISO/IEC 19075-6](https://www.iso.org/standard/78937.html)

## 1. Supported Syntax

| JSONPATH	                        | Description                                                                                                                          |
|----------------------------------|-----------------------------------------------------------------------------------------------------------------------------|
| $	                               | Root object, e.g., $.name                                                                                                                |
| [num]	                           | Array access, where num is a number and can be negative. e.g., $.leader.departments[-1].name 	                                                                    |
| [num0,num1,num2...]              | Multiple array element access, where num is a number and can be negative, returning multiple elements from the array. e.g., $[0,3,-2,5]                                                                            |
| [start:end]		                    | Array slice access, where start and end are the starting and ending indices and can be negative, returning multiple elements from the array. e.g., $[0:5]                                                                      |
| [start:end :step]	               | Array slice access with a step, where start and end are the starting and ending indices (can be negative), and step is the increment. e.g., $[0:5:2]                                                            |
| [?(@.key)]                       | Object property non-null filter, e.g., $.departs[?(@.name)]	                                                                                            |
| [?(@.key > 123)]		               | Numeric object property comparison filter, e.g., $.departs[id >= 123]. Comparison operators supported: =, !=, >, >=, <, <=	                                                                  |
| [?(@.key = '123')]		             | String object property comparison filter, e.g., $.departs[?(@..name = '123')]. Comparison operators supported: =, !=, >, >=, <, <=	                                                        |
| [?(@.key like 'aa%')]		          | String 'like' filter, e.g., $.departs[?(@..name like 'sz*')]. Only the '%' wildcard is supported. `not like` is also supported.	                                                          |
| [?(@.key rlike 'regexpr')]		     | String regular expression match filter,	                                                                                                               | e.g., departs[name rlike 'aa(.)*']. The regex syntax is JDK's standard. `not rlike` is also supported. 	|
| [?(@.key in ('v0', 'v1'))]		     | IN filter, supports string and numeric types. e.g., $.departs[?(@.name in ('wenshao','Yako'))] or $.departs[id not in (101,102)] 	                            |
| [?(@.key between 234 and 456)]		 | BETWEEN filter, supports numeric types. `not between` is also supported. e.g., $.departs[?(@.id between 101 and 201)] <br/> $.departs[?(@.id not between 101 and 201)] |
| length() or size()		             | Array length. e.g., $.values.size(). Supports java.util.Map, java.util.Collection, and arrays.                                                            |
| .		                              | Property access, e.g., $.name 	                                                                                                             |
| ..		                             | Deep scan property access, e.g., $..name 	                                                                                                    |
| *		                              | All properties of an object, e.g., $.leader.*                                                                                                        |
| ['key']		                        | Property access. e.g., $['name'] 	                                                                                                          |
| ['key0','key1']		                | Multiple property access. e.g., $['id','name'] 	                                                                                                   |

The following two syntaxes have the same meaning:
```java
$.store.book[0].title
```
and
```java
$['store']['book'][0]['title']
```

### 1.1 Functions
| Function      | Return Type     | Description      |
|---------------|----------|------------------|
| type          | string   | Returns the type of the object          |
| length/size   | integer  | Returns the length of a collection or string     |
| first         | Any      | The first element in a collection         |
| last          | Any      | The last element in a collection        |
| values        | sequence | The `values()` of a Map type     |
| entries       | sequence | The `entrySet()` of a Map type   |
| trim          | string   | Returns the trimmed string     |
| double        | double   | Converts the target type to a double |
| ceil          | number   | Returns the ceiling of a numeric value   |
| abs           | number   | Returns the absolute value of a numeric value      |
| lower         | string   | Converts a string to lower case         |
| upper         | string   | Converts a string to upper case        |
| index(x)      | int      | The parameter x can be a number or a string    |

### 1.2 Aggregate Functions
| Function | Return Type   | Description   |
|----------|--------|---------------|
| min      |        |               |
| max      |        |               |
| first    |        | Returns the first element of the collection   |
| last     |        | Returns the last element of the collection |
| avg      | double |               |

### 1.3 Filter Operators
| Operator        | Description  |
|-----------------|--------------|
| =               | Equal          |
| !=  or <>       | Not equal          |
| >               | Greater than          |
| >=              | Greater than or equal to        |
| <               | Less than          |
| <=              | Less than or equal to        |
| ~=              |              |
| like            | Similar to LIKE in SQL |
| not like        |              |
| rlike           |            |
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

## 2. Syntax Examples

| JSONPath   | Meaning              |
| ---------- | ----------------- |
| $          | Root object             |
| $[-1]      | Last element           |
| $[:-2]     | First to the second to last element    |
| $[1:]      | All elements from the second one on    |
| $[1,2,3]   | The 1st, 2nd, and 3rd elements in the collection   |


# 3. API Examples

### 3.1 Example 1
```java
public void test_entity() throws Exception {
    Entity entity = new Entity(123, new Object());

    assertSame(entity.getValue(), JSONPath.eval(entity, "$.value")); 
    assertTrue(JSONPath.contains(entity, "$.value"));
    assertEquals(2, JSONPath.eval(entity, "$.length()"));
    assertEquals(0, JSONPath.eval(new Object[0], "$.length()"));
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

### 3.2 Example 2
Read a specific property from multiple elements in a collection.
```java
List<Entity> entities = new ArrayList<Entity>();
entities.add(new Entity("wenshao"));
entities.add(new Entity("ljw2083"));

List<String> names = (List<String>)JSONPath.eval(entities, "$.name"); // returns all names from the entities
assertSame(entities.get(0).getName(), names.get(0));
assertSame(entities.get(1).getName(), names.get(1));
```
### 3.3 Example 3
Return multiple elements from a collection.
```java
List<Entity> entities = new ArrayList<Entity>();
entities.add(new Entity("wenshao"));
entities.add(new Entity("ljw2083"));
entities.add(new Entity("Yako"));

List<Entity> result = (List<Entity>)JSONPath.eval(entities, "[1,2]"); // returns elements at index 1 and 2
assertEquals(2, result.size());
assertSame(entities.get(1), result.get(0));
assertSame(entities.get(2), result.get(1));
```
### 3.4 Example 4
Return a subset of a collection by range.
```java
List<Entity> entities = new ArrayList<Entity>();
entities.add(new Entity("wenshao"));
entities.add(new Entity("ljw2083"));
entities.add(new Entity("Yako"));

List<Entity> result = (List<Entity>)JSONPath.eval(entities, "[0:2]"); // returns elements from index 0 to 2 (inclusive)
assertEquals(3, result.size());
assertSame(entities.get(0), result.get(0));
assertSame(entities.get(1), result.get(1));
assertSame(entities.get(2), result.get(2));
```
### 3.5 Example 5
Return a subset of a collection by filtering with a condition.
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
### 3.6 Example 6
Filter object based on property value, modify object, and add an element to an array property.
```java
Entity entity = new Entity(1001, "ljw2083");
assertSame(entity , JSONPath.eval(entity, "[?(@.id = 1001)]"));

JSONPath.set(entity, "id", 123456); // Set the id field to 123456
assertEquals(123456, entity.getId().intValue());

JSONPath.set(entity, "value", new int[0]); // Set the value field to an empty array
```

### 3.7 Example 7
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
