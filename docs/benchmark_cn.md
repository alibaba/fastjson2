# 1. Eishay测试
```
                                   create     ser   deser   total   size  +dfl
json-array/fastjson/databind           30     455     414     869    284   171
protobuf                              159     601     319     919    242   152
cbor/jackson+afterburner/databind      29     565     753    1318    398   251
json/fastjson/databind                 29     659     707    1366    489   271
json/jackson+afterburner/databind      28     663     837    1500    488   271
cbor/jackson/databind                  30     587    1040    1628    398   251
json/jackson/databind                  29     697    1099    1796    488   271
json/gson/databind                     28    2091    1803    3893    489   268
hessian                                29    2062    2619    4681    504   319
java-built-in                          29    2815   15730   18545    892   520
```
* 测试代码 https://github.com/wenshao/jvm-serializers

# 2. 直接JMH性能测试比较
## 2.1 测试环境
* MacBook Pro (16-inch, 2021) MacApple M1 Max, 64GB Memory
* ZuluJDK

## 2.1.1 比较的版本
* Fastjson 2.0.1
* Fastjson 1.2.79
* Jackson 2.12.4

## 2.1.2 JDK版本
* JDK 8 zulu8.60.0.21-ca-jdk8.0.322-macosx_aarch64
* JDK 11 zulu11.54.25-ca-jdk11.0.14.1-macosx_aarch64
* JDK 17 zulu17.32.13-ca-jdk17.0.2-macosx_aarch64

## 2.2 Parse性能比较
* 测试代码 https://github.com/alibaba/fastjson2/blob/2.0.1/core/src/test/java/com/alibaba/fastjson_perf/eishay/EishayParse.java


### 2.2.1 场景介绍及结论
* EishayParseTreeString场景，将String解析成JSONObject/JSONArray或者HashMap/ArrayList。在这个场景，fastjson2表现出了两倍于fastjson1的性能
* EishayParseString场景，将String反序列化为JavaBean对象，在这个场景fastjson2相对于fastjson1性能提升了30%的性能。
* EishayParseStringPretty，将格式化带空格和换行符缩进的String反序列化为JavaBean对象，fastjson2在3.44倍于fastjson1。这个场景在fastjson1中是弱项，在fastjson2中采用新解析的算法，性能有了非常大提升。
* EishayParseUTF8Bytes，将UTF8格式的byte[]反序列化为JavaBean对象，

### 2.2.2 MacOS_M1Max_ARM_Zulu_JDK8

```
Benchmark                                  Mode  Cnt     Score     Error   Units
EishayParseTreeString.fastjson2           thrpt    5  1297.637 ±  14.343  ops/ms
EishayParseTreeString.fastjson1           thrpt    5   649.525 ±   2.845  ops/ms
EishayParseTreeString.jackson             thrpt    5   701.278 ±  17.552  ops/ms

EishayParseTreeUTF8Bytes.fastjson2        thrpt    5  1059.120 ±  17.679  ops/ms
EishayParseTreeUTF8Bytes.fastjson1        thrpt    5   654.592 ±   3.706  ops/ms
EishayParseTreeUTF8Bytes.jackson          thrpt    5   825.801 ±   3.161  ops/ms

EishayParseString.fastjson2               thrpt    5  2057.589 ±   9.382  ops/ms
EishayParseString.fastjson1               thrpt    5  1588.114 ±   4.540  ops/ms
EishayParseString.jackson                 thrpt    5   718.630 ±  14.570  ops/ms

EishayParseStringPretty.fastjson2         thrpt    5  1519.731 ± 108.501  ops/ms
EishayParseStringPretty.fastjson1         thrpt    5   441.860 ±  12.675  ops/ms
EishayParseStringPretty.jackson           thrpt    5   659.436 ±  14.518  ops/ms

EishayParseUTF8Bytes.fastjson2            thrpt    5  1580.093 ±  11.714  ops/ms
EishayParseUTF8Bytes.fastjson1            thrpt    5  1488.098 ±   7.587  ops/ms
EishayParseUTF8Bytes.jackson              thrpt    5   973.172 ±   4.252  ops/ms

EishayParseUTF8BytesPretty.fastjson2      thrpt    5  1623.723 ±   5.420  ops/ms
EishayParseUTF8BytesPretty.fastjson1      thrpt    5   434.529 ±   1.160  ops/ms
EishayParseUTF8BytesPretty.jackson        thrpt    5   861.123 ±   2.946  ops/ms
```

### 2.2.3 MacOS_M1Max_ARM_Zulu_JDK11

```
Benchmark                                  Mode  Cnt     Score     Error   Units
EishayParseTreeString.fastjson2           thrpt    5  1113.560 ±   2.853  ops/ms
EishayParseTreeString.fastjson1           thrpt    5   602.356 ±   3.139  ops/ms
EishayParseTreeString.jackson             thrpt    5   771.085 ±  56.116  ops/ms

EishayParseTreeUTF8Bytes.fastjson2        thrpt    5  1076.274 ±   4.028  ops/ms
EishayParseTreeUTF8Bytes.fastjson1        thrpt    5   549.811 ±   1.122  ops/ms
EishayParseTreeUTF8Bytes.jackson          thrpt    5   900.454 ±   4.688  ops/ms

EishayParseString.fastjson2               thrpt    5  2049.978 ±   8.870  ops/ms
EishayParseString.fastjson1               thrpt    5  1498.918 ±  18.825  ops/ms
EishayParseString.jackson                 thrpt    5   684.231 ±  10.414  ops/ms

EishayParseStringPretty.fastjson2         thrpt    5  1753.379 ±   7.210  ops/ms
EishayParseStringPretty.fastjson1         thrpt    5   379.946 ±   5.317  ops/ms
EishayParseStringPretty.jackson           thrpt    5   657.031 ±   7.039  ops/ms

EishayParseUTF8Bytes.fastjson2            thrpt    5  2000.253 ± 134.147  ops/ms
EishayParseUTF8Bytes.fastjson1            thrpt    5  1365.798 ±   6.996  ops/ms
EishayParseUTF8Bytes.jackson              thrpt    5   909.116 ±   2.941  ops/ms

EishayParseUTF8BytesPretty.fastjson2      thrpt    5  1630.228 ± 392.834  ops/ms
EishayParseUTF8BytesPretty.fastjson1      thrpt    5   367.705 ±   0.933  ops/ms
EishayParseUTF8BytesPretty.jackson        thrpt    5   883.384 ±   4.067  ops/ms
```

### 2.2.4 MacOS_M1Max_ARM_Zulu_JDK17

```
Benchmark                                  Mode  Cnt     Score     Error   Units
EishayParseTreeString.fastjson2           thrpt    5  1310.585 ±   4.254  ops/ms
EishayParseTreeString.fastjson1           thrpt    5   843.619 ±   3.138  ops/ms
EishayParseTreeString.jackson             thrpt    5   762.283 ±   2.127  ops/ms

EishayParseTreeUTF8Bytes.fastjson2        thrpt    5  1043.344 ±   6.560  ops/ms
EishayParseTreeUTF8Bytes.fastjson1        thrpt    5   768.128 ±   1.319  ops/ms
EishayParseTreeUTF8Bytes.jackson          thrpt    5   924.336 ±   3.618  ops/ms

EishayParseString.fastjson2               thrpt    5  2157.744 ±   7.418  ops/ms
EishayParseString.fastjson1               thrpt    5  2105.299 ±  11.867  ops/ms
EishayParseString.jackson                 thrpt    5   704.006 ±   6.809  ops/ms

EishayParseStringPretty.fastjson2         thrpt    5  1692.457 ±   7.409  ops/ms
EishayParseStringPretty.fastjson1         thrpt    5   499.127 ±  13.863  ops/ms
EishayParseStringPretty.jackson           thrpt    5   665.835 ±  11.313  ops/ms

EishayParseUTF8Bytes.fastjson2            thrpt    5  1473.672 ± 374.877  ops/ms
EishayParseUTF8Bytes.fastjson1            thrpt    5  1713.926 ±   4.349  ops/ms
EishayParseUTF8Bytes.jackson              thrpt    5   943.198 ±   3.717  ops/ms

EishayParseUTF8BytesPretty.fastjson2      thrpt    5  1297.958 ± 301.814  ops/ms
EishayParseUTF8BytesPretty.fastjson1      thrpt    5   460.399 ±   2.872  ops/ms
EishayParseUTF8BytesPretty.jackson        thrpt    5   832.620 ±   3.739  ops/ms
```

## 2.3 WriteString
* 测试代码 https://github.com/alibaba/fastjson2/blob/2.0.1/core/src/test/java/com/alibaba/fastjson_perf/eishay/EishayWrite.java

### 2.3.1 场景介绍及结论
* EishayWriteString场景，将JavaBean对象序列化为字符串。这个场景中，fastjson2比fastjson1和jackson分别有164%和85%的性能提升
* EishayWriteUTF8Bytes场景，将JavaBean对象序列化为UTF8格式的byte数组。这个场景中，fastjson2比fastjson1和jackson分别有185%和93%的性能提升

### 2.3.2 MacOS_M1Max_ARM_Zulu_JDK8

```
Benchmark                         Mode  Cnt     Score   Error   Units
EishayWriteString.fastjson2      thrpt    5  3076.439 ± 4.692  ops/ms
EishayWriteString.fastjson1      thrpt    5  1162.488 ± 9.076  ops/ms
EishayWriteString.jackson        thrpt    5  1664.961 ± 3.204  ops/ms

EishayWriteUTF8Bytes.fastjson2   thrpt    5  2868.597 ± 5.674  ops/ms
EishayWriteUTF8Bytes.fastjson1   thrpt    5  1005.945 ± 3.602  ops/ms
EishayWriteUTF8Bytes.jackson     thrpt    5  1481.545 ± 5.302  ops/ms
```

### 2.3.3 MacOS_M1Max_ARM_Zulu_JDK11

```
Benchmark                        Mode  Cnt     Score    Error   Units
EishayWriteString.fastjson2     thrpt    5  2816.795 ± 11.322  ops/ms
EishayWriteString.fastjson1     thrpt    5  1063.862 ±  7.016  ops/ms
EishayWriteString.jackson       thrpt    5  1617.422 ±  5.465  ops/ms

EishayWriteUTF8Bytes.fastjson2  thrpt    5  3051.648 ±  8.048  ops/ms
EishayWriteUTF8Bytes.fastjson1  thrpt    5  1033.902 ± 11.964  ops/ms
EishayWriteUTF8Bytes.jackson    thrpt    5  1381.491 ± 12.199  ops/ms
```

### 2.3.4 MacOS_M1Max_ARM_Zulu_JDK17

```
Benchmark                        Mode  Cnt     Score    Error   Units
EishayWriteString.fastjson2     thrpt    5  3064.658 ± 16.216  ops/ms
EishayWriteString.fastjson1     thrpt    5  1250.379 ±  6.057  ops/ms
EishayWriteString.jackson       thrpt    5  1682.306 ±  6.945  ops/ms

EishayWriteUTF8Bytes.fastjson2  thrpt    5  2895.860 ± 78.134  ops/ms
EishayWriteUTF8Bytes.fastjson1  thrpt    5  1015.617 ± 11.698  ops/ms
EishayWriteUTF8Bytes.jackson    thrpt    5  1599.584 ±  3.278  ops/ms
```
