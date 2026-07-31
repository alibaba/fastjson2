`JSONReader` is the underlying implementation for reading JSON data in fastjson2. To facilitate different types of input, it provides the following implementations:

*   **JSONReaderUTF16**: Used to process input that is a `char[]`. Under JDK 8, a `String` is converted to a `char[]` and processed using the `JSONReaderUTF16` implementation. On JDK 9 or higher, if `coder=1`, this implementation is also used.
*   **JSONReaderUTF8**: Used to process input that is a UTF-8 encoded `byte[]`.
*   **JSONReaderASCII**: A subclass of `JSONReaderUTF8`, used for the `coder=0` optimization in JDK 9 and later.

```java
class JSONReader { }

class JSONReaderUTF16 extends JSONReader {
    char[] chars;
}

class JSONReaderUTF8 extends JSONReader {
    byte[] bytes;
}

class JSONReaderASCII extends JSONReaderUTF8 { }
```
