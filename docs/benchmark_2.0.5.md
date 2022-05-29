# ParseString JDK 8
```java
Benchmark                                  Mode  Cnt     Score    Error   Units
EishayParseString.fastjson1               thrpt    5  1493.916 ±  3.882  ops/ms
EishayParseString.fastjson2               thrpt    5  1819.807 ±  4.735  ops/ms
EishayParseString.jackson                 thrpt    5   780.202 ±  3.061  ops/ms

EishayParseStringPretty.fastjson1         thrpt    5   446.901 ±  3.624  ops/ms
EishayParseStringPretty.fastjson2         thrpt    5  1543.258 ± 13.012  ops/ms
EishayParseStringPretty.jackson           thrpt    5   702.875 ±  3.482  ops/ms
```

# ParseString JDK 11
```java
Benchmark                           Mode  Cnt     Score     Error   Units
EishayParseString.fastjson1        thrpt    5  1488.971 ±   3.802  ops/ms
EishayParseString.fastjson2        thrpt    5  2000.528 ± 125.804  ops/ms
EishayParseString.jackson          thrpt    5   743.393 ±   3.411  ops/ms
        
EishayParseStringPretty.fastjson1  thrpt    5   384.105 ±   5.310  ops/ms
EishayParseStringPretty.fastjson2  thrpt    5  1692.648 ±   9.319  ops/ms
EishayParseStringPretty.jackson    thrpt    5   698.171 ±   4.019  ops/ms
```

# ParseTree String JDK 8
```java
Benchmark                                 Mode     Cnt Score      Error   Units
EishayParseTreeString.fastjson1           thrpt    5   686.325 ±  5.677  ops/ms
EishayParseTreeString.fastjson2           thrpt    5  1394.743 ±  6.399  ops/ms
EishayParseTreeString.jackson             thrpt    5   771.913 ±  2.678  ops/ms

EishayParseTreeStringPretty.fastjson1     thrpt    5   646.390 ±  1.886  ops/ms
EishayParseTreeStringPretty.fastjson2     thrpt    5  1057.306 ±  2.656  ops/ms
EishayParseTreeStringPretty.jackson       thrpt    5   706.540 ±  6.180  ops/ms
```

# ParseTree String JDK 11
```java
Benchmark                               Mode  Cnt     Score   Error   Units
EishayParseTreeString.fastjson1        thrpt    5   688.337 ± 6.488  ops/ms
EishayParseTreeString.fastjson2        thrpt    5  1376.418 ± 6.132  ops/ms
EishayParseTreeString.jackson          thrpt    5   779.793 ± 2.930  ops/ms
        
EishayParseTreeStringPretty.fastjson1  thrpt    5   647.772 ± 4.477  ops/ms
EishayParseTreeStringPretty.fastjson2  thrpt    5  1055.319 ± 7.983  ops/ms
EishayParseTreeStringPretty.jackson    thrpt    5   724.291 ± 2.673  ops/ms
```

# ParseTree UTF8 Bytes JDK 8
```java
Benchmark                                 Mode     Cnt Score      Error   Units
EishayParseTreeUTF8Bytes.fastjson1        thrpt    5   644.541 ±  4.656  ops/ms
EishayParseTreeUTF8Bytes.fastjson2        thrpt    5  1114.174 ±  5.865  ops/ms
EishayParseTreeUTF8Bytes.jackson          thrpt    5   885.003 ±  5.625  ops/ms

EishayParseTreeUTF8BytesPretty.fastjson1  thrpt    5   580.241 ±  3.561  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2  thrpt    5   949.221 ±  5.655  ops/ms
EishayParseTreeUTF8BytesPretty.jackson    thrpt    5   844.908 ±  2.074  ops/ms
```

# Parse UT8 Bytes JDK 8
```java
Benchmark                                 Mode     Cnt Score      Error   Units
EishayParseUTF8Bytes.fastjson1            thrpt    5  1451.195 ±  2.582  ops/ms
EishayParseUTF8Bytes.fastjson2            thrpt    5  1756.928 ±  6.582  ops/ms
EishayParseUTF8Bytes.jackson              thrpt    5  1039.762 ±  3.673  ops/ms

EishayParseUTF8BytesPretty.fastjson1      thrpt    5   434.372 ±  2.056  ops/ms
EishayParseUTF8BytesPretty.fastjson2      thrpt    5  1473.479 ±  9.926  ops/ms
EishayParseUTF8BytesPretty.jackson        thrpt    5   934.662 ±  5.554  ops/ms
```

## Write JDK 8
```java
Benchmark                                  Mode  Cnt     Score    Error   Units
EishayWriteBinary.fastjson2JSONB          thrpt    5  3372.305 ± 32.003  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes      thrpt    5  2731.581 ±  8.537  ops/ms
EishayWriteBinary.hessian                 thrpt    5   635.553 ±  2.457  ops/ms

EishayWriteString.fastjson1               thrpt    5  1210.404 ±  4.500  ops/ms
EishayWriteString.fastjson2               thrpt    5  2836.711 ±  8.704  ops/ms
EishayWriteString.jackson                 thrpt    5  1685.915 ± 11.411  ops/ms

EishayWriteUTF8Bytes.fastjson1            thrpt    5  1049.106 ±  4.306  ops/ms
EishayWriteUTF8Bytes.fastjson2            thrpt    5  2732.122 ± 11.217  ops/ms
EishayWriteUTF8Bytes.jackson              thrpt    5  1565.262 ±  6.551  ops/ms
```

## Write JDK 11
```java
Benchmark                              Mode  Cnt     Score    Error   Units
EishayWriteBinary.fastjson2JSONB      thrpt    5  4627.764 ± 13.814  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes  thrpt    5  2925.956 ± 14.951  ops/ms
EishayWriteBinary.hessian             thrpt    5   710.814 ±  8.688  ops/ms
        
EishayWriteString.fastjson1           thrpt    5  1070.011 ±  3.273  ops/ms
EishayWriteString.fastjson2           thrpt    5  2811.842 ± 14.266  ops/ms
EishayWriteString.jackson             thrpt    5  1625.847 ±  3.216  ops/ms
        
EishayWriteUTF8Bytes.fastjson1        thrpt    5   945.418 ± 11.056  ops/ms
EishayWriteUTF8Bytes.fastjson2        thrpt    5  2907.124 ±  7.865  ops/ms
EishayWriteUTF8Bytes.jackson          thrpt    5  1379.972 ± 12.567  ops/ms
```
