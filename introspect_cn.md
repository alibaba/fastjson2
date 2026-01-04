# Fastjson2 内省包文档

## 概述

`com.alibaba.fastjson2.introspect` 包为 fastjson2 提供了基于反射的属性访问功能。该包包含用于在 JSON 序列化和反序列化操作期间进行高效属性访问的类和接口。

该包的核心是 `PropertyAccessor` 接口，无论底层访问机制（字段访问、方法调用或函数式接口）如何，它都提供了统一的 API 来获取和设置对象属性。

`PropertyAccessorFactory` 类是创建优化属性访问器的主要工厂，根据属性类型和访问方法提供针对基本类型、包装类和复杂对象的实现。

## 主要功能

- 针对基本类型优化的访问器，避免装箱/拆箱开销
- 支持基于字段和基于方法的属性访问
- 基于函数式接口的访问器以获得最大性能
- 兼容类型之间的自动类型转换
- 带有详细错误消息的异常处理
- 针对不同性能要求的多种实现策略

## 架构

内省包实现了分层架构，具有多种访问策略：

### 1. PropertyAccessor 接口

`PropertyAccessor` 接口是核心抽象，定义了用于通用访问对象属性的方法。它提供了统一的方法来获取和设置对象的属性，通过各种访问器方法支持基本类型和对象。

主要方法包括：
- `getObject()`、`getByteValue()`、`getCharValue()`、`getShortValue()`、`getIntValue()`、`getLongValue()`、`getFloatValue()`、`getDoubleValue()`、`getBooleanValue()`
- `setObject()`、`setByteValue()`、`setCharValue()`、`setShortValue()`、`setIntValue()`、`setLongValue()`、`setFloatValue()`、`setDoubleValue()`、`setBooleanValue()`

### 2. 基础访问器类

该包提供了三个抽象基类，实现不同的访问策略：

#### FieldAccessor
- 用于基于字段的属性访问器的抽象基类
- 提供使用反射访问对象字段的通用功能
- 处理字段元数据并根据字段是否声明为 final 来确定字段是否支持设置
- 通过反射使用直接字段访问实现 PropertyAccessor 接口

#### MethodAccessor
- 用于基于方法的属性访问器的抽象基类
- 提供使用 getter 和 setter 方法访问对象属性的通用功能
- 允许通过标准的 getter/setter 方法对访问属性
- 使用方法调用实现 PropertyAccessor 接口进行属性访问

#### FunctionAccessor
- 用于基于函数式的属性访问器的抽象基类
- 提供使用 getter 和 setter 函数访问对象属性的通用功能
- 允许通过函数式接口而不是直接字段访问或方法调用来访问属性
- 使用函数式接口实现 PropertyAccessor 接口进行属性访问

### 3. 属性访问器工厂层次结构

该包包含一个复杂的工厂层次结构，提供多种属性访问策略：

#### PropertyAccessorFactory
- 创建属性访问器的主要工厂类，使用反射
- 为不同类型（基本类型、String、BigInteger、BigDecimal）提供优化的访问器实现，以优化性能并避免装箱/拆箱开销
- 为不同类型创建专门的访问器实现

#### PropertyAccessorFactoryLambda
- 扩展 PropertyAccessorFactory 并添加对 LambdaMetafactory 基于访问的支持
- 创建用于属性访问的高效函数式接口
- 使用 MethodHandle 和 LambdaMetafactory 提供优化的构造函数实例化

#### PropertyAccessorFactoryMethodHandle
- 在 JDK 11+ 上可用，使用 MethodHandles.Lookup 进行字段和方法访问
- 使用 MethodHandles.Lookup 的 unreflectGetter/unreflectSetter 而不是 VarHandle 进行字段访问
- 提供访问对象属性的替代方式

#### PropertyAccessorFactoryVarHandle
- 在 JDK 11+ 上可用，使用 VarHandle 进行字段访问
- 使用 VarHandle API 提供高性能属性访问，这比传统的反射或基于 Unsafe 的方法更高效

#### PropertyAccessorFactoryUnsafe
- 使用 Unsafe 操作进行字段访问，以提供比基于反射的访问更好的性能
- 创建使用 Unsafe 直接内存访问来获取和设置字段值的属性访问器，这比传统反射更快
- 注意：使用 sun.misc.Unsafe，这不是标准 Java API 的一部分，可能不是所有 JVM 实现都可用

## 类型特定的访问器接口

该包包含针对不同类型优化的专门接口：

- `PropertyAccessorBooleanValue`、`PropertyAccessorByteValue`、`PropertyAccessorShortValue`、`PropertyAccessorIntValue`、`PropertyAccessorLongValue`、`PropertyAccessorFloatValue`、`PropertyAccessorDoubleValue`、`PropertyAccessorCharValue`
- `PropertyAccessorObject`、`PropertyAccessorString`、`PropertyAccessorBigInteger`、`PropertyAccessorBigDecimal`、`PropertyAccessorNumber`
- 这些接口提供类型特定的 getter 和 setter 方法，并处理兼容类型之间的转换

## 构造函数支持

工厂还提供创建基于构造函数的函数的方法：
- `createSupplier(Constructor)`：创建一个可以使用给定构造函数实例化对象的 Supplier
- `createFunction(Constructor)`：创建一个可以使用给定构造函数实例化对象的 Function
- `createIntFunction(Constructor)`、`createLongFunction(Constructor)`、`createDoubleFunction(Constructor)`：为不同类型参数创建专门的函数类型

## 架构图示

### 类继承层次图

```
                                    PropertyAccessor (接口)
                                           |
                                    PropertyAccessorObject
                                           |
                   +------------------------+------------------------+
                   |                        |                        |
            FieldAccessor              MethodAccessor        FunctionAccessor
                   |                        |                        |
         +---------+---------+              |                        |
         |         |         |              |                        |
   FieldAccessor   |         |              |                        |
  Reflect/Unsafe/  |         |              |                        |
  MethodHandle/    |         |              |                        |
  VarHandle        |         |              |                        |
         |         |         |              |                        |
         +---------+---------+              |                        |
                   |                        |                        |
                   +------------------------+------------------------+
                                            |
                                    PropertyAccessorFactory
                                            |
                   +------------------------+------------------------+
                   |                        |                        |
      PropertyAccessorFactoryLambda  PropertyAccessorFactoryUnsafe  |
                   |                        |                        |
                   |            +-----------+-----------+            |
                   |            |           |           |            |
                   |    PropertyAccessorFactoryVarHandle |            |
                   |            |           |           |            |
                   |            |    PropertyAccessorFactoryMethodHandle
                   |            |           |           |
                   |            |           |           |
         (基于Lambda)       (基于VarHandle)       (基于MethodHandle)
```

### 组件交互图

```
┌─────────────────────────────────────────────────────────────────────┐
│                        Fastjson2 内省包                             │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌─────────────────┐    ┌─────────────────────┐    ┌─────────────┐ │
│  │    用户代码     │    │    属性访问器       │    │    对象     │ │
│  │  (JSON序列化/    │◄──►│  (PropertyAccessor) │◄──►│   字段/     │ │
│  │   反序列化)     │    │                     │    │  方法/函数  │ │
│  └─────────────────┘    └─────────────────────┘    └─────────────┘ │
│         │                        │                                    │
│         │                        │                                    │
│         │    ┌─────────────────────────────────────────────────────┐ │
│         └───►│ 工厂层次结构                                        │ │
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
└─────────────────────────────────────────────────────────────────────┘
```

## 性能考虑

内省包在设计时考虑了性能：

1. **类型特定的访问器**：根据字段类型创建不同的访问器实现，为每种特定类型提供最佳性能
2. **避免装箱/拆箱**：针对基本类型的专门访问器接口避免了装箱和拆箱的开销
3. **多种访问策略**：该包提供多种访问策略（反射、MethodHandle、VarHandle、Unsafe），以选择最适合运行时环境的最有效策略
4. **LambdaMetafactory 集成**：使用 LambdaMetafactory 创建高效的函数式接口进行属性访问

## 在 Fastjson2 中的使用

内省包在 fastjson2 内部用于在序列化和反序列化操作期间进行高效的属性访问。它允许 fastjson2 高效地读取和写入对象属性，无论它们是通过字段、getter/setter 方法还是函数式接口访问的。