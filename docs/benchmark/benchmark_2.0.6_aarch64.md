# Parse String to Tree
## JDK 8
```java
Benchmark                               Mode  Cnt     Score    Error   Units
EishayParseTreeString.fastjson1        thrpt    5   690.383 ±  4.276  ops/ms
EishayParseTreeString.fastjson2        thrpt    5  1386.174 ±  4.704  ops/ms
EishayParseTreeString.jackson          thrpt    5   774.475 ±  3.621  ops/ms
        
EishayParseTreeStringPretty.fastjson1  thrpt    5   647.956 ±  2.429  ops/ms
EishayParseTreeStringPretty.fastjson2  thrpt    5  1107.921 ±  8.633  ops/ms
EishayParseTreeStringPretty.jackson    thrpt    5   730.040 ±  6.874  ops/ms
```

## JDK 11
```java
Benchmark                               Mode  Cnt     Score    Error   Units
EishayParseTreeString.fastjson1        thrpt    5   574.153 ±  3.880  ops/ms
EishayParseTreeString.fastjson2        thrpt    5  1105.571 ± 14.606  ops/ms
EishayParseTreeString.jackson          thrpt    5   741.634 ± 72.398  ops/ms
        
EishayParseTreeStringPretty.fastjson1  thrpt    5   483.151 ±  3.533  ops/ms
EishayParseTreeStringPretty.fastjson2  thrpt    5   974.937 ±  1.301  ops/ms
EishayParseTreeStringPretty.jackson    thrpt    5   671.629 ± 70.256  ops/ms
```

## JDK 17
```java
Benchmark                               Mode  Cnt     Score    Error   Units
EishayParseTreeString.fastjson1        thrpt    5   863.519 ±  2.238  ops/ms
EishayParseTreeString.fastjson2        thrpt    5  1306.557 ±  6.490  ops/ms
EishayParseTreeString.jackson          thrpt    5   821.810 ±  4.003  ops/ms
        
EishayParseTreeStringPretty.fastjson1  thrpt    5   738.134 ±  3.791  ops/ms
EishayParseTreeStringPretty.fastjson2  thrpt    5  1169.001 ± 27.757  ops/ms
EishayParseTreeStringPretty.jackson    thrpt    5   732.547 ±  1.321  ops/ms
```

# Pars String to Java Bean

## JDK 8
```java
Benchmark                           Mode  Cnt     Score    Error   Units
EishayParseString.fastjson1        thrpt    5  1506.121 ± 10.540  ops/ms
EishayParseString.fastjson2        thrpt    5  1904.445 ± 15.548  ops/ms
EishayParseString.jackson          thrpt    5   785.943 ±  8.026  ops/ms

EishayParseStringPretty.fastjson1  thrpt    5   444.626 ±  2.095  ops/ms
EishayParseStringPretty.fastjson2  thrpt    5  1555.524 ± 61.102  ops/ms
EishayParseStringPretty.jackson    thrpt    5   697.317 ±  2.651  ops/ms
```

## JDK 11
```java
Benchmark                           Mode  Cnt     Score    Error   Units
EishayParseString.fastjson1        thrpt    5  1492.807 ±  2.045  ops/ms
EishayParseString.fastjson2        thrpt    5  2051.944 ±  3.973  ops/ms
EishayParseString.jackson          thrpt    5   734.331 ±  7.443  ops/ms
        
EishayParseStringPretty.fastjson1  thrpt    5   376.842 ±  8.139  ops/ms
EishayParseStringPretty.fastjson2  thrpt    5  1637.182 ± 12.742  ops/ms
EishayParseStringPretty.jackson    thrpt    5   683.865 ±  4.538  ops/ms
```

## JDK 17
```java
Benchmark                           Mode  Cnt     Score    Error   Units
EishayParseString.fastjson1        thrpt    5  2140.765 ± 29.275  ops/ms
EishayParseString.fastjson2        thrpt    5  2076.290 ± 15.175  ops/ms
EishayParseString.jackson          thrpt    5   733.161 ±  6.732  ops/ms
EishayParseStringPretty.fastjson1  thrpt    5   464.842 ± 33.520  ops/ms
EishayParseStringPretty.fastjson2  thrpt    5  1622.839 ±  2.434  ops/ms
EishayParseStringPretty.jackson    thrpt    5   679.034 ± 13.266  ops/ms
```

# Parse UTF8 Bytes to Java Bean
## JDK8 
```java
Benchmark                        Mode  Cnt     Score    Error   Units
EishayParseUTF8Bytes.fastjson1  thrpt    5  1458.770 ±  8.930  ops/ms
EishayParseUTF8Bytes.fastjson2  thrpt    5  1702.374 ± 12.128  ops/ms
EishayParseUTF8Bytes.jackson    thrpt    5  1038.472 ±  6.786  ops/ms
```

## JDK 11
```java
Benchmark                        Mode  Cnt     Score    Error   Units
EishayParseUTF8Bytes.fastjson1  thrpt    5  1334.734 ±  1.977  ops/ms
EishayParseUTF8Bytes.fastjson2  thrpt    5  1703.925 ± 28.962  ops/ms
EishayParseUTF8Bytes.jackson    thrpt    5   940.884 ±  5.354  ops/ms
```

## JDK 17
```java
Benchmark                        Mode  Cnt     Score    Error   Units
EishayParseUTF8Bytes.fastjson1  thrpt    5  1711.159 ± 19.152  ops/ms
EishayParseUTF8Bytes.fastjson2  thrpt    5  1390.439 ± 41.888  ops/ms
EishayParseUTF8Bytes.jackson    thrpt    5   955.714 ±  7.880  ops/ms
```

# Parse UTF8 Bytes to JSONObject
## JDK 8
```java
Benchmark                                  Mode  Cnt     Score    Error   Units
EishayParseTreeUTF8Bytes.fastjson1        thrpt    5   643.562 ±  2.385  ops/ms
EishayParseTreeUTF8Bytes.fastjson2        thrpt    5  1007.917 ± 39.889  ops/ms
EishayParseTreeUTF8Bytes.jackson          thrpt    5   912.784 ±  3.692  ops/ms
        
EishayParseTreeUTF8BytesPretty.fastjson1  thrpt    5   591.731 ±  5.591  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2  thrpt    5   911.364 ±  4.378  ops/ms
EishayParseTreeUTF8BytesPretty.jackson    thrpt    5   835.962 ±  3.779  ops/ms
```

## JDK 11
```java
Benchmark                                  Mode  Cnt     Score    Error   Units
EishayParseTreeUTF8Bytes.fastjson1        thrpt    5   527.782 ±  1.978  ops/ms
EishayParseTreeUTF8Bytes.fastjson2        thrpt    5  1019.449 ± 11.031  ops/ms
EishayParseTreeUTF8Bytes.jackson          thrpt    5   864.616 ±  4.383  ops/ms
        
EishayParseTreeUTF8BytesPretty.fastjson1  thrpt    5   423.342 ±  1.860  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2  thrpt    5   889.119 ±  3.368  ops/ms
EishayParseTreeUTF8BytesPretty.jackson    thrpt    5   819.754 ±  3.266  ops/ms
```

## JDK 17
```java
Benchmark                                  Mode  Cnt    Score    Error   Units
EishayParseTreeUTF8Bytes.fastjson1        thrpt    5  744.198 ±  1.210  ops/ms
EishayParseTreeUTF8Bytes.fastjson2        thrpt    5  972.853 ±  0.782  ops/ms
EishayParseTreeUTF8Bytes.jackson          thrpt    5  960.142 ± 28.373  ops/ms
        
EishayParseTreeUTF8BytesPretty.fastjson1  thrpt    5  608.995 ±  4.504  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2  thrpt    5  817.232 ±  2.426  ops/ms
EishayParseTreeUTF8BytesPretty.jackson    thrpt    5  866.337 ± 38.149  ops/ms
```

# Parse Pretty UTF8 Bytes to Java Bean
## JDK 8
```java
Benchmark                              Mode  Cnt     Score   Error   Units
EishayParseUTF8BytesPretty.fastjson1  thrpt    5   434.409 ±  2.682  ops/ms
EishayParseUTF8BytesPretty.fastjson2  thrpt    5  1439.937 ±  8.955  ops/ms
EishayParseUTF8BytesPretty.jackson    thrpt    5   934.234 ±  5.490  ops/ms
```

## JDK 11
```java
Benchmark                              Mode  Cnt     Score   Error   Units
EishayParseUTF8BytesPretty.fastjson1  thrpt    5   357.830 ± 5.486  ops/ms
EishayParseUTF8BytesPretty.fastjson2  thrpt    5  1478.987 ± 7.093  ops/ms
EishayParseUTF8BytesPretty.jackson    thrpt    5   886.232 ± 1.921  ops/ms
```

## JDK 17
```java
Benchmark                              Mode  Cnt     Score    Error   Units
EishayParseUTF8BytesPretty.fastjson1  thrpt    5   457.828 ±  2.161  ops/ms
EishayParseUTF8BytesPretty.fastjson2  thrpt    5  1184.651 ± 60.593  ops/ms
EishayParseUTF8BytesPretty.jackson    thrpt    5   862.287 ±  6.359  ops/ms
```

# Write
## JDK 8
```java
Benchmark                              Mode  Cnt     Score    Error   Units
EishayWriteBinary.fastjson2JSONB      thrpt    5  3366.282 ± 29.277  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes  thrpt    5  2737.281 ± 10.204  ops/ms
EishayWriteBinary.hessian             thrpt    5   651.355 ±  3.048  ops/ms
        
EishayWriteString.fastjson1           thrpt    5  1089.242 ±  6.237  ops/ms
EishayWriteString.fastjson2           thrpt    5  2844.144 ± 12.060  ops/ms
EishayWriteString.jackson             thrpt    5  1669.517 ± 11.336  ops/ms
        
EishayWriteUTF8Bytes.fastjson1        thrpt    5   970.088 ±  8.858  ops/ms
EishayWriteUTF8Bytes.fastjson2        thrpt    5  2733.845 ± 21.108  ops/ms
EishayWriteUTF8Bytes.jackson          thrpt    5  1562.804 ± 12.561  ops/ms
```

## JDK 11
```java
Benchmark                              Mode  Cnt     Score    Error   Units
EishayWriteBinary.fastjson2JSONB      thrpt    5  4680.749 ± 37.041  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes  thrpt    5  2937.289 ± 36.179  ops/ms
EishayWriteBinary.hessian             thrpt    5   712.120 ±  8.515  ops/ms
        
EishayWriteString.fastjson1           thrpt    5  1170.191 ±  3.183  ops/ms
EishayWriteString.fastjson2           thrpt    5  2822.569 ±  4.740  ops/ms
EishayWriteString.jackson             thrpt    5  1638.000 ±  6.776  ops/ms
        
EishayWriteUTF8Bytes.fastjson1        thrpt    5   957.850 ±  8.579  ops/ms
EishayWriteUTF8Bytes.fastjson2        thrpt    5  2939.642 ± 19.545  ops/ms
EishayWriteUTF8Bytes.jackson          thrpt    5  1395.740 ±  2.111  ops/ms
```

## JDK 17
```java
Benchmark                              Mode  Cnt     Score    Error   Units
EishayWriteBinary.fastjson2JSONB      thrpt    5  4507.545 ± 18.047  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes  thrpt    5  2889.997 ± 30.163  ops/ms
EishayWriteBinary.hessian             thrpt    5   702.402 ±  1.737  ops/ms
        
EishayWriteString.fastjson1           thrpt    5  1119.264 ±  3.899  ops/ms
EishayWriteString.fastjson2           thrpt    5  2842.660 ±  3.624  ops/ms
EishayWriteString.jackson             thrpt    5  1686.173 ±  8.708  ops/ms
        
EishayWriteUTF8Bytes.fastjson1        thrpt    5   966.029 ±  3.632  ops/ms
EishayWriteUTF8Bytes.fastjson2        thrpt    5  2946.277 ±  5.727  ops/ms
EishayWriteUTF8Bytes.jackson          thrpt    5  1515.163 ± 22.535  ops/ms
```
