# Fastjson2 Introspect Package Documentation

## Overview

The `com.alibaba.fastjson2.introspect` package provides a comprehensive reflection-based property access functionality for fastjson2. This package contains classes and interfaces for efficient property access during JSON serialization and deserialization operations.

The core of this package is the `PropertyAccessor` interface, which provides a unified API for getting and setting object properties regardless of the underlying access mechanism (field access, method calls, or functional interfaces).

The `PropertyAccessorFactory` class serves as the main factory for creating optimized property accessors based on the property type and access method, providing implementations for primitive types, wrapper classes, and complex objects.

## Key Features

- Optimized accessors for primitive types to avoid boxing/unboxing overhead
- Support for both field-based and method-based property access
- Functional interface-based accessors for maximum performance
- Automatic type conversion between compatible types
- Exception handling with detailed error messages
- Multiple implementation strategies for different performance requirements

## Architecture

The introspect package implements a hierarchical architecture with multiple access strategies:

### 1. PropertyAccessor Interface

The `PropertyAccessor` interface is the core abstraction that defines methods for accessing object properties generically. It provides unified methods for getting and setting properties of objects, supporting both primitive types and objects through various accessor methods.

Key methods include:
- `getObject()`, `getByteValue()`, `getCharValue()`, `getShortValue()`, `getIntValue()`, `getLongValue()`, `getFloatValue()`, `getDoubleValue()`, `getBooleanValue()`
- `setObject()`, `setByteValue()`, `setCharValue()`, `setShortValue()`, `setIntValue()`, `setLongValue()`, `setFloatValue()`, `setDoubleValue()`, `setBooleanValue()`

### 2. Base Accessor Classes

The package provides three abstract base classes that implement different access strategies:

#### FieldAccessor
- Abstract base class for field-based property accessors
- Provides common functionality for accessing object fields using reflection
- Handles field metadata and determines if the field supports setting based on whether the field is declared as final
- Implements PropertyAccessor interface using direct field access through reflection

#### MethodAccessor
- Abstract base class for method-based property accessors
- Provides common functionality for accessing object properties using getter and setter methods
- Allows for accessing properties through standard getter/setter method pairs
- Implements PropertyAccessor interface using method invocation for property access

#### FunctionAccessor
- Abstract base class for function-based property accessors
- Provides common functionality for accessing object properties using getter and setter functions
- Allows for accessing properties through functional interfaces rather than direct field access or method invocation
- Implements PropertyAccessor interface using functional interfaces for property access

### 3. Property Accessor Factory Hierarchy

The package includes a sophisticated factory hierarchy that provides multiple strategies for property access:

#### PropertyAccessorFactory
- The main factory class that creates property accessors using reflection
- Provides optimized accessor implementations for different data types (primitives, String, BigInteger, BigDecimal) to optimize performance and avoid boxing/unboxing overhead where possible
- Creates specialized accessor implementations for different data types

#### PropertyAccessorFactoryLambda
- Extends PropertyAccessorFactory and adds support for LambdaMetafactory-based access
- Creates efficient functional interfaces for property access when possible
- Provides optimized constructor instantiation using MethodHandle and LambdaMetafactory

#### PropertyAccessorFactoryMethodHandle
- Available on JDK 11+, uses MethodHandles.Lookup for field and method access
- Uses MethodHandles.Lookup's unreflectGetter/unreflectSetter instead of VarHandle for field access
- Provides an alternative way to access object properties efficiently

#### PropertyAccessorFactoryVarHandle
- Available on JDK 11+, uses VarHandle for field access
- Provides high-performance property access using the VarHandle API which is more efficient than traditional reflection or Unsafe-based approaches

#### PropertyAccessorFactoryUnsafe
- Uses Unsafe operations for field access to provide better performance compared to reflection-based access
- Creates property accessors that use direct memory access via Unsafe to get and set field values, which is faster than traditional reflection
- Note: Uses sun.misc.Unsafe, which is not part of the standard Java API and may not be available in all JVM implementations

## Type-Specific Accessor Interfaces

The package includes specialized interfaces for different data types to optimize performance:

- `PropertyAccessorBooleanValue`, `PropertyAccessorByteValue`, `PropertyAccessorShortValue`, `PropertyAccessorIntValue`, `PropertyAccessorLongValue`, `PropertyAccessorFloatValue`, `PropertyAccessorDoubleValue`, `PropertyAccessorCharValue`
- `PropertyAccessorObject`, `PropertyAccessorString`, `PropertyAccessorBigInteger`, `PropertyAccessorBigDecimal`, `PropertyAccessorNumber`
- These interfaces provide type-specific getter and setter methods and handle conversions between compatible types

## Constructor Support

The factory also provides methods to create constructor-based functions:
- `createSupplier(Constructor)`: Creates a Supplier that can instantiate objects using the given constructor
- `createFunction(Constructor)`: Creates a Function that can instantiate objects using the given constructor
- `createIntFunction(Constructor)`, `createLongFunction(Constructor)`, `createDoubleFunction(Constructor)`: Create specialized function types for different parameter types

## Architecture Diagrams

### Class Hierarchy Diagram

```
                             PropertyAccessor (Interface)
                                    │
                             PropertyAccessorObject
                                    │
                   ┌───────────────────────────────────────────────┐
                   │                                               │
            FieldAccessor        MethodAccessor        FunctionAccessor
                   │                    │                          │
         ┌─────────┼─────────┐          │                          │
         │         │         │          │                          │
FieldAccessor   FieldAccessor  FieldAccessor                       │
Reflect-based    Unsafe-based  MethodHandle-based                  │
         │         │         │          │                          │
         └─────────┼─────────┘          │                          │
                   │                    │                          │
                   └────────────────────┼──────────────────────────┘
                                        │
                            PropertyAccessorFactory
                                        │
                   ┌────────────────────┼────────────────────┐
                   │                    │                    │
PropertyAccessorFactoryLambda  PropertyAccessorFactoryUnsafe │
                   │                    │                    │
                   │        ┌───────────┼───────────┐        │
                   │        │           │           │        │
                   │  PropertyAccessorFactoryVarHandle       │
                   │        │           │           │        │
                   │        │PropertyAccessorFactoryMethodHandle
                   │        │           │           │        │
                   │        │           │           │        │
         Lambda-based    VarHandle-based    MethodHandle-based
```

### Component Interaction Diagram

```
┌──────────────────────────────────────────────────────────────────────┐
│                    Fastjson2 Introspect Package                      │
├──────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  ┌─────────────────┐    ┌─────────────────────┐    ┌─────────────┐   │
│  │   User Code     │    │  Property Accessor  │    │   Object    │   │
│  │ (JSON Ser/Des)  │◄──►│  (PropertyAccessor) │◄──►│   Fields/   │   │
│  └─────────────────┘    └─────────────────────┘    │ Methods/    │   │
│         │                        │                 │ Functions   │   │
│         │                        │                 └─────────────┘   │
│         │                        │                                   │
│         │    ┌─────────────────────────────────────────────────────┐ │
│         └───►│ Factory Hierarchy                                   │ │
│              │                                                     │ │
│              │ ┌─────────────────────┐ ┌─────────────────────────┐ │ │
│              │ │ PropertyAccessor    │ │ PropertyAccessor        │ │ │
│              │ │ Factory (Base)      │ │ FactoryMethodHandle     │ │ │
│              │ └─────────────────────┘ └─────────────────────────┘ │ │
│              │        ▲                           ▲                │ │
│              │        │                           │                │ │
│              │ ┌─────────────────────┐ ┌─────────────────────────┐ │ │
│              │ │ PropertyAccessor    │ │ PropertyAccessor        │ │ │
│              │ │ FactoryLambda       │ │ FactoryVarHandle        │ │ │
│              │ └─────────────────────┘ └─────────────────────────┘ │ │
│              │        ▲                           ▲                │ │
│              │        │                           │                │ │
│              │ ┌─────────────────────┐ ┌─────────────────────────┐ │ │
│              │ │ PropertyAccessor    │ │ PropertyAccessor        │ │ │
│              │ │ FactoryUnsafe       │ │ FactoryMethodHandle     │ │ │
│              │ └─────────────────────┘ └─────────────────────────┘ │ │
│              └─────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────────────┘
```

## Performance Considerations

The introspect package is designed with performance in mind:

1. **Type-Specific Accessors**: Different accessor implementations are created based on the field type to provide optimal performance for each specific type
2. **Avoiding Boxing/Unboxing**: Specialized accessor interfaces for primitive types avoid the overhead of boxing and unboxing
3. **Multiple Access Strategies**: The package provides multiple access strategies (reflection, MethodHandle, VarHandle, Unsafe) to choose the most efficient one for the runtime environment
4. **LambdaMetafactory Integration**: Uses LambdaMetafactory to create efficient functional interfaces for property access

## Error Handling

All accessor implementations provide proper error handling with detailed exception messages. When errors occur during property access, the implementations create JSON exceptions with detailed information about the operation that failed.

## Usage in Fastjson2

The introspect package is used internally by fastjson2 for efficient property access during serialization and deserialization operations. It allows fastjson2 to efficiently read and write object properties regardless of whether they are accessed through fields, getter/setter methods, or functional interfaces.
