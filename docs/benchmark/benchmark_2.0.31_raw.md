# ecs.g8i.xlarge-jdk1.8.0_371
```java
Benchmark                                          Mode  Cnt     Score    Error   Units
EishayParseBinary.fastjson2UTF8Bytes              thrpt    5  1511.891 ?  4.514  ops/ms
EishayParseBinary.hessian                         thrpt    5   377.677 ?  0.256  ops/ms
EishayParseBinary.javaSerialize                   thrpt    5    53.987 ?  0.037  ops/ms
EishayParseBinary.jsonb                           thrpt    5  2759.858 ?  9.547  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes  thrpt    5  2283.531 ?  8.041  ops/ms
EishayParseBinaryArrayMapping.jsonb               thrpt    5  4474.397 ? 14.311  ops/ms
EishayParseBinaryArrayMapping.kryo                thrpt    5  2287.200 ?  3.139  ops/ms
EishayParseBinaryArrayMapping.protobuf            thrpt    5  1528.585 ?  5.922  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB          thrpt    5  2120.664 ?  1.394  ops/ms
EishayParseBinaryAutoType.hessian                 thrpt    5   369.706 ?  1.357  ops/ms
EishayParseBinaryAutoType.javaSerialize           thrpt    5    52.805 ?  0.061  ops/ms
EishayParseString.fastjson1                       thrpt    5  1317.773 ?  3.623  ops/ms
EishayParseString.fastjson2                       thrpt    5  1690.224 ?  3.823  ops/ms
EishayParseString.gson                            thrpt    5   524.716 ?  4.308  ops/ms
EishayParseString.jackson                         thrpt    5   660.150 ?  2.099  ops/ms
EishayParseStringPretty.fastjson1                 thrpt    5   311.449 ?  0.332  ops/ms
EishayParseStringPretty.fastjson2                 thrpt    5  1267.976 ?  3.043  ops/ms
EishayParseStringPretty.gson                      thrpt    5   486.165 ?  5.381  ops/ms
EishayParseStringPretty.jackson                   thrpt    5   590.545 ?  1.441  ops/ms
EishayParseTreeString.fastjson1                   thrpt    5   649.152 ?  0.879  ops/ms
EishayParseTreeString.fastjson2                   thrpt    5  1217.858 ?  3.365  ops/ms
EishayParseTreeString.gson                        thrpt    5   388.725 ?  1.834  ops/ms
EishayParseTreeString.jackson                     thrpt    5   731.904 ?  1.263  ops/ms
EishayParseTreeStringPretty.fastjson1             thrpt    5   574.693 ?  0.725  ops/ms
EishayParseTreeStringPretty.fastjson2             thrpt    5   976.489 ?  1.159  ops/ms
EishayParseTreeStringPretty.gson                  thrpt    5   360.722 ?  1.857  ops/ms
EishayParseTreeStringPretty.jackson               thrpt    5   643.000 ?  2.197  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                thrpt    5   577.309 ?  1.601  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                thrpt    5  1142.615 ?  3.384  ops/ms
EishayParseTreeUTF8Bytes.gson                     thrpt    5   359.162 ?  1.850  ops/ms
EishayParseTreeUTF8Bytes.jackson                  thrpt    5   788.174 ?  0.902  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1          thrpt    5   488.003 ?  6.039  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2          thrpt    5   961.099 ?  1.678  ops/ms
EishayParseTreeUTF8BytesPretty.gson               thrpt    5   335.999 ?  2.397  ops/ms
EishayParseTreeUTF8BytesPretty.jackson            thrpt    5   713.400 ?  2.219  ops/ms
EishayParseUTF8Bytes.fastjson1                    thrpt    5  1045.289 ? 73.472  ops/ms
EishayParseUTF8Bytes.fastjson2                    thrpt    5  1474.703 ?  1.825  ops/ms
EishayParseUTF8Bytes.gson                         thrpt    5   365.178 ?  1.125  ops/ms
EishayParseUTF8Bytes.jackson                      thrpt    5   735.305 ?  1.316  ops/ms
EishayParseUTF8BytesPretty.fastjson1              thrpt    5   290.678 ?  0.667  ops/ms
EishayParseUTF8BytesPretty.fastjson2              thrpt    5  1179.398 ?  2.473  ops/ms
EishayParseUTF8BytesPretty.gson                   thrpt    5   332.716 ?  2.691  ops/ms
EishayParseUTF8BytesPretty.jackson                thrpt    5   640.746 ?  0.973  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes              thrpt    5  2008.827 ?  3.446  ops/ms
EishayWriteBinary.hessian                         thrpt    5   365.535 ?  1.829  ops/ms
EishayWriteBinary.javaSerialize                   thrpt    5   286.042 ?  2.330  ops/ms
EishayWriteBinary.jsonb                           thrpt    5  2401.409 ? 28.284  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes  thrpt    5  2375.756 ?  2.463  ops/ms
EishayWriteBinaryArrayMapping.jsonb               thrpt    5  3970.092 ?  6.124  ops/ms
EishayWriteBinaryArrayMapping.kryo                thrpt    5  2298.002 ?  2.651  ops/ms
EishayWriteBinaryArrayMapping.protobuf            thrpt    5  1612.143 ?  1.127  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB          thrpt    5  1703.071 ?  8.519  ops/ms
EishayWriteBinaryAutoType.hessian                 thrpt    5   368.031 ?  3.328  ops/ms
EishayWriteBinaryAutoType.javaSerialize           thrpt    5   284.814 ?  4.488  ops/ms
EishayWriteString.fastjson1                       thrpt    5   756.034 ?  5.918  ops/ms
EishayWriteString.fastjson2                       thrpt    5  2160.454 ? 13.987  ops/ms
EishayWriteString.gson                            thrpt    5   588.121 ?  0.964  ops/ms
EishayWriteString.jackson                         thrpt    5  1289.232 ? 13.255  ops/ms
EishayWriteStringTree.fastjson1                   thrpt    5  1031.712 ? 13.803  ops/ms
EishayWriteStringTree.fastjson2                   thrpt    5  1527.449 ? 31.100  ops/ms
EishayWriteStringTree.gson                        thrpt    5   622.314 ?  3.126  ops/ms
EishayWriteStringTree.jackson                     thrpt    5  1199.741 ? 13.614  ops/ms
EishayWriteUTF8Bytes.fastjson1                    thrpt    5   678.988 ?  1.366  ops/ms
EishayWriteUTF8Bytes.fastjson2                    thrpt    5  2024.236 ?  4.770  ops/ms
EishayWriteUTF8Bytes.gson                         thrpt    5   480.702 ?  4.199  ops/ms
EishayWriteUTF8Bytes.jackson                      thrpt    5  1133.543 ?  1.049  ops/ms
```
# ecs.g8i.xlarge-jdk-11.0.19
```java
Benchmark                                          Mode  Cnt     Score     Error   Units
EishayParseBinary.fastjson2UTF8Bytes              thrpt    5  1579.256 ?   5.980  ops/ms
EishayParseBinary.hessian                         thrpt    5   328.149 ?   0.358  ops/ms
EishayParseBinary.javaSerialize                   thrpt    5    54.596 ?   0.085  ops/ms
EishayParseBinary.jsonb                           thrpt    5  3450.883 ?  10.520  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes  thrpt    5  2493.722 ?   7.813  ops/ms
EishayParseBinaryArrayMapping.jsonb               thrpt    5  5082.606 ?  29.990  ops/ms
EishayParseBinaryArrayMapping.kryo                thrpt    5  2053.640 ?   4.320  ops/ms
EishayParseBinaryArrayMapping.protobuf            thrpt    5  1663.790 ?   5.745  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB          thrpt    5  2400.167 ?   8.240  ops/ms
EishayParseBinaryAutoType.hessian                 thrpt    5   296.906 ?   0.652  ops/ms
EishayParseBinaryAutoType.javaSerialize           thrpt    5    53.779 ?   0.161  ops/ms
EishayParseString.fastjson1                       thrpt    5  1196.171 ?  26.767  ops/ms
EishayParseString.fastjson2                       thrpt    5  1536.041 ?   3.957  ops/ms
EishayParseString.gson                            thrpt    5   538.271 ?   2.473  ops/ms
EishayParseString.jackson                         thrpt    5   617.054 ?   1.029  ops/ms
EishayParseStringPretty.fastjson1                 thrpt    5   304.178 ?   0.392  ops/ms
EishayParseStringPretty.fastjson2                 thrpt    5  1143.633 ?   0.198  ops/ms
EishayParseStringPretty.gson                      thrpt    5   499.921 ?   2.336  ops/ms
EishayParseStringPretty.jackson                   thrpt    5   559.554 ?   1.468  ops/ms
EishayParseTreeString.fastjson1                   thrpt    5   556.096 ?   1.896  ops/ms
EishayParseTreeString.fastjson2                   thrpt    5  1080.210 ?   1.591  ops/ms
EishayParseTreeString.gson                        thrpt    5   364.650 ?   0.830  ops/ms
EishayParseTreeString.jackson                     thrpt    5   611.223 ?   2.056  ops/ms
EishayParseTreeStringPretty.fastjson1             thrpt    5   466.363 ?   2.468  ops/ms
EishayParseTreeStringPretty.fastjson2             thrpt    5   904.083 ?   1.561  ops/ms
EishayParseTreeStringPretty.gson                  thrpt    5   353.892 ?   2.364  ops/ms
EishayParseTreeStringPretty.jackson               thrpt    5   537.850 ?   0.740  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                thrpt    5   507.014 ?   3.724  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                thrpt    5  1061.335 ?   1.434  ops/ms
EishayParseTreeUTF8Bytes.gson                     thrpt    5   355.189 ?   0.789  ops/ms
EishayParseTreeUTF8Bytes.jackson                  thrpt    5   684.083 ?   1.157  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1          thrpt    5   420.170 ?   0.213  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2          thrpt    5   994.875 ?   1.318  ops/ms
EishayParseTreeUTF8BytesPretty.gson               thrpt    5   341.126 ?   0.701  ops/ms
EishayParseTreeUTF8BytesPretty.jackson            thrpt    5   618.967 ?   1.038  ops/ms
EishayParseUTF8Bytes.fastjson1                    thrpt    5   982.066 ?   2.822  ops/ms
EishayParseUTF8Bytes.fastjson2                    thrpt    5  1489.831 ?   3.174  ops/ms
EishayParseUTF8Bytes.gson                         thrpt    5   362.003 ?   0.339  ops/ms
EishayParseUTF8Bytes.jackson                      thrpt    5   703.584 ?   1.374  ops/ms
EishayParseUTF8BytesPretty.fastjson1              thrpt    5   284.612 ?   0.784  ops/ms
EishayParseUTF8BytesPretty.fastjson2              thrpt    5  1170.358 ?   1.608  ops/ms
EishayParseUTF8BytesPretty.gson                   thrpt    5   346.469 ?   0.882  ops/ms
EishayParseUTF8BytesPretty.jackson                thrpt    5   617.858 ?   1.437  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes              thrpt    5  2189.174 ?   7.982  ops/ms
EishayWriteBinary.hessian                         thrpt    5   358.185 ?   3.224  ops/ms
EishayWriteBinary.javaSerialize                   thrpt    5   270.640 ?   1.137  ops/ms
EishayWriteBinary.jsonb                           thrpt    5  3316.078 ?  10.915  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes  thrpt    5  2874.014 ?   7.802  ops/ms
EishayWriteBinaryArrayMapping.jsonb               thrpt    5  7351.358 ? 202.596  ops/ms
EishayWriteBinaryArrayMapping.kryo                thrpt    5  2452.830 ?   2.870  ops/ms
EishayWriteBinaryArrayMapping.protobuf            thrpt    5  1800.416 ?   2.809  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB          thrpt    5  1986.060 ?   5.703  ops/ms
EishayWriteBinaryAutoType.hessian                 thrpt    5   357.156 ?   1.716  ops/ms
EishayWriteBinaryAutoType.javaSerialize           thrpt    5   312.462 ?   5.190  ops/ms
EishayWriteString.fastjson1                       thrpt    5   721.396 ?   1.184  ops/ms
EishayWriteString.fastjson2                       thrpt    5  1940.291 ?   1.825  ops/ms
EishayWriteString.gson                            thrpt    5   479.932 ?   5.030  ops/ms
EishayWriteString.jackson                         thrpt    5  1188.887 ?   2.089  ops/ms
EishayWriteStringTree.fastjson1                   thrpt    5   983.119 ?   2.527  ops/ms
EishayWriteStringTree.fastjson2                   thrpt    5  1504.428 ?   6.179  ops/ms
EishayWriteStringTree.gson                        thrpt    5   474.823 ?   0.815  ops/ms
EishayWriteStringTree.jackson                     thrpt    5  1183.387 ?   1.135  ops/ms
EishayWriteUTF8Bytes.fastjson1                    thrpt    5   666.707 ?   0.880  ops/ms
EishayWriteUTF8Bytes.fastjson2                    thrpt    5  2143.866 ?   1.854  ops/ms
EishayWriteUTF8Bytes.gson                         thrpt    5   443.699 ?   1.224  ops/ms
EishayWriteUTF8Bytes.jackson                      thrpt    5  1087.262 ?   2.655  ops/ms
```
# ecs.g8i.xlarge-jdk-17.0.7
```java
Benchmark                                          Mode  Cnt     Score    Error   Units
EishayParseBinary.fastjson2UTF8Bytes              thrpt    5  1619.913 ?  8.142  ops/ms
EishayParseBinary.hessian                         thrpt    5   332.592 ?  0.267  ops/ms
EishayParseBinary.javaSerialize                   thrpt    5    57.429 ?  0.089  ops/ms
EishayParseBinary.jsonb                           thrpt    5  3743.773 ? 16.923  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes  thrpt    5  2843.076 ?  6.110  ops/ms
EishayParseBinaryArrayMapping.jsonb               thrpt    5  5868.347 ? 31.963  ops/ms
EishayParseBinaryArrayMapping.kryo                thrpt    5  2242.052 ?  4.952  ops/ms
EishayParseBinaryArrayMapping.protobuf            thrpt    5  2004.014 ?  4.183  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB          thrpt    5  2668.394 ? 12.939  ops/ms
EishayParseBinaryAutoType.hessian                 thrpt    5   327.022 ?  0.533  ops/ms
EishayParseBinaryAutoType.javaSerialize           thrpt    5    57.702 ?  0.074  ops/ms
EishayParseString.fastjson1                       thrpt    5  1577.432 ?  0.800  ops/ms
EishayParseString.fastjson2                       thrpt    5  1698.843 ?  2.969  ops/ms
EishayParseString.gson                            thrpt    5   533.883 ?  1.996  ops/ms
EishayParseString.jackson                         thrpt    5   635.174 ?  0.542  ops/ms
EishayParseStringPretty.fastjson1                 thrpt    5   363.614 ?  0.145  ops/ms
EishayParseStringPretty.fastjson2                 thrpt    5  1195.855 ?  1.389  ops/ms
EishayParseStringPretty.gson                      thrpt    5   492.582 ?  0.563  ops/ms
EishayParseStringPretty.jackson                   thrpt    5   587.740 ?  0.658  ops/ms
EishayParseTreeString.fastjson1                   thrpt    5   740.114 ?  3.918  ops/ms
EishayParseTreeString.fastjson2                   thrpt    5  1295.383 ?  1.716  ops/ms
EishayParseTreeString.gson                        thrpt    5   372.173 ?  0.223  ops/ms
EishayParseTreeString.jackson                     thrpt    5   662.056 ?  5.328  ops/ms
EishayParseTreeStringPretty.fastjson1             thrpt    5   629.991 ?  1.674  ops/ms
EishayParseTreeStringPretty.fastjson2             thrpt    5  1045.591 ?  2.029  ops/ms
EishayParseTreeStringPretty.gson                  thrpt    5   346.327 ?  0.343  ops/ms
EishayParseTreeStringPretty.jackson               thrpt    5   600.388 ?  0.254  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                thrpt    5   620.601 ?  1.318  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                thrpt    5  1240.508 ?  2.202  ops/ms
EishayParseTreeUTF8Bytes.gson                     thrpt    5   359.827 ?  0.681  ops/ms
EishayParseTreeUTF8Bytes.jackson                  thrpt    5   786.225 ?  1.774  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1          thrpt    5   528.982 ?  1.018  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2          thrpt    5  1047.469 ?  4.737  ops/ms
EishayParseTreeUTF8BytesPretty.gson               thrpt    5   339.029 ?  1.044  ops/ms
EishayParseTreeUTF8BytesPretty.jackson            thrpt    5   689.751 ?  1.169  ops/ms
EishayParseUTF8Bytes.fastjson1                    thrpt    5  1196.830 ?  0.953  ops/ms
EishayParseUTF8Bytes.fastjson2                    thrpt    5  1625.367 ?  5.784  ops/ms
EishayParseUTF8Bytes.gson                         thrpt    5   366.648 ?  1.618  ops/ms
EishayParseUTF8Bytes.jackson                      thrpt    5   719.380 ?  1.350  ops/ms
EishayParseUTF8BytesPretty.fastjson1              thrpt    5   323.397 ?  0.231  ops/ms
EishayParseUTF8BytesPretty.fastjson2              thrpt    5  1214.233 ?  1.018  ops/ms
EishayParseUTF8BytesPretty.gson                   thrpt    5   339.650 ?  0.566  ops/ms
EishayParseUTF8BytesPretty.jackson                thrpt    5   633.810 ?  0.801  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes              thrpt    5  2513.145 ?  6.824  ops/ms
EishayWriteBinary.hessian                         thrpt    5   348.754 ?  1.784  ops/ms
EishayWriteBinary.javaSerialize                   thrpt    5   286.833 ?  0.947  ops/ms
EishayWriteBinary.jsonb                           thrpt    5  4391.672 ? 41.306  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes  thrpt    5  2813.553 ?  5.742  ops/ms
EishayWriteBinaryArrayMapping.jsonb               thrpt    5  8017.896 ? 53.129  ops/ms
EishayWriteBinaryArrayMapping.kryo                thrpt    5  2549.223 ?  4.263  ops/ms
EishayWriteBinaryArrayMapping.protobuf            thrpt    5  1927.993 ?  2.537  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB          thrpt    5  2233.217 ?  5.229  ops/ms
EishayWriteBinaryAutoType.hessian                 thrpt    5   348.134 ?  8.265  ops/ms
EishayWriteBinaryAutoType.javaSerialize           thrpt    5   285.478 ?  3.288  ops/ms
EishayWriteString.fastjson1                       thrpt    5   782.940 ?  0.882  ops/ms
EishayWriteString.fastjson2                       thrpt    5  1912.154 ?  2.751  ops/ms
EishayWriteString.gson                            thrpt    5   248.328 ?  0.114  ops/ms
EishayWriteString.jackson                         thrpt    5  1334.289 ?  2.038  ops/ms
EishayWriteStringTree.fastjson1                   thrpt    5  1004.128 ?  2.208  ops/ms
EishayWriteStringTree.fastjson2                   thrpt    5  1320.483 ?  1.711  ops/ms
EishayWriteStringTree.gson                        thrpt    5   253.605 ?  0.181  ops/ms
EishayWriteStringTree.jackson                     thrpt    5  1153.611 ?  2.272  ops/ms
EishayWriteUTF8Bytes.fastjson1                    thrpt    5   687.609 ?  1.724  ops/ms
EishayWriteUTF8Bytes.fastjson2                    thrpt    5  2542.318 ?  6.871  ops/ms
EishayWriteUTF8Bytes.gson                         thrpt    5   239.325 ?  0.263  ops/ms
EishayWriteUTF8Bytes.jackson                      thrpt    5  1249.521 ?  4.338  ops/ms
```
# ecs.g7.xlarge-jdk1.8.0_371
```java
Benchmark                                          Mode  Cnt     Score    Error   Units
EishayParseBinary.fastjson2UTF8Bytes              thrpt    5  1119.126 ? 20.340  ops/ms
EishayParseBinary.hessian                         thrpt    5   270.323 ?  0.157  ops/ms
EishayParseBinary.javaSerialize                   thrpt    5    46.367 ?  0.092  ops/ms
EishayParseBinary.jsonb                           thrpt    5  1757.500 ?  1.746  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes  thrpt    5  1660.614 ?  2.213  ops/ms
EishayParseBinaryArrayMapping.jsonb               thrpt    5  2613.671 ?  2.216  ops/ms
EishayParseBinaryArrayMapping.kryo                thrpt    5  1720.196 ?  2.475  ops/ms
EishayParseBinaryArrayMapping.protobuf            thrpt    5  1044.048 ?  1.296  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB          thrpt    5  1470.409 ?  1.128  ops/ms
EishayParseBinaryAutoType.hessian                 thrpt    5   281.940 ?  0.482  ops/ms
EishayParseBinaryAutoType.javaSerialize           thrpt    5    47.647 ?  0.075  ops/ms
EishayParseString.fastjson1                       thrpt    5  1003.106 ?  0.856  ops/ms
EishayParseString.fastjson2                       thrpt    5  1233.302 ?  2.433  ops/ms
EishayParseString.gson                            thrpt    5   419.583 ?  2.389  ops/ms
EishayParseString.jackson                         thrpt    5   518.812 ?  1.217  ops/ms
EishayParseStringPretty.fastjson1                 thrpt    5   270.924 ?  0.203  ops/ms
EishayParseStringPretty.fastjson2                 thrpt    5   943.415 ?  0.629  ops/ms
EishayParseStringPretty.gson                      thrpt    5   402.761 ?  0.405  ops/ms
EishayParseStringPretty.jackson                   thrpt    5   462.438 ?  0.644  ops/ms
EishayParseTreeString.fastjson1                   thrpt    5   519.070 ?  1.116  ops/ms
EishayParseTreeString.fastjson2                   thrpt    5   972.417 ?  2.108  ops/ms
EishayParseTreeString.gson                        thrpt    5   335.640 ?  1.067  ops/ms
EishayParseTreeString.jackson                     thrpt    5   543.933 ?  0.639  ops/ms
EishayParseTreeStringPretty.fastjson1             thrpt    5   453.850 ?  1.959  ops/ms
EishayParseTreeStringPretty.fastjson2             thrpt    5   784.389 ?  0.485  ops/ms
EishayParseTreeStringPretty.gson                  thrpt    5   317.307 ?  0.727  ops/ms
EishayParseTreeStringPretty.jackson               thrpt    5   488.228 ?  0.521  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                thrpt    5   450.224 ? 29.926  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                thrpt    5   853.427 ?  1.487  ops/ms
EishayParseTreeUTF8Bytes.gson                     thrpt    5   310.522 ?  0.802  ops/ms
EishayParseTreeUTF8Bytes.jackson                  thrpt    5   577.737 ?  1.069  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1          thrpt    5   370.688 ?  0.797  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2          thrpt    5   689.540 ?  0.383  ops/ms
EishayParseTreeUTF8BytesPretty.gson               thrpt    5   292.742 ?  0.583  ops/ms
EishayParseTreeUTF8BytesPretty.jackson            thrpt    5   533.806 ?  3.072  ops/ms
EishayParseUTF8Bytes.fastjson1                    thrpt    5   807.478 ?  1.050  ops/ms
EishayParseUTF8Bytes.fastjson2                    thrpt    5  1096.761 ?  1.613  ops/ms
EishayParseUTF8Bytes.gson                         thrpt    5   309.288 ?  0.785  ops/ms
EishayParseUTF8Bytes.jackson                      thrpt    5   594.415 ?  0.751  ops/ms
EishayParseUTF8BytesPretty.fastjson1              thrpt    5   244.235 ?  0.352  ops/ms
EishayParseUTF8BytesPretty.fastjson2              thrpt    5   850.825 ?  1.067  ops/ms
EishayParseUTF8BytesPretty.gson                   thrpt    5   284.602 ?  0.238  ops/ms
EishayParseUTF8BytesPretty.jackson                thrpt    5   501.128 ?  1.627  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes              thrpt    5  1584.524 ?  5.393  ops/ms
EishayWriteBinary.hessian                         thrpt    5   338.643 ?  1.275  ops/ms
EishayWriteBinary.javaSerialize                   thrpt    5   226.974 ?  0.981  ops/ms
EishayWriteBinary.jsonb                           thrpt    5  2055.728 ?  3.875  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes  thrpt    5  2027.376 ?  1.859  ops/ms
EishayWriteBinaryArrayMapping.jsonb               thrpt    5  3349.231 ?  6.776  ops/ms
EishayWriteBinaryArrayMapping.kryo                thrpt    5  1862.278 ?  3.549  ops/ms
EishayWriteBinaryArrayMapping.protobuf            thrpt    5  1305.997 ?  2.637  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB          thrpt    5  1351.274 ?  4.269  ops/ms
EishayWriteBinaryAutoType.hessian                 thrpt    5   342.829 ?  2.902  ops/ms
EishayWriteBinaryAutoType.javaSerialize           thrpt    5   228.872 ?  0.862  ops/ms
EishayWriteString.fastjson1                       thrpt    5   627.294 ?  0.831  ops/ms
EishayWriteString.fastjson2                       thrpt    5  1661.538 ?  3.316  ops/ms
EishayWriteString.gson                            thrpt    5   451.329 ?  0.585  ops/ms
EishayWriteString.jackson                         thrpt    5  1049.422 ?  1.453  ops/ms
EishayWriteStringTree.fastjson1                   thrpt    5   819.034 ?  7.478  ops/ms
EishayWriteStringTree.fastjson2                   thrpt    5  1292.075 ?  4.077  ops/ms
EishayWriteStringTree.gson                        thrpt    5   439.648 ?  0.397  ops/ms
EishayWriteStringTree.jackson                     thrpt    5   984.359 ?  1.784  ops/ms
EishayWriteUTF8Bytes.fastjson1                    thrpt    5   575.322 ?  2.278  ops/ms
EishayWriteUTF8Bytes.fastjson2                    thrpt    5  1604.133 ?  1.318  ops/ms
EishayWriteUTF8Bytes.gson                         thrpt    5   389.557 ?  0.569  ops/ms
EishayWriteUTF8Bytes.jackson                      thrpt    5   939.713 ?  4.357  ops/ms
```
# ecs.g7.xlarge-jdk-11.0.19
```java
Benchmark                                          Mode  Cnt     Score    Error   Units
EishayParseBinary.fastjson2UTF8Bytes              thrpt    5  1201.364 ?  1.012  ops/ms
EishayParseBinary.hessian                         thrpt    5   226.872 ?  1.028  ops/ms
EishayParseBinary.javaSerialize                   thrpt    5    49.202 ?  0.190  ops/ms
EishayParseBinary.jsonb                           thrpt    5  2659.893 ?  8.480  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes  thrpt    5  1986.436 ?  1.277  ops/ms
EishayParseBinaryArrayMapping.jsonb               thrpt    5  3756.585 ?  6.254  ops/ms
EishayParseBinaryArrayMapping.kryo                thrpt    5  1657.078 ?  0.683  ops/ms
EishayParseBinaryArrayMapping.protobuf            thrpt    5  1301.953 ?  2.126  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB          thrpt    5  1989.474 ?  2.649  ops/ms
EishayParseBinaryAutoType.hessian                 thrpt    5   252.734 ?  0.419  ops/ms
EishayParseBinaryAutoType.javaSerialize           thrpt    5    47.535 ?  0.085  ops/ms
EishayParseString.fastjson1                       thrpt    5   894.412 ? 15.214  ops/ms
EishayParseString.fastjson2                       thrpt    5  1211.641 ?  2.036  ops/ms
EishayParseString.gson                            thrpt    5   429.635 ?  1.243  ops/ms
EishayParseString.jackson                         thrpt    5   485.497 ?  0.832  ops/ms
EishayParseStringPretty.fastjson1                 thrpt    5   243.630 ?  0.175  ops/ms
EishayParseStringPretty.fastjson2                 thrpt    5   912.808 ?  1.921  ops/ms
EishayParseStringPretty.gson                      thrpt    5   379.566 ?  1.821  ops/ms
EishayParseStringPretty.jackson                   thrpt    5   428.744 ?  0.796  ops/ms
EishayParseTreeString.fastjson1                   thrpt    5   415.438 ?  0.362  ops/ms
EishayParseTreeString.fastjson2                   thrpt    5   825.688 ?  1.312  ops/ms
EishayParseTreeString.gson                        thrpt    5   312.196 ?  1.033  ops/ms
EishayParseTreeString.jackson                     thrpt    5   470.936 ?  1.155  ops/ms
EishayParseTreeStringPretty.fastjson1             thrpt    5   353.383 ?  0.379  ops/ms
EishayParseTreeStringPretty.fastjson2             thrpt    5   696.535 ?  0.479  ops/ms
EishayParseTreeStringPretty.gson                  thrpt    5   292.360 ?  1.265  ops/ms
EishayParseTreeStringPretty.jackson               thrpt    5   442.777 ?  0.324  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                thrpt    5   365.524 ?  2.303  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                thrpt    5   786.057 ?  1.240  ops/ms
EishayParseTreeUTF8Bytes.gson                     thrpt    5   309.741 ?  1.101  ops/ms
EishayParseTreeUTF8Bytes.jackson                  thrpt    5   515.198 ?  0.439  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1          thrpt    5   314.888 ?  1.029  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2          thrpt    5   783.009 ?  1.375  ops/ms
EishayParseTreeUTF8BytesPretty.gson               thrpt    5   284.527 ?  1.059  ops/ms
EishayParseTreeUTF8BytesPretty.jackson            thrpt    5   480.607 ?  0.723  ops/ms
EishayParseUTF8Bytes.fastjson1                    thrpt    5   737.617 ?  0.591  ops/ms
EishayParseUTF8Bytes.fastjson2                    thrpt    5  1198.901 ?  2.513  ops/ms
EishayParseUTF8Bytes.gson                         thrpt    5   309.160 ?  0.428  ops/ms
EishayParseUTF8Bytes.jackson                      thrpt    5   554.518 ? 10.312  ops/ms
EishayParseUTF8BytesPretty.fastjson1              thrpt    5   228.741 ?  0.308  ops/ms
EishayParseUTF8BytesPretty.fastjson2              thrpt    5   915.013 ?  1.131  ops/ms
EishayParseUTF8BytesPretty.gson                   thrpt    5   297.117 ?  0.491  ops/ms
EishayParseUTF8BytesPretty.jackson                thrpt    5   496.005 ?  0.537  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes              thrpt    5  1631.457 ?  3.185  ops/ms
EishayWriteBinary.hessian                         thrpt    5   329.514 ?  0.296  ops/ms
EishayWriteBinary.javaSerialize                   thrpt    5   233.638 ?  1.057  ops/ms
EishayWriteBinary.jsonb                           thrpt    5  2610.309 ?  3.361  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes  thrpt    5  2157.545 ?  2.558  ops/ms
EishayWriteBinaryArrayMapping.jsonb               thrpt    5  5232.945 ? 10.258  ops/ms
EishayWriteBinaryArrayMapping.kryo                thrpt    5  2002.510 ?  1.854  ops/ms
EishayWriteBinaryArrayMapping.protobuf            thrpt    5  1396.293 ?  3.306  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB          thrpt    5  1437.120 ?  3.242  ops/ms
EishayWriteBinaryAutoType.hessian                 thrpt    5   327.902 ?  2.253  ops/ms
EishayWriteBinaryAutoType.javaSerialize           thrpt    5   234.430 ?  1.298  ops/ms
EishayWriteString.fastjson1                       thrpt    5   579.808 ?  0.980  ops/ms
EishayWriteString.fastjson2                       thrpt    5  1461.183 ?  6.155  ops/ms
EishayWriteString.gson                            thrpt    5   355.461 ?  2.213  ops/ms
EishayWriteString.jackson                         thrpt    5   950.495 ?  0.809  ops/ms
EishayWriteStringTree.fastjson1                   thrpt    5   775.552 ?  1.987  ops/ms
EishayWriteStringTree.fastjson2                   thrpt    5  1302.780 ?  3.397  ops/ms
EishayWriteStringTree.gson                        thrpt    5   372.003 ?  0.960  ops/ms
EishayWriteStringTree.jackson                     thrpt    5   937.694 ?  1.313  ops/ms
EishayWriteUTF8Bytes.fastjson1                    thrpt    5   546.694 ?  0.476  ops/ms
EishayWriteUTF8Bytes.fastjson2                    thrpt    5  1532.657 ?  2.352  ops/ms
EishayWriteUTF8Bytes.gson                         thrpt    5   344.385 ?  1.112  ops/ms
EishayWriteUTF8Bytes.jackson                      thrpt    5   862.945 ?  1.040  ops/ms
```
# ecs.g7.xlarge-jdk-17.0.7
```java
Benchmark                                          Mode  Cnt     Score    Error   Units
EishayParseBinary.fastjson2UTF8Bytes              thrpt    5  1320.229 ?  4.864  ops/ms
EishayParseBinary.hessian                         thrpt    5   269.114 ?  0.196  ops/ms
EishayParseBinary.javaSerialize                   thrpt    5    53.329 ?  0.074  ops/ms
EishayParseBinary.jsonb                           thrpt    5  2991.681 ?  8.220  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes  thrpt    5  2268.066 ?  3.439  ops/ms
EishayParseBinaryArrayMapping.jsonb               thrpt    5  4553.583 ?  7.624  ops/ms
EishayParseBinaryArrayMapping.kryo                thrpt    5  1802.391 ?  2.894  ops/ms
EishayParseBinaryArrayMapping.protobuf            thrpt    5  1733.013 ?  3.759  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB          thrpt    5  2068.466 ? 18.756  ops/ms
EishayParseBinaryAutoType.hessian                 thrpt    5   267.611 ?  0.340  ops/ms
EishayParseBinaryAutoType.javaSerialize           thrpt    5    53.321 ?  0.029  ops/ms
EishayParseString.fastjson1                       thrpt    5  1251.820 ?  0.881  ops/ms
EishayParseString.fastjson2                       thrpt    5  1338.427 ?  1.981  ops/ms
EishayParseString.gson                            thrpt    5   454.755 ?  0.869  ops/ms
EishayParseString.jackson                         thrpt    5   492.196 ?  1.118  ops/ms
EishayParseStringPretty.fastjson1                 thrpt    5   298.352 ?  0.379  ops/ms
EishayParseStringPretty.fastjson2                 thrpt    5   946.852 ?  4.395  ops/ms
EishayParseStringPretty.gson                      thrpt    5   414.222 ?  0.407  ops/ms
EishayParseStringPretty.jackson                   thrpt    5   450.022 ?  0.391  ops/ms
EishayParseTreeString.fastjson1                   thrpt    5   581.415 ?  0.199  ops/ms
EishayParseTreeString.fastjson2                   thrpt    5  1074.247 ?  0.589  ops/ms
EishayParseTreeString.gson                        thrpt    5   317.445 ?  0.500  ops/ms
EishayParseTreeString.jackson                     thrpt    5   499.647 ?  0.839  ops/ms
EishayParseTreeStringPretty.fastjson1             thrpt    5   488.057 ?  1.477  ops/ms
EishayParseTreeStringPretty.fastjson2             thrpt    5   845.101 ?  1.290  ops/ms
EishayParseTreeStringPretty.gson                  thrpt    5   298.671 ?  0.474  ops/ms
EishayParseTreeStringPretty.jackson               thrpt    5   463.301 ?  0.483  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                thrpt    5   500.590 ?  0.527  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                thrpt    5  1046.461 ?  0.324  ops/ms
EishayParseTreeUTF8Bytes.gson                     thrpt    5   309.358 ?  0.402  ops/ms
EishayParseTreeUTF8Bytes.jackson                  thrpt    5   581.934 ?  1.017  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1          thrpt    5   420.182 ?  1.446  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2          thrpt    5   863.928 ? 16.885  ops/ms
EishayParseTreeUTF8BytesPretty.gson               thrpt    5   291.080 ?  0.694  ops/ms
EishayParseTreeUTF8BytesPretty.jackson            thrpt    5   530.772 ?  0.630  ops/ms
EishayParseUTF8Bytes.fastjson1                    thrpt    5   954.016 ?  1.541  ops/ms
EishayParseUTF8Bytes.fastjson2                    thrpt    5  1337.973 ?  1.497  ops/ms
EishayParseUTF8Bytes.gson                         thrpt    5   311.488 ?  0.818  ops/ms
EishayParseUTF8Bytes.jackson                      thrpt    5   549.383 ?  0.203  ops/ms
EishayParseUTF8BytesPretty.fastjson1              thrpt    5   274.706 ?  0.108  ops/ms
EishayParseUTF8BytesPretty.fastjson2              thrpt    5   961.547 ?  1.170  ops/ms
EishayParseUTF8BytesPretty.gson                   thrpt    5   296.755 ?  0.396  ops/ms
EishayParseUTF8BytesPretty.jackson                thrpt    5   480.712 ?  0.324  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes              thrpt    5  1937.813 ?  1.500  ops/ms
EishayWriteBinary.hessian                         thrpt    5   327.441 ?  0.578  ops/ms
EishayWriteBinary.javaSerialize                   thrpt    5   233.403 ?  1.117  ops/ms
EishayWriteBinary.jsonb                           thrpt    5  3713.037 ?  3.744  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes  thrpt    5  2206.509 ?  2.266  ops/ms
EishayWriteBinaryArrayMapping.jsonb               thrpt    5  6218.985 ?  7.363  ops/ms
EishayWriteBinaryArrayMapping.kryo                thrpt    5  2021.853 ?  1.461  ops/ms
EishayWriteBinaryArrayMapping.protobuf            thrpt    5  1743.386 ?  8.896  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB          thrpt    5  1694.588 ?  4.764  ops/ms
EishayWriteBinaryAutoType.hessian                 thrpt    5   325.904 ?  0.454  ops/ms
EishayWriteBinaryAutoType.javaSerialize           thrpt    5   235.283 ?  0.343  ops/ms
EishayWriteString.fastjson1                       thrpt    5   638.808 ?  1.171  ops/ms
EishayWriteString.fastjson2                       thrpt    5  1706.875 ?  1.423  ops/ms
EishayWriteString.gson                            thrpt    5   241.802 ?  0.125  ops/ms
EishayWriteString.jackson                         thrpt    5  1017.566 ?  1.383  ops/ms
EishayWriteStringTree.fastjson1                   thrpt    5   803.770 ?  1.537  ops/ms
EishayWriteStringTree.fastjson2                   thrpt    5  1233.027 ?  1.518  ops/ms
EishayWriteStringTree.gson                        thrpt    5   247.527 ?  0.335  ops/ms
EishayWriteStringTree.jackson                     thrpt    5   893.873 ?  7.656  ops/ms
EishayWriteUTF8Bytes.fastjson1                    thrpt    5   579.941 ?  2.413  ops/ms
EishayWriteUTF8Bytes.fastjson2                    thrpt    5  1946.599 ?  1.382  ops/ms
EishayWriteUTF8Bytes.gson                         thrpt    5   239.196 ?  0.758  ops/ms
EishayWriteUTF8Bytes.jackson                      thrpt    5   996.164 ?  2.257  ops/ms
```
# ecs.g8m.xlarge-jdk1.8.0_371
```java
Benchmark                                          Mode  Cnt     Score    Error   Units
EishayParseBinary.fastjson2UTF8Bytes              thrpt    5   787.631 ? 46.645  ops/ms
EishayParseBinary.hessian                         thrpt    5   205.654 ?  6.388  ops/ms
EishayParseBinary.javaSerialize                   thrpt    5    39.758 ?  0.537  ops/ms
EishayParseBinary.jsonb                           thrpt    5  1314.074 ? 60.747  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes  thrpt    5  1216.588 ? 36.467  ops/ms
EishayParseBinaryArrayMapping.jsonb               thrpt    5  2377.704 ? 88.153  ops/ms
EishayParseBinaryArrayMapping.kryo                thrpt    5  1182.491 ?  9.575  ops/ms
EishayParseBinaryArrayMapping.protobuf            thrpt    5   795.838 ? 52.560  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB          thrpt    5  1137.556 ? 22.646  ops/ms
EishayParseBinaryAutoType.hessian                 thrpt    5   210.365 ?  7.035  ops/ms
EishayParseBinaryAutoType.javaSerialize           thrpt    5    38.395 ?  0.596  ops/ms
EishayParseString.fastjson1                       thrpt    5   742.109 ? 39.900  ops/ms
EishayParseString.fastjson2                       thrpt    5   868.417 ? 32.262  ops/ms
EishayParseString.gson                            thrpt    5   352.621 ?  7.963  ops/ms
EishayParseString.jackson                         thrpt    5   354.219 ?  3.357  ops/ms
EishayParseStringPretty.fastjson1                 thrpt    5   201.686 ?  4.397  ops/ms
EishayParseStringPretty.fastjson2                 thrpt    5   700.888 ? 10.424  ops/ms
EishayParseStringPretty.gson                      thrpt    5   330.010 ? 11.661  ops/ms
EishayParseStringPretty.jackson                   thrpt    5   315.288 ? 10.732  ops/ms
EishayParseTreeString.fastjson1                   thrpt    5   300.253 ? 23.005  ops/ms
EishayParseTreeString.fastjson2                   thrpt    5   533.879 ? 35.481  ops/ms
EishayParseTreeString.gson                        thrpt    5   257.519 ? 24.099  ops/ms
EishayParseTreeString.jackson                     thrpt    5   292.611 ? 14.047  ops/ms
EishayParseTreeStringPretty.fastjson1             thrpt    5   278.709 ?  6.038  ops/ms
EishayParseTreeStringPretty.fastjson2             thrpt    5   479.404 ? 27.528  ops/ms
EishayParseTreeStringPretty.gson                  thrpt    5   252.691 ? 12.688  ops/ms
EishayParseTreeStringPretty.jackson               thrpt    5   275.643 ?  6.216  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                thrpt    5   267.126 ? 15.922  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                thrpt    5   530.738 ? 22.716  ops/ms
EishayParseTreeUTF8Bytes.gson                     thrpt    5   227.156 ? 18.054  ops/ms
EishayParseTreeUTF8Bytes.jackson                  thrpt    5   302.175 ? 23.056  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1          thrpt    5   237.753 ?  5.191  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2          thrpt    5   480.616 ? 15.571  ops/ms
EishayParseTreeUTF8BytesPretty.gson               thrpt    5   212.145 ?  5.865  ops/ms
EishayParseTreeUTF8BytesPretty.jackson            thrpt    5   303.921 ? 11.544  ops/ms
EishayParseUTF8Bytes.fastjson1                    thrpt    5   670.306 ? 10.434  ops/ms
EishayParseUTF8Bytes.fastjson2                    thrpt    5   798.497 ? 19.758  ops/ms
EishayParseUTF8Bytes.gson                         thrpt    5   231.252 ?  8.458  ops/ms
EishayParseUTF8Bytes.jackson                      thrpt    5   395.075 ? 22.663  ops/ms
EishayParseUTF8BytesPretty.fastjson1              thrpt    5   199.692 ?  5.323  ops/ms
EishayParseUTF8BytesPretty.fastjson2              thrpt    5   666.450 ?  5.113  ops/ms
EishayParseUTF8BytesPretty.gson                   thrpt    5   230.013 ?  5.826  ops/ms
EishayParseUTF8BytesPretty.jackson                thrpt    5   364.506 ? 19.089  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes              thrpt    5  1346.469 ? 31.019  ops/ms
EishayWriteBinary.hessian                         thrpt    5   351.861 ?  6.942  ops/ms
EishayWriteBinary.javaSerialize                   thrpt    5   209.251 ?  2.763  ops/ms
EishayWriteBinary.jsonb                           thrpt    5  1785.742 ? 50.644  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes  thrpt    5  1612.142 ? 19.446  ops/ms
EishayWriteBinaryArrayMapping.jsonb               thrpt    5  2876.746 ? 73.430  ops/ms
EishayWriteBinaryArrayMapping.kryo                thrpt    5  1448.851 ?  8.408  ops/ms
EishayWriteBinaryArrayMapping.protobuf            thrpt    5   954.720 ? 45.010  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB          thrpt    5  1146.915 ? 12.800  ops/ms
EishayWriteBinaryAutoType.hessian                 thrpt    5   340.535 ?  7.939  ops/ms
EishayWriteBinaryAutoType.javaSerialize           thrpt    5   206.677 ?  2.367  ops/ms
EishayWriteString.fastjson1                       thrpt    5   522.106 ? 14.820  ops/ms
EishayWriteString.fastjson2                       thrpt    5  1339.769 ? 16.112  ops/ms
EishayWriteString.gson                            thrpt    5   378.320 ?  3.690  ops/ms
EishayWriteString.jackson                         thrpt    5   634.644 ?  6.537  ops/ms
EishayWriteStringTree.fastjson1                   thrpt    5   590.771 ? 15.955  ops/ms
EishayWriteStringTree.fastjson2                   thrpt    5   962.525 ? 11.667  ops/ms
EishayWriteStringTree.gson                        thrpt    5   424.483 ?  6.116  ops/ms
EishayWriteStringTree.jackson                     thrpt    5   703.541 ? 13.892  ops/ms
EishayWriteUTF8Bytes.fastjson1                    thrpt    5   479.858 ?  9.295  ops/ms
EishayWriteUTF8Bytes.fastjson2                    thrpt    5  1337.171 ? 25.351  ops/ms
EishayWriteUTF8Bytes.gson                         thrpt    5   303.740 ?  1.875  ops/ms
EishayWriteUTF8Bytes.jackson                      thrpt    5   678.596 ?  9.842  ops/ms
```
# ecs.g8m.xlarge-jdk-11.0.19
```java
Benchmark                                          Mode  Cnt     Score    Error   Units
EishayParseBinary.fastjson2UTF8Bytes              thrpt    5  1064.587 ?  5.029  ops/ms
EishayParseBinary.hessian                         thrpt    5   213.910 ?  1.152  ops/ms
EishayParseBinary.javaSerialize                   thrpt    5    40.784 ?  0.216  ops/ms
EishayParseBinary.jsonb                           thrpt    5  2056.391 ? 43.700  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes  thrpt    5  1597.309 ? 14.805  ops/ms
EishayParseBinaryArrayMapping.jsonb               thrpt    5  2992.282 ? 48.421  ops/ms
EishayParseBinaryArrayMapping.kryo                thrpt    5  1424.500 ?  8.369  ops/ms
EishayParseBinaryArrayMapping.protobuf            thrpt    5  1019.717 ? 13.931  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB          thrpt    5  1553.043 ? 28.749  ops/ms
EishayParseBinaryAutoType.hessian                 thrpt    5   218.066 ?  1.321  ops/ms
EishayParseBinaryAutoType.javaSerialize           thrpt    5    40.865 ?  0.156  ops/ms
EishayParseString.fastjson1                       thrpt    5   809.328 ? 11.085  ops/ms
EishayParseString.fastjson2                       thrpt    5  1080.554 ?  5.792  ops/ms
EishayParseString.gson                            thrpt    5   378.228 ?  2.330  ops/ms
EishayParseString.jackson                         thrpt    5   401.314 ?  1.635  ops/ms
EishayParseStringPretty.fastjson1                 thrpt    5   239.083 ?  2.285  ops/ms
EishayParseStringPretty.fastjson2                 thrpt    5   787.003 ?  3.852  ops/ms
EishayParseStringPretty.gson                      thrpt    5   352.488 ?  2.380  ops/ms
EishayParseStringPretty.jackson                   thrpt    5   354.847 ?  2.823  ops/ms
EishayParseTreeString.fastjson1                   thrpt    5   373.025 ?  2.035  ops/ms
EishayParseTreeString.fastjson2                   thrpt    5   770.017 ? 12.912  ops/ms
EishayParseTreeString.gson                        thrpt    5   294.402 ?  2.887  ops/ms
EishayParseTreeString.jackson                     thrpt    5   353.511 ?  1.857  ops/ms
EishayParseTreeStringPretty.fastjson1             thrpt    5   320.452 ?  3.122  ops/ms
EishayParseTreeStringPretty.fastjson2             thrpt    5   644.137 ?  7.284  ops/ms
EishayParseTreeStringPretty.gson                  thrpt    5   275.572 ?  1.385  ops/ms
EishayParseTreeStringPretty.jackson               thrpt    5   338.243 ?  3.675  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                thrpt    5   338.831 ?  2.387  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                thrpt    5   771.861 ? 14.980  ops/ms
EishayParseTreeUTF8Bytes.gson                     thrpt    5   294.467 ?  6.995  ops/ms
EishayParseTreeUTF8Bytes.jackson                  thrpt    5   397.511 ?  8.106  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1          thrpt    5   282.588 ? 38.782  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2          thrpt    5   638.830 ?  8.399  ops/ms
EishayParseTreeUTF8BytesPretty.gson               thrpt    5   270.370 ?  1.550  ops/ms
EishayParseTreeUTF8BytesPretty.jackson            thrpt    5   387.020 ?  3.164  ops/ms
EishayParseUTF8Bytes.fastjson1                    thrpt    5   677.180 ? 23.056  ops/ms
EishayParseUTF8Bytes.fastjson2                    thrpt    5  1073.263 ?  9.993  ops/ms
EishayParseUTF8Bytes.gson                         thrpt    5   289.494 ?  2.147  ops/ms
EishayParseUTF8Bytes.jackson                      thrpt    5   445.167 ?  3.139  ops/ms
EishayParseUTF8BytesPretty.fastjson1              thrpt    5   222.209 ?  2.869  ops/ms
EishayParseUTF8BytesPretty.fastjson2              thrpt    5   776.640 ?  3.922  ops/ms
EishayParseUTF8BytesPretty.gson                   thrpt    5   277.411 ?  5.558  ops/ms
EishayParseUTF8BytesPretty.jackson                thrpt    5   407.498 ?  2.656  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes              thrpt    5  1483.311 ? 10.291  ops/ms
EishayWriteBinary.hessian                         thrpt    5   365.817 ?  1.627  ops/ms
EishayWriteBinary.javaSerialize                   thrpt    5   222.724 ?  0.546  ops/ms
EishayWriteBinary.jsonb                           thrpt    5  2282.981 ? 32.170  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes  thrpt    5  1829.902 ? 23.431  ops/ms
EishayWriteBinaryArrayMapping.jsonb               thrpt    5  4470.941 ? 53.460  ops/ms
EishayWriteBinaryArrayMapping.kryo                thrpt    5  1608.262 ?  2.432  ops/ms
EishayWriteBinaryArrayMapping.protobuf            thrpt    5  1316.244 ? 24.440  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB          thrpt    5  1350.768 ?  8.116  ops/ms
EishayWriteBinaryAutoType.hessian                 thrpt    5   354.219 ?  3.153  ops/ms
EishayWriteBinaryAutoType.javaSerialize           thrpt    5   223.331 ?  1.439  ops/ms
EishayWriteString.fastjson1                       thrpt    5   524.922 ?  3.951  ops/ms
EishayWriteString.fastjson2                       thrpt    5  1375.933 ?  5.472  ops/ms
EishayWriteString.gson                            thrpt    5   317.848 ? 68.308  ops/ms
EishayWriteString.jackson                         thrpt    5   716.433 ? 11.307  ops/ms
EishayWriteStringTree.fastjson1                   thrpt    5   640.534 ?  5.235  ops/ms
EishayWriteStringTree.fastjson2                   thrpt    5  1139.304 ?  4.720  ops/ms
EishayWriteStringTree.gson                        thrpt    5   374.348 ?  2.186  ops/ms
EishayWriteStringTree.jackson                     thrpt    5   709.481 ?  4.031  ops/ms
EishayWriteUTF8Bytes.fastjson1                    thrpt    5   536.723 ?  1.441  ops/ms
EishayWriteUTF8Bytes.fastjson2                    thrpt    5  1481.772 ?  6.012  ops/ms
EishayWriteUTF8Bytes.gson                         thrpt    5   329.426 ?  0.495  ops/ms
EishayWriteUTF8Bytes.jackson                      thrpt    5   655.340 ?  6.849  ops/ms
```
# ecs.g8m.xlarge-jdk-17.0.7
```java
Benchmark                                          Mode  Cnt     Score    Error   Units
EishayParseBinary.fastjson2UTF8Bytes              thrpt    5  1098.261 ? 17.713  ops/ms
EishayParseBinary.hessian                         thrpt    5   232.903 ?  2.458  ops/ms
EishayParseBinary.javaSerialize                   thrpt    5    41.789 ?  0.224  ops/ms
EishayParseBinary.jsonb                           thrpt    5  2116.528 ? 92.978  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes  thrpt    5  1633.581 ? 22.402  ops/ms
EishayParseBinaryArrayMapping.jsonb               thrpt    5  3093.614 ? 44.429  ops/ms
EishayParseBinaryArrayMapping.kryo                thrpt    5  1460.077 ? 30.086  ops/ms
EishayParseBinaryArrayMapping.protobuf            thrpt    5  1305.106 ? 35.607  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB          thrpt    5  1588.355 ?  8.172  ops/ms
EishayParseBinaryAutoType.hessian                 thrpt    5   237.336 ?  2.230  ops/ms
EishayParseBinaryAutoType.javaSerialize           thrpt    5    42.336 ?  0.236  ops/ms
EishayParseString.fastjson1                       thrpt    5  1117.076 ? 11.630  ops/ms
EishayParseString.fastjson2                       thrpt    5  1121.071 ?  7.313  ops/ms
EishayParseString.gson                            thrpt    5   386.301 ?  1.876  ops/ms
EishayParseString.jackson                         thrpt    5   401.921 ?  3.036  ops/ms
EishayParseStringPretty.fastjson1                 thrpt    5   267.451 ?  1.195  ops/ms
EishayParseStringPretty.fastjson2                 thrpt    5   804.723 ?  6.186  ops/ms
EishayParseStringPretty.gson                      thrpt    5   355.120 ?  2.152  ops/ms
EishayParseStringPretty.jackson                   thrpt    5   370.302 ?  2.207  ops/ms
EishayParseTreeString.fastjson1                   thrpt    5   400.742 ?  5.342  ops/ms
EishayParseTreeString.fastjson2                   thrpt    5   804.500 ? 13.895  ops/ms
EishayParseTreeString.gson                        thrpt    5   306.193 ?  2.362  ops/ms
EishayParseTreeString.jackson                     thrpt    5   384.257 ?  2.213  ops/ms
EishayParseTreeStringPretty.fastjson1             thrpt    5   356.260 ?  2.815  ops/ms
EishayParseTreeStringPretty.fastjson2             thrpt    5   651.421 ? 12.976  ops/ms
EishayParseTreeStringPretty.gson                  thrpt    5   285.840 ?  2.811  ops/ms
EishayParseTreeStringPretty.jackson               thrpt    5   355.629 ?  5.394  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                thrpt    5   361.333 ?  5.919  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                thrpt    5   811.563 ?  9.997  ops/ms
EishayParseTreeUTF8Bytes.gson                     thrpt    5   294.572 ?  2.676  ops/ms
EishayParseTreeUTF8Bytes.jackson                  thrpt    5   460.635 ?  7.664  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1          thrpt    5   321.323 ?  1.721  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2          thrpt    5   639.707 ?  4.573  ops/ms
EishayParseTreeUTF8BytesPretty.gson               thrpt    5   277.005 ?  2.102  ops/ms
EishayParseTreeUTF8BytesPretty.jackson            thrpt    5   421.938 ?  5.733  ops/ms
EishayParseUTF8Bytes.fastjson1                    thrpt    5   867.747 ? 10.738  ops/ms
EishayParseUTF8Bytes.fastjson2                    thrpt    5  1109.418 ?  9.952  ops/ms
EishayParseUTF8Bytes.gson                         thrpt    5   293.297 ?  2.945  ops/ms
EishayParseUTF8Bytes.jackson                      thrpt    5   458.061 ?  5.257  ops/ms
EishayParseUTF8BytesPretty.fastjson1              thrpt    5   247.224 ?  1.735  ops/ms
EishayParseUTF8BytesPretty.fastjson2              thrpt    5   813.047 ?  6.831  ops/ms
EishayParseUTF8BytesPretty.gson                   thrpt    5   270.749 ?  4.302  ops/ms
EishayParseUTF8BytesPretty.jackson                thrpt    5   421.556 ?  2.640  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes              thrpt    5  1458.832 ? 10.785  ops/ms
EishayWriteBinary.hessian                         thrpt    5   351.838 ?  1.861  ops/ms
EishayWriteBinary.javaSerialize                   thrpt    5   220.535 ?  1.644  ops/ms
EishayWriteBinary.jsonb                           thrpt    5  2363.805 ? 17.086  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes  thrpt    5  1807.765 ?  3.281  ops/ms
EishayWriteBinaryArrayMapping.jsonb               thrpt    5  4793.138 ? 63.911  ops/ms
EishayWriteBinaryArrayMapping.kryo                thrpt    5  1535.164 ?  8.795  ops/ms
EishayWriteBinaryArrayMapping.protobuf            thrpt    5  1468.361 ? 24.871  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB          thrpt    5  1369.095 ? 56.898  ops/ms
EishayWriteBinaryAutoType.hessian                 thrpt    5   357.008 ?  1.199  ops/ms
EishayWriteBinaryAutoType.javaSerialize           thrpt    5   215.655 ?  1.079  ops/ms
EishayWriteString.fastjson1                       thrpt    5   530.787 ?  1.172  ops/ms
EishayWriteString.fastjson2                       thrpt    5  1353.222 ?  5.549  ops/ms
EishayWriteString.gson                            thrpt    5   226.081 ?  0.689  ops/ms
EishayWriteString.jackson                         thrpt    5   701.815 ? 12.565  ops/ms
EishayWriteStringTree.fastjson1                   thrpt    5   657.677 ?  2.252  ops/ms
EishayWriteStringTree.fastjson2                   thrpt    5  1141.969 ?  3.930  ops/ms
EishayWriteStringTree.gson                        thrpt    5   227.643 ?  1.201  ops/ms
EishayWriteStringTree.jackson                     thrpt    5   690.850 ?  4.567  ops/ms
EishayWriteUTF8Bytes.fastjson1                    thrpt    5   485.617 ?  2.508  ops/ms
EishayWriteUTF8Bytes.fastjson2                    thrpt    5  1469.373 ? 11.409  ops/ms
EishayWriteUTF8Bytes.gson                         thrpt    5   220.625 ?  1.517  ops/ms
EishayWriteUTF8Bytes.jackson                      thrpt    5   713.611 ?  1.164  ops/ms
```
# OrangePI5-jdk1.8.0_371
```java
Benchmark                                          Mode  Cnt     Score     Error   Units
EishayParseBinary.fastjson2UTF8Bytes              thrpt    5   580.721 ±  43.497  ops/ms
EishayParseBinary.hessian                         thrpt    5   129.780 ±   2.628  ops/ms
EishayParseBinary.javaSerialize                   thrpt    5    27.284 ±   1.179  ops/ms
EishayParseBinary.jsonb                           thrpt    5   974.711 ±  56.997  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes  thrpt    5   846.744 ±  24.468  ops/ms
EishayParseBinaryArrayMapping.jsonb               thrpt    5  1663.366 ±  61.723  ops/ms
EishayParseBinaryArrayMapping.kryo                thrpt    5   961.558 ±  45.308  ops/ms
EishayParseBinaryArrayMapping.protobuf            thrpt    5   619.539 ±  35.980  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB          thrpt    5   629.459 ±  43.448  ops/ms
EishayParseBinaryAutoType.hessian                 thrpt    5   149.438 ±   8.109  ops/ms
EishayParseBinaryAutoType.javaSerialize           thrpt    5    25.835 ±   1.694  ops/ms
EishayParseString.fastjson1                       thrpt    5   545.476 ±  15.685  ops/ms
EishayParseString.fastjson2                       thrpt    5   644.371 ±  66.317  ops/ms
EishayParseString.gson                            thrpt    5   218.286 ±   7.941  ops/ms
EishayParseString.jackson                         thrpt    5   270.504 ±   6.241  ops/ms
EishayParseStringPretty.fastjson1                 thrpt    5   155.593 ±   7.070  ops/ms
EishayParseStringPretty.fastjson2                 thrpt    5   532.539 ±  34.045  ops/ms
EishayParseStringPretty.gson                      thrpt    5   201.412 ±  11.627  ops/ms
EishayParseStringPretty.jackson                   thrpt    5   239.492 ±   7.079  ops/ms
EishayParseTreeString.fastjson1                   thrpt    5   220.388 ±  18.798  ops/ms
EishayParseTreeString.fastjson2                   thrpt    5   424.899 ±  19.893  ops/ms
EishayParseTreeString.gson                        thrpt    5   176.076 ±   7.211  ops/ms
EishayParseTreeString.jackson                     thrpt    5   232.725 ±  11.829  ops/ms
EishayParseTreeStringPretty.fastjson1             thrpt    5   205.527 ±   7.834  ops/ms
EishayParseTreeStringPretty.fastjson2             thrpt    5   382.213 ±  13.486  ops/ms
EishayParseTreeStringPretty.gson                  thrpt    5   165.855 ±  10.503  ops/ms
EishayParseTreeStringPretty.jackson               thrpt    5   221.050 ±   7.497  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                thrpt    5   177.366 ±   3.127  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                thrpt    5   410.969 ±   4.999  ops/ms
EishayParseTreeUTF8Bytes.gson                     thrpt    5   155.807 ±   4.192  ops/ms
EishayParseTreeUTF8Bytes.jackson                  thrpt    5   266.229 ±  13.098  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1          thrpt    5   148.663 ±   6.548  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2          thrpt    5   366.709 ±  15.712  ops/ms
EishayParseTreeUTF8BytesPretty.gson               thrpt    5   142.969 ±   4.776  ops/ms
EishayParseTreeUTF8BytesPretty.jackson            thrpt    5   249.830 ±  10.644  ops/ms
EishayParseUTF8Bytes.fastjson1                    thrpt    5   401.205 ±  24.279  ops/ms
EishayParseUTF8Bytes.fastjson2                    thrpt    5   588.592 ±  28.233  ops/ms
EishayParseUTF8Bytes.gson                         thrpt    5   153.767 ±   7.970  ops/ms
EishayParseUTF8Bytes.jackson                      thrpt    5   310.008 ±  14.913  ops/ms
EishayParseUTF8BytesPretty.fastjson1              thrpt    5   136.853 ±   4.383  ops/ms
EishayParseUTF8BytesPretty.fastjson2              thrpt    5   491.103 ±  14.164  ops/ms
EishayParseUTF8BytesPretty.gson                   thrpt    5   149.820 ±   5.302  ops/ms
EishayParseUTF8BytesPretty.jackson                thrpt    5   295.040 ±  16.022  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes              thrpt    5   847.016 ±  42.940  ops/ms
EishayWriteBinary.hessian                         thrpt    5   187.730 ±  43.102  ops/ms
EishayWriteBinary.javaSerialize                   thrpt    5   122.014 ±   6.116  ops/ms
EishayWriteBinary.jsonb                           thrpt    5  1050.557 ±  57.196  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes  thrpt    5  1036.254 ±  12.489  ops/ms
EishayWriteBinaryArrayMapping.jsonb               thrpt    5  1635.369 ± 142.577  ops/ms
EishayWriteBinaryArrayMapping.kryo                thrpt    5   848.635 ±  58.856  ops/ms
EishayWriteBinaryArrayMapping.protobuf            thrpt    5   697.370 ±  37.591  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB          thrpt    5   696.080 ±  11.382  ops/ms
EishayWriteBinaryAutoType.hessian                 thrpt    5   183.123 ±   3.216  ops/ms
EishayWriteBinaryAutoType.javaSerialize           thrpt    5   119.456 ±   4.030  ops/ms
EishayWriteString.fastjson1                       thrpt    5   304.198 ±   9.794  ops/ms
EishayWriteString.fastjson2                       thrpt    5   796.679 ±  38.484  ops/ms
EishayWriteString.gson                            thrpt    5   206.845 ±   8.615  ops/ms
EishayWriteString.jackson                         thrpt    5   376.198 ±  26.791  ops/ms
EishayWriteStringTree.fastjson1                   thrpt    5   334.329 ±  13.689  ops/ms
EishayWriteStringTree.fastjson2                   thrpt    5   499.099 ±  47.294  ops/ms
EishayWriteStringTree.gson                        thrpt    5   224.586 ±   9.363  ops/ms
EishayWriteStringTree.jackson                     thrpt    5   384.317 ±  14.084  ops/ms
EishayWriteUTF8Bytes.fastjson1                    thrpt    5   267.822 ±  11.617  ops/ms
EishayWriteUTF8Bytes.fastjson2                    thrpt    5   843.261 ±  51.983  ops/ms
EishayWriteUTF8Bytes.gson                         thrpt    5   176.107 ±   6.374  ops/ms
EishayWriteUTF8Bytes.jackson                      thrpt    5   396.633 ±  20.850  ops/ms
```
# OrangePI5-jdk-11.0.19
```java
Benchmark                                          Mode  Cnt     Score    Error   Units
EishayParseBinary.fastjson2UTF8Bytes              thrpt    5   688.364 ±  9.790  ops/ms
EishayParseBinary.hessian                         thrpt    5   132.056 ±  0.156  ops/ms
EishayParseBinary.javaSerialize                   thrpt    5    27.123 ±  1.000  ops/ms
EishayParseBinary.jsonb                           thrpt    5  1297.638 ± 18.509  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes  thrpt    5  1003.048 ± 86.144  ops/ms
EishayParseBinaryArrayMapping.jsonb               thrpt    5  1971.755 ± 71.332  ops/ms
EishayParseBinaryArrayMapping.kryo                thrpt    5   932.488 ± 45.778  ops/ms
EishayParseBinaryArrayMapping.protobuf            thrpt    5   838.303 ± 23.844  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB          thrpt    5   800.131 ± 19.556  ops/ms
EishayParseBinaryAutoType.hessian                 thrpt    5   130.694 ±  4.567  ops/ms
EishayParseBinaryAutoType.javaSerialize           thrpt    5    25.964 ±  1.070  ops/ms
EishayParseString.fastjson1                       thrpt    5   564.942 ± 12.155  ops/ms
EishayParseString.fastjson2                       thrpt    5   669.914 ± 22.830  ops/ms
EishayParseString.gson                            thrpt    5   224.297 ± 10.329  ops/ms
EishayParseString.jackson                         thrpt    5   262.142 ±  5.525  ops/ms
EishayParseStringPretty.fastjson1                 thrpt    5   159.294 ±  4.475  ops/ms
EishayParseStringPretty.fastjson2                 thrpt    5   520.255 ± 16.434  ops/ms
EishayParseStringPretty.gson                      thrpt    5   210.469 ±  8.008  ops/ms
EishayParseStringPretty.jackson                   thrpt    5   243.445 ± 12.309  ops/ms
EishayParseTreeString.fastjson1                   thrpt    5   237.761 ±  0.741  ops/ms
EishayParseTreeString.fastjson2                   thrpt    5   423.181 ± 15.638  ops/ms
EishayParseTreeString.gson                        thrpt    5   188.062 ±  6.590  ops/ms
EishayParseTreeString.jackson                     thrpt    5   232.018 ± 14.141  ops/ms
EishayParseTreeStringPretty.fastjson1             thrpt    5   188.630 ±  8.207  ops/ms
EishayParseTreeStringPretty.fastjson2             thrpt    5   400.461 ± 10.039  ops/ms
EishayParseTreeStringPretty.gson                  thrpt    5   177.889 ±  6.912  ops/ms
EishayParseTreeStringPretty.jackson               thrpt    5   227.320 ±  3.084  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                thrpt    5   210.719 ±  9.929  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                thrpt    5   476.579 ± 11.933  ops/ms
EishayParseTreeUTF8Bytes.gson                     thrpt    5   174.649 ±  7.405  ops/ms
EishayParseTreeUTF8Bytes.jackson                  thrpt    5   282.617 ±  5.530  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1          thrpt    5   167.274 ±  5.131  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2          thrpt    5   402.907 ±  3.680  ops/ms
EishayParseTreeUTF8BytesPretty.gson               thrpt    5   164.977 ±  1.929  ops/ms
EishayParseTreeUTF8BytesPretty.jackson            thrpt    5   269.516 ± 11.885  ops/ms
EishayParseUTF8Bytes.fastjson1                    thrpt    5   448.931 ±  3.274  ops/ms
EishayParseUTF8Bytes.fastjson2                    thrpt    5   693.756 ± 22.201  ops/ms
EishayParseUTF8Bytes.gson                         thrpt    5   175.610 ±  5.815  ops/ms
EishayParseUTF8Bytes.jackson                      thrpt    5   299.370 ±  8.859  ops/ms
EishayParseUTF8BytesPretty.fastjson1              thrpt    5   145.974 ±  6.036  ops/ms
EishayParseUTF8BytesPretty.fastjson2              thrpt    5   529.145 ± 23.264  ops/ms
EishayParseUTF8BytesPretty.gson                   thrpt    5   165.154 ±  8.944  ops/ms
EishayParseUTF8BytesPretty.jackson                thrpt    5   273.893 ±  7.873  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes              thrpt    5   918.953 ± 27.125  ops/ms
EishayWriteBinary.hessian                         thrpt    5   189.767 ± 39.443  ops/ms
EishayWriteBinary.javaSerialize                   thrpt    5   125.936 ±  5.009  ops/ms
EishayWriteBinary.jsonb                           thrpt    5  1392.199 ± 59.406  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes  thrpt    5  1090.587 ±  3.040  ops/ms
EishayWriteBinaryArrayMapping.jsonb               thrpt    5  2449.052 ± 43.674  ops/ms
EishayWriteBinaryArrayMapping.kryo                thrpt    5  1017.621 ±  3.722  ops/ms
EishayWriteBinaryArrayMapping.protobuf            thrpt    5   868.540 ± 34.687  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB          thrpt    5   772.445 ± 19.176  ops/ms
EishayWriteBinaryAutoType.hessian                 thrpt    5   189.736 ± 18.624  ops/ms
EishayWriteBinaryAutoType.javaSerialize           thrpt    5   129.705 ±  3.553  ops/ms
EishayWriteString.fastjson1                       thrpt    5   312.821 ±  0.302  ops/ms
EishayWriteString.fastjson2                       thrpt    5   850.649 ±  3.192  ops/ms
EishayWriteString.gson                            thrpt    5   207.917 ±  6.904  ops/ms
EishayWriteString.jackson                         thrpt    5   439.410 ± 14.282  ops/ms
EishayWriteStringTree.fastjson1                   thrpt    5   367.027 ± 14.563  ops/ms
EishayWriteStringTree.fastjson2                   thrpt    5   618.446 ±  4.619  ops/ms
EishayWriteStringTree.gson                        thrpt    5   222.415 ±  4.713  ops/ms
EishayWriteStringTree.jackson                     thrpt    5   442.216 ±  6.323  ops/ms
EishayWriteUTF8Bytes.fastjson1                    thrpt    5   290.154 ±  8.764  ops/ms
EishayWriteUTF8Bytes.fastjson2                    thrpt    5   908.149 ± 25.362  ops/ms
EishayWriteUTF8Bytes.gson                         thrpt    5   197.832 ±  1.930  ops/ms
EishayWriteUTF8Bytes.jackson                      thrpt    5   386.680 ±  9.213  ops/ms
```
# OrangePI5-jdk-17.0.7
```java
Benchmark                                          Mode  Cnt     Score     Error   Units
EishayParseBinary.fastjson2UTF8Bytes              thrpt    5   692.904 ±  12.420  ops/ms
EishayParseBinary.hessian                         thrpt    5   154.975 ±   5.988  ops/ms
EishayParseBinary.javaSerialize                   thrpt    5    29.751 ±   1.172  ops/ms
EishayParseBinary.jsonb                           thrpt    5  1368.797 ±  68.667  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes  thrpt    5  1005.377 ±  21.761  ops/ms
EishayParseBinaryArrayMapping.jsonb               thrpt    5  2090.885 ± 170.597  ops/ms
EishayParseBinaryArrayMapping.kryo                thrpt    5  1002.062 ±  33.165  ops/ms
EishayParseBinaryArrayMapping.protobuf            thrpt    5  1036.036 ±  36.988  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB          thrpt    5   852.840 ±  19.862  ops/ms
EishayParseBinaryAutoType.hessian                 thrpt    5   153.870 ±   8.277  ops/ms
EishayParseBinaryAutoType.javaSerialize           thrpt    5    29.618 ±   1.039  ops/ms
EishayParseString.fastjson1                       thrpt    5   806.769 ±  24.261  ops/ms
EishayParseString.fastjson2                       thrpt    5   686.582 ±  10.629  ops/ms
EishayParseString.gson                            thrpt    5   226.778 ±   8.546  ops/ms
EishayParseString.jackson                         thrpt    5   276.950 ±   3.226  ops/ms
EishayParseStringPretty.fastjson1                 thrpt    5   184.340 ±   7.314  ops/ms
EishayParseStringPretty.fastjson2                 thrpt    5   529.047 ±  22.951  ops/ms
EishayParseStringPretty.gson                      thrpt    5   210.648 ±  11.126  ops/ms
EishayParseStringPretty.jackson                   thrpt    5   254.217 ±   5.728  ops/ms
EishayParseTreeString.fastjson1                   thrpt    5   281.713 ±  12.072  ops/ms
EishayParseTreeString.fastjson2                   thrpt    5   479.914 ±  15.081  ops/ms
EishayParseTreeString.gson                        thrpt    5   180.328 ±   5.377  ops/ms
EishayParseTreeString.jackson                     thrpt    5   252.662 ±   4.605  ops/ms
EishayParseTreeStringPretty.fastjson1             thrpt    5   247.164 ±  10.607  ops/ms
EishayParseTreeStringPretty.fastjson2             thrpt    5   401.535 ±  20.494  ops/ms
EishayParseTreeStringPretty.gson                  thrpt    5   170.914 ±   7.753  ops/ms
EishayParseTreeStringPretty.jackson               thrpt    5   235.599 ±   2.332  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                thrpt    5   244.286 ±  12.944  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                thrpt    5   487.619 ±  24.651  ops/ms
EishayParseTreeUTF8Bytes.gson                     thrpt    5   170.974 ±   9.005  ops/ms
EishayParseTreeUTF8Bytes.jackson                  thrpt    5   304.735 ±  10.905  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1          thrpt    5   205.949 ±  10.416  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2          thrpt    5   409.277 ±  20.119  ops/ms
EishayParseTreeUTF8BytesPretty.gson               thrpt    5   159.454 ±   5.292  ops/ms
EishayParseTreeUTF8BytesPretty.jackson            thrpt    5   290.428 ±  17.010  ops/ms
EishayParseUTF8Bytes.fastjson1                    thrpt    5   580.445 ±  16.298  ops/ms
EishayParseUTF8Bytes.fastjson2                    thrpt    5   694.752 ±  58.361  ops/ms
EishayParseUTF8Bytes.gson                         thrpt    5   170.678 ±   6.488  ops/ms
EishayParseUTF8Bytes.jackson                      thrpt    5   309.293 ±  12.528  ops/ms
EishayParseUTF8BytesPretty.fastjson1              thrpt    5   165.412 ±   3.276  ops/ms
EishayParseUTF8BytesPretty.fastjson2              thrpt    5   534.803 ±   6.656  ops/ms
EishayParseUTF8BytesPretty.gson                   thrpt    5   156.637 ±   6.532  ops/ms
EishayParseUTF8BytesPretty.jackson                thrpt    5   283.494 ±   6.629  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes              thrpt    5   936.332 ±  36.878  ops/ms
EishayWriteBinary.hessian                         thrpt    5   212.527 ±  43.935  ops/ms
EishayWriteBinary.javaSerialize                   thrpt    5   128.690 ±   2.739  ops/ms
EishayWriteBinary.jsonb                           thrpt    5  1461.287 ±  32.306  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes  thrpt    5  1101.845 ±  56.488  ops/ms
EishayWriteBinaryArrayMapping.jsonb               thrpt    5  2601.854 ± 155.842  ops/ms
EishayWriteBinaryArrayMapping.kryo                thrpt    5   994.451 ±   2.970  ops/ms
EishayWriteBinaryArrayMapping.protobuf            thrpt    5   934.377 ±  18.806  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB          thrpt    5   824.730 ±  21.910  ops/ms
EishayWriteBinaryAutoType.hessian                 thrpt    5   199.591 ±  35.493  ops/ms
EishayWriteBinaryAutoType.javaSerialize           thrpt    5   129.250 ±   7.187  ops/ms
EishayWriteString.fastjson1                       thrpt    5   354.613 ±  12.366  ops/ms
EishayWriteString.fastjson2                       thrpt    5   820.299 ±  43.998  ops/ms
EishayWriteString.gson                            thrpt    5   164.140 ±   3.532  ops/ms
EishayWriteString.jackson                         thrpt    5   441.410 ±  23.214  ops/ms
EishayWriteStringTree.fastjson1                   thrpt    5   371.929 ±  16.152  ops/ms
EishayWriteStringTree.fastjson2                   thrpt    5   602.272 ±  30.994  ops/ms
EishayWriteStringTree.gson                        thrpt    5   167.378 ±   8.074  ops/ms
EishayWriteStringTree.jackson                     thrpt    5   413.576 ±  20.425  ops/ms
EishayWriteUTF8Bytes.fastjson1                    thrpt    5   295.258 ±  13.857  ops/ms
EishayWriteUTF8Bytes.fastjson2                    thrpt    5   932.071 ±  45.757  ops/ms
EishayWriteUTF8Bytes.gson                         thrpt    5   160.617 ±   3.557  ops/ms
EishayWriteUTF8Bytes.jackson                      thrpt    5   434.204 ±  15.534  ops/ms
```
