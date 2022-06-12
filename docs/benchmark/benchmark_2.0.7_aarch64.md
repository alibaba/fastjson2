# 1. 测试环境
* MacOS Monterey Version 12.4(21F79)
* Chip Apple M1 Max
* Memory 64 GB

## 1.1 JDK
* JDK 8 zulu8.60.0.21-ca-jdk8.0.322-macosx_aarch64
* JDK 11 zulu11.52.13-ca-jdk11.0.13-macosx_aarch64
* JDK 17 zulu17.32.13-ca-jdk17.0.2-macosx_aarch64
* JMH 1.35

## 1.2 JSON库的版本
* Jackson 2.13.3
* FASTJSON 1.2.83
* [FASTJSON 2.0.7](https://github.com/alibaba/fastjson2/releases/tag/2.0.7)
* Hessian 4.0.66

## 1.3 测试代码
* https://github.com/alibaba/fastjson2/tree/2.0.7/benchmark/src/main/java/com/alibaba/fastjson2/benchmark/eishay

# 2. Parse String to Tree
这个场景是将字符串解析为JSONObject或者HashMap，不涉及绑定JavaBean对象。
## JDK 8
JDK 8环境下FASTJSON2比FASTJSON1快96.46%，比Jackson快74.59%。
```java
Benchmark                         Mode  Cnt     Score   Error   Units
EishayParseTreeString.fastjson1  thrpt    5   689.334 ± 4.869  ops/ms
EishayParseTreeString.fastjson2  thrpt    5  1354.274 ± 5.955  ops/ms
EishayParseTreeString.jackson    thrpt    5   775.679 ± 3.524  ops/ms
```

## JDK 11
JDK 11环境下FASTJSON2比FASTJSON1快89.11%，比Jackson快45.49%。
```java
Benchmark                         Mode  Cnt     Score   Error   Units
EishayParseTreeString.fastjson1  thrpt    5   583.254 ± 3.813  ops/ms
EishayParseTreeString.fastjson2  thrpt    5  1103.024 ± 2.497  ops/ms
EishayParseTreeString.jackson    thrpt    5   758.132 ± 4.719  ops/ms
```

## JDK 17
JDK 11环境下FASTJSON2比FASTJSON1快48.40%，比Jackson快60.37%。
```java
Benchmark                         Mode  Cnt     Score    Error   Units
EishayParseTreeString.fastjson1  thrpt    5   868.541 ±  2.946  ops/ms
EishayParseTreeString.fastjson2  thrpt    5  1289.000 ± 11.511  ops/ms
EishayParseTreeString.jackson    thrpt    5   803.867 ±  4.779  ops/ms
```

# 3. Pars String to Java Bean
这个场景是将一个没有格式化的JSON字符串反序列化为JavaBean对象，是最常用的场景，这个是fastjson1的强项。
## JDK 8
JDK 8环境下FASTJSON2比FASTJSON1快18.5%，比Jackson快134.6%。
```java
Benchmark                     Mode  Cnt     Score    Error   Units
EishayParseString.fastjson1  thrpt    5  1539.912 ± 13.148  ops/ms
EishayParseString.fastjson2  thrpt    5  1825.665 ±  9.912  ops/ms
EishayParseString.jackson    thrpt    5   777.996 ±  3.173  ops/ms
```

## JDK 11
JDK 11环境下FASTJSON 2比FASTJSON 1快38%，比Jackson快170.6%。
```java
Benchmark                     Mode  Cnt     Score    Error   Units
EishayParseString.fastjson1  thrpt    5  1483.601 ±  7.487  ops/ms
EishayParseString.fastjson2  thrpt    5  2047.763 ± 18.252  ops/ms
EishayParseString.jackson    thrpt    5   756.721 ±  3.526  ops/ms
```

## JDK 17
JDK 17环境下FASTJSON 2和FASTJSON 1性能很接近，比Jackson快172.8%。 这个场景FASTJSON 2没有比FASTJSON 1性能接近的原因有两个：
1. 这个FASTJSON 1的最强场景
2. 由于JDK 17的一些限制使得在不加特别参数时，FASTJSON 2对String.value字段直接访问的优化手段无法使用限制了性能进一步提升
```java
Benchmark                     Mode  Cnt     Score   Error   Units
EishayParseString.fastjson1  thrpt    5  2074.488 ± 7.897  ops/ms
EishayParseString.fastjson2  thrpt    5  2089.508 ± 4.307  ops/ms
EishayParseString.jackson    thrpt    5   765.751 ± 6.288  ops/ms
```

# 4. Parse UTF8 Bytes to Java Bean
将UTF8编码的Bytes反序列化为JavaBean，这个场景在网络传输和缓存场景常用
## JDK8
JDK 8环境下FASTJSON 2比FASTJSON 1快20.96%，比Jackson快65.03%。
```java
Benchmark                        Mode  Cnt     Score    Error   Units
EishayParseUTF8Bytes.fastjson1  thrpt    5  1444.714 ± 11.622  ops/ms
EishayParseUTF8Bytes.fastjson2  thrpt    5  1747.637 ± 13.112  ops/ms
EishayParseUTF8Bytes.jackson    thrpt    5  1058.978 ±  9.580  ops/ms
```

## JDK 11
JDK 11环境下FASTJSON 2比FASTJSON 1快39.82%，比Jackson快96.81%。
```java
Benchmark                        Mode  Cnt     Score    Error   Units
EishayParseUTF8Bytes.fastjson1  thrpt    5  1338.422 ± 10.127  ops/ms
EishayParseUTF8Bytes.fastjson2  thrpt    5  1871.489 ± 42.011  ops/ms
EishayParseUTF8Bytes.jackson    thrpt    5   950.886 ± 10.520  ops/ms
```

## JDK 17
JDK 17环境下FASTJSON 2比FASTJSON 1快8.92%，比Jackson快98.07%。
```java
Benchmark                        Mode  Cnt     Score    Error   Units
EishayParseUTF8Bytes.fastjson1  thrpt    5  1699.103 ±  4.741  ops/ms
EishayParseUTF8Bytes.fastjson2  thrpt    5  1850.714 ± 10.967  ops/ms
EishayParseUTF8Bytes.jackson    thrpt    5   934.355 ± 10.108  ops/ms
```

# 5. Parse UTF8 Bytes to JSONObject
将带有空格缩进的格式化的UTF8编码的Bytes反序列化为JSONObject/HashMap，不涉及绑定JavaBean对象。

## JDK 8
JDK 8环境下FASTJSON 2比FASTJSON 1快64.86%，比Jackson快21.24%。
```java
Benchmark                            Mode  Cnt     Score   Error   Units
EishayParseTreeUTF8Bytes.fastjson1  thrpt    5   667.564 ± 4.341  ops/ms
EishayParseTreeUTF8Bytes.fastjson2  thrpt    5  1100.551 ± 5.987  ops/ms
EishayParseTreeUTF8Bytes.jackson    thrpt    5   907.719 ± 4.987  ops/ms
```

## JDK 11
JDK 11环境下FASTJSON 2比FASTJSON 1快84.03%，比Jackson快14%。
```java
Benchmark                            Mode  Cnt    Score    Error   Units
EishayParseTreeUTF8Bytes.fastjson1  thrpt    5  540.954 ±  1.934  ops/ms
EishayParseTreeUTF8Bytes.fastjson2  thrpt    5  995.549 ± 14.924  ops/ms
EishayParseTreeUTF8Bytes.jackson    thrpt    5  873.258 ± 12.942  ops/ms
```

## JDK 17
JDK 11环境下FASTJSON 2比FASTJSON 1快39.62%，比Jackson快6.47%。
```java
Benchmark                            Mode  Cnt     Score    Error   Units
EishayParseTreeUTF8Bytes.fastjson1  thrpt    5   737.135 ±  5.644  ops/ms
EishayParseTreeUTF8Bytes.fastjson2  thrpt    5  1029.244 ±  3.325  ops/ms
EishayParseTreeUTF8Bytes.jackson    thrpt    5   966.648 ± 18.829  ops/ms
```

# 6. Parse Pretty UTF8 Bytes to Java Bean
将带有空格缩进的格式化的UTF8编码的Bytes反序列化为JavaBean对象。
## JDK 8
JDK 8环境下FASTJSON 2比FASTJSON 1快61.62%，比Jackson快43.83%。
```java
Benchmark                               Mode  Cnt     Score   Error   Units
EishayParseTreeStringPretty.fastjson1  thrpt    5   639.783 ± 4.227  ops/ms
EishayParseTreeStringPretty.fastjson2  thrpt    5  1031.769 ± 6.524  ops/ms
EishayParseTreeStringPretty.jackson    thrpt    5   717.353 ± 3.492  ops/ms
```

## JDK 11
JDK 11环境下FASTJSON 2比FASTJSON 1快99.12%，比Jackson快49.51%。
```java
Benchmark                               Mode  Cnt    Score    Error   Units
EishayParseTreeStringPretty.fastjson1  thrpt    5  485.915 ±  1.206  ops/ms
EishayParseTreeStringPretty.fastjson2  thrpt    5  967.587 ±  3.667  ops/ms
EishayParseTreeStringPretty.jackson    thrpt    5  647.169 ± 74.370  ops/ms
```

## JDK 17
JDK 17环境下FASTJSON 2比FASTJSON 1快52.29%，比Jackson快54.94%。
```java
Benchmark                               Mode  Cnt     Score     Error   Units
EishayParseTreeStringPretty.fastjson1  thrpt    5   734.173 ±   4.344  ops/ms
EishayParseTreeStringPretty.fastjson2  thrpt    5  1132.815 ± 111.454  ops/ms
EishayParseTreeStringPretty.jackson    thrpt    5   731.110 ±   4.889  ops/ms
```

# 7. Write JavaBean to String
这个场景是将JavaBean对象序列化为字符串
## JDK 8
JDK 11环境下FASTJSON 2比FASTJSON 1快153.68%，比Jackson快68.52%。
```java
Benchmark                     Mode  Cnt     Score    Error   Units
EishayWriteString.fastjson1  thrpt    5  1116.513 ±  9.060  ops/ms
EishayWriteString.fastjson2  thrpt    5  2832.451 ± 32.996  ops/ms
EishayWriteString.jackson    thrpt    5  1680.735 ± 20.092  ops/ms
```

## JDK 11
JDK 11环境下FASTJSON 2比FASTJSON 1快160%，比Jackson快62.99%。
```java
Benchmark                     Mode  Cnt     Score    Error   Units
EishayWriteString.fastjson1  thrpt    5  1074.701 ±  6.658  ops/ms
EishayWriteString.fastjson2  thrpt    5  2794.764 ± 23.689  ops/ms
EishayWriteString.jackson    thrpt    5  1714.676 ± 20.331  ops/ms
```

## JDK 17
JDK 11环境下FASTJSON 2比FASTJSON 1快124.52%，比Jackson快61.14%。
```java
Benchmark                     Mode  Cnt     Score    Error   Units
EishayWriteString.fastjson1  thrpt    5  1247.903 ±  8.185  ops/ms
EishayWriteString.fastjson2  thrpt    5  2801.838 ± 25.323  ops/ms
EishayWriteString.jackson    thrpt    5  1738.689 ± 10.372  ops/ms
```

# 8. Write JavaBean to UTF8 Bytes
这个场景是将JavaBean对象序列化为UTF8编码的Bytes
## JDK 8
JDK 8环境下FASTJSON 2比FASTJSON 1快78.42%，比Jackson快81.13%。
```java
Benchmark                        Mode  Cnt     Score    Error   Units
EishayWriteUTF8Bytes.fastjson1  thrpt    5   976.186 ±  5.080  ops/ms
EishayWriteUTF8Bytes.fastjson2  thrpt    5  2717.983 ± 18.739  ops/ms
EishayWriteUTF8Bytes.jackson    thrpt    5  1500.564 ± 12.812  ops/ms
```

## JDK 11
JDK 11环境下FASTJSON 2比FASTJSON 1快201.67%，比Jackson快110.46%。
```java
Benchmark                        Mode  Cnt     Score    Error   Units
EishayWriteUTF8Bytes.fastjson1  thrpt    5   961.756 ±  9.957  ops/ms
EishayWriteUTF8Bytes.fastjson2  thrpt    5  2901.339 ±  9.204  ops/ms
EishayWriteUTF8Bytes.jackson    thrpt    5  1378.505 ± 14.243  ops/ms
```

## JDK 17
JDK 17环境下FASTJSON 2比FASTJSON 1快207.22%，比Jackson快84.05%。
```java
Benchmark                        Mode  Cnt     Score    Error   Units
EishayWriteUTF8Bytes.fastjson1  thrpt    5   952.709 ±  8.649  ops/ms
EishayWriteUTF8Bytes.fastjson2  thrpt    5  2926.923 ±  5.526  ops/ms
EishayWriteUTF8Bytes.jackson    thrpt    5  1590.268 ± 12.166  ops/ms
```

# 9. Write JavaBean to Binary
这个是序列化的场景，将JavaBean序列化为二进制格式，用于缓存和网络传输。这个场景可以看出JSONB的极速性能。
## JDK 8
FASTJSON2 JSONB比快425.14%，比java内置序列化快672.31%
```java
Benchmark                              Mode  Cnt     Score    Error   Units
EishayWriteBinary.fastjson2JSONB      thrpt    5  3355.913 ± 22.156  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes  thrpt    5  2719.407 ±  9.733  ops/ms
EishayWriteBinary.hessian             thrpt    5   639.043 ±  5.000  ops/ms
EishayWriteBinary.javaSerialize       thrpt    5   434.528 ±  2.837  ops/ms
```

## JDK 11
```java
Benchmark                              Mode  Cnt     Score    Error   Units
EishayWriteBinary.fastjson2JSONB      thrpt    5  4608.308 ± 54.115  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes  thrpt    5  2902.881 ± 15.651  ops/ms
EishayWriteBinary.hessian             thrpt    5   727.097 ± 21.115  ops/ms
EishayWriteBinary.javaSerialize       thrpt    5   447.319 ±  0.102  ops/ms
```

## JDK 17
```java
Benchmark                              Mode  Cnt     Score    Error   Units
EishayWriteBinary.fastjson2JSONB      thrpt    5  4489.651 ± 15.350  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes  thrpt    5  2925.373 ±  9.413  ops/ms
EishayWriteBinary.hessian             thrpt    5   711.496 ±  4.984  ops/ms
EishayWriteBinary.javaSerialize       thrpt    5   453.639 ±  4.727  ops/ms
```

# 10. Read Binary to JavaBean
这个是序列化的场景，将二进制的byte数组反序列化为JavaBean，用于缓存和网络传输。这个场景可以看出JSONB的极速性能。
## JDK 8
FASTJSON2 JSONB比快731.28%，比java内置序列化快5139.68%
```java
Benchmark                              Mode  Cnt     Score    Error   Units
EishayParseBinary.fastjson2JSONB      thrpt    5  3130.576 ± 23.829  ops/ms
EishayParseBinary.fastjson2UTF8Bytes  thrpt    5  1775.479 ±  9.215  ops/ms
EishayParseBinary.hessian             thrpt    5   376.593 ±  1.423  ops/ms
EishayParseBinary.javaSerialize       thrpt    5    60.898 ±  0.200  ops/ms
```

## JDK 11
```java
Benchmark                              Mode  Cnt     Score    Error   Units
EishayParseBinary.fastjson2JSONB      thrpt    5  2443.326 ± 11.017  ops/ms
EishayParseBinary.fastjson2UTF8Bytes  thrpt    5  1889.431 ± 15.771  ops/ms
EishayParseBinary.hessian             thrpt    5   361.239 ±  2.726  ops/ms
EishayParseBinary.javaSerialize       thrpt    5    61.553 ±  0.097  ops/ms
```

## JDK 17
```java
Benchmark                              Mode  Cnt     Score    Error   Units
EishayParseBinary.fastjson2JSONB      thrpt    5  2525.308 ± 19.115  ops/ms
EishayParseBinary.fastjson2UTF8Bytes  thrpt    5  1770.374 ±  5.918  ops/ms
EishayParseBinary.hessian             thrpt    5   361.265 ±  1.357  ops/ms
EishayParseBinary.javaSerialize       thrpt    5    71.450 ±  0.625  ops/ms
```
