# FASTJSON 2 Autotype机制介绍

## 1. AutoType功能介绍
FASTJSON支持AutoType功能，这个功能在序列化的JSON字符串中带上类型信息，在反序列化时，不需要传入类型，实现自动类型识别。

## 2. AutoType安全机制介绍
* 必须显示打开才能使用。和fastjson 1.x不一样，fastjson 1.x为了兼容有一个白名单，在fastjson 2中，没有任何白名单，也不包括任何Exception类的白名单，必须显示打开才能使用。这可以保证缺省配置下是安全的。
* 支持配置safeMode，在safeMode打开后，显式传入AutoType参数也不起作用
* 显式打开后，会经过内置黑名单过滤。该黑名单能拦截大部分常见风险，这个机制不能保证绝对安全，打开AutoType不应该在暴露在公网的场景下使用。


## 3. AutoType功能使用介绍

### 3.1 序列化带上类型信息
序列化是带上类型信息，需要使用JSONWriter.Feature.WriteClassName。比如：
```java
Bean bean = ...;
String jsonString = JSON.toJSONString(bean, JSONWriter.Feature.WriteClassName);
```

很多时候，root对象是可以知道类型的，里面的对象字段是基类或者不确定类型，这个时候不输出root对象的类型信息，可以减少序列化结果的大小，也能提升反序列化的性能。
```java
Bean bean = ...;
String jsonString = JSON.toJSONString(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.NotWriteRootClassName);
```


### 3.2 反序列化打开AutoType功能支持自动类型
```java
Bean bean = (Bean) JSON.parseObject(jsonString, Object.class, JSONReader.Feature.SupportAutoType);
```

## 4. 配置safeMode
### 4.1 JVM启动参数配置
```
-Dfastjson2.parser.safeMode=true
```
