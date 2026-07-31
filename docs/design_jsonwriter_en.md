`JSONWriter` is the underlying implementation for serialization in fastjson2. For the two scenarios of `toJSONString` and `toJSONBytes`, the `JSONWriterUTF8` and `JSONWriterUTF16` implementations are used.

*   **JSONWriterUTF16**: When `JSON.toJSONString` is used, `JSONWriterUTF16` is used by default.
*   **JSONWriterUTF8**: When `JSON.toJSONBytes` is used, `JSONWriterUTF8` is used by default. The `JSONWriterUTF8` implementation is also used when `JSON.toJSONString` is combined with `JSONWriter.Feature.OptimizedForAscii`.

```java
class JSONWriter { }

class JSONWriterUTF8 extends JSONWriter { }

class JSONWriterUTF16 extends JSONWriter { }
```
