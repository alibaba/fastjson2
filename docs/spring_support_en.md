# Integrate Fastjson2 in SpringFramework

# 0. Download

Fastjson2 adopts a multi-module structure design, and the support for frameworks such as SpringFramework is now
independent in the `extension` dependency.

`Maven`:

```xml
<dependency>
    <groupId>com.alibaba.fastjson2</groupId>
    <artifactId>fastjson2-extension-spring5</artifactId>
    <version>2.0.58</version>
</dependency>

or

<dependency>
    <groupId>com.alibaba.fastjson2</groupId>
    <artifactId>fastjson2-extension-spring6</artifactId>
    <version>2.0.58</version>
</dependency>
```

`Gradle`:

```groovy
dependencies {
    implementation 'com.alibaba.fastjson2:fastjson2-extension-spring5:2.0.58'
}

or

dependencies {
    implementation 'com.alibaba.fastjson2:fastjson2-extension-spring6:2.0.58'
}
```
> After version 2.0.23, in order to be compatible with Spring 5.x / 6.x, different versions are independently opened with different dependency packages.
> If using a version earlier than 2.0.23, please refer to:

`Maven`:

```xml
<dependency>
    <groupId>com.alibaba.fastjson2</groupId>
    <artifactId>fastjson2-extension</artifactId>
    <version>2.0.x</version>
</dependency>
```

`Gradle`:

```groovy
dependencies {
    implementation 'com.alibaba.fastjson2:fastjson2-extension:2.0.x'
}
```

# 1. Configuration

Fastjson2 has redesigned the behavior of serialization and deserialization, so `FastJsonConfig` will also be re-adapted.

**Package**: `com.alibaba.fastjson2.support.config.FastJsonConfig`

**Attributes**:

param | Type | Desc
---- | ---- | ----
charset | Charset | The specified character set, default UTF-8
dateFormat | String | The specified date format, default yyyy-MM-dd HH:mm:ss
writerFilters | Filter[] | Configure serialization filters
writerFeatures | JSONWriter.Feature[] | Configure the specified behavior of serialization. For more configuration, see [Features](features_en.md)
readerFilters | Filter[] | Configure deserialization filters
readerFeatures | JSONReader.Feature[] | Configure the specified behavior of deserialization. For more configuration, see [Features](features_en.md)
jsonb | boolean | Use JSONB for serialization and deserialization, the default is false
symbolTable | JSONB.SymbolTable | JSONB serialization and deserialization symbol table, only valid when using JSONB

# 2. Integrate Fastjson2 in Spring Web MVC

In Fastjson2, `FastJsonHttpMessageConverter` and `FastJsonJsonView` can also be used to provide a better performance
experience for Web applications built with Spring MVC.

## 2. Configure FastJsonHttpMessageConverter

```java
@Configuration
public class WebMvcConfigurer extends WebMvcConfigurationSupport {

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        FastJsonHttpMessageConverter converter = new FastJsonHttpMessageConverter();
        FastJsonConfig config = new FastJsonConfig();
        config.setDateFormat("yyyy-MM-dd HH:mm:ss");
        config.setCharset(StandardCharsets.UTF_8);
        
        converter.setFastJsonConfig(config);
        // From version 2.0.59, FastJsonHttpMessageConverter default charset is already UTF-8, no need to set manually
        // converter.setDefaultCharset(StandardCharsets.UTF_8);
        converters.add(0, converter);
    }
}
```

## 2.2  Spring Web MVC View

Use `FastJsonJsonView` to set Spring MVC's default view model resolver to improve the speed
of `@Controller` `@ResponseBody` `ModelAndView` JSON serialization.

**Package**: `com.alibaba.fastjson2.support.spring.webservlet.view.FastJsonJsonView`

**Before Spring 5 Example**:

```java

@Configuration
@EnableWebMvc
public class WebMvcConfigurer extends WebMvcConfigurerAdapter {

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        FastJsonJsonView fastJsonJsonView = new FastJsonJsonView();
        //custom configuration...
        //FastJsonConfig config = new FastJsonConfig();
        //config.set...
        //fastJsonJsonView.setFastJsonConfig(config);
        registry.enableContentNegotiation(fastJsonJsonView);
    }
}
```

Starting from Spring 5.0, `WebMvcConfigurerAdapter` has been deprecated, you can directly implement the `WebMvcConfigurer` interface without using this adapter.
**After Spring 5 Example**:

```java

@Configuration
@EnableWebMvc
public class CustomWebMvcConfigurer implements WebMvcConfigurer {

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        FastJsonJsonView fastJsonJsonView = new FastJsonJsonView();
        //custom configuration...
        //FastJsonConfig config = new FastJsonConfig();
        //config.set...
        //fastJsonJsonView.setFastJsonConfig(config);
        registry.enableContentNegotiation(fastJsonJsonView);
    }

}
```

> Reference: Spring Framework official documentation Spring Web MVC section, [For more configuration](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-config).

# 3. Integrate Fastjson2 in Spring Webflux

Fastjson2 provides support for reactive codec in Spring, to replace Spring's default Decoder and Encoder with Fastjson2Decoder and Fastjson2Encoder.

**Packages**:

- `com.alibaba.fastjson2.support.spring.codec.Fastjson2Decoder`
- `com.alibaba.fastjson2.support.spring.codec.Fastjson2Encoder`

**Spring6 Packages**:

- `com.alibaba.fastjson2.support.spring6.codec.Fastjson2Decoder`
- `com.alibaba.fastjson2.support.spring6.codec.Fastjson2Encoder`

**Example1**:

```java
@Configuration
public class CustomWebfluxConfigure implements WebFluxConfigurer {

    @Override
    public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
        //configurer.registerDefaults(false);
        CodecConfigurer.CustomCodecs customCodecs = configurer.customCodecs();
        //自定义配置
        FastJsonConfig config = new FastJsonConfig();
        config.setCharset(StandardCharsets.UTF_8);
        config.setDateFormat("yyyy-MM-dd HH:mm:ss");
        //设置支持的媒体类型
        MimeType[] supportedMimeTypes = new MimeType[]{MediaType.APPLICATION_JSON};
        //注册解码器
        customCodecs.register(new Fastjson2Decoder(config,supportedMimeTypes));
        //注册编码器
        customCodecs.register(new Fastjson2Encoder(config,supportedMimeTypes));
    }
}
```

**Example2**:

```java
@Bean
public CodecCustomizer codecCustomizer() {
    return configurer -> {
        //configurer.registerDefaults(false);
        CodecConfigurer.CustomCodecs customCodecs = configurer.customCodecs();
        //自定义配置
        FastJsonConfig config = new FastJsonConfig();
        config.setCharset(StandardCharsets.UTF_8);
        config.setDateFormat("yyyy-MM-dd HH:mm:ss");
        //设置支持的媒体类型
        MimeType[] supportedMimeTypes = new MimeType[]{MediaType.APPLICATION_JSON};
        //注册解码器
        customCodecs.register(new Fastjson2Decoder(config, supportedMimeTypes));
        //注册编码器
        customCodecs.register(new Fastjson2Encoder(config, supportedMimeTypes));
    };
}
```

# 4. Integrate Fastjson2 in Spring Web Socket

In Fastjson2, Spring WebSocket is also supported, which can be configured using `FastjsonSockJsMessageCodec`.

**Package**: `com.alibaba.fastjson2.support.spring.websocket.sockjs.FastjsonSockJsMessageCodec`

**Example**:

```java

@Component
@EnableWebSocket
public class WebSocketConfig extends WebMvcConfigurerAdapter implements WebSocketConfigurer {

    @Resource
    WebSocketHandler handler;

    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        //custom configuration...
        //FastjsonSockJsMessageCodec messageCodec = new FastjsonSockJsMessageCodec();
        //FastJsonConfig config = new FastJsonConfig();
        //config.set...
        //messageCodec.setFastJsonConfig(config);
        registry.addHandler(handler, "/sockjs").withSockJS().setMessageCodec(new FastjsonSockJsMessageCodec());
    }

}
```

> Reference: Spring Framework official documentation Spring Web Socket section, [For more configuration](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#websocket).

# 5. Integrate Fastjson2 in Spring Data Redis

In Fastjson2, you can also use `GenericFastJsonRedisSerializer` or `FastJsonRedisSerializer` to provide a better
performance experience for Spring Data Redis.

## 5.1 Generic Redis Serializer

Use `GenericFastJsonRedisSerializer` as the `RedisSerializer` of `RedisTemplate` to improve JSON serialization and
deserialization speed.

**Package**: `com.alibaba.fastjson2.support.spring.data.redis.GenericFastJsonRedisSerializer`

**Example**:

```java

@Configuration
public class RedisConfiguration {

    @Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate redisTemplate = new RedisTemplate();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        GenericFastJsonRedisSerializer fastJsonRedisSerializer = new GenericFastJsonRedisSerializer();
        redisTemplate.setDefaultSerializer(fastJsonRedisSerializer);//Set the default Serialize, including keySerializer & valueSerializer

        //redisTemplate.setKeySerializer(fastJsonRedisSerializer);//Only set keySerializer
        //redisTemplate.setValueSerializer(fastJsonRedisSerializer);//Only set valueSerializer
        return redisTemplate;
    }
}
```

## 5.2 Customized Redis Serializer

Usually, `GenericFastJsonRedisSerializer` can be used for most scenarios. If you want to define a specific type
of `RedisTemplate`, you can use `FastJsonRedisSerializer` instead of `GenericFastJsonRedisSerializer` , the
configuration is similar.

**Package**: `com.alibaba.fastjson2.support.spring.data.redis.FastJsonRedisSerializer`

**Example**:

```java

@Configuration
public class RedisConfiguration {

    @Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate redisTemplate = new RedisTemplate();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        FastJsonRedisSerializer fastJsonRedisSerializer = new FastJsonRedisSerializer(User.class);
        redisTemplate.setDefaultSerializer(fastJsonRedisSerializer);
        return redisTemplate;
    }
}

```

## 5.3 JSONB Redis Serializer

If you plan to use JSONB as an object serialization/deserialization method and have higher serialization speed
requirements, you can configure the `jsonb` parameter, which is a new support in fastjson 2.0.6, and the configuration
is also very simple.

**Example**:

```java

@Configuration
public class RedisConfiguration {

    @Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate redisTemplate = new RedisTemplate();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        // GenericFastJsonRedisSerializer use jsonb
        // GenericFastJsonRedisSerializer fastJsonRedisSerializer = new GenericFastJsonRedisSerializer(true);

        // FastJsonRedisSerializer use jsonb
        FastJsonRedisSerializer fastJsonRedisSerializer = new FastJsonRedisSerializer(User.class);
        // FastJsonConfig fastJsonConfig = new FastJsonConfig();
        // fastJsonConfig.setJSONB(true);
        // fastJsonRedisSerializer.setFastJsonConfig(fastJsonConfig);
        redisTemplate.setDefaultSerializer(fastJsonRedisSerializer);

        return redisTemplate;
    }
}

```

> Reference: Spring Data Redis official documentation, [For more configuration](https://docs.spring.io/spring-data/redis/docs/current/reference/html/).

# 6. Integrate Fastjson2 in Spring Messaging

In Fastjson2, you can use `MappingFastJsonMessageConverter` to provide a better performance experience for Spring
Messaging.

## 6.1 JSON Message Converter

Use `MappingFastJsonMessageConverter` as Spring Cloud Stream or Spring Messaging to speed up message serialization and
deserialization.

**Package**: `com.alibaba.fastjson2.support.spring.messaging.converter.MappingFastJsonMessageConverter`

**Example**:

```java

@Configuration
public class StreamConfiguration {

    @Bean
    @StreamMessageConverter
    public MappingFastJsonMessageConverter messageConverter() {
        return new MappingFastJsonMessageConverter();
    }
}

```

## 6.2 JSONB Message Converter

If you plan to use JSONB as the object serialization/deserialization method and have high requirements on serialization
speed, you can configure the `jsonb` parameter of `FastJsonConfig`, which is a new support in fastjson 2.0.6 version ,
the configuration is also very simple.

_Note: JSONB only supports serializing the payload of Message to byte[]_

**Example**:

```java

@Configuration
public class StreamConfiguration {

    @Bean
    @StreamMessageConverter
    public MappingFastJsonMessageConverter messageConverter() {
        MappingFastJsonMessageConverter messageConverter = new MappingFastJsonMessageConverter();
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        fastJsonConfig.setJSONB(true); // use jsonb
        messageConverter.setFastJsonConfig(fastJsonConfig);
        return messageConverter;
    }
}

```

> Reference: Spring Messaging official documentation, [For more configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/messaging.html#messaging).
