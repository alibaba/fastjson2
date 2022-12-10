# 在 Spring 中集成 Fastjson2

# 0. 依赖配置

Fastjson2采用多module的结构设计，对SpringFramework等框架的支持现独立在`extension`包中。

`Maven`:

```xml

<dependency>
    <groupId>com.alibaba.fastjson2</groupId>
    <artifactId>fastjson2-extension</artifactId>
    <version>2.0.21</version>
</dependency>
```

`Gradle`:

```groovy
dependencies {
    implementation 'com.alibaba.fastjson2:fastjson2-extension:2.0.21'
}
```

# 1. 参数配置

Fastjson2对于序列化和反序列化的行为进行了重新设计，所以`FastJsonConfig`也会重新适配。

**Package**: `com.alibaba.fastjson2.support.config.FastJsonConfig`

**Attributes**:

参数 | 类型 | 描述
---- | ---- | ----
charset | Charset | 指定的字符集，默认UTF-8
dateFormat | String | 指定的日期格式，默认yyyy-MM-dd HH:mm:ss
writerFilters | Filter[] | 配置序列化过滤器
writerFeatures | JSONWriter.Feature[] | 配置序列化的指定行为，更多配置请见：[Features](features_cn.md)
readerFilters | Filter[] | 配置反序列化过滤器
readerFeatures | JSONReader.Feature[] | 配置反序列化的指定行为，更多配置请见：[Features](features_cn.md)
jsonb | boolean | 是否采用JSONB进行序列化和反序列化，默认false
symbolTable | JSONB.SymbolTable | JSONB序列化和反序列化的符号表，只有使用JSONB时生效

# 2. 在 Spring Web MVC 中集成 Fastjson2

在Fastjson2中，同样可以使用`FastJsonHttpMessageConverter` 和 `FastJsonJsonView` 为 Spring MVC 构建的 Web 应用提供更好的性能体验。

## 2.1  Spring Web MVC Converter

使用 `FastJsonHttpMessageConverter` 来替换 Spring MVC 默认的 `HttpMessageConverter`
以提高 `@RestController` `@ResponseBody` `@RequestBody` 注解的 JSON序列化和反序列化速度。

**Package**: `com.alibaba.fastjson2.support.spring.http.converter.FastJsonHttpMessageConverter`

**Example**:

```java

@Configuration
@EnableWebMvc
public class WebMvcConfigurer extends WebMvcConfigurerAdapter {

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        FastJsonHttpMessageConverter converter = new FastJsonHttpMessageConverter();
        //自定义配置...
        FastJsonConfig config = new FastJsonConfig();
        config.setDateFormat("yyyy-MM-dd HH:mm:ss");
        config.setReaderFeatures(JSONReader.Feature.FieldBased, JSONReader.Feature.SupportArrayToBean);
        config.setWriterFeatures(JSONWriter.Feature.WriteMapNullValue, JSONWriter.Feature.PrettyFormat);
        converter.setFastJsonConfig(config);
        converter.setDefaultCharset(StandardCharsets.UTF_8);
        converter.setSupportedMediaTypes(Collections.singletonList(MediaType.APPLICATION_JSON));
        converters.add(0, converter);
    }
}
```

## 2.2  Spring Web MVC View

使用 `FastJsonJsonView` 来设置 Spring MVC 默认的视图模型解析器，以提高 `@Controller` `@ResponseBody` `ModelAndView` JSON序列化速度。

**Package**: `com.alibaba.fastjson2.support.spring.webservlet.view.FastJsonJsonView`

**Example**:

```java

@Configuration
@EnableWebMvc
public class WebMvcConfigurer extends WebMvcConfigurerAdapter {

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        FastJsonJsonView fastJsonJsonView = new FastJsonJsonView();
        //自定义配置...
        //FastJsonConfig config = new FastJsonConfig();
        //config.set...
        //fastJsonJsonView.setFastJsonConfig(config);
        registry.enableContentNegotiation(fastJsonJsonView);
    }
}
```

> 参考：Spring Framework 官方文档 Spring Web MVC 部分，[查看更多](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-config) 。

# 3. 在 Spring Web Socket 中集成 Fastjson2

在Fastjson2中，同样也对 Spring WebSocket 给予支持，可以使用 `FastjsonSockJsMessageCodec` 进行配置。

**Package**: `com.alibaba.fastjson2.support.spring.websocket.sockjs.FastjsonSockJsMessageCodec`

**Example**:

```java

@Component
@EnableWebSocket
public class WebSocketConfig extends WebMvcConfigurerAdapter implements WebSocketConfigurer {

    @Resource
    WebSocketHandler handler;

    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        //自定义配置...
        //FastjsonSockJsMessageCodec messageCodec = new FastjsonSockJsMessageCodec();
        //FastJsonConfig config = new FastJsonConfig();
        //config.set...
        //messageCodec.setFastJsonConfig(config);
        registry.addHandler(handler, "/sockjs").withSockJS().setMessageCodec(new FastjsonSockJsMessageCodec());
    }

}
```

> 参考：Spring Framework 官方文档 Spring Web Socket 部分，[查看更多](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#websocket) 。

# 4. 在 Spring Data Redis 中集成 Fastjson2

在Fastjson2中，同样可以使用 `GenericFastJsonRedisSerializer` 或 `FastJsonRedisSerializer` 为 Spring Data Redis 提供更好的性能体验。

## 4.1 Generic Redis Serializer

使用 `GenericFastJsonRedisSerializer` 作为 `RedisTemplate` 的 `RedisSerializer` 来提升JSON序列化和反序列化速度。

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
        redisTemplate.setDefaultSerializer(fastJsonRedisSerializer);//设置默认的Serialize，包含 keySerializer & valueSerializer

        //redisTemplate.setKeySerializer(fastJsonRedisSerializer);//单独设置keySerializer
        //redisTemplate.setValueSerializer(fastJsonRedisSerializer);//单独设置valueSerializer
        return redisTemplate;
    }
}
```

## 4.2 Customized Redis Serializer

通常使用 `GenericFastJsonRedisSerializer` 即可满足大部分场景，如果你想定义特定类型专用的 `RedisTemplate` 可以使用 `FastJsonRedisSerializer`
来代替 `GenericFastJsonRedisSerializer` ，配置是类似的。

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

## 4.3 JSONB Redis Serializer

如果你准备使用 JSONB 作为对象序列/反序列化的方式并对序列化速度有较高的要求的话，可以对jsonb参数进行配置，该参数是 fastjson 2.0.6 版本中新增的支持，配置也很简单。

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

> 参考：Spring Data Redis 官方文档，[查看更多](https://docs.spring.io/spring-data/redis/docs/current/reference/html/) 。

# 5. 在 Spring Messaging 中集成 Fastjson2

在Fastjson2中，同样可以使用 `MappingFastJsonMessageConverter` 为 Spring Messaging 提供更好的性能体验。

## 5.1 JSON Message Converter

使用 `MappingFastJsonMessageConverter` 作为 Spring Cloud Stream 或 Spring Messaging 来提升Message的序列化和反序列化速度。

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

## 5.2 JSONB Message Converter

如果你准备使用 JSONB 作为对象序列/反序列化的方式并对序列化速度有较高的要求的话，可以对 `FastJsonConfig` 的 `jsonb` 参数进行配置，该参数是 fastjson 2.0.6 版本中新增的支持，配置也很简单。

_注意：JSONB仅支持将Message的payload序列化为byte[]_

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

> 参考：Spring Messaging 官方文档，[查看更多](https://docs.spring.io/spring-boot/docs/current/reference/html/messaging.html#messaging) 。
