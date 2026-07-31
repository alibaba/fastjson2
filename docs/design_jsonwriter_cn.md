
JSONWriter是fastjson2序列化的底层实现，针对toJSONString和toJSONBytes两种场景，会使用JSONWriterUTF8和JSONWriterUTF16两种实现。

* JSONWriterUTF16 当使用JSON.toJSONString时，缺省使用JSONWriterUTF16。
* JSONWriterUTF8 当使用JSON.toJSONBytes时，缺省使用JSONWriterUTF8，在使用JSON.toJSONString结合JSONWriter.Feature.OptimizedForAscii使用时，也会用JSONWriterUTF8实现。

```java
class JSONWriter { }

class JSONWriterUTF8 extends JSONWriter { }

class JSONWriterUTF16 extends JSONWriter { }
```
