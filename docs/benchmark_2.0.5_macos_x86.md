# JDK 8
```java
Benchmark                                  Mode  Cnt     Score     Error   Units
EishayParseString.fastjson1               thrpt    5  1100.867 ±  32.876  ops/ms
EishayParseString.fastjson2               thrpt    5  1341.962 ±  76.989  ops/ms
EishayParseString.jackson                 thrpt    5   510.125 ±  29.376  ops/ms
EishayParseStringPretty.fastjson1         thrpt    5   294.598 ±  15.579  ops/ms
EishayParseStringPretty.fastjson2         thrpt    5   964.325 ±  48.560  ops/ms
EishayParseStringPretty.jackson           thrpt    5   458.514 ±  21.526  ops/ms
EishayParseTreeString.fastjson1           thrpt    5   509.788 ±  25.788  ops/ms
EishayParseTreeString.fastjson2           thrpt    5   800.122 ±  33.175  ops/ms
EishayParseTreeString.jackson             thrpt    5   511.344 ±  24.244  ops/ms
EishayParseTreeStringPretty.fastjson1     thrpt    5   429.257 ±  24.179  ops/ms
EishayParseTreeStringPretty.fastjson2     thrpt    5   674.642 ±  27.978  ops/ms
EishayParseTreeStringPretty.jackson       thrpt    5   473.769 ±  26.109  ops/ms
EishayParseTreeUTF8Bytes.fastjson1        thrpt    5   465.876 ±   7.562  ops/ms
EishayParseTreeUTF8Bytes.fastjson2        thrpt    5   646.213 ±  16.122  ops/ms
EishayParseTreeUTF8Bytes.jackson          thrpt    5   643.855 ±  15.816  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1  thrpt    5   381.557 ±   2.984  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2  thrpt    5   565.655 ±   8.875  ops/ms
EishayParseTreeUTF8BytesPretty.jackson    thrpt    5   527.618 ±   6.288  ops/ms
EishayParseUTF8Bytes.fastjson1            thrpt    5   882.351 ±  10.750  ops/ms
EishayParseUTF8Bytes.fastjson2            thrpt    5  1101.346 ±  15.071  ops/ms
EishayParseUTF8Bytes.jackson              thrpt    5   622.826 ±  18.181  ops/ms
EishayParseUTF8BytesPretty.fastjson1      thrpt    5   269.658 ±   2.806  ops/ms
EishayParseUTF8BytesPretty.fastjson2      thrpt    5   844.831 ±   2.416  ops/ms
EishayParseUTF8BytesPretty.jackson        thrpt    5   554.997 ±  15.678  ops/ms
EishayWriteBinary.fastjson2JSONB          thrpt    5  1790.008 ±  78.967  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes      thrpt    5  1367.115 ±  69.565  ops/ms
EishayWriteBinary.hessian                 thrpt    5   413.852 ±  10.741  ops/ms
EishayWriteString.fastjson1               thrpt    5   594.949 ± 171.651  ops/ms
EishayWriteString.fastjson2               thrpt    5  1539.392 ±  64.931  ops/ms
EishayWriteString.jackson                 thrpt    5   858.694 ±  44.991  ops/ms
EishayWriteUTF8Bytes.fastjson1            thrpt    5   541.241 ±  27.363  ops/ms
EishayWriteUTF8Bytes.fastjson2            thrpt    5  1373.006 ±  37.007  ops/ms
EishayWriteUTF8Bytes.jackson              thrpt    5   796.893 ±  44.708  ops/ms
```

# JDK 11
```java

Benchmark                                  Mode  Cnt     Score     Error   Units
EishayParseString.fastjson1               thrpt    5   892.101 ±  12.057  ops/ms
EishayParseString.fastjson2               thrpt    5  1142.005 ±  13.173  ops/ms
EishayParseString.jackson                 thrpt    5   441.089 ±   5.163  ops/ms
EishayParseStringPretty.fastjson1         thrpt    5   259.478 ±   6.814  ops/ms
EishayParseStringPretty.fastjson2         thrpt    5   953.158 ±   9.319  ops/ms
EishayParseStringPretty.jackson           thrpt    5   409.603 ±   8.199  ops/ms
EishayParseTreeString.fastjson1           thrpt    5   418.209 ±   6.579  ops/ms
EishayParseTreeString.fastjson2           thrpt    5   631.983 ±  12.890  ops/ms
EishayParseTreeString.jackson             thrpt    5   430.630 ±   4.885  ops/ms
EishayParseTreeStringPretty.fastjson1     thrpt    5   361.453 ±  11.543  ops/ms
EishayParseTreeStringPretty.fastjson2     thrpt    5   562.405 ±   6.248  ops/ms
EishayParseTreeStringPretty.jackson       thrpt    5   401.304 ±   4.320  ops/ms
EishayParseTreeUTF8Bytes.fastjson1        thrpt    5   377.805 ±  24.231  ops/ms
EishayParseTreeUTF8Bytes.fastjson2        thrpt    5   632.101 ±  32.795  ops/ms
EishayParseTreeUTF8Bytes.jackson          thrpt    5   528.750 ±  28.742  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1  thrpt    5   314.252 ±  12.694  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2  thrpt    5   540.843 ±  16.771  ops/ms
EishayParseTreeUTF8BytesPretty.jackson    thrpt    5   456.050 ±  21.610  ops/ms
EishayParseUTF8Bytes.fastjson1            thrpt    5   717.625 ±  40.122  ops/ms
EishayParseUTF8Bytes.fastjson2            thrpt    5  1049.536 ±  53.513  ops/ms
EishayParseUTF8Bytes.jackson              thrpt    5   567.762 ±  29.373  ops/ms
EishayParseUTF8BytesPretty.fastjson1      thrpt    5   236.189 ±  18.179  ops/ms
EishayParseUTF8BytesPretty.fastjson2      thrpt    5   865.083 ±  46.919  ops/ms
EishayParseUTF8BytesPretty.jackson        thrpt    5   501.315 ±  19.625  ops/ms
EishayWriteBinary.fastjson2JSONB          thrpt    5  2313.274 ±  47.656  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes      thrpt    5  1556.355 ± 108.143  ops/ms
EishayWriteBinary.hessian                 thrpt    5   378.347 ±  12.742  ops/ms
EishayWriteString.fastjson1               thrpt    5   567.679 ±  15.657  ops/ms
EishayWriteString.fastjson2               thrpt    5  1536.810 ±  15.857  ops/ms
EishayWriteString.jackson                 thrpt    5   894.929 ±  15.486  ops/ms
EishayWriteUTF8Bytes.fastjson1            thrpt    5   494.690 ±   4.409  ops/ms
EishayWriteUTF8Bytes.fastjson2            thrpt    5  1597.920 ±  37.875  ops/ms
EishayWriteUTF8Bytes.jackson              thrpt    5   761.683 ±  22.436  ops/ms

```
