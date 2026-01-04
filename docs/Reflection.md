# Fastjson2 Reflection Package Documentation

## Overview

The `com.alibaba.fastjson2.reflect` package provides an efficient and flexible property access mechanism that serves as the backbone for JSON serialization and deserialization in fastjson2. This package abstracts different property access strategies (field access, method invocation, and functional interfaces) behind a unified `PropertyAccessor` interface, enabling high-performance property access with automatic type conversion capabilities.

## Key Components

### 1. PropertyAccessor Interface

The `PropertyAccessor` interface is the central abstraction in this package. It provides methods to:

- Get and set property values for all primitive types and objects
- Access property metadata (name, type, class)
- Check whether getting or setting is supported for the property
- Handle type conversions between compatible types

### 2. Property Accessor Implementations

The package supports three main types of property access:

#### Field-based Access
- Direct field access through reflection
- Implemented in `FieldAccessor` and its specialized subclasses
- Optimized for each primitive type to avoid boxing/unboxing

#### Method-based Access  
- Getter/setter method invocation
- Implemented in `MethodAccessor` and its specialized subclasses
- Supports standard JavaBean patterns

#### Function-based Access
- Functional interface access using lambdas/method handles
- Implemented in `FunctionAccessor` and its specialized subclasses
- Provides the highest performance for repeated access

### 3. PropertyAccessorFactory

The `PropertyAccessorFactory` class is responsible for:

- Creating optimized property accessors based on the property type
- Supporting field-based, method-based, and function-based access strategies
- Providing specialized implementations for each primitive and wrapper type
- Handling automatic type conversions between compatible types

### 4. PropertyAccessorFactoryLambda

The `PropertyAccessorFactoryLambda` extends the base factory to provide:

- Lambda-based property access using `LambdaMetafactory`
- High-performance functional interfaces created at runtime
- Optimized access through method handles
- Support for chainable/fluent setters

## Design Principles

### Performance Optimization
- Specialized accessor implementations for each primitive type to minimize boxing/unboxing
- Direct field access when possible for maximum speed
- Lambda-based access for frequently accessed properties
- Automatic type conversion methods to handle common use cases

### Type Safety
- Strict validation of method signatures during accessor creation
- Compile-time type checking where possible
- Runtime error handling with detailed exception messages

### Flexibility
- Support for multiple access patterns (field, method, functional)
- Extensible interface design allowing custom accessor implementations
- Generic type information preservation
- Support for complex object hierarchies

## Usage Examples

### Creating Field-Based Accessors

```java
import com.alibaba.fastjson2.reflect.PropertyAccessorFactory;
import java.lang.reflect.Field;

public class Example {
    private int value;
    private String name;
    
    public static void main(String[] args) throws NoSuchFieldException {
        PropertyAccessorFactory factory = new PropertyAccessorFactory();
        
        Field valueField = Example.class.getDeclaredField("value");
        PropertyAccessor valueAccessor = factory.create(valueField);
        
        Example obj = new Example();
        valueAccessor.setIntValue(obj, 42);
        int value = valueAccessor.getIntValue(obj);
        System.out.println("Value: " + value); // Value: 42
    }
}
```

### Creating Method-Based Accessors

```java
import com.alibaba.fastjson2.reflect.PropertyAccessorFactory;
import java.lang.reflect.Method;

public class Example {
    private String name;
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public static void main(String[] args) throws NoSuchMethodException {
        PropertyAccessorFactory factory = new PropertyAccessorFactory();
        
        Method getter = Example.class.getMethod("getName");
        Method setter = Example.class.getMethod("setName", String.class);
        
        PropertyAccessor accessor = factory.create("name", getter, setter);
        
        Example obj = new Example();
        accessor.setObject(obj, "Hello");
        String name = (String) accessor.getObject(obj);
        System.out.println("Name: " + name); // Name: Hello
    }
}
```

### Creating Function-Based Accessors

```java
import com.alibaba.fastjson2.reflect.PropertyAccessorFactory;

import java.util.function.Function;
import java.util.function.BiConsumer;

public class Example {
    public String value;
    
    public static void main(String[] args) {
        PropertyAccessorFactory factory = new PropertyAccessorFactory();
        
        Function<Example, String> getter = obj -> obj.value;
        BiConsumer<Example, String> setter = (obj, val) -> obj.value = val;
        
        PropertyAccessor accessor = factory.create(
            "value", String.class, String.class, getter, setter);
            
        Example obj = new Example();
        accessor.setObject(obj, "Test");
        String value = (String) accessor.getObject(obj);
        System.out.println("Value: " + value); // Value: Test
    }
}
```

## Type Conversion

The property accessor implementations automatically handle conversions between compatible types:

- Primitive to wrapper types (e.g., `int` to `Integer`)
- Numeric type widening (e.g., `int` to `long`, `float` to `double`)
- String to primitive conversions for setter methods
- Object to primitive conversions when possible

## Error Handling

All accessor implementations provide detailed error messages in case of reflection failures:

- `JSONException` with property name and operation context
- Cause preservation to help debug the underlying issue
- Specific error messages for field accessibility issues

## Performance Considerations

1. **Field Access**: Fastest for direct field access, but requires appropriate field permissions
2. **Method Access**: Good performance for standard getter/setter patterns
3. **Lambda Access**: Best for frequently accessed properties due to JVM optimizations
4. **Type-Specific Accessors**: Use the primitive-specific methods (e.g., `getIntValue`) rather than generic `getObject` for better performance

## Thread Safety

Property accessors are generally immutable after creation and can be safely shared across threads. However, the objects they access may not be thread-safe, so proper synchronization is still required at the application level.

## Integration with Fastjson2

This package is primarily used internally by fastjson2 for:

- Object serialization to JSON
- JSON deserialization to objects
- Property name mapping and transformation
- Type conversion during serialization/deserialization
- Custom property filtering and validation