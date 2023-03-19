# ecs.c7.xlarge-jdk1.8.0_361
```java
Benchmark                                                 Mode  Cnt     Score    Error   Units
EishayParseBinary.fastjson2UTF8Bytes                     thrpt    5  1021.227 ? 15.930  ops/ms
EishayParseBinary.hessian                                thrpt    5   282.397 ?  4.605  ops/ms
EishayParseBinary.javaSerialize                          thrpt    5    47.601 ?  0.763  ops/ms
EishayParseBinary.jsonb                                  thrpt    5  1700.885 ? 18.765  ops/ms
EishayParseBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5  1471.864 ? 20.938  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  1634.745 ? 37.343  ops/ms
EishayParseBinaryArrayMapping.jsonb                      thrpt    5  2740.171 ? 45.777  ops/ms
EishayParseBinaryArrayMapping.kryo                       thrpt    5  1700.698 ? 28.205  ops/ms
EishayParseBinaryArrayMapping.protobuf                   thrpt    5  1040.423 ? 25.867  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB                 thrpt    5  1422.034 ? 31.271  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB_autoTypeFilter  thrpt    5  1364.731 ? 10.177  ops/ms
EishayParseBinaryAutoType.hessian                        thrpt    5   288.511 ?  3.074  ops/ms
EishayParseBinaryAutoType.javaSerialize                  thrpt    5    46.836 ?  0.778  ops/ms
EishayParseString.fastjson1                              thrpt    5   969.297 ? 11.471  ops/ms
EishayParseString.fastjson2                              thrpt    5  1275.975 ? 19.245  ops/ms
EishayParseString.gson                                   thrpt    5   426.440 ?  4.243  ops/ms
EishayParseString.jackson                                thrpt    5   516.210 ?  9.239  ops/ms
EishayParseStringPretty.fastjson1                        thrpt    5   264.502 ?  2.520  ops/ms
EishayParseStringPretty.fastjson2                        thrpt    5   950.492 ? 11.028  ops/ms
EishayParseStringPretty.gson                             thrpt    5   403.787 ?  5.515  ops/ms
EishayParseStringPretty.jackson                          thrpt    5   480.051 ?  2.627  ops/ms
EishayParseTreeString.fastjson1                          thrpt    5   503.555 ?  4.961  ops/ms
EishayParseTreeString.fastjson2                          thrpt    5   937.001 ? 12.206  ops/ms
EishayParseTreeString.gson                               thrpt    5   328.918 ?  5.948  ops/ms
EishayParseTreeString.jackson                            thrpt    5   549.272 ? 12.535  ops/ms
EishayParseTreeStringPretty.fastjson1                    thrpt    5   431.393 ?  4.688  ops/ms
EishayParseTreeStringPretty.fastjson2                    thrpt    5   704.519 ? 10.892  ops/ms
EishayParseTreeStringPretty.gson                         thrpt    5   322.204 ?  1.935  ops/ms
EishayParseTreeStringPretty.jackson                      thrpt    5   482.088 ?  9.155  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                       thrpt    5   428.125 ? 12.991  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                       thrpt    5   855.130 ? 20.690  ops/ms
EishayParseTreeUTF8Bytes.gson                            thrpt    5   312.609 ?  1.964  ops/ms
EishayParseTreeUTF8Bytes.jackson                         thrpt    5   581.635 ?  9.831  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1                 thrpt    5   366.022 ? 10.897  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2                 thrpt    5   706.639 ? 13.056  ops/ms
EishayParseTreeUTF8BytesPretty.gson                      thrpt    5   292.252 ?  8.236  ops/ms
EishayParseTreeUTF8BytesPretty.jackson                   thrpt    5   521.456 ? 11.109  ops/ms
EishayParseUTF8Bytes.dsljson                             thrpt    5   906.579 ? 18.521  ops/ms
EishayParseUTF8Bytes.fastjson1                           thrpt    5   774.381 ?  5.758  ops/ms
EishayParseUTF8Bytes.fastjson2                           thrpt    5  1096.180 ?  6.778  ops/ms
EishayParseUTF8Bytes.gson                                thrpt    5   316.737 ?  5.734  ops/ms
EishayParseUTF8Bytes.jackson                             thrpt    5   604.382 ?  7.962  ops/ms
EishayParseUTF8BytesPretty.fastjson1                     thrpt    5   239.864 ?  4.672  ops/ms
EishayParseUTF8BytesPretty.fastjson2                     thrpt    5   806.853 ? 12.958  ops/ms
EishayParseUTF8BytesPretty.gson                          thrpt    5   290.422 ?  5.227  ops/ms
EishayParseUTF8BytesPretty.jackson                       thrpt    5   519.596 ? 12.868  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes                     thrpt    5  1506.690 ? 31.705  ops/ms
EishayWriteBinary.hessian                                thrpt    5   344.445 ? 15.055  ops/ms
EishayWriteBinary.javaSerialize                          thrpt    5   223.185 ?  2.660  ops/ms
EishayWriteBinary.jsonb                                  thrpt    5  1880.009 ? 51.267  ops/ms
EishayWriteBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   767.300 ? 18.993  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  1828.969 ? 20.824  ops/ms
EishayWriteBinaryArrayMapping.jsonb                      thrpt    5  3097.706 ? 57.360  ops/ms
EishayWriteBinaryArrayMapping.kryo                       thrpt    5  1834.781 ? 34.056  ops/ms
EishayWriteBinaryArrayMapping.protobuf                   thrpt    5  1317.235 ? 31.113  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB                 thrpt    5  1291.535 ? 27.038  ops/ms
EishayWriteBinaryAutoType.fastjson2UTF8Bytes             thrpt    5  1479.158 ? 36.986  ops/ms
EishayWriteBinaryAutoType.hessian                        thrpt    5   337.889 ?  5.723  ops/ms
EishayWriteBinaryAutoType.javaSerialize                  thrpt    5   237.405 ?  6.126  ops/ms
EishayWriteString.fastjson1                              thrpt    5   617.465 ?  6.316  ops/ms
EishayWriteString.fastjson2                              thrpt    5  1425.406 ? 24.240  ops/ms
EishayWriteString.gson                                   thrpt    5   438.237 ?  5.952  ops/ms
EishayWriteString.jackson                                thrpt    5   994.869 ? 21.298  ops/ms
EishayWriteStringTree.fastjson1                          thrpt    5   836.378 ? 16.651  ops/ms
EishayWriteStringTree.fastjson2                          thrpt    5  1210.470 ? 17.175  ops/ms
EishayWriteStringTree.gson                               thrpt    5   472.093 ?  9.836  ops/ms
EishayWriteStringTree.jackson                            thrpt    5   948.870 ? 14.893  ops/ms
EishayWriteStringTree1x.fastjson1                        thrpt    5   803.563 ? 20.191  ops/ms
EishayWriteStringTree1x.fastjson2                        thrpt    5  1074.261 ? 18.907  ops/ms
EishayWriteStringTree1x.gson                             thrpt    5   473.351 ?  4.342  ops/ms
EishayWriteStringTree1x.jackson                          thrpt    5   869.180 ? 25.784  ops/ms
EishayWriteUTF8Bytes.fastjson1                           thrpt    5   586.257 ?  4.760  ops/ms
EishayWriteUTF8Bytes.fastjson2                           thrpt    5  1514.597 ? 26.454  ops/ms
EishayWriteUTF8Bytes.gson                                thrpt    5   383.590 ?  5.040  ops/ms
EishayWriteUTF8Bytes.jackson                             thrpt    5   925.776 ?  9.492  ops/ms
```
# ecs.c7.xlarge-jdk-11.0.18
```java
Benchmark                                                 Mode  Cnt     Score    Error   Units
EishayParseBinary.fastjson2UTF8Bytes                     thrpt    5  1290.005 ?  8.629  ops/ms
EishayParseBinary.hessian                                thrpt    5   254.289 ?  0.444  ops/ms
EishayParseBinary.javaSerialize                          thrpt    5    47.897 ?  0.404  ops/ms
EishayParseBinary.jsonb                                  thrpt    5  2626.758 ? 17.434  ops/ms
EishayParseBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5  1490.289 ? 21.401  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  1904.449 ? 10.553  ops/ms
EishayParseBinaryArrayMapping.jsonb                      thrpt    5  3952.737 ? 38.749  ops/ms
EishayParseBinaryArrayMapping.kryo                       thrpt    5  1595.044 ? 18.477  ops/ms
EishayParseBinaryArrayMapping.protobuf                   thrpt    5  1305.218 ? 17.126  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB                 thrpt    5  1915.518 ? 18.278  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB_autoTypeFilter  thrpt    5  1628.745 ? 11.692  ops/ms
EishayParseBinaryAutoType.hessian                        thrpt    5   252.958 ?  2.590  ops/ms
EishayParseBinaryAutoType.javaSerialize                  thrpt    5    49.918 ?  0.582  ops/ms
EishayParseString.fastjson1                              thrpt    5   923.186 ?  6.960  ops/ms
EishayParseString.fastjson2                              thrpt    5  1184.086 ? 24.128  ops/ms
EishayParseString.gson                                   thrpt    5   419.243 ?  3.725  ops/ms
EishayParseString.jackson                                thrpt    5   506.262 ?  1.818  ops/ms
EishayParseStringPretty.fastjson1                        thrpt    5   244.005 ?  1.151  ops/ms
EishayParseStringPretty.fastjson2                        thrpt    5   882.294 ?  6.592  ops/ms
EishayParseStringPretty.gson                             thrpt    5   384.278 ?  0.852  ops/ms
EishayParseStringPretty.jackson                          thrpt    5   456.278 ?  1.332  ops/ms
EishayParseTreeString.fastjson1                          thrpt    5   385.063 ?  1.525  ops/ms
EishayParseTreeString.fastjson2                          thrpt    5   828.292 ?  3.718  ops/ms
EishayParseTreeString.gson                               thrpt    5   310.019 ?  1.226  ops/ms
EishayParseTreeString.jackson                            thrpt    5   470.965 ?  2.266  ops/ms
EishayParseTreeStringPretty.fastjson1                    thrpt    5   348.092 ?  2.889  ops/ms
EishayParseTreeStringPretty.fastjson2                    thrpt    5   665.792 ?  3.575  ops/ms
EishayParseTreeStringPretty.gson                         thrpt    5   302.371 ?  1.394  ops/ms
EishayParseTreeStringPretty.jackson                      thrpt    5   419.410 ?  3.002  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                       thrpt    5   365.867 ?  7.056  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                       thrpt    5   832.986 ?  2.273  ops/ms
EishayParseTreeUTF8Bytes.gson                            thrpt    5   321.528 ?  2.440  ops/ms
EishayParseTreeUTF8Bytes.jackson                         thrpt    5   529.384 ?  5.876  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1                 thrpt    5   305.839 ?  3.026  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2                 thrpt    5   669.110 ?  7.267  ops/ms
EishayParseTreeUTF8BytesPretty.gson                      thrpt    5   297.826 ?  1.720  ops/ms
EishayParseTreeUTF8BytesPretty.jackson                   thrpt    5   481.592 ?  5.676  ops/ms
EishayParseUTF8Bytes.dsljson                             thrpt    5   869.425 ? 14.235  ops/ms
EishayParseUTF8Bytes.fastjson1                           thrpt    5   748.264 ?  7.158  ops/ms
EishayParseUTF8Bytes.fastjson2                           thrpt    5  1210.481 ? 15.021  ops/ms
EishayParseUTF8Bytes.gson                                thrpt    5   311.924 ?  3.367  ops/ms
EishayParseUTF8Bytes.jackson                             thrpt    5   531.783 ?  3.843  ops/ms
EishayParseUTF8BytesPretty.fastjson1                     thrpt    5   224.381 ?  1.595  ops/ms
EishayParseUTF8BytesPretty.fastjson2                     thrpt    5   888.235 ? 36.904  ops/ms
EishayParseUTF8BytesPretty.gson                          thrpt    5   292.653 ?  3.103  ops/ms
EishayParseUTF8BytesPretty.jackson                       thrpt    5   494.721 ?  4.915  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes                     thrpt    5  1580.593 ?  9.831  ops/ms
EishayWriteBinary.hessian                                thrpt    5   329.473 ?  3.268  ops/ms
EishayWriteBinary.javaSerialize                          thrpt    5   213.279 ?  1.849  ops/ms
EishayWriteBinary.jsonb                                  thrpt    5  2440.718 ? 30.658  ops/ms
EishayWriteBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   720.883 ?  8.930  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  2075.164 ? 19.613  ops/ms
EishayWriteBinaryArrayMapping.jsonb                      thrpt    5  4522.875 ? 25.876  ops/ms
EishayWriteBinaryArrayMapping.kryo                       thrpt    5  1979.943 ? 32.029  ops/ms
EishayWriteBinaryArrayMapping.protobuf                   thrpt    5  1540.292 ?  9.504  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB                 thrpt    5  1428.065 ? 17.915  ops/ms
EishayWriteBinaryAutoType.fastjson2UTF8Bytes             thrpt    5  1537.336 ? 11.373  ops/ms
EishayWriteBinaryAutoType.hessian                        thrpt    5   331.241 ?  2.733  ops/ms
EishayWriteBinaryAutoType.javaSerialize                  thrpt    5   215.735 ?  1.800  ops/ms
EishayWriteString.fastjson1                              thrpt    5   585.857 ?  5.264  ops/ms
EishayWriteString.fastjson2                              thrpt    5  1493.577 ? 13.108  ops/ms
EishayWriteString.gson                                   thrpt    5   367.352 ? 13.438  ops/ms
EishayWriteString.jackson                                thrpt    5   933.701 ?  3.507  ops/ms
EishayWriteStringTree.fastjson1                          thrpt    5   777.902 ?  5.924  ops/ms
EishayWriteStringTree.fastjson2                          thrpt    5  1245.471 ? 22.730  ops/ms
EishayWriteStringTree.gson                               thrpt    5   384.021 ? 17.669  ops/ms
EishayWriteStringTree.jackson                            thrpt    5   953.973 ?  3.892  ops/ms
EishayWriteStringTree1x.fastjson1                        thrpt    5   746.847 ? 30.179  ops/ms
EishayWriteStringTree1x.fastjson2                        thrpt    5  1113.491 ? 11.936  ops/ms
EishayWriteStringTree1x.gson                             thrpt    5   367.657 ?  2.240  ops/ms
EishayWriteStringTree1x.jackson                          thrpt    5   861.013 ? 25.128  ops/ms
EishayWriteUTF8Bytes.fastjson1                           thrpt    5   544.291 ?  2.527  ops/ms
EishayWriteUTF8Bytes.fastjson2                           thrpt    5  1611.243 ? 19.769  ops/ms
EishayWriteUTF8Bytes.gson                                thrpt    5   360.092 ?  2.580  ops/ms
EishayWriteUTF8Bytes.jackson                             thrpt    5   867.523 ?  5.127  ops/ms
```
# ecs.c7.xlarge-jdk-17.0.6
```java
Benchmark                                                 Mode  Cnt     Score    Error   Units
EishayParseBinary.fastjson2UTF8Bytes                     thrpt    5  1437.568 ? 11.924  ops/ms
EishayParseBinary.hessian                                thrpt    5   259.836 ?  1.742  ops/ms
EishayParseBinary.javaSerialize                          thrpt    5    53.739 ?  0.424  ops/ms
EishayParseBinary.jsonb                                  thrpt    5  2913.209 ? 26.768  ops/ms
EishayParseBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5  1711.816 ? 13.252  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  2347.838 ? 19.588  ops/ms
EishayParseBinaryArrayMapping.jsonb                      thrpt    5  4802.083 ? 60.278  ops/ms
EishayParseBinaryArrayMapping.kryo                       thrpt    5  1799.819 ? 11.155  ops/ms
EishayParseBinaryArrayMapping.protobuf                   thrpt    5  1673.519 ? 19.330  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB                 thrpt    5  2182.570 ? 19.799  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB_autoTypeFilter  thrpt    5  1788.478 ? 16.833  ops/ms
EishayParseBinaryAutoType.hessian                        thrpt    5   260.831 ?  0.966  ops/ms
EishayParseBinaryAutoType.javaSerialize                  thrpt    5    53.998 ?  0.457  ops/ms
EishayParseString.fastjson1                              thrpt    5  1267.555 ?  4.686  ops/ms
EishayParseString.fastjson2                              thrpt    5  1289.502 ?  4.843  ops/ms
EishayParseString.gson                                   thrpt    5   461.859 ?  1.903  ops/ms
EishayParseString.jackson                                thrpt    5   495.332 ?  5.188  ops/ms
EishayParseStringPretty.fastjson1                        thrpt    5   299.046 ?  2.765  ops/ms
EishayParseStringPretty.fastjson2                        thrpt    5   916.203 ?  4.401  ops/ms
EishayParseStringPretty.gson                             thrpt    5   432.176 ?  3.927  ops/ms
EishayParseStringPretty.jackson                          thrpt    5   455.607 ?  1.438  ops/ms
EishayParseTreeString.fastjson1                          thrpt    5   560.349 ?  3.163  ops/ms
EishayParseTreeString.fastjson2                          thrpt    5  1051.942 ?  8.909  ops/ms
EishayParseTreeString.gson                               thrpt    5   330.198 ?  1.596  ops/ms
EishayParseTreeString.jackson                            thrpt    5   526.592 ?  2.842  ops/ms
EishayParseTreeStringPretty.fastjson1                    thrpt    5   474.911 ?  5.696  ops/ms
EishayParseTreeStringPretty.fastjson2                    thrpt    5   801.232 ?  2.712  ops/ms
EishayParseTreeStringPretty.gson                         thrpt    5   305.488 ?  1.786  ops/ms
EishayParseTreeStringPretty.jackson                      thrpt    5   453.744 ?  2.904  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                       thrpt    5   492.125 ?  5.020  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                       thrpt    5  1054.996 ?  7.818  ops/ms
EishayParseTreeUTF8Bytes.gson                            thrpt    5   315.102 ?  2.565  ops/ms
EishayParseTreeUTF8Bytes.jackson                         thrpt    5   562.764 ?  5.422  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1                 thrpt    5   417.998 ?  0.926  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2                 thrpt    5   802.899 ?  1.779  ops/ms
EishayParseTreeUTF8BytesPretty.gson                      thrpt    5   301.975 ?  1.609  ops/ms
EishayParseTreeUTF8BytesPretty.jackson                   thrpt    5   523.965 ?  4.300  ops/ms
EishayParseUTF8Bytes.dsljson                             thrpt    5   828.617 ?  6.545  ops/ms
EishayParseUTF8Bytes.fastjson1                           thrpt    5   958.636 ?  5.511  ops/ms
EishayParseUTF8Bytes.fastjson2                           thrpt    5  1321.512 ?  6.979  ops/ms
EishayParseUTF8Bytes.gson                                thrpt    5   318.736 ?  1.344  ops/ms
EishayParseUTF8Bytes.jackson                             thrpt    5   555.918 ?  2.024  ops/ms
EishayParseUTF8BytesPretty.fastjson1                     thrpt    5   266.756 ?  1.345  ops/ms
EishayParseUTF8BytesPretty.fastjson2                     thrpt    5   942.583 ?  7.341  ops/ms
EishayParseUTF8BytesPretty.gson                          thrpt    5   306.357 ?  3.121  ops/ms
EishayParseUTF8BytesPretty.jackson                       thrpt    5   486.605 ?  2.328  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes                     thrpt    5  1906.714 ? 15.918  ops/ms
EishayWriteBinary.hessian                                thrpt    5   323.899 ?  1.171  ops/ms
EishayWriteBinary.javaSerialize                          thrpt    5   231.968 ?  0.516  ops/ms
EishayWriteBinary.jsonb                                  thrpt    5  3364.353 ? 40.292  ops/ms
EishayWriteBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   711.077 ?  5.695  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  2188.213 ? 19.522  ops/ms
EishayWriteBinaryArrayMapping.jsonb                      thrpt    5  5527.278 ? 94.301  ops/ms
EishayWriteBinaryArrayMapping.kryo                       thrpt    5  2043.990 ? 25.048  ops/ms
EishayWriteBinaryArrayMapping.protobuf                   thrpt    5  1750.990 ?  8.583  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB                 thrpt    5  1693.082 ? 13.030  ops/ms
EishayWriteBinaryAutoType.fastjson2UTF8Bytes             thrpt    5  1815.614 ? 12.602  ops/ms
EishayWriteBinaryAutoType.hessian                        thrpt    5   318.398 ?  1.037  ops/ms
EishayWriteBinaryAutoType.javaSerialize                  thrpt    5   222.138 ?  1.301  ops/ms
EishayWriteString.fastjson1                              thrpt    5   618.883 ?  4.038  ops/ms
EishayWriteString.fastjson2                              thrpt    5  1665.650 ?  7.981  ops/ms
EishayWriteString.gson                                   thrpt    5   243.952 ?  1.066  ops/ms
EishayWriteString.jackson                                thrpt    5  1034.405 ?  7.929  ops/ms
EishayWriteStringTree.fastjson1                          thrpt    5   796.732 ?  6.091  ops/ms
EishayWriteStringTree.fastjson2                          thrpt    5  1183.837 ?  3.243  ops/ms
EishayWriteStringTree.gson                               thrpt    5   247.782 ?  2.465  ops/ms
EishayWriteStringTree.jackson                            thrpt    5   861.244 ?  6.093  ops/ms
EishayWriteStringTree1x.fastjson1                        thrpt    5   708.191 ?  8.738  ops/ms
EishayWriteStringTree1x.fastjson2                        thrpt    5   908.416 ? 10.283  ops/ms
EishayWriteStringTree1x.gson                             thrpt    5   240.884 ?  1.652  ops/ms
EishayWriteStringTree1x.jackson                          thrpt    5   771.891 ?  6.652  ops/ms
EishayWriteUTF8Bytes.fastjson1                           thrpt    5   570.384 ?  3.273  ops/ms
EishayWriteUTF8Bytes.fastjson2                           thrpt    5  1933.337 ? 25.981  ops/ms
EishayWriteUTF8Bytes.gson                                thrpt    5   242.848 ?  1.678  ops/ms
EishayWriteUTF8Bytes.jackson                             thrpt    5   995.436 ?  7.591  ops/ms
```
# ecs.c7.xlarge-graalvm-ce-java11-22.3.1
```java
Benchmark                                                 Mode  Cnt     Score     Error   Units
EishayParseBinary.fastjson2UTF8Bytes                     thrpt    5  1184.769 ?  17.048  ops/ms
EishayParseBinary.hessian                                thrpt    5   254.207 ?   3.128  ops/ms
EishayParseBinary.javaSerialize                          thrpt    5    49.569 ?   0.672  ops/ms
EishayParseBinary.jsonb                                  thrpt    5  2811.501 ?  24.826  ops/ms
EishayParseBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5  1501.910 ?  40.417  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  2144.841 ?  26.408  ops/ms
EishayParseBinaryArrayMapping.jsonb                      thrpt    5  4612.274 ?   8.976  ops/ms
EishayParseBinaryArrayMapping.kryo                       thrpt    5  1727.200 ?  10.543  ops/ms
EishayParseBinaryArrayMapping.protobuf                   thrpt    5  1635.471 ?  41.750  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB                 thrpt    5  1946.455 ?  13.186  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB_autoTypeFilter  thrpt    5  1819.101 ?  25.611  ops/ms
EishayParseBinaryAutoType.hessian                        thrpt    5   244.481 ?   1.880  ops/ms
EishayParseBinaryAutoType.javaSerialize                  thrpt    5    48.710 ?   0.568  ops/ms
EishayParseString.fastjson1                              thrpt    5  1108.730 ?   7.918  ops/ms
EishayParseString.fastjson2                              thrpt    5  1187.719 ?  16.457  ops/ms
EishayParseString.gson                                   thrpt    5   506.639 ?   4.522  ops/ms
EishayParseString.jackson                                thrpt    5   510.052 ?   1.086  ops/ms
EishayParseStringPretty.fastjson1                        thrpt    5   307.326 ?   6.771  ops/ms
EishayParseStringPretty.fastjson2                        thrpt    5   860.291 ?   8.549  ops/ms
EishayParseStringPretty.gson                             thrpt    5   451.798 ?  10.466  ops/ms
EishayParseStringPretty.jackson                          thrpt    5   458.983 ?   5.610  ops/ms
EishayParseTreeString.fastjson1                          thrpt    5   500.731 ?   3.191  ops/ms
EishayParseTreeString.fastjson2                          thrpt    5   907.201 ?  11.864  ops/ms
EishayParseTreeString.gson                               thrpt    5   432.917 ?   3.663  ops/ms
EishayParseTreeString.jackson                            thrpt    5   527.568 ?   3.081  ops/ms
EishayParseTreeStringPretty.fastjson1                    thrpt    5   419.364 ?   5.421  ops/ms
EishayParseTreeStringPretty.fastjson2                    thrpt    5   749.489 ?   3.329  ops/ms
EishayParseTreeStringPretty.gson                         thrpt    5   393.005 ?   3.184  ops/ms
EishayParseTreeStringPretty.jackson                      thrpt    5   495.070 ?   5.586  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                       thrpt    5   436.961 ?   8.635  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                       thrpt    5   867.483 ?   6.961  ops/ms
EishayParseTreeUTF8Bytes.gson                            thrpt    5   405.236 ?   0.787  ops/ms
EishayParseTreeUTF8Bytes.jackson                         thrpt    5   500.877 ?   3.723  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1                 thrpt    5   364.766 ?   2.889  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2                 thrpt    5   692.591 ?   7.991  ops/ms
EishayParseTreeUTF8BytesPretty.gson                      thrpt    5   369.764 ?   2.662  ops/ms
EishayParseTreeUTF8BytesPretty.jackson                   thrpt    5   458.238 ?   2.062  ops/ms
EishayParseUTF8Bytes.dsljson                             thrpt    5   908.666 ?  37.690  ops/ms
EishayParseUTF8Bytes.fastjson1                           thrpt    5   789.996 ?   9.761  ops/ms
EishayParseUTF8Bytes.fastjson2                           thrpt    5  1108.370 ?  17.986  ops/ms
EishayParseUTF8Bytes.gson                                thrpt    5   406.713 ?   5.223  ops/ms
EishayParseUTF8Bytes.jackson                             thrpt    5   561.613 ?   8.983  ops/ms
EishayParseUTF8BytesPretty.fastjson1                     thrpt    5   265.881 ?   9.895  ops/ms
EishayParseUTF8BytesPretty.fastjson2                     thrpt    5   848.943 ?  15.544  ops/ms
EishayParseUTF8BytesPretty.gson                          thrpt    5   366.860 ?   6.162  ops/ms
EishayParseUTF8BytesPretty.jackson                       thrpt    5   487.269 ?   5.992  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes                     thrpt    5  1764.133 ?  29.348  ops/ms
EishayWriteBinary.hessian                                thrpt    5   394.908 ?   3.156  ops/ms
EishayWriteBinary.javaSerialize                          thrpt    5   222.606 ?   4.092  ops/ms
EishayWriteBinary.jsonb                                  thrpt    5  2824.016 ?  40.418  ops/ms
EishayWriteBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   721.079 ?   7.933  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  2297.881 ?  29.314  ops/ms
EishayWriteBinaryArrayMapping.jsonb                      thrpt    5  5931.127 ? 101.903  ops/ms
EishayWriteBinaryArrayMapping.kryo                       thrpt    5  2140.558 ?  16.639  ops/ms
EishayWriteBinaryArrayMapping.protobuf                   thrpt    5  1880.294 ?  10.734  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB                 thrpt    5  1396.239 ?  20.001  ops/ms
EishayWriteBinaryAutoType.fastjson2UTF8Bytes             thrpt    5  1751.419 ?  25.759  ops/ms
EishayWriteBinaryAutoType.hessian                        thrpt    5   394.779 ?   5.656  ops/ms
EishayWriteBinaryAutoType.javaSerialize                  thrpt    5   220.306 ?   2.402  ops/ms
EishayWriteString.fastjson1                              thrpt    5   747.114 ?   7.901  ops/ms
EishayWriteString.fastjson2                              thrpt    5  1606.809 ?  18.492  ops/ms
EishayWriteString.gson                                   thrpt    5   382.962 ?   2.390  ops/ms
EishayWriteString.jackson                                thrpt    5   939.963 ?   7.609  ops/ms
EishayWriteStringTree.fastjson1                          thrpt    5   769.232 ?   5.401  ops/ms
EishayWriteStringTree.fastjson2                          thrpt    5  1453.595 ?  26.040  ops/ms
EishayWriteStringTree.gson                               thrpt    5   428.776 ?   5.020  ops/ms
EishayWriteStringTree.jackson                            thrpt    5  1012.782 ?  11.447  ops/ms
EishayWriteStringTree1x.fastjson1                        thrpt    5   710.589 ?  16.921  ops/ms
EishayWriteStringTree1x.fastjson2                        thrpt    5  1176.939 ?  12.060  ops/ms
EishayWriteStringTree1x.gson                             thrpt    5   394.864 ?  14.143  ops/ms
EishayWriteStringTree1x.jackson                          thrpt    5   821.886 ?   9.810  ops/ms
EishayWriteUTF8Bytes.fastjson1                           thrpt    5   656.170 ?   4.608  ops/ms
EishayWriteUTF8Bytes.fastjson2                           thrpt    5  1746.746 ?   1.440  ops/ms
EishayWriteUTF8Bytes.gson                                thrpt    5   375.456 ?   6.966  ops/ms
EishayWriteUTF8Bytes.jackson                             thrpt    5   890.242 ?   4.839  ops/ms
```
# ecs.c7.xlarge-graalvm-ce-java17-22.3.1
```java
Benchmark                                                 Mode  Cnt     Score    Error   Units
EishayParseBinary.fastjson2UTF8Bytes                     thrpt    5  1303.566 ? 12.004  ops/ms
EishayParseBinary.hessian                                thrpt    5   243.999 ?  0.696  ops/ms
EishayParseBinary.javaSerialize                          thrpt    5    52.265 ?  0.370  ops/ms
EishayParseBinary.jsonb                                  thrpt    5  3385.490 ? 50.924  ops/ms
EishayParseBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5  1652.672 ? 30.257  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  2247.877 ?  8.019  ops/ms
EishayParseBinaryArrayMapping.jsonb                      thrpt    5  5159.294 ? 32.164  ops/ms
EishayParseBinaryArrayMapping.kryo                       thrpt    5  1783.075 ?  9.306  ops/ms
EishayParseBinaryArrayMapping.protobuf                   thrpt    5  1901.215 ? 18.207  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB                 thrpt    5  2163.645 ? 25.683  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB_autoTypeFilter  thrpt    5  1999.622 ? 23.223  ops/ms
EishayParseBinaryAutoType.hessian                        thrpt    5   243.163 ?  1.391  ops/ms
EishayParseBinaryAutoType.javaSerialize                  thrpt    5    53.245 ?  0.264  ops/ms
EishayParseString.fastjson1                              thrpt    5  1435.162 ?  5.011  ops/ms
EishayParseString.fastjson2                              thrpt    5  1237.734 ?  6.608  ops/ms
EishayParseString.gson                                   thrpt    5   508.993 ?  2.724  ops/ms
EishayParseString.jackson                                thrpt    5   509.405 ?  3.254  ops/ms
EishayParseStringPretty.fastjson1                        thrpt    5   383.939 ?  3.257  ops/ms
EishayParseStringPretty.fastjson2                        thrpt    5   925.208 ?  2.860  ops/ms
EishayParseStringPretty.gson                             thrpt    5   432.933 ?  4.307  ops/ms
EishayParseStringPretty.jackson                          thrpt    5   486.794 ?  2.779  ops/ms
EishayParseTreeString.fastjson1                          thrpt    5   663.720 ?  3.424  ops/ms
EishayParseTreeString.fastjson2                          thrpt    5  1008.033 ? 11.366  ops/ms
EishayParseTreeString.gson                               thrpt    5   443.003 ?  3.367  ops/ms
EishayParseTreeString.jackson                            thrpt    5   514.445 ?  3.016  ops/ms
EishayParseTreeStringPretty.fastjson1                    thrpt    5   546.104 ?  2.489  ops/ms
EishayParseTreeStringPretty.fastjson2                    thrpt    5   778.815 ?  2.776  ops/ms
EishayParseTreeStringPretty.gson                         thrpt    5   404.169 ?  7.454  ops/ms
EishayParseTreeStringPretty.jackson                      thrpt    5   478.078 ?  3.710  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                       thrpt    5   545.737 ?  2.790  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                       thrpt    5   937.904 ? 12.189  ops/ms
EishayParseTreeUTF8Bytes.gson                            thrpt    5   421.385 ?  3.841  ops/ms
EishayParseTreeUTF8Bytes.jackson                         thrpt    5   533.171 ?  6.930  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1                 thrpt    5   447.496 ?  1.998  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2                 thrpt    5   733.720 ?  4.967  ops/ms
EishayParseTreeUTF8BytesPretty.gson                      thrpt    5   376.896 ?  2.901  ops/ms
EishayParseTreeUTF8BytesPretty.jackson                   thrpt    5   506.329 ?  6.086  ops/ms
EishayParseUTF8Bytes.dsljson                             thrpt    5   587.961 ? 15.759  ops/ms
EishayParseUTF8Bytes.fastjson1                           thrpt    5   942.844 ?  1.496  ops/ms
EishayParseUTF8Bytes.fastjson2                           thrpt    5  1212.540 ?  8.711  ops/ms
EishayParseUTF8Bytes.gson                                thrpt    5   410.623 ?  3.429  ops/ms
EishayParseUTF8Bytes.jackson                             thrpt    5   595.836 ?  3.502  ops/ms
EishayParseUTF8BytesPretty.fastjson1                     thrpt    5   316.047 ?  5.235  ops/ms
EishayParseUTF8BytesPretty.fastjson2                     thrpt    5   888.739 ? 13.250  ops/ms
EishayParseUTF8BytesPretty.gson                          thrpt    5   373.487 ?  1.998  ops/ms
EishayParseUTF8BytesPretty.jackson                       thrpt    5   520.818 ?  3.809  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes                     thrpt    5  1991.533 ? 25.019  ops/ms
EishayWriteBinary.hessian                                thrpt    5   400.015 ?  1.925  ops/ms
EishayWriteBinary.javaSerialize                          thrpt    5   227.114 ?  2.096  ops/ms
EishayWriteBinary.jsonb                                  thrpt    5  3455.562 ? 28.382  ops/ms
EishayWriteBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   772.735 ?  4.735  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  2519.844 ? 19.925  ops/ms
EishayWriteBinaryArrayMapping.jsonb                      thrpt    5  7235.884 ? 72.063  ops/ms
EishayWriteBinaryArrayMapping.kryo                       thrpt    5  2227.488 ? 79.545  ops/ms
EishayWriteBinaryArrayMapping.protobuf                   thrpt    5  2147.349 ?  9.101  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB                 thrpt    5  1595.988 ? 13.171  ops/ms
EishayWriteBinaryAutoType.fastjson2UTF8Bytes             thrpt    5  1937.926 ? 25.319  ops/ms
EishayWriteBinaryAutoType.hessian                        thrpt    5   403.981 ?  4.341  ops/ms
EishayWriteBinaryAutoType.javaSerialize                  thrpt    5   230.418 ?  0.237  ops/ms
EishayWriteString.fastjson1                              thrpt    5   822.504 ?  6.786  ops/ms
EishayWriteString.fastjson2                              thrpt    5  1808.327 ? 15.627  ops/ms
EishayWriteString.gson                                   thrpt    5   243.883 ?  0.355  ops/ms
EishayWriteString.jackson                                thrpt    5   993.322 ? 14.498  ops/ms
EishayWriteStringTree.fastjson1                          thrpt    5   850.943 ? 10.245  ops/ms
EishayWriteStringTree.fastjson2                          thrpt    5  1426.993 ? 32.325  ops/ms
EishayWriteStringTree.gson                               thrpt    5   247.528 ?  1.101  ops/ms
EishayWriteStringTree.jackson                            thrpt    5   993.654 ?  9.196  ops/ms
EishayWriteStringTree1x.fastjson1                        thrpt    5   698.230 ?  5.709  ops/ms
EishayWriteStringTree1x.fastjson2                        thrpt    5  1208.164 ? 12.782  ops/ms
EishayWriteStringTree1x.gson                             thrpt    5   242.363 ?  2.669  ops/ms
EishayWriteStringTree1x.jackson                          thrpt    5   865.214 ?  8.115  ops/ms
EishayWriteUTF8Bytes.fastjson1                           thrpt    5   704.270 ?  4.202  ops/ms
EishayWriteUTF8Bytes.fastjson2                           thrpt    5  2013.978 ?  6.873  ops/ms
EishayWriteUTF8Bytes.gson                                thrpt    5   235.180 ?  1.345  ops/ms
EishayWriteUTF8Bytes.jackson                             thrpt    5  1027.116 ? 10.737  ops/ms
```
# ecs.c7.xlarge-graalvm-ee-java11-22.3.1
```java
Benchmark                                                 Mode  Cnt     Score    Error   Units
EishayParseBinary.fastjson2UTF8Bytes                     thrpt    5  1558.493 ? 22.725  ops/ms
EishayParseBinary.hessian                                thrpt    5   391.618 ?  3.240  ops/ms
EishayParseBinary.javaSerialize                          thrpt    5    51.438 ?  0.542  ops/ms
EishayParseBinary.jsonb                                  thrpt    5  3575.943 ? 56.264  ops/ms
EishayParseBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5  1862.409 ? 14.679  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  2441.924 ? 22.043  ops/ms
EishayParseBinaryArrayMapping.jsonb                      thrpt    5  5056.801 ? 75.751  ops/ms
EishayParseBinaryArrayMapping.kryo                       thrpt    5  1821.129 ? 15.261  ops/ms
EishayParseBinaryArrayMapping.protobuf                   thrpt    5  2176.537 ? 29.228  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB                 thrpt    5  2497.174 ? 18.623  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB_autoTypeFilter  thrpt    5  2366.851 ? 52.051  ops/ms
EishayParseBinaryAutoType.hessian                        thrpt    5   340.038 ?  4.683  ops/ms
EishayParseBinaryAutoType.javaSerialize                  thrpt    5    52.586 ?  0.674  ops/ms
EishayParseString.fastjson1                              thrpt    5  1181.318 ?  8.902  ops/ms
EishayParseString.fastjson2                              thrpt    5  1498.304 ? 15.439  ops/ms
EishayParseString.gson                                   thrpt    5   482.799 ?  4.953  ops/ms
EishayParseString.jackson                                thrpt    5   493.889 ?  3.819  ops/ms
EishayParseStringPretty.fastjson1                        thrpt    5   319.552 ?  6.841  ops/ms
EishayParseStringPretty.fastjson2                        thrpt    5   984.215 ? 29.432  ops/ms
EishayParseStringPretty.gson                             thrpt    5   446.381 ?  3.931  ops/ms
EishayParseStringPretty.jackson                          thrpt    5   440.014 ?  8.905  ops/ms
EishayParseTreeString.fastjson1                          thrpt    5   599.252 ?  4.490  ops/ms
EishayParseTreeString.fastjson2                          thrpt    5  1010.722 ? 13.694  ops/ms
EishayParseTreeString.gson                               thrpt    5   402.950 ?  5.028  ops/ms
EishayParseTreeString.jackson                            thrpt    5   555.966 ?  7.181  ops/ms
EishayParseTreeStringPretty.fastjson1                    thrpt    5   461.906 ?  2.246  ops/ms
EishayParseTreeStringPretty.fastjson2                    thrpt    5   849.290 ?  8.786  ops/ms
EishayParseTreeStringPretty.gson                         thrpt    5   406.557 ?  4.532  ops/ms
EishayParseTreeStringPretty.jackson                      thrpt    5   509.928 ? 15.641  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                       thrpt    5   504.376 ?  8.010  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                       thrpt    5  1017.496 ?  7.072  ops/ms
EishayParseTreeUTF8Bytes.gson                            thrpt    5   411.491 ?  4.566  ops/ms
EishayParseTreeUTF8Bytes.jackson                         thrpt    5   609.771 ?  4.193  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1                 thrpt    5   396.391 ?  4.075  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2                 thrpt    5   830.264 ? 11.812  ops/ms
EishayParseTreeUTF8BytesPretty.gson                      thrpt    5   379.334 ?  4.446  ops/ms
EishayParseTreeUTF8BytesPretty.jackson                   thrpt    5   544.211 ?  5.104  ops/ms
EishayParseUTF8Bytes.dsljson                             thrpt    5   954.264 ?  2.669  ops/ms
EishayParseUTF8Bytes.fastjson1                           thrpt    5   849.153 ?  9.069  ops/ms
EishayParseUTF8Bytes.fastjson2                           thrpt    5  1390.966 ? 11.540  ops/ms
EishayParseUTF8Bytes.gson                                thrpt    5   405.754 ?  7.523  ops/ms
EishayParseUTF8Bytes.jackson                             thrpt    5   527.476 ?  6.472  ops/ms
EishayParseUTF8BytesPretty.fastjson1                     thrpt    5   285.819 ? 10.465  ops/ms
EishayParseUTF8BytesPretty.fastjson2                     thrpt    5   981.557 ?  4.888  ops/ms
EishayParseUTF8BytesPretty.gson                          thrpt    5   383.606 ?  2.140  ops/ms
EishayParseUTF8BytesPretty.jackson                       thrpt    5   481.861 ?  1.868  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes                     thrpt    5  1956.391 ? 23.336  ops/ms
EishayWriteBinary.hessian                                thrpt    5   475.175 ?  7.210  ops/ms
EishayWriteBinary.javaSerialize                          thrpt    5   244.951 ?  6.457  ops/ms
EishayWriteBinary.jsonb                                  thrpt    5  3884.551 ? 61.292  ops/ms
EishayWriteBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   821.597 ?  7.302  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  2541.036 ? 22.052  ops/ms
EishayWriteBinaryArrayMapping.jsonb                      thrpt    5  8875.049 ? 79.316  ops/ms
EishayWriteBinaryArrayMapping.kryo                       thrpt    5  2469.945 ? 20.587  ops/ms
EishayWriteBinaryArrayMapping.protobuf                   thrpt    5  3416.691 ? 21.400  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB                 thrpt    5  1835.485 ? 19.939  ops/ms
EishayWriteBinaryAutoType.fastjson2UTF8Bytes             thrpt    5  1869.377 ? 19.141  ops/ms
EishayWriteBinaryAutoType.hessian                        thrpt    5   477.947 ?  3.760  ops/ms
EishayWriteBinaryAutoType.javaSerialize                  thrpt    5   241.992 ?  2.605  ops/ms
EishayWriteString.fastjson1                              thrpt    5   765.314 ? 10.044  ops/ms
EishayWriteString.fastjson2                              thrpt    5  1869.074 ? 17.094  ops/ms
EishayWriteString.gson                                   thrpt    5   348.469 ?  5.288  ops/ms
EishayWriteString.jackson                                thrpt    5   964.817 ?  8.722  ops/ms
EishayWriteStringTree.fastjson1                          thrpt    5   821.212 ? 10.768  ops/ms
EishayWriteStringTree.fastjson2                          thrpt    5  1410.312 ? 15.035  ops/ms
EishayWriteStringTree.gson                               thrpt    5   380.692 ?  1.048  ops/ms
EishayWriteStringTree.jackson                            thrpt    5   929.050 ? 25.257  ops/ms
EishayWriteStringTree1x.fastjson1                        thrpt    5   879.010 ? 28.363  ops/ms
EishayWriteStringTree1x.fastjson2                        thrpt    5  1330.073 ? 15.462  ops/ms
EishayWriteStringTree1x.gson                             thrpt    5   376.218 ?  2.542  ops/ms
EishayWriteStringTree1x.jackson                          thrpt    5   890.809 ? 22.246  ops/ms
EishayWriteUTF8Bytes.fastjson1                           thrpt    5   662.968 ?  6.445  ops/ms
EishayWriteUTF8Bytes.fastjson2                           thrpt    5  1905.032 ? 10.583  ops/ms
EishayWriteUTF8Bytes.gson                                thrpt    5   346.769 ?  1.163  ops/ms
EishayWriteUTF8Bytes.jackson                             thrpt    5  1019.631 ?  6.618  ops/ms
```
# ecs.c7.xlarge-graalvm-ee-java17-22.3.1
```java
Benchmark                                                 Mode  Cnt     Score     Error   Units
EishayParseBinary.fastjson2UTF8Bytes                     thrpt    5  1330.645 ?  14.986  ops/ms
EishayParseBinary.hessian                                thrpt    5   380.165 ?   1.850  ops/ms
EishayParseBinary.javaSerialize                          thrpt    5    57.698 ?   0.876  ops/ms
EishayParseBinary.jsonb                                  thrpt    5  3487.341 ?  26.063  ops/ms
EishayParseBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5  1743.441 ?  13.783  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  2233.599 ?   5.237  ops/ms
EishayParseBinaryArrayMapping.jsonb                      thrpt    5  6698.250 ? 102.848  ops/ms
EishayParseBinaryArrayMapping.kryo                       thrpt    5  1817.954 ?  14.247  ops/ms
EishayParseBinaryArrayMapping.protobuf                   thrpt    5  2590.939 ?  45.327  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB                 thrpt    5  2173.272 ?  15.813  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB_autoTypeFilter  thrpt    5  2126.769 ?  32.531  ops/ms
EishayParseBinaryAutoType.hessian                        thrpt    5   406.181 ?   4.548  ops/ms
EishayParseBinaryAutoType.javaSerialize                  thrpt    5    56.769 ?   0.402  ops/ms
EishayParseString.fastjson1                              thrpt    5  1725.838 ?  11.090  ops/ms
EishayParseString.fastjson2                              thrpt    5  1360.456 ?  12.011  ops/ms
EishayParseString.gson                                   thrpt    5   467.035 ?   4.192  ops/ms
EishayParseString.jackson                                thrpt    5   494.252 ?   1.537  ops/ms
EishayParseStringPretty.fastjson1                        thrpt    5   400.241 ?   4.210  ops/ms
EishayParseStringPretty.fastjson2                        thrpt    5   986.227 ?   8.559  ops/ms
EishayParseStringPretty.gson                             thrpt    5   432.537 ?   3.124  ops/ms
EishayParseStringPretty.jackson                          thrpt    5   430.902 ?   3.598  ops/ms
EishayParseTreeString.fastjson1                          thrpt    5   689.876 ?   7.771  ops/ms
EishayParseTreeString.fastjson2                          thrpt    5  1093.144 ?   5.351  ops/ms
EishayParseTreeString.gson                               thrpt    5   411.828 ?   2.353  ops/ms
EishayParseTreeString.jackson                            thrpt    5   550.792 ?   7.956  ops/ms
EishayParseTreeStringPretty.fastjson1                    thrpt    5   561.201 ?   3.911  ops/ms
EishayParseTreeStringPretty.fastjson2                    thrpt    5   887.544 ?   6.793  ops/ms
EishayParseTreeStringPretty.gson                         thrpt    5   390.197 ?   3.957  ops/ms
EishayParseTreeStringPretty.jackson                      thrpt    5   484.315 ?   4.167  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                       thrpt    5   573.940 ?   3.212  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                       thrpt    5  1023.826 ?   6.089  ops/ms
EishayParseTreeUTF8Bytes.gson                            thrpt    5   413.525 ?   2.622  ops/ms
EishayParseTreeUTF8Bytes.jackson                         thrpt    5   583.954 ?   3.414  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1                 thrpt    5   455.361 ?   2.685  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2                 thrpt    5   868.931 ?   6.948  ops/ms
EishayParseTreeUTF8BytesPretty.gson                      thrpt    5   399.732 ?   2.682  ops/ms
EishayParseTreeUTF8BytesPretty.jackson                   thrpt    5   578.137 ?   1.922  ops/ms
EishayParseUTF8Bytes.dsljson                             thrpt    5   886.295 ?   4.201  ops/ms
EishayParseUTF8Bytes.fastjson1                           thrpt    5  1112.657 ?   5.994  ops/ms
EishayParseUTF8Bytes.fastjson2                           thrpt    5  1360.851 ?   9.898  ops/ms
EishayParseUTF8Bytes.gson                                thrpt    5   423.122 ?   2.237  ops/ms
EishayParseUTF8Bytes.jackson                             thrpt    5   560.240 ?   2.703  ops/ms
EishayParseUTF8BytesPretty.fastjson1                     thrpt    5   350.804 ?   2.875  ops/ms
EishayParseUTF8BytesPretty.fastjson2                     thrpt    5   931.994 ?   9.951  ops/ms
EishayParseUTF8BytesPretty.gson                          thrpt    5   415.365 ?   3.954  ops/ms
EishayParseUTF8BytesPretty.jackson                       thrpt    5   504.521 ?   4.270  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes                     thrpt    5  1958.589 ?   8.385  ops/ms
EishayWriteBinary.hessian                                thrpt    5   486.293 ?   4.139  ops/ms
EishayWriteBinary.javaSerialize                          thrpt    5   239.181 ?   1.644  ops/ms
EishayWriteBinary.jsonb                                  thrpt    5  4103.772 ?  21.946  ops/ms
EishayWriteBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   869.275 ?   7.149  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  2595.885 ?  25.909  ops/ms
EishayWriteBinaryArrayMapping.jsonb                      thrpt    5  9384.563 ?  40.727  ops/ms
EishayWriteBinaryArrayMapping.kryo                       thrpt    5  2498.557 ?  24.882  ops/ms
EishayWriteBinaryArrayMapping.protobuf                   thrpt    5  3407.475 ?  17.149  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB                 thrpt    5  1888.250 ?  24.304  ops/ms
EishayWriteBinaryAutoType.fastjson2UTF8Bytes             thrpt    5  1935.391 ?  13.835  ops/ms
EishayWriteBinaryAutoType.hessian                        thrpt    5   484.813 ?   7.520  ops/ms
EishayWriteBinaryAutoType.javaSerialize                  thrpt    5   239.056 ?   2.327  ops/ms
EishayWriteString.fastjson1                              thrpt    5   778.555 ?   5.720  ops/ms
EishayWriteString.fastjson2                              thrpt    5  1862.843 ?  15.981  ops/ms
EishayWriteString.gson                                   thrpt    5   245.770 ?   1.797  ops/ms
EishayWriteString.jackson                                thrpt    5  1014.458 ?  10.375  ops/ms
EishayWriteStringTree.fastjson1                          thrpt    5   923.037 ?   9.197  ops/ms
EishayWriteStringTree.fastjson2                          thrpt    5  1468.635 ?  15.743  ops/ms
EishayWriteStringTree.gson                               thrpt    5   248.857 ?   2.600  ops/ms
EishayWriteStringTree.jackson                            thrpt    5   955.151 ?   6.263  ops/ms
EishayWriteStringTree1x.fastjson1                        thrpt    5   906.413 ?   4.903  ops/ms
EishayWriteStringTree1x.fastjson2                        thrpt    5  1348.396 ?   9.836  ops/ms
EishayWriteStringTree1x.gson                             thrpt    5   247.302 ?   2.473  ops/ms
EishayWriteStringTree1x.jackson                          thrpt    5   947.648 ?   9.436  ops/ms
EishayWriteUTF8Bytes.fastjson1                           thrpt    5   757.226 ?   2.985  ops/ms
EishayWriteUTF8Bytes.fastjson2                           thrpt    5  1968.037 ?  15.608  ops/ms
EishayWriteUTF8Bytes.gson                                thrpt    5   241.234 ?   1.195  ops/ms
EishayWriteUTF8Bytes.jackson                             thrpt    5  1077.563 ?   7.002  ops/ms
```

# ecs.g8m.xlarge-jdk1.8.0_341_aarch64
```java
Benchmark                                                 Mode  Cnt     Score     Error   Units
EishayParseBinary.fastjson2UTF8Bytes                     thrpt    5   830.694 ?  20.115  ops/ms
EishayParseBinary.hessian                                thrpt    5   203.359 ?   4.986  ops/ms
EishayParseBinary.javaSerialize                          thrpt    5    41.880 ?   0.895  ops/ms
EishayParseBinary.jsonb                                  thrpt    5  1356.444 ?  80.252  ops/ms
EishayParseBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5  1104.528 ?  29.366  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  1159.478 ?  50.491  ops/ms
EishayParseBinaryArrayMapping.jsonb                      thrpt    5  2380.074 ?  98.420  ops/ms
EishayParseBinaryArrayMapping.kryo                       thrpt    5  1246.425 ? 104.122  ops/ms
EishayParseBinaryArrayMapping.protobuf                   thrpt    5   741.164 ?  56.054  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB                 thrpt    5  1197.997 ?  64.496  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB_autoTypeFilter  thrpt    5  1079.510 ?  53.642  ops/ms
EishayParseBinaryAutoType.hessian                        thrpt    5   221.253 ?  11.061  ops/ms
EishayParseBinaryAutoType.javaSerialize                  thrpt    5    42.356 ?   0.531  ops/ms
EishayParseString.fastjson1                              thrpt    5   741.597 ?  18.958  ops/ms
EishayParseString.fastjson2                              thrpt    5   877.338 ?  26.670  ops/ms
EishayParseString.gson                                   thrpt    5   346.236 ?  12.272  ops/ms
EishayParseString.jackson                                thrpt    5   362.676 ?  20.370  ops/ms
EishayParseStringPretty.fastjson1                        thrpt    5   213.283 ?   5.829  ops/ms
EishayParseStringPretty.fastjson2                        thrpt    5   692.936 ?  22.977  ops/ms
EishayParseStringPretty.gson                             thrpt    5   320.859 ?   7.205  ops/ms
EishayParseStringPretty.jackson                          thrpt    5   334.575 ?  13.018  ops/ms
EishayParseTreeString.fastjson1                          thrpt    5   311.489 ?  10.260  ops/ms
EishayParseTreeString.fastjson2                          thrpt    5   541.912 ?  49.077  ops/ms
EishayParseTreeString.gson                               thrpt    5   260.213 ?   6.280  ops/ms
EishayParseTreeString.jackson                            thrpt    5   285.566 ?  14.848  ops/ms
EishayParseTreeStringPretty.fastjson1                    thrpt    5   285.338 ?  13.790  ops/ms
EishayParseTreeStringPretty.fastjson2                    thrpt    5   485.775 ?  21.167  ops/ms
EishayParseTreeStringPretty.gson                         thrpt    5   240.524 ?   5.756  ops/ms
EishayParseTreeStringPretty.jackson                      thrpt    5   277.168 ?  19.071  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                       thrpt    5   258.152 ?   8.789  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                       thrpt    5   522.795 ?  51.528  ops/ms
EishayParseTreeUTF8Bytes.gson                            thrpt    5   229.899 ?   8.228  ops/ms
EishayParseTreeUTF8Bytes.jackson                         thrpt    5   306.973 ?   9.600  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1                 thrpt    5   228.606 ?   3.163  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2                 thrpt    5   456.749 ?  18.338  ops/ms
EishayParseTreeUTF8BytesPretty.gson                      thrpt    5   219.897 ?   7.021  ops/ms
EishayParseTreeUTF8BytesPretty.jackson                   thrpt    5   295.959 ?  23.838  ops/ms
EishayParseUTF8Bytes.dsljson                             thrpt    5   582.611 ?  24.425  ops/ms
EishayParseUTF8Bytes.fastjson1                           thrpt    5   665.914 ?   8.534  ops/ms
EishayParseUTF8Bytes.fastjson2                           thrpt    5   793.331 ?  32.540  ops/ms
EishayParseUTF8Bytes.gson                                thrpt    5   234.937 ?   8.389  ops/ms
EishayParseUTF8Bytes.jackson                             thrpt    5   405.807 ?  19.263  ops/ms
EishayParseUTF8BytesPretty.fastjson1                     thrpt    5   205.710 ?   8.714  ops/ms
EishayParseUTF8BytesPretty.fastjson2                     thrpt    5   652.589 ?  19.142  ops/ms
EishayParseUTF8BytesPretty.gson                          thrpt    5   220.865 ?   7.124  ops/ms
EishayParseUTF8BytesPretty.jackson                       thrpt    5   370.551 ?  15.112  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes                     thrpt    5  1321.168 ?  15.564  ops/ms
EishayWriteBinary.hessian                                thrpt    5   350.817 ?   1.811  ops/ms
EishayWriteBinary.javaSerialize                          thrpt    5   207.634 ?   1.945  ops/ms
EishayWriteBinary.jsonb                                  thrpt    5  1697.421 ?  24.718  ops/ms
EishayWriteBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   510.526 ?  11.637  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  1550.548 ?  39.804  ops/ms
EishayWriteBinaryArrayMapping.jsonb                      thrpt    5  2626.723 ?  85.154  ops/ms
EishayWriteBinaryArrayMapping.kryo                       thrpt    5  1443.062 ?  10.261  ops/ms
EishayWriteBinaryArrayMapping.protobuf                   thrpt    5  1078.534 ?  35.189  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB                 thrpt    5  1104.415 ?  14.857  ops/ms
EishayWriteBinaryAutoType.fastjson2UTF8Bytes             thrpt    5  1271.763 ?  27.063  ops/ms
EishayWriteBinaryAutoType.hessian                        thrpt    5   342.973 ?   1.319  ops/ms
EishayWriteBinaryAutoType.javaSerialize                  thrpt    5   206.399 ?   2.062  ops/ms
EishayWriteString.fastjson1                              thrpt    5   527.376 ?   9.406  ops/ms
EishayWriteString.fastjson2                              thrpt    5  1228.957 ?  22.036  ops/ms
EishayWriteString.gson                                   thrpt    5   367.988 ?   4.244  ops/ms
EishayWriteString.jackson                                thrpt    5   638.520 ?   8.618  ops/ms
EishayWriteStringTree.fastjson1                          thrpt    5   578.770 ?  12.946  ops/ms
EishayWriteStringTree.fastjson2                          thrpt    5   923.546 ?  15.796  ops/ms
EishayWriteStringTree.gson                               thrpt    5   406.305 ?   3.750  ops/ms
EishayWriteStringTree.jackson                            thrpt    5   686.736 ?   8.405  ops/ms
EishayWriteStringTree1x.fastjson1                        thrpt    5   546.775 ?  12.565  ops/ms
EishayWriteStringTree1x.fastjson2                        thrpt    5   822.467 ?   6.234  ops/ms
EishayWriteStringTree1x.gson                             thrpt    5   378.015 ?   3.376  ops/ms
EishayWriteStringTree1x.jackson                          thrpt    5   614.439 ?  12.578  ops/ms
EishayWriteUTF8Bytes.fastjson1                           thrpt    5   474.679 ?  11.520  ops/ms
EishayWriteUTF8Bytes.fastjson2                           thrpt    5  1327.630 ?  12.419  ops/ms
EishayWriteUTF8Bytes.gson                                thrpt    5   316.015 ?   2.453  ops/ms
EishayWriteUTF8Bytes.jackson                             thrpt    5   684.044 ?  15.672  ops/ms
```
# ecs.g8m.xlarge-jdk-11.0.16_aarch64
```java
Benchmark                                                 Mode  Cnt     Score     Error   Units
EishayParseBinary.fastjson2UTF8Bytes                     thrpt    5  1159.528 ?   9.007  ops/ms
EishayParseBinary.hessian                                thrpt    5   209.039 ?  10.703  ops/ms
EishayParseBinary.javaSerialize                          thrpt    5    41.064 ?   0.412  ops/ms
EishayParseBinary.jsonb                                  thrpt    5  2086.298 ?   6.286  ops/ms
EishayParseBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5  1270.220 ?   5.871  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  1632.471 ?   3.299  ops/ms
EishayParseBinaryArrayMapping.jsonb                      thrpt    5  3212.304 ?   7.107  ops/ms
EishayParseBinaryArrayMapping.kryo                       thrpt    5  1413.762 ?   1.011  ops/ms
EishayParseBinaryArrayMapping.protobuf                   thrpt    5  1116.099 ?   1.964  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB                 thrpt    5  1603.910 ?   2.401  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB_autoTypeFilter  thrpt    5  1565.631 ?   4.155  ops/ms
EishayParseBinaryAutoType.hessian                        thrpt    5   213.897 ?   2.607  ops/ms
EishayParseBinaryAutoType.javaSerialize                  thrpt    5    41.488 ?   0.618  ops/ms
EishayParseString.fastjson1                              thrpt    5   741.707 ? 185.504  ops/ms
EishayParseString.fastjson2                              thrpt    5  1081.778 ?   9.156  ops/ms
EishayParseString.gson                                   thrpt    5   375.704 ?   7.577  ops/ms
EishayParseString.jackson                                thrpt    5   404.484 ?   0.904  ops/ms
EishayParseStringPretty.fastjson1                        thrpt    5   240.271 ?  11.585  ops/ms
EishayParseStringPretty.fastjson2                        thrpt    5   775.711 ?  10.135  ops/ms
EishayParseStringPretty.gson                             thrpt    5   348.568 ?   1.263  ops/ms
EishayParseStringPretty.jackson                          thrpt    5   376.576 ?  32.710  ops/ms
EishayParseTreeString.fastjson1                          thrpt    5   358.608 ?   2.284  ops/ms
EishayParseTreeString.fastjson2                          thrpt    5   784.292 ?   2.128  ops/ms
EishayParseTreeString.gson                               thrpt    5   253.098 ? 162.477  ops/ms
EishayParseTreeString.jackson                            thrpt    5   356.512 ?  18.712  ops/ms
EishayParseTreeStringPretty.fastjson1                    thrpt    5   285.104 ?   2.862  ops/ms
EishayParseTreeStringPretty.fastjson2                    thrpt    5   631.279 ?   1.777  ops/ms
EishayParseTreeStringPretty.gson                         thrpt    5   280.365 ?   3.982  ops/ms
EishayParseTreeStringPretty.jackson                      thrpt    5   346.970 ?   1.182  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                       thrpt    5   284.538 ?  73.629  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                       thrpt    5   785.431 ?   2.046  ops/ms
EishayParseTreeUTF8Bytes.gson                            thrpt    5   296.272 ?   0.919  ops/ms
EishayParseTreeUTF8Bytes.jackson                         thrpt    5   423.192 ?   0.856  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1                 thrpt    5   231.946 ?  20.698  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2                 thrpt    5   616.450 ?  16.576  ops/ms
EishayParseTreeUTF8BytesPretty.gson                      thrpt    5   282.121 ?   1.154  ops/ms
EishayParseTreeUTF8BytesPretty.jackson                   thrpt    5   391.786 ?   2.863  ops/ms
EishayParseUTF8Bytes.dsljson                             thrpt    5   658.185 ?   2.210  ops/ms
EishayParseUTF8Bytes.fastjson1                           thrpt    5   598.385 ?  58.104  ops/ms
EishayParseUTF8Bytes.fastjson2                           thrpt    5  1086.999 ?   5.244  ops/ms
EishayParseUTF8Bytes.gson                                thrpt    5   296.384 ?   0.801  ops/ms
EishayParseUTF8Bytes.jackson                             thrpt    5   456.233 ?  24.469  ops/ms
EishayParseUTF8BytesPretty.fastjson1                     thrpt    5   226.580 ?   1.372  ops/ms
EishayParseUTF8BytesPretty.fastjson2                     thrpt    5   768.253 ?   5.213  ops/ms
EishayParseUTF8BytesPretty.gson                          thrpt    5   280.342 ?   0.992  ops/ms
EishayParseUTF8BytesPretty.jackson                       thrpt    5   417.357 ?   8.605  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes                     thrpt    5  1407.107 ?  55.061  ops/ms
EishayWriteBinary.hessian                                thrpt    5   348.514 ?   2.553  ops/ms
EishayWriteBinary.javaSerialize                          thrpt    5   221.712 ?   0.442  ops/ms
EishayWriteBinary.jsonb                                  thrpt    5  2170.094 ?  10.994  ops/ms
EishayWriteBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   463.229 ?  97.458  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  1726.566 ?   5.040  ops/ms
EishayWriteBinaryArrayMapping.jsonb                      thrpt    5  4166.081 ?   8.114  ops/ms
EishayWriteBinaryArrayMapping.kryo                       thrpt    5  1525.470 ?   4.005  ops/ms
EishayWriteBinaryArrayMapping.protobuf                   thrpt    5  1398.842 ?  10.225  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB                 thrpt    5  1333.008 ?   3.628  ops/ms
EishayWriteBinaryAutoType.fastjson2UTF8Bytes             thrpt    5  1340.094 ?  23.851  ops/ms
EishayWriteBinaryAutoType.hessian                        thrpt    5   341.067 ?   2.517  ops/ms
EishayWriteBinaryAutoType.javaSerialize                  thrpt    5   221.196 ?   1.039  ops/ms
EishayWriteString.fastjson1                              thrpt    5   498.395 ?  32.094  ops/ms
EishayWriteString.fastjson2                              thrpt    5  1313.549 ?   3.764  ops/ms
EishayWriteString.gson                                   thrpt    5   319.698 ?   6.908  ops/ms
EishayWriteString.jackson                                thrpt    5   508.149 ? 139.916  ops/ms
EishayWriteStringTree.fastjson1                          thrpt    5   565.756 ?  34.316  ops/ms
EishayWriteStringTree.fastjson2                          thrpt    5  1077.641 ?   4.924  ops/ms
EishayWriteStringTree.gson                               thrpt    5   353.722 ?   1.089  ops/ms
EishayWriteStringTree.jackson                            thrpt    5   719.522 ?  10.811  ops/ms
EishayWriteStringTree1x.fastjson1                        thrpt    5   554.449 ?   1.313  ops/ms
EishayWriteStringTree1x.fastjson2                        thrpt    5   924.541 ?  16.409  ops/ms
EishayWriteStringTree1x.gson                             thrpt    5   344.792 ?   1.224  ops/ms
EishayWriteStringTree1x.jackson                          thrpt    5   696.365 ?  12.262  ops/ms
EishayWriteUTF8Bytes.fastjson1                           thrpt    5   479.086 ?   1.015  ops/ms
EishayWriteUTF8Bytes.fastjson2                           thrpt    5  1443.410 ?   6.849  ops/ms
EishayWriteUTF8Bytes.gson                                thrpt    5   323.705 ?   0.461  ops/ms
EishayWriteUTF8Bytes.jackson                             thrpt    5   659.749 ?  11.312  ops/ms
```
# ecs.g8m.xlarge-jdk-17.0.4_aarch64
```java
Benchmark                                                 Mode  Cnt     Score     Error   Units
EishayParseBinary.fastjson2UTF8Bytes                     thrpt    5  1157.091 ?   5.547  ops/ms
EishayParseBinary.hessian                                thrpt    5   235.193 ?   1.255  ops/ms
EishayParseBinary.javaSerialize                          thrpt    5    40.371 ?   1.746  ops/ms
EishayParseBinary.jsonb                                  thrpt    5  2297.308 ?   8.164  ops/ms
EishayParseBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5  1397.294 ?   3.074  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  1657.514 ?  10.849  ops/ms
EishayParseBinaryArrayMapping.jsonb                      thrpt    5  3389.481 ?   4.756  ops/ms
EishayParseBinaryArrayMapping.kryo                       thrpt    5  1461.457 ?  14.684  ops/ms
EishayParseBinaryArrayMapping.protobuf                   thrpt    5  1348.137 ?   3.894  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB                 thrpt    5  1668.870 ?   5.608  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB_autoTypeFilter  thrpt    5  1596.504 ?   6.501  ops/ms
EishayParseBinaryAutoType.hessian                        thrpt    5   220.028 ?  13.028  ops/ms
EishayParseBinaryAutoType.javaSerialize                  thrpt    5    41.345 ?   2.589  ops/ms
EishayParseString.fastjson1                              thrpt    5  1147.901 ?  22.693  ops/ms
EishayParseString.fastjson2                              thrpt    5  1093.084 ?  25.162  ops/ms
EishayParseString.gson                                   thrpt    5   363.786 ?  12.881  ops/ms
EishayParseString.jackson                                thrpt    5   388.002 ?  55.474  ops/ms
EishayParseStringPretty.fastjson1                        thrpt    5   261.491 ?  52.404  ops/ms
EishayParseStringPretty.fastjson2                        thrpt    5   800.644 ?   6.777  ops/ms
EishayParseStringPretty.gson                             thrpt    5   335.130 ?  12.087  ops/ms
EishayParseStringPretty.jackson                          thrpt    5   358.895 ?  59.547  ops/ms
EishayParseTreeString.fastjson1                          thrpt    5   410.028 ?  28.586  ops/ms
EishayParseTreeString.fastjson2                          thrpt    5   788.952 ?  11.116  ops/ms
EishayParseTreeString.gson                               thrpt    5   307.036 ?   1.011  ops/ms
EishayParseTreeString.jackson                            thrpt    5   388.191 ?   2.191  ops/ms
EishayParseTreeStringPretty.fastjson1                    thrpt    5   372.399 ?   2.376  ops/ms
EishayParseTreeStringPretty.fastjson2                    thrpt    5   635.498 ?   4.819  ops/ms
EishayParseTreeStringPretty.gson                         thrpt    5   278.167 ?  14.046  ops/ms
EishayParseTreeStringPretty.jackson                      thrpt    5   351.185 ?   8.546  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                       thrpt    5   290.362 ?  36.014  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                       thrpt    5   793.199 ?   4.181  ops/ms
EishayParseTreeUTF8Bytes.gson                            thrpt    5   275.702 ?  11.041  ops/ms
EishayParseTreeUTF8Bytes.jackson                         thrpt    5   473.790 ?   7.368  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1                 thrpt    5   281.235 ?  24.413  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2                 thrpt    5   622.834 ?   3.760  ops/ms
EishayParseTreeUTF8BytesPretty.gson                      thrpt    5   277.421 ?   4.757  ops/ms
EishayParseTreeUTF8BytesPretty.jackson                   thrpt    5   412.772 ?   2.515  ops/ms
EishayParseUTF8Bytes.dsljson                             thrpt    5   575.938 ?  72.707  ops/ms
EishayParseUTF8Bytes.fastjson1                           thrpt    5   670.278 ?  77.037  ops/ms
EishayParseUTF8Bytes.fastjson2                           thrpt    5  1110.109 ?   2.901  ops/ms
EishayParseUTF8Bytes.gson                                thrpt    5   298.194 ?   5.908  ops/ms
EishayParseUTF8Bytes.jackson                             thrpt    5   461.750 ?  11.123  ops/ms
EishayParseUTF8BytesPretty.fastjson1                     thrpt    5   151.890 ?  37.090  ops/ms
EishayParseUTF8BytesPretty.fastjson2                     thrpt    5   774.542 ?   8.046  ops/ms
EishayParseUTF8BytesPretty.gson                          thrpt    5   277.568 ?   4.460  ops/ms
EishayParseUTF8BytesPretty.jackson                       thrpt    5   350.499 ? 101.449  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes                     thrpt    5  1396.924 ?  16.583  ops/ms
EishayWriteBinary.hessian                                thrpt    5   348.499 ?   0.690  ops/ms
EishayWriteBinary.javaSerialize                          thrpt    5   211.463 ?   1.147  ops/ms
EishayWriteBinary.jsonb                                  thrpt    5  2222.881 ?   7.857  ops/ms
EishayWriteBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   560.105 ?  41.968  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  1731.338 ?  19.889  ops/ms
EishayWriteBinaryArrayMapping.jsonb                      thrpt    5  4348.320 ?  25.522  ops/ms
EishayWriteBinaryArrayMapping.kryo                       thrpt    5  1576.168 ?   3.996  ops/ms
EishayWriteBinaryArrayMapping.protobuf                   thrpt    5  1502.215 ?  25.078  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB                 thrpt    5  1345.119 ?   5.110  ops/ms
EishayWriteBinaryAutoType.fastjson2UTF8Bytes             thrpt    5  1338.188 ?  24.220  ops/ms
EishayWriteBinaryAutoType.hessian                        thrpt    5   334.523 ?   0.991  ops/ms
EishayWriteBinaryAutoType.javaSerialize                  thrpt    5   200.052 ?   0.394  ops/ms
EishayWriteString.fastjson1                              thrpt    5   500.116 ?  18.564  ops/ms
EishayWriteString.fastjson2                              thrpt    5  1299.443 ?  12.222  ops/ms
EishayWriteString.gson                                   thrpt    5   225.756 ?   0.868  ops/ms
EishayWriteString.jackson                                thrpt    5   735.252 ?   4.238  ops/ms
EishayWriteStringTree.fastjson1                          thrpt    5   629.448 ?   4.343  ops/ms
EishayWriteStringTree.fastjson2                          thrpt    5  1063.744 ?   7.613  ops/ms
EishayWriteStringTree.gson                               thrpt    5   204.293 ?  80.230  ops/ms
EishayWriteStringTree.jackson                            thrpt    5   668.780 ?   2.023  ops/ms
EishayWriteStringTree1x.fastjson1                        thrpt    5   565.506 ?   1.747  ops/ms
EishayWriteStringTree1x.fastjson2                        thrpt    5   758.916 ?   1.945  ops/ms
EishayWriteStringTree1x.gson                             thrpt    5   215.605 ?   1.136  ops/ms
EishayWriteStringTree1x.jackson                          thrpt    5   611.806 ?  52.743  ops/ms
EishayWriteUTF8Bytes.fastjson1                           thrpt    5   484.949 ?   4.839  ops/ms
EishayWriteUTF8Bytes.fastjson2                           thrpt    5  1422.573 ?  12.893  ops/ms
EishayWriteUTF8Bytes.gson                                thrpt    5   216.899 ?   5.449  ops/ms
EishayWriteUTF8Bytes.jackson                             thrpt    5   676.498 ?  47.491  ops/ms
```
# ecs.g8m.xlarge-graalvm-ce-java11-22.3.1
```java
Benchmark                                                 Mode  Cnt     Score    Error   Units
EishayParseBinary.fastjson2UTF8Bytes                     thrpt    5   867.501 ?  7.923  ops/ms
EishayParseBinary.hessian                                thrpt    5   179.417 ?  0.542  ops/ms
EishayParseBinary.javaSerialize                          thrpt    5    41.015 ?  0.283  ops/ms
EishayParseBinary.jsonb                                  thrpt    5  2134.437 ?  5.902  ops/ms
EishayParseBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5  1157.135 ?  6.352  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  1375.744 ? 12.059  ops/ms
EishayParseBinaryArrayMapping.jsonb                      thrpt    5  4094.212 ? 13.546  ops/ms
EishayParseBinaryArrayMapping.kryo                       thrpt    5  1392.629 ?  0.390  ops/ms
EishayParseBinaryArrayMapping.protobuf                   thrpt    5  1185.257 ?  3.007  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB                 thrpt    5  1755.044 ?  2.426  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB_autoTypeFilter  thrpt    5  1595.831 ?  1.833  ops/ms
EishayParseBinaryAutoType.hessian                        thrpt    5   178.006 ?  1.195  ops/ms
EishayParseBinaryAutoType.javaSerialize                  thrpt    5    40.116 ?  0.816  ops/ms
EishayParseString.fastjson1                              thrpt    5   812.372 ?  1.010  ops/ms
EishayParseString.fastjson2                              thrpt    5  1052.952 ?  4.031  ops/ms
EishayParseString.gson                                   thrpt    5   401.184 ?  2.753  ops/ms
EishayParseString.jackson                                thrpt    5   403.785 ?  0.693  ops/ms
EishayParseStringPretty.fastjson1                        thrpt    5   227.564 ? 19.202  ops/ms
EishayParseStringPretty.fastjson2                        thrpt    5   765.851 ?  2.159  ops/ms
EishayParseStringPretty.gson                             thrpt    5   365.554 ?  0.387  ops/ms
EishayParseStringPretty.jackson                          thrpt    5   362.652 ?  0.335  ops/ms
EishayParseTreeString.fastjson1                          thrpt    5   408.072 ?  3.933  ops/ms
EishayParseTreeString.fastjson2                          thrpt    5   753.186 ?  7.116  ops/ms
EishayParseTreeString.gson                               thrpt    5   389.662 ?  6.436  ops/ms
EishayParseTreeString.jackson                            thrpt    5   434.595 ?  4.657  ops/ms
EishayParseTreeStringPretty.fastjson1                    thrpt    5   336.945 ?  0.704  ops/ms
EishayParseTreeStringPretty.fastjson2                    thrpt    5   622.402 ?  1.678  ops/ms
EishayParseTreeStringPretty.gson                         thrpt    5   352.338 ?  3.939  ops/ms
EishayParseTreeStringPretty.jackson                      thrpt    5   405.530 ?  0.618  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                       thrpt    5   299.253 ? 49.880  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                       thrpt    5   672.118 ? 19.037  ops/ms
EishayParseTreeUTF8Bytes.gson                            thrpt    5   328.086 ? 42.525  ops/ms
EishayParseTreeUTF8Bytes.jackson                         thrpt    5   411.798 ?  0.713  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1                 thrpt    5   210.372 ? 23.412  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2                 thrpt    5   525.892 ?  1.943  ops/ms
EishayParseTreeUTF8BytesPretty.gson                      thrpt    5   309.353 ?  1.157  ops/ms
EishayParseTreeUTF8BytesPretty.jackson                   thrpt    5   376.028 ?  1.434  ops/ms
EishayParseUTF8Bytes.dsljson                             thrpt    5   655.337 ?  0.866  ops/ms
EishayParseUTF8Bytes.fastjson1                           thrpt    5   548.097 ? 40.694  ops/ms
EishayParseUTF8Bytes.fastjson2                           thrpt    5   857.433 ? 12.207  ops/ms
EishayParseUTF8Bytes.gson                                thrpt    5   304.206 ? 90.001  ops/ms
EishayParseUTF8Bytes.jackson                             thrpt    5   417.743 ? 32.564  ops/ms
EishayParseUTF8BytesPretty.fastjson1                     thrpt    5   163.853 ?  6.433  ops/ms
EishayParseUTF8BytesPretty.fastjson2                     thrpt    5   633.071 ?  4.352  ops/ms
EishayParseUTF8BytesPretty.gson                          thrpt    5   284.761 ?  5.884  ops/ms
EishayParseUTF8BytesPretty.jackson                       thrpt    5   399.184 ?  0.599  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes                     thrpt    5  1320.161 ?  4.025  ops/ms
EishayWriteBinary.hessian                                thrpt    5   332.201 ?  6.959  ops/ms
EishayWriteBinary.javaSerialize                          thrpt    5   195.600 ?  0.682  ops/ms
EishayWriteBinary.jsonb                                  thrpt    5  2555.917 ? 12.242  ops/ms
EishayWriteBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   628.271 ?  7.828  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  1920.238 ?  3.315  ops/ms
EishayWriteBinaryArrayMapping.jsonb                      thrpt    5  4934.792 ?  8.602  ops/ms
EishayWriteBinaryArrayMapping.kryo                       thrpt    5  1669.724 ?  1.860  ops/ms
EishayWriteBinaryArrayMapping.protobuf                   thrpt    5  1462.065 ?  1.021  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB                 thrpt    5  1438.112 ?  5.642  ops/ms
EishayWriteBinaryAutoType.fastjson2UTF8Bytes             thrpt    5  1421.519 ?  9.973  ops/ms
EishayWriteBinaryAutoType.hessian                        thrpt    5   327.771 ?  5.676  ops/ms
EishayWriteBinaryAutoType.javaSerialize                  thrpt    5   196.688 ?  0.990  ops/ms
EishayWriteString.fastjson1                              thrpt    5   595.570 ?  4.336  ops/ms
EishayWriteString.fastjson2                              thrpt    5  1359.618 ? 61.024  ops/ms
EishayWriteString.gson                                   thrpt    5   325.288 ?  0.265  ops/ms
EishayWriteString.jackson                                thrpt    5   738.149 ?  1.713  ops/ms
EishayWriteStringTree.fastjson1                          thrpt    5   399.494 ?  1.535  ops/ms
EishayWriteStringTree.fastjson2                          thrpt    5  1288.057 ?  1.505  ops/ms
EishayWriteStringTree.gson                               thrpt    5   351.411 ?  0.550  ops/ms
EishayWriteStringTree.jackson                            thrpt    5   765.545 ?  1.909  ops/ms
EishayWriteStringTree1x.fastjson1                        thrpt    5   356.734 ?  0.679  ops/ms
EishayWriteStringTree1x.fastjson2                        thrpt    5   999.983 ?  3.740  ops/ms
EishayWriteStringTree1x.gson                             thrpt    5   192.002 ? 40.133  ops/ms
EishayWriteStringTree1x.jackson                          thrpt    5   661.539 ?  0.740  ops/ms
EishayWriteUTF8Bytes.fastjson1                           thrpt    5   481.925 ?  4.099  ops/ms
EishayWriteUTF8Bytes.fastjson2                           thrpt    5  1443.326 ?  1.590  ops/ms
EishayWriteUTF8Bytes.gson                                thrpt    5   303.389 ?  0.643  ops/ms
EishayWriteUTF8Bytes.jackson                             thrpt    5   714.137 ? 11.120  ops/ms
```
# ecs.g8m.xlarge-graalvm-ce-java17-22.3.1
```java
Benchmark                                                 Mode  Cnt     Score     Error   Units
EishayParseBinary.fastjson2UTF8Bytes                     thrpt    5   938.213 ?  13.326  ops/ms
EishayParseBinary.hessian                                thrpt    5   179.386 ?  12.845  ops/ms
EishayParseBinary.javaSerialize                          thrpt    5    41.193 ?   3.589  ops/ms
EishayParseBinary.jsonb                                  thrpt    5  2640.635 ?   7.365  ops/ms
EishayParseBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5  1189.361 ?   2.635  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  1490.015 ?  25.642  ops/ms
EishayParseBinaryArrayMapping.jsonb                      thrpt    5  3960.444 ?  10.003  ops/ms
EishayParseBinaryArrayMapping.kryo                       thrpt    5  1400.980 ?   2.532  ops/ms
EishayParseBinaryArrayMapping.protobuf                   thrpt    5  1552.308 ?   5.705  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB                 thrpt    5  1830.315 ?   2.692  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB_autoTypeFilter  thrpt    5  1654.022 ?   4.691  ops/ms
EishayParseBinaryAutoType.hessian                        thrpt    5   182.033 ?   7.111  ops/ms
EishayParseBinaryAutoType.javaSerialize                  thrpt    5    41.061 ?   2.157  ops/ms
EishayParseString.fastjson1                              thrpt    5  1045.039 ?   3.635  ops/ms
EishayParseString.fastjson2                              thrpt    5  1089.433 ?   3.490  ops/ms
EishayParseString.gson                                   thrpt    5   395.316 ?   1.015  ops/ms
EishayParseString.jackson                                thrpt    5   420.843 ?   1.653  ops/ms
EishayParseStringPretty.fastjson1                        thrpt    5   293.269 ?   2.211  ops/ms
EishayParseStringPretty.fastjson2                        thrpt    5   773.397 ?  30.357  ops/ms
EishayParseStringPretty.gson                             thrpt    5   355.595 ?   9.124  ops/ms
EishayParseStringPretty.jackson                          thrpt    5   389.924 ?   0.922  ops/ms
EishayParseTreeString.fastjson1                          thrpt    5   522.350 ?   1.312  ops/ms
EishayParseTreeString.fastjson2                          thrpt    5   779.154 ?   2.783  ops/ms
EishayParseTreeString.gson                               thrpt    5   392.152 ?   6.850  ops/ms
EishayParseTreeString.jackson                            thrpt    5   464.977 ?   1.637  ops/ms
EishayParseTreeStringPretty.fastjson1                    thrpt    5   442.569 ?   1.157  ops/ms
EishayParseTreeStringPretty.fastjson2                    thrpt    5   624.646 ?   2.037  ops/ms
EishayParseTreeStringPretty.gson                         thrpt    5   354.947 ?   4.065  ops/ms
EishayParseTreeStringPretty.jackson                      thrpt    5   411.006 ?   1.697  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                       thrpt    5   431.786 ?   1.201  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                       thrpt    5   635.848 ?   9.529  ops/ms
EishayParseTreeUTF8Bytes.gson                            thrpt    5   348.041 ?   3.643  ops/ms
EishayParseTreeUTF8Bytes.jackson                         thrpt    5   461.785 ?   1.568  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1                 thrpt    5   350.435 ?   1.356  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2                 thrpt    5   539.023 ?   4.841  ops/ms
EishayParseTreeUTF8BytesPretty.gson                      thrpt    5   314.193 ?   2.238  ops/ms
EishayParseTreeUTF8BytesPretty.jackson                   thrpt    5   422.467 ?   1.395  ops/ms
EishayParseUTF8Bytes.dsljson                             thrpt    5   315.774 ?  68.730  ops/ms
EishayParseUTF8Bytes.fastjson1                           thrpt    5   744.984 ?   1.334  ops/ms
EishayParseUTF8Bytes.fastjson2                           thrpt    5   893.457 ?   8.820  ops/ms
EishayParseUTF8Bytes.gson                                thrpt    5   350.877 ?   3.405  ops/ms
EishayParseUTF8Bytes.jackson                             thrpt    5   465.428 ?   1.441  ops/ms
EishayParseUTF8BytesPretty.fastjson1                     thrpt    5   207.611 ?  31.681  ops/ms
EishayParseUTF8BytesPretty.fastjson2                     thrpt    5   630.239 ?  38.456  ops/ms
EishayParseUTF8BytesPretty.gson                          thrpt    5   316.045 ?   2.880  ops/ms
EishayParseUTF8BytesPretty.jackson                       thrpt    5   361.515 ?  76.123  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes                     thrpt    5  1480.575 ?   7.266  ops/ms
EishayWriteBinary.hessian                                thrpt    5   324.419 ?   2.504  ops/ms
EishayWriteBinary.javaSerialize                          thrpt    5   187.790 ?   0.414  ops/ms
EishayWriteBinary.jsonb                                  thrpt    5  2687.960 ?   8.027  ops/ms
EishayWriteBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   625.334 ?  12.014  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  1929.022 ?  17.533  ops/ms
EishayWriteBinaryArrayMapping.jsonb                      thrpt    5  5237.048 ?  11.811  ops/ms
EishayWriteBinaryArrayMapping.kryo                       thrpt    5  1697.364 ?   4.415  ops/ms
EishayWriteBinaryArrayMapping.protobuf                   thrpt    5  1419.013 ? 248.083  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB                 thrpt    5  1462.628 ?   5.047  ops/ms
EishayWriteBinaryAutoType.fastjson2UTF8Bytes             thrpt    5  1238.750 ?  61.515  ops/ms
EishayWriteBinaryAutoType.hessian                        thrpt    5   343.872 ?   2.272  ops/ms
EishayWriteBinaryAutoType.javaSerialize                  thrpt    5   188.163 ?   0.346  ops/ms
EishayWriteString.fastjson1                              thrpt    5   682.347 ?   1.340  ops/ms
EishayWriteString.fastjson2                              thrpt    5  1452.891 ?   8.221  ops/ms
EishayWriteString.gson                                   thrpt    5   202.412 ?   6.744  ops/ms
EishayWriteString.jackson                                thrpt    5   733.532 ?   5.966  ops/ms
EishayWriteStringTree.fastjson1                          thrpt    5   590.783 ?  54.282  ops/ms
EishayWriteStringTree.fastjson2                          thrpt    5  1322.254 ?   3.242  ops/ms
EishayWriteStringTree.gson                               thrpt    5   214.569 ?   1.400  ops/ms
EishayWriteStringTree.jackson                            thrpt    5   780.373 ?   3.803  ops/ms
EishayWriteStringTree1x.fastjson1                        thrpt    5   439.988 ?   1.803  ops/ms
EishayWriteStringTree1x.fastjson2                        thrpt    5  1029.923 ?   2.693  ops/ms
EishayWriteStringTree1x.gson                             thrpt    5   196.701 ?   3.827  ops/ms
EishayWriteStringTree1x.jackson                          thrpt    5   675.230 ?   0.694  ops/ms
EishayWriteUTF8Bytes.fastjson1                           thrpt    5   438.033 ?  48.279  ops/ms
EishayWriteUTF8Bytes.fastjson2                           thrpt    5  1459.487 ?  39.850  ops/ms
EishayWriteUTF8Bytes.gson                                thrpt    5   197.419 ?   1.321  ops/ms
EishayWriteUTF8Bytes.jackson                             thrpt    5   621.988 ? 104.370  ops/ms
```
# ecs.g8m.xlarge-graalvm-ee-java11-22.3.1
```java
Benchmark                                                 Mode  Cnt     Score     Error   Units
EishayParseBinary.fastjson2UTF8Bytes                     thrpt    5  1115.286 ?   7.139  ops/ms
EishayParseBinary.hessian                                thrpt    5   313.907 ?   0.512  ops/ms
EishayParseBinary.javaSerialize                          thrpt    5    41.410 ?   2.012  ops/ms
EishayParseBinary.jsonb                                  thrpt    5  2937.091 ?  11.953  ops/ms
EishayParseBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5  1324.148 ?   4.460  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  1698.142 ?   4.980  ops/ms
EishayParseBinaryArrayMapping.jsonb                      thrpt    5  4444.019 ?  19.691  ops/ms
EishayParseBinaryArrayMapping.kryo                       thrpt    5  1304.391 ?   5.304  ops/ms
EishayParseBinaryArrayMapping.protobuf                   thrpt    5  1767.254 ?  13.576  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB                 thrpt    5  2019.136 ?   2.754  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB_autoTypeFilter  thrpt    5  1792.204 ?   2.198  ops/ms
EishayParseBinaryAutoType.hessian                        thrpt    5   308.212 ?   0.877  ops/ms
EishayParseBinaryAutoType.javaSerialize                  thrpt    5    44.665 ?   0.323  ops/ms
EishayParseString.fastjson1                              thrpt    5   960.132 ?   9.295  ops/ms
EishayParseString.fastjson2                              thrpt    5  1197.244 ?   3.668  ops/ms
EishayParseString.gson                                   thrpt    5   353.493 ?  31.975  ops/ms
EishayParseString.jackson                                thrpt    5   435.970 ?   1.238  ops/ms
EishayParseStringPretty.fastjson1                        thrpt    5   269.392 ?   4.245  ops/ms
EishayParseStringPretty.fastjson2                        thrpt    5   839.161 ?   2.403  ops/ms
EishayParseStringPretty.gson                             thrpt    5   332.823 ?  92.205  ops/ms
EishayParseStringPretty.jackson                          thrpt    5   350.737 ?  23.725  ops/ms
EishayParseTreeString.fastjson1                          thrpt    5   487.950 ?  18.087  ops/ms
EishayParseTreeString.fastjson2                          thrpt    5   850.952 ?   4.744  ops/ms
EishayParseTreeString.gson                               thrpt    5   336.719 ?  30.659  ops/ms
EishayParseTreeString.jackson                            thrpt    5   395.934 ?  55.789  ops/ms
EishayParseTreeStringPretty.fastjson1                    thrpt    5   385.619 ?   3.354  ops/ms
EishayParseTreeStringPretty.fastjson2                    thrpt    5   688.709 ?   3.521  ops/ms
EishayParseTreeStringPretty.gson                         thrpt    5   358.723 ?   2.554  ops/ms
EishayParseTreeStringPretty.jackson                      thrpt    5   373.716 ? 122.036  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                       thrpt    5   388.444 ?   8.117  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                       thrpt    5   769.694 ?  85.460  ops/ms
EishayParseTreeUTF8Bytes.gson                            thrpt    5   360.661 ?   0.720  ops/ms
EishayParseTreeUTF8Bytes.jackson                         thrpt    5   500.999 ?   1.048  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1                 thrpt    5   317.785 ?   5.034  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2                 thrpt    5   636.970 ?   3.234  ops/ms
EishayParseTreeUTF8BytesPretty.gson                      thrpt    5   286.812 ?  73.272  ops/ms
EishayParseTreeUTF8BytesPretty.jackson                   thrpt    5   439.536 ?  28.724  ops/ms
EishayParseUTF8Bytes.dsljson                             thrpt    5   716.506 ?  17.543  ops/ms
EishayParseUTF8Bytes.fastjson1                           thrpt    5   474.250 ? 147.842  ops/ms
EishayParseUTF8Bytes.fastjson2                           thrpt    5  1037.866 ?   8.583  ops/ms
EishayParseUTF8Bytes.gson                                thrpt    5   346.433 ?  38.206  ops/ms
EishayParseUTF8Bytes.jackson                             thrpt    5   415.033 ?  41.610  ops/ms
EishayParseUTF8BytesPretty.fastjson1                     thrpt    5   211.089 ?  19.696  ops/ms
EishayParseUTF8BytesPretty.fastjson2                     thrpt    5   754.585 ?   4.745  ops/ms
EishayParseUTF8BytesPretty.gson                          thrpt    5   286.271 ?  51.324  ops/ms
EishayParseUTF8BytesPretty.jackson                       thrpt    5   383.681 ?   9.172  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes                     thrpt    5  1189.044 ? 923.845  ops/ms
EishayWriteBinary.hessian                                thrpt    5   385.184 ?   6.870  ops/ms
EishayWriteBinary.javaSerialize                          thrpt    5   223.756 ?   0.244  ops/ms
EishayWriteBinary.jsonb                                  thrpt    5  3201.366 ?   9.541  ops/ms
EishayWriteBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   529.161 ?  17.136  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  2045.192 ?  14.640  ops/ms
EishayWriteBinaryArrayMapping.jsonb                      thrpt    5  6455.783 ?  19.125  ops/ms
EishayWriteBinaryArrayMapping.kryo                       thrpt    5  1917.075 ?   2.239  ops/ms
EishayWriteBinaryArrayMapping.protobuf                   thrpt    5  2461.681 ?   7.079  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB                 thrpt    5  1595.686 ?   3.435  ops/ms
EishayWriteBinaryAutoType.fastjson2UTF8Bytes             thrpt    5  1586.706 ?   6.963  ops/ms
EishayWriteBinaryAutoType.hessian                        thrpt    5   376.571 ?   3.763  ops/ms
EishayWriteBinaryAutoType.javaSerialize                  thrpt    5   225.757 ?   0.898  ops/ms
EishayWriteString.fastjson1                              thrpt    5   563.327 ? 136.097  ops/ms
EishayWriteString.fastjson2                              thrpt    5  1558.980 ?  10.178  ops/ms
EishayWriteString.gson                                   thrpt    5   291.926 ?  92.546  ops/ms
EishayWriteString.jackson                                thrpt    5   780.298 ? 166.696  ops/ms
EishayWriteStringTree.fastjson1                          thrpt    5   596.169 ?  63.973  ops/ms
EishayWriteStringTree.fastjson2                          thrpt    5  1230.258 ?   6.382  ops/ms
EishayWriteStringTree.gson                               thrpt    5   271.493 ?  98.309  ops/ms
EishayWriteStringTree.jackson                            thrpt    5   831.389 ?   3.396  ops/ms
EishayWriteStringTree1x.fastjson1                        thrpt    5   617.236 ? 112.246  ops/ms
EishayWriteStringTree1x.fastjson2                        thrpt    5  1108.928 ?   6.446  ops/ms
EishayWriteStringTree1x.gson                             thrpt    5   374.240 ?  12.735  ops/ms
EishayWriteStringTree1x.jackson                          thrpt    5   776.458 ?   5.619  ops/ms
EishayWriteUTF8Bytes.fastjson1                           thrpt    5   573.537 ?   2.441  ops/ms
EishayWriteUTF8Bytes.fastjson2                           thrpt    5  1176.633 ? 435.280  ops/ms
EishayWriteUTF8Bytes.gson                                thrpt    5   306.786 ?  77.819  ops/ms
EishayWriteUTF8Bytes.jackson                             thrpt    5   608.454 ?  46.650  ops/ms
```
# ecs.g8m.xlarge-graalvm-ee-java17-22.3.1
```java
Benchmark                                                 Mode  Cnt     Score     Error   Units
EishayParseBinary.fastjson2UTF8Bytes                     thrpt    5  1117.186 ?   1.968  ops/ms
EishayParseBinary.hessian                                thrpt    5   331.510 ?   0.598  ops/ms
EishayParseBinary.javaSerialize                          thrpt    5    42.867 ?   1.184  ops/ms
EishayParseBinary.jsonb                                  thrpt    5  2758.443 ?   7.478  ops/ms
EishayParseBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5  1233.864 ?   4.264  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  1773.058 ?   2.478  ops/ms
EishayParseBinaryArrayMapping.jsonb                      thrpt    5  5702.893 ?  11.597  ops/ms
EishayParseBinaryArrayMapping.kryo                       thrpt    5  1308.091 ?   3.258  ops/ms
EishayParseBinaryArrayMapping.protobuf                   thrpt    5  2256.635 ?   3.850  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB                 thrpt    5  1851.247 ?   6.228  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB_autoTypeFilter  thrpt    5  1782.030 ?   3.597  ops/ms
EishayParseBinaryAutoType.hessian                        thrpt    5   344.444 ?   0.788  ops/ms
EishayParseBinaryAutoType.javaSerialize                  thrpt    5    41.849 ?   5.035  ops/ms
EishayParseString.fastjson1                              thrpt    5  1377.415 ?   3.271  ops/ms
EishayParseString.fastjson2                              thrpt    5  1151.483 ?   2.115  ops/ms
EishayParseString.gson                                   thrpt    5   416.141 ?   3.650  ops/ms
EishayParseString.jackson                                thrpt    5   435.793 ?   1.832  ops/ms
EishayParseStringPretty.fastjson1                        thrpt    5   336.420 ?   1.502  ops/ms
EishayParseStringPretty.fastjson2                        thrpt    5   843.844 ?   3.273  ops/ms
EishayParseStringPretty.gson                             thrpt    5   389.916 ?   1.858  ops/ms
EishayParseStringPretty.jackson                          thrpt    5   397.203 ?   2.751  ops/ms
EishayParseTreeString.fastjson1                          thrpt    5   583.074 ?   2.482  ops/ms
EishayParseTreeString.fastjson2                          thrpt    5   886.776 ?   0.657  ops/ms
EishayParseTreeString.gson                               thrpt    5   375.970 ?   3.497  ops/ms
EishayParseTreeString.jackson                            thrpt    5   477.923 ?   0.514  ops/ms
EishayParseTreeStringPretty.fastjson1                    thrpt    5   486.475 ?   1.468  ops/ms
EishayParseTreeStringPretty.fastjson2                    thrpt    5   708.345 ?   1.685  ops/ms
EishayParseTreeStringPretty.gson                         thrpt    5   342.827 ?   1.360  ops/ms
EishayParseTreeStringPretty.jackson                      thrpt    5   419.365 ?   1.480  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                       thrpt    5   441.346 ?  12.561  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                       thrpt    5   813.432 ?   2.996  ops/ms
EishayParseTreeUTF8Bytes.gson                            thrpt    5   359.248 ?   2.127  ops/ms
EishayParseTreeUTF8Bytes.jackson                         thrpt    5   510.509 ?   0.796  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1                 thrpt    5   358.328 ?  10.452  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2                 thrpt    5   638.007 ?   0.569  ops/ms
EishayParseTreeUTF8BytesPretty.gson                      thrpt    5   336.307 ?   1.069  ops/ms
EishayParseTreeUTF8BytesPretty.jackson                   thrpt    5   464.694 ?   1.852  ops/ms
EishayParseUTF8Bytes.dsljson                             thrpt    5   585.251 ?  50.750  ops/ms
EishayParseUTF8Bytes.fastjson1                           thrpt    5   864.922 ?   3.115  ops/ms
EishayParseUTF8Bytes.fastjson2                           thrpt    5  1025.837 ?   3.078  ops/ms
EishayParseUTF8Bytes.gson                                thrpt    5   361.208 ?   0.540  ops/ms
EishayParseUTF8Bytes.jackson                             thrpt    5   443.048 ?   0.788  ops/ms
EishayParseUTF8BytesPretty.fastjson1                     thrpt    5   260.918 ?  23.136  ops/ms
EishayParseUTF8BytesPretty.fastjson2                     thrpt    5   752.726 ?   3.024  ops/ms
EishayParseUTF8BytesPretty.gson                          thrpt    5   327.938 ?   0.735  ops/ms
EishayParseUTF8BytesPretty.jackson                       thrpt    5   395.782 ?   7.558  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes                     thrpt    5  1700.333 ?   7.065  ops/ms
EishayWriteBinary.hessian                                thrpt    5   379.125 ?   6.475  ops/ms
EishayWriteBinary.javaSerialize                          thrpt    5   218.899 ?   0.609  ops/ms
EishayWriteBinary.jsonb                                  thrpt    5  3235.905 ?  13.037  ops/ms
EishayWriteBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   728.797 ?  29.801  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  2050.055 ?   9.925  ops/ms
EishayWriteBinaryArrayMapping.jsonb                      thrpt    5  6913.002 ?  17.239  ops/ms
EishayWriteBinaryArrayMapping.kryo                       thrpt    5  1951.488 ?   9.241  ops/ms
EishayWriteBinaryArrayMapping.protobuf                   thrpt    5  2424.503 ?   2.683  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB                 thrpt    5  1620.882 ?  14.842  ops/ms
EishayWriteBinaryAutoType.fastjson2UTF8Bytes             thrpt    5  1407.184 ? 335.369  ops/ms
EishayWriteBinaryAutoType.hessian                        thrpt    5   388.458 ?   2.486  ops/ms
EishayWriteBinaryAutoType.javaSerialize                  thrpt    5   220.251 ?   0.473  ops/ms
EishayWriteString.fastjson1                              thrpt    5   637.783 ?  24.569  ops/ms
EishayWriteString.fastjson2                              thrpt    5  1589.790 ?   6.534  ops/ms
EishayWriteString.gson                                   thrpt    5   194.536 ?  47.642  ops/ms
EishayWriteString.jackson                                thrpt    5   839.328 ?  32.004  ops/ms
EishayWriteStringTree.fastjson1                          thrpt    5   557.317 ?  45.846  ops/ms
EishayWriteStringTree.fastjson2                          thrpt    5  1234.846 ?   6.828  ops/ms
EishayWriteStringTree.gson                               thrpt    5   237.216 ?   8.619  ops/ms
EishayWriteStringTree.jackson                            thrpt    5   797.710 ?   0.729  ops/ms
EishayWriteStringTree1x.fastjson1                        thrpt    5   765.953 ?   4.711  ops/ms
EishayWriteStringTree1x.fastjson2                        thrpt    5  1110.292 ?   3.019  ops/ms
EishayWriteStringTree1x.gson                             thrpt    5   236.060 ?   2.758  ops/ms
EishayWriteStringTree1x.jackson                          thrpt    5   764.946 ?   1.141  ops/ms
EishayWriteUTF8Bytes.fastjson1                           thrpt    5   681.073 ?   3.028  ops/ms
EishayWriteUTF8Bytes.fastjson2                           thrpt    5  1630.648 ? 130.156  ops/ms
EishayWriteUTF8Bytes.gson                                thrpt    5   226.497 ?   0.471  ops/ms
EishayWriteUTF8Bytes.jackson                             thrpt    5   909.140 ?  13.109  ops/ms
```

# OrangePi5-jdk1.8.0_361
```java
Benchmark                                                 Mode  Cnt     Score    Error   Units
EishayParseBinary.fastjson2UTF8Bytes                     thrpt    5   569.871 ± 19.450  ops/ms
EishayParseBinary.hessian                                thrpt    5   152.381 ± 12.184  ops/ms
EishayParseBinary.javaSerialize                          thrpt    5    27.582 ±  0.466  ops/ms
EishayParseBinary.jsonb                                  thrpt    5  1001.870 ± 29.989  ops/ms
EishayParseBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   686.470 ± 25.624  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5   828.801 ± 35.329  ops/ms
EishayParseBinaryArrayMapping.jsonb                      thrpt    5  1719.978 ± 38.700  ops/ms
EishayParseBinaryArrayMapping.kryo                       thrpt    5  1008.169 ± 11.533  ops/ms
EishayParseBinaryArrayMapping.protobuf                   thrpt    5   723.324 ± 24.992  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB                 thrpt    5   635.463 ± 13.906  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB_autoTypeFilter  thrpt    5   637.630 ± 26.776  ops/ms
EishayParseBinaryAutoType.hessian                        thrpt    5   132.544 ±  7.178  ops/ms
EishayParseBinaryAutoType.javaSerialize                  thrpt    5    26.736 ±  1.801  ops/ms
EishayParseString.fastjson1                              thrpt    5   560.878 ± 27.993  ops/ms
EishayParseString.fastjson2                              thrpt    5   654.595 ± 16.015  ops/ms
EishayParseString.gson                                   thrpt    5   219.880 ±  4.032  ops/ms
EishayParseString.jackson                                thrpt    5   274.647 ± 14.926  ops/ms
EishayParseStringPretty.fastjson1                        thrpt    5   157.851 ± 12.311  ops/ms
EishayParseStringPretty.fastjson2                        thrpt    5   526.290 ± 14.727  ops/ms
EishayParseStringPretty.gson                             thrpt    5   202.658 ±  9.949  ops/ms
EishayParseStringPretty.jackson                          thrpt    5   244.421 ± 14.603  ops/ms
EishayParseTreeString.fastjson1                          thrpt    5   220.208 ±  7.328  ops/ms
EishayParseTreeString.fastjson2                          thrpt    5   429.501 ± 27.931  ops/ms
EishayParseTreeString.gson                               thrpt    5   174.630 ± 10.366  ops/ms
EishayParseTreeString.jackson                            thrpt    5   238.991 ± 15.177  ops/ms
EishayParseTreeStringPretty.fastjson1                    thrpt    5   206.557 ±  9.526  ops/ms
EishayParseTreeStringPretty.fastjson2                    thrpt    5   379.920 ± 10.127  ops/ms
EishayParseTreeStringPretty.gson                         thrpt    5   164.440 ±  8.056  ops/ms
EishayParseTreeStringPretty.jackson                      thrpt    5   219.437 ± 11.183  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                       thrpt    5   175.066 ±  8.145  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                       thrpt    5   410.534 ± 14.070  ops/ms
EishayParseTreeUTF8Bytes.gson                            thrpt    5   151.716 ±  3.362  ops/ms
EishayParseTreeUTF8Bytes.jackson                         thrpt    5   268.021 ± 16.882  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1                 thrpt    5   154.262 ±  6.994  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2                 thrpt    5   360.205 ± 14.435  ops/ms
EishayParseTreeUTF8BytesPretty.gson                      thrpt    5   148.988 ±  6.154  ops/ms
EishayParseTreeUTF8BytesPretty.jackson                   thrpt    5   247.848 ±  9.005  ops/ms
EishayParseUTF8Bytes.dsljson                             thrpt    5   431.222 ± 34.058  ops/ms
EishayParseUTF8Bytes.fastjson1                           thrpt    5   403.504 ± 15.560  ops/ms
EishayParseUTF8Bytes.fastjson2                           thrpt    5   558.420 ± 23.688  ops/ms
EishayParseUTF8Bytes.gson                                thrpt    5   150.852 ±  8.179  ops/ms
EishayParseUTF8Bytes.jackson                             thrpt    5   319.153 ± 15.269  ops/ms
EishayParseUTF8BytesPretty.fastjson1                     thrpt    5   139.627 ±  5.505  ops/ms
EishayParseUTF8BytesPretty.fastjson2                     thrpt    5   457.182 ± 21.335  ops/ms
EishayParseUTF8BytesPretty.gson                          thrpt    5   145.046 ±  4.644  ops/ms
EishayParseUTF8BytesPretty.jackson                       thrpt    5   286.720 ± 10.398  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes                     thrpt    5   838.577 ± 40.436  ops/ms
EishayWriteBinary.hessian                                thrpt    5   190.760 ± 38.375  ops/ms
EishayWriteBinary.javaSerialize                          thrpt    5   121.691 ±  8.227  ops/ms
EishayWriteBinary.jsonb                                  thrpt    5  1041.292 ± 37.699  ops/ms
EishayWriteBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   321.650 ±  9.778  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5   968.116 ± 95.644  ops/ms
EishayWriteBinaryArrayMapping.jsonb                      thrpt    5  1563.079 ± 48.393  ops/ms
EishayWriteBinaryArrayMapping.kryo                       thrpt    5   882.742 ± 30.450  ops/ms
EishayWriteBinaryArrayMapping.protobuf                   thrpt    5   708.588 ± 52.331  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB                 thrpt    5   670.145 ± 31.775  ops/ms
EishayWriteBinaryAutoType.fastjson2UTF8Bytes             thrpt    5   787.483 ± 88.089  ops/ms
EishayWriteBinaryAutoType.hessian                        thrpt    5   182.971 ± 23.405  ops/ms
EishayWriteBinaryAutoType.javaSerialize                  thrpt    5   121.381 ±  7.070  ops/ms
EishayWriteString.fastjson1                              thrpt    5   283.573 ± 18.518  ops/ms
EishayWriteString.fastjson2                              thrpt    5   697.023 ± 22.547  ops/ms
EishayWriteString.gson                                   thrpt    5   200.770 ± 11.891  ops/ms
EishayWriteString.jackson                                thrpt    5   388.961 ±  9.072  ops/ms
EishayWriteStringTree.fastjson1                          thrpt    5   327.315 ± 13.891  ops/ms
EishayWriteStringTree.fastjson2                          thrpt    5   489.998 ±  9.755  ops/ms
EishayWriteStringTree.gson                               thrpt    5   228.081 ±  8.249  ops/ms
EishayWriteStringTree.jackson                            thrpt    5   398.634 ± 26.564  ops/ms
EishayWriteStringTree1x.fastjson1                        thrpt    5   328.304 ± 16.758  ops/ms
EishayWriteStringTree1x.fastjson2                        thrpt    5   438.909 ± 16.841  ops/ms
EishayWriteStringTree1x.gson                             thrpt    5   223.852 ± 12.520  ops/ms
EishayWriteStringTree1x.jackson                          thrpt    5   375.114 ± 26.949  ops/ms
EishayWriteUTF8Bytes.fastjson1                           thrpt    5   283.210 ± 16.204  ops/ms
EishayWriteUTF8Bytes.fastjson2                           thrpt    5   829.612 ± 62.768  ops/ms
EishayWriteUTF8Bytes.gson                                thrpt    5   166.997 ±  2.821  ops/ms
EishayWriteUTF8Bytes.jackson                             thrpt    5   394.135 ± 13.394  ops/ms
```
# OrangePi5-jdk-11.0.18
```java
Benchmark                                                 Mode  Cnt     Score    Error   Units
EishayParseBinary.fastjson2UTF8Bytes                     thrpt    5   709.240 ± 45.963  ops/ms
EishayParseBinary.hessian                                thrpt    5   135.850 ±  4.578  ops/ms
EishayParseBinary.javaSerialize                          thrpt    5    27.295 ±  0.074  ops/ms
EishayParseBinary.jsonb                                  thrpt    5  1341.294 ± 39.279  ops/ms
EishayParseBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   745.076 ± 40.670  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5   996.256 ± 23.074  ops/ms
EishayParseBinaryArrayMapping.jsonb                      thrpt    5  2082.584 ± 38.627  ops/ms
EishayParseBinaryArrayMapping.kryo                       thrpt    5   955.132 ± 25.854  ops/ms
EishayParseBinaryArrayMapping.protobuf                   thrpt    5   758.088 ± 31.336  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB                 thrpt    5   808.881 ± 25.179  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB_autoTypeFilter  thrpt    5   792.967 ± 30.384  ops/ms
EishayParseBinaryAutoType.hessian                        thrpt    5   135.712 ±  5.125  ops/ms
EishayParseBinaryAutoType.javaSerialize                  thrpt    5    26.256 ±  0.827  ops/ms
EishayParseString.fastjson1                              thrpt    5   577.243 ± 16.632  ops/ms
EishayParseString.fastjson2                              thrpt    5   676.719 ± 27.005  ops/ms
EishayParseString.gson                                   thrpt    5   227.293 ±  9.221  ops/ms
EishayParseString.jackson                                thrpt    5   263.619 ±  9.135  ops/ms
EishayParseStringPretty.fastjson1                        thrpt    5   162.004 ±  2.774  ops/ms
EishayParseStringPretty.fastjson2                        thrpt    5   516.370 ± 12.960  ops/ms
EishayParseStringPretty.gson                             thrpt    5   205.997 ±  7.160  ops/ms
EishayParseStringPretty.jackson                          thrpt    5   250.162 ±  4.350  ops/ms
EishayParseTreeString.fastjson1                          thrpt    5   239.215 ±  5.422  ops/ms
EishayParseTreeString.fastjson2                          thrpt    5   477.027 ± 22.253  ops/ms
EishayParseTreeString.gson                               thrpt    5   184.971 ±  7.713  ops/ms
EishayParseTreeString.jackson                            thrpt    5   250.949 ±  8.697  ops/ms
EishayParseTreeStringPretty.fastjson1                    thrpt    5   189.244 ± 12.271  ops/ms
EishayParseTreeStringPretty.fastjson2                    thrpt    5   387.035 ± 29.786  ops/ms
EishayParseTreeStringPretty.gson                         thrpt    5   177.118 ±  3.416  ops/ms
EishayParseTreeStringPretty.jackson                      thrpt    5   232.250 ±  9.180  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                       thrpt    5   213.458 ±  7.998  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                       thrpt    5   476.645 ± 20.803  ops/ms
EishayParseTreeUTF8Bytes.gson                            thrpt    5   173.092 ±  6.792  ops/ms
EishayParseTreeUTF8Bytes.jackson                         thrpt    5   293.194 ±  2.891  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1                 thrpt    5   164.706 ±  7.013  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2                 thrpt    5   390.475 ± 16.921  ops/ms
EishayParseTreeUTF8BytesPretty.gson                      thrpt    5   164.171 ±  7.696  ops/ms
EishayParseTreeUTF8BytesPretty.jackson                   thrpt    5   273.649 ± 10.600  ops/ms
EishayParseUTF8Bytes.dsljson                             thrpt    5   442.887 ± 20.661  ops/ms
EishayParseUTF8Bytes.fastjson1                           thrpt    5   454.815 ±  6.447  ops/ms
EishayParseUTF8Bytes.fastjson2                           thrpt    5   688.989 ± 20.751  ops/ms
EishayParseUTF8Bytes.gson                                thrpt    5   177.047 ±  7.330  ops/ms
EishayParseUTF8Bytes.jackson                             thrpt    5   305.389 ± 11.572  ops/ms
EishayParseUTF8BytesPretty.fastjson1                     thrpt    5   142.043 ±  4.273  ops/ms
EishayParseUTF8BytesPretty.fastjson2                     thrpt    5   517.237 ± 22.119  ops/ms
EishayParseUTF8BytesPretty.gson                          thrpt    5   164.783 ±  1.941  ops/ms
EishayParseUTF8BytesPretty.jackson                       thrpt    5   280.679 ± 10.050  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes                     thrpt    5   944.298 ± 56.401  ops/ms
EishayWriteBinary.hessian                                thrpt    5   192.799 ± 23.016  ops/ms
EishayWriteBinary.javaSerialize                          thrpt    5   129.654 ±  3.206  ops/ms
EishayWriteBinary.jsonb                                  thrpt    5  1390.380 ± 68.638  ops/ms
EishayWriteBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   307.779 ± 12.461  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  1064.796 ± 20.495  ops/ms
EishayWriteBinaryArrayMapping.jsonb                      thrpt    5  2357.968 ± 37.238  ops/ms
EishayWriteBinaryArrayMapping.kryo                       thrpt    5   990.407 ±  2.166  ops/ms
EishayWriteBinaryArrayMapping.protobuf                   thrpt    5   863.948 ± 44.988  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB                 thrpt    5   765.039 ±  4.348  ops/ms
EishayWriteBinaryAutoType.fastjson2UTF8Bytes             thrpt    5   854.113 ± 39.718  ops/ms
EishayWriteBinaryAutoType.hessian                        thrpt    5   187.995 ± 11.667  ops/ms
EishayWriteBinaryAutoType.javaSerialize                  thrpt    5   132.902 ±  6.889  ops/ms
EishayWriteString.fastjson1                              thrpt    5   332.814 ±  7.062  ops/ms
EishayWriteString.fastjson2                              thrpt    5   854.896 ±  9.596  ops/ms
EishayWriteString.gson                                   thrpt    5   208.743 ±  2.593  ops/ms
EishayWriteString.jackson                                thrpt    5   423.161 ± 17.464  ops/ms
EishayWriteStringTree.fastjson1                          thrpt    5   366.040 ± 14.403  ops/ms
EishayWriteStringTree.fastjson2                          thrpt    5   605.653 ±  8.389  ops/ms
EishayWriteStringTree.gson                               thrpt    5   219.655 ± 11.055  ops/ms
EishayWriteStringTree.jackson                            thrpt    5   449.373 ± 23.026  ops/ms
EishayWriteStringTree1x.fastjson1                        thrpt    5   340.748 ±  9.317  ops/ms
EishayWriteStringTree1x.fastjson2                        thrpt    5   542.968 ± 19.173  ops/ms
EishayWriteStringTree1x.gson                             thrpt    5   218.544 ±  5.898  ops/ms
EishayWriteStringTree1x.jackson                          thrpt    5   425.959 ± 11.802  ops/ms
EishayWriteUTF8Bytes.fastjson1                           thrpt    5   267.425 ±  0.931  ops/ms
EishayWriteUTF8Bytes.fastjson2                           thrpt    5   913.220 ± 19.807  ops/ms
EishayWriteUTF8Bytes.gson                                thrpt    5   196.843 ±  6.555  ops/ms
EishayWriteUTF8Bytes.jackson                             thrpt    5   389.362 ± 10.429  ops/ms
```
# OrangePi5-jdk-17.0.6
```java
Benchmark                                                 Mode  Cnt     Score    Error   Units
EishayParseBinary.fastjson2UTF8Bytes                     thrpt    5   725.593 ± 33.759  ops/ms
EishayParseBinary.hessian                                thrpt    5   156.354 ±  8.275  ops/ms
EishayParseBinary.javaSerialize                          thrpt    5    29.963 ±  0.866  ops/ms
EishayParseBinary.jsonb                                  thrpt    5  1460.020 ± 69.955  ops/ms
EishayParseBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   855.869 ± 20.332  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  1030.341 ± 38.834  ops/ms
EishayParseBinaryArrayMapping.jsonb                      thrpt    5  2209.274 ± 71.319  ops/ms
EishayParseBinaryArrayMapping.kryo                       thrpt    5  1029.827 ± 63.506  ops/ms
EishayParseBinaryArrayMapping.protobuf                   thrpt    5   963.066 ± 53.342  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB                 thrpt    5   829.524 ± 21.175  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB_autoTypeFilter  thrpt    5   837.154 ± 17.873  ops/ms
EishayParseBinaryAutoType.hessian                        thrpt    5   154.807 ±  4.104  ops/ms
EishayParseBinaryAutoType.javaSerialize                  thrpt    5    29.375 ±  0.760  ops/ms
EishayParseString.fastjson1                              thrpt    5   809.552 ± 33.572  ops/ms
EishayParseString.fastjson2                              thrpt    5   695.143 ± 35.159  ops/ms
EishayParseString.gson                                   thrpt    5   224.739 ± 10.089  ops/ms
EishayParseString.jackson                                thrpt    5   284.603 ± 11.603  ops/ms
EishayParseStringPretty.fastjson1                        thrpt    5   186.118 ±  7.501  ops/ms
EishayParseStringPretty.fastjson2                        thrpt    5   519.434 ± 20.934  ops/ms
EishayParseStringPretty.gson                             thrpt    5   210.992 ±  8.744  ops/ms
EishayParseStringPretty.jackson                          thrpt    5   255.614 ±  8.785  ops/ms
EishayParseTreeString.fastjson1                          thrpt    5   286.966 ±  7.010  ops/ms
EishayParseTreeString.fastjson2                          thrpt    5   486.906 ± 28.679  ops/ms
EishayParseTreeString.gson                               thrpt    5   180.124 ±  4.606  ops/ms
EishayParseTreeString.jackson                            thrpt    5   263.940 ± 15.240  ops/ms
EishayParseTreeStringPretty.fastjson1                    thrpt    5   251.506 ± 11.058  ops/ms
EishayParseTreeStringPretty.fastjson2                    thrpt    5   395.700 ±  7.144  ops/ms
EishayParseTreeStringPretty.gson                         thrpt    5   170.897 ±  9.642  ops/ms
EishayParseTreeStringPretty.jackson                      thrpt    5   253.243 ±  9.106  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                       thrpt    5   244.172 ± 11.482  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                       thrpt    5   481.408 ± 37.906  ops/ms
EishayParseTreeUTF8Bytes.gson                            thrpt    5   169.936 ±  4.989  ops/ms
EishayParseTreeUTF8Bytes.jackson                         thrpt    5   325.195 ± 23.657  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1                 thrpt    5   209.768 ±  7.369  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2                 thrpt    5   392.710 ± 20.650  ops/ms
EishayParseTreeUTF8BytesPretty.gson                      thrpt    5   155.957 ±  4.317  ops/ms
EishayParseTreeUTF8BytesPretty.jackson                   thrpt    5   298.030 ± 10.042  ops/ms
EishayParseUTF8Bytes.dsljson                             thrpt    5   432.798 ± 15.891  ops/ms
EishayParseUTF8Bytes.fastjson1                           thrpt    5   590.042 ± 21.970  ops/ms
EishayParseUTF8Bytes.fastjson2                           thrpt    5   705.224 ± 27.436  ops/ms
EishayParseUTF8Bytes.gson                                thrpt    5   168.232 ± 12.209  ops/ms
EishayParseUTF8Bytes.jackson                             thrpt    5   315.579 ± 20.430  ops/ms
EishayParseUTF8BytesPretty.fastjson1                     thrpt    5   162.985 ±  2.534  ops/ms
EishayParseUTF8BytesPretty.fastjson2                     thrpt    5   519.973 ± 12.297  ops/ms
EishayParseUTF8BytesPretty.gson                          thrpt    5   160.749 ±  6.445  ops/ms
EishayParseUTF8BytesPretty.jackson                       thrpt    5   285.796 ± 12.439  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes                     thrpt    5   904.624 ± 21.407  ops/ms
EishayWriteBinary.hessian                                thrpt    5   197.715 ± 14.318  ops/ms
EishayWriteBinary.javaSerialize                          thrpt    5   125.307 ±  4.688  ops/ms
EishayWriteBinary.jsonb                                  thrpt    5  1421.877 ± 47.190  ops/ms
EishayWriteBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   333.424 ± 12.794  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  1079.658 ± 39.778  ops/ms
EishayWriteBinaryArrayMapping.jsonb                      thrpt    5  2448.889 ± 82.856  ops/ms
EishayWriteBinaryArrayMapping.kryo                       thrpt    5   996.505 ± 43.573  ops/ms
EishayWriteBinaryArrayMapping.protobuf                   thrpt    5  1020.465 ± 45.862  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB                 thrpt    5   784.785 ± 30.260  ops/ms
EishayWriteBinaryAutoType.fastjson2UTF8Bytes             thrpt    5   857.410 ± 21.295  ops/ms
EishayWriteBinaryAutoType.hessian                        thrpt    5   200.916 ± 24.269  ops/ms
EishayWriteBinaryAutoType.javaSerialize                  thrpt    5   130.687 ±  7.334  ops/ms
EishayWriteString.fastjson1                              thrpt    5   340.099 ± 13.878  ops/ms
EishayWriteString.fastjson2                              thrpt    5   845.826 ±  5.309  ops/ms
EishayWriteString.gson                                   thrpt    5   169.121 ±  4.691  ops/ms
EishayWriteString.jackson                                thrpt    5   460.896 ± 18.945  ops/ms
EishayWriteStringTree.fastjson1                          thrpt    5   371.986 ± 18.196  ops/ms
EishayWriteStringTree.fastjson2                          thrpt    5   603.249 ± 30.222  ops/ms
EishayWriteStringTree.gson                               thrpt    5   169.894 ±  0.438  ops/ms
EishayWriteStringTree.jackson                            thrpt    5   411.374 ± 10.799  ops/ms
EishayWriteStringTree1x.fastjson1                        thrpt    5   324.977 ± 15.851  ops/ms
EishayWriteStringTree1x.fastjson2                        thrpt    5   442.740 ± 23.041  ops/ms
EishayWriteStringTree1x.gson                             thrpt    5   161.926 ±  9.401  ops/ms
EishayWriteStringTree1x.jackson                          thrpt    5   384.041 ± 10.223  ops/ms
EishayWriteUTF8Bytes.fastjson1                           thrpt    5   302.058 ± 19.041  ops/ms
EishayWriteUTF8Bytes.fastjson2                           thrpt    5   937.373 ± 27.991  ops/ms
EishayWriteUTF8Bytes.gson                                thrpt    5   159.198 ±  3.135  ops/ms
EishayWriteUTF8Bytes.jackson                             thrpt    5   427.692 ± 10.242  ops/ms
```
# OrangePi5-zulu8.68.0.21-ca-jdk8.0.362-linux_aarch64
```java
Benchmark                                                 Mode  Cnt     Score     Error   Units
EishayParseBinary.fastjson2UTF8Bytes                     thrpt    5   555.796 ±  41.295  ops/ms
EishayParseBinary.hessian                                thrpt    5   150.425 ±   6.162  ops/ms
EishayParseBinary.javaSerialize                          thrpt    5    26.933 ±   0.875  ops/ms
EishayParseBinary.jsonb                                  thrpt    5   994.987 ±  37.185  ops/ms
EishayParseBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   699.196 ±  41.179  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5   833.327 ±  38.294  ops/ms
EishayParseBinaryArrayMapping.jsonb                      thrpt    5  1727.008 ± 109.734  ops/ms
EishayParseBinaryArrayMapping.kryo                       thrpt    5  1010.769 ±  34.028  ops/ms
EishayParseBinaryArrayMapping.protobuf                   thrpt    5   632.625 ±  24.225  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB                 thrpt    5   638.954 ±  25.188  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB_autoTypeFilter  thrpt    5   635.161 ±   6.387  ops/ms
EishayParseBinaryAutoType.hessian                        thrpt    5   151.588 ±   6.149  ops/ms
EishayParseBinaryAutoType.javaSerialize                  thrpt    5    26.268 ±   0.864  ops/ms
EishayParseString.fastjson1                              thrpt    5   567.590 ±  22.017  ops/ms
EishayParseString.fastjson2                              thrpt    5   665.015 ±  29.505  ops/ms
EishayParseString.gson                                   thrpt    5   222.990 ±   9.187  ops/ms
EishayParseString.jackson                                thrpt    5   277.226 ±  10.737  ops/ms
EishayParseStringPretty.fastjson1                        thrpt    5   158.643 ±   5.876  ops/ms
EishayParseStringPretty.fastjson2                        thrpt    5   533.864 ±  32.399  ops/ms
EishayParseStringPretty.gson                             thrpt    5   206.878 ±   3.104  ops/ms
EishayParseStringPretty.jackson                          thrpt    5   249.753 ±   7.529  ops/ms
EishayParseTreeString.fastjson1                          thrpt    5   216.911 ±   9.362  ops/ms
EishayParseTreeString.fastjson2                          thrpt    5   444.139 ±   9.586  ops/ms
EishayParseTreeString.gson                               thrpt    5   181.385 ±   4.089  ops/ms
EishayParseTreeString.jackson                            thrpt    5   242.465 ±  12.496  ops/ms
EishayParseTreeStringPretty.fastjson1                    thrpt    5   212.190 ±   6.723  ops/ms
EishayParseTreeStringPretty.fastjson2                    thrpt    5   385.657 ±   6.569  ops/ms
EishayParseTreeStringPretty.gson                         thrpt    5   170.213 ±   6.342  ops/ms
EishayParseTreeStringPretty.jackson                      thrpt    5   224.752 ±   5.494  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                       thrpt    5   175.493 ±   9.621  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                       thrpt    5   405.948 ±  13.237  ops/ms
EishayParseTreeUTF8Bytes.gson                            thrpt    5   154.653 ±   2.751  ops/ms
EishayParseTreeUTF8Bytes.jackson                         thrpt    5   303.162 ±  14.904  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1                 thrpt    5   156.550 ±   9.981  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2                 thrpt    5   352.731 ±  24.676  ops/ms
EishayParseTreeUTF8BytesPretty.gson                      thrpt    5   145.519 ±   8.193  ops/ms
EishayParseTreeUTF8BytesPretty.jackson                   thrpt    5   275.149 ±  14.506  ops/ms
EishayParseUTF8Bytes.dsljson                             thrpt    5   467.420 ±  28.488  ops/ms
EishayParseUTF8Bytes.fastjson1                           thrpt    5   410.986 ±  18.197  ops/ms
EishayParseUTF8Bytes.fastjson2                           thrpt    5   545.892 ±  57.133  ops/ms
EishayParseUTF8Bytes.gson                                thrpt    5   156.605 ±   3.059  ops/ms
EishayParseUTF8Bytes.jackson                             thrpt    5   327.339 ±  16.794  ops/ms
EishayParseUTF8BytesPretty.fastjson1                     thrpt    5   142.099 ±   8.119  ops/ms
EishayParseUTF8BytesPretty.fastjson2                     thrpt    5   447.417 ±  29.145  ops/ms
EishayParseUTF8BytesPretty.gson                          thrpt    5   145.679 ±   5.299  ops/ms
EishayParseUTF8BytesPretty.jackson                       thrpt    5   298.190 ±   6.125  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes                     thrpt    5   825.019 ±  55.395  ops/ms
EishayWriteBinary.hessian                                thrpt    5   182.956 ±  21.785  ops/ms
EishayWriteBinary.javaSerialize                          thrpt    5   121.658 ±   4.403  ops/ms
EishayWriteBinary.jsonb                                  thrpt    5  1011.693 ±  18.574  ops/ms
EishayWriteBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   320.886 ±  19.041  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5   972.729 ±  41.666  ops/ms
EishayWriteBinaryArrayMapping.jsonb                      thrpt    5  1576.471 ±  18.746  ops/ms
EishayWriteBinaryArrayMapping.kryo                       thrpt    5   820.193 ±  34.331  ops/ms
EishayWriteBinaryArrayMapping.protobuf                   thrpt    5   696.930 ±  20.146  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB                 thrpt    5   695.575 ±  28.674  ops/ms
EishayWriteBinaryAutoType.fastjson2UTF8Bytes             thrpt    5   783.995 ±  64.048  ops/ms
EishayWriteBinaryAutoType.hessian                        thrpt    5   186.741 ±  36.788  ops/ms
EishayWriteBinaryAutoType.javaSerialize                  thrpt    5   121.849 ±   4.214  ops/ms
EishayWriteString.fastjson1                              thrpt    5   309.593 ±  20.028  ops/ms
EishayWriteString.fastjson2                              thrpt    5   700.600 ±  30.201  ops/ms
EishayWriteString.gson                                   thrpt    5   209.554 ±  10.636  ops/ms
EishayWriteString.jackson                                thrpt    5   397.103 ±  14.176  ops/ms
EishayWriteStringTree.fastjson1                          thrpt    5   334.141 ±   9.951  ops/ms
EishayWriteStringTree.fastjson2                          thrpt    5   486.045 ±  34.313  ops/ms
EishayWriteStringTree.gson                               thrpt    5   222.099 ±   9.271  ops/ms
EishayWriteStringTree.jackson                            thrpt    5   392.898 ±   2.476  ops/ms
EishayWriteStringTree1x.fastjson1                        thrpt    5   318.760 ±   3.531  ops/ms
EishayWriteStringTree1x.fastjson2                        thrpt    5   438.440 ±  16.394  ops/ms
EishayWriteStringTree1x.gson                             thrpt    5   226.655 ±  13.808  ops/ms
EishayWriteStringTree1x.jackson                          thrpt    5   367.268 ±  13.664  ops/ms
EishayWriteUTF8Bytes.fastjson1                           thrpt    5   281.028 ±  20.771  ops/ms
EishayWriteUTF8Bytes.fastjson2                           thrpt    5   826.952 ±  28.258  ops/ms
EishayWriteUTF8Bytes.gson                                thrpt    5   166.595 ±  12.435  ops/ms
EishayWriteUTF8Bytes.jackson                             thrpt    5   408.753 ±  20.644  ops/ms
```
# OrangePi5-zulu11.62.17-ca-jdk11.0.18-linux_aarch64
```java
Benchmark                                                 Mode  Cnt     Score    Error   Units
EishayParseBinary.fastjson2UTF8Bytes                     thrpt    5   711.279 ±  1.427  ops/ms
EishayParseBinary.hessian                                thrpt    5   136.731 ±  0.441  ops/ms
EishayParseBinary.javaSerialize                          thrpt    5    26.275 ±  1.097  ops/ms
EishayParseBinary.jsonb                                  thrpt    5  1362.969 ± 66.134  ops/ms
EishayParseBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   741.339 ± 11.360  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  1012.558 ± 31.331  ops/ms
EishayParseBinaryArrayMapping.jsonb                      thrpt    5  2090.915 ± 36.502  ops/ms
EishayParseBinaryArrayMapping.kryo                       thrpt    5   974.045 ± 19.989  ops/ms
EishayParseBinaryArrayMapping.protobuf                   thrpt    5   779.955 ± 24.322  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB                 thrpt    5   812.588 ± 21.869  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB_autoTypeFilter  thrpt    5   796.373 ± 17.931  ops/ms
EishayParseBinaryAutoType.hessian                        thrpt    5   138.747 ±  4.045  ops/ms
EishayParseBinaryAutoType.javaSerialize                  thrpt    5    26.637 ±  1.029  ops/ms
EishayParseString.fastjson1                              thrpt    5   585.042 ± 15.101  ops/ms
EishayParseString.fastjson2                              thrpt    5   674.736 ± 27.507  ops/ms
EishayParseString.gson                                   thrpt    5   227.834 ±  4.964  ops/ms
EishayParseString.jackson                                thrpt    5   266.435 ±  7.671  ops/ms
EishayParseStringPretty.fastjson1                        thrpt    5   162.592 ±  6.473  ops/ms
EishayParseStringPretty.fastjson2                        thrpt    5   518.258 ± 24.437  ops/ms
EishayParseStringPretty.gson                             thrpt    5   212.440 ±  4.889  ops/ms
EishayParseStringPretty.jackson                          thrpt    5   249.892 ±  7.647  ops/ms
EishayParseTreeString.fastjson1                          thrpt    5   245.358 ±  7.081  ops/ms
EishayParseTreeString.fastjson2                          thrpt    5   482.102 ± 10.983  ops/ms
EishayParseTreeString.gson                               thrpt    5   188.718 ±  3.287  ops/ms
EishayParseTreeString.jackson                            thrpt    5   251.589 ±  8.396  ops/ms
EishayParseTreeStringPretty.fastjson1                    thrpt    5   193.597 ±  3.893  ops/ms
EishayParseTreeStringPretty.fastjson2                    thrpt    5   392.030 ± 23.782  ops/ms
EishayParseTreeStringPretty.gson                         thrpt    5   176.505 ±  7.005  ops/ms
EishayParseTreeStringPretty.jackson                      thrpt    5   235.294 ±  5.202  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                       thrpt    5   211.636 ±  9.484  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                       thrpt    5   480.471 ±  2.444  ops/ms
EishayParseTreeUTF8Bytes.gson                            thrpt    5   177.063 ±  3.337  ops/ms
EishayParseTreeUTF8Bytes.jackson                         thrpt    5   294.423 ±  2.781  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1                 thrpt    5   170.215 ±  7.503  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2                 thrpt    5   382.339 ±  9.508  ops/ms
EishayParseTreeUTF8BytesPretty.gson                      thrpt    5   162.225 ±  6.082  ops/ms
EishayParseTreeUTF8BytesPretty.jackson                   thrpt    5   270.598 ±  8.307  ops/ms
EishayParseUTF8Bytes.dsljson                             thrpt    5   432.304 ± 22.644  ops/ms
EishayParseUTF8Bytes.fastjson1                           thrpt    5   458.947 ± 14.373  ops/ms
EishayParseUTF8Bytes.fastjson2                           thrpt    5   683.102 ± 12.181  ops/ms
EishayParseUTF8Bytes.gson                                thrpt    5   174.696 ±  5.952  ops/ms
EishayParseUTF8Bytes.jackson                             thrpt    5   308.228 ±  4.668  ops/ms
EishayParseUTF8BytesPretty.fastjson1                     thrpt    5   144.549 ±  5.627  ops/ms
EishayParseUTF8BytesPretty.fastjson2                     thrpt    5   514.590 ±  9.443  ops/ms
EishayParseUTF8BytesPretty.gson                          thrpt    5   166.300 ±  9.926  ops/ms
EishayParseUTF8BytesPretty.jackson                       thrpt    5   285.790 ±  0.696  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes                     thrpt    5   919.516 ± 38.409  ops/ms
EishayWriteBinary.hessian                                thrpt    5   196.823 ± 31.991  ops/ms
EishayWriteBinary.javaSerialize                          thrpt    5   133.532 ±  0.426  ops/ms
EishayWriteBinary.jsonb                                  thrpt    5  1352.470 ± 35.973  ops/ms
EishayWriteBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   309.575 ±  0.650  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  1063.086 ±  5.040  ops/ms
EishayWriteBinaryArrayMapping.jsonb                      thrpt    5  2340.972 ± 61.568  ops/ms
EishayWriteBinaryArrayMapping.kryo                       thrpt    5   994.653 ± 36.083  ops/ms
EishayWriteBinaryArrayMapping.protobuf                   thrpt    5   897.842 ± 33.493  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB                 thrpt    5   786.712 ± 23.311  ops/ms
EishayWriteBinaryAutoType.fastjson2UTF8Bytes             thrpt    5   908.625 ± 25.686  ops/ms
EishayWriteBinaryAutoType.hessian                        thrpt    5   198.441 ± 28.797  ops/ms
EishayWriteBinaryAutoType.javaSerialize                  thrpt    5   133.281 ±  4.000  ops/ms
EishayWriteString.fastjson1                              thrpt    5   297.573 ±  4.333  ops/ms
EishayWriteString.fastjson2                              thrpt    5   834.150 ± 42.875  ops/ms
EishayWriteString.gson                                   thrpt    5   209.395 ± 12.141  ops/ms
EishayWriteString.jackson                                thrpt    5   411.863 ±  1.519  ops/ms
EishayWriteStringTree.fastjson1                          thrpt    5   357.930 ± 12.370  ops/ms
EishayWriteStringTree.fastjson2                          thrpt    5   606.390 ± 16.142  ops/ms
EishayWriteStringTree.gson                               thrpt    5   219.910 ±  9.693  ops/ms
EishayWriteStringTree.jackson                            thrpt    5   445.661 ±  0.442  ops/ms
EishayWriteStringTree1x.fastjson1                        thrpt    5   353.544 ±  9.604  ops/ms
EishayWriteStringTree1x.fastjson2                        thrpt    5   553.419 ±  4.409  ops/ms
EishayWriteStringTree1x.gson                             thrpt    5   219.126 ±  6.606  ops/ms
EishayWriteStringTree1x.jackson                          thrpt    5   426.464 ±  6.488  ops/ms
EishayWriteUTF8Bytes.fastjson1                           thrpt    5   274.218 ± 12.625  ops/ms
EishayWriteUTF8Bytes.fastjson2                           thrpt    5   899.630 ±  9.794  ops/ms
EishayWriteUTF8Bytes.gson                                thrpt    5   192.954 ±  5.243  ops/ms
EishayWriteUTF8Bytes.jackson                             thrpt    5   380.068 ±  1.400  ops/ms
```
# OrangePi5-zulu17.40.19-ca-jdk17.0.6-linux_aarch64
```java
Benchmark                                                 Mode  Cnt     Score     Error   Units
EishayParseBinary.fastjson2UTF8Bytes                     thrpt    5   724.851 ±  27.439  ops/ms
EishayParseBinary.hessian                                thrpt    5   155.364 ±   4.449  ops/ms
EishayParseBinary.javaSerialize                          thrpt    5    28.476 ±   0.800  ops/ms
EishayParseBinary.jsonb                                  thrpt    5  1418.073 ±  39.394  ops/ms
EishayParseBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   871.177 ±  31.924  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  1032.381 ±  27.571  ops/ms
EishayParseBinaryArrayMapping.jsonb                      thrpt    5  2211.085 ±  92.177  ops/ms
EishayParseBinaryArrayMapping.kryo                       thrpt    5  1019.467 ±  38.085  ops/ms
EishayParseBinaryArrayMapping.protobuf                   thrpt    5   828.822 ±  44.786  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB                 thrpt    5   832.330 ±  46.907  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB_autoTypeFilter  thrpt    5   842.515 ±   9.645  ops/ms
EishayParseBinaryAutoType.hessian                        thrpt    5   154.060 ±   2.235  ops/ms
EishayParseBinaryAutoType.javaSerialize                  thrpt    5    29.019 ±   0.919  ops/ms
EishayParseString.fastjson1                              thrpt    5   806.739 ±  38.508  ops/ms
EishayParseString.fastjson2                              thrpt    5   695.457 ±  35.257  ops/ms
EishayParseString.gson                                   thrpt    5   219.122 ±   6.917  ops/ms
EishayParseString.jackson                                thrpt    5   281.711 ±  11.105  ops/ms
EishayParseStringPretty.fastjson1                        thrpt    5   184.000 ±   5.048  ops/ms
EishayParseStringPretty.fastjson2                        thrpt    5   516.241 ±  29.003  ops/ms
EishayParseStringPretty.gson                             thrpt    5   212.039 ±   6.142  ops/ms
EishayParseStringPretty.jackson                          thrpt    5   261.052 ±   7.885  ops/ms
EishayParseTreeString.fastjson1                          thrpt    5   276.605 ±  16.293  ops/ms
EishayParseTreeString.fastjson2                          thrpt    5   480.617 ±  25.368  ops/ms
EishayParseTreeString.gson                               thrpt    5   178.217 ±   8.378  ops/ms
EishayParseTreeString.jackson                            thrpt    5   265.280 ±  10.030  ops/ms
EishayParseTreeStringPretty.fastjson1                    thrpt    5   241.468 ±   9.184  ops/ms
EishayParseTreeStringPretty.fastjson2                    thrpt    5   392.474 ±  14.480  ops/ms
EishayParseTreeStringPretty.gson                         thrpt    5   169.928 ±   6.732  ops/ms
EishayParseTreeStringPretty.jackson                      thrpt    5   247.044 ±  13.285  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                       thrpt    5   236.399 ±   5.915  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                       thrpt    5   471.738 ±  12.129  ops/ms
EishayParseTreeUTF8Bytes.gson                            thrpt    5   168.196 ±   2.565  ops/ms
EishayParseTreeUTF8Bytes.jackson                         thrpt    5   316.144 ±  11.230  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1                 thrpt    5   199.342 ±   6.851  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2                 thrpt    5   389.049 ±  11.388  ops/ms
EishayParseTreeUTF8BytesPretty.gson                      thrpt    5   161.120 ±   8.159  ops/ms
EishayParseTreeUTF8BytesPretty.jackson                   thrpt    5   279.520 ±   9.355  ops/ms
EishayParseUTF8Bytes.dsljson                             thrpt    5   433.148 ±  24.317  ops/ms
EishayParseUTF8Bytes.fastjson1                           thrpt    5   583.093 ±  26.245  ops/ms
EishayParseUTF8Bytes.fastjson2                           thrpt    5   694.599 ±  50.189  ops/ms
EishayParseUTF8Bytes.gson                                thrpt    5   168.247 ±   3.894  ops/ms
EishayParseUTF8Bytes.jackson                             thrpt    5   308.434 ±  21.462  ops/ms
EishayParseUTF8BytesPretty.fastjson1                     thrpt    5   163.801 ±   5.673  ops/ms
EishayParseUTF8BytesPretty.fastjson2                     thrpt    5   516.891 ±   6.216  ops/ms
EishayParseUTF8BytesPretty.gson                          thrpt    5   159.014 ±   8.822  ops/ms
EishayParseUTF8BytesPretty.jackson                       thrpt    5   287.250 ±  10.906  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes                     thrpt    5   900.716 ±  19.688  ops/ms
EishayWriteBinary.hessian                                thrpt    5   205.378 ±  38.075  ops/ms
EishayWriteBinary.javaSerialize                          thrpt    5   132.539 ±   5.041  ops/ms
EishayWriteBinary.jsonb                                  thrpt    5  1420.531 ±  51.720  ops/ms
EishayWriteBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   321.735 ±  11.164  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  1055.631 ±  49.516  ops/ms
EishayWriteBinaryArrayMapping.jsonb                      thrpt    5  2377.107 ± 111.209  ops/ms
EishayWriteBinaryArrayMapping.kryo                       thrpt    5  1011.516 ±  34.528  ops/ms
EishayWriteBinaryArrayMapping.protobuf                   thrpt    5   917.968 ±  16.730  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB                 thrpt    5   781.496 ±  40.455  ops/ms
EishayWriteBinaryAutoType.fastjson2UTF8Bytes             thrpt    5   841.561 ±  38.057  ops/ms
EishayWriteBinaryAutoType.hessian                        thrpt    5   213.961 ±  18.605  ops/ms
EishayWriteBinaryAutoType.javaSerialize                  thrpt    5   132.455 ±   3.343  ops/ms
EishayWriteString.fastjson1                              thrpt    5   325.713 ±  11.944  ops/ms
EishayWriteString.fastjson2                              thrpt    5   836.831 ±  42.599  ops/ms
EishayWriteString.gson                                   thrpt    5   165.031 ±   5.965  ops/ms
EishayWriteString.jackson                                thrpt    5   453.249 ±  16.433  ops/ms
EishayWriteStringTree.fastjson1                          thrpt    5   378.950 ±  18.382  ops/ms
EishayWriteStringTree.fastjson2                          thrpt    5   596.849 ±   6.204  ops/ms
EishayWriteStringTree.gson                               thrpt    5   165.595 ±   3.017  ops/ms
EishayWriteStringTree.jackson                            thrpt    5   409.102 ±   9.047  ops/ms
EishayWriteStringTree1x.fastjson1                        thrpt    5   323.729 ±   9.912  ops/ms
EishayWriteStringTree1x.fastjson2                        thrpt    5   444.825 ±  23.446  ops/ms
EishayWriteStringTree1x.gson                             thrpt    5   161.835 ±   6.144  ops/ms
EishayWriteStringTree1x.jackson                          thrpt    5   383.973 ±  20.796  ops/ms
EishayWriteUTF8Bytes.fastjson1                           thrpt    5   294.593 ±  15.148  ops/ms
EishayWriteUTF8Bytes.fastjson2                           thrpt    5   909.607 ±  46.254  ops/ms
EishayWriteUTF8Bytes.gson                                thrpt    5   162.192 ±   6.537  ops/ms
EishayWriteUTF8Bytes.jackson                             thrpt    5   410.103 ±  14.699  ops/ms
```
# OrangePi5-graalvm-ce-java11-22.3.1
```java
Benchmark                                                 Mode  Cnt     Score     Error   Units
EishayParseBinary.fastjson2UTF8Bytes                     thrpt    5   522.426 ±   6.917  ops/ms
EishayParseBinary.hessian                                thrpt    5   116.169 ±   2.140  ops/ms
EishayParseBinary.javaSerialize                          thrpt    5    25.949 ±   0.651  ops/ms
EishayParseBinary.jsonb                                  thrpt    5  1439.219 ±  51.424  ops/ms
EishayParseBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   680.626 ±  22.138  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5   828.494 ±  35.097  ops/ms
EishayParseBinaryArrayMapping.jsonb                      thrpt    5  2720.290 ±  98.647  ops/ms
EishayParseBinaryArrayMapping.kryo                       thrpt    5   873.383 ±  41.114  ops/ms
EishayParseBinaryArrayMapping.protobuf                   thrpt    5   764.312 ±  34.982  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB                 thrpt    5   936.106 ±  22.987  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB_autoTypeFilter  thrpt    5   725.177 ±  23.968  ops/ms
EishayParseBinaryAutoType.hessian                        thrpt    5   109.969 ±   1.311  ops/ms
EishayParseBinaryAutoType.javaSerialize                  thrpt    5    25.765 ±   0.020  ops/ms
EishayParseString.fastjson1                              thrpt    5   552.149 ±  27.768  ops/ms
EishayParseString.fastjson2                              thrpt    5   632.079 ±  17.534  ops/ms
EishayParseString.gson                                   thrpt    5   239.426 ±  10.165  ops/ms
EishayParseString.jackson                                thrpt    5   258.340 ±   3.126  ops/ms
EishayParseStringPretty.fastjson1                        thrpt    5   156.470 ±   5.949  ops/ms
EishayParseStringPretty.fastjson2                        thrpt    5   494.209 ±  37.931  ops/ms
EishayParseStringPretty.gson                             thrpt    5   201.378 ±   5.099  ops/ms
EishayParseStringPretty.jackson                          thrpt    5   235.566 ±  10.425  ops/ms
EishayParseTreeString.fastjson1                          thrpt    5   260.688 ±   6.562  ops/ms
EishayParseTreeString.fastjson2                          thrpt    5   464.549 ±  31.245  ops/ms
EishayParseTreeString.gson                               thrpt    5   204.224 ±  14.048  ops/ms
EishayParseTreeString.jackson                            thrpt    5   287.791 ±  14.351  ops/ms
EishayParseTreeStringPretty.fastjson1                    thrpt    5   214.567 ±   8.323  ops/ms
EishayParseTreeStringPretty.fastjson2                    thrpt    5   387.511 ±  22.148  ops/ms
EishayParseTreeStringPretty.gson                         thrpt    5   191.719 ±   5.707  ops/ms
EishayParseTreeStringPretty.jackson                      thrpt    5   257.143 ±  12.956  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                       thrpt    5   209.872 ±   7.103  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                       thrpt    5   389.965 ±  19.065  ops/ms
EishayParseTreeUTF8Bytes.gson                            thrpt    5   175.960 ±   8.959  ops/ms
EishayParseTreeUTF8Bytes.jackson                         thrpt    5   287.581 ±  17.121  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1                 thrpt    5   171.753 ±   3.947  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2                 thrpt    5   319.826 ±  12.661  ops/ms
EishayParseTreeUTF8BytesPretty.gson                      thrpt    5   160.761 ±   3.971  ops/ms
EishayParseTreeUTF8BytesPretty.jackson                   thrpt    5   255.526 ±   9.499  ops/ms
EishayParseUTF8Bytes.dsljson                             thrpt    5   424.983 ±   3.489  ops/ms
EishayParseUTF8Bytes.fastjson1                           thrpt    5   388.579 ±  26.219  ops/ms
EishayParseUTF8Bytes.fastjson2                           thrpt    5   502.684 ±  12.110  ops/ms
EishayParseUTF8Bytes.gson                                thrpt    5   176.509 ±   4.562  ops/ms
EishayParseUTF8Bytes.jackson                             thrpt    5   283.009 ±   8.191  ops/ms
EishayParseUTF8BytesPretty.fastjson1                     thrpt    5   131.625 ±   4.883  ops/ms
EishayParseUTF8BytesPretty.fastjson2                     thrpt    5   389.482 ±  15.262  ops/ms
EishayParseUTF8BytesPretty.gson                          thrpt    5   160.810 ±   9.023  ops/ms
EishayParseUTF8BytesPretty.jackson                       thrpt    5   265.806 ±  21.482  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes                     thrpt    5   950.652 ±  26.953  ops/ms
EishayWriteBinary.hessian                                thrpt    5   116.997 ±   2.502  ops/ms
EishayWriteBinary.javaSerialize                          thrpt    5   108.007 ±   7.729  ops/ms
EishayWriteBinary.jsonb                                  thrpt    5  1741.166 ± 104.974  ops/ms
EishayWriteBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   332.970 ±   7.281  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  1041.300 ±  30.391  ops/ms
EishayWriteBinaryArrayMapping.jsonb                      thrpt    5  2151.304 ±  57.404  ops/ms
EishayWriteBinaryArrayMapping.kryo                       thrpt    5  1005.640 ±   3.464  ops/ms
EishayWriteBinaryArrayMapping.protobuf                   thrpt    5   848.629 ±   7.679  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB                 thrpt    5   785.729 ±  39.154  ops/ms
EishayWriteBinaryAutoType.fastjson2UTF8Bytes             thrpt    5   911.110 ±  17.714  ops/ms
EishayWriteBinaryAutoType.hessian                        thrpt    5   115.633 ±   4.208  ops/ms
EishayWriteBinaryAutoType.javaSerialize                  thrpt    5   113.070 ±   6.847  ops/ms
EishayWriteString.fastjson1                              thrpt    5   345.487 ±   9.221  ops/ms
EishayWriteString.fastjson2                              thrpt    5   902.480 ±  35.777  ops/ms
EishayWriteString.gson                                   thrpt    5   190.203 ±   5.199  ops/ms
EishayWriteString.jackson                                thrpt    5   422.514 ±  12.048  ops/ms
EishayWriteStringTree.fastjson1                          thrpt    5   249.597 ±   0.940  ops/ms
EishayWriteStringTree.fastjson2                          thrpt    5   692.056 ±  24.446  ops/ms
EishayWriteStringTree.gson                               thrpt    5   190.225 ±   8.861  ops/ms
EishayWriteStringTree.jackson                            thrpt    5   462.169 ±  14.494  ops/ms
EishayWriteStringTree1x.fastjson1                        thrpt    5   210.811 ±   1.132  ops/ms
EishayWriteStringTree1x.fastjson2                        thrpt    5   561.939 ±  27.980  ops/ms
EishayWriteStringTree1x.gson                             thrpt    5   181.530 ±   8.482  ops/ms
EishayWriteStringTree1x.jackson                          thrpt    5   403.872 ±  13.777  ops/ms
EishayWriteUTF8Bytes.fastjson1                           thrpt    5   280.481 ±  12.998  ops/ms
EishayWriteUTF8Bytes.fastjson2                           thrpt    5   957.745 ±  23.775  ops/ms
EishayWriteUTF8Bytes.gson                                thrpt    5   169.404 ±   7.678  ops/ms
EishayWriteUTF8Bytes.jackson                             thrpt    5   394.493 ±   7.909  ops/ms
```
# OrangePi5-graalvm-ce-java17-22.3.1
```java
Benchmark                                                 Mode  Cnt     Score     Error   Units
EishayParseBinary.fastjson2UTF8Bytes                     thrpt    5   556.587 ±  13.478  ops/ms
EishayParseBinary.hessian                                thrpt    5   119.943 ±   6.689  ops/ms
EishayParseBinary.javaSerialize                          thrpt    5    27.963 ±   0.256  ops/ms
EishayParseBinary.jsonb                                  thrpt    5  1723.632 ±  36.622  ops/ms
EishayParseBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   733.254 ±  28.755  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5   900.684 ±  20.720  ops/ms
EishayParseBinaryArrayMapping.jsonb                      thrpt    5  2873.038 ±  97.431  ops/ms
EishayParseBinaryArrayMapping.kryo                       thrpt    5   915.508 ±  38.829  ops/ms
EishayParseBinaryArrayMapping.protobuf                   thrpt    5  1011.623 ±  32.731  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB                 thrpt    5  1003.736 ±  38.237  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB_autoTypeFilter  thrpt    5   903.315 ±  23.455  ops/ms
EishayParseBinaryAutoType.hessian                        thrpt    5   120.538 ±   3.512  ops/ms
EishayParseBinaryAutoType.javaSerialize                  thrpt    5    27.393 ±   0.469  ops/ms
EishayParseString.fastjson1                              thrpt    5   715.484 ±  30.326  ops/ms
EishayParseString.fastjson2                              thrpt    5   690.514 ±  37.278  ops/ms
EishayParseString.gson                                   thrpt    5   229.838 ±   6.708  ops/ms
EishayParseString.jackson                                thrpt    5   279.878 ±  11.187  ops/ms
EishayParseStringPretty.fastjson1                        thrpt    5   192.104 ±   6.281  ops/ms
EishayParseStringPretty.fastjson2                        thrpt    5   525.441 ±  18.128  ops/ms
EishayParseStringPretty.gson                             thrpt    5   210.713 ±   5.505  ops/ms
EishayParseStringPretty.jackson                          thrpt    5   256.530 ±  12.804  ops/ms
EishayParseTreeString.fastjson1                          thrpt    5   335.267 ±   9.826  ops/ms
EishayParseTreeString.fastjson2                          thrpt    5   463.534 ±  19.671  ops/ms
EishayParseTreeString.gson                               thrpt    5   202.707 ±  10.912  ops/ms
EishayParseTreeString.jackson                            thrpt    5   298.068 ±  12.512  ops/ms
EishayParseTreeStringPretty.fastjson1                    thrpt    5   280.038 ±  20.596  ops/ms
EishayParseTreeStringPretty.fastjson2                    thrpt    5   398.432 ±  14.605  ops/ms
EishayParseTreeStringPretty.gson                         thrpt    5   189.870 ±   8.895  ops/ms
EishayParseTreeStringPretty.jackson                      thrpt    5   251.255 ±   9.026  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                       thrpt    5   256.258 ±  11.711  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                       thrpt    5   386.290 ±  17.015  ops/ms
EishayParseTreeUTF8Bytes.gson                            thrpt    5   177.774 ±   4.868  ops/ms
EishayParseTreeUTF8Bytes.jackson                         thrpt    5   298.348 ±  17.496  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1                 thrpt    5   208.435 ±   9.333  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2                 thrpt    5   323.853 ±   5.310  ops/ms
EishayParseTreeUTF8BytesPretty.gson                      thrpt    5   161.862 ±   7.281  ops/ms
EishayParseTreeUTF8BytesPretty.jackson                   thrpt    5   299.183 ±  16.615  ops/ms
EishayParseUTF8Bytes.dsljson                             thrpt    5   304.260 ±   8.000  ops/ms
EishayParseUTF8Bytes.fastjson1                           thrpt    5   477.918 ±  19.780  ops/ms
EishayParseUTF8Bytes.fastjson2                           thrpt    5   539.197 ±  24.446  ops/ms
EishayParseUTF8Bytes.gson                                thrpt    5   177.988 ±   4.005  ops/ms
EishayParseUTF8Bytes.jackson                             thrpt    5   296.896 ±  12.226  ops/ms
EishayParseUTF8BytesPretty.fastjson1                     thrpt    5   163.265 ±   4.923  ops/ms
EishayParseUTF8BytesPretty.fastjson2                     thrpt    5   403.853 ±   7.085  ops/ms
EishayParseUTF8BytesPretty.gson                          thrpt    5   162.122 ±   8.799  ops/ms
EishayParseUTF8BytesPretty.jackson                       thrpt    5   267.652 ±  13.199  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes                     thrpt    5   921.354 ±  44.027  ops/ms
EishayWriteBinary.hessian                                thrpt    5   167.107 ±  16.829  ops/ms
EishayWriteBinary.javaSerialize                          thrpt    5   107.845 ±   2.677  ops/ms
EishayWriteBinary.jsonb                                  thrpt    5  1644.749 ±  56.516  ops/ms
EishayWriteBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   351.792 ±  16.168  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  1172.832 ±  44.994  ops/ms
EishayWriteBinaryArrayMapping.jsonb                      thrpt    5  2503.800 ± 132.264  ops/ms
EishayWriteBinaryArrayMapping.kryo                       thrpt    5  1021.202 ±  29.709  ops/ms
EishayWriteBinaryArrayMapping.protobuf                   thrpt    5   977.120 ±  36.001  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB                 thrpt    5   804.700 ±  25.554  ops/ms
EishayWriteBinaryAutoType.fastjson2UTF8Bytes             thrpt    5   927.335 ±  23.609  ops/ms
EishayWriteBinaryAutoType.hessian                        thrpt    5   168.707 ±   7.543  ops/ms
EishayWriteBinaryAutoType.javaSerialize                  thrpt    5   107.005 ±   4.617  ops/ms
EishayWriteString.fastjson1                              thrpt    5   388.328 ±   6.243  ops/ms
EishayWriteString.fastjson2                              thrpt    5   976.700 ±  37.785  ops/ms
EishayWriteString.gson                                   thrpt    5   152.852 ±   7.255  ops/ms
EishayWriteString.jackson                                thrpt    5   450.476 ±  17.028  ops/ms
EishayWriteStringTree.fastjson1                          thrpt    5   400.559 ±  24.812  ops/ms
EishayWriteStringTree.fastjson2                          thrpt    5   701.439 ±  28.188  ops/ms
EishayWriteStringTree.gson                               thrpt    5   161.006 ±   1.853  ops/ms
EishayWriteStringTree.jackson                            thrpt    5   471.477 ±  21.594  ops/ms
EishayWriteStringTree1x.fastjson1                        thrpt    5   249.668 ±  13.793  ops/ms
EishayWriteStringTree1x.fastjson2                        thrpt    5   561.095 ±  21.575  ops/ms
EishayWriteStringTree1x.gson                             thrpt    5   150.530 ±   6.822  ops/ms
EishayWriteStringTree1x.jackson                          thrpt    5   422.762 ±  11.300  ops/ms
EishayWriteUTF8Bytes.fastjson1                           thrpt    5   285.561 ±   3.280  ops/ms
EishayWriteUTF8Bytes.fastjson2                           thrpt    5   916.122 ±  37.434  ops/ms
EishayWriteUTF8Bytes.gson                                thrpt    5   137.720 ±   0.447  ops/ms
EishayWriteUTF8Bytes.jackson                             thrpt    5   442.525 ±  14.231  ops/ms
```
# OrangePi5-graalvm-ee-java11-22.3.1
```java
Benchmark                                                 Mode  Cnt     Score     Error   Units
EishayParseBinary.fastjson2UTF8Bytes                     thrpt    5   771.924 ±  31.462  ops/ms
EishayParseBinary.hessian                                thrpt    5   191.830 ±   9.097  ops/ms
EishayParseBinary.javaSerialize                          thrpt    5    28.086 ±   0.752  ops/ms
EishayParseBinary.jsonb                                  thrpt    5  1996.320 ±  47.788  ops/ms
EishayParseBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   696.955 ±  30.319  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  1171.407 ±  64.978  ops/ms
EishayParseBinaryArrayMapping.jsonb                      thrpt    5  3299.025 ± 142.951  ops/ms
EishayParseBinaryArrayMapping.kryo                       thrpt    5   906.719 ±  26.687  ops/ms
EishayParseBinaryArrayMapping.protobuf                   thrpt    5  1248.175 ±  62.749  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB                 thrpt    5  1078.773 ±  61.461  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB_autoTypeFilter  thrpt    5   950.072 ±  21.193  ops/ms
EishayParseBinaryAutoType.hessian                        thrpt    5   170.881 ±   4.404  ops/ms
EishayParseBinaryAutoType.javaSerialize                  thrpt    5    28.532 ±   1.413  ops/ms
EishayParseString.fastjson1                              thrpt    5   592.693 ±  25.019  ops/ms
EishayParseString.fastjson2                              thrpt    5   776.001 ±  20.307  ops/ms
EishayParseString.gson                                   thrpt    5   224.473 ±   8.844  ops/ms
EishayParseString.jackson                                thrpt    5   266.690 ±  11.313  ops/ms
EishayParseStringPretty.fastjson1                        thrpt    5   178.705 ±   4.519  ops/ms
EishayParseStringPretty.fastjson2                        thrpt    5   590.046 ±  27.849  ops/ms
EishayParseStringPretty.gson                             thrpt    5   207.462 ±   7.426  ops/ms
EishayParseStringPretty.jackson                          thrpt    5   260.788 ±   6.592  ops/ms
EishayParseTreeString.fastjson1                          thrpt    5   326.643 ±  19.458  ops/ms
EishayParseTreeString.fastjson2                          thrpt    5   530.376 ±  26.627  ops/ms
EishayParseTreeString.gson                               thrpt    5   201.990 ±  10.628  ops/ms
EishayParseTreeString.jackson                            thrpt    5   286.000 ±   5.281  ops/ms
EishayParseTreeStringPretty.fastjson1                    thrpt    5   247.314 ±   2.926  ops/ms
EishayParseTreeStringPretty.fastjson2                    thrpt    5   458.085 ±  23.044  ops/ms
EishayParseTreeStringPretty.gson                         thrpt    5   195.544 ±   6.917  ops/ms
EishayParseTreeStringPretty.jackson                      thrpt    5   248.232 ±   9.639  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                       thrpt    5   246.370 ±  14.212  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                       thrpt    5   498.590 ±  19.872  ops/ms
EishayParseTreeUTF8Bytes.gson                            thrpt    5   183.011 ±   6.851  ops/ms
EishayParseTreeUTF8Bytes.jackson                         thrpt    5   316.264 ±  16.678  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1                 thrpt    5   203.220 ±  19.264  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2                 thrpt    5   426.711 ±  15.240  ops/ms
EishayParseTreeUTF8BytesPretty.gson                      thrpt    5   171.959 ±   8.597  ops/ms
EishayParseTreeUTF8BytesPretty.jackson                   thrpt    5   277.139 ±   5.060  ops/ms
EishayParseUTF8Bytes.dsljson                             thrpt    5   464.165 ±  32.829  ops/ms
EishayParseUTF8Bytes.fastjson1                           thrpt    5   401.703 ±   9.754  ops/ms
EishayParseUTF8Bytes.fastjson2                           thrpt    5   704.301 ±  36.451  ops/ms
EishayParseUTF8Bytes.gson                                thrpt    5   186.554 ±  30.373  ops/ms
EishayParseUTF8Bytes.jackson                             thrpt    5   282.421 ±   1.934  ops/ms
EishayParseUTF8BytesPretty.fastjson1                     thrpt    5   144.071 ±   4.288  ops/ms
EishayParseUTF8BytesPretty.fastjson2                     thrpt    5   531.182 ±   5.201  ops/ms
EishayParseUTF8BytesPretty.gson                          thrpt    5   172.719 ±   7.727  ops/ms
EishayParseUTF8BytesPretty.jackson                       thrpt    5   253.315 ±   3.083  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes                     thrpt    5  1081.783 ±  28.177  ops/ms
EishayWriteBinary.hessian                                thrpt    5   211.489 ±  13.310  ops/ms
EishayWriteBinary.javaSerialize                          thrpt    5   127.575 ±   0.989  ops/ms
EishayWriteBinary.jsonb                                  thrpt    5  1851.745 ±  32.742  ops/ms
EishayWriteBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   323.103 ±  16.027  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  1249.080 ±  23.637  ops/ms
EishayWriteBinaryArrayMapping.jsonb                      thrpt    5  3081.854 ±  62.133  ops/ms
EishayWriteBinaryArrayMapping.kryo                       thrpt    5  1256.190 ±  56.667  ops/ms
EishayWriteBinaryArrayMapping.protobuf                   thrpt    5  1628.710 ±  51.711  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB                 thrpt    5   873.840 ±  25.162  ops/ms
EishayWriteBinaryAutoType.fastjson2UTF8Bytes             thrpt    5  1060.122 ±  53.110  ops/ms
EishayWriteBinaryAutoType.hessian                        thrpt    5   206.628 ±   9.223  ops/ms
EishayWriteBinaryAutoType.javaSerialize                  thrpt    5   131.239 ±   1.320  ops/ms
EishayWriteString.fastjson1                              thrpt    5   409.167 ±   5.643  ops/ms
EishayWriteString.fastjson2                              thrpt    5  1036.237 ±  54.840  ops/ms
EishayWriteString.gson                                   thrpt    5   213.704 ±   5.264  ops/ms
EishayWriteString.jackson                                thrpt    5   496.696 ±  30.331  ops/ms
EishayWriteStringTree.fastjson1                          thrpt    5   398.441 ±   9.707  ops/ms
EishayWriteStringTree.fastjson2                          thrpt    5   719.490 ±  36.691  ops/ms
EishayWriteStringTree.gson                               thrpt    5   252.031 ±   4.165  ops/ms
EishayWriteStringTree.jackson                            thrpt    5   511.884 ±   5.652  ops/ms
EishayWriteStringTree1x.fastjson1                        thrpt    5   415.642 ±   7.926  ops/ms
EishayWriteStringTree1x.fastjson2                        thrpt    5   646.207 ±  37.070  ops/ms
EishayWriteStringTree1x.gson                             thrpt    5   233.697 ±   3.782  ops/ms
EishayWriteStringTree1x.jackson                          thrpt    5   482.118 ±  14.956  ops/ms
EishayWriteUTF8Bytes.fastjson1                           thrpt    5   313.015 ±   4.907  ops/ms
EishayWriteUTF8Bytes.fastjson2                           thrpt    5  1075.593 ±  46.590  ops/ms
EishayWriteUTF8Bytes.gson                                thrpt    5   204.853 ±   1.971  ops/ms
EishayWriteUTF8Bytes.jackson                             thrpt    5   502.284 ±  16.191  ops/ms
```
# OrangePi5-graalvm-ee-java17-22.3.1
```java
Benchmark                                                 Mode  Cnt     Score     Error   Units
EishayParseBinary.fastjson2UTF8Bytes                     thrpt    5   783.483 ±  33.439  ops/ms
EishayParseBinary.hessian                                thrpt    5   201.715 ±   7.967  ops/ms
EishayParseBinary.javaSerialize                          thrpt    5    30.290 ±   0.108  ops/ms
EishayParseBinary.jsonb                                  thrpt    5  2020.125 ±  22.043  ops/ms
EishayParseBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   676.812 ±   9.814  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  1281.235 ±  41.653  ops/ms
EishayParseBinaryArrayMapping.jsonb                      thrpt    5  3866.281 ± 304.049  ops/ms
EishayParseBinaryArrayMapping.kryo                       thrpt    5   938.393 ±  16.610  ops/ms
EishayParseBinaryArrayMapping.protobuf                   thrpt    5  1549.284 ±  40.493  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB                 thrpt    5  1246.679 ±  23.763  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB_autoTypeFilter  thrpt    5  1028.082 ±  77.919  ops/ms
EishayParseBinaryAutoType.hessian                        thrpt    5   207.444 ±   3.617  ops/ms
EishayParseBinaryAutoType.javaSerialize                  thrpt    5    29.796 ±   1.718  ops/ms
EishayParseString.fastjson1                              thrpt    5   920.545 ±  35.508  ops/ms
EishayParseString.fastjson2                              thrpt    5   774.152 ±  25.129  ops/ms
EishayParseString.gson                                   thrpt    5   243.223 ±   7.047  ops/ms
EishayParseString.jackson                                thrpt    5   294.380 ±   8.499  ops/ms
EishayParseStringPretty.fastjson1                        thrpt    5   222.694 ±  11.082  ops/ms
EishayParseStringPretty.fastjson2                        thrpt    5   593.730 ±  17.295  ops/ms
EishayParseStringPretty.gson                             thrpt    5   222.444 ±   8.122  ops/ms
EishayParseStringPretty.jackson                          thrpt    5   265.651 ±   2.999  ops/ms
EishayParseTreeString.fastjson1                          thrpt    5   383.122 ±   9.029  ops/ms
EishayParseTreeString.fastjson2                          thrpt    5   544.288 ±   9.191  ops/ms
EishayParseTreeString.gson                               thrpt    5   201.140 ±   6.990  ops/ms
EishayParseTreeString.jackson                            thrpt    5   300.762 ±  14.545  ops/ms
EishayParseTreeStringPretty.fastjson1                    thrpt    5   314.482 ±   9.967  ops/ms
EishayParseTreeStringPretty.fastjson2                    thrpt    5   470.702 ±  13.594  ops/ms
EishayParseTreeStringPretty.gson                         thrpt    5   192.114 ±   7.189  ops/ms
EishayParseTreeStringPretty.jackson                      thrpt    5   272.185 ±  14.017  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                       thrpt    5   276.037 ±   9.669  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                       thrpt    5   506.587 ±  20.098  ops/ms
EishayParseTreeUTF8Bytes.gson                            thrpt    5   195.328 ±   7.863  ops/ms
EishayParseTreeUTF8Bytes.jackson                         thrpt    5   320.130 ±   5.654  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1                 thrpt    5   224.057 ±   6.278  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2                 thrpt    5   428.829 ±  12.831  ops/ms
EishayParseTreeUTF8BytesPretty.gson                      thrpt    5   177.688 ±  19.815  ops/ms
EishayParseTreeUTF8BytesPretty.jackson                   thrpt    5   281.513 ±   8.207  ops/ms
EishayParseUTF8Bytes.dsljson                             thrpt    5   450.626 ±   8.127  ops/ms
EishayParseUTF8Bytes.fastjson1                           thrpt    5   518.567 ±  17.115  ops/ms
EishayParseUTF8Bytes.fastjson2                           thrpt    5   695.452 ±  20.270  ops/ms
EishayParseUTF8Bytes.gson                                thrpt    5   187.919 ±   6.112  ops/ms
EishayParseUTF8Bytes.jackson                             thrpt    5   298.250 ±   9.305  ops/ms
EishayParseUTF8BytesPretty.fastjson1                     thrpt    5   175.382 ±   8.195  ops/ms
EishayParseUTF8BytesPretty.fastjson2                     thrpt    5   528.611 ±  16.639  ops/ms
EishayParseUTF8BytesPretty.gson                          thrpt    5   180.183 ±  10.382  ops/ms
EishayParseUTF8BytesPretty.jackson                       thrpt    5   267.256 ±  12.136  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes                     thrpt    5  1097.688 ±  37.963  ops/ms
EishayWriteBinary.hessian                                thrpt    5   211.246 ±  44.295  ops/ms
EishayWriteBinary.javaSerialize                          thrpt    5   124.469 ±   1.233  ops/ms
EishayWriteBinary.jsonb                                  thrpt    5  1895.286 ±  21.015  ops/ms
EishayWriteBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   414.707 ±  17.235  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  1269.576 ±  52.663  ops/ms
EishayWriteBinaryArrayMapping.jsonb                      thrpt    5  3635.466 ± 202.146  ops/ms
EishayWriteBinaryArrayMapping.kryo                       thrpt    5  1274.430 ±  58.003  ops/ms
EishayWriteBinaryArrayMapping.protobuf                   thrpt    5  1638.695 ±  45.611  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB                 thrpt    5   899.851 ±  11.825  ops/ms
EishayWriteBinaryAutoType.fastjson2UTF8Bytes             thrpt    5  1069.621 ±  25.891  ops/ms
EishayWriteBinaryAutoType.hessian                        thrpt    5   222.860 ±  46.584  ops/ms
EishayWriteBinaryAutoType.javaSerialize                  thrpt    5   127.619 ±   6.659  ops/ms
EishayWriteString.fastjson1                              thrpt    5   416.465 ±  20.792  ops/ms
EishayWriteString.fastjson2                              thrpt    5  1022.405 ±  28.051  ops/ms
EishayWriteString.gson                                   thrpt    5   164.976 ±   5.364  ops/ms
EishayWriteString.jackson                                thrpt    5   512.964 ±  16.865  ops/ms
EishayWriteStringTree.fastjson1                          thrpt    5   406.072 ±  11.691  ops/ms
EishayWriteStringTree.fastjson2                          thrpt    5   723.293 ±  29.856  ops/ms
EishayWriteStringTree.gson                               thrpt    5   181.882 ±   7.208  ops/ms
EishayWriteStringTree.jackson                            thrpt    5   516.136 ±  18.508  ops/ms
EishayWriteStringTree1x.fastjson1                        thrpt    5   426.577 ±  18.246  ops/ms
EishayWriteStringTree1x.fastjson2                        thrpt    5   648.984 ±  26.698  ops/ms
EishayWriteStringTree1x.gson                             thrpt    5   174.238 ±   4.611  ops/ms
EishayWriteStringTree1x.jackson                          thrpt    5   485.723 ±   6.379  ops/ms
EishayWriteUTF8Bytes.fastjson1                           thrpt    5   415.298 ±  13.001  ops/ms
EishayWriteUTF8Bytes.fastjson2                           thrpt    5  1096.023 ±  23.293  ops/ms
EishayWriteUTF8Bytes.gson                                thrpt    5   156.955 ±   3.735  ops/ms
EishayWriteUTF8Bytes.jackson                             thrpt    5   551.924 ±  25.432  ops/ms
```

# AppleM1Pro-zulu-8.jdk
```java
Benchmark                                                 Mode  Cnt     Score     Error   Units
EishayParseBinary.fastjson2UTF8Bytes                     thrpt    5  1381.883 ±  88.703  ops/ms
EishayParseBinary.hessian                                thrpt    5   381.224 ±   9.123  ops/ms
EishayParseBinary.javaSerialize                          thrpt    5    57.986 ±   0.125  ops/ms
EishayParseBinary.jsonb                                  thrpt    5  2854.396 ±  52.251  ops/ms
EishayParseBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5  2475.166 ±   8.269  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  2005.356 ±   6.899  ops/ms
EishayParseBinaryArrayMapping.jsonb                      thrpt    5  5363.094 ±  15.867  ops/ms
EishayParseBinaryArrayMapping.kryo                       thrpt    5  2136.900 ± 408.735  ops/ms
EishayParseBinaryArrayMapping.protobuf                   thrpt    5  1636.373 ±  18.188  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB                 thrpt    5  2543.596 ±  11.992  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB_autoTypeFilter  thrpt    5  2400.162 ±   3.763  ops/ms
EishayParseBinaryAutoType.hessian                        thrpt    5   382.938 ±  60.645  ops/ms
EishayParseBinaryAutoType.javaSerialize                  thrpt    5    58.475 ±   0.786  ops/ms
EishayParseString.fastjson1                              thrpt    5  1523.559 ±  33.004  ops/ms
EishayParseString.fastjson2                              thrpt    5  1800.005 ±  26.160  ops/ms
EishayParseString.gson                                   thrpt    5   656.160 ±  12.186  ops/ms
EishayParseString.jackson                                thrpt    5   746.680 ±  12.280  ops/ms
EishayParseStringPretty.fastjson1                        thrpt    5   437.956 ±  19.506  ops/ms
EishayParseStringPretty.fastjson2                        thrpt    5  1504.423 ±  42.809  ops/ms
EishayParseStringPretty.gson                             thrpt    5   619.225 ±  13.529  ops/ms
EishayParseStringPretty.jackson                          thrpt    5   686.434 ±  46.747  ops/ms
EishayParseTreeString.fastjson1                          thrpt    5   682.974 ±  20.248  ops/ms
EishayParseTreeString.fastjson2                          thrpt    5  1383.601 ±  26.211  ops/ms
EishayParseTreeString.gson                               thrpt    5   543.104 ±  34.923  ops/ms
EishayParseTreeString.jackson                            thrpt    5   745.159 ±  16.571  ops/ms
EishayParseTreeStringPretty.fastjson1                    thrpt    5   644.034 ±  18.637  ops/ms
EishayParseTreeStringPretty.fastjson2                    thrpt    5  1211.350 ±  26.575  ops/ms
EishayParseTreeStringPretty.gson                         thrpt    5   520.598 ±  24.560  ops/ms
EishayParseTreeStringPretty.jackson                      thrpt    5   678.861 ±  20.239  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                       thrpt    5   619.412 ±  17.674  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                       thrpt    5  1361.940 ±  10.497  ops/ms
EishayParseTreeUTF8Bytes.gson                            thrpt    5   598.760 ±   7.763  ops/ms
EishayParseTreeUTF8Bytes.jackson                         thrpt    5   920.820 ±  43.708  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1                 thrpt    5   591.193 ±   7.421  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2                 thrpt    5  1167.444 ±  13.260  ops/ms
EishayParseTreeUTF8BytesPretty.gson                      thrpt    5   511.854 ±   7.979  ops/ms
EishayParseTreeUTF8BytesPretty.jackson                   thrpt    5   816.152 ±   8.657  ops/ms
EishayParseUTF8Bytes.dsljson                             thrpt    5  1033.840 ±  72.252  ops/ms
EishayParseUTF8Bytes.fastjson1                           thrpt    5  1467.446 ±  23.625  ops/ms
EishayParseUTF8Bytes.fastjson2                           thrpt    5  1359.371 ± 119.698  ops/ms
EishayParseUTF8Bytes.gson                                thrpt    5   592.320 ±  10.447  ops/ms
EishayParseUTF8Bytes.jackson                             thrpt    5  1001.071 ±  13.557  ops/ms
EishayParseUTF8BytesPretty.fastjson1                     thrpt    5   429.316 ±  12.555  ops/ms
EishayParseUTF8BytesPretty.fastjson2                     thrpt    5  1155.452 ±  71.246  ops/ms
EishayParseUTF8BytesPretty.gson                          thrpt    5   546.727 ±   7.910  ops/ms
EishayParseUTF8BytesPretty.jackson                       thrpt    5   901.314 ±  19.555  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes                     thrpt    5  2871.151 ±  53.059  ops/ms
EishayWriteBinary.hessian                                thrpt    5   629.175 ±  15.047  ops/ms
EishayWriteBinary.javaSerialize                          thrpt    5   424.644 ±   8.588  ops/ms
EishayWriteBinary.jsonb                                  thrpt    5  3637.938 ±  43.101  ops/ms
EishayWriteBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5  1141.010 ±  21.848  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  3199.541 ±  64.899  ops/ms
EishayWriteBinaryArrayMapping.jsonb                      thrpt    5  5819.786 ±  53.568  ops/ms
EishayWriteBinaryArrayMapping.kryo                       thrpt    5  2535.149 ±  33.532  ops/ms
EishayWriteBinaryArrayMapping.protobuf                   thrpt    5  2214.889 ±  30.083  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB                 thrpt    5  2486.244 ±  28.336  ops/ms
EishayWriteBinaryAutoType.fastjson2UTF8Bytes             thrpt    5  2808.383 ±  11.279  ops/ms
EishayWriteBinaryAutoType.hessian                        thrpt    5   616.722 ±  14.897  ops/ms
EishayWriteBinaryAutoType.javaSerialize                  thrpt    5   437.507 ±   3.239  ops/ms
EishayWriteString.fastjson1                              thrpt    5  1231.183 ±   4.586  ops/ms
EishayWriteString.fastjson2                              thrpt    5  2920.647 ±   8.164  ops/ms
EishayWriteString.gson                                   thrpt    5   755.440 ±   2.378  ops/ms
EishayWriteString.jackson                                thrpt    5  1725.935 ±   8.199  ops/ms
EishayWriteStringTree.fastjson1                          thrpt    5  1284.724 ±   4.124  ops/ms
EishayWriteStringTree.fastjson2                          thrpt    5  1880.707 ±   3.874  ops/ms
EishayWriteStringTree.gson                               thrpt    5   836.171 ±  12.315  ops/ms
EishayWriteStringTree.jackson                            thrpt    5  1582.641 ±   2.663  ops/ms
EishayWriteStringTree1x.fastjson1                        thrpt    5  1233.677 ±   2.674  ops/ms
EishayWriteStringTree1x.fastjson2                        thrpt    5  1708.182 ±   4.164  ops/ms
EishayWriteStringTree1x.gson                             thrpt    5   794.787 ±   4.334  ops/ms
EishayWriteStringTree1x.jackson                          thrpt    5  1475.124 ±   4.561  ops/ms
EishayWriteUTF8Bytes.fastjson1                           thrpt    5  1073.020 ±   5.562  ops/ms
EishayWriteUTF8Bytes.fastjson2                           thrpt    5  2867.095 ±  45.716  ops/ms
EishayWriteUTF8Bytes.gson                                thrpt    5   664.498 ±   7.160  ops/ms
EishayWriteUTF8Bytes.jackson                             thrpt    5  1580.438 ±  13.893  ops/ms
```
# AppleM1Pro-zulu-11.jdk
```java
Benchmark                                                 Mode  Cnt     Score     Error   Units
EishayParseBinary.fastjson2UTF8Bytes                     thrpt    5  1770.164 ±  21.448  ops/ms
EishayParseBinary.hessian                                thrpt    5   339.336 ±   5.060  ops/ms
EishayParseBinary.javaSerialize                          thrpt    5    58.569 ±   2.292  ops/ms
EishayParseBinary.jsonb                                  thrpt    5  3018.042 ±  14.990  ops/ms
EishayParseBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5  2496.044 ±  61.514  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  2887.896 ±  23.506  ops/ms
EishayParseBinaryArrayMapping.jsonb                      thrpt    5  6349.479 ±  55.408  ops/ms
EishayParseBinaryArrayMapping.kryo                       thrpt    5  1777.819 ±  94.334  ops/ms
EishayParseBinaryArrayMapping.protobuf                   thrpt    5  1944.279 ±  12.474  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB                 thrpt    5  3104.450 ±  23.809  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB_autoTypeFilter  thrpt    5  2856.737 ±  28.874  ops/ms
EishayParseBinaryAutoType.hessian                        thrpt    5   378.538 ±   5.197  ops/ms
EishayParseBinaryAutoType.javaSerialize                  thrpt    5    61.460 ±   0.972  ops/ms
EishayParseString.fastjson1                              thrpt    5  1402.328 ±  13.977  ops/ms
EishayParseString.fastjson2                              thrpt    5  1876.148 ±  14.896  ops/ms
EishayParseString.gson                                   thrpt    5   734.055 ±   6.304  ops/ms
EishayParseString.jackson                                thrpt    5   728.383 ±  11.330  ops/ms
EishayParseStringPretty.fastjson1                        thrpt    5   330.791 ±  36.973  ops/ms
EishayParseStringPretty.fastjson2                        thrpt    5  1603.484 ±  18.681  ops/ms
EishayParseStringPretty.gson                             thrpt    5   652.634 ±  54.398  ops/ms
EishayParseStringPretty.jackson                          thrpt    5   685.482 ±  19.838  ops/ms
EishayParseTreeString.fastjson1                          thrpt    5   518.881 ±  17.055  ops/ms
EishayParseTreeString.fastjson2                          thrpt    5  1249.748 ±  10.124  ops/ms
EishayParseTreeString.gson                               thrpt    5   571.768 ±   7.437  ops/ms
EishayParseTreeString.jackson                            thrpt    5   711.815 ±   8.075  ops/ms
EishayParseTreeStringPretty.fastjson1                    thrpt    5   404.686 ±  27.106  ops/ms
EishayParseTreeStringPretty.fastjson2                    thrpt    5  1074.933 ±   9.372  ops/ms
EishayParseTreeStringPretty.gson                         thrpt    5   526.252 ±   4.808  ops/ms
EishayParseTreeStringPretty.jackson                      thrpt    5   655.612 ±   5.496  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                       thrpt    5   514.451 ±  12.301  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                       thrpt    5  1563.084 ±   1.811  ops/ms
EishayParseTreeUTF8Bytes.gson                            thrpt    5   568.977 ±   5.250  ops/ms
EishayParseTreeUTF8Bytes.jackson                         thrpt    5   934.421 ±  79.313  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1                 thrpt    5   401.633 ±  20.802  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2                 thrpt    5  1256.396 ±   2.880  ops/ms
EishayParseTreeUTF8BytesPretty.gson                      thrpt    5   558.940 ±   4.839  ops/ms
EishayParseTreeUTF8BytesPretty.jackson                   thrpt    5   844.695 ±  77.810  ops/ms
EishayParseUTF8Bytes.dsljson                             thrpt    5   662.507 ± 113.175  ops/ms
EishayParseUTF8Bytes.fastjson1                           thrpt    5  1288.777 ±  22.619  ops/ms
EishayParseUTF8Bytes.fastjson2                           thrpt    5  1683.926 ±  28.093  ops/ms
EishayParseUTF8Bytes.gson                                thrpt    5   563.696 ±   3.202  ops/ms
EishayParseUTF8Bytes.jackson                             thrpt    5   860.343 ±  21.197  ops/ms
EishayParseUTF8BytesPretty.fastjson1                     thrpt    5   338.365 ±  11.794  ops/ms
EishayParseUTF8BytesPretty.fastjson2                     thrpt    5  1510.996 ±  20.368  ops/ms
EishayParseUTF8BytesPretty.gson                          thrpt    5   561.573 ±   2.511  ops/ms
EishayParseUTF8BytesPretty.jackson                       thrpt    5   792.162 ±  35.172  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes                     thrpt    5  3123.999 ±  21.286  ops/ms
EishayWriteBinary.hessian                                thrpt    5   693.634 ±   6.645  ops/ms
EishayWriteBinary.javaSerialize                          thrpt    5   437.251 ±   3.893  ops/ms
EishayWriteBinary.jsonb                                  thrpt    5  4926.143 ±  66.342  ops/ms
EishayWriteBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5  1051.256 ±  12.108  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  3412.018 ±  28.574  ops/ms
EishayWriteBinaryArrayMapping.jsonb                      thrpt    5  9443.866 ±  69.441  ops/ms
EishayWriteBinaryArrayMapping.kryo                       thrpt    5  2913.971 ±  33.248  ops/ms
EishayWriteBinaryArrayMapping.protobuf                   thrpt    5  2299.246 ±  19.784  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB                 thrpt    5  2911.123 ±  29.179  ops/ms
EishayWriteBinaryAutoType.fastjson2UTF8Bytes             thrpt    5  3046.631 ±  33.703  ops/ms
EishayWriteBinaryAutoType.hessian                        thrpt    5   678.153 ±   5.118  ops/ms
EishayWriteBinaryAutoType.javaSerialize                  thrpt    5   434.057 ±   4.375  ops/ms
EishayWriteString.fastjson1                              thrpt    5   873.541 ±  15.081  ops/ms
EishayWriteString.fastjson2                              thrpt    5  2905.056 ±  34.009  ops/ms
EishayWriteString.gson                                   thrpt    5   660.575 ±  11.783  ops/ms
EishayWriteString.jackson                                thrpt    5  1667.165 ±  29.720  ops/ms
EishayWriteStringTree.fastjson1                          thrpt    5  1205.387 ±  19.738  ops/ms
EishayWriteStringTree.fastjson2                          thrpt    5  1948.896 ±  35.066  ops/ms
EishayWriteStringTree.gson                               thrpt    5   698.374 ±  12.200  ops/ms
EishayWriteStringTree.jackson                            thrpt    5  1538.286 ±  25.872  ops/ms
EishayWriteStringTree1x.fastjson1                        thrpt    5  1203.686 ±  20.413  ops/ms
EishayWriteStringTree1x.fastjson2                        thrpt    5  1774.141 ±  21.423  ops/ms
EishayWriteStringTree1x.gson                             thrpt    5   681.528 ±   4.600  ops/ms
EishayWriteStringTree1x.jackson                          thrpt    5  1495.499 ±  18.551  ops/ms
EishayWriteUTF8Bytes.fastjson1                           thrpt    5  1050.521 ±  10.007  ops/ms
EishayWriteUTF8Bytes.fastjson2                           thrpt    5  3137.584 ±  59.178  ops/ms
EishayWriteUTF8Bytes.gson                                thrpt    5   656.054 ±   7.325  ops/ms
EishayWriteUTF8Bytes.jackson                             thrpt    5  1400.792 ±  23.806  ops/ms
```
# AppleM1Pro-zulu-17.jdk
```java
Benchmark                                                 Mode  Cnt     Score     Error   Units
EishayParseBinary.fastjson2UTF8Bytes                     thrpt    5  1692.574 ±  41.057  ops/ms
EishayParseBinary.hessian                                thrpt    5   339.523 ±   5.716  ops/ms
EishayParseBinary.javaSerialize                          thrpt    5    66.736 ±   2.078  ops/ms
EishayParseBinary.jsonb                                  thrpt    5  3389.412 ±  81.366  ops/ms
EishayParseBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5  2600.329 ±  16.494  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  2805.843 ±  44.388  ops/ms
EishayParseBinaryArrayMapping.jsonb                      thrpt    5  6593.366 ±  18.033  ops/ms
EishayParseBinaryArrayMapping.kryo                       thrpt    5  1792.093 ± 103.029  ops/ms
EishayParseBinaryArrayMapping.protobuf                   thrpt    5  1942.654 ±  52.806  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB                 thrpt    5  3204.135 ±  32.469  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB_autoTypeFilter  thrpt    5  2958.608 ±  28.167  ops/ms
EishayParseBinaryAutoType.hessian                        thrpt    5   328.210 ±  23.215  ops/ms
EishayParseBinaryAutoType.javaSerialize                  thrpt    5    67.910 ±   5.256  ops/ms
EishayParseString.fastjson1                              thrpt    5  2099.912 ± 163.070  ops/ms
EishayParseString.fastjson2                              thrpt    5  1943.998 ±  58.207  ops/ms
EishayParseString.gson                                   thrpt    5   708.294 ±  17.535  ops/ms
EishayParseString.jackson                                thrpt    5   659.946 ±  84.229  ops/ms
EishayParseStringPretty.fastjson1                        thrpt    5   406.564 ±  37.399  ops/ms
EishayParseStringPretty.fastjson2                        thrpt    5  1626.052 ±  30.740  ops/ms
EishayParseStringPretty.gson                             thrpt    5   703.953 ±  10.381  ops/ms
EishayParseStringPretty.jackson                          thrpt    5   612.241 ±  41.064  ops/ms
EishayParseTreeString.fastjson1                          thrpt    5   773.547 ±  40.631  ops/ms
EishayParseTreeString.fastjson2                          thrpt    5  1500.900 ±  56.012  ops/ms
EishayParseTreeString.gson                               thrpt    5   551.099 ±   4.161  ops/ms
EishayParseTreeString.jackson                            thrpt    5   778.360 ±   7.307  ops/ms
EishayParseTreeStringPretty.fastjson1                    thrpt    5   693.035 ±  19.306  ops/ms
EishayParseTreeStringPretty.fastjson2                    thrpt    5  1204.443 ±  17.613  ops/ms
EishayParseTreeStringPretty.gson                         thrpt    5   523.800 ±   5.715  ops/ms
EishayParseTreeStringPretty.jackson                      thrpt    5   712.814 ±  13.858  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                       thrpt    5   689.817 ±  18.602  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                       thrpt    5  1560.220 ±  32.306  ops/ms
EishayParseTreeUTF8Bytes.gson                            thrpt    5   570.485 ±   8.201  ops/ms
EishayParseTreeUTF8Bytes.jackson                         thrpt    5   872.319 ±  14.805  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1                 thrpt    5   571.616 ±  15.566  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2                 thrpt    5  1243.346 ±  15.678  ops/ms
EishayParseTreeUTF8BytesPretty.gson                      thrpt    5   561.292 ±  17.071  ops/ms
EishayParseTreeUTF8BytesPretty.jackson                   thrpt    5   903.825 ±  25.146  ops/ms
EishayParseUTF8Bytes.dsljson                             thrpt    5   684.458 ± 156.128  ops/ms
EishayParseUTF8Bytes.fastjson1                           thrpt    5  1690.625 ±  28.245  ops/ms
EishayParseUTF8Bytes.fastjson2                           thrpt    5  1725.803 ±  72.440  ops/ms
EishayParseUTF8Bytes.gson                                thrpt    5   560.202 ±   7.702  ops/ms
EishayParseUTF8Bytes.jackson                             thrpt    5   772.636 ±  26.900  ops/ms
EishayParseUTF8BytesPretty.fastjson1                     thrpt    5   422.230 ±  23.256  ops/ms
EishayParseUTF8BytesPretty.fastjson2                     thrpt    5  1559.216 ±  23.002  ops/ms
EishayParseUTF8BytesPretty.gson                          thrpt    5   567.657 ±  13.951  ops/ms
EishayParseUTF8BytesPretty.jackson                       thrpt    5   766.196 ±  18.522  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes                     thrpt    5  3120.611 ±  29.835  ops/ms
EishayWriteBinary.hessian                                thrpt    5   735.263 ± 108.348  ops/ms
EishayWriteBinary.javaSerialize                          thrpt    5   463.593 ±   8.056  ops/ms
EishayWriteBinary.jsonb                                  thrpt    5  5086.965 ±  55.561  ops/ms
EishayWriteBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5  1149.113 ±  15.477  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  3423.743 ±  66.623  ops/ms
EishayWriteBinaryArrayMapping.jsonb                      thrpt    5  9770.343 ±  99.151  ops/ms
EishayWriteBinaryArrayMapping.kryo                       thrpt    5  2969.237 ±  32.744  ops/ms
EishayWriteBinaryArrayMapping.protobuf                   thrpt    5  2490.864 ±  42.912  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB                 thrpt    5  2944.208 ±  34.621  ops/ms
EishayWriteBinaryAutoType.fastjson2UTF8Bytes             thrpt    5  3062.764 ±  35.779  ops/ms
EishayWriteBinaryAutoType.hessian                        thrpt    5   690.683 ±  28.038  ops/ms
EishayWriteBinaryAutoType.javaSerialize                  thrpt    5   445.123 ±   5.051  ops/ms
EishayWriteString.fastjson1                              thrpt    5  1165.985 ±  19.080  ops/ms
EishayWriteString.fastjson2                              thrpt    5  2903.157 ±  38.499  ops/ms
EishayWriteString.gson                                   thrpt    5   563.059 ±   5.954  ops/ms
EishayWriteString.jackson                                thrpt    5  1701.044 ±  22.535  ops/ms
EishayWriteStringTree.fastjson1                          thrpt    5  1270.139 ±  15.150  ops/ms
EishayWriteStringTree.fastjson2                          thrpt    5  1922.846 ±  16.666  ops/ms
EishayWriteStringTree.gson                               thrpt    5   567.825 ±   7.485  ops/ms
EishayWriteStringTree.jackson                            thrpt    5  1428.381 ±  21.345  ops/ms
EishayWriteStringTree1x.fastjson1                        thrpt    5  1132.020 ±  14.763  ops/ms
EishayWriteStringTree1x.fastjson2                        thrpt    5  1478.478 ±  14.701  ops/ms
EishayWriteStringTree1x.gson                             thrpt    5   559.250 ±   6.449  ops/ms
EishayWriteStringTree1x.jackson                          thrpt    5  1319.919 ±  14.638  ops/ms
EishayWriteUTF8Bytes.fastjson1                           thrpt    5  1053.009 ±  12.031  ops/ms
EishayWriteUTF8Bytes.fastjson2                           thrpt    5  3119.823 ±  39.373  ops/ms
EishayWriteUTF8Bytes.gson                                thrpt    5   558.694 ±   9.514  ops/ms
EishayWriteUTF8Bytes.jackson                             thrpt    5  1532.711 ±  12.356  ops/ms
```
# AppleM1Pro-graalvm-ce-java11-22.3.1
```java
Benchmark                                                 Mode  Cnt     Score      Error   Units
EishayParseBinary.fastjson2UTF8Bytes                     thrpt    5   910.723 ±   52.962  ops/ms
EishayParseBinary.hessian                                thrpt    5   272.879 ±    6.274  ops/ms
EishayParseBinary.javaSerialize                          thrpt    5    55.156 ±    0.696  ops/ms
EishayParseBinary.jsonb                                  thrpt    5  2186.930 ±   18.231  ops/ms
EishayParseBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5  1610.265 ±   37.429  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  1507.778 ± 1154.570  ops/ms
EishayParseBinaryArrayMapping.jsonb                      thrpt    5  5145.749 ±   37.618  ops/ms
EishayParseBinaryArrayMapping.kryo                       thrpt    5  1705.162 ±   22.986  ops/ms
EishayParseBinaryArrayMapping.protobuf                   thrpt    5  1896.229 ±   17.103  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB                 thrpt    5  3282.771 ±   21.596  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB_autoTypeFilter  thrpt    5  2295.789 ±   28.815  ops/ms
EishayParseBinaryAutoType.hessian                        thrpt    5   277.959 ±    6.872  ops/ms
EishayParseBinaryAutoType.javaSerialize                  thrpt    5    57.119 ±    0.842  ops/ms
EishayParseString.fastjson1                              thrpt    5  1174.487 ±   24.467  ops/ms
EishayParseString.fastjson2                              thrpt    5  1278.928 ±   63.290  ops/ms
EishayParseString.gson                                   thrpt    5   767.595 ±    6.928  ops/ms
EishayParseString.jackson                                thrpt    5   574.269 ±   29.661  ops/ms
EishayParseStringPretty.fastjson1                        thrpt    5   329.727 ±   21.935  ops/ms
EishayParseStringPretty.fastjson2                        thrpt    5  1069.261 ±  313.537  ops/ms
EishayParseStringPretty.gson                             thrpt    5   654.819 ±    5.222  ops/ms
EishayParseStringPretty.jackson                          thrpt    5   500.257 ±   14.284  ops/ms
EishayParseTreeString.fastjson1                          thrpt    5   704.437 ±   13.540  ops/ms
EishayParseTreeString.fastjson2                          thrpt    5  1329.152 ±   13.171  ops/ms
EishayParseTreeString.gson                               thrpt    5   627.962 ±    5.996  ops/ms
EishayParseTreeString.jackson                            thrpt    5   769.372 ±    5.824  ops/ms
EishayParseTreeStringPretty.fastjson1                    thrpt    5   517.335 ±   19.933  ops/ms
EishayParseTreeStringPretty.fastjson2                    thrpt    5  1106.673 ±   16.410  ops/ms
EishayParseTreeStringPretty.gson                         thrpt    5   576.132 ±    2.868  ops/ms
EishayParseTreeStringPretty.jackson                      thrpt    5   705.354 ±    7.523  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                       thrpt    5   557.952 ±    6.792  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                       thrpt    5   961.201 ±   12.785  ops/ms
EishayParseTreeUTF8Bytes.gson                            thrpt    5   555.862 ±   16.306  ops/ms
EishayParseTreeUTF8Bytes.jackson                         thrpt    5   898.183 ±    3.928  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1                 thrpt    5   437.773 ±   18.895  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2                 thrpt    5   759.149 ±   16.127  ops/ms
EishayParseTreeUTF8BytesPretty.gson                      thrpt    5   522.827 ±    1.052  ops/ms
EishayParseTreeUTF8BytesPretty.jackson                   thrpt    5   749.356 ±   11.790  ops/ms
EishayParseUTF8Bytes.dsljson                             thrpt    5   678.463 ±   78.015  ops/ms
EishayParseUTF8Bytes.fastjson1                           thrpt    5   840.785 ±   55.453  ops/ms
EishayParseUTF8Bytes.fastjson2                           thrpt    5   900.795 ±   47.318  ops/ms
EishayParseUTF8Bytes.gson                                thrpt    5   531.893 ±    3.502  ops/ms
EishayParseUTF8Bytes.jackson                             thrpt    5   712.219 ±   15.969  ops/ms
EishayParseUTF8BytesPretty.fastjson1                     thrpt    5   319.246 ±   11.789  ops/ms
EishayParseUTF8BytesPretty.fastjson2                     thrpt    5   701.182 ±  128.427  ops/ms
EishayParseUTF8BytesPretty.gson                          thrpt    5   523.249 ±    0.780  ops/ms
EishayParseUTF8BytesPretty.jackson                       thrpt    5   684.454 ±   28.076  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes                     thrpt    5  2987.633 ±    7.356  ops/ms
EishayWriteBinary.hessian                                thrpt    5   427.200 ±    4.667  ops/ms
EishayWriteBinary.javaSerialize                          thrpt    5   380.768 ±    3.619  ops/ms
EishayWriteBinary.jsonb                                  thrpt    5  3817.420 ±   30.860  ops/ms
EishayWriteBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5  1113.323 ±   13.520  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5  2848.329 ±   27.158  ops/ms
EishayWriteBinaryArrayMapping.jsonb                      thrpt    5  5512.089 ±   54.030  ops/ms
EishayWriteBinaryArrayMapping.kryo                       thrpt    5  2908.382 ±   23.664  ops/ms
EishayWriteBinaryArrayMapping.protobuf                   thrpt    5  1761.535 ±   58.556  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB                 thrpt    5  2408.305 ±   20.467  ops/ms
EishayWriteBinaryAutoType.fastjson2UTF8Bytes             thrpt    5  2933.396 ±   21.457  ops/ms
EishayWriteBinaryAutoType.hessian                        thrpt    5   424.785 ±    3.000  ops/ms
EishayWriteBinaryAutoType.javaSerialize                  thrpt    5   379.246 ±    3.765  ops/ms
EishayWriteString.fastjson1                              thrpt    5  1027.243 ±    6.211  ops/ms
EishayWriteString.fastjson2                              thrpt    5  2850.243 ±   21.835  ops/ms
EishayWriteString.gson                                   thrpt    5   618.574 ±    5.438  ops/ms
EishayWriteString.jackson                                thrpt    5  1542.750 ±   13.188  ops/ms
EishayWriteStringTree.fastjson1                          thrpt    5   415.942 ±    4.109  ops/ms
EishayWriteStringTree.fastjson2                          thrpt    5  2128.637 ±   15.833  ops/ms
EishayWriteStringTree.gson                               thrpt    5   600.107 ±    5.149  ops/ms
EishayWriteStringTree.jackson                            thrpt    5  1421.899 ±   14.705  ops/ms
EishayWriteStringTree1x.fastjson1                        thrpt    5   374.561 ±    3.002  ops/ms
EishayWriteStringTree1x.fastjson2                        thrpt    5  1534.442 ±   11.601  ops/ms
EishayWriteStringTree1x.gson                             thrpt    5   570.884 ±    3.547  ops/ms
EishayWriteStringTree1x.jackson                          thrpt    5  1262.039 ±   11.404  ops/ms
EishayWriteUTF8Bytes.fastjson1                           thrpt    5   794.869 ±   10.968  ops/ms
EishayWriteUTF8Bytes.fastjson2                           thrpt    5  3013.692 ±   20.861  ops/ms
EishayWriteUTF8Bytes.gson                                thrpt    5   519.760 ±    5.580  ops/ms
EishayWriteUTF8Bytes.jackson                             thrpt    5  1408.752 ±   12.564  ops/ms
```
# AppleM1Pro-graalvm-ce-java17-22.3.1
```java
Benchmark                                                 Mode  Cnt      Score     Error   Units
EishayParseBinary.fastjson2UTF8Bytes                     thrpt    5    916.915 ±  91.735  ops/ms
EishayParseBinary.hessian                                thrpt    5    283.296 ±  16.908  ops/ms
EishayParseBinary.javaSerialize                          thrpt    5     61.074 ±   2.974  ops/ms
EishayParseBinary.jsonb                                  thrpt    5   3210.298 ± 106.180  ops/ms
EishayParseBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   1915.619 ±  11.053  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5   1470.654 ± 120.858  ops/ms
EishayParseBinaryArrayMapping.jsonb                      thrpt    5   8761.373 ±  16.089  ops/ms
EishayParseBinaryArrayMapping.kryo                       thrpt    5   1634.465 ±  62.463  ops/ms
EishayParseBinaryArrayMapping.protobuf                   thrpt    5   2610.067 ±  17.768  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB                 thrpt    5   3157.392 ±   5.511  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB_autoTypeFilter  thrpt    5   2938.320 ±   8.770  ops/ms
EishayParseBinaryAutoType.hessian                        thrpt    5    277.677 ±   8.875  ops/ms
EishayParseBinaryAutoType.javaSerialize                  thrpt    5     63.104 ±   1.905  ops/ms
EishayParseString.fastjson1                              thrpt    5   1318.848 ±  63.003  ops/ms
EishayParseString.fastjson2                              thrpt    5   1394.572 ±  83.966  ops/ms
EishayParseString.gson                                   thrpt    5    666.812 ±  62.437  ops/ms
EishayParseString.jackson                                thrpt    5    578.733 ±  39.926  ops/ms
EishayParseStringPretty.fastjson1                        thrpt    5    368.599 ±  18.483  ops/ms
EishayParseStringPretty.fastjson2                        thrpt    5   1220.871 ±  39.873  ops/ms
EishayParseStringPretty.gson                             thrpt    5    646.939 ±   8.641  ops/ms
EishayParseStringPretty.jackson                          thrpt    5    554.677 ±  25.172  ops/ms
EishayParseTreeString.fastjson1                          thrpt    5    845.879 ±  38.167  ops/ms
EishayParseTreeString.fastjson2                          thrpt    5   1306.475 ±  24.444  ops/ms
EishayParseTreeString.gson                               thrpt    5    604.223 ±  18.664  ops/ms
EishayParseTreeString.jackson                            thrpt    5    817.492 ±   5.992  ops/ms
EishayParseTreeStringPretty.fastjson1                    thrpt    5    682.439 ±  23.214  ops/ms
EishayParseTreeStringPretty.fastjson2                    thrpt    5   1112.926 ±  23.554  ops/ms
EishayParseTreeStringPretty.gson                         thrpt    5    564.931 ±  19.938  ops/ms
EishayParseTreeStringPretty.jackson                      thrpt    5    609.158 ±  20.540  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                       thrpt    5    673.417 ±  37.840  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                       thrpt    5    965.285 ±   5.080  ops/ms
EishayParseTreeUTF8Bytes.gson                            thrpt    5    546.875 ±   9.334  ops/ms
EishayParseTreeUTF8Bytes.jackson                         thrpt    5    762.272 ±  22.083  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1                 thrpt    5    498.591 ±  20.787  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2                 thrpt    5    778.394 ±  13.169  ops/ms
EishayParseTreeUTF8BytesPretty.gson                      thrpt    5    529.974 ±   5.371  ops/ms
EishayParseTreeUTF8BytesPretty.jackson                   thrpt    5    715.037 ±  20.186  ops/ms
EishayParseUTF8Bytes.dsljson                             thrpt    5    470.923 ±  41.902  ops/ms
EishayParseUTF8Bytes.fastjson1                           thrpt    5   1064.558 ±  17.811  ops/ms
EishayParseUTF8Bytes.fastjson2                           thrpt    5    822.471 ± 153.735  ops/ms
EishayParseUTF8Bytes.gson                                thrpt    5    543.342 ±  12.265  ops/ms
EishayParseUTF8Bytes.jackson                             thrpt    5    739.915 ±  23.979  ops/ms
EishayParseUTF8BytesPretty.fastjson1                     thrpt    5    386.740 ±  11.740  ops/ms
EishayParseUTF8BytesPretty.fastjson2                     thrpt    5    763.875 ±  56.575  ops/ms
EishayParseUTF8BytesPretty.gson                          thrpt    5    531.760 ±   7.831  ops/ms
EishayParseUTF8BytesPretty.jackson                       thrpt    5    669.173 ±  40.970  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes                     thrpt    5   3080.560 ±  33.150  ops/ms
EishayWriteBinary.hessian                                thrpt    5    705.202 ±  21.520  ops/ms
EishayWriteBinary.javaSerialize                          thrpt    5    384.595 ±   6.800  ops/ms
EishayWriteBinary.jsonb                                  thrpt    5   5754.830 ±  59.933  ops/ms
EishayWriteBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5    870.008 ±  11.485  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5   3770.462 ±  44.588  ops/ms
EishayWriteBinaryArrayMapping.jsonb                      thrpt    5  10148.573 ±  95.139  ops/ms
EishayWriteBinaryArrayMapping.kryo                       thrpt    5   2987.298 ±  33.100  ops/ms
EishayWriteBinaryArrayMapping.protobuf                   thrpt    5   2874.798 ±  34.381  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB                 thrpt    5   3046.257 ±  31.496  ops/ms
EishayWriteBinaryAutoType.fastjson2UTF8Bytes             thrpt    5   2990.200 ±  35.140  ops/ms
EishayWriteBinaryAutoType.hessian                        thrpt    5    691.908 ±  17.303  ops/ms
EishayWriteBinaryAutoType.javaSerialize                  thrpt    5    384.065 ±   4.051  ops/ms
EishayWriteString.fastjson1                              thrpt    5   1107.522 ±   9.614  ops/ms
EishayWriteString.fastjson2                              thrpt    5   2940.466 ±  35.447  ops/ms
EishayWriteString.gson                                   thrpt    5    425.238 ±   5.029  ops/ms
EishayWriteString.jackson                                thrpt    5   1582.944 ±  15.258  ops/ms
EishayWriteStringTree.fastjson1                          thrpt    5    515.559 ±   7.170  ops/ms
EishayWriteStringTree.fastjson2                          thrpt    5   2174.637 ±  25.258  ops/ms
EishayWriteStringTree.gson                               thrpt    5    434.806 ±   4.643  ops/ms
EishayWriteStringTree.jackson                            thrpt    5   1436.395 ±  17.959  ops/ms
EishayWriteStringTree1x.fastjson1                        thrpt    5    462.419 ±   6.160  ops/ms
EishayWriteStringTree1x.fastjson2                        thrpt    5   1774.334 ±  31.603  ops/ms
EishayWriteStringTree1x.gson                             thrpt    5    428.675 ±   5.633  ops/ms
EishayWriteStringTree1x.jackson                          thrpt    5   1272.927 ±  13.654  ops/ms
EishayWriteUTF8Bytes.fastjson1                           thrpt    5    832.224 ±  11.979  ops/ms
EishayWriteUTF8Bytes.fastjson2                           thrpt    5   3099.143 ±  39.774  ops/ms
EishayWriteUTF8Bytes.gson                                thrpt    5    383.137 ±   5.427  ops/ms
EishayWriteUTF8Bytes.jackson                             thrpt    5   1633.592 ±  18.144  ops/ms
```
# AppleM1Pro-graalvm-ee-java11-22.3.1
```java
Benchmark                                                 Mode  Cnt      Score     Error   Units
EishayParseBinary.fastjson2UTF8Bytes                     thrpt    5   1309.056 ±  36.955  ops/ms
EishayParseBinary.hessian                                thrpt    5    500.247 ±   4.937  ops/ms
EishayParseBinary.javaSerialize                          thrpt    5     62.018 ±   0.606  ops/ms
EishayParseBinary.jsonb                                  thrpt    5   5779.764 ±  23.764  ops/ms
EishayParseBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   2268.784 ±  21.008  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5   2927.639 ±  63.273  ops/ms
EishayParseBinaryArrayMapping.jsonb                      thrpt    5   9697.524 ± 325.038  ops/ms
EishayParseBinaryArrayMapping.kryo                       thrpt    5   2285.601 ±  87.106  ops/ms
EishayParseBinaryArrayMapping.protobuf                   thrpt    5   4039.139 ±  34.841  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB                 thrpt    5   3316.959 ±  37.127  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB_autoTypeFilter  thrpt    5   3204.476 ±  19.341  ops/ms
EishayParseBinaryAutoType.hessian                        thrpt    5    515.237 ±   5.100  ops/ms
EishayParseBinaryAutoType.javaSerialize                  thrpt    5     62.465 ±   0.662  ops/ms
EishayParseString.fastjson1                              thrpt    5   1861.872 ±  37.136  ops/ms
EishayParseString.fastjson2                              thrpt    5   1337.910 ± 194.473  ops/ms
EishayParseString.gson                                   thrpt    5    718.436 ±   4.736  ops/ms
EishayParseString.jackson                                thrpt    5    731.325 ±  14.199  ops/ms
EishayParseStringPretty.fastjson1                        thrpt    5    406.580 ±  44.152  ops/ms
EishayParseStringPretty.fastjson2                        thrpt    5   1130.159 ±  45.879  ops/ms
EishayParseStringPretty.gson                             thrpt    5    641.670 ±  11.097  ops/ms
EishayParseStringPretty.jackson                          thrpt    5    642.377 ±  38.010  ops/ms
EishayParseTreeString.fastjson1                          thrpt    5   1033.659 ±   8.970  ops/ms
EishayParseTreeString.fastjson2                          thrpt    5   1605.859 ±   8.849  ops/ms
EishayParseTreeString.gson                               thrpt    5    625.542 ±   0.797  ops/ms
EishayParseTreeString.jackson                            thrpt    5    876.563 ±   1.770  ops/ms
EishayParseTreeStringPretty.fastjson1                    thrpt    5    861.023 ±   7.423  ops/ms
EishayParseTreeStringPretty.fastjson2                    thrpt    5   1363.874 ±   5.335  ops/ms
EishayParseTreeStringPretty.gson                         thrpt    5    620.989 ±   1.093  ops/ms
EishayParseTreeStringPretty.jackson                      thrpt    5    827.758 ±   3.187  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                       thrpt    5    763.422 ±   5.383  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                       thrpt    5   1492.028 ±   7.039  ops/ms
EishayParseTreeUTF8Bytes.gson                            thrpt    5    642.398 ±   0.451  ops/ms
EishayParseTreeUTF8Bytes.jackson                         thrpt    5    996.736 ±   3.894  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1                 thrpt    5    580.094 ±  13.272  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2                 thrpt    5   1255.587 ±   4.282  ops/ms
EishayParseTreeUTF8BytesPretty.gson                      thrpt    5    667.568 ±   2.156  ops/ms
EishayParseTreeUTF8BytesPretty.jackson                   thrpt    5    874.610 ±  48.483  ops/ms
EishayParseUTF8Bytes.dsljson                             thrpt    5    767.458 ±  36.654  ops/ms
EishayParseUTF8Bytes.fastjson1                           thrpt    5   1145.801 ±   8.532  ops/ms
EishayParseUTF8Bytes.fastjson2                           thrpt    5   1216.992 ±  81.920  ops/ms
EishayParseUTF8Bytes.gson                                thrpt    5    601.589 ±   9.155  ops/ms
EishayParseUTF8Bytes.jackson                             thrpt    5    812.695 ±  15.456  ops/ms
EishayParseUTF8BytesPretty.fastjson1                     thrpt    5    394.565 ±  11.621  ops/ms
EishayParseUTF8BytesPretty.fastjson2                     thrpt    5    974.561 ± 124.469  ops/ms
EishayParseUTF8BytesPretty.gson                          thrpt    5    664.538 ±   6.190  ops/ms
EishayParseUTF8BytesPretty.jackson                       thrpt    5    736.042 ±  17.357  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes                     thrpt    5   3524.377 ±  30.631  ops/ms
EishayWriteBinary.hessian                                thrpt    5    828.129 ±   6.024  ops/ms
EishayWriteBinary.javaSerialize                          thrpt    5    490.888 ±   4.956  ops/ms
EishayWriteBinary.jsonb                                  thrpt    5   6435.284 ±  55.854  ops/ms
EishayWriteBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5    863.756 ±  11.193  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5   3834.845 ±  29.488  ops/ms
EishayWriteBinaryArrayMapping.jsonb                      thrpt    5  14978.757 ± 108.948  ops/ms
EishayWriteBinaryArrayMapping.kryo                       thrpt    5   3544.756 ±  35.026  ops/ms
EishayWriteBinaryArrayMapping.protobuf                   thrpt    5   4898.449 ±  31.831  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB                 thrpt    5   3553.814 ±  36.915  ops/ms
EishayWriteBinaryAutoType.fastjson2UTF8Bytes             thrpt    5   3491.858 ±  27.901  ops/ms
EishayWriteBinaryAutoType.hessian                        thrpt    5    829.438 ±   4.775  ops/ms
EishayWriteBinaryAutoType.javaSerialize                  thrpt    5    484.086 ±   3.026  ops/ms
EishayWriteString.fastjson1                              thrpt    5   1442.432 ±  11.731  ops/ms
EishayWriteString.fastjson2                              thrpt    5   3378.575 ±  29.050  ops/ms
EishayWriteString.gson                                   thrpt    5    720.659 ±  21.549  ops/ms
EishayWriteString.jackson                                thrpt    5   1818.591 ±  16.170  ops/ms
EishayWriteStringTree.fastjson1                          thrpt    5   1011.721 ±   9.791  ops/ms
EishayWriteStringTree.fastjson2                          thrpt    5   2308.892 ±  21.347  ops/ms
EishayWriteStringTree.gson                               thrpt    5    804.692 ±   6.059  ops/ms
EishayWriteStringTree.jackson                            thrpt    5   1553.488 ±  13.862  ops/ms
EishayWriteStringTree1x.fastjson1                        thrpt    5   1381.892 ±  11.632  ops/ms
EishayWriteStringTree1x.fastjson2                        thrpt    5   2027.965 ±   8.103  ops/ms
EishayWriteStringTree1x.gson                             thrpt    5    754.287 ±   2.174  ops/ms
EishayWriteStringTree1x.jackson                          thrpt    5   1472.396 ±  10.368  ops/ms
EishayWriteUTF8Bytes.fastjson1                           thrpt    5   1003.143 ±   3.053  ops/ms
EishayWriteUTF8Bytes.fastjson2                           thrpt    5   3548.491 ±   7.423  ops/ms
EishayWriteUTF8Bytes.gson                                thrpt    5    651.919 ±   1.392  ops/ms
EishayWriteUTF8Bytes.jackson                             thrpt    5   1861.546 ±   1.669  ops/ms
```
# AppleM1Pro-graalvm-ee-java17-22.3.1
```java
Benchmark                                                 Mode  Cnt      Score     Error   Units
EishayParseBinary.fastjson2UTF8Bytes                     thrpt    5   1591.112 ±  19.869  ops/ms
EishayParseBinary.hessian                                thrpt    5    553.503 ±   7.483  ops/ms
EishayParseBinary.javaSerialize                          thrpt    5     68.006 ±   1.615  ops/ms
EishayParseBinary.jsonb                                  thrpt    5   5703.977 ±  20.106  ops/ms
EishayParseBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   2061.302 ±  23.587  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5   3606.468 ±  14.853  ops/ms
EishayParseBinaryArrayMapping.jsonb                      thrpt    5  11140.286 ±  21.955  ops/ms
EishayParseBinaryArrayMapping.kryo                       thrpt    5   2225.906 ±  35.600  ops/ms
EishayParseBinaryArrayMapping.protobuf                   thrpt    5   4470.418 ±  16.063  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB                 thrpt    5   3330.798 ±  15.167  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB_autoTypeFilter  thrpt    5   3331.443 ±  32.968  ops/ms
EishayParseBinaryAutoType.hessian                        thrpt    5    671.593 ±   8.168  ops/ms
EishayParseBinaryAutoType.javaSerialize                  thrpt    5     69.338 ±   2.485  ops/ms
EishayParseString.fastjson1                              thrpt    5   1797.118 ±  27.896  ops/ms
EishayParseString.fastjson2                              thrpt    5   1733.339 ± 182.974  ops/ms
EishayParseString.gson                                   thrpt    5    726.256 ±  24.980  ops/ms
EishayParseString.jackson                                thrpt    5    757.474 ±  42.058  ops/ms
EishayParseStringPretty.fastjson1                        thrpt    5    479.625 ±  37.301  ops/ms
EishayParseStringPretty.fastjson2                        thrpt    5   1429.477 ±  63.792  ops/ms
EishayParseStringPretty.gson                             thrpt    5    773.832 ±  24.691  ops/ms
EishayParseStringPretty.jackson                          thrpt    5    675.860 ±  11.596  ops/ms
EishayParseTreeString.fastjson1                          thrpt    5   1152.589 ±   9.098  ops/ms
EishayParseTreeString.fastjson2                          thrpt    5   1615.447 ±  19.121  ops/ms
EishayParseTreeString.gson                               thrpt    5    621.600 ±  32.956  ops/ms
EishayParseTreeString.jackson                            thrpt    5    893.775 ±  17.029  ops/ms
EishayParseTreeStringPretty.fastjson1                    thrpt    5    962.722 ±   5.259  ops/ms
EishayParseTreeStringPretty.fastjson2                    thrpt    5   1368.102 ±  11.537  ops/ms
EishayParseTreeStringPretty.gson                         thrpt    5    598.299 ±   8.709  ops/ms
EishayParseTreeStringPretty.jackson                      thrpt    5    812.535 ±  10.674  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                       thrpt    5    831.967 ±   8.360  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                       thrpt    5   1503.070 ±  25.073  ops/ms
EishayParseTreeUTF8Bytes.gson                            thrpt    5    611.212 ±  11.802  ops/ms
EishayParseTreeUTF8Bytes.jackson                         thrpt    5    995.518 ±   8.396  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1                 thrpt    5    668.503 ±   5.995  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2                 thrpt    5   1252.118 ±  20.124  ops/ms
EishayParseTreeUTF8BytesPretty.gson                      thrpt    5    650.840 ±   9.260  ops/ms
EishayParseTreeUTF8BytesPretty.jackson                   thrpt    5    911.828 ±   8.118  ops/ms
EishayParseUTF8Bytes.dsljson                             thrpt    5    691.136 ± 169.023  ops/ms
EishayParseUTF8Bytes.fastjson1                           thrpt    5   1446.879 ±  15.696  ops/ms
EishayParseUTF8Bytes.fastjson2                           thrpt    5   1275.869 ± 233.933  ops/ms
EishayParseUTF8Bytes.gson                                thrpt    5    587.987 ±  20.738  ops/ms
EishayParseUTF8Bytes.jackson                             thrpt    5    781.229 ±  51.514  ops/ms
EishayParseUTF8BytesPretty.fastjson1                     thrpt    5    444.959 ±  21.807  ops/ms
EishayParseUTF8BytesPretty.fastjson2                     thrpt    5   1215.698 ±  33.599  ops/ms
EishayParseUTF8BytesPretty.gson                          thrpt    5    650.722 ±   5.834  ops/ms
EishayParseUTF8BytesPretty.jackson                       thrpt    5    683.774 ±  32.476  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes                     thrpt    5   3510.315 ±   8.629  ops/ms
EishayWriteBinary.hessian                                thrpt    5    843.927 ±  12.106  ops/ms
EishayWriteBinary.javaSerialize                          thrpt    5    484.724 ±   0.679  ops/ms
EishayWriteBinary.jsonb                                  thrpt    5   7172.400 ±  16.581  ops/ms
EishayWriteBinaryArrayMapping.fastjson1UTF8Bytes         thrpt    5   1429.540 ±   8.720  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes         thrpt    5   3822.690 ±  56.310  ops/ms
EishayWriteBinaryArrayMapping.jsonb                      thrpt    5  15030.589 ± 125.121  ops/ms
EishayWriteBinaryArrayMapping.kryo                       thrpt    5   3803.909 ±  76.345  ops/ms
EishayWriteBinaryArrayMapping.protobuf                   thrpt    5   4920.094 ±  45.038  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB                 thrpt    5   3576.012 ±  21.637  ops/ms
EishayWriteBinaryAutoType.fastjson2UTF8Bytes             thrpt    5   3498.380 ±  35.868  ops/ms
EishayWriteBinaryAutoType.hessian                        thrpt    5    836.026 ±   3.724  ops/ms
EishayWriteBinaryAutoType.javaSerialize                  thrpt    5    477.541 ±   4.328  ops/ms
EishayWriteString.fastjson1                              thrpt    5   1402.821 ±  12.203  ops/ms
EishayWriteString.fastjson2                              thrpt    5   3377.665 ±  27.217  ops/ms
EishayWriteString.gson                                   thrpt    5    436.091 ±   4.172  ops/ms
EishayWriteString.jackson                                thrpt    5   1842.140 ±  22.932  ops/ms
EishayWriteStringTree.fastjson1                          thrpt    5   1100.832 ±   5.429  ops/ms
EishayWriteStringTree.fastjson2                          thrpt    5   2296.148 ±  22.355  ops/ms
EishayWriteStringTree.gson                               thrpt    5    442.203 ±   4.298  ops/ms
EishayWriteStringTree.jackson                            thrpt    5   1575.639 ±  15.657  ops/ms
EishayWriteStringTree1x.fastjson1                        thrpt    5   1371.999 ±  12.604  ops/ms
EishayWriteStringTree1x.fastjson2                        thrpt    5   2048.344 ±  15.729  ops/ms
EishayWriteStringTree1x.gson                             thrpt    5    439.843 ±   4.450  ops/ms
EishayWriteStringTree1x.jackson                          thrpt    5   1479.341 ±  19.939  ops/ms
EishayWriteUTF8Bytes.fastjson1                           thrpt    5   1249.904 ±  36.623  ops/ms
EishayWriteUTF8Bytes.fastjson2                           thrpt    5   3547.541 ±  48.335  ops/ms
EishayWriteUTF8Bytes.gson                                thrpt    5    424.858 ±   7.559  ops/ms
EishayWriteUTF8Bytes.jackson                             thrpt    5   1955.230 ±  34.034  ops/ms
```

