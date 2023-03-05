JDK 17中提供了[vector api](https://openjdk.org/jeps/426)，可以用SIMD来优化性能。

fastjson 2.0.24中已经支持vector api，这个优化目前处于incubator状态，需要通过如下方法打开：

加上依赖
```xml
<dependency>
    <groupId>com.alibaba.fastjson2</groupId>
    <artifactId>fastjson2-incubator-vector</artifactId>
    <version>2.0.24</version>
</dependency>
```

JVM启动参数加上
```shell
--add-modules=jdk.incubator.vector
```
