# FASTJSON 2 Autotype机制介绍

## 1. AutoType功能介绍
FASTJSON支持AutoType功能，这个功能会在序列化的JSON字符串中带上类型信息，在反序列化时，不需要传入类型，实现自动类型识别。

## 2. AutoType安全机制介绍
* 必须显示打开才能使用。和fastjson 1.x不一样，fastjson 1.x为了兼容有一个白名单，在fastjson 2中，没有任何白名单，也不包括任何Exception类的白名单，必须显示打开才能使用。这可以保证缺省配置下是安全的。
* 支持配置safeMode，在safeMode打开后，显示传入AutoType参数也不起作用。
* 显示打开后，会经过内置黑名单过滤。该黑名单能拦截大部分常见风险，这个机制不能保证绝对安全，打开AutoType不应该在暴露在公网的场景下使用。


## 3. AutoType功能使用介绍

### 3.1 序列化时带上类型信息
如果需要序列化时带上类型信息，需要使用JSONWriter.Feature.WriteClassName。比如：
```java
Bean bean = ...;
String jsonString = JSON.toJSONString(bean, JSONWriter.Feature.WriteClassName);
```

很多时候，root对象是可以知道类型的，里面的对象字段是基类或者不确定类型，这个时候不输出root对象的类型信息，可以减少序列化结果的大小，也能提升反序列化的性能。
```java
Bean bean = ...;
String jsonString = JSON.toJSONString(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.NotWriteRootClassName);
```


### 3.2 反序列化时打开AutoType功能以支持自动类型
```java
Bean bean = (Bean) JSON.parseObject(jsonString, Object.class, JSONReader.Feature.SupportAutoType);
```

## 4. 配置safeMode
配置SafeMode会完全禁用AutoType功能，如果程序中显示指定类型，AutoType功能也不会生效。
### 4.1 JVM启动参数配置
```
-Dfastjson2.parser.safeMode=true
```

## 5. 使用AutoTypeFilter在不打开AutoTypeSupport时实现自动类型
当打开AutoTypeSupport，虽然内置了一个比较广泛的黑名单，但仍然是不够安全的。下面有一种办法是控制当前调用的AutoType支持范围，避免全局打开，这个更安全。
```java
public class FastJsonRedisSerializer<T> implements RedisSerializer<T> {
    static final Filter autoTypeFilter = JSONReader.autoTypeFilter(
            // 按需加上需要支持自动类型的类名前缀，范围越小越安全
            "org.springframework.security.core.authority.SimpleGrantedAuthority"
    );

    private Class<T> clazz;

    public FastJsonRedisSerializer(Class<T> clazz) {
        super();
        this.clazz = clazz;
    }

    @Override
    public byte[] serialize(T t) {
        if (t == null) {
            return new byte[0];
        }
        return JSON.toJSONBytes(t, JSONWriter.Feature.WriteClassName);
    }

    @Override
    public T deserialize(byte[] bytes) {
        if (bytes == null || bytes.length <= 0) {
            return null;
        }

        return JSON.parseObject(bytes, clazz, autoTypeFilter);
    }
}
```
