# JDK8
```java
Benchmark                                  Mode  Cnt     Score    Error   Units
EishayParseString.fastjson1               thrpt    5  1061.991 ± 17.093  ops/ms
EishayParseString.fastjson2               thrpt    5  1318.068 ± 11.152  ops/ms
EishayParseString.jackson                 thrpt    5   483.895 ±  5.281  ops/ms
EishayParseStringPretty.fastjson1         thrpt    5   281.783 ±  1.413  ops/ms
EishayParseStringPretty.fastjson2         thrpt    5   971.703 ± 39.671  ops/ms
EishayParseStringPretty.jackson           thrpt    5   440.288 ±  4.494  ops/ms
EishayParseTreeString.fastjson1           thrpt    5   489.514 ±  5.290  ops/ms
EishayParseTreeString.fastjson2           thrpt    5   828.556 ±  4.524  ops/ms
EishayParseTreeString.jackson             thrpt    5   520.110 ±  2.905  ops/ms
EishayParseTreeStringPretty.fastjson1     thrpt    5   417.436 ±  2.848  ops/ms
EishayParseTreeStringPretty.fastjson2     thrpt    5   646.834 ± 23.953  ops/ms
EishayParseTreeStringPretty.jackson       thrpt    5   453.234 ±  2.164  ops/ms
EishayParseTreeUTF8Bytes.fastjson1        thrpt    5   449.161 ±  8.076  ops/ms
EishayParseTreeUTF8Bytes.fastjson2        thrpt    5   637.364 ±  7.723  ops/ms
EishayParseTreeUTF8Bytes.jackson          thrpt    5   627.170 ±  4.615  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1  thrpt    5   367.809 ±  4.421  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2  thrpt    5   567.836 ± 17.822  ops/ms
EishayParseTreeUTF8BytesPretty.jackson    thrpt    5   587.310 ±  4.058  ops/ms
EishayParseUTF8Bytes.fastjson1            thrpt    5   860.651 ±  7.588  ops/ms
EishayParseUTF8Bytes.fastjson2            thrpt    5  1070.207 ±  3.395  ops/ms
EishayParseUTF8Bytes.jackson              thrpt    5   628.152 ±  2.871  ops/ms
EishayParseUTF8BytesPretty.fastjson1      thrpt    5   258.413 ±  1.622  ops/ms
EishayParseUTF8BytesPretty.fastjson2      thrpt    5   821.269 ± 14.557  ops/ms
EishayParseUTF8BytesPretty.jackson        thrpt    5   539.509 ±  7.856  ops/ms
EishayWriteBinary.fastjson2JSONB          thrpt    5  1811.319 ± 15.349  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes      thrpt    5  1412.531 ± 20.810  ops/ms
EishayWriteBinary.hessian                 thrpt    5   354.366 ± 13.414  ops/ms
EishayWriteString.fastjson1               thrpt    5   601.317 ±  3.872  ops/ms
EishayWriteString.fastjson2               thrpt    5  1569.851 ± 40.246  ops/ms
EishayWriteString.jackson                 thrpt    5   892.890 ± 13.587  ops/ms
EishayWriteUTF8Bytes.fastjson1            thrpt    5   554.241 ±  5.395  ops/ms
EishayWriteUTF8Bytes.fastjson2            thrpt    5  1410.075 ± 13.943  ops/ms
EishayWriteUTF8Bytes.jackson              thrpt    5   852.145 ± 28.950  ops/ms
```

# JDK 11
```java
Benchmark                                  Mode  Cnt     Score     Error   Units
EishayParseString.fastjson1               thrpt    5   887.356 ±   9.016  ops/ms
EishayParseString.fastjson2               thrpt    5  1120.134 ±  12.298  ops/ms
EishayParseString.jackson                 thrpt    5   443.838 ±  13.647  ops/ms
EishayParseStringPretty.fastjson1         thrpt    5   259.519 ±  19.996  ops/ms
EishayParseStringPretty.fastjson2         thrpt    5   907.202 ±  83.423  ops/ms
EishayParseStringPretty.jackson           thrpt    5   416.903 ±  31.547  ops/ms
EishayParseTreeString.fastjson1           thrpt    5   416.987 ±  30.168  ops/ms
EishayParseTreeString.fastjson2           thrpt    5   602.596 ±  48.394  ops/ms
EishayParseTreeString.jackson             thrpt    5   423.125 ±  29.992  ops/ms
EishayParseTreeStringPretty.fastjson1     thrpt    5   358.219 ±  15.611  ops/ms
EishayParseTreeStringPretty.fastjson2     thrpt    5   527.974 ±  22.442  ops/ms
EishayParseTreeStringPretty.jackson       thrpt    5   410.249 ±  10.593  ops/ms
EishayParseTreeUTF8Bytes.fastjson1        thrpt    5   385.412 ±   3.509  ops/ms
EishayParseTreeUTF8Bytes.fastjson2        thrpt    5   646.987 ±   6.180  ops/ms
EishayParseTreeUTF8Bytes.jackson          thrpt    5   544.050 ±   7.236  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1  thrpt    5   329.352 ±   3.195  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2  thrpt    5   578.526 ±  24.141  ops/ms
EishayParseTreeUTF8BytesPretty.jackson    thrpt    5   465.974 ±   8.096  ops/ms
EishayParseUTF8Bytes.fastjson1            thrpt    5   748.986 ±   7.442  ops/ms
EishayParseUTF8Bytes.fastjson2            thrpt    5  1100.177 ±  10.898  ops/ms
EishayParseUTF8Bytes.jackson              thrpt    5   568.232 ±  15.777  ops/ms
EishayParseUTF8BytesPretty.fastjson1      thrpt    5   242.321 ±   2.302  ops/ms
EishayParseUTF8BytesPretty.fastjson2      thrpt    5   875.307 ±  26.695  ops/ms
EishayParseUTF8BytesPretty.jackson        thrpt    5   519.879 ±  35.691  ops/ms
EishayWriteBinary.fastjson2JSONB          thrpt    5  2351.074 ± 157.984  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes      thrpt    5  1602.488 ± 108.703  ops/ms
EishayWriteBinary.hessian                 thrpt    5   379.616 ±  11.856  ops/ms
EishayWriteString.fastjson1               thrpt    5   587.434 ±  36.490  ops/ms
EishayWriteString.fastjson2               thrpt    5  1548.319 ± 104.863  ops/ms
EishayWriteString.jackson                 thrpt    5   875.705 ±  65.023  ops/ms
EishayWriteUTF8Bytes.fastjson1            thrpt    5   503.268 ±  25.529  ops/ms
EishayWriteUTF8Bytes.fastjson2            thrpt    5  1588.390 ±  94.829  ops/ms
EishayWriteUTF8Bytes.jackson              thrpt    5   756.370 ±  32.025  ops/ms
```

# JDK 17
```java
Benchmark                                  Mode  Cnt     Score     Error   Units
EishayParseString.fastjson1               thrpt    5  1240.880 ±  22.847  ops/ms
EishayParseString.fastjson2               thrpt    5  1158.755 ±  26.232  ops/ms
EishayParseString.jackson                 thrpt    5   479.701 ±   6.714  ops/ms
EishayParseStringPretty.fastjson1         thrpt    5   297.450 ±   5.146  ops/ms
EishayParseStringPretty.fastjson2         thrpt    5   909.789 ±  11.161  ops/ms
EishayParseStringPretty.jackson           thrpt    5   447.225 ±  16.388  ops/ms
EishayParseTreeString.fastjson1           thrpt    5   521.609 ±  10.929  ops/ms
EishayParseTreeString.fastjson2           thrpt    5   624.974 ±  48.872  ops/ms
EishayParseTreeString.jackson             thrpt    5   482.425 ±  36.314  ops/ms
EishayParseTreeStringPretty.fastjson1     thrpt    5   443.164 ±  28.400  ops/ms
EishayParseTreeStringPretty.fastjson2     thrpt    5   544.234 ±  32.516  ops/ms
EishayParseTreeStringPretty.jackson       thrpt    5   443.870 ±  25.451  ops/ms
EishayParseTreeUTF8Bytes.fastjson1        thrpt    5   439.494 ±  23.895  ops/ms
EishayParseTreeUTF8Bytes.fastjson2        thrpt    5   583.129 ±  43.193  ops/ms
EishayParseTreeUTF8Bytes.jackson          thrpt    5   549.007 ±  33.851  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1  thrpt    5   386.217 ±  13.166  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2  thrpt    5   519.703 ±  23.498  ops/ms
EishayParseTreeUTF8BytesPretty.jackson    thrpt    5   498.391 ±   7.324  ops/ms
EishayParseUTF8Bytes.fastjson1            thrpt    5   929.639 ±  19.634  ops/ms
EishayParseUTF8Bytes.fastjson2            thrpt    5  1020.883 ±  18.793  ops/ms
EishayParseUTF8Bytes.jackson              thrpt    5   554.546 ±   7.424  ops/ms
EishayParseUTF8BytesPretty.fastjson1      thrpt    5   272.655 ±   3.946  ops/ms
EishayParseUTF8BytesPretty.fastjson2      thrpt    5   823.107 ±   5.506  ops/ms
EishayParseUTF8BytesPretty.jackson        thrpt    5   497.885 ±   7.152  ops/ms
EishayWriteBinary.fastjson2JSONB          thrpt    5  2400.102 ±  49.172  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes      thrpt    5  1654.640 ±  77.292  ops/ms
EishayWriteBinary.hessian                 thrpt    5   363.360 ±  17.688  ops/ms
EishayWriteString.fastjson1               thrpt    5   598.635 ±  45.475  ops/ms
EishayWriteString.fastjson2               thrpt    5  1611.368 ± 134.099  ops/ms
EishayWriteString.jackson                 thrpt    5   897.802 ±  58.267  ops/ms
EishayWriteUTF8Bytes.fastjson1            thrpt    5   531.670 ±  35.878  ops/ms
EishayWriteUTF8Bytes.fastjson2            thrpt    5  1651.618 ± 124.609  ops/ms
EishayWriteUTF8Bytes.jackson              thrpt    5   848.798 ±  44.764  ops/ms
```
