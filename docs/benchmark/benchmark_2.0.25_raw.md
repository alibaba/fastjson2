# ecs.c7.xlarge-jdk1.8.0_361
```java
Benchmark                                                 Mode  Cnt     Score    Error   Units
EishayParseBinary.fastjson2UTF8Bytes                     thrpt    5  1131.307 ? 19.177  ops/ms
EishayParseBinary.hessian                                thrpt    5   288.613 ?  3.771  ops/ms
EishayParseBinary.javaSerialize                          thrpt    5    46.374 ?  0.582  ops/ms
EishayParseBinary.jsonb                                  thrpt    5  1696.142 ? 24.183  ops/ms
EishayParseBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5  1479.633 ? 18.213  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  1646.472 ? 17.176  ops/ms
EishayParseBinaryArrayMapping.jsonb                      thrpt    5  2678.213 ? 35.549  ops/ms
EishayParseBinaryArrayMapping.kryo                       thrpt    5  1698.730 ? 32.144  ops/ms
EishayParseBinaryArrayMapping.protobuf                   thrpt    5  1020.105 ? 16.690  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB                 thrpt    5  1410.936 ? 12.570  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB_autoTypeFilter  thrpt    5  1388.933 ? 20.540  ops/ms
EishayParseBinaryAutoType.hessian                        thrpt    5   292.836 ?  4.619  ops/ms
EishayParseBinaryAutoType.javaSerialize                  thrpt    5    47.053 ?  0.653  ops/ms
EishayParseString.fastjson1                              thrpt    5   990.652 ?  8.159  ops/ms
EishayParseString.fastjson2                              thrpt    5  1299.146 ? 19.728  ops/ms
EishayParseString.gson                                   thrpt    5   421.446 ?  9.679  ops/ms
EishayParseString.jackson                                thrpt    5   531.363 ?  5.705  ops/ms
EishayParseStringPretty.fastjson1                        thrpt    5   257.616 ?  4.223  ops/ms
EishayParseStringPretty.fastjson2                        thrpt    5   955.391 ? 17.982  ops/ms
EishayParseStringPretty.gson                             thrpt    5   410.836 ?  7.607  ops/ms
EishayParseStringPretty.jackson                          thrpt    5   484.417 ? 10.558  ops/ms
EishayParseTreeString.fastjson1                          thrpt    5   508.807 ?  6.809  ops/ms
EishayParseTreeString.fastjson2                          thrpt    5   989.460 ? 11.874  ops/ms
EishayParseTreeString.gson                               thrpt    5   341.400 ?  3.140  ops/ms
EishayParseTreeString.jackson                            thrpt    5   532.436 ?  8.297  ops/ms
EishayParseTreeStringPretty.fastjson1                    thrpt    5   448.003 ?  4.473  ops/ms
EishayParseTreeStringPretty.fastjson2                    thrpt    5   774.547 ? 10.784  ops/ms
EishayParseTreeStringPretty.gson                         thrpt    5   318.061 ?  5.073  ops/ms
EishayParseTreeStringPretty.jackson                      thrpt    5   497.114 ?  9.511  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                       thrpt    5   451.865 ?  2.521  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                       thrpt    5   820.879 ?  5.925  ops/ms
EishayParseTreeUTF8Bytes.gson                            thrpt    5   312.721 ?  1.755  ops/ms
EishayParseTreeUTF8Bytes.jackson                         thrpt    5   568.494 ? 11.567  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1                 thrpt    5   374.637 ?  1.298  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2                 thrpt    5   743.591 ? 18.303  ops/ms
EishayParseTreeUTF8BytesPretty.gson                      thrpt    5   296.086 ?  4.978  ops/ms
EishayParseTreeUTF8BytesPretty.jackson                   thrpt    5   532.822 ?  4.257  ops/ms
EishayParseUTF8Bytes.dsljson                             thrpt    5   902.359 ? 18.141  ops/ms
EishayParseUTF8Bytes.fastjson1                           thrpt    5   793.440 ?  7.181  ops/ms
EishayParseUTF8Bytes.fastjson2                           thrpt    5  1071.945 ?  6.885  ops/ms
EishayParseUTF8Bytes.gson                                thrpt    5   313.105 ?  4.410  ops/ms
EishayParseUTF8Bytes.jackson                             thrpt    5   599.987 ?  8.935  ops/ms
EishayParseUTF8BytesPretty.fastjson1                     thrpt    5   240.508 ?  2.366  ops/ms
EishayParseUTF8BytesPretty.fastjson2                     thrpt    5   835.423 ? 10.601  ops/ms
EishayParseUTF8BytesPretty.gson                          thrpt    5   290.825 ?  1.442  ops/ms
EishayParseUTF8BytesPretty.jackson                       thrpt    5   515.842 ?  8.268  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes                     thrpt    5  1531.271 ? 31.907  ops/ms
EishayWriteBinary.hessian                                thrpt    5   332.203 ?  2.385  ops/ms
EishayWriteBinary.javaSerialize                          thrpt    5   235.417 ?  3.491  ops/ms
EishayWriteBinary.jsonb                                  thrpt    5  2008.214 ? 46.180  ops/ms
EishayWriteBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   767.216 ? 18.118  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  1855.014 ? 23.660  ops/ms
EishayWriteBinaryArrayMapping.jsonb                      thrpt    5  3188.787 ? 52.146  ops/ms
EishayWriteBinaryArrayMapping.kryo                       thrpt    5  1956.721 ? 24.840  ops/ms
EishayWriteBinaryArrayMapping.protobuf                   thrpt    5  1367.749 ? 21.414  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB                 thrpt    5  1302.582 ? 21.425  ops/ms
EishayWriteBinaryAutoType.fastjson2UTF8Bytes             thrpt    5  1480.453 ? 24.698  ops/ms
EishayWriteBinaryAutoType.hessian                        thrpt    5   341.049 ?  2.079  ops/ms
EishayWriteBinaryAutoType.javaSerialize                  thrpt    5   237.880 ?  4.110  ops/ms
EishayWriteString.fastjson1                              thrpt    5   623.801 ?  9.459  ops/ms
EishayWriteString.fastjson2                              thrpt    5  1621.473 ? 35.144  ops/ms
EishayWriteString.gson                                   thrpt    5   454.525 ?  3.387  ops/ms
EishayWriteString.jackson                                thrpt    5  1022.974 ?  8.565  ops/ms
EishayWriteStringTree.fastjson1                          thrpt    5   810.743 ? 11.790  ops/ms
EishayWriteStringTree.fastjson2                          thrpt    5  1221.288 ? 20.545  ops/ms
EishayWriteStringTree.gson                               thrpt    5   489.780 ? 15.972  ops/ms
EishayWriteStringTree.jackson                            thrpt    5   915.691 ? 11.590  ops/ms
EishayWriteStringTree1x.fastjson1                        thrpt    5   764.804 ? 11.504  ops/ms
EishayWriteStringTree1x.fastjson2                        thrpt    5  1098.312 ? 11.017  ops/ms
EishayWriteStringTree1x.gson                             thrpt    5   479.306 ?  3.326  ops/ms
EishayWriteStringTree1x.jackson                          thrpt    5   892.256 ? 11.192  ops/ms
EishayWriteUTF8Bytes.fastjson1                           thrpt    5   586.528 ?  2.167  ops/ms
EishayWriteUTF8Bytes.fastjson2                           thrpt    5  1519.336 ? 20.203  ops/ms
EishayWriteUTF8Bytes.gson                                thrpt    5   384.825 ?  2.929  ops/ms
EishayWriteUTF8Bytes.jackson                             thrpt    5   921.067 ?  6.573  ops/ms
```
# ecs.c7.xlarge-jdk-11.0.18
```java
Benchmark                                                 Mode  Cnt     Score    Error   Units
EishayParseBinary.fastjson2UTF8Bytes                     thrpt    5  1276.382 ?  1.811  ops/ms
EishayParseBinary.hessian                                thrpt    5   257.385 ?  0.344  ops/ms
EishayParseBinary.javaSerialize                          thrpt    5    48.222 ?  0.360  ops/ms
EishayParseBinary.jsonb                                  thrpt    5  2509.343 ? 12.680  ops/ms
EishayParseBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5  1486.777 ?  4.097  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  1928.905 ? 12.007  ops/ms
EishayParseBinaryArrayMapping.jsonb                      thrpt    5  3798.171 ? 13.593  ops/ms
EishayParseBinaryArrayMapping.kryo                       thrpt    5  1608.647 ?  9.439  ops/ms
EishayParseBinaryArrayMapping.protobuf                   thrpt    5  1345.224 ?  6.413  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB                 thrpt    5  1904.342 ? 22.779  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB_autoTypeFilter  thrpt    5  1669.456 ? 15.374  ops/ms
EishayParseBinaryAutoType.hessian                        thrpt    5   259.907 ?  0.426  ops/ms
EishayParseBinaryAutoType.javaSerialize                  thrpt    5    48.598 ?  0.211  ops/ms
EishayParseString.fastjson1                              thrpt    5   933.112 ?  8.954  ops/ms
EishayParseString.fastjson2                              thrpt    5  1212.341 ? 12.203  ops/ms
EishayParseString.gson                                   thrpt    5   425.908 ?  1.005  ops/ms
EishayParseString.jackson                                thrpt    5   505.503 ?  2.171  ops/ms
EishayParseStringPretty.fastjson1                        thrpt    5   245.609 ?  0.976  ops/ms
EishayParseStringPretty.fastjson2                        thrpt    5   901.219 ? 11.339  ops/ms
EishayParseStringPretty.gson                             thrpt    5   379.915 ?  2.491  ops/ms
EishayParseStringPretty.jackson                          thrpt    5   427.613 ?  4.719  ops/ms
EishayParseTreeString.fastjson1                          thrpt    5   409.965 ?  1.422  ops/ms
EishayParseTreeString.fastjson2                          thrpt    5   874.083 ? 10.183  ops/ms
EishayParseTreeString.gson                               thrpt    5   323.082 ?  1.866  ops/ms
EishayParseTreeString.jackson                            thrpt    5   463.287 ?  6.935  ops/ms
EishayParseTreeStringPretty.fastjson1                    thrpt    5   353.031 ?  1.684  ops/ms
EishayParseTreeStringPretty.fastjson2                    thrpt    5   693.599 ?  4.473  ops/ms
EishayParseTreeStringPretty.gson                         thrpt    5   309.801 ?  1.742  ops/ms
EishayParseTreeStringPretty.jackson                      thrpt    5   424.172 ?  1.883  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                       thrpt    5   375.996 ?  1.492  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                       thrpt    5   840.402 ?  3.234  ops/ms
EishayParseTreeUTF8Bytes.gson                            thrpt    5   316.240 ?  1.688  ops/ms
EishayParseTreeUTF8Bytes.jackson                         thrpt    5   500.259 ?  4.474  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1                 thrpt    5   323.116 ?  6.129  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2                 thrpt    5   632.181 ?  3.309  ops/ms
EishayParseTreeUTF8BytesPretty.gson                      thrpt    5   289.772 ?  0.635  ops/ms
EishayParseTreeUTF8BytesPretty.jackson                   thrpt    5   477.531 ?  4.626  ops/ms
EishayParseUTF8Bytes.dsljson                             thrpt    5   890.877 ?  8.532  ops/ms
EishayParseUTF8Bytes.fastjson1                           thrpt    5   760.192 ?  4.297  ops/ms
EishayParseUTF8Bytes.fastjson2                           thrpt    5  1191.665 ?  6.756  ops/ms
EishayParseUTF8Bytes.gson                                thrpt    5   312.012 ?  1.387  ops/ms
EishayParseUTF8Bytes.jackson                             thrpt    5   521.958 ?  2.322  ops/ms
EishayParseUTF8BytesPretty.fastjson1                     thrpt    5   231.007 ?  1.107  ops/ms
EishayParseUTF8BytesPretty.fastjson2                     thrpt    5   911.757 ?  6.375  ops/ms
EishayParseUTF8BytesPretty.gson                          thrpt    5   300.717 ?  2.694  ops/ms
EishayParseUTF8BytesPretty.jackson                       thrpt    5   508.028 ? 12.649  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes                     thrpt    5  1606.664 ? 13.750  ops/ms
EishayWriteBinary.hessian                                thrpt    5   337.668 ?  2.284  ops/ms
EishayWriteBinary.javaSerialize                          thrpt    5   208.776 ?  1.954  ops/ms
EishayWriteBinary.jsonb                                  thrpt    5  2454.831 ? 18.211  ops/ms
EishayWriteBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   708.509 ?  3.353  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  2095.329 ?  9.333  ops/ms
EishayWriteBinaryArrayMapping.jsonb                      thrpt    5  4611.814 ? 30.902  ops/ms
EishayWriteBinaryArrayMapping.kryo                       thrpt    5  2025.581 ? 35.631  ops/ms
EishayWriteBinaryArrayMapping.protobuf                   thrpt    5  1426.360 ? 12.678  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB                 thrpt    5  1441.833 ? 15.680  ops/ms
EishayWriteBinaryAutoType.fastjson2UTF8Bytes             thrpt    5  1563.179 ? 13.988  ops/ms
EishayWriteBinaryAutoType.hessian                        thrpt    5   337.717 ?  1.314  ops/ms
EishayWriteBinaryAutoType.javaSerialize                  thrpt    5   225.574 ?  3.038  ops/ms
EishayWriteString.fastjson1                              thrpt    5   587.154 ?  6.884  ops/ms
EishayWriteString.fastjson2                              thrpt    5  1498.423 ? 11.426  ops/ms
EishayWriteString.gson                                   thrpt    5   362.823 ?  3.087  ops/ms
EishayWriteString.jackson                                thrpt    5   971.220 ?  8.317  ops/ms
EishayWriteStringTree.fastjson1                          thrpt    5   765.295 ? 11.918  ops/ms
EishayWriteStringTree.fastjson2                          thrpt    5  1272.614 ?  6.822  ops/ms
EishayWriteStringTree.gson                               thrpt    5   386.290 ?  9.452  ops/ms
EishayWriteStringTree.jackson                            thrpt    5   951.230 ?  3.120  ops/ms
EishayWriteStringTree1x.fastjson1                        thrpt    5   729.704 ?  6.721  ops/ms
EishayWriteStringTree1x.fastjson2                        thrpt    5  1120.819 ?  3.387  ops/ms
EishayWriteStringTree1x.gson                             thrpt    5   375.955 ?  3.823  ops/ms
EishayWriteStringTree1x.jackson                          thrpt    5   892.952 ?  3.647  ops/ms
EishayWriteUTF8Bytes.fastjson1                           thrpt    5   546.818 ?  4.441  ops/ms
EishayWriteUTF8Bytes.fastjson2                           thrpt    5  1642.074 ?  9.336  ops/ms
EishayWriteUTF8Bytes.gson                                thrpt    5   363.262 ?  3.092  ops/ms
EishayWriteUTF8Bytes.jackson                             thrpt    5   891.099 ?  7.239  ops/ms
```
# ecs.c7.xlarge-jdk-17.0.6
```java
Benchmark                                                 Mode  Cnt     Score    Error   Units
EishayParseBinary.fastjson2UTF8Bytes                     thrpt    5  1413.621 ?  7.920  ops/ms
EishayParseBinary.hessian                                thrpt    5   265.442 ?  0.486  ops/ms
EishayParseBinary.javaSerialize                          thrpt    5    55.098 ?  0.513  ops/ms
EishayParseBinary.jsonb                                  thrpt    5  2925.881 ? 17.395  ops/ms
EishayParseBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5  1720.790 ?  6.559  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  2184.545 ? 10.709  ops/ms
EishayParseBinaryArrayMapping.jsonb                      thrpt    5  5019.048 ? 11.880  ops/ms
EishayParseBinaryArrayMapping.kryo                       thrpt    5  1834.641 ?  9.838  ops/ms
EishayParseBinaryArrayMapping.protobuf                   thrpt    5  1635.181 ?  7.110  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB                 thrpt    5  2149.310 ?  2.506  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB_autoTypeFilter  thrpt    5  1817.566 ? 12.098  ops/ms
EishayParseBinaryAutoType.hessian                        thrpt    5   263.340 ?  1.040  ops/ms
EishayParseBinaryAutoType.javaSerialize                  thrpt    5    54.556 ?  0.471  ops/ms
EishayParseString.fastjson1                              thrpt    5  1287.125 ?  2.594  ops/ms
EishayParseString.fastjson2                              thrpt    5  1274.216 ?  5.336  ops/ms
EishayParseString.gson                                   thrpt    5   464.798 ?  2.329  ops/ms
EishayParseString.jackson                                thrpt    5   498.256 ?  1.399  ops/ms
EishayParseStringPretty.fastjson1                        thrpt    5   303.141 ?  1.398  ops/ms
EishayParseStringPretty.fastjson2                        thrpt    5   949.623 ?  6.232  ops/ms
EishayParseStringPretty.gson                             thrpt    5   426.923 ?  1.165  ops/ms
EishayParseStringPretty.jackson                          thrpt    5   465.452 ?  2.297  ops/ms
EishayParseTreeString.fastjson1                          thrpt    5   568.499 ?  2.168  ops/ms
EishayParseTreeString.fastjson2                          thrpt    5  1084.482 ?  4.865  ops/ms
EishayParseTreeString.gson                               thrpt    5   323.609 ?  0.991  ops/ms
EishayParseTreeString.jackson                            thrpt    5   525.089 ?  1.499  ops/ms
EishayParseTreeStringPretty.fastjson1                    thrpt    5   492.125 ?  2.495  ops/ms
EishayParseTreeStringPretty.fastjson2                    thrpt    5   783.595 ?  1.322  ops/ms
EishayParseTreeStringPretty.gson                         thrpt    5   312.619 ?  1.390  ops/ms
EishayParseTreeStringPretty.jackson                      thrpt    5   458.912 ?  4.945  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                       thrpt    5   497.023 ?  2.418  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                       thrpt    5  1071.296 ?  5.743  ops/ms
EishayParseTreeUTF8Bytes.gson                            thrpt    5   313.443 ?  3.226  ops/ms
EishayParseTreeUTF8Bytes.jackson                         thrpt    5   579.576 ?  1.665  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1                 thrpt    5   417.432 ?  1.210  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2                 thrpt    5   822.817 ?  1.955  ops/ms
EishayParseTreeUTF8BytesPretty.gson                      thrpt    5   305.125 ?  1.985  ops/ms
EishayParseTreeUTF8BytesPretty.jackson                   thrpt    5   527.214 ?  2.170  ops/ms
EishayParseUTF8Bytes.dsljson                             thrpt    5   833.416 ?  5.238  ops/ms
EishayParseUTF8Bytes.fastjson1                           thrpt    5   977.909 ?  3.601  ops/ms
EishayParseUTF8Bytes.fastjson2                           thrpt    5  1335.764 ?  4.365  ops/ms
EishayParseUTF8Bytes.gson                                thrpt    5   315.675 ?  1.668  ops/ms
EishayParseUTF8Bytes.jackson                             thrpt    5   553.890 ?  4.068  ops/ms
EishayParseUTF8BytesPretty.fastjson1                     thrpt    5   270.024 ?  2.439  ops/ms
EishayParseUTF8BytesPretty.fastjson2                     thrpt    5   954.500 ?  5.355  ops/ms
EishayParseUTF8BytesPretty.gson                          thrpt    5   302.844 ?  0.475  ops/ms
EishayParseUTF8BytesPretty.jackson                       thrpt    5   504.118 ?  3.111  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes                     thrpt    5  1934.628 ? 14.232  ops/ms
EishayWriteBinary.hessian                                thrpt    5   326.915 ?  2.570  ops/ms
EishayWriteBinary.javaSerialize                          thrpt    5   240.542 ?  2.147  ops/ms
EishayWriteBinary.jsonb                                  thrpt    5  3420.571 ? 28.971  ops/ms
EishayWriteBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   722.180 ?  4.886  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  2179.407 ?  8.159  ops/ms
EishayWriteBinaryArrayMapping.jsonb                      thrpt    5  5474.523 ? 49.634  ops/ms
EishayWriteBinaryArrayMapping.kryo                       thrpt    5  2096.012 ? 15.221  ops/ms
EishayWriteBinaryArrayMapping.protobuf                   thrpt    5  1763.089 ?  5.890  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB                 thrpt    5  1691.528 ? 15.065  ops/ms
EishayWriteBinaryAutoType.fastjson2UTF8Bytes             thrpt    5  1878.427 ? 21.827  ops/ms
EishayWriteBinaryAutoType.hessian                        thrpt    5   325.745 ?  0.661  ops/ms
EishayWriteBinaryAutoType.javaSerialize                  thrpt    5   240.584 ?  0.996  ops/ms
EishayWriteString.fastjson1                              thrpt    5   632.669 ?  2.801  ops/ms
EishayWriteString.fastjson2                              thrpt    5  1642.088 ?  8.707  ops/ms
EishayWriteString.gson                                   thrpt    5   244.488 ?  1.485  ops/ms
EishayWriteString.jackson                                thrpt    5  1043.624 ?  2.768  ops/ms
EishayWriteStringTree.fastjson1                          thrpt    5   821.621 ?  3.287  ops/ms
EishayWriteStringTree.fastjson2                          thrpt    5  1210.081 ?  5.415  ops/ms
EishayWriteStringTree.gson                               thrpt    5   248.810 ?  1.334  ops/ms
EishayWriteStringTree.jackson                            thrpt    5   877.356 ?  4.283  ops/ms
EishayWriteStringTree1x.fastjson1                        thrpt    5   724.056 ?  9.291  ops/ms
EishayWriteStringTree1x.fastjson2                        thrpt    5   916.396 ?  5.965  ops/ms
EishayWriteStringTree1x.gson                             thrpt    5   240.570 ?  1.254  ops/ms
EishayWriteStringTree1x.jackson                          thrpt    5   786.197 ?  2.995  ops/ms
EishayWriteUTF8Bytes.fastjson1                           thrpt    5   581.667 ?  4.860  ops/ms
EishayWriteUTF8Bytes.fastjson2                           thrpt    5  1937.323 ? 17.136  ops/ms
EishayWriteUTF8Bytes.gson                                thrpt    5   244.057 ?  0.250  ops/ms
EishayWriteUTF8Bytes.jackson                             thrpt    5   994.842 ?  3.153  ops/ms
```
# ecs.c7.xlarge-graalvm-ce-java11-22.3.1
```java
Benchmark                                                 Mode  Cnt     Score    Error   Units
EishayParseBinary.fastjson2UTF8Bytes                     thrpt    5  1224.618 ?  7.097  ops/ms
EishayParseBinary.hessian                                thrpt    5   234.654 ?  1.857  ops/ms
EishayParseBinary.javaSerialize                          thrpt    5    50.203 ?  0.526  ops/ms
EishayParseBinary.jsonb                                  thrpt    5  2822.905 ?  7.333  ops/ms
EishayParseBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5  1546.348 ? 42.372  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  1816.220 ? 18.993  ops/ms
EishayParseBinaryArrayMapping.jsonb                      thrpt    5  4820.957 ? 13.215  ops/ms
EishayParseBinaryArrayMapping.kryo                       thrpt    5  1748.878 ? 27.508  ops/ms
EishayParseBinaryArrayMapping.protobuf                   thrpt    5  1626.445 ?  9.258  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB                 thrpt    5  1992.400 ? 19.880  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB_autoTypeFilter  thrpt    5  1821.458 ? 24.018  ops/ms
EishayParseBinaryAutoType.hessian                        thrpt    5   256.836 ?  3.277  ops/ms
EishayParseBinaryAutoType.javaSerialize                  thrpt    5    50.452 ?  0.542  ops/ms
EishayParseString.fastjson1                              thrpt    5  1100.906 ?  4.876  ops/ms
EishayParseString.fastjson2                              thrpt    5  1208.295 ?  1.219  ops/ms
EishayParseString.gson                                   thrpt    5   521.873 ?  8.380  ops/ms
EishayParseString.jackson                                thrpt    5   478.899 ?  4.926  ops/ms
EishayParseStringPretty.fastjson1                        thrpt    5   296.813 ?  3.324  ops/ms
EishayParseStringPretty.fastjson2                        thrpt    5   871.339 ?  8.541  ops/ms
EishayParseStringPretty.gson                             thrpt    5   471.396 ?  2.654  ops/ms
EishayParseStringPretty.jackson                          thrpt    5   465.619 ? 11.674  ops/ms
EishayParseTreeString.fastjson1                          thrpt    5   506.706 ?  2.514  ops/ms
EishayParseTreeString.fastjson2                          thrpt    5   929.017 ?  8.106  ops/ms
EishayParseTreeString.gson                               thrpt    5   439.294 ?  2.686  ops/ms
EishayParseTreeString.jackson                            thrpt    5   536.542 ?  1.326  ops/ms
EishayParseTreeStringPretty.fastjson1                    thrpt    5   422.192 ?  1.717  ops/ms
EishayParseTreeStringPretty.fastjson2                    thrpt    5   735.085 ?  3.678  ops/ms
EishayParseTreeStringPretty.gson                         thrpt    5   406.320 ?  3.457  ops/ms
EishayParseTreeStringPretty.jackson                      thrpt    5   469.286 ?  3.858  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                       thrpt    5   426.010 ?  2.120  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                       thrpt    5   912.990 ? 10.182  ops/ms
EishayParseTreeUTF8Bytes.gson                            thrpt    5   412.192 ?  3.234  ops/ms
EishayParseTreeUTF8Bytes.jackson                         thrpt    5   506.015 ?  3.746  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1                 thrpt    5   359.799 ?  2.645  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2                 thrpt    5   687.335 ? 12.823  ops/ms
EishayParseTreeUTF8BytesPretty.gson                      thrpt    5   376.002 ?  5.515  ops/ms
EishayParseTreeUTF8BytesPretty.jackson                   thrpt    5   465.782 ?  5.638  ops/ms
EishayParseUTF8Bytes.dsljson                             thrpt    5   905.222 ?  9.871  ops/ms
EishayParseUTF8Bytes.fastjson1                           thrpt    5   791.091 ?  7.654  ops/ms
EishayParseUTF8Bytes.fastjson2                           thrpt    5  1108.217 ?  7.418  ops/ms
EishayParseUTF8Bytes.gson                                thrpt    5   413.173 ?  4.251  ops/ms
EishayParseUTF8Bytes.jackson                             thrpt    5   555.855 ?  3.826  ops/ms
EishayParseUTF8BytesPretty.fastjson1                     thrpt    5   272.525 ?  7.141  ops/ms
EishayParseUTF8BytesPretty.fastjson2                     thrpt    5   845.494 ?  5.545  ops/ms
EishayParseUTF8BytesPretty.gson                          thrpt    5   372.490 ?  3.010  ops/ms
EishayParseUTF8BytesPretty.jackson                       thrpt    5   505.719 ?  8.593  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes                     thrpt    5  1761.360 ? 14.023  ops/ms
EishayWriteBinary.hessian                                thrpt    5   399.915 ?  2.251  ops/ms
EishayWriteBinary.javaSerialize                          thrpt    5   221.329 ?  1.629  ops/ms
EishayWriteBinary.jsonb                                  thrpt    5  2911.320 ? 23.442  ops/ms
EishayWriteBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   729.609 ?  8.215  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  2290.591 ? 16.018  ops/ms
EishayWriteBinaryArrayMapping.jsonb                      thrpt    5  5774.367 ? 18.409  ops/ms
EishayWriteBinaryArrayMapping.kryo                       thrpt    5  2130.931 ? 22.643  ops/ms
EishayWriteBinaryArrayMapping.protobuf                   thrpt    5  1905.693 ? 13.400  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB                 thrpt    5  1489.825 ? 15.227  ops/ms
EishayWriteBinaryAutoType.fastjson2UTF8Bytes             thrpt    5  1768.566 ? 10.749  ops/ms
EishayWriteBinaryAutoType.hessian                        thrpt    5   404.196 ?  2.428  ops/ms
EishayWriteBinaryAutoType.javaSerialize                  thrpt    5   225.043 ?  1.275  ops/ms
EishayWriteString.fastjson1                              thrpt    5   756.871 ?  6.846  ops/ms
EishayWriteString.fastjson2                              thrpt    5  1661.083 ?  9.180  ops/ms
EishayWriteString.gson                                   thrpt    5   388.895 ?  2.253  ops/ms
EishayWriteString.jackson                                thrpt    5   938.768 ? 11.366  ops/ms
EishayWriteStringTree.fastjson1                          thrpt    5   827.087 ? 11.412  ops/ms
EishayWriteStringTree.fastjson2                          thrpt    5  1450.710 ? 21.976  ops/ms
EishayWriteStringTree.gson                               thrpt    5   447.091 ?  7.518  ops/ms
EishayWriteStringTree.jackson                            thrpt    5  1029.599 ?  7.698  ops/ms
EishayWriteStringTree1x.fastjson1                        thrpt    5   701.613 ?  3.609  ops/ms
EishayWriteStringTree1x.fastjson2                        thrpt    5  1184.259 ?  8.512  ops/ms
EishayWriteStringTree1x.gson                             thrpt    5   401.615 ?  2.261  ops/ms
EishayWriteStringTree1x.jackson                          thrpt    5   795.438 ?  4.266  ops/ms
EishayWriteUTF8Bytes.fastjson1                           thrpt    5   683.220 ?  2.862  ops/ms
EishayWriteUTF8Bytes.fastjson2                           thrpt    5  1782.622 ? 22.140  ops/ms
EishayWriteUTF8Bytes.gson                                thrpt    5   382.840 ?  2.223  ops/ms
EishayWriteUTF8Bytes.jackson                             thrpt    5   884.229 ?  9.105  ops/ms
```
# ecs.c7.xlarge-graalvm-ce-java17-22.3.1
```java
Benchmark                                                 Mode  Cnt     Score    Error   Units
EishayParseBinary.fastjson2UTF8Bytes                     thrpt    5  1332.497 ?  4.835  ops/ms
EishayParseBinary.hessian                                thrpt    5   244.092 ?  1.669  ops/ms
EishayParseBinary.javaSerialize                          thrpt    5    54.714 ?  0.146  ops/ms
EishayParseBinary.jsonb                                  thrpt    5  3485.419 ? 11.488  ops/ms
EishayParseBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5  1677.182 ? 54.928  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  2279.357 ? 11.891  ops/ms
EishayParseBinaryArrayMapping.jsonb                      thrpt    5  5341.054 ? 29.019  ops/ms
EishayParseBinaryArrayMapping.kryo                       thrpt    5  1769.739 ?  7.995  ops/ms
EishayParseBinaryArrayMapping.protobuf                   thrpt    5  1769.116 ?  6.114  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB                 thrpt    5  2220.378 ? 31.148  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB_autoTypeFilter  thrpt    5  1976.378 ? 19.333  ops/ms
EishayParseBinaryAutoType.hessian                        thrpt    5   250.459 ?  0.441  ops/ms
EishayParseBinaryAutoType.javaSerialize                  thrpt    5    54.015 ?  0.293  ops/ms
EishayParseString.fastjson1                              thrpt    5  1467.073 ?  4.907  ops/ms
EishayParseString.fastjson2                              thrpt    5  1278.270 ?  6.528  ops/ms
EishayParseString.gson                                   thrpt    5   518.661 ?  4.268  ops/ms
EishayParseString.jackson                                thrpt    5   522.664 ?  1.880  ops/ms
EishayParseStringPretty.fastjson1                        thrpt    5   385.516 ?  1.984  ops/ms
EishayParseStringPretty.fastjson2                        thrpt    5   919.551 ?  9.100  ops/ms
EishayParseStringPretty.gson                             thrpt    5   475.466 ?  2.563  ops/ms
EishayParseStringPretty.jackson                          thrpt    5   474.964 ?  1.793  ops/ms
EishayParseTreeString.fastjson1                          thrpt    5   658.688 ?  2.691  ops/ms
EishayParseTreeString.fastjson2                          thrpt    5  1009.284 ?  2.976  ops/ms
EishayParseTreeString.gson                               thrpt    5   434.827 ?  3.682  ops/ms
EishayParseTreeString.jackson                            thrpt    5   550.382 ?  2.438  ops/ms
EishayParseTreeStringPretty.fastjson1                    thrpt    5   551.742 ?  3.296  ops/ms
EishayParseTreeStringPretty.fastjson2                    thrpt    5   748.501 ?  2.169  ops/ms
EishayParseTreeStringPretty.gson                         thrpt    5   396.134 ?  1.519  ops/ms
EishayParseTreeStringPretty.jackson                      thrpt    5   476.646 ?  3.881  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                       thrpt    5   550.822 ?  1.933  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                       thrpt    5   967.040 ?  5.846  ops/ms
EishayParseTreeUTF8Bytes.gson                            thrpt    5   409.225 ?  4.837  ops/ms
EishayParseTreeUTF8Bytes.jackson                         thrpt    5   558.805 ?  0.588  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1                 thrpt    5   444.709 ?  1.051  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2                 thrpt    5   747.711 ?  2.369  ops/ms
EishayParseTreeUTF8BytesPretty.gson                      thrpt    5   382.867 ?  2.928  ops/ms
EishayParseTreeUTF8BytesPretty.jackson                   thrpt    5   555.964 ?  2.932  ops/ms
EishayParseUTF8Bytes.dsljson                             thrpt    5   588.798 ? 14.303  ops/ms
EishayParseUTF8Bytes.fastjson1                           thrpt    5   945.567 ?  4.246  ops/ms
EishayParseUTF8Bytes.fastjson2                           thrpt    5  1224.185 ?  2.831  ops/ms
EishayParseUTF8Bytes.gson                                thrpt    5   423.364 ?  3.305  ops/ms
EishayParseUTF8Bytes.jackson                             thrpt    5   598.121 ?  6.182  ops/ms
EishayParseUTF8BytesPretty.fastjson1                     thrpt    5   324.556 ?  1.867  ops/ms
EishayParseUTF8BytesPretty.fastjson2                     thrpt    5   890.478 ?  2.189  ops/ms
EishayParseUTF8BytesPretty.gson                          thrpt    5   389.658 ?  0.841  ops/ms
EishayParseUTF8BytesPretty.jackson                       thrpt    5   524.351 ?  2.998  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes                     thrpt    5  2068.151 ? 18.079  ops/ms
EishayWriteBinary.hessian                                thrpt    5   411.032 ?  2.023  ops/ms
EishayWriteBinary.javaSerialize                          thrpt    5   228.574 ?  1.396  ops/ms
EishayWriteBinary.jsonb                                  thrpt    5  3516.609 ? 53.716  ops/ms
EishayWriteBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   790.539 ?  3.960  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  2464.724 ?  2.969  ops/ms
EishayWriteBinaryArrayMapping.jsonb                      thrpt    5  7460.247 ? 84.421  ops/ms
EishayWriteBinaryArrayMapping.kryo                       thrpt    5  2228.625 ?  8.244  ops/ms
EishayWriteBinaryArrayMapping.protobuf                   thrpt    5  2208.053 ?  8.317  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB                 thrpt    5  1561.102 ? 14.874  ops/ms
EishayWriteBinaryAutoType.fastjson2UTF8Bytes             thrpt    5  1962.039 ? 13.003  ops/ms
EishayWriteBinaryAutoType.hessian                        thrpt    5   413.771 ?  1.807  ops/ms
EishayWriteBinaryAutoType.javaSerialize                  thrpt    5   234.589 ?  1.304  ops/ms
EishayWriteString.fastjson1                              thrpt    5   821.540 ?  2.535  ops/ms
EishayWriteString.fastjson2                              thrpt    5  1797.593 ? 20.169  ops/ms
EishayWriteString.gson                                   thrpt    5   244.083 ?  0.485  ops/ms
EishayWriteString.jackson                                thrpt    5   983.273 ?  8.999  ops/ms
EishayWriteStringTree.fastjson1                          thrpt    5   843.444 ?  4.216  ops/ms
EishayWriteStringTree.fastjson2                          thrpt    5  1424.976 ? 15.535  ops/ms
EishayWriteStringTree.gson                               thrpt    5   247.651 ?  2.551  ops/ms
EishayWriteStringTree.jackson                            thrpt    5  1033.315 ?  7.138  ops/ms
EishayWriteStringTree1x.fastjson1                        thrpt    5   749.885 ? 11.822  ops/ms
EishayWriteStringTree1x.fastjson2                        thrpt    5  1214.924 ?  7.845  ops/ms
EishayWriteStringTree1x.gson                             thrpt    5   243.917 ?  0.769  ops/ms
EishayWriteStringTree1x.jackson                          thrpt    5   846.777 ?  8.195  ops/ms
EishayWriteUTF8Bytes.fastjson1                           thrpt    5   714.339 ?  8.343  ops/ms
EishayWriteUTF8Bytes.fastjson2                           thrpt    5  2016.074 ?  8.426  ops/ms
EishayWriteUTF8Bytes.gson                                thrpt    5   235.396 ?  1.374  ops/ms
EishayWriteUTF8Bytes.jackson                             thrpt    5  1068.326 ? 15.686  ops/ms
```
# ecs.c7.xlarge-graalvm-ee-java11-22.3.1
```java
Benchmark                                                 Mode  Cnt     Score    Error   Units
EishayParseBinary.fastjson2UTF8Bytes                     thrpt    5  1518.618 ? 11.617  ops/ms
EishayParseBinary.hessian                                thrpt    5   398.073 ?  3.097  ops/ms
EishayParseBinary.javaSerialize                          thrpt    5    52.863 ?  0.364  ops/ms
EishayParseBinary.jsonb                                  thrpt    5  3560.223 ? 21.320  ops/ms
EishayParseBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5  1744.470 ?  8.387  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  2242.897 ? 10.849  ops/ms
EishayParseBinaryArrayMapping.jsonb                      thrpt    5  5677.920 ? 21.792  ops/ms
EishayParseBinaryArrayMapping.kryo                       thrpt    5  1840.564 ? 24.767  ops/ms
EishayParseBinaryArrayMapping.protobuf                   thrpt    5  2135.575 ? 38.353  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB                 thrpt    5  2475.178 ? 14.500  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB_autoTypeFilter  thrpt    5  2384.201 ? 26.984  ops/ms
EishayParseBinaryAutoType.hessian                        thrpt    5   391.213 ?  5.354  ops/ms
EishayParseBinaryAutoType.javaSerialize                  thrpt    5    52.328 ?  0.440  ops/ms
EishayParseString.fastjson1                              thrpt    5  1177.810 ?  6.444  ops/ms
EishayParseString.fastjson2                              thrpt    5  1458.074 ? 15.616  ops/ms
EishayParseString.gson                                   thrpt    5   490.568 ?  3.384  ops/ms
EishayParseString.jackson                                thrpt    5   499.382 ?  5.355  ops/ms
EishayParseStringPretty.fastjson1                        thrpt    5   324.150 ?  8.490  ops/ms
EishayParseStringPretty.fastjson2                        thrpt    5   931.902 ?  6.384  ops/ms
EishayParseStringPretty.gson                             thrpt    5   436.676 ?  2.469  ops/ms
EishayParseStringPretty.jackson                          thrpt    5   448.951 ? 15.846  ops/ms
EishayParseTreeString.fastjson1                          thrpt    5   597.009 ?  6.107  ops/ms
EishayParseTreeString.fastjson2                          thrpt    5  1043.986 ?  5.010  ops/ms
EishayParseTreeString.gson                               thrpt    5   425.048 ?  3.856  ops/ms
EishayParseTreeString.jackson                            thrpt    5   560.948 ?  3.695  ops/ms
EishayParseTreeStringPretty.fastjson1                    thrpt    5   474.323 ?  6.890  ops/ms
EishayParseTreeStringPretty.fastjson2                    thrpt    5   858.105 ?  4.462  ops/ms
EishayParseTreeStringPretty.gson                         thrpt    5   402.688 ?  3.099  ops/ms
EishayParseTreeStringPretty.jackson                      thrpt    5   496.982 ?  2.136  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                       thrpt    5   504.729 ?  4.735  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                       thrpt    5  1015.726 ?  9.916  ops/ms
EishayParseTreeUTF8Bytes.gson                            thrpt    5   417.074 ?  4.283  ops/ms
EishayParseTreeUTF8Bytes.jackson                         thrpt    5   601.003 ?  6.047  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1                 thrpt    5   386.686 ?  2.794  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2                 thrpt    5   845.635 ?  6.914  ops/ms
EishayParseTreeUTF8BytesPretty.gson                      thrpt    5   388.661 ?  3.771  ops/ms
EishayParseTreeUTF8BytesPretty.jackson                   thrpt    5   556.526 ?  8.361  ops/ms
EishayParseUTF8Bytes.dsljson                             thrpt    5   965.567 ?  2.859  ops/ms
EishayParseUTF8Bytes.fastjson1                           thrpt    5   828.819 ?  4.618  ops/ms
EishayParseUTF8Bytes.fastjson2                           thrpt    5  1421.358 ?  7.644  ops/ms
EishayParseUTF8Bytes.gson                                thrpt    5   415.951 ?  6.097  ops/ms
EishayParseUTF8Bytes.jackson                             thrpt    5   531.992 ?  5.537  ops/ms
EishayParseUTF8BytesPretty.fastjson1                     thrpt    5   271.672 ?  3.707  ops/ms
EishayParseUTF8BytesPretty.fastjson2                     thrpt    5   984.281 ?  4.860  ops/ms
EishayParseUTF8BytesPretty.gson                          thrpt    5   388.626 ?  3.151  ops/ms
EishayParseUTF8BytesPretty.jackson                       thrpt    5   496.063 ?  0.594  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes                     thrpt    5  1937.779 ? 24.262  ops/ms
EishayWriteBinary.hessian                                thrpt    5   478.785 ?  2.087  ops/ms
EishayWriteBinary.javaSerialize                          thrpt    5   241.327 ?  1.662  ops/ms
EishayWriteBinary.jsonb                                  thrpt    5  3900.271 ? 37.939  ops/ms
EishayWriteBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   811.757 ?  7.060  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  2488.904 ? 20.746  ops/ms
EishayWriteBinaryArrayMapping.jsonb                      thrpt    5  8588.629 ? 67.670  ops/ms
EishayWriteBinaryArrayMapping.kryo                       thrpt    5  2540.832 ? 17.585  ops/ms
EishayWriteBinaryArrayMapping.protobuf                   thrpt    5  3462.649 ? 21.197  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB                 thrpt    5  1819.699 ? 33.230  ops/ms
EishayWriteBinaryAutoType.fastjson2UTF8Bytes             thrpt    5  1858.835 ? 11.351  ops/ms
EishayWriteBinaryAutoType.hessian                        thrpt    5   477.072 ?  4.472  ops/ms
EishayWriteBinaryAutoType.javaSerialize                  thrpt    5   245.992 ?  3.788  ops/ms
EishayWriteString.fastjson1                              thrpt    5   775.182 ? 10.892  ops/ms
EishayWriteString.fastjson2                              thrpt    5  1884.133 ? 19.257  ops/ms
EishayWriteString.gson                                   thrpt    5   372.492 ?  3.430  ops/ms
EishayWriteString.jackson                                thrpt    5   969.914 ?  6.603  ops/ms
EishayWriteStringTree.fastjson1                          thrpt    5   867.861 ?  8.925  ops/ms
EishayWriteStringTree.fastjson2                          thrpt    5  1457.597 ?  8.512  ops/ms
EishayWriteStringTree.gson                               thrpt    5   387.679 ?  6.528  ops/ms
EishayWriteStringTree.jackson                            thrpt    5   923.266 ? 12.405  ops/ms
EishayWriteStringTree1x.fastjson1                        thrpt    5   901.044 ?  7.404  ops/ms
EishayWriteStringTree1x.fastjson2                        thrpt    5  1334.829 ?  9.288  ops/ms
EishayWriteStringTree1x.gson                             thrpt    5   377.644 ?  4.671  ops/ms
EishayWriteStringTree1x.jackson                          thrpt    5   924.527 ? 22.222  ops/ms
EishayWriteUTF8Bytes.fastjson1                           thrpt    5   667.122 ?  6.541  ops/ms
EishayWriteUTF8Bytes.fastjson2                           thrpt    5  1922.464 ? 11.769  ops/ms
EishayWriteUTF8Bytes.gson                                thrpt    5   338.824 ?  1.723  ops/ms
EishayWriteUTF8Bytes.jackson                             thrpt    5  1053.645 ?  9.927  ops/ms
```
# ecs.c7.xlarge-graalvm-ee-java17-22.3.1
```java
Benchmark                                                 Mode  Cnt      Score    Error   Units
EishayParseBinary.fastjson2UTF8Bytes                     thrpt    5   1341.815 ?  5.260  ops/ms
EishayParseBinary.hessian                                thrpt    5    390.149 ?  2.420  ops/ms
EishayParseBinary.javaSerialize                          thrpt    5     57.619 ?  0.350  ops/ms
EishayParseBinary.jsonb                                  thrpt    5   3574.920 ? 11.372  ops/ms
EishayParseBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   1747.509 ? 15.863  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5   2233.336 ?  9.002  ops/ms
EishayParseBinaryArrayMapping.jsonb                      thrpt    5   6616.327 ? 34.940  ops/ms
EishayParseBinaryArrayMapping.kryo                       thrpt    5   1809.363 ?  6.840  ops/ms
EishayParseBinaryArrayMapping.protobuf                   thrpt    5   2616.040 ? 11.881  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB                 thrpt    5   2215.074 ? 20.621  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB_autoTypeFilter  thrpt    5   2134.232 ? 11.046  ops/ms
EishayParseBinaryAutoType.hessian                        thrpt    5    410.462 ?  1.689  ops/ms
EishayParseBinaryAutoType.javaSerialize                  thrpt    5     58.568 ?  0.357  ops/ms
EishayParseString.fastjson1                              thrpt    5   1701.052 ?  6.526  ops/ms
EishayParseString.fastjson2                              thrpt    5   1369.841 ?  3.587  ops/ms
EishayParseString.gson                                   thrpt    5    474.635 ?  3.330  ops/ms
EishayParseString.jackson                                thrpt    5    479.175 ?  1.959  ops/ms
EishayParseStringPretty.fastjson1                        thrpt    5    388.067 ?  5.211  ops/ms
EishayParseStringPretty.fastjson2                        thrpt    5   1018.198 ?  5.279  ops/ms
EishayParseStringPretty.gson                             thrpt    5    435.581 ?  1.636  ops/ms
EishayParseStringPretty.jackson                          thrpt    5    440.217 ?  2.350  ops/ms
EishayParseTreeString.fastjson1                          thrpt    5    686.780 ?  3.338  ops/ms
EishayParseTreeString.fastjson2                          thrpt    5   1084.823 ?  9.083  ops/ms
EishayParseTreeString.gson                               thrpt    5    431.693 ?  2.425  ops/ms
EishayParseTreeString.jackson                            thrpt    5    553.938 ?  2.334  ops/ms
EishayParseTreeStringPretty.fastjson1                    thrpt    5    555.149 ?  1.615  ops/ms
EishayParseTreeStringPretty.fastjson2                    thrpt    5    881.284 ?  3.066  ops/ms
EishayParseTreeStringPretty.gson                         thrpt    5    395.444 ?  2.773  ops/ms
EishayParseTreeStringPretty.jackson                      thrpt    5    509.876 ?  2.013  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                       thrpt    5    570.859 ?  3.267  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                       thrpt    5   1056.021 ?  6.306  ops/ms
EishayParseTreeUTF8Bytes.gson                            thrpt    5    425.372 ?  1.656  ops/ms
EishayParseTreeUTF8Bytes.jackson                         thrpt    5    576.233 ?  1.795  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1                 thrpt    5    458.030 ?  1.645  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2                 thrpt    5    873.209 ?  3.166  ops/ms
EishayParseTreeUTF8BytesPretty.gson                      thrpt    5    402.814 ?  1.132  ops/ms
EishayParseTreeUTF8BytesPretty.jackson                   thrpt    5    568.382 ?  1.507  ops/ms
EishayParseUTF8Bytes.dsljson                             thrpt    5    922.725 ?  6.355  ops/ms
EishayParseUTF8Bytes.fastjson1                           thrpt    5   1217.239 ?  3.805  ops/ms
EishayParseUTF8Bytes.fastjson2                           thrpt    5   1396.281 ?  5.558  ops/ms
EishayParseUTF8Bytes.gson                                thrpt    5    426.546 ?  1.900  ops/ms
EishayParseUTF8Bytes.jackson                             thrpt    5    560.877 ?  1.626  ops/ms
EishayParseUTF8BytesPretty.fastjson1                     thrpt    5    361.673 ?  2.017  ops/ms
EishayParseUTF8BytesPretty.fastjson2                     thrpt    5    938.099 ?  6.633  ops/ms
EishayParseUTF8BytesPretty.gson                          thrpt    5    406.191 ?  4.291  ops/ms
EishayParseUTF8BytesPretty.jackson                       thrpt    5    508.970 ?  1.735  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes                     thrpt    5   1982.089 ?  4.948  ops/ms
EishayWriteBinary.hessian                                thrpt    5    493.806 ?  1.971  ops/ms
EishayWriteBinary.javaSerialize                          thrpt    5    241.384 ?  2.101  ops/ms
EishayWriteBinary.jsonb                                  thrpt    5   4000.404 ? 14.520  ops/ms
EishayWriteBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5    894.960 ?  5.436  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5   2548.555 ? 17.696  ops/ms
EishayWriteBinaryArrayMapping.jsonb                      thrpt    5  10442.062 ? 24.346  ops/ms
EishayWriteBinaryArrayMapping.kryo                       thrpt    5   2472.066 ? 13.509  ops/ms
EishayWriteBinaryArrayMapping.protobuf                   thrpt    5   3509.048 ? 15.041  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB                 thrpt    5   1885.132 ? 15.050  ops/ms
EishayWriteBinaryAutoType.fastjson2UTF8Bytes             thrpt    5   1952.366 ? 16.888  ops/ms
EishayWriteBinaryAutoType.hessian                        thrpt    5    486.277 ?  1.417  ops/ms
EishayWriteBinaryAutoType.javaSerialize                  thrpt    5    240.504 ?  0.986  ops/ms
EishayWriteString.fastjson1                              thrpt    5    781.905 ?  1.886  ops/ms
EishayWriteString.fastjson2                              thrpt    5   1885.386 ?  7.666  ops/ms
EishayWriteString.gson                                   thrpt    5    244.389 ?  1.347  ops/ms
EishayWriteString.jackson                                thrpt    5    993.338 ? 10.547  ops/ms
EishayWriteStringTree.fastjson1                          thrpt    5    930.400 ?  3.381  ops/ms
EishayWriteStringTree.fastjson2                          thrpt    5   1487.974 ? 10.792  ops/ms
EishayWriteStringTree.gson                               thrpt    5    249.245 ?  0.935  ops/ms
EishayWriteStringTree.jackson                            thrpt    5    959.596 ?  8.936  ops/ms
EishayWriteStringTree1x.fastjson1                        thrpt    5    888.332 ?  4.052  ops/ms
EishayWriteStringTree1x.fastjson2                        thrpt    5   1328.452 ?  6.575  ops/ms
EishayWriteStringTree1x.gson                             thrpt    5    248.864 ?  1.662  ops/ms
EishayWriteStringTree1x.jackson                          thrpt    5    923.678 ?  3.003  ops/ms
EishayWriteUTF8Bytes.fastjson1                           thrpt    5    761.867 ?  3.804  ops/ms
EishayWriteUTF8Bytes.fastjson2                           thrpt    5   1981.814 ? 10.255  ops/ms
EishayWriteUTF8Bytes.gson                                thrpt    5    244.619 ?  1.393  ops/ms
EishayWriteUTF8Bytes.jackson                             thrpt    5   1085.742 ?  9.008  ops/ms
```

# OrangePi5-jdk1.8.0_361
```java
Benchmark                                                 Mode  Cnt     Score     Error   Units
EishayParseBinary.fastjson2UTF8Bytes                     thrpt    5   558.805 ±  18.891  ops/ms
EishayParseBinary.hessian                                thrpt    5   133.112 ±   3.895  ops/ms
EishayParseBinary.javaSerialize                          thrpt    5    26.947 ±   1.244  ops/ms
EishayParseBinary.jsonb                                  thrpt    5   984.376 ±  36.978  ops/ms
EishayParseBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   692.504 ±  19.669  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5   807.756 ±  31.525  ops/ms
EishayParseBinaryArrayMapping.jsonb                      thrpt    5  1706.577 ± 107.347  ops/ms
EishayParseBinaryArrayMapping.kryo                       thrpt    5   986.741 ±  58.683  ops/ms
EishayParseBinaryArrayMapping.protobuf                   thrpt    5   675.494 ±  48.701  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB                 thrpt    5   632.850 ±  44.118  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB_autoTypeFilter  thrpt    5   633.157 ±  43.323  ops/ms
EishayParseBinaryAutoType.hessian                        thrpt    5   149.914 ±  11.721  ops/ms
EishayParseBinaryAutoType.javaSerialize                  thrpt    5    26.660 ±   1.431  ops/ms
EishayParseString.fastjson1                              thrpt    5   562.067 ±  37.139  ops/ms
EishayParseString.fastjson2                              thrpt    5   666.967 ±  29.854  ops/ms
EishayParseString.gson                                   thrpt    5   219.222 ±  10.066  ops/ms
EishayParseString.jackson                                thrpt    5   278.829 ±  21.957  ops/ms
EishayParseStringPretty.fastjson1                        thrpt    5   157.818 ±   6.826  ops/ms
EishayParseStringPretty.fastjson2                        thrpt    5   530.328 ±  33.841  ops/ms
EishayParseStringPretty.gson                             thrpt    5   207.378 ±   7.693  ops/ms
EishayParseStringPretty.jackson                          thrpt    5   244.058 ±  12.866  ops/ms
EishayParseTreeString.fastjson1                          thrpt    5   216.838 ±   9.124  ops/ms
EishayParseTreeString.fastjson2                          thrpt    5   426.732 ±  20.136  ops/ms
EishayParseTreeString.gson                               thrpt    5   180.686 ±   7.801  ops/ms
EishayParseTreeString.jackson                            thrpt    5   236.187 ±  18.052  ops/ms
EishayParseTreeStringPretty.fastjson1                    thrpt    5   205.884 ±   8.148  ops/ms
EishayParseTreeStringPretty.fastjson2                    thrpt    5   379.218 ±  16.055  ops/ms
EishayParseTreeStringPretty.gson                         thrpt    5   170.956 ±   4.457  ops/ms
EishayParseTreeStringPretty.jackson                      thrpt    5   223.512 ±  11.903  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                       thrpt    5   174.033 ±   5.734  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                       thrpt    5   400.205 ±  18.265  ops/ms
EishayParseTreeUTF8Bytes.gson                            thrpt    5   157.755 ±   7.604  ops/ms
EishayParseTreeUTF8Bytes.jackson                         thrpt    5   290.200 ±  20.172  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1                 thrpt    5   153.348 ±   5.370  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2                 thrpt    5   345.466 ±  11.757  ops/ms
EishayParseTreeUTF8BytesPretty.gson                      thrpt    5   145.416 ±   7.386  ops/ms
EishayParseTreeUTF8BytesPretty.jackson                   thrpt    5   279.503 ±  11.826  ops/ms
EishayParseUTF8Bytes.dsljson                             thrpt    5   452.948 ±  17.373  ops/ms
EishayParseUTF8Bytes.fastjson1                           thrpt    5   408.174 ±  16.136  ops/ms
EishayParseUTF8Bytes.fastjson2                           thrpt    5   552.625 ±  31.892  ops/ms
EishayParseUTF8Bytes.gson                                thrpt    5   153.166 ±   8.162  ops/ms
EishayParseUTF8Bytes.jackson                             thrpt    5   304.581 ±  19.097  ops/ms
EishayParseUTF8BytesPretty.fastjson1                     thrpt    5   136.423 ±   5.399  ops/ms
EishayParseUTF8BytesPretty.fastjson2                     thrpt    5   456.449 ±  28.230  ops/ms
EishayParseUTF8BytesPretty.gson                          thrpt    5   147.212 ±   5.604  ops/ms
EishayParseUTF8BytesPretty.jackson                       thrpt    5   290.981 ±   7.275  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes                     thrpt    5   825.871 ±  53.367  ops/ms
EishayWriteBinary.hessian                                thrpt    5   184.968 ±  41.251  ops/ms
EishayWriteBinary.javaSerialize                          thrpt    5   119.054 ±   4.218  ops/ms
EishayWriteBinary.jsonb                                  thrpt    5  1019.659 ± 121.186  ops/ms
EishayWriteBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   326.842 ±  17.712  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5   972.417 ±  53.871  ops/ms
EishayWriteBinaryArrayMapping.jsonb                      thrpt    5  1539.861 ±  56.257  ops/ms
EishayWriteBinaryArrayMapping.kryo                       thrpt    5   840.541 ±  57.708  ops/ms
EishayWriteBinaryArrayMapping.protobuf                   thrpt    5   679.258 ±  53.316  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB                 thrpt    5   665.615 ±  45.461  ops/ms
EishayWriteBinaryAutoType.fastjson2UTF8Bytes             thrpt    5   783.496 ±  34.574  ops/ms
EishayWriteBinaryAutoType.hessian                        thrpt    5   188.513 ±  23.435  ops/ms
EishayWriteBinaryAutoType.javaSerialize                  thrpt    5   120.762 ±   4.440  ops/ms
EishayWriteString.fastjson1                              thrpt    5   271.899 ±   9.938  ops/ms
EishayWriteString.fastjson2                              thrpt    5   692.295 ±  16.628  ops/ms
EishayWriteString.gson                                   thrpt    5   207.440 ±  12.978  ops/ms
EishayWriteString.jackson                                thrpt    5   377.990 ±  21.872  ops/ms
EishayWriteStringTree.fastjson1                          thrpt    5   334.377 ±  22.055  ops/ms
EishayWriteStringTree.fastjson2                          thrpt    5   485.238 ±  41.831  ops/ms
EishayWriteStringTree.gson                               thrpt    5   225.198 ±  15.272  ops/ms
EishayWriteStringTree.jackson                            thrpt    5   393.534 ±   5.748  ops/ms
EishayWriteStringTree1x.fastjson1                        thrpt    5   323.246 ±  10.807  ops/ms
EishayWriteStringTree1x.fastjson2                        thrpt    5   434.113 ±  15.989  ops/ms
EishayWriteStringTree1x.gson                             thrpt    5   224.430 ±   5.287  ops/ms
EishayWriteStringTree1x.jackson                          thrpt    5   365.824 ±  11.902  ops/ms
EishayWriteUTF8Bytes.fastjson1                           thrpt    5   265.172 ±  12.677  ops/ms
EishayWriteUTF8Bytes.fastjson2                           thrpt    5   824.867 ±  44.424  ops/ms
EishayWriteUTF8Bytes.gson                                thrpt    5   171.973 ±   6.735  ops/ms
EishayWriteUTF8Bytes.jackson                             thrpt    5   394.485 ±  12.477  ops/ms
```
# OrangePi5-jdk-11.0.18
```java
Benchmark                                                 Mode  Cnt     Score     Error   Units
EishayParseBinary.fastjson2UTF8Bytes                     thrpt    5   695.591 ±  13.986  ops/ms
EishayParseBinary.hessian                                thrpt    5   134.948 ±   0.295  ops/ms
EishayParseBinary.javaSerialize                          thrpt    5    26.370 ±   0.033  ops/ms
EishayParseBinary.jsonb                                  thrpt    5  1311.434 ±  57.460  ops/ms
EishayParseBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   746.190 ±  32.063  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5   997.994 ±  37.175  ops/ms
EishayParseBinaryArrayMapping.jsonb                      thrpt    5  2040.717 ± 130.159  ops/ms
EishayParseBinaryArrayMapping.kryo                       thrpt    5   935.555 ±  14.867  ops/ms
EishayParseBinaryArrayMapping.protobuf                   thrpt    5   704.601 ±  22.916  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB                 thrpt    5   799.463 ±   4.054  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB_autoTypeFilter  thrpt    5   812.117 ±  31.187  ops/ms
EishayParseBinaryAutoType.hessian                        thrpt    5   134.644 ±   3.899  ops/ms
EishayParseBinaryAutoType.javaSerialize                  thrpt    5    26.786 ±   0.118  ops/ms
EishayParseString.fastjson1                              thrpt    5   565.786 ±  25.402  ops/ms
EishayParseString.fastjson2                              thrpt    5   677.041 ±   8.346  ops/ms
EishayParseString.gson                                   thrpt    5   223.121 ±   3.108  ops/ms
EishayParseString.jackson                                thrpt    5   260.076 ±   3.967  ops/ms
EishayParseStringPretty.fastjson1                        thrpt    5   161.625 ±   6.095  ops/ms
EishayParseStringPretty.fastjson2                        thrpt    5   504.466 ±  19.512  ops/ms
EishayParseStringPretty.gson                             thrpt    5   209.540 ±   5.905  ops/ms
EishayParseStringPretty.jackson                          thrpt    5   248.201 ±   7.099  ops/ms
EishayParseTreeString.fastjson1                          thrpt    5   234.435 ±   8.489  ops/ms
EishayParseTreeString.fastjson2                          thrpt    5   464.786 ±  10.474  ops/ms
EishayParseTreeString.gson                               thrpt    5   186.731 ±   9.235  ops/ms
EishayParseTreeString.jackson                            thrpt    5   241.197 ±  10.410  ops/ms
EishayParseTreeStringPretty.fastjson1                    thrpt    5   189.698 ±   8.642  ops/ms
EishayParseTreeStringPretty.fastjson2                    thrpt    5   382.905 ±  14.259  ops/ms
EishayParseTreeStringPretty.gson                         thrpt    5   177.554 ±   3.739  ops/ms
EishayParseTreeStringPretty.jackson                      thrpt    5   226.189 ±   1.199  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                       thrpt    5   208.578 ±   6.253  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                       thrpt    5   471.127 ±   6.774  ops/ms
EishayParseTreeUTF8Bytes.gson                            thrpt    5   174.441 ±  10.411  ops/ms
EishayParseTreeUTF8Bytes.jackson                         thrpt    5   284.777 ±   6.562  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1                 thrpt    5   165.705 ±   0.218  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2                 thrpt    5   385.190 ±  14.830  ops/ms
EishayParseTreeUTF8BytesPretty.gson                      thrpt    5   165.256 ±   5.316  ops/ms
EishayParseTreeUTF8BytesPretty.jackson                   thrpt    5   266.034 ±   3.243  ops/ms
EishayParseUTF8Bytes.dsljson                             thrpt    5   435.238 ±  14.960  ops/ms
EishayParseUTF8Bytes.fastjson1                           thrpt    5   453.000 ±  21.101  ops/ms
EishayParseUTF8Bytes.fastjson2                           thrpt    5   678.369 ±   0.590  ops/ms
EishayParseUTF8Bytes.gson                                thrpt    5   175.171 ±   6.866  ops/ms
EishayParseUTF8Bytes.jackson                             thrpt    5   311.674 ±   7.886  ops/ms
EishayParseUTF8BytesPretty.fastjson1                     thrpt    5   143.964 ±   3.584  ops/ms
EishayParseUTF8BytesPretty.fastjson2                     thrpt    5   509.785 ±  19.617  ops/ms
EishayParseUTF8BytesPretty.gson                          thrpt    5   162.869 ±   5.830  ops/ms
EishayParseUTF8BytesPretty.jackson                       thrpt    5   277.998 ±   7.931  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes                     thrpt    5   865.116 ±  34.879  ops/ms
EishayWriteBinary.hessian                                thrpt    5   195.551 ±  27.559  ops/ms
EishayWriteBinary.javaSerialize                          thrpt    5   132.001 ±   7.841  ops/ms
EishayWriteBinary.jsonb                                  thrpt    5  1357.042 ±  50.220  ops/ms
EishayWriteBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   301.712 ±  14.636  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  1058.555 ±  29.649  ops/ms
EishayWriteBinaryArrayMapping.jsonb                      thrpt    5  2367.514 ±  99.571  ops/ms
EishayWriteBinaryArrayMapping.kryo                       thrpt    5   989.968 ±   2.335  ops/ms
EishayWriteBinaryArrayMapping.protobuf                   thrpt    5   857.477 ±  29.569  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB                 thrpt    5   755.611 ±  17.260  ops/ms
EishayWriteBinaryAutoType.fastjson2UTF8Bytes             thrpt    5   851.757 ±  43.471  ops/ms
EishayWriteBinaryAutoType.hessian                        thrpt    5   198.726 ±  44.999  ops/ms
EishayWriteBinaryAutoType.javaSerialize                  thrpt    5   131.659 ±   5.179  ops/ms
EishayWriteString.fastjson1                              thrpt    5   315.617 ±   9.233  ops/ms
EishayWriteString.fastjson2                              thrpt    5   825.878 ±  21.892  ops/ms
EishayWriteString.gson                                   thrpt    5   206.836 ±   8.606  ops/ms
EishayWriteString.jackson                                thrpt    5   421.663 ±   4.936  ops/ms
EishayWriteStringTree.fastjson1                          thrpt    5   352.606 ±  11.647  ops/ms
EishayWriteStringTree.fastjson2                          thrpt    5   596.344 ±  14.972  ops/ms
EishayWriteStringTree.gson                               thrpt    5   216.667 ±   4.563  ops/ms
EishayWriteStringTree.jackson                            thrpt    5   444.654 ±   0.281  ops/ms
EishayWriteStringTree1x.fastjson1                        thrpt    5   349.423 ±   6.347  ops/ms
EishayWriteStringTree1x.fastjson2                        thrpt    5   537.155 ±   1.963  ops/ms
EishayWriteStringTree1x.gson                             thrpt    5   215.101 ±   3.156  ops/ms
EishayWriteStringTree1x.jackson                          thrpt    5   424.508 ±  11.445  ops/ms
EishayWriteUTF8Bytes.fastjson1                           thrpt    5   293.521 ±   1.066  ops/ms
EishayWriteUTF8Bytes.fastjson2                           thrpt    5   905.415 ±   3.312  ops/ms
EishayWriteUTF8Bytes.gson                                thrpt    5   185.005 ±   5.293  ops/ms
EishayWriteUTF8Bytes.jackson                             thrpt    5   391.477 ±   9.937  ops/ms
```
# OrangePi5-jdk-17.0.6
```java
Benchmark                                                 Mode  Cnt     Score    Error   Units
EishayParseBinary.fastjson2UTF8Bytes                     thrpt    5   720.816 ± 32.638  ops/ms
EishayParseBinary.hessian                                thrpt    5   152.298 ±  3.046  ops/ms
EishayParseBinary.javaSerialize                          thrpt    5    28.921 ±  0.535  ops/ms
EishayParseBinary.jsonb                                  thrpt    5  1387.438 ± 52.723  ops/ms
EishayParseBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   831.357 ± 41.493  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  1004.992 ± 34.244  ops/ms
EishayParseBinaryArrayMapping.jsonb                      thrpt    5  2182.835 ± 53.585  ops/ms
EishayParseBinaryArrayMapping.kryo                       thrpt    5   993.052 ± 49.551  ops/ms
EishayParseBinaryArrayMapping.protobuf                   thrpt    5   903.922 ± 12.282  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB                 thrpt    5   821.304 ±  8.906  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB_autoTypeFilter  thrpt    5   813.715 ± 14.913  ops/ms
EishayParseBinaryAutoType.hessian                        thrpt    5   151.213 ±  3.129  ops/ms
EishayParseBinaryAutoType.javaSerialize                  thrpt    5    29.499 ±  0.893  ops/ms
EishayParseString.fastjson1                              thrpt    5   791.620 ± 31.383  ops/ms
EishayParseString.fastjson2                              thrpt    5   692.222 ± 23.184  ops/ms
EishayParseString.gson                                   thrpt    5   221.791 ±  5.302  ops/ms
EishayParseString.jackson                                thrpt    5   274.316 ±  5.556  ops/ms
EishayParseStringPretty.fastjson1                        thrpt    5   186.359 ±  0.901  ops/ms
EishayParseStringPretty.fastjson2                        thrpt    5   511.994 ± 24.690  ops/ms
EishayParseStringPretty.gson                             thrpt    5   208.950 ± 10.936  ops/ms
EishayParseStringPretty.jackson                          thrpt    5   254.810 ±  2.200  ops/ms
EishayParseTreeString.fastjson1                          thrpt    5   279.406 ± 14.262  ops/ms
EishayParseTreeString.fastjson2                          thrpt    5   480.418 ± 21.815  ops/ms
EishayParseTreeString.gson                               thrpt    5   180.733 ±  3.666  ops/ms
EishayParseTreeString.jackson                            thrpt    5   265.404 ±  9.929  ops/ms
EishayParseTreeStringPretty.fastjson1                    thrpt    5   242.919 ±  7.460  ops/ms
EishayParseTreeStringPretty.fastjson2                    thrpt    5   387.472 ± 17.345  ops/ms
EishayParseTreeStringPretty.gson                         thrpt    5   170.277 ±  7.240  ops/ms
EishayParseTreeStringPretty.jackson                      thrpt    5   240.124 ±  6.333  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                       thrpt    5   243.412 ±  3.502  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                       thrpt    5   469.301 ± 19.118  ops/ms
EishayParseTreeUTF8Bytes.gson                            thrpt    5   169.781 ±  9.424  ops/ms
EishayParseTreeUTF8Bytes.jackson                         thrpt    5   315.812 ± 15.647  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1                 thrpt    5   205.460 ± 10.903  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2                 thrpt    5   385.934 ±  6.553  ops/ms
EishayParseTreeUTF8BytesPretty.gson                      thrpt    5   156.940 ±  1.380  ops/ms
EishayParseTreeUTF8BytesPretty.jackson                   thrpt    5   289.706 ± 23.800  ops/ms
EishayParseUTF8Bytes.dsljson                             thrpt    5   427.398 ± 16.341  ops/ms
EishayParseUTF8Bytes.fastjson1                           thrpt    5   579.041 ± 19.416  ops/ms
EishayParseUTF8Bytes.fastjson2                           thrpt    5   691.771 ± 31.002  ops/ms
EishayParseUTF8Bytes.gson                                thrpt    5   167.988 ±  5.597  ops/ms
EishayParseUTF8Bytes.jackson                             thrpt    5   305.618 ± 10.523  ops/ms
EishayParseUTF8BytesPretty.fastjson1                     thrpt    5   161.192 ±  6.533  ops/ms
EishayParseUTF8BytesPretty.fastjson2                     thrpt    5   510.760 ±  5.672  ops/ms
EishayParseUTF8BytesPretty.gson                          thrpt    5   157.266 ±  7.767  ops/ms
EishayParseUTF8BytesPretty.jackson                       thrpt    5   275.652 ± 11.607  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes                     thrpt    5   866.996 ± 33.842  ops/ms
EishayWriteBinary.hessian                                thrpt    5   200.460 ± 19.539  ops/ms
EishayWriteBinary.javaSerialize                          thrpt    5   130.295 ±  5.301  ops/ms
EishayWriteBinary.jsonb                                  thrpt    5  1401.194 ± 27.027  ops/ms
EishayWriteBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   327.440 ± 11.488  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  1057.954 ± 22.364  ops/ms
EishayWriteBinaryArrayMapping.jsonb                      thrpt    5  2406.928 ± 92.524  ops/ms
EishayWriteBinaryArrayMapping.kryo                       thrpt    5   957.629 ±  4.439  ops/ms
EishayWriteBinaryArrayMapping.protobuf                   thrpt    5   995.626 ± 35.640  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB                 thrpt    5   804.810 ± 31.957  ops/ms
EishayWriteBinaryAutoType.fastjson2UTF8Bytes             thrpt    5   829.429 ± 29.503  ops/ms
EishayWriteBinaryAutoType.hessian                        thrpt    5   196.955 ± 18.186  ops/ms
EishayWriteBinaryAutoType.javaSerialize                  thrpt    5   125.495 ±  6.778  ops/ms
EishayWriteString.fastjson1                              thrpt    5   324.950 ±  5.853  ops/ms
EishayWriteString.fastjson2                              thrpt    5   827.908 ± 44.603  ops/ms
EishayWriteString.gson                                   thrpt    5   165.427 ±  0.766  ops/ms
EishayWriteString.jackson                                thrpt    5   429.209 ± 15.470  ops/ms
EishayWriteStringTree.fastjson1                          thrpt    5   368.913 ± 11.407  ops/ms
EishayWriteStringTree.fastjson2                          thrpt    5   589.724 ± 18.258  ops/ms
EishayWriteStringTree.gson                               thrpt    5   161.727 ±  6.396  ops/ms
EishayWriteStringTree.jackson                            thrpt    5   408.187 ± 16.611  ops/ms
EishayWriteStringTree1x.fastjson1                        thrpt    5   324.772 ± 17.514  ops/ms
EishayWriteStringTree1x.fastjson2                        thrpt    5   439.113 ±  7.040  ops/ms
EishayWriteStringTree1x.gson                             thrpt    5   160.059 ±  0.665  ops/ms
EishayWriteStringTree1x.jackson                          thrpt    5   387.677 ± 18.832  ops/ms
EishayWriteUTF8Bytes.fastjson1                           thrpt    5   280.405 ±  5.208  ops/ms
EishayWriteUTF8Bytes.fastjson2                           thrpt    5   899.322 ± 34.123  ops/ms
EishayWriteUTF8Bytes.gson                                thrpt    5   157.761 ±  4.720  ops/ms
EishayWriteUTF8Bytes.jackson                             thrpt    5   421.539 ±  7.689  ops/ms
```
# OrangePi5-zulu8.68.0.21-ca-jdk8.0.362-linux_aarch64
```java
Benchmark                                                 Mode  Cnt     Score    Error   Units
EishayParseBinary.fastjson2UTF8Bytes                     thrpt    5   549.746 ± 26.896  ops/ms
EishayParseBinary.hessian                                thrpt    5   128.369 ±  4.062  ops/ms
EishayParseBinary.javaSerialize                          thrpt    5    26.371 ±  1.515  ops/ms
EishayParseBinary.jsonb                                  thrpt    5   985.408 ± 45.534  ops/ms
EishayParseBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   683.864 ± 37.398  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5   818.206 ± 28.108  ops/ms
EishayParseBinaryArrayMapping.jsonb                      thrpt    5  1697.445 ± 91.845  ops/ms
EishayParseBinaryArrayMapping.kryo                       thrpt    5   984.282 ± 29.537  ops/ms
EishayParseBinaryArrayMapping.protobuf                   thrpt    5   554.418 ± 30.958  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB                 thrpt    5   630.075 ± 13.886  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB_autoTypeFilter  thrpt    5   634.088 ± 23.783  ops/ms
EishayParseBinaryAutoType.hessian                        thrpt    5   131.281 ±  3.073  ops/ms
EishayParseBinaryAutoType.javaSerialize                  thrpt    5    26.615 ±  1.273  ops/ms
EishayParseString.fastjson1                              thrpt    5   558.952 ± 35.298  ops/ms
EishayParseString.fastjson2                              thrpt    5   661.686 ± 42.040  ops/ms
EishayParseString.gson                                   thrpt    5   220.299 ± 11.189  ops/ms
EishayParseString.jackson                                thrpt    5   271.921 ±  9.546  ops/ms
EishayParseStringPretty.fastjson1                        thrpt    5   150.791 ±  8.988  ops/ms
EishayParseStringPretty.fastjson2                        thrpt    5   524.636 ± 33.567  ops/ms
EishayParseStringPretty.gson                             thrpt    5   204.785 ± 14.912  ops/ms
EishayParseStringPretty.jackson                          thrpt    5   246.685 ± 11.626  ops/ms
EishayParseTreeString.fastjson1                          thrpt    5   214.503 ±  9.825  ops/ms
EishayParseTreeString.fastjson2                          thrpt    5   434.508 ± 19.642  ops/ms
EishayParseTreeString.gson                               thrpt    5   177.399 ±  7.032  ops/ms
EishayParseTreeString.jackson                            thrpt    5   235.739 ±  7.501  ops/ms
EishayParseTreeStringPretty.fastjson1                    thrpt    5   205.091 ±  9.872  ops/ms
EishayParseTreeStringPretty.fastjson2                    thrpt    5   377.484 ±  8.774  ops/ms
EishayParseTreeStringPretty.gson                         thrpt    5   163.979 ±  4.964  ops/ms
EishayParseTreeStringPretty.jackson                      thrpt    5   223.870 ± 12.590  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                       thrpt    5   171.559 ±  7.333  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                       thrpt    5   391.901 ± 40.285  ops/ms
EishayParseTreeUTF8Bytes.gson                            thrpt    5   157.094 ±  4.227  ops/ms
EishayParseTreeUTF8Bytes.jackson                         thrpt    5   288.963 ± 18.437  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1                 thrpt    5   153.124 ±  4.721  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2                 thrpt    5   344.363 ± 19.276  ops/ms
EishayParseTreeUTF8BytesPretty.gson                      thrpt    5   144.922 ±  3.472  ops/ms
EishayParseTreeUTF8BytesPretty.jackson                   thrpt    5   268.528 ±  6.184  ops/ms
EishayParseUTF8Bytes.dsljson                             thrpt    5   446.117 ± 21.547  ops/ms
EishayParseUTF8Bytes.fastjson1                           thrpt    5   404.543 ± 19.672  ops/ms
EishayParseUTF8Bytes.fastjson2                           thrpt    5   539.414 ± 29.141  ops/ms
EishayParseUTF8Bytes.gson                                thrpt    5   154.054 ± 12.780  ops/ms
EishayParseUTF8Bytes.jackson                             thrpt    5   316.268 ±  6.125  ops/ms
EishayParseUTF8BytesPretty.fastjson1                     thrpt    5   137.521 ±  7.234  ops/ms
EishayParseUTF8BytesPretty.fastjson2                     thrpt    5   449.827 ± 13.469  ops/ms
EishayParseUTF8BytesPretty.gson                          thrpt    5   146.161 ±  8.160  ops/ms
EishayParseUTF8BytesPretty.jackson                       thrpt    5   295.078 ± 17.887  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes                     thrpt    5   818.723 ± 56.050  ops/ms
EishayWriteBinary.hessian                                thrpt    5   184.955 ± 20.849  ops/ms
EishayWriteBinary.javaSerialize                          thrpt    5   121.403 ±  2.837  ops/ms
EishayWriteBinary.jsonb                                  thrpt    5  1089.842 ± 58.577  ops/ms
EishayWriteBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   318.880 ± 18.053  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5   961.180 ± 18.685  ops/ms
EishayWriteBinaryArrayMapping.jsonb                      thrpt    5  1564.910 ± 46.402  ops/ms
EishayWriteBinaryArrayMapping.kryo                       thrpt    5   839.818 ± 30.121  ops/ms
EishayWriteBinaryArrayMapping.protobuf                   thrpt    5   691.397 ± 17.083  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB                 thrpt    5   678.598 ± 28.875  ops/ms
EishayWriteBinaryAutoType.fastjson2UTF8Bytes             thrpt    5   782.022 ± 35.295  ops/ms
EishayWriteBinaryAutoType.hessian                        thrpt    5   181.670 ± 13.126  ops/ms
EishayWriteBinaryAutoType.javaSerialize                  thrpt    5   123.531 ±  3.362  ops/ms
EishayWriteString.fastjson1                              thrpt    5   300.261 ± 10.119  ops/ms
EishayWriteString.fastjson2                              thrpt    5   700.500 ± 21.363  ops/ms
EishayWriteString.gson                                   thrpt    5   206.628 ± 13.761  ops/ms
EishayWriteString.jackson                                thrpt    5   384.987 ± 19.578  ops/ms
EishayWriteStringTree.fastjson1                          thrpt    5   326.261 ± 15.185  ops/ms
EishayWriteStringTree.fastjson2                          thrpt    5   483.310 ± 29.626  ops/ms
EishayWriteStringTree.gson                               thrpt    5   234.414 ± 20.349  ops/ms
EishayWriteStringTree.jackson                            thrpt    5   386.523 ± 27.714  ops/ms
EishayWriteStringTree1x.fastjson1                        thrpt    5   314.059 ±  9.123  ops/ms
EishayWriteStringTree1x.fastjson2                        thrpt    5   434.156 ± 12.559  ops/ms
EishayWriteStringTree1x.gson                             thrpt    5   220.549 ±  8.609  ops/ms
EishayWriteStringTree1x.jackson                          thrpt    5   372.316 ± 23.851  ops/ms
EishayWriteUTF8Bytes.fastjson1                           thrpt    5   267.113 ± 12.449  ops/ms
EishayWriteUTF8Bytes.fastjson2                           thrpt    5   809.498 ± 46.180  ops/ms
EishayWriteUTF8Bytes.gson                                thrpt    5   170.710 ±  4.576  ops/ms
EishayWriteUTF8Bytes.jackson                             thrpt    5   405.588 ± 12.732  ops/ms
```
# OrangePi5-zulu11.62.17-ca-jdk11.0.18-linux_aarch64
```java
Benchmark                                                 Mode  Cnt     Score    Error   Units
EishayParseBinary.fastjson2UTF8Bytes                     thrpt    5   702.374 ± 36.536  ops/ms
EishayParseBinary.hessian                                thrpt    5   134.508 ±  2.202  ops/ms
EishayParseBinary.javaSerialize                          thrpt    5    25.920 ±  0.142  ops/ms
EishayParseBinary.jsonb                                  thrpt    5  1348.922 ± 17.930  ops/ms
EishayParseBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   745.419 ± 11.792  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5   999.670 ± 42.319  ops/ms
EishayParseBinaryArrayMapping.jsonb                      thrpt    5  2080.421 ± 45.677  ops/ms
EishayParseBinaryArrayMapping.kryo                       thrpt    5   975.756 ± 35.142  ops/ms
EishayParseBinaryArrayMapping.protobuf                   thrpt    5   697.340 ± 10.473  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB                 thrpt    5   823.834 ± 25.364  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB_autoTypeFilter  thrpt    5   806.563 ± 17.931  ops/ms
EishayParseBinaryAutoType.hessian                        thrpt    5   134.546 ±  1.131  ops/ms
EishayParseBinaryAutoType.javaSerialize                  thrpt    5    25.613 ±  0.035  ops/ms
EishayParseString.fastjson1                              thrpt    5   569.129 ± 22.066  ops/ms
EishayParseString.fastjson2                              thrpt    5   682.140 ± 24.604  ops/ms
EishayParseString.gson                                   thrpt    5   227.497 ±  5.760  ops/ms
EishayParseString.jackson                                thrpt    5   263.682 ±  9.246  ops/ms
EishayParseStringPretty.fastjson1                        thrpt    5   156.675 ±  2.858  ops/ms
EishayParseStringPretty.fastjson2                        thrpt    5   514.613 ±  1.110  ops/ms
EishayParseStringPretty.gson                             thrpt    5   208.656 ±  4.644  ops/ms
EishayParseStringPretty.jackson                          thrpt    5   243.397 ± 10.581  ops/ms
EishayParseTreeString.fastjson1                          thrpt    5   235.913 ±  9.018  ops/ms
EishayParseTreeString.fastjson2                          thrpt    5   471.661 ± 13.398  ops/ms
EishayParseTreeString.gson                               thrpt    5   185.339 ±  4.349  ops/ms
EishayParseTreeString.jackson                            thrpt    5   250.842 ± 11.020  ops/ms
EishayParseTreeStringPretty.fastjson1                    thrpt    5   191.655 ±  0.514  ops/ms
EishayParseTreeStringPretty.fastjson2                    thrpt    5   384.122 ± 22.852  ops/ms
EishayParseTreeStringPretty.gson                         thrpt    5   178.706 ±  6.870  ops/ms
EishayParseTreeStringPretty.jackson                      thrpt    5   225.523 ±  5.681  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                       thrpt    5   204.342 ±  3.788  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                       thrpt    5   478.476 ± 14.893  ops/ms
EishayParseTreeUTF8Bytes.gson                            thrpt    5   173.579 ±  5.202  ops/ms
EishayParseTreeUTF8Bytes.jackson                         thrpt    5   288.305 ± 12.691  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1                 thrpt    5   165.112 ±  5.213  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2                 thrpt    5   385.910 ±  9.423  ops/ms
EishayParseTreeUTF8BytesPretty.gson                      thrpt    5   163.123 ±  5.384  ops/ms
EishayParseTreeUTF8BytesPretty.jackson                   thrpt    5   273.962 ±  7.006  ops/ms
EishayParseUTF8Bytes.dsljson                             thrpt    5   427.018 ± 21.645  ops/ms
EishayParseUTF8Bytes.fastjson1                           thrpt    5   453.495 ± 18.651  ops/ms
EishayParseUTF8Bytes.fastjson2                           thrpt    5   683.487 ± 16.502  ops/ms
EishayParseUTF8Bytes.gson                                thrpt    5   172.351 ±  3.149  ops/ms
EishayParseUTF8Bytes.jackson                             thrpt    5   306.746 ± 11.601  ops/ms
EishayParseUTF8BytesPretty.fastjson1                     thrpt    5   143.402 ±  8.625  ops/ms
EishayParseUTF8BytesPretty.fastjson2                     thrpt    5   508.090 ± 31.588  ops/ms
EishayParseUTF8BytesPretty.gson                          thrpt    5   165.105 ±  5.526  ops/ms
EishayParseUTF8BytesPretty.jackson                       thrpt    5   282.549 ± 12.188  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes                     thrpt    5   904.607 ± 24.695  ops/ms
EishayWriteBinary.hessian                                thrpt    5   193.961 ± 42.677  ops/ms
EishayWriteBinary.javaSerialize                          thrpt    5   128.819 ±  1.533  ops/ms
EishayWriteBinary.jsonb                                  thrpt    5  1344.465 ± 97.158  ops/ms
EishayWriteBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   298.119 ± 12.312  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  1047.156 ±  2.279  ops/ms
EishayWriteBinaryArrayMapping.jsonb                      thrpt    5  2296.306 ± 77.472  ops/ms
EishayWriteBinaryArrayMapping.kryo                       thrpt    5   970.658 ± 40.415  ops/ms
EishayWriteBinaryArrayMapping.protobuf                   thrpt    5   830.026 ± 25.039  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB                 thrpt    5   789.387 ± 27.153  ops/ms
EishayWriteBinaryAutoType.fastjson2UTF8Bytes             thrpt    5   843.874 ±  6.463  ops/ms
EishayWriteBinaryAutoType.hessian                        thrpt    5   211.356 ± 47.855  ops/ms
EishayWriteBinaryAutoType.javaSerialize                  thrpt    5   128.277 ±  6.127  ops/ms
EishayWriteString.fastjson1                              thrpt    5   293.627 ±  0.184  ops/ms
EishayWriteString.fastjson2                              thrpt    5   836.959 ± 21.014  ops/ms
EishayWriteString.gson                                   thrpt    5   206.593 ±  6.470  ops/ms
EishayWriteString.jackson                                thrpt    5   403.408 ±  7.265  ops/ms
EishayWriteStringTree.fastjson1                          thrpt    5   368.831 ±  0.729  ops/ms
EishayWriteStringTree.fastjson2                          thrpt    5   598.573 ± 34.413  ops/ms
EishayWriteStringTree.gson                               thrpt    5   217.402 ±  6.363  ops/ms
EishayWriteStringTree.jackson                            thrpt    5   439.048 ± 10.268  ops/ms
EishayWriteStringTree1x.fastjson1                        thrpt    5   322.570 ±  0.746  ops/ms
EishayWriteStringTree1x.fastjson2                        thrpt    5   541.672 ± 17.259  ops/ms
EishayWriteStringTree1x.gson                             thrpt    5   216.879 ±  7.749  ops/ms
EishayWriteStringTree1x.jackson                          thrpt    5   416.736 ± 16.461  ops/ms
EishayWriteUTF8Bytes.fastjson1                           thrpt    5   273.233 ±  7.261  ops/ms
EishayWriteUTF8Bytes.fastjson2                           thrpt    5   931.725 ± 35.221  ops/ms
EishayWriteUTF8Bytes.gson                                thrpt    5   191.824 ±  1.755  ops/ms
EishayWriteUTF8Bytes.jackson                             thrpt    5   373.783 ±  0.243  ops/ms
```
# OrangePi5-zulu17.40.19-ca-jdk17.0.6-linux_aarch64
```java
Benchmark                                                 Mode  Cnt     Score     Error   Units
EishayParseBinary.fastjson2UTF8Bytes                     thrpt    5   718.958 ±  14.141  ops/ms
EishayParseBinary.hessian                                thrpt    5   153.025 ±   6.317  ops/ms
EishayParseBinary.javaSerialize                          thrpt    5    29.310 ±   1.337  ops/ms
EishayParseBinary.jsonb                                  thrpt    5  1413.125 ±  77.263  ops/ms
EishayParseBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   849.956 ±  50.031  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  1009.692 ±  46.093  ops/ms
EishayParseBinaryArrayMapping.jsonb                      thrpt    5  2217.509 ± 105.063  ops/ms
EishayParseBinaryArrayMapping.kryo                       thrpt    5   990.980 ±  47.482  ops/ms
EishayParseBinaryArrayMapping.protobuf                   thrpt    5   896.574 ±  41.865  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB                 thrpt    5   827.893 ±  30.747  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB_autoTypeFilter  thrpt    5   813.115 ±  46.085  ops/ms
EishayParseBinaryAutoType.hessian                        thrpt    5   152.087 ±   7.762  ops/ms
EishayParseBinaryAutoType.javaSerialize                  thrpt    5    28.963 ±   1.056  ops/ms
EishayParseString.fastjson1                              thrpt    5   788.708 ±   9.655  ops/ms
EishayParseString.fastjson2                              thrpt    5   688.794 ±  23.634  ops/ms
EishayParseString.gson                                   thrpt    5   217.238 ±   6.365  ops/ms
EishayParseString.jackson                                thrpt    5   270.022 ±   7.255  ops/ms
EishayParseStringPretty.fastjson1                        thrpt    5   182.593 ±  11.391  ops/ms
EishayParseStringPretty.fastjson2                        thrpt    5   513.593 ±  24.928  ops/ms
EishayParseStringPretty.gson                             thrpt    5   208.953 ±   8.774  ops/ms
EishayParseStringPretty.jackson                          thrpt    5   259.006 ±   9.950  ops/ms
EishayParseTreeString.fastjson1                          thrpt    5   273.274 ±   3.943  ops/ms
EishayParseTreeString.fastjson2                          thrpt    5   479.380 ±  25.315  ops/ms
EishayParseTreeString.gson                               thrpt    5   179.357 ±  11.460  ops/ms
EishayParseTreeString.jackson                            thrpt    5   264.857 ±   6.662  ops/ms
EishayParseTreeStringPretty.fastjson1                    thrpt    5   237.541 ±  11.612  ops/ms
EishayParseTreeStringPretty.fastjson2                    thrpt    5   388.508 ±  26.974  ops/ms
EishayParseTreeStringPretty.gson                         thrpt    5   170.022 ±   4.913  ops/ms
EishayParseTreeStringPretty.jackson                      thrpt    5   246.909 ±  14.399  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                       thrpt    5   232.437 ±  10.654  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                       thrpt    5   471.285 ±  16.839  ops/ms
EishayParseTreeUTF8Bytes.gson                            thrpt    5   167.943 ±   2.688  ops/ms
EishayParseTreeUTF8Bytes.jackson                         thrpt    5   309.404 ±  21.547  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1                 thrpt    5   198.055 ±   9.044  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2                 thrpt    5   381.711 ±  17.962  ops/ms
EishayParseTreeUTF8BytesPretty.gson                      thrpt    5   156.940 ±   6.103  ops/ms
EishayParseTreeUTF8BytesPretty.jackson                   thrpt    5   290.654 ±  20.348  ops/ms
EishayParseUTF8Bytes.dsljson                             thrpt    5   425.937 ±  13.511  ops/ms
EishayParseUTF8Bytes.fastjson1                           thrpt    5   562.892 ±  23.684  ops/ms
EishayParseUTF8Bytes.fastjson2                           thrpt    5   693.115 ±  38.206  ops/ms
EishayParseUTF8Bytes.gson                                thrpt    5   166.688 ±   6.509  ops/ms
EishayParseUTF8Bytes.jackson                             thrpt    5   302.218 ±  16.485  ops/ms
EishayParseUTF8BytesPretty.fastjson1                     thrpt    5   162.592 ±   7.753  ops/ms
EishayParseUTF8BytesPretty.fastjson2                     thrpt    5   510.675 ±  35.229  ops/ms
EishayParseUTF8BytesPretty.gson                          thrpt    5   155.505 ±   4.471  ops/ms
EishayParseUTF8BytesPretty.jackson                       thrpt    5   286.007 ±  14.762  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes                     thrpt    5   886.468 ±  49.720  ops/ms
EishayWriteBinary.hessian                                thrpt    5   201.223 ±  30.523  ops/ms
EishayWriteBinary.javaSerialize                          thrpt    5   130.652 ±   3.386  ops/ms
EishayWriteBinary.jsonb                                  thrpt    5  1401.824 ±  25.645  ops/ms
EishayWriteBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   329.639 ±  12.749  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  1050.138 ±  25.303  ops/ms
EishayWriteBinaryArrayMapping.jsonb                      thrpt    5  2387.749 ±  91.387  ops/ms
EishayWriteBinaryArrayMapping.kryo                       thrpt    5   960.385 ±  35.214  ops/ms
EishayWriteBinaryArrayMapping.protobuf                   thrpt    5   987.650 ±  16.049  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB                 thrpt    5   784.456 ±  15.623  ops/ms
EishayWriteBinaryAutoType.fastjson2UTF8Bytes             thrpt    5   828.710 ±  42.482  ops/ms
EishayWriteBinaryAutoType.hessian                        thrpt    5   201.440 ±  32.691  ops/ms
EishayWriteBinaryAutoType.javaSerialize                  thrpt    5   130.306 ±   5.372  ops/ms
EishayWriteString.fastjson1                              thrpt    5   304.935 ±  12.252  ops/ms
EishayWriteString.fastjson2                              thrpt    5   811.686 ±  14.140  ops/ms
EishayWriteString.gson                                   thrpt    5   163.162 ±   5.246  ops/ms
EishayWriteString.jackson                                thrpt    5   437.016 ±  13.963  ops/ms
EishayWriteStringTree.fastjson1                          thrpt    5   361.706 ±  21.891  ops/ms
EishayWriteStringTree.fastjson2                          thrpt    5   592.180 ±  21.213  ops/ms
EishayWriteStringTree.gson                               thrpt    5   162.591 ±   5.982  ops/ms
EishayWriteStringTree.jackson                            thrpt    5   407.132 ±  19.726  ops/ms
EishayWriteStringTree1x.fastjson1                        thrpt    5   320.513 ±   9.815  ops/ms
EishayWriteStringTree1x.fastjson2                        thrpt    5   437.111 ±  14.571  ops/ms
EishayWriteStringTree1x.gson                             thrpt    5   161.053 ±   7.396  ops/ms
EishayWriteStringTree1x.jackson                          thrpt    5   382.480 ±  18.860  ops/ms
EishayWriteUTF8Bytes.fastjson1                           thrpt    5   292.753 ±   7.857  ops/ms
EishayWriteUTF8Bytes.fastjson2                           thrpt    5   885.081 ±  23.900  ops/ms
EishayWriteUTF8Bytes.gson                                thrpt    5   156.470 ±   7.493  ops/ms
EishayWriteUTF8Bytes.jackson                             thrpt    5   407.995 ±  11.180  ops/ms
```
# OrangePi5-graalvm-ce-java11-22.3.1
```java
Benchmark                                                 Mode  Cnt     Score    Error   Units
EishayParseBinary.fastjson2UTF8Bytes                     thrpt    5   517.702 ±  9.500  ops/ms
EishayParseBinary.hessian                                thrpt    5   109.046 ±  3.625  ops/ms
EishayParseBinary.javaSerialize                          thrpt    5    25.440 ±  0.600  ops/ms
EishayParseBinary.jsonb                                  thrpt    5  1410.870 ± 54.810  ops/ms
EishayParseBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   678.126 ± 27.366  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5   818.478 ± 58.920  ops/ms
EishayParseBinaryArrayMapping.jsonb                      thrpt    5  2075.961 ± 55.770  ops/ms
EishayParseBinaryArrayMapping.kryo                       thrpt    5   859.514 ±  4.807  ops/ms
EishayParseBinaryArrayMapping.protobuf                   thrpt    5   791.394 ± 30.361  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB                 thrpt    5   935.251 ± 49.679  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB_autoTypeFilter  thrpt    5   836.876 ± 30.765  ops/ms
EishayParseBinaryAutoType.hessian                        thrpt    5   107.154 ±  2.939  ops/ms
EishayParseBinaryAutoType.javaSerialize                  thrpt    5    25.744 ±  0.104  ops/ms
EishayParseString.fastjson1                              thrpt    5   535.275 ± 34.809  ops/ms
EishayParseString.fastjson2                              thrpt    5   621.946 ± 22.505  ops/ms
EishayParseString.gson                                   thrpt    5   219.927 ± 10.257  ops/ms
EishayParseString.jackson                                thrpt    5   257.991 ± 18.302  ops/ms
EishayParseStringPretty.fastjson1                        thrpt    5   151.203 ± 10.342  ops/ms
EishayParseStringPretty.fastjson2                        thrpt    5   490.970 ± 23.651  ops/ms
EishayParseStringPretty.gson                             thrpt    5   193.382 ± 10.021  ops/ms
EishayParseStringPretty.jackson                          thrpt    5   228.324 ±  9.399  ops/ms
EishayParseTreeString.fastjson1                          thrpt    5   255.212 ±  6.014  ops/ms
EishayParseTreeString.fastjson2                          thrpt    5   454.049 ±  7.794  ops/ms
EishayParseTreeString.gson                               thrpt    5   204.332 ±  6.028  ops/ms
EishayParseTreeString.jackson                            thrpt    5   284.527 ±  7.481  ops/ms
EishayParseTreeStringPretty.fastjson1                    thrpt    5   210.977 ± 13.754  ops/ms
EishayParseTreeStringPretty.fastjson2                    thrpt    5   391.237 ± 11.559  ops/ms
EishayParseTreeStringPretty.gson                         thrpt    5   187.155 ±  5.476  ops/ms
EishayParseTreeStringPretty.jackson                      thrpt    5   256.416 ± 10.025  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                       thrpt    5   206.632 ±  6.724  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                       thrpt    5   381.444 ± 15.057  ops/ms
EishayParseTreeUTF8Bytes.gson                            thrpt    5   175.825 ±  3.478  ops/ms
EishayParseTreeUTF8Bytes.jackson                         thrpt    5   278.516 ±  5.537  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1                 thrpt    5   166.740 ±  3.909  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2                 thrpt    5   316.014 ±  7.590  ops/ms
EishayParseTreeUTF8BytesPretty.gson                      thrpt    5   159.999 ±  5.105  ops/ms
EishayParseTreeUTF8BytesPretty.jackson                   thrpt    5   254.778 ±  8.321  ops/ms
EishayParseUTF8Bytes.dsljson                             thrpt    5   416.518 ± 16.756  ops/ms
EishayParseUTF8Bytes.fastjson1                           thrpt    5   385.399 ± 13.932  ops/ms
EishayParseUTF8Bytes.fastjson2                           thrpt    5   505.568 ±  5.715  ops/ms
EishayParseUTF8Bytes.gson                                thrpt    5   175.120 ±  2.630  ops/ms
EishayParseUTF8Bytes.jackson                             thrpt    5   284.783 ±  4.364  ops/ms
EishayParseUTF8BytesPretty.fastjson1                     thrpt    5   129.156 ±  5.745  ops/ms
EishayParseUTF8BytesPretty.fastjson2                     thrpt    5   381.262 ±  1.524  ops/ms
EishayParseUTF8BytesPretty.gson                          thrpt    5   161.516 ±  6.222  ops/ms
EishayParseUTF8BytesPretty.jackson                       thrpt    5   261.067 ± 11.361  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes                     thrpt    5   954.802 ± 27.779  ops/ms
EishayWriteBinary.hessian                                thrpt    5   117.771 ±  4.512  ops/ms
EishayWriteBinary.javaSerialize                          thrpt    5   108.534 ±  4.890  ops/ms
EishayWriteBinary.jsonb                                  thrpt    5  1715.295 ± 80.540  ops/ms
EishayWriteBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   325.951 ± 19.754  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  1047.937 ± 68.288  ops/ms
EishayWriteBinaryArrayMapping.jsonb                      thrpt    5  2129.584 ± 98.209  ops/ms
EishayWriteBinaryArrayMapping.kryo                       thrpt    5   970.124 ±  3.114  ops/ms
EishayWriteBinaryArrayMapping.protobuf                   thrpt    5   856.980 ± 42.120  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB                 thrpt    5   790.210 ± 44.318  ops/ms
EishayWriteBinaryAutoType.fastjson2UTF8Bytes             thrpt    5   900.605 ± 41.710  ops/ms
EishayWriteBinaryAutoType.hessian                        thrpt    5   116.531 ±  5.714  ops/ms
EishayWriteBinaryAutoType.javaSerialize                  thrpt    5   108.888 ±  5.392  ops/ms
EishayWriteString.fastjson1                              thrpt    5   327.358 ±  2.029  ops/ms
EishayWriteString.fastjson2                              thrpt    5   920.314 ± 32.661  ops/ms
EishayWriteString.gson                                   thrpt    5   187.495 ± 11.936  ops/ms
EishayWriteString.jackson                                thrpt    5   422.450 ± 18.575  ops/ms
EishayWriteStringTree.fastjson1                          thrpt    5   247.350 ±  9.122  ops/ms
EishayWriteStringTree.fastjson2                          thrpt    5   682.792 ± 28.752  ops/ms
EishayWriteStringTree.gson                               thrpt    5   182.470 ± 15.785  ops/ms
EishayWriteStringTree.jackson                            thrpt    5   444.630 ± 22.045  ops/ms
EishayWriteStringTree1x.fastjson1                        thrpt    5   206.719 ±  7.663  ops/ms
EishayWriteStringTree1x.fastjson2                        thrpt    5   550.201 ± 23.860  ops/ms
EishayWriteStringTree1x.gson                             thrpt    5   177.122 ±  5.165  ops/ms
EishayWriteStringTree1x.jackson                          thrpt    5   392.559 ± 16.681  ops/ms
EishayWriteUTF8Bytes.fastjson1                           thrpt    5   264.922 ±  5.783  ops/ms
EishayWriteUTF8Bytes.fastjson2                           thrpt    5   936.190 ± 46.869  ops/ms
EishayWriteUTF8Bytes.gson                                thrpt    5   172.734 ±  4.104  ops/ms
EishayWriteUTF8Bytes.jackson                             thrpt    5   379.341 ± 19.913  ops/ms
```
# OrangePi5-graalvm-ce-java17-22.3.1
```java
Benchmark                                                 Mode  Cnt     Score    Error   Units
EishayParseBinary.fastjson2UTF8Bytes                     thrpt    5   541.512 ± 15.503  ops/ms
EishayParseBinary.hessian                                thrpt    5   121.925 ±  2.298  ops/ms
EishayParseBinary.javaSerialize                          thrpt    5    27.547 ±  0.961  ops/ms
EishayParseBinary.jsonb                                  thrpt    5  1703.355 ± 64.666  ops/ms
EishayParseBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   725.301 ± 10.963  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5   887.836 ± 27.505  ops/ms
EishayParseBinaryArrayMapping.jsonb                      thrpt    5  2596.112 ± 65.516  ops/ms
EishayParseBinaryArrayMapping.kryo                       thrpt    5   875.878 ± 23.331  ops/ms
EishayParseBinaryArrayMapping.protobuf                   thrpt    5   943.215 ± 55.659  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB                 thrpt    5   958.993 ± 55.670  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB_autoTypeFilter  thrpt    5   875.478 ± 50.901  ops/ms
EishayParseBinaryAutoType.hessian                        thrpt    5   116.509 ±  3.793  ops/ms
EishayParseBinaryAutoType.javaSerialize                  thrpt    5    27.530 ±  1.197  ops/ms
EishayParseString.fastjson1                              thrpt    5   702.901 ± 25.717  ops/ms
EishayParseString.fastjson2                              thrpt    5   690.001 ±  4.194  ops/ms
EishayParseString.gson                                   thrpt    5   230.104 ±  8.941  ops/ms
EishayParseString.jackson                                thrpt    5   269.076 ± 12.448  ops/ms
EishayParseStringPretty.fastjson1                        thrpt    5   193.901 ±  5.478  ops/ms
EishayParseStringPretty.fastjson2                        thrpt    5   510.441 ± 27.510  ops/ms
EishayParseStringPretty.gson                             thrpt    5   207.978 ±  8.541  ops/ms
EishayParseStringPretty.jackson                          thrpt    5   246.177 ±  6.341  ops/ms
EishayParseTreeString.fastjson1                          thrpt    5   333.041 ±  9.533  ops/ms
EishayParseTreeString.fastjson2                          thrpt    5   456.345 ± 17.669  ops/ms
EishayParseTreeString.gson                               thrpt    5   203.192 ± 13.669  ops/ms
EishayParseTreeString.jackson                            thrpt    5   272.823 ±  8.429  ops/ms
EishayParseTreeStringPretty.fastjson1                    thrpt    5   272.525 ± 10.655  ops/ms
EishayParseTreeStringPretty.fastjson2                    thrpt    5   387.336 ± 15.540  ops/ms
EishayParseTreeStringPretty.gson                         thrpt    5   187.184 ± 10.948  ops/ms
EishayParseTreeStringPretty.jackson                      thrpt    5   267.123 ±  4.920  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                       thrpt    5   258.205 ±  5.932  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                       thrpt    5   382.160 ± 20.323  ops/ms
EishayParseTreeUTF8Bytes.gson                            thrpt    5   174.882 ±  7.070  ops/ms
EishayParseTreeUTF8Bytes.jackson                         thrpt    5   298.015 ± 13.829  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1                 thrpt    5   204.696 ± 13.126  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2                 thrpt    5   322.160 ± 16.446  ops/ms
EishayParseTreeUTF8BytesPretty.gson                      thrpt    5   159.489 ±  6.693  ops/ms
EishayParseTreeUTF8BytesPretty.jackson                   thrpt    5   271.663 ± 11.800  ops/ms
EishayParseUTF8Bytes.dsljson                             thrpt    5   306.045 ± 13.407  ops/ms
EishayParseUTF8Bytes.fastjson1                           thrpt    5   473.852 ± 14.675  ops/ms
EishayParseUTF8Bytes.fastjson2                           thrpt    5   523.945 ± 22.481  ops/ms
EishayParseUTF8Bytes.gson                                thrpt    5   175.226 ± 10.751  ops/ms
EishayParseUTF8Bytes.jackson                             thrpt    5   292.891 ±  5.190  ops/ms
EishayParseUTF8BytesPretty.fastjson1                     thrpt    5   160.318 ±  8.706  ops/ms
EishayParseUTF8BytesPretty.fastjson2                     thrpt    5   395.716 ±  6.817  ops/ms
EishayParseUTF8BytesPretty.gson                          thrpt    5   161.605 ±  6.247  ops/ms
EishayParseUTF8BytesPretty.jackson                       thrpt    5   259.841 ±  8.643  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes                     thrpt    5   915.111 ± 23.158  ops/ms
EishayWriteBinary.hessian                                thrpt    5   126.934 ±  2.353  ops/ms
EishayWriteBinary.javaSerialize                          thrpt    5   105.284 ±  8.222  ops/ms
EishayWriteBinary.jsonb                                  thrpt    5  1557.455 ± 34.869  ops/ms
EishayWriteBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   351.435 ±  3.003  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  1151.544 ± 36.165  ops/ms
EishayWriteBinaryArrayMapping.jsonb                      thrpt    5  2591.623 ± 86.349  ops/ms
EishayWriteBinaryArrayMapping.kryo                       thrpt    5   989.925 ±  8.684  ops/ms
EishayWriteBinaryArrayMapping.protobuf                   thrpt    5   915.389 ± 26.869  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB                 thrpt    5   789.804 ± 24.549  ops/ms
EishayWriteBinaryAutoType.fastjson2UTF8Bytes             thrpt    5   915.592 ± 20.063  ops/ms
EishayWriteBinaryAutoType.hessian                        thrpt    5   172.374 ±  9.732  ops/ms
EishayWriteBinaryAutoType.javaSerialize                  thrpt    5   106.154 ±  4.761  ops/ms
EishayWriteString.fastjson1                              thrpt    5   379.729 ±  9.147  ops/ms
EishayWriteString.fastjson2                              thrpt    5   958.825 ± 20.967  ops/ms
EishayWriteString.gson                                   thrpt    5   151.379 ±  3.913  ops/ms
EishayWriteString.jackson                                thrpt    5   434.073 ± 19.955  ops/ms
EishayWriteStringTree.fastjson1                          thrpt    5   361.329 ± 17.690  ops/ms
EishayWriteStringTree.fastjson2                          thrpt    5   686.447 ± 37.518  ops/ms
EishayWriteStringTree.gson                               thrpt    5   159.370 ±  8.022  ops/ms
EishayWriteStringTree.jackson                            thrpt    5   454.828 ± 21.937  ops/ms
EishayWriteStringTree1x.fastjson1                        thrpt    5   250.495 ±  7.601  ops/ms
EishayWriteStringTree1x.fastjson2                        thrpt    5   542.411 ± 22.271  ops/ms
EishayWriteStringTree1x.gson                             thrpt    5   147.270 ±  0.274  ops/ms
EishayWriteStringTree1x.jackson                          thrpt    5   400.072 ± 16.585  ops/ms
EishayWriteUTF8Bytes.fastjson1                           thrpt    5   289.807 ±  4.269  ops/ms
EishayWriteUTF8Bytes.fastjson2                           thrpt    5   954.629 ± 37.707  ops/ms
EishayWriteUTF8Bytes.gson                                thrpt    5   138.160 ±  4.644  ops/ms
EishayWriteUTF8Bytes.jackson                             thrpt    5   435.435 ±  9.452  ops/ms
```
# OrangePi5-graalvm-ee-java11-22.3.1
```java
Benchmark                                                 Mode  Cnt     Score     Error   Units
EishayParseBinary.fastjson2UTF8Bytes                     thrpt    5   749.668 ±  20.840  ops/ms
EishayParseBinary.hessian                                thrpt    5   189.312 ±   9.447  ops/ms
EishayParseBinary.javaSerialize                          thrpt    5    26.936 ±   0.365  ops/ms
EishayParseBinary.jsonb                                  thrpt    5  1987.747 ±  41.442  ops/ms
EishayParseBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   695.372 ±  38.839  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  1159.320 ±  46.174  ops/ms
EishayParseBinaryArrayMapping.jsonb                      thrpt    5  3292.282 ± 159.810  ops/ms
EishayParseBinaryArrayMapping.kryo                       thrpt    5   896.785 ±  35.024  ops/ms
EishayParseBinaryArrayMapping.protobuf                   thrpt    5  1240.666 ±  87.995  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB                 thrpt    5  1086.664 ±  20.268  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB_autoTypeFilter  thrpt    5   949.138 ±  50.887  ops/ms
EishayParseBinaryAutoType.hessian                        thrpt    5   173.099 ±   6.358  ops/ms
EishayParseBinaryAutoType.javaSerialize                  thrpt    5    27.422 ±   0.148  ops/ms
EishayParseString.fastjson1                              thrpt    5   597.188 ±  29.496  ops/ms
EishayParseString.fastjson2                              thrpt    5   767.159 ±  35.566  ops/ms
EishayParseString.gson                                   thrpt    5   225.025 ±   3.095  ops/ms
EishayParseString.jackson                                thrpt    5   267.566 ±  13.304  ops/ms
EishayParseStringPretty.fastjson1                        thrpt    5   169.333 ±   5.818  ops/ms
EishayParseStringPretty.fastjson2                        thrpt    5   579.768 ±  20.070  ops/ms
EishayParseStringPretty.gson                             thrpt    5   205.932 ±   4.869  ops/ms
EishayParseStringPretty.jackson                          thrpt    5   234.417 ±   1.719  ops/ms
EishayParseTreeString.fastjson1                          thrpt    5   319.547 ±   7.037  ops/ms
EishayParseTreeString.fastjson2                          thrpt    5   520.047 ±  24.159  ops/ms
EishayParseTreeString.gson                               thrpt    5   204.165 ±   4.792  ops/ms
EishayParseTreeString.jackson                            thrpt    5   301.549 ±  12.469  ops/ms
EishayParseTreeStringPretty.fastjson1                    thrpt    5   248.333 ±  15.234  ops/ms
EishayParseTreeStringPretty.fastjson2                    thrpt    5   450.416 ±  14.466  ops/ms
EishayParseTreeStringPretty.gson                         thrpt    5   190.067 ±  16.527  ops/ms
EishayParseTreeStringPretty.jackson                      thrpt    5   251.119 ±  11.639  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                       thrpt    5   241.941 ±   8.762  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                       thrpt    5   491.499 ±  17.602  ops/ms
EishayParseTreeUTF8Bytes.gson                            thrpt    5   183.893 ±   4.448  ops/ms
EishayParseTreeUTF8Bytes.jackson                         thrpt    5   300.356 ±  10.202  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1                 thrpt    5   203.062 ±   6.836  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2                 thrpt    5   420.733 ±  19.754  ops/ms
EishayParseTreeUTF8BytesPretty.gson                      thrpt    5   178.151 ±   6.387  ops/ms
EishayParseTreeUTF8BytesPretty.jackson                   thrpt    5   272.981 ±  11.858  ops/ms
EishayParseUTF8Bytes.dsljson                             thrpt    5   466.614 ±  20.678  ops/ms
EishayParseUTF8Bytes.fastjson1                           thrpt    5   397.683 ±   1.396  ops/ms
EishayParseUTF8Bytes.fastjson2                           thrpt    5   684.974 ±  23.907  ops/ms
EishayParseUTF8Bytes.gson                                thrpt    5   187.995 ±   5.561  ops/ms
EishayParseUTF8Bytes.jackson                             thrpt    5   280.185 ±   2.730  ops/ms
EishayParseUTF8BytesPretty.fastjson1                     thrpt    5   141.256 ±   4.180  ops/ms
EishayParseUTF8BytesPretty.fastjson2                     thrpt    5   520.983 ±  34.002  ops/ms
EishayParseUTF8BytesPretty.gson                          thrpt    5   181.238 ±   8.010  ops/ms
EishayParseUTF8BytesPretty.jackson                       thrpt    5   251.038 ±   8.820  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes                     thrpt    5  1062.526 ±  21.567  ops/ms
EishayWriteBinary.hessian                                thrpt    5   221.643 ±  21.208  ops/ms
EishayWriteBinary.javaSerialize                          thrpt    5   125.994 ±   2.629  ops/ms
EishayWriteBinary.jsonb                                  thrpt    5  1785.221 ±  91.303  ops/ms
EishayWriteBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   313.854 ±  18.887  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  1208.660 ±  13.300  ops/ms
EishayWriteBinaryArrayMapping.jsonb                      thrpt    5  2996.447 ±  69.268  ops/ms
EishayWriteBinaryArrayMapping.kryo                       thrpt    5  1195.232 ±  13.380  ops/ms
EishayWriteBinaryArrayMapping.protobuf                   thrpt    5  1572.086 ±  41.605  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB                 thrpt    5   872.883 ±  27.488  ops/ms
EishayWriteBinaryAutoType.fastjson2UTF8Bytes             thrpt    5  1036.205 ±  26.353  ops/ms
EishayWriteBinaryAutoType.hessian                        thrpt    5   215.001 ±  28.856  ops/ms
EishayWriteBinaryAutoType.javaSerialize                  thrpt    5   126.411 ±   4.715  ops/ms
EishayWriteString.fastjson1                              thrpt    5   404.555 ±  17.501  ops/ms
EishayWriteString.fastjson2                              thrpt    5  1038.676 ±  24.308  ops/ms
EishayWriteString.gson                                   thrpt    5   210.285 ±   6.059  ops/ms
EishayWriteString.jackson                                thrpt    5   463.718 ±   6.142  ops/ms
EishayWriteStringTree.fastjson1                          thrpt    5   397.328 ±   8.424  ops/ms
EishayWriteStringTree.fastjson2                          thrpt    5   691.988 ±  29.535  ops/ms
EishayWriteStringTree.gson                               thrpt    5   251.910 ±   8.097  ops/ms
EishayWriteStringTree.jackson                            thrpt    5   499.257 ±  20.078  ops/ms
EishayWriteStringTree1x.fastjson1                        thrpt    5   400.109 ±  13.623  ops/ms
EishayWriteStringTree1x.fastjson2                        thrpt    5   638.556 ±  22.364  ops/ms
EishayWriteStringTree1x.gson                             thrpt    5   224.481 ±  18.334  ops/ms
EishayWriteStringTree1x.jackson                          thrpt    5   478.440 ±   5.334  ops/ms
EishayWriteUTF8Bytes.fastjson1                           thrpt    5   316.120 ±   2.925  ops/ms
EishayWriteUTF8Bytes.fastjson2                           thrpt    5  1038.522 ±  28.873  ops/ms
EishayWriteUTF8Bytes.gson                                thrpt    5   204.492 ±   1.523  ops/ms
EishayWriteUTF8Bytes.jackson                             thrpt    5   472.897 ±  17.367  ops/ms
```
# OrangePi5-graalvm-ee-java17-22.3.1
```java
Benchmark                                                 Mode  Cnt     Score     Error   Units
EishayParseBinary.fastjson2UTF8Bytes                     thrpt    5   764.668 ±  31.448  ops/ms
EishayParseBinary.hessian                                thrpt    5   203.559 ±   4.682  ops/ms
EishayParseBinary.javaSerialize                          thrpt    5    29.550 ±   0.687  ops/ms
EishayParseBinary.jsonb                                  thrpt    5  2060.330 ±  92.645  ops/ms
EishayParseBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   683.782 ±  22.422  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  1265.474 ±  48.091  ops/ms
EishayParseBinaryArrayMapping.jsonb                      thrpt    5  3789.462 ±  65.087  ops/ms
EishayParseBinaryArrayMapping.kryo                       thrpt    5   904.255 ±  49.088  ops/ms
EishayParseBinaryArrayMapping.protobuf                   thrpt    5  1507.697 ±  95.162  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB                 thrpt    5  1155.268 ±  33.825  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB_autoTypeFilter  thrpt    5   995.049 ±  47.707  ops/ms
EishayParseBinaryAutoType.hessian                        thrpt    5   199.939 ±   7.804  ops/ms
EishayParseBinaryAutoType.javaSerialize                  thrpt    5    29.354 ±   0.480  ops/ms
EishayParseString.fastjson1                              thrpt    5   905.424 ±  31.320  ops/ms
EishayParseString.fastjson2                              thrpt    5   780.188 ±  34.376  ops/ms
EishayParseString.gson                                   thrpt    5   242.473 ±   7.045  ops/ms
EishayParseString.jackson                                thrpt    5   289.851 ±   9.491  ops/ms
EishayParseStringPretty.fastjson1                        thrpt    5   218.000 ±   7.460  ops/ms
EishayParseStringPretty.fastjson2                        thrpt    5   582.617 ±  20.475  ops/ms
EishayParseStringPretty.gson                             thrpt    5   215.832 ±   4.699  ops/ms
EishayParseStringPretty.jackson                          thrpt    5   256.522 ±   5.553  ops/ms
EishayParseTreeString.fastjson1                          thrpt    5   378.358 ±  11.460  ops/ms
EishayParseTreeString.fastjson2                          thrpt    5   535.013 ±   6.028  ops/ms
EishayParseTreeString.gson                               thrpt    5   196.184 ±   4.876  ops/ms
EishayParseTreeString.jackson                            thrpt    5   297.628 ±   5.830  ops/ms
EishayParseTreeStringPretty.fastjson1                    thrpt    5   304.704 ±  14.482  ops/ms
EishayParseTreeStringPretty.fastjson2                    thrpt    5   463.978 ±   2.291  ops/ms
EishayParseTreeStringPretty.gson                         thrpt    5   192.435 ±  12.805  ops/ms
EishayParseTreeStringPretty.jackson                      thrpt    5   265.462 ±  10.618  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                       thrpt    5   273.725 ±   9.281  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                       thrpt    5   505.163 ±  10.901  ops/ms
EishayParseTreeUTF8Bytes.gson                            thrpt    5   190.516 ±   7.024  ops/ms
EishayParseTreeUTF8Bytes.jackson                         thrpt    5   308.618 ±   9.630  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1                 thrpt    5   222.400 ±   6.403  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2                 thrpt    5   429.628 ±  21.631  ops/ms
EishayParseTreeUTF8BytesPretty.gson                      thrpt    5   178.517 ±  46.458  ops/ms
EishayParseTreeUTF8BytesPretty.jackson                   thrpt    5   273.443 ±  11.957  ops/ms
EishayParseUTF8Bytes.dsljson                             thrpt    5   441.002 ±  13.542  ops/ms
EishayParseUTF8Bytes.fastjson1                           thrpt    5   497.913 ±   9.935  ops/ms
EishayParseUTF8Bytes.fastjson2                           thrpt    5   690.487 ±   5.219  ops/ms
EishayParseUTF8Bytes.gson                                thrpt    5   191.671 ±   8.176  ops/ms
EishayParseUTF8Bytes.jackson                             thrpt    5   291.025 ±  16.406  ops/ms
EishayParseUTF8BytesPretty.fastjson1                     thrpt    5   169.654 ±   7.153  ops/ms
EishayParseUTF8BytesPretty.fastjson2                     thrpt    5   523.180 ±  32.999  ops/ms
EishayParseUTF8BytesPretty.gson                          thrpt    5   181.058 ±  19.444  ops/ms
EishayParseUTF8BytesPretty.jackson                       thrpt    5   263.668 ±  12.253  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes                     thrpt    5  1076.261 ±  20.514  ops/ms
EishayWriteBinary.hessian                                thrpt    5   217.037 ±  45.869  ops/ms
EishayWriteBinary.javaSerialize                          thrpt    5   120.311 ±   5.170  ops/ms
EishayWriteBinary.jsonb                                  thrpt    5  1865.095 ±  60.264  ops/ms
EishayWriteBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   420.954 ±   9.774  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  1233.366 ±  56.243  ops/ms
EishayWriteBinaryArrayMapping.jsonb                      thrpt    5  3583.531 ± 142.306  ops/ms
EishayWriteBinaryArrayMapping.kryo                       thrpt    5  1222.469 ±  33.582  ops/ms
EishayWriteBinaryArrayMapping.protobuf                   thrpt    5  1662.526 ±  41.525  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB                 thrpt    5   896.160 ±  31.268  ops/ms
EishayWriteBinaryAutoType.fastjson2UTF8Bytes             thrpt    5   981.810 ±  47.491  ops/ms
EishayWriteBinaryAutoType.hessian                        thrpt    5   227.102 ±  57.663  ops/ms
EishayWriteBinaryAutoType.javaSerialize                  thrpt    5   120.828 ±   5.394  ops/ms
EishayWriteString.fastjson1                              thrpt    5   386.018 ±  22.733  ops/ms
EishayWriteString.fastjson2                              thrpt    5  1041.491 ±  32.520  ops/ms
EishayWriteString.gson                                   thrpt    5   159.146 ±   5.537  ops/ms
EishayWriteString.jackson                                thrpt    5   490.855 ±  13.048  ops/ms
EishayWriteStringTree.fastjson1                          thrpt    5   399.643 ±  17.038  ops/ms
EishayWriteStringTree.fastjson2                          thrpt    5   711.978 ±  35.690  ops/ms
EishayWriteStringTree.gson                               thrpt    5   173.161 ±   5.905  ops/ms
EishayWriteStringTree.jackson                            thrpt    5   500.340 ±  11.199  ops/ms
EishayWriteStringTree1x.fastjson1                        thrpt    5   419.705 ±  18.602  ops/ms
EishayWriteStringTree1x.fastjson2                        thrpt    5   638.056 ±  27.724  ops/ms
EishayWriteStringTree1x.gson                             thrpt    5   172.167 ±   2.943  ops/ms
EishayWriteStringTree1x.jackson                          thrpt    5   477.034 ±   5.236  ops/ms
EishayWriteUTF8Bytes.fastjson1                           thrpt    5   405.948 ±   8.319  ops/ms
EishayWriteUTF8Bytes.fastjson2                           thrpt    5  1083.686 ±  25.980  ops/ms
EishayWriteUTF8Bytes.gson                                thrpt    5   153.107 ±   3.612  ops/ms
EishayWriteUTF8Bytes.jackson                             thrpt    5   521.907 ±  35.530  ops/ms
```

# Apple_M1_Pro-zulu-8.jdk
```java
Benchmark                                                 Mode  Cnt     Score     Error   Units
EishayParseBinary.fastjson2UTF8Bytes                     thrpt    5  1404.751 ±  19.333  ops/ms
EishayParseBinary.hessian                                thrpt    5   378.512 ±  12.091  ops/ms
EishayParseBinary.javaSerialize                          thrpt    5    61.093 ±   0.100  ops/ms
EishayParseBinary.jsonb                                  thrpt    5  2838.248 ±  18.010  ops/ms
EishayParseBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5  2470.031 ±   4.769  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  2012.126 ±  14.454  ops/ms
EishayParseBinaryArrayMapping.jsonb                      thrpt    5  5384.407 ±  22.564  ops/ms
EishayParseBinaryArrayMapping.kryo                       thrpt    5  2185.700 ± 224.219  ops/ms
EishayParseBinaryArrayMapping.protobuf                   thrpt    5  1611.936 ±   2.180  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB                 thrpt    5  2557.731 ±   5.658  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB_autoTypeFilter  thrpt    5  2403.324 ±   8.297  ops/ms
EishayParseBinaryAutoType.hessian                        thrpt    5   390.523 ±  10.043  ops/ms
EishayParseBinaryAutoType.javaSerialize                  thrpt    5    61.356 ±   0.886  ops/ms
EishayParseString.fastjson1                              thrpt    5  1505.754 ±   4.243  ops/ms
EishayParseString.fastjson2                              thrpt    5  1811.801 ±  58.961  ops/ms
EishayParseString.gson                                   thrpt    5   664.122 ±  30.900  ops/ms
EishayParseString.jackson                                thrpt    5   743.601 ±  43.674  ops/ms
EishayParseStringPretty.fastjson1                        thrpt    5   441.515 ±  22.834  ops/ms
EishayParseStringPretty.fastjson2                        thrpt    5  1514.755 ±  10.094  ops/ms
EishayParseStringPretty.gson                             thrpt    5   614.839 ±   7.363  ops/ms
EishayParseStringPretty.jackson                          thrpt    5   654.252 ± 144.289  ops/ms
EishayParseTreeString.fastjson1                          thrpt    5   692.370 ±   7.964  ops/ms
EishayParseTreeString.fastjson2                          thrpt    5  1386.813 ±  14.296  ops/ms
EishayParseTreeString.gson                               thrpt    5   552.978 ±  18.225  ops/ms
EishayParseTreeString.jackson                            thrpt    5   710.968 ± 111.256  ops/ms
EishayParseTreeStringPretty.fastjson1                    thrpt    5   647.307 ±  10.425  ops/ms
EishayParseTreeStringPretty.fastjson2                    thrpt    5  1223.781 ±  14.134  ops/ms
EishayParseTreeStringPretty.gson                         thrpt    5   523.040 ±  30.830  ops/ms
EishayParseTreeStringPretty.jackson                      thrpt    5   692.013 ±  70.812  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                       thrpt    5   623.950 ±   7.501  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                       thrpt    5  1365.631 ±  12.755  ops/ms
EishayParseTreeUTF8Bytes.gson                            thrpt    5   591.847 ±  21.534  ops/ms
EishayParseTreeUTF8Bytes.jackson                         thrpt    5   920.004 ±   7.932  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1                 thrpt    5   597.000 ±   7.380  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2                 thrpt    5  1152.502 ±  18.708  ops/ms
EishayParseTreeUTF8BytesPretty.gson                      thrpt    5   508.485 ±  35.818  ops/ms
EishayParseTreeUTF8BytesPretty.jackson                   thrpt    5   821.931 ±   8.984  ops/ms
EishayParseUTF8Bytes.dsljson                             thrpt    5   942.730 ± 272.014  ops/ms
EishayParseUTF8Bytes.fastjson1                           thrpt    5  1470.430 ±  24.972  ops/ms
EishayParseUTF8Bytes.fastjson2                           thrpt    5  1384.495 ±  23.125  ops/ms
EishayParseUTF8Bytes.gson                                thrpt    5   589.278 ±   7.246  ops/ms
EishayParseUTF8Bytes.jackson                             thrpt    5   994.847 ±  30.706  ops/ms
EishayParseUTF8BytesPretty.fastjson1                     thrpt    5   430.931 ±   7.552  ops/ms
EishayParseUTF8BytesPretty.fastjson2                     thrpt    5  1178.844 ±  24.145  ops/ms
EishayParseUTF8BytesPretty.gson                          thrpt    5   517.731 ±   5.294  ops/ms
EishayParseUTF8BytesPretty.jackson                       thrpt    5   922.035 ±   6.772  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes                     thrpt    5  2890.625 ±  18.289  ops/ms
EishayWriteBinary.hessian                                thrpt    5   619.308 ±   7.640  ops/ms
EishayWriteBinary.javaSerialize                          thrpt    5   438.452 ±   3.703  ops/ms
EishayWriteBinary.jsonb                                  thrpt    5  3642.745 ±  32.085  ops/ms
EishayWriteBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5  1151.642 ±  16.691  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  3222.551 ±  14.441  ops/ms
EishayWriteBinaryArrayMapping.jsonb                      thrpt    5  5707.609 ±  17.708  ops/ms
EishayWriteBinaryArrayMapping.kryo                       thrpt    5  2542.029 ±  16.856  ops/ms
EishayWriteBinaryArrayMapping.protobuf                   thrpt    5  2165.202 ±   8.138  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB                 thrpt    5  2553.711 ±   8.839  ops/ms
EishayWriteBinaryAutoType.fastjson2UTF8Bytes             thrpt    5  2821.010 ±   7.733  ops/ms
EishayWriteBinaryAutoType.hessian                        thrpt    5   634.743 ±   8.924  ops/ms
EishayWriteBinaryAutoType.javaSerialize                  thrpt    5   440.294 ±   1.679  ops/ms
EishayWriteString.fastjson1                              thrpt    5  1169.394 ±   2.142  ops/ms
EishayWriteString.fastjson2                              thrpt    5  2960.485 ±  10.652  ops/ms
EishayWriteString.gson                                   thrpt    5   731.245 ±   9.467  ops/ms
EishayWriteString.jackson                                thrpt    5  1735.869 ±  14.597  ops/ms
EishayWriteStringTree.fastjson1                          thrpt    5  1299.506 ±   7.100  ops/ms
EishayWriteStringTree.fastjson2                          thrpt    5  1898.534 ±  12.806  ops/ms
EishayWriteStringTree.gson                               thrpt    5   847.867 ±   1.977  ops/ms
EishayWriteStringTree.jackson                            thrpt    5  1598.247 ±   6.829  ops/ms
EishayWriteStringTree1x.fastjson1                        thrpt    5  1237.651 ±   5.577  ops/ms
EishayWriteStringTree1x.fastjson2                        thrpt    5  1722.057 ±  17.131  ops/ms
EishayWriteStringTree1x.gson                             thrpt    5   807.740 ±   0.919  ops/ms
EishayWriteStringTree1x.jackson                          thrpt    5  1496.310 ±   4.423  ops/ms
EishayWriteUTF8Bytes.fastjson1                           thrpt    5  1023.527 ±  11.656  ops/ms
EishayWriteUTF8Bytes.fastjson2                           thrpt    5  2892.846 ±  35.767  ops/ms
EishayWriteUTF8Bytes.gson                                thrpt    5   650.016 ±   5.948  ops/ms
EishayWriteUTF8Bytes.jackson                             thrpt    5  1589.841 ±  16.030  ops/ms
```
# Apple_M1_Pro-zulu-17.jdk
```java
Benchmark                                                 Mode  Cnt     Score     Error   Units
EishayParseBinary.fastjson2UTF8Bytes                     thrpt    5  1655.164 ±  10.461  ops/ms
EishayParseBinary.hessian                                thrpt    5   339.994 ±   6.409  ops/ms
EishayParseBinary.javaSerialize                          thrpt    5    68.932 ±   0.965  ops/ms
EishayParseBinary.jsonb                                  thrpt    5  3344.688 ±  86.437  ops/ms
EishayParseBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5  2561.929 ±  15.425  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  2574.562 ±  71.309  ops/ms
EishayParseBinaryArrayMapping.jsonb                      thrpt    5  6595.451 ±  10.042  ops/ms
EishayParseBinaryArrayMapping.kryo                       thrpt    5  1760.158 ±  61.448  ops/ms
EishayParseBinaryArrayMapping.protobuf                   thrpt    5  2030.549 ±  60.276  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB                 thrpt    5  3187.551 ±  46.528  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB_autoTypeFilter  thrpt    5  2936.665 ±  23.466  ops/ms
EishayParseBinaryAutoType.hessian                        thrpt    5   316.644 ±  15.924  ops/ms
EishayParseBinaryAutoType.javaSerialize                  thrpt    5    61.602 ±   3.788  ops/ms
EishayParseString.fastjson1                              thrpt    5  2076.783 ± 121.981  ops/ms
EishayParseString.fastjson2                              thrpt    5  1861.294 ±  26.831  ops/ms
EishayParseString.gson                                   thrpt    5   721.184 ±  18.084  ops/ms
EishayParseString.jackson                                thrpt    5   605.597 ±  28.844  ops/ms
EishayParseStringPretty.fastjson1                        thrpt    5   387.221 ±  45.440  ops/ms
EishayParseStringPretty.fastjson2                        thrpt    5  1579.739 ±  27.591  ops/ms
EishayParseStringPretty.gson                             thrpt    5   680.453 ±  15.619  ops/ms
EishayParseStringPretty.jackson                          thrpt    5   565.789 ±  45.185  ops/ms
EishayParseTreeString.fastjson1                          thrpt    5   724.780 ±  50.770  ops/ms
EishayParseTreeString.fastjson2                          thrpt    5  1499.716 ±  16.385  ops/ms
EishayParseTreeString.gson                               thrpt    5   541.313 ±  20.736  ops/ms
EishayParseTreeString.jackson                            thrpt    5   775.458 ±   8.201  ops/ms
EishayParseTreeStringPretty.fastjson1                    thrpt    5   656.517 ±  23.574  ops/ms
EishayParseTreeStringPretty.fastjson2                    thrpt    5  1187.827 ±  24.121  ops/ms
EishayParseTreeStringPretty.gson                         thrpt    5   496.154 ±  28.647  ops/ms
EishayParseTreeStringPretty.jackson                      thrpt    5   699.669 ±  17.839  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                       thrpt    5   682.980 ±  10.805  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                       thrpt    5  1548.730 ±  20.657  ops/ms
EishayParseTreeUTF8Bytes.gson                            thrpt    5   545.083 ±  13.727  ops/ms
EishayParseTreeUTF8Bytes.jackson                         thrpt    5   862.234 ±  19.985  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1                 thrpt    5   549.319 ±  16.997  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2                 thrpt    5  1229.478 ±  14.361  ops/ms
EishayParseTreeUTF8BytesPretty.gson                      thrpt    5   557.777 ±  14.366  ops/ms
EishayParseTreeUTF8BytesPretty.jackson                   thrpt    5   889.546 ±  12.161  ops/ms
EishayParseUTF8Bytes.dsljson                             thrpt    5   647.733 ±  77.920  ops/ms
EishayParseUTF8Bytes.fastjson1                           thrpt    5  1681.711 ±  29.084  ops/ms
EishayParseUTF8Bytes.fastjson2                           thrpt    5  1566.484 ±  39.181  ops/ms
EishayParseUTF8Bytes.gson                                thrpt    5   558.540 ±   5.180  ops/ms
EishayParseUTF8Bytes.jackson                             thrpt    5   787.790 ±  85.069  ops/ms
EishayParseUTF8BytesPretty.fastjson1                     thrpt    5   390.053 ±  32.445  ops/ms
EishayParseUTF8BytesPretty.fastjson2                     thrpt    5  1522.939 ±  18.692  ops/ms
EishayParseUTF8BytesPretty.gson                          thrpt    5   555.460 ±  16.910  ops/ms
EishayParseUTF8BytesPretty.jackson                       thrpt    5   707.865 ±  52.695  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes                     thrpt    5  3117.706 ±  26.497  ops/ms
EishayWriteBinary.hessian                                thrpt    5   684.918 ±  58.881  ops/ms
EishayWriteBinary.javaSerialize                          thrpt    5   463.288 ±   3.442  ops/ms
EishayWriteBinary.jsonb                                  thrpt    5  5012.228 ±  51.692  ops/ms
EishayWriteBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5  1079.349 ±   5.669  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  3325.741 ±  24.502  ops/ms
EishayWriteBinaryArrayMapping.jsonb                      thrpt    5  9764.298 ±  53.827  ops/ms
EishayWriteBinaryArrayMapping.kryo                       thrpt    5  2974.813 ±  18.576  ops/ms
EishayWriteBinaryArrayMapping.protobuf                   thrpt    5  2438.219 ±  61.272  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB                 thrpt    5  2944.967 ±  14.830  ops/ms
EishayWriteBinaryAutoType.fastjson2UTF8Bytes             thrpt    5  3053.953 ±  18.249  ops/ms
EishayWriteBinaryAutoType.hessian                        thrpt    5   675.677 ±   5.734  ops/ms
EishayWriteBinaryAutoType.javaSerialize                  thrpt    5   442.251 ±   3.236  ops/ms
EishayWriteString.fastjson1                              thrpt    5  1241.511 ±   3.319  ops/ms
EishayWriteString.fastjson2                              thrpt    5  2886.967 ±  22.377  ops/ms
EishayWriteString.gson                                   thrpt    5   562.147 ±   3.820  ops/ms
EishayWriteString.jackson                                thrpt    5  1697.224 ±  10.864  ops/ms
EishayWriteStringTree.fastjson1                          thrpt    5  1269.979 ±   5.112  ops/ms
EishayWriteStringTree.fastjson2                          thrpt    5  1909.684 ±  10.501  ops/ms
EishayWriteStringTree.gson                               thrpt    5   561.632 ±   4.285  ops/ms
EishayWriteStringTree.jackson                            thrpt    5  1420.402 ±   9.666  ops/ms
EishayWriteStringTree1x.fastjson1                        thrpt    5  1131.848 ±   7.943  ops/ms
EishayWriteStringTree1x.fastjson2                        thrpt    5  1460.200 ±   6.793  ops/ms
EishayWriteStringTree1x.gson                             thrpt    5   553.824 ±   3.574  ops/ms
EishayWriteStringTree1x.jackson                          thrpt    5  1278.307 ±   7.019  ops/ms
EishayWriteUTF8Bytes.fastjson1                           thrpt    5  1033.495 ±   7.075  ops/ms
EishayWriteUTF8Bytes.fastjson2                           thrpt    5  3100.510 ±  18.195  ops/ms
EishayWriteUTF8Bytes.gson                                thrpt    5   555.081 ±   4.210  ops/ms
EishayWriteUTF8Bytes.jackson                             thrpt    5  1521.229 ±   9.806  ops/ms
```
# Apple_M1_Pro-graalvm-ce-java11-22.3.1
```java
Benchmark                                                 Mode  Cnt     Score     Error   Units
EishayParseBinary.fastjson2UTF8Bytes                     thrpt    5   876.625 ±  91.849  ops/ms
EishayParseBinary.hessian                                thrpt    5   243.723 ±   3.283  ops/ms
EishayParseBinary.javaSerialize                          thrpt    5    57.126 ±   1.120  ops/ms
EishayParseBinary.jsonb                                  thrpt    5  2181.652 ±   9.391  ops/ms
EishayParseBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5  1609.296 ±   3.392  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  1446.009 ± 903.444  ops/ms
EishayParseBinaryArrayMapping.jsonb                      thrpt    5  5181.932 ±   7.362  ops/ms
EishayParseBinaryArrayMapping.kryo                       thrpt    5  1748.439 ±  31.966  ops/ms
EishayParseBinaryArrayMapping.protobuf                   thrpt    5  1906.275 ±   4.199  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB                 thrpt    5  2931.183 ±  11.671  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB_autoTypeFilter  thrpt    5  2866.760 ±   2.370  ops/ms
EishayParseBinaryAutoType.hessian                        thrpt    5   286.767 ±   9.133  ops/ms
EishayParseBinaryAutoType.javaSerialize                  thrpt    5    56.118 ±   0.257  ops/ms
EishayParseString.fastjson1                              thrpt    5  1135.997 ±  32.211  ops/ms
EishayParseString.fastjson2                              thrpt    5  1266.585 ±  53.945  ops/ms
EishayParseString.gson                                   thrpt    5   737.545 ±   4.780  ops/ms
EishayParseString.jackson                                thrpt    5   582.729 ±  21.793  ops/ms
EishayParseStringPretty.fastjson1                        thrpt    5   329.764 ±  25.107  ops/ms
EishayParseStringPretty.fastjson2                        thrpt    5  1109.484 ± 261.509  ops/ms
EishayParseStringPretty.gson                             thrpt    5   640.779 ±   4.865  ops/ms
EishayParseStringPretty.jackson                          thrpt    5   580.739 ±  31.268  ops/ms
EishayParseTreeString.fastjson1                          thrpt    5   673.862 ±  11.774  ops/ms
EishayParseTreeString.fastjson2                          thrpt    5  1327.450 ±   6.132  ops/ms
EishayParseTreeString.gson                               thrpt    5   627.312 ±   2.981  ops/ms
EishayParseTreeString.jackson                            thrpt    5   741.676 ±   9.529  ops/ms
EishayParseTreeStringPretty.fastjson1                    thrpt    5   542.202 ±   5.406  ops/ms
EishayParseTreeStringPretty.fastjson2                    thrpt    5  1108.193 ±   4.355  ops/ms
EishayParseTreeStringPretty.gson                         thrpt    5   579.777 ±   2.328  ops/ms
EishayParseTreeStringPretty.jackson                      thrpt    5   714.959 ±   5.685  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                       thrpt    5   552.298 ±  16.095  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                       thrpt    5   969.507 ±   7.113  ops/ms
EishayParseTreeUTF8Bytes.gson                            thrpt    5   536.288 ±   2.415  ops/ms
EishayParseTreeUTF8Bytes.jackson                         thrpt    5   868.207 ±   8.660  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1                 thrpt    5   440.657 ±  22.763  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2                 thrpt    5   753.539 ±  16.417  ops/ms
EishayParseTreeUTF8BytesPretty.gson                      thrpt    5   521.969 ±   5.358  ops/ms
EishayParseTreeUTF8BytesPretty.jackson                   thrpt    5   772.751 ±  11.025  ops/ms
EishayParseUTF8Bytes.dsljson                             thrpt    5   674.049 ±  43.370  ops/ms
EishayParseUTF8Bytes.fastjson1                           thrpt    5   860.799 ±  55.036  ops/ms
EishayParseUTF8Bytes.fastjson2                           thrpt    5   882.327 ±  59.602  ops/ms
EishayParseUTF8Bytes.gson                                thrpt    5   561.701 ±   2.352  ops/ms
EishayParseUTF8Bytes.jackson                             thrpt    5   602.426 ±  39.897  ops/ms
EishayParseUTF8BytesPretty.fastjson1                     thrpt    5   327.009 ±  11.104  ops/ms
EishayParseUTF8BytesPretty.fastjson2                     thrpt    5   686.395 ± 109.016  ops/ms
EishayParseUTF8BytesPretty.gson                          thrpt    5   522.609 ±   3.414  ops/ms
EishayParseUTF8BytesPretty.jackson                       thrpt    5   534.875 ±  25.854  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes                     thrpt    5  3023.427 ±  20.638  ops/ms
EishayWriteBinary.hessian                                thrpt    5   425.200 ±   2.655  ops/ms
EishayWriteBinary.javaSerialize                          thrpt    5   380.861 ±   1.468  ops/ms
EishayWriteBinary.jsonb                                  thrpt    5  5607.419 ±  24.675  ops/ms
EishayWriteBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5  1119.013 ±   7.269  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  3746.542 ±   7.140  ops/ms
EishayWriteBinaryArrayMapping.jsonb                      thrpt    5  9958.764 ±  29.723  ops/ms
EishayWriteBinaryArrayMapping.kryo                       thrpt    5  2934.688 ±   2.813  ops/ms
EishayWriteBinaryArrayMapping.protobuf                   thrpt    5  1680.446 ±  26.524  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB                 thrpt    5  2417.638 ±   6.843  ops/ms
EishayWriteBinaryAutoType.fastjson2UTF8Bytes             thrpt    5  2942.002 ±   7.611  ops/ms
EishayWriteBinaryAutoType.hessian                        thrpt    5   423.495 ±   0.986  ops/ms
EishayWriteBinaryAutoType.javaSerialize                  thrpt    5   381.952 ±   0.464  ops/ms
EishayWriteString.fastjson1                              thrpt    5  1345.474 ±   2.431  ops/ms
EishayWriteString.fastjson2                              thrpt    5  2934.994 ±   7.402  ops/ms
EishayWriteString.gson                                   thrpt    5   626.735 ±   0.422  ops/ms
EishayWriteString.jackson                                thrpt    5  1543.577 ±   4.746  ops/ms
EishayWriteStringTree.fastjson1                          thrpt    5   428.496 ±   1.045  ops/ms
EishayWriteStringTree.fastjson2                          thrpt    5  2149.485 ±   9.191  ops/ms
EishayWriteStringTree.gson                               thrpt    5   597.663 ±   1.480  ops/ms
EishayWriteStringTree.jackson                            thrpt    5  1424.690 ±   0.996  ops/ms
EishayWriteStringTree1x.fastjson1                        thrpt    5   380.471 ±   0.597  ops/ms
EishayWriteStringTree1x.fastjson2                        thrpt    5  1538.267 ±   3.466  ops/ms
EishayWriteStringTree1x.gson                             thrpt    5   565.945 ±   2.735  ops/ms
EishayWriteStringTree1x.jackson                          thrpt    5  1313.753 ±   8.898  ops/ms
EishayWriteUTF8Bytes.fastjson1                           thrpt    5   786.200 ±   4.761  ops/ms
EishayWriteUTF8Bytes.fastjson2                           thrpt    5  3057.307 ±  19.761  ops/ms
EishayWriteUTF8Bytes.gson                                thrpt    5   519.634 ±   3.385  ops/ms
EishayWriteUTF8Bytes.jackson                             thrpt    5  1467.579 ±   4.516  ops/ms
```
# Apple_M1_Pro-graalvm-ce-java17-22.3.1
```java
Benchmark                                                 Mode  Cnt      Score     Error   Units
EishayParseBinary.fastjson2UTF8Bytes                     thrpt    5    882.892 ±  98.798  ops/ms
EishayParseBinary.hessian                                thrpt    5    276.333 ±  14.934  ops/ms
EishayParseBinary.javaSerialize                          thrpt    5     62.825 ±   2.350  ops/ms
EishayParseBinary.jsonb                                  thrpt    5   3256.598 ± 276.009  ops/ms
EishayParseBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   1919.979 ±  11.120  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5   2280.892 ±  36.929  ops/ms
EishayParseBinaryArrayMapping.jsonb                      thrpt    5   8741.050 ±  36.705  ops/ms
EishayParseBinaryArrayMapping.kryo                       thrpt    5   1705.350 ±  15.845  ops/ms
EishayParseBinaryArrayMapping.protobuf                   thrpt    5   2235.056 ±  38.835  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB                 thrpt    5   3341.436 ±  44.010  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB_autoTypeFilter  thrpt    5   2961.791 ±  27.386  ops/ms
EishayParseBinaryAutoType.hessian                        thrpt    5    272.517 ±  12.043  ops/ms
EishayParseBinaryAutoType.javaSerialize                  thrpt    5     62.436 ±   2.053  ops/ms
EishayParseString.fastjson1                              thrpt    5   1345.240 ±  26.126  ops/ms
EishayParseString.fastjson2                              thrpt    5   1451.183 ± 252.674  ops/ms
EishayParseString.gson                                   thrpt    5    714.062 ±  23.122  ops/ms
EishayParseString.jackson                                thrpt    5    611.876 ±  98.887  ops/ms
EishayParseStringPretty.fastjson1                        thrpt    5    361.816 ±  34.142  ops/ms
EishayParseStringPretty.fastjson2                        thrpt    5   1209.089 ±  80.064  ops/ms
EishayParseStringPretty.gson                             thrpt    5    645.003 ±  15.585  ops/ms
EishayParseStringPretty.jackson                          thrpt    5    560.162 ±  42.949  ops/ms
EishayParseTreeString.fastjson1                          thrpt    5    852.293 ±  37.353  ops/ms
EishayParseTreeString.fastjson2                          thrpt    5   1324.567 ±   8.080  ops/ms
EishayParseTreeString.gson                               thrpt    5    609.227 ±   7.324  ops/ms
EishayParseTreeString.jackson                            thrpt    5    757.925 ±   9.318  ops/ms
EishayParseTreeStringPretty.fastjson1                    thrpt    5    707.210 ±  32.274  ops/ms
EishayParseTreeStringPretty.fastjson2                    thrpt    5   1134.477 ±  11.429  ops/ms
EishayParseTreeStringPretty.gson                         thrpt    5    567.364 ±   7.781  ops/ms
EishayParseTreeStringPretty.jackson                      thrpt    5    777.992 ±  15.259  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                       thrpt    5    667.443 ±   9.578  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                       thrpt    5    990.755 ±  31.431  ops/ms
EishayParseTreeUTF8Bytes.gson                            thrpt    5    545.091 ±  11.024  ops/ms
EishayParseTreeUTF8Bytes.jackson                         thrpt    5    799.239 ±  28.976  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1                 thrpt    5    515.329 ±  30.924  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2                 thrpt    5    806.957 ±  16.621  ops/ms
EishayParseTreeUTF8BytesPretty.gson                      thrpt    5    519.339 ±   4.587  ops/ms
EishayParseTreeUTF8BytesPretty.jackson                   thrpt    5    751.540 ±  20.282  ops/ms
EishayParseUTF8Bytes.dsljson                             thrpt    5    453.101 ±  27.954  ops/ms
EishayParseUTF8Bytes.fastjson1                           thrpt    5   1092.287 ±  10.512  ops/ms
EishayParseUTF8Bytes.fastjson2                           thrpt    5    848.088 ± 104.094  ops/ms
EishayParseUTF8Bytes.gson                                thrpt    5    534.884 ±  12.898  ops/ms
EishayParseUTF8Bytes.jackson                             thrpt    5    663.143 ± 106.746  ops/ms
EishayParseUTF8BytesPretty.fastjson1                     thrpt    5    370.345 ±  23.885  ops/ms
EishayParseUTF8BytesPretty.fastjson2                     thrpt    5    640.196 ±  47.285  ops/ms
EishayParseUTF8BytesPretty.gson                          thrpt    5    520.722 ±   4.344  ops/ms
EishayParseUTF8BytesPretty.jackson                       thrpt    5    527.601 ±  41.519  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes                     thrpt    5   3113.385 ±  40.050  ops/ms
EishayWriteBinary.hessian                                thrpt    5    711.612 ±   9.654  ops/ms
EishayWriteBinary.javaSerialize                          thrpt    5    386.411 ±   4.042  ops/ms
EishayWriteBinary.jsonb                                  thrpt    5   5667.029 ±  31.741  ops/ms
EishayWriteBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5    851.158 ±   8.432  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5   3781.375 ±  26.182  ops/ms
EishayWriteBinaryArrayMapping.jsonb                      thrpt    5  10184.715 ±  92.199  ops/ms
EishayWriteBinaryArrayMapping.kryo                       thrpt    5   2953.043 ±  26.836  ops/ms
EishayWriteBinaryArrayMapping.protobuf                   thrpt    5   2755.648 ±  55.724  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB                 thrpt    5   3021.513 ±  18.500  ops/ms
EishayWriteBinaryAutoType.fastjson2UTF8Bytes             thrpt    5   2991.826 ±  16.080  ops/ms
EishayWriteBinaryAutoType.hessian                        thrpt    5    697.025 ±   4.927  ops/ms
EishayWriteBinaryAutoType.javaSerialize                  thrpt    5    383.033 ±   2.352  ops/ms
EishayWriteString.fastjson1                              thrpt    5   1101.008 ±   6.847  ops/ms
EishayWriteString.fastjson2                              thrpt    5   2964.834 ±  16.385  ops/ms
EishayWriteString.gson                                   thrpt    5    421.758 ±   2.421  ops/ms
EishayWriteString.jackson                                thrpt    5   1499.439 ±   7.187  ops/ms
EishayWriteStringTree.fastjson1                          thrpt    5    517.902 ±   3.619  ops/ms
EishayWriteStringTree.fastjson2                          thrpt    5   2155.176 ±  15.745  ops/ms
EishayWriteStringTree.gson                               thrpt    5    432.156 ±   3.018  ops/ms
EishayWriteStringTree.jackson                            thrpt    5   1428.067 ±   5.547  ops/ms
EishayWriteStringTree1x.fastjson1                        thrpt    5    461.169 ±   2.851  ops/ms
EishayWriteStringTree1x.fastjson2                        thrpt    5   1769.114 ±   6.785  ops/ms
EishayWriteStringTree1x.gson                             thrpt    5    426.072 ±   2.572  ops/ms
EishayWriteStringTree1x.jackson                          thrpt    5   1252.440 ±   3.034  ops/ms
EishayWriteUTF8Bytes.fastjson1                           thrpt    5    965.560 ±   6.631  ops/ms
EishayWriteUTF8Bytes.fastjson2                           thrpt    5   3107.308 ±  22.772  ops/ms
EishayWriteUTF8Bytes.gson                                thrpt    5    380.395 ±   2.149  ops/ms
EishayWriteUTF8Bytes.jackson                             thrpt    5   1624.094 ±  11.711  ops/ms
```
# Apple_M1_Pro-graalvm-ee-java11-22.3.1
```java
Benchmark                                                 Mode  Cnt      Score     Error   Units
EishayParseBinary.fastjson2UTF8Bytes                     thrpt    5   1257.757 ±  84.142  ops/ms
EishayParseBinary.hessian                                thrpt    5    652.769 ±   4.865  ops/ms
EishayParseBinary.javaSerialize                          thrpt    5     63.335 ±   0.412  ops/ms
EishayParseBinary.jsonb                                  thrpt    5   5787.318 ±  16.428  ops/ms
EishayParseBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   2269.649 ±   3.152  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5   2905.128 ± 230.878  ops/ms
EishayParseBinaryArrayMapping.jsonb                      thrpt    5   9537.255 ±  99.469  ops/ms
EishayParseBinaryArrayMapping.kryo                       thrpt    5   2172.053 ±  45.075  ops/ms
EishayParseBinaryArrayMapping.protobuf                   thrpt    5   4087.729 ±  28.975  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB                 thrpt    5   3428.836 ±  16.017  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB_autoTypeFilter  thrpt    5   3189.822 ±   7.391  ops/ms
EishayParseBinaryAutoType.hessian                        thrpt    5    618.531 ±   4.823  ops/ms
EishayParseBinaryAutoType.javaSerialize                  thrpt    5     59.956 ±   1.214  ops/ms
EishayParseString.fastjson1                              thrpt    5   1867.750 ±  33.546  ops/ms
EishayParseString.fastjson2                              thrpt    5   1321.855 ± 175.337  ops/ms
EishayParseString.gson                                   thrpt    5    759.976 ±   1.325  ops/ms
EishayParseString.jackson                                thrpt    5    787.710 ±   8.405  ops/ms
EishayParseStringPretty.fastjson1                        thrpt    5    406.451 ±  10.500  ops/ms
EishayParseStringPretty.fastjson2                        thrpt    5   1083.581 ±  42.282  ops/ms
EishayParseStringPretty.gson                             thrpt    5    735.662 ±   5.192  ops/ms
EishayParseStringPretty.jackson                          thrpt    5    576.015 ±  15.158  ops/ms
EishayParseTreeString.fastjson1                          thrpt    5   1007.798 ±   7.049  ops/ms
EishayParseTreeString.fastjson2                          thrpt    5   1604.808 ±   9.566  ops/ms
EishayParseTreeString.gson                               thrpt    5    659.908 ±   2.794  ops/ms
EishayParseTreeString.jackson                            thrpt    5    887.666 ±   6.855  ops/ms
EishayParseTreeStringPretty.fastjson1                    thrpt    5    806.841 ±   9.370  ops/ms
EishayParseTreeStringPretty.fastjson2                    thrpt    5   1360.905 ±   9.933  ops/ms
EishayParseTreeStringPretty.gson                         thrpt    5    634.419 ±   7.241  ops/ms
EishayParseTreeStringPretty.jackson                      thrpt    5    754.136 ±   9.450  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                       thrpt    5    756.808 ±   6.206  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                       thrpt    5   1489.926 ±   9.179  ops/ms
EishayParseTreeUTF8Bytes.gson                            thrpt    5    639.504 ±   3.353  ops/ms
EishayParseTreeUTF8Bytes.jackson                         thrpt    5    975.341 ±   5.908  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1                 thrpt    5    614.088 ±   8.400  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2                 thrpt    5   1253.213 ±   7.259  ops/ms
EishayParseTreeUTF8BytesPretty.gson                      thrpt    5    663.701 ±  11.597  ops/ms
EishayParseTreeUTF8BytesPretty.jackson                   thrpt    5    864.544 ±  55.618  ops/ms
EishayParseUTF8Bytes.dsljson                             thrpt    5    747.029 ±  65.658  ops/ms
EishayParseUTF8Bytes.fastjson1                           thrpt    5   1147.308 ±  18.945  ops/ms
EishayParseUTF8Bytes.fastjson2                           thrpt    5   1223.528 ±  94.822  ops/ms
EishayParseUTF8Bytes.gson                                thrpt    5    638.088 ±   6.807  ops/ms
EishayParseUTF8Bytes.jackson                             thrpt    5    806.851 ±  24.081  ops/ms
EishayParseUTF8BytesPretty.fastjson1                     thrpt    5    406.956 ±   3.809  ops/ms
EishayParseUTF8BytesPretty.fastjson2                     thrpt    5    948.009 ±  65.034  ops/ms
EishayParseUTF8BytesPretty.gson                          thrpt    5    666.694 ±   4.914  ops/ms
EishayParseUTF8BytesPretty.jackson                       thrpt    5    732.388 ±  22.726  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes                     thrpt    5   3552.114 ±  29.640  ops/ms
EishayWriteBinary.hessian                                thrpt    5    812.296 ±   6.356  ops/ms
EishayWriteBinary.javaSerialize                          thrpt    5    484.932 ±   2.696  ops/ms
EishayWriteBinary.jsonb                                  thrpt    5   6439.310 ±  36.330  ops/ms
EishayWriteBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5    865.625 ±   4.562  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5   3865.635 ±  18.517  ops/ms
EishayWriteBinaryArrayMapping.jsonb                      thrpt    5  14661.723 ±  13.572  ops/ms
EishayWriteBinaryArrayMapping.kryo                       thrpt    5   3468.546 ±   3.252  ops/ms
EishayWriteBinaryArrayMapping.protobuf                   thrpt    5   4890.399 ±   6.874  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB                 thrpt    5   3525.240 ±  18.218  ops/ms
EishayWriteBinaryAutoType.fastjson2UTF8Bytes             thrpt    5   3495.797 ±   6.620  ops/ms
EishayWriteBinaryAutoType.hessian                        thrpt    5    827.337 ±   1.387  ops/ms
EishayWriteBinaryAutoType.javaSerialize                  thrpt    5    488.726 ±   0.882  ops/ms
EishayWriteString.fastjson1                              thrpt    5   1418.528 ±   3.745  ops/ms
EishayWriteString.fastjson2                              thrpt    5   3380.228 ±   8.375  ops/ms
EishayWriteString.gson                                   thrpt    5    688.061 ±  14.078  ops/ms
EishayWriteString.jackson                                thrpt    5   1814.617 ±  10.525  ops/ms
EishayWriteStringTree.fastjson1                          thrpt    5   1016.555 ±   1.855  ops/ms
EishayWriteStringTree.fastjson2                          thrpt    5   2306.520 ±   1.017  ops/ms
EishayWriteStringTree.gson                               thrpt    5    764.089 ±   1.420  ops/ms
EishayWriteStringTree.jackson                            thrpt    5   1550.763 ±   2.851  ops/ms
EishayWriteStringTree1x.fastjson1                        thrpt    5   1383.995 ±   0.926  ops/ms
EishayWriteStringTree1x.fastjson2                        thrpt    5   2040.258 ±   2.259  ops/ms
EishayWriteStringTree1x.gson                             thrpt    5    754.062 ±   1.630  ops/ms
EishayWriteStringTree1x.jackson                          thrpt    5   1463.500 ±  10.185  ops/ms
EishayWriteUTF8Bytes.fastjson1                           thrpt    5   1004.556 ±   5.086  ops/ms
EishayWriteUTF8Bytes.fastjson2                           thrpt    5   3532.501 ±  21.524  ops/ms
EishayWriteUTF8Bytes.gson                                thrpt    5    692.741 ±   4.142  ops/ms
EishayWriteUTF8Bytes.jackson                             thrpt    5   1873.121 ±  17.162  ops/ms
```
