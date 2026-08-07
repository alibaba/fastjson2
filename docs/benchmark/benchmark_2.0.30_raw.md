# ecs.g8i.xlarge-jdk1.8.0_371
```java
Benchmark                                          Mode  Cnt     Score    Error   Units
EishayParseBinary.fastjson2UTF8Bytes              thrpt    5  1489.382 ?  6.424  ops/ms
EishayParseBinary.hessian                         thrpt    5   355.608 ?  1.156  ops/ms
EishayParseBinary.javaSerialize                   thrpt    5    52.253 ?  0.115  ops/ms
EishayParseBinary.jsonb                           thrpt    5  2675.408 ?  8.892  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes  thrpt    5  2272.602 ?  6.191  ops/ms
EishayParseBinaryArrayMapping.jsonb               thrpt    5  4442.654 ? 11.268  ops/ms
EishayParseBinaryArrayMapping.kryo                thrpt    5  2281.041 ?  4.934  ops/ms
EishayParseBinaryArrayMapping.protobuf            thrpt    5  1524.777 ?  7.964  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB          thrpt    5  2073.660 ?  9.200  ops/ms
EishayParseBinaryAutoType.hessian                 thrpt    5   378.136 ?  1.863  ops/ms
EishayParseBinaryAutoType.javaSerialize           thrpt    5    54.302 ?  0.109  ops/ms
EishayParseString.fastjson1                       thrpt    5  1318.558 ?  1.992  ops/ms
EishayParseString.fastjson2                       thrpt    5  1621.642 ?  8.962  ops/ms
EishayParseString.gson                            thrpt    5   511.125 ?  4.265  ops/ms
EishayParseString.jackson                         thrpt    5   658.749 ?  0.834  ops/ms
EishayParseStringPretty.fastjson1                 thrpt    5   317.257 ?  0.334  ops/ms
EishayParseStringPretty.fastjson2                 thrpt    5  1265.629 ?  2.959  ops/ms
EishayParseStringPretty.gson                      thrpt    5   495.042 ?  4.924  ops/ms
EishayParseStringPretty.jackson                   thrpt    5   584.118 ?  0.983  ops/ms
EishayParseTreeString.fastjson1                   thrpt    5   657.264 ?  1.273  ops/ms
EishayParseTreeString.fastjson2                   thrpt    5  1202.042 ?  3.765  ops/ms
EishayParseTreeString.gson                        thrpt    5   390.091 ?  1.875  ops/ms
EishayParseTreeString.jackson                     thrpt    5   732.443 ?  5.781  ops/ms
EishayParseTreeStringPretty.fastjson1             thrpt    5   571.402 ?  0.964  ops/ms
EishayParseTreeStringPretty.fastjson2             thrpt    5   997.589 ?  4.907  ops/ms
EishayParseTreeStringPretty.gson                  thrpt    5   371.117 ?  1.585  ops/ms
EishayParseTreeStringPretty.jackson               thrpt    5   637.905 ?  1.337  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                thrpt    5   571.486 ?  2.203  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                thrpt    5  1130.453 ?  4.036  ops/ms
EishayParseTreeUTF8Bytes.gson                     thrpt    5   357.579 ?  1.322  ops/ms
EishayParseTreeUTF8Bytes.jackson                  thrpt    5   775.959 ?  2.016  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1          thrpt    5   479.942 ?  1.073  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2          thrpt    5   972.045 ?  2.059  ops/ms
EishayParseTreeUTF8BytesPretty.gson               thrpt    5   331.735 ?  1.274  ops/ms
EishayParseTreeUTF8BytesPretty.jackson            thrpt    5   715.253 ?  0.696  ops/ms
EishayParseUTF8Bytes.fastjson1                    thrpt    5  1042.980 ?  2.781  ops/ms
EishayParseUTF8Bytes.fastjson2                    thrpt    5  1448.529 ?  1.880  ops/ms
EishayParseUTF8Bytes.gson                         thrpt    5   358.017 ?  2.519  ops/ms
EishayParseUTF8Bytes.jackson                      thrpt    5   723.276 ?  0.484  ops/ms
EishayParseUTF8BytesPretty.fastjson1              thrpt    5   289.303 ?  1.216  ops/ms
EishayParseUTF8BytesPretty.fastjson2              thrpt    5  1189.501 ?  3.746  ops/ms
EishayParseUTF8BytesPretty.gson                   thrpt    5   338.714 ?  1.787  ops/ms
EishayParseUTF8BytesPretty.jackson                thrpt    5   660.519 ?  1.067  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes              thrpt    5  2004.762 ?  6.634  ops/ms
EishayWriteBinary.hessian                         thrpt    5   361.474 ?  3.148  ops/ms
EishayWriteBinary.javaSerialize                   thrpt    5   275.439 ?  3.083  ops/ms
EishayWriteBinary.jsonb                           thrpt    5  2287.932 ? 11.209  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes  thrpt    5  2388.109 ?  1.448  ops/ms
EishayWriteBinaryArrayMapping.jsonb               thrpt    5  3976.008 ? 15.269  ops/ms
EishayWriteBinaryArrayMapping.kryo                thrpt    5  2348.823 ?  4.459  ops/ms
EishayWriteBinaryArrayMapping.protobuf            thrpt    5  1714.636 ?  1.417  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB          thrpt    5  1708.784 ? 31.699  ops/ms
EishayWriteBinaryAutoType.hessian                 thrpt    5   357.469 ?  4.489  ops/ms
EishayWriteBinaryAutoType.javaSerialize           thrpt    5   288.086 ?  5.851  ops/ms
EishayWriteString.fastjson1                       thrpt    5   762.657 ?  9.743  ops/ms
EishayWriteString.fastjson2                       thrpt    5  2081.852 ? 27.564  ops/ms
EishayWriteString.gson                            thrpt    5   585.672 ?  3.597  ops/ms
EishayWriteString.jackson                         thrpt    5  1290.470 ? 15.757  ops/ms
EishayWriteStringTree.fastjson1                   thrpt    5  1010.954 ? 13.725  ops/ms
EishayWriteStringTree.fastjson2                   thrpt    5  1508.938 ? 12.891  ops/ms
EishayWriteStringTree.gson                        thrpt    5   624.185 ?  2.545  ops/ms
EishayWriteStringTree.jackson                     thrpt    5  1181.659 ?  9.702  ops/ms
EishayWriteUTF8Bytes.fastjson1                    thrpt    5   692.775 ?  1.034  ops/ms
EishayWriteUTF8Bytes.fastjson2                    thrpt    5  2019.675 ?  5.817  ops/ms
EishayWriteUTF8Bytes.gson                         thrpt    5   485.236 ?  6.604  ops/ms
EishayWriteUTF8Bytes.jackson                      thrpt    5  1108.676 ?  9.865  ops/ms
```
# ecs.g8i.xlarge-jdk-11.0.19
```java
Benchmark                                          Mode  Cnt     Score    Error   Units
EishayParseBinary.fastjson2UTF8Bytes              thrpt    5  1545.422 ?  8.156  ops/ms
EishayParseBinary.hessian                         thrpt    5   334.600 ?  0.515  ops/ms
EishayParseBinary.javaSerialize                   thrpt    5    53.918 ?  0.068  ops/ms
EishayParseBinary.jsonb                           thrpt    5  3374.806 ?  9.604  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes  thrpt    5  2542.270 ?  8.362  ops/ms
EishayParseBinaryArrayMapping.jsonb               thrpt    5  5084.585 ? 32.305  ops/ms
EishayParseBinaryArrayMapping.kryo                thrpt    5  2105.846 ?  8.149  ops/ms
EishayParseBinaryArrayMapping.protobuf            thrpt    5  1669.832 ?  6.225  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB          thrpt    5  2381.550 ? 18.274  ops/ms
EishayParseBinaryAutoType.hessian                 thrpt    5   344.034 ?  1.645  ops/ms
EishayParseBinaryAutoType.javaSerialize           thrpt    5    53.371 ?  0.255  ops/ms
EishayParseString.fastjson1                       thrpt    5  1120.536 ? 24.910  ops/ms
EishayParseString.fastjson2                       thrpt    5  1566.153 ?  6.120  ops/ms
EishayParseString.gson                            thrpt    5   538.069 ?  2.926  ops/ms
EishayParseString.jackson                         thrpt    5   614.191 ?  0.638  ops/ms
EishayParseStringPretty.fastjson1                 thrpt    5   306.862 ?  0.464  ops/ms
EishayParseStringPretty.fastjson2                 thrpt    5  1149.394 ?  1.761  ops/ms
EishayParseStringPretty.gson                      thrpt    5   493.596 ?  2.086  ops/ms
EishayParseStringPretty.jackson                   thrpt    5   566.207 ?  1.411  ops/ms
EishayParseTreeString.fastjson1                   thrpt    5   550.246 ?  1.207  ops/ms
EishayParseTreeString.fastjson2                   thrpt    5  1081.430 ?  1.588  ops/ms
EishayParseTreeString.gson                        thrpt    5   373.732 ?  1.933  ops/ms
EishayParseTreeString.jackson                     thrpt    5   617.104 ?  0.827  ops/ms
EishayParseTreeStringPretty.fastjson1             thrpt    5   469.754 ?  1.011  ops/ms
EishayParseTreeStringPretty.fastjson2             thrpt    5   905.197 ?  0.960  ops/ms
EishayParseTreeStringPretty.gson                  thrpt    5   354.030 ?  0.521  ops/ms
EishayParseTreeStringPretty.jackson               thrpt    5   555.434 ?  1.993  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                thrpt    5   500.025 ?  2.850  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                thrpt    5  1147.806 ?  3.025  ops/ms
EishayParseTreeUTF8Bytes.gson                     thrpt    5   363.967 ?  0.595  ops/ms
EishayParseTreeUTF8Bytes.jackson                  thrpt    5   767.397 ?  1.859  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1          thrpt    5   419.269 ?  1.294  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2          thrpt    5   865.020 ?  2.186  ops/ms
EishayParseTreeUTF8BytesPretty.gson               thrpt    5   340.704 ?  2.587  ops/ms
EishayParseTreeUTF8BytesPretty.jackson            thrpt    5   615.728 ?  1.550  ops/ms
EishayParseUTF8Bytes.fastjson1                    thrpt    5   981.693 ?  0.899  ops/ms
EishayParseUTF8Bytes.fastjson2                    thrpt    5  1537.120 ?  6.035  ops/ms
EishayParseUTF8Bytes.gson                         thrpt    5   364.351 ?  0.780  ops/ms
EishayParseUTF8Bytes.jackson                      thrpt    5   679.992 ?  1.320  ops/ms
EishayParseUTF8BytesPretty.fastjson1              thrpt    5   285.687 ?  0.684  ops/ms
EishayParseUTF8BytesPretty.fastjson2              thrpt    5  1151.518 ?  0.949  ops/ms
EishayParseUTF8BytesPretty.gson                   thrpt    5   339.233 ?  0.845  ops/ms
EishayParseUTF8BytesPretty.jackson                thrpt    5   624.173 ?  1.298  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes              thrpt    5  2087.085 ? 15.516  ops/ms
EishayWriteBinary.hessian                         thrpt    5   351.499 ?  8.566  ops/ms
EishayWriteBinary.javaSerialize                   thrpt    5   267.272 ?  2.929  ops/ms
EishayWriteBinary.jsonb                           thrpt    5  3353.033 ? 21.895  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes  thrpt    5  2818.274 ?  7.931  ops/ms
EishayWriteBinaryArrayMapping.jsonb               thrpt    5  7081.360 ? 38.684  ops/ms
EishayWriteBinaryArrayMapping.kryo                thrpt    5  2456.796 ?  5.302  ops/ms
EishayWriteBinaryArrayMapping.protobuf            thrpt    5  1707.250 ?  3.099  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB          thrpt    5  1952.914 ? 12.332  ops/ms
EishayWriteBinaryAutoType.hessian                 thrpt    5   356.608 ?  3.305  ops/ms
EishayWriteBinaryAutoType.javaSerialize           thrpt    5   288.060 ?  4.124  ops/ms
EishayWriteString.fastjson1                       thrpt    5   727.893 ?  1.697  ops/ms
EishayWriteString.fastjson2                       thrpt    5  1957.322 ?  5.868  ops/ms
EishayWriteString.gson                            thrpt    5   457.153 ?  1.199  ops/ms
EishayWriteString.jackson                         thrpt    5  1178.683 ?  2.937  ops/ms
EishayWriteStringTree.fastjson1                   thrpt    5   966.903 ?  3.717  ops/ms
EishayWriteStringTree.fastjson2                   thrpt    5  1504.782 ? 10.718  ops/ms
EishayWriteStringTree.gson                        thrpt    5   475.260 ?  1.338  ops/ms
EishayWriteStringTree.jackson                     thrpt    5  1144.347 ?  2.513  ops/ms
EishayWriteUTF8Bytes.fastjson1                    thrpt    5   675.854 ?  0.932  ops/ms
EishayWriteUTF8Bytes.fastjson2                    thrpt    5  2147.388 ?  4.978  ops/ms
EishayWriteUTF8Bytes.gson                         thrpt    5   446.452 ?  2.091  ops/ms
EishayWriteUTF8Bytes.jackson                      thrpt    5  1103.739 ?  1.699  ops/ms
```
# ecs.g8i.xlarge-jdk-17.0.7
```java
Benchmark                                          Mode  Cnt     Score    Error   Units
EishayParseBinary.fastjson2UTF8Bytes              thrpt    5  1615.575 ?  2.370  ops/ms
EishayParseBinary.hessian                         thrpt    5   332.009 ?  0.976  ops/ms
EishayParseBinary.javaSerialize                   thrpt    5    57.752 ?  0.069  ops/ms
EishayParseBinary.jsonb                           thrpt    5  3780.839 ?  9.121  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes  thrpt    5  2817.527 ?  6.415  ops/ms
EishayParseBinaryArrayMapping.jsonb               thrpt    5  5870.246 ? 44.258  ops/ms
EishayParseBinaryArrayMapping.kryo                thrpt    5  2261.314 ?  6.033  ops/ms
EishayParseBinaryArrayMapping.protobuf            thrpt    5  2022.071 ? 11.164  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB          thrpt    5  2656.491 ? 19.474  ops/ms
EishayParseBinaryAutoType.hessian                 thrpt    5   328.391 ?  0.625  ops/ms
EishayParseBinaryAutoType.javaSerialize           thrpt    5    58.669 ?  0.051  ops/ms
EishayParseString.fastjson1                       thrpt    5  1603.032 ?  1.317  ops/ms
EishayParseString.fastjson2                       thrpt    5  1651.937 ?  5.104  ops/ms
EishayParseString.gson                            thrpt    5   539.974 ?  0.246  ops/ms
EishayParseString.jackson                         thrpt    5   653.524 ? 18.797  ops/ms
EishayParseStringPretty.fastjson1                 thrpt    5   356.468 ?  0.240  ops/ms
EishayParseStringPretty.fastjson2                 thrpt    5  1240.210 ?  3.126  ops/ms
EishayParseStringPretty.gson                      thrpt    5   489.855 ?  0.495  ops/ms
EishayParseStringPretty.jackson                   thrpt    5   562.466 ?  2.117  ops/ms
EishayParseTreeString.fastjson1                   thrpt    5   717.846 ?  1.621  ops/ms
EishayParseTreeString.fastjson2                   thrpt    5  1281.704 ?  4.468  ops/ms
EishayParseTreeString.gson                        thrpt    5   367.278 ?  0.590  ops/ms
EishayParseTreeString.jackson                     thrpt    5   655.606 ?  2.974  ops/ms
EishayParseTreeStringPretty.fastjson1             thrpt    5   622.572 ?  1.109  ops/ms
EishayParseTreeStringPretty.fastjson2             thrpt    5  1034.418 ?  1.013  ops/ms
EishayParseTreeStringPretty.gson                  thrpt    5   347.354 ?  0.651  ops/ms
EishayParseTreeStringPretty.jackson               thrpt    5   591.818 ?  1.361  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                thrpt    5   654.667 ?  1.498  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                thrpt    5  1233.260 ?  2.623  ops/ms
EishayParseTreeUTF8Bytes.gson                     thrpt    5   359.228 ?  1.905  ops/ms
EishayParseTreeUTF8Bytes.jackson                  thrpt    5   781.804 ?  1.785  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1          thrpt    5   520.058 ?  0.970  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2          thrpt    5  1041.960 ? 18.202  ops/ms
EishayParseTreeUTF8BytesPretty.gson               thrpt    5   339.168 ?  0.961  ops/ms
EishayParseTreeUTF8BytesPretty.jackson            thrpt    5   696.018 ?  1.144  ops/ms
EishayParseUTF8Bytes.fastjson1                    thrpt    5  1193.240 ?  1.216  ops/ms
EishayParseUTF8Bytes.fastjson2                    thrpt    5  1616.565 ?  2.486  ops/ms
EishayParseUTF8Bytes.gson                         thrpt    5   358.849 ?  1.149  ops/ms
EishayParseUTF8Bytes.jackson                      thrpt    5   725.323 ?  1.823  ops/ms
EishayParseUTF8BytesPretty.fastjson1              thrpt    5   325.333 ?  0.509  ops/ms
EishayParseUTF8BytesPretty.fastjson2              thrpt    5  1260.294 ?  2.579  ops/ms
EishayParseUTF8BytesPretty.gson                   thrpt    5   340.318 ?  0.673  ops/ms
EishayParseUTF8BytesPretty.jackson                thrpt    5   651.543 ?  1.217  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes              thrpt    5  2568.701 ? 13.756  ops/ms
EishayWriteBinary.hessian                         thrpt    5   342.251 ?  2.099  ops/ms
EishayWriteBinary.javaSerialize                   thrpt    5   269.449 ?  0.869  ops/ms
EishayWriteBinary.jsonb                           thrpt    5  4391.696 ? 39.950  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes  thrpt    5  2831.900 ?  3.743  ops/ms
EishayWriteBinaryArrayMapping.jsonb               thrpt    5  8004.938 ? 39.164  ops/ms
EishayWriteBinaryArrayMapping.kryo                thrpt    5  2504.014 ?  3.063  ops/ms
EishayWriteBinaryArrayMapping.protobuf            thrpt    5  1933.815 ?  1.560  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB          thrpt    5  2165.508 ?  5.032  ops/ms
EishayWriteBinaryAutoType.hessian                 thrpt    5   340.700 ?  2.474  ops/ms
EishayWriteBinaryAutoType.javaSerialize           thrpt    5   272.056 ?  1.686  ops/ms
EishayWriteString.fastjson1                       thrpt    5   789.117 ?  0.994  ops/ms
EishayWriteString.fastjson2                       thrpt    5  1898.827 ?  2.349  ops/ms
EishayWriteString.gson                            thrpt    5   246.492 ?  0.157  ops/ms
EishayWriteString.jackson                         thrpt    5  1315.670 ?  1.517  ops/ms
EishayWriteStringTree.fastjson1                   thrpt    5  1000.311 ?  1.660  ops/ms
EishayWriteStringTree.fastjson2                   thrpt    5  1310.261 ?  4.549  ops/ms
EishayWriteStringTree.gson                        thrpt    5   254.394 ?  0.153  ops/ms
EishayWriteStringTree.jackson                     thrpt    5  1172.485 ?  2.597  ops/ms
EishayWriteUTF8Bytes.fastjson1                    thrpt    5   636.401 ?  1.279  ops/ms
EishayWriteUTF8Bytes.fastjson2                    thrpt    5  2485.052 ?  6.734  ops/ms
EishayWriteUTF8Bytes.gson                         thrpt    5   246.286 ?  0.248  ops/ms
EishayWriteUTF8Bytes.jackson                      thrpt    5  1238.227 ?  5.184  ops/ms
```
# ecs.g7.xlarge-jdk1.8.0_371
```java
Benchmark                                          Mode  Cnt     Score    Error   Units
EishayParseBinary.fastjson2UTF8Bytes              thrpt    5  1099.692 ?  4.555  ops/ms
EishayParseBinary.hessian                         thrpt    5   294.444 ?  0.494  ops/ms
EishayParseBinary.javaSerialize                   thrpt    5    46.877 ?  0.107  ops/ms
EishayParseBinary.jsonb                           thrpt    5  1737.086 ?  3.040  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes  thrpt    5  1664.323 ?  6.095  ops/ms
EishayParseBinaryArrayMapping.jsonb               thrpt    5  2619.733 ?  6.269  ops/ms
EishayParseBinaryArrayMapping.kryo                thrpt    5  1716.718 ?  6.236  ops/ms
EishayParseBinaryArrayMapping.protobuf            thrpt    5  1045.450 ?  2.249  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB          thrpt    5  1491.659 ?  3.797  ops/ms
EishayParseBinaryAutoType.hessian                 thrpt    5   285.054 ?  0.998  ops/ms
EishayParseBinaryAutoType.javaSerialize           thrpt    5    46.090 ?  0.043  ops/ms
EishayParseString.fastjson1                       thrpt    5   977.168 ?  1.163  ops/ms
EishayParseString.fastjson2                       thrpt    5  1228.732 ?  2.605  ops/ms
EishayParseString.gson                            thrpt    5   426.092 ?  2.818  ops/ms
EishayParseString.jackson                         thrpt    5   529.411 ?  2.523  ops/ms
EishayParseStringPretty.fastjson1                 thrpt    5   271.246 ?  0.875  ops/ms
EishayParseStringPretty.fastjson2                 thrpt    5   963.682 ?  2.355  ops/ms
EishayParseStringPretty.gson                      thrpt    5   409.351 ?  1.906  ops/ms
EishayParseStringPretty.jackson                   thrpt    5   455.074 ?  0.695  ops/ms
EishayParseTreeString.fastjson1                   thrpt    5   486.845 ?  1.322  ops/ms
EishayParseTreeString.fastjson2                   thrpt    5   988.049 ?  5.402  ops/ms
EishayParseTreeString.gson                        thrpt    5   337.434 ?  0.516  ops/ms
EishayParseTreeString.jackson                     thrpt    5   544.864 ?  1.822  ops/ms
EishayParseTreeStringPretty.fastjson1             thrpt    5   454.214 ?  1.154  ops/ms
EishayParseTreeStringPretty.fastjson2             thrpt    5   778.509 ?  1.441  ops/ms
EishayParseTreeStringPretty.gson                  thrpt    5   317.747 ?  0.914  ops/ms
EishayParseTreeStringPretty.jackson               thrpt    5   506.838 ?  0.841  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                thrpt    5   445.204 ?  1.071  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                thrpt    5   841.076 ?  2.878  ops/ms
EishayParseTreeUTF8Bytes.gson                     thrpt    5   303.403 ?  1.438  ops/ms
EishayParseTreeUTF8Bytes.jackson                  thrpt    5   586.673 ?  2.748  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1          thrpt    5   373.771 ?  1.457  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2          thrpt    5   705.923 ?  2.704  ops/ms
EishayParseTreeUTF8BytesPretty.gson               thrpt    5   291.256 ?  1.406  ops/ms
EishayParseTreeUTF8BytesPretty.jackson            thrpt    5   528.277 ?  0.941  ops/ms
EishayParseUTF8Bytes.fastjson1                    thrpt    5   817.797 ?  1.205  ops/ms
EishayParseUTF8Bytes.fastjson2                    thrpt    5  1115.504 ?  3.559  ops/ms
EishayParseUTF8Bytes.gson                         thrpt    5   304.565 ?  5.250  ops/ms
EishayParseUTF8Bytes.jackson                      thrpt    5   567.374 ?  0.928  ops/ms
EishayParseUTF8BytesPretty.fastjson1              thrpt    5   247.996 ?  0.514  ops/ms
EishayParseUTF8BytesPretty.fastjson2              thrpt    5   845.005 ?  1.973  ops/ms
EishayParseUTF8BytesPretty.gson                   thrpt    5   284.904 ?  1.914  ops/ms
EishayParseUTF8BytesPretty.jackson                thrpt    5   502.997 ?  1.938  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes              thrpt    5  1583.744 ?  8.599  ops/ms
EishayWriteBinary.hessian                         thrpt    5   338.644 ?  3.021  ops/ms
EishayWriteBinary.javaSerialize                   thrpt    5   221.790 ?  1.614  ops/ms
EishayWriteBinary.jsonb                           thrpt    5  2083.852 ? 20.732  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes  thrpt    5  1977.545 ?  6.044  ops/ms
EishayWriteBinaryArrayMapping.jsonb               thrpt    5  3420.100 ? 23.806  ops/ms
EishayWriteBinaryArrayMapping.kryo                thrpt    5  1854.600 ?  3.649  ops/ms
EishayWriteBinaryArrayMapping.protobuf            thrpt    5  1321.198 ?  2.044  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB          thrpt    5  1382.169 ?  7.698  ops/ms
EishayWriteBinaryAutoType.hessian                 thrpt    5   334.523 ?  1.836  ops/ms
EishayWriteBinaryAutoType.javaSerialize           thrpt    5   231.100 ?  0.930  ops/ms
EishayWriteString.fastjson1                       thrpt    5   622.295 ?  2.966  ops/ms
EishayWriteString.fastjson2                       thrpt    5  1712.295 ? 13.381  ops/ms
EishayWriteString.gson                            thrpt    5   449.276 ?  3.382  ops/ms
EishayWriteString.jackson                         thrpt    5  1028.523 ?  4.700  ops/ms
EishayWriteStringTree.fastjson1                   thrpt    5   822.104 ?  4.858  ops/ms
EishayWriteStringTree.fastjson2                   thrpt    5  1276.544 ?  9.785  ops/ms
EishayWriteStringTree.gson                        thrpt    5   482.098 ?  2.520  ops/ms
EishayWriteStringTree.jackson                     thrpt    5   945.308 ?  8.303  ops/ms
EishayWriteUTF8Bytes.fastjson1                    thrpt    5   572.999 ?  1.863  ops/ms
EishayWriteUTF8Bytes.fastjson2                    thrpt    5  1600.989 ?  9.876  ops/ms
EishayWriteUTF8Bytes.gson                         thrpt    5   381.374 ?  2.323  ops/ms
EishayWriteUTF8Bytes.jackson                      thrpt    5   930.199 ?  6.061  ops/ms
```
# ecs.g7.xlarge-jdk-11.0.19
```java
Benchmark                                          Mode  Cnt     Score    Error   Units
EishayParseBinary.fastjson2UTF8Bytes              thrpt    5  1219.212 ?  2.387  ops/ms
EishayParseBinary.hessian                         thrpt    5   225.061 ?  0.493  ops/ms
EishayParseBinary.javaSerialize                   thrpt    5    49.750 ?  0.141  ops/ms
EishayParseBinary.jsonb                           thrpt    5  2721.088 ? 14.177  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes  thrpt    5  1928.752 ?  6.682  ops/ms
EishayParseBinaryArrayMapping.jsonb               thrpt    5  3710.841 ? 97.114  ops/ms
EishayParseBinaryArrayMapping.kryo                thrpt    5  1630.425 ?  2.982  ops/ms
EishayParseBinaryArrayMapping.protobuf            thrpt    5  1302.412 ?  6.110  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB          thrpt    5  1939.912 ? 14.478  ops/ms
EishayParseBinaryAutoType.hessian                 thrpt    5   266.509 ?  0.735  ops/ms
EishayParseBinaryAutoType.javaSerialize           thrpt    5    46.981 ?  0.082  ops/ms
EishayParseString.fastjson1                       thrpt    5   898.568 ? 21.900  ops/ms
EishayParseString.fastjson2                       thrpt    5  1231.726 ?  6.925  ops/ms
EishayParseString.gson                            thrpt    5   407.976 ?  0.962  ops/ms
EishayParseString.jackson                         thrpt    5   468.142 ?  2.256  ops/ms
EishayParseStringPretty.fastjson1                 thrpt    5   240.085 ?  0.559  ops/ms
EishayParseStringPretty.fastjson2                 thrpt    5   896.510 ?  1.895  ops/ms
EishayParseStringPretty.gson                      thrpt    5   409.115 ?  0.744  ops/ms
EishayParseStringPretty.jackson                   thrpt    5   422.612 ?  0.799  ops/ms
EishayParseTreeString.fastjson1                   thrpt    5   392.647 ?  1.715  ops/ms
EishayParseTreeString.fastjson2                   thrpt    5   834.317 ?  4.840  ops/ms
EishayParseTreeString.gson                        thrpt    5   317.371 ?  1.384  ops/ms
EishayParseTreeString.jackson                     thrpt    5   456.077 ?  0.627  ops/ms
EishayParseTreeStringPretty.fastjson1             thrpt    5   344.930 ?  1.371  ops/ms
EishayParseTreeStringPretty.fastjson2             thrpt    5   703.461 ?  2.646  ops/ms
EishayParseTreeStringPretty.gson                  thrpt    5   307.868 ?  1.171  ops/ms
EishayParseTreeStringPretty.jackson               thrpt    5   427.458 ?  1.346  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                thrpt    5   366.166 ?  1.950  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                thrpt    5   936.554 ?  2.506  ops/ms
EishayParseTreeUTF8Bytes.gson                     thrpt    5   302.682 ?  1.124  ops/ms
EishayParseTreeUTF8Bytes.jackson                  thrpt    5   523.583 ?  2.413  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1          thrpt    5   314.028 ?  1.361  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2          thrpt    5   705.382 ?  2.769  ops/ms
EishayParseTreeUTF8BytesPretty.gson               thrpt    5   290.970 ?  0.463  ops/ms
EishayParseTreeUTF8BytesPretty.jackson            thrpt    5   477.231 ?  1.374  ops/ms
EishayParseUTF8Bytes.fastjson1                    thrpt    5   753.285 ?  1.117  ops/ms
EishayParseUTF8Bytes.fastjson2                    thrpt    5  1187.295 ?  7.024  ops/ms
EishayParseUTF8Bytes.gson                         thrpt    5   307.130 ?  0.894  ops/ms
EishayParseUTF8Bytes.jackson                      thrpt    5   538.649 ?  1.721  ops/ms
EishayParseUTF8BytesPretty.fastjson1              thrpt    5   220.983 ?  0.134  ops/ms
EishayParseUTF8BytesPretty.fastjson2              thrpt    5   899.487 ?  8.112  ops/ms
EishayParseUTF8BytesPretty.gson                   thrpt    5   290.295 ?  1.466  ops/ms
EishayParseUTF8BytesPretty.jackson                thrpt    5   500.796 ?  3.534  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes              thrpt    5  1603.231 ?  9.690  ops/ms
EishayWriteBinary.hessian                         thrpt    5   324.614 ?  2.017  ops/ms
EishayWriteBinary.javaSerialize                   thrpt    5   217.033 ?  1.488  ops/ms
EishayWriteBinary.jsonb                           thrpt    5  2543.175 ? 11.453  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes  thrpt    5  2154.608 ?  3.262  ops/ms
EishayWriteBinaryArrayMapping.jsonb               thrpt    5  5119.801 ? 33.895  ops/ms
EishayWriteBinaryArrayMapping.kryo                thrpt    5  2051.610 ?  2.237  ops/ms
EishayWriteBinaryArrayMapping.protobuf            thrpt    5  1467.811 ?  1.508  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB          thrpt    5  1465.398 ? 13.700  ops/ms
EishayWriteBinaryAutoType.hessian                 thrpt    5   324.201 ?  0.957  ops/ms
EishayWriteBinaryAutoType.javaSerialize           thrpt    5   221.429 ?  1.720  ops/ms
EishayWriteString.fastjson1                       thrpt    5   598.034 ?  0.853  ops/ms
EishayWriteString.fastjson2                       thrpt    5  1447.373 ?  4.669  ops/ms
EishayWriteString.gson                            thrpt    5   358.069 ?  0.346  ops/ms
EishayWriteString.jackson                         thrpt    5   958.842 ?  2.865  ops/ms
EishayWriteStringTree.fastjson1                   thrpt    5   759.184 ?  3.445  ops/ms
EishayWriteStringTree.fastjson2                   thrpt    5  1302.738 ?  4.937  ops/ms
EishayWriteStringTree.gson                        thrpt    5   373.502 ?  0.654  ops/ms
EishayWriteStringTree.jackson                     thrpt    5   888.980 ?  2.321  ops/ms
EishayWriteUTF8Bytes.fastjson1                    thrpt    5   554.836 ?  1.457  ops/ms
EishayWriteUTF8Bytes.fastjson2                    thrpt    5  1605.967 ?  8.250  ops/ms
EishayWriteUTF8Bytes.gson                         thrpt    5   348.997 ?  1.983  ops/ms
EishayWriteUTF8Bytes.jackson                      thrpt    5   875.346 ?  3.795  ops/ms
```
# ecs.g7.xlarge-jdk-17.0.7
```java
Benchmark                                          Mode  Cnt     Score    Error   Units
EishayParseBinary.fastjson2UTF8Bytes              thrpt    5  1336.625 ?  6.129  ops/ms
EishayParseBinary.hessian                         thrpt    5   257.531 ?  0.541  ops/ms
EishayParseBinary.javaSerialize                   thrpt    5    54.108 ?  0.107  ops/ms
EishayParseBinary.jsonb                           thrpt    5  2978.325 ? 11.871  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes  thrpt    5  2231.598 ?  8.255  ops/ms
EishayParseBinaryArrayMapping.jsonb               thrpt    5  4410.789 ? 24.980  ops/ms
EishayParseBinaryArrayMapping.kryo                thrpt    5  1836.200 ?  6.356  ops/ms
EishayParseBinaryArrayMapping.protobuf            thrpt    5  1702.909 ?  8.307  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB          thrpt    5  2011.917 ?  7.114  ops/ms
EishayParseBinaryAutoType.hessian                 thrpt    5   262.836 ?  1.045  ops/ms
EishayParseBinaryAutoType.javaSerialize           thrpt    5    53.658 ?  0.193  ops/ms
EishayParseString.fastjson1                       thrpt    5  1247.705 ? 31.862  ops/ms
EishayParseString.fastjson2                       thrpt    5  1233.256 ?  1.851  ops/ms
EishayParseString.gson                            thrpt    5   440.958 ?  0.954  ops/ms
EishayParseString.jackson                         thrpt    5   506.806 ?  0.973  ops/ms
EishayParseStringPretty.fastjson1                 thrpt    5   303.324 ?  1.152  ops/ms
EishayParseStringPretty.fastjson2                 thrpt    5   954.571 ?  1.259  ops/ms
EishayParseStringPretty.gson                      thrpt    5   421.307 ?  0.679  ops/ms
EishayParseStringPretty.jackson                   thrpt    5   431.163 ?  1.657  ops/ms
EishayParseTreeString.fastjson1                   thrpt    5   563.777 ?  2.138  ops/ms
EishayParseTreeString.fastjson2                   thrpt    5  1059.564 ?  4.195  ops/ms
EishayParseTreeString.gson                        thrpt    5   312.850 ?  2.671  ops/ms
EishayParseTreeString.jackson                     thrpt    5   514.542 ?  5.725  ops/ms
EishayParseTreeStringPretty.fastjson1             thrpt    5   490.770 ?  1.671  ops/ms
EishayParseTreeStringPretty.fastjson2             thrpt    5   818.577 ?  3.043  ops/ms
EishayParseTreeStringPretty.gson                  thrpt    5   294.418 ?  0.512  ops/ms
EishayParseTreeStringPretty.jackson               thrpt    5   471.290 ?  1.453  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                thrpt    5   499.936 ?  3.165  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                thrpt    5  1019.365 ?  5.746  ops/ms
EishayParseTreeUTF8Bytes.gson                     thrpt    5   305.572 ?  0.650  ops/ms
EishayParseTreeUTF8Bytes.jackson                  thrpt    5   590.909 ?  3.333  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1          thrpt    5   421.945 ?  2.618  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2          thrpt    5   805.016 ?  3.700  ops/ms
EishayParseTreeUTF8BytesPretty.gson               thrpt    5   293.834 ?  0.931  ops/ms
EishayParseTreeUTF8BytesPretty.jackson            thrpt    5   520.087 ?  1.661  ops/ms
EishayParseUTF8Bytes.fastjson1                    thrpt    5   960.051 ?  3.947  ops/ms
EishayParseUTF8Bytes.fastjson2                    thrpt    5  1319.306 ?  4.053  ops/ms
EishayParseUTF8Bytes.gson                         thrpt    5   318.748 ?  1.696  ops/ms
EishayParseUTF8Bytes.jackson                      thrpt    5   546.506 ?  0.750  ops/ms
EishayParseUTF8BytesPretty.fastjson1              thrpt    5   271.728 ?  0.289  ops/ms
EishayParseUTF8BytesPretty.fastjson2              thrpt    5   965.949 ?  1.617  ops/ms
EishayParseUTF8BytesPretty.gson                   thrpt    5   292.435 ?  1.339  ops/ms
EishayParseUTF8BytesPretty.jackson                thrpt    5   499.816 ?  1.073  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes              thrpt    5  1936.673 ? 10.493  ops/ms
EishayWriteBinary.hessian                         thrpt    5   320.551 ?  1.664  ops/ms
EishayWriteBinary.javaSerialize                   thrpt    5   217.297 ?  1.347  ops/ms
EishayWriteBinary.jsonb                           thrpt    5  3515.606 ? 23.115  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes  thrpt    5  2266.282 ?  7.669  ops/ms
EishayWriteBinaryArrayMapping.jsonb               thrpt    5  6212.794 ? 43.052  ops/ms
EishayWriteBinaryArrayMapping.kryo                thrpt    5  1984.712 ?  3.091  ops/ms
EishayWriteBinaryArrayMapping.protobuf            thrpt    5  1665.128 ?  4.535  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB          thrpt    5  1701.095 ?  6.855  ops/ms
EishayWriteBinaryAutoType.hessian                 thrpt    5   323.521 ?  4.530  ops/ms
EishayWriteBinaryAutoType.javaSerialize           thrpt    5   219.721 ?  1.329  ops/ms
EishayWriteString.fastjson1                       thrpt    5   636.110 ?  0.362  ops/ms
EishayWriteString.fastjson2                       thrpt    5  1663.356 ?  6.709  ops/ms
EishayWriteString.gson                            thrpt    5   241.458 ?  0.989  ops/ms
EishayWriteString.jackson                         thrpt    5  1023.048 ?  3.002  ops/ms
EishayWriteStringTree.fastjson1                   thrpt    5   808.074 ?  5.321  ops/ms
EishayWriteStringTree.fastjson2                   thrpt    5  1238.635 ?  5.174  ops/ms
EishayWriteStringTree.gson                        thrpt    5   250.976 ?  0.757  ops/ms
EishayWriteStringTree.jackson                     thrpt    5   900.744 ?  3.126  ops/ms
EishayWriteUTF8Bytes.fastjson1                    thrpt    5   555.388 ? 40.677  ops/ms
EishayWriteUTF8Bytes.fastjson2                    thrpt    5  1910.674 ?  7.949  ops/ms
EishayWriteUTF8Bytes.gson                         thrpt    5   240.553 ?  0.385  ops/ms
EishayWriteUTF8Bytes.jackson                      thrpt    5   994.487 ?  3.943  ops/ms
```
# ecs.g8m.xlarge-jdk1.8.0_371
```java
Benchmark                                          Mode  Cnt     Score    Error   Units
EishayParseBinary.fastjson2UTF8Bytes              thrpt    5   805.894 ? 50.808  ops/ms
EishayParseBinary.hessian                         thrpt    5   194.563 ?  5.237  ops/ms
EishayParseBinary.javaSerialize                   thrpt    5    40.059 ?  0.393  ops/ms
EishayParseBinary.jsonb                           thrpt    5  1356.707 ? 51.119  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes  thrpt    5  1236.417 ? 51.679  ops/ms
EishayParseBinaryArrayMapping.jsonb               thrpt    5  2209.794 ? 89.475  ops/ms
EishayParseBinaryArrayMapping.kryo                thrpt    5  1242.221 ? 36.828  ops/ms
EishayParseBinaryArrayMapping.protobuf            thrpt    5   749.934 ? 35.242  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB          thrpt    5  1136.818 ? 42.888  ops/ms
EishayParseBinaryAutoType.hessian                 thrpt    5   216.429 ?  6.845  ops/ms
EishayParseBinaryAutoType.javaSerialize           thrpt    5    38.375 ?  0.642  ops/ms
EishayParseString.fastjson1                       thrpt    5   730.718 ? 31.802  ops/ms
EishayParseString.fastjson2                       thrpt    5   905.570 ? 42.907  ops/ms
EishayParseString.gson                            thrpt    5   353.498 ?  8.393  ops/ms
EishayParseString.jackson                         thrpt    5   364.425 ?  9.829  ops/ms
EishayParseStringPretty.fastjson1                 thrpt    5   201.504 ?  6.130  ops/ms
EishayParseStringPretty.fastjson2                 thrpt    5   699.142 ? 20.085  ops/ms
EishayParseStringPretty.gson                      thrpt    5   324.626 ?  6.072  ops/ms
EishayParseStringPretty.jackson                   thrpt    5   311.221 ? 11.644  ops/ms
EishayParseTreeString.fastjson1                   thrpt    5   294.953 ? 18.202  ops/ms
EishayParseTreeString.fastjson2                   thrpt    5   568.553 ? 50.774  ops/ms
EishayParseTreeString.gson                        thrpt    5   258.463 ? 10.057  ops/ms
EishayParseTreeString.jackson                     thrpt    5   282.663 ? 10.106  ops/ms
EishayParseTreeStringPretty.fastjson1             thrpt    5   267.267 ? 10.137  ops/ms
EishayParseTreeStringPretty.fastjson2             thrpt    5   498.184 ? 22.614  ops/ms
EishayParseTreeStringPretty.gson                  thrpt    5   243.532 ?  3.855  ops/ms
EishayParseTreeStringPretty.jackson               thrpt    5   264.952 ?  5.334  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                thrpt    5   249.954 ?  8.757  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                thrpt    5   563.551 ? 36.001  ops/ms
EishayParseTreeUTF8Bytes.gson                     thrpt    5   231.978 ? 13.844  ops/ms
EishayParseTreeUTF8Bytes.jackson                  thrpt    5   282.514 ? 16.917  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1          thrpt    5   257.259 ? 11.764  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2          thrpt    5   497.829 ? 54.224  ops/ms
EishayParseTreeUTF8BytesPretty.gson               thrpt    5   227.554 ?  5.099  ops/ms
EishayParseTreeUTF8BytesPretty.jackson            thrpt    5   276.693 ?  9.284  ops/ms
EishayParseUTF8Bytes.fastjson1                    thrpt    5   674.229 ? 12.410  ops/ms
EishayParseUTF8Bytes.fastjson2                    thrpt    5   795.137 ? 63.713  ops/ms
EishayParseUTF8Bytes.gson                         thrpt    5   239.929 ?  4.478  ops/ms
EishayParseUTF8Bytes.jackson                      thrpt    5   370.150 ? 29.033  ops/ms
EishayParseUTF8BytesPretty.fastjson1              thrpt    5   196.830 ?  6.466  ops/ms
EishayParseUTF8BytesPretty.fastjson2              thrpt    5   663.885 ? 23.050  ops/ms
EishayParseUTF8BytesPretty.gson                   thrpt    5   214.301 ?  6.500  ops/ms
EishayParseUTF8BytesPretty.jackson                thrpt    5   344.330 ? 23.644  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes              thrpt    5  1330.854 ? 21.628  ops/ms
EishayWriteBinary.hessian                         thrpt    5   339.293 ?  2.143  ops/ms
EishayWriteBinary.javaSerialize                   thrpt    5   203.945 ?  2.279  ops/ms
EishayWriteBinary.jsonb                           thrpt    5  1750.218 ?  7.975  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes  thrpt    5  1614.920 ? 25.629  ops/ms
EishayWriteBinaryArrayMapping.jsonb               thrpt    5  2859.545 ? 62.511  ops/ms
EishayWriteBinaryArrayMapping.kryo                thrpt    5  1455.185 ?  4.205  ops/ms
EishayWriteBinaryArrayMapping.protobuf            thrpt    5   994.066 ? 23.793  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB          thrpt    5  1178.156 ? 19.068  ops/ms
EishayWriteBinaryAutoType.hessian                 thrpt    5   352.542 ?  2.686  ops/ms
EishayWriteBinaryAutoType.javaSerialize           thrpt    5   206.525 ?  0.980  ops/ms
EishayWriteString.fastjson1                       thrpt    5   521.426 ? 18.933  ops/ms
EishayWriteString.fastjson2                       thrpt    5  1359.108 ? 19.127  ops/ms
EishayWriteString.gson                            thrpt    5   374.484 ?  4.191  ops/ms
EishayWriteString.jackson                         thrpt    5   637.953 ? 12.525  ops/ms
EishayWriteStringTree.fastjson1                   thrpt    5   589.634 ? 10.846  ops/ms
EishayWriteStringTree.fastjson2                   thrpt    5   965.250 ?  7.074  ops/ms
EishayWriteStringTree.gson                        thrpt    5   437.534 ?  5.400  ops/ms
EishayWriteStringTree.jackson                     thrpt    5   698.779 ?  8.239  ops/ms
EishayWriteUTF8Bytes.fastjson1                    thrpt    5   479.496 ? 10.368  ops/ms
EishayWriteUTF8Bytes.fastjson2                    thrpt    5  1336.315 ? 23.168  ops/ms
EishayWriteUTF8Bytes.gson                         thrpt    5   319.024 ?  1.965  ops/ms
EishayWriteUTF8Bytes.jackson                      thrpt    5   672.574 ?  7.817  ops/ms
```
# ecs.g8m.xlarge-jdk-11.0.19
```java
Benchmark                                          Mode  Cnt     Score     Error   Units
EishayParseBinary.fastjson2UTF8Bytes              thrpt    5  1059.111 ?  34.299  ops/ms
EishayParseBinary.hessian                         thrpt    5   213.985 ?   5.020  ops/ms
EishayParseBinary.javaSerialize                   thrpt    5    40.898 ?   0.141  ops/ms
EishayParseBinary.jsonb                           thrpt    5  1865.068 ? 119.298  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes  thrpt    5  1543.569 ?  26.309  ops/ms
EishayParseBinaryArrayMapping.jsonb               thrpt    5  2780.071 ?  59.765  ops/ms
EishayParseBinaryArrayMapping.kryo                thrpt    5  1361.626 ?  22.220  ops/ms
EishayParseBinaryArrayMapping.protobuf            thrpt    5   960.236 ?  16.815  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB          thrpt    5  1481.965 ?  28.109  ops/ms
EishayParseBinaryAutoType.hessian                 thrpt    5   217.012 ?   1.490  ops/ms
EishayParseBinaryAutoType.javaSerialize           thrpt    5    40.620 ?   0.269  ops/ms
EishayParseString.fastjson1                       thrpt    5   824.498 ?  12.431  ops/ms
EishayParseString.fastjson2                       thrpt    5  1058.371 ?   6.278  ops/ms
EishayParseString.gson                            thrpt    5   383.066 ?   3.025  ops/ms
EishayParseString.jackson                         thrpt    5   388.054 ?   3.252  ops/ms
EishayParseStringPretty.fastjson1                 thrpt    5   240.396 ?   3.007  ops/ms
EishayParseStringPretty.fastjson2                 thrpt    5   772.312 ?   6.668  ops/ms
EishayParseStringPretty.gson                      thrpt    5   348.749 ?   2.196  ops/ms
EishayParseStringPretty.jackson                   thrpt    5   355.535 ?   4.414  ops/ms
EishayParseTreeString.fastjson1                   thrpt    5   368.274 ?   7.197  ops/ms
EishayParseTreeString.fastjson2                   thrpt    5   740.065 ?  16.725  ops/ms
EishayParseTreeString.gson                        thrpt    5   294.599 ?  10.278  ops/ms
EishayParseTreeString.jackson                     thrpt    5   391.670 ?   9.770  ops/ms
EishayParseTreeStringPretty.fastjson1             thrpt    5   322.124 ?   2.992  ops/ms
EishayParseTreeStringPretty.fastjson2             thrpt    5   620.401 ?  22.777  ops/ms
EishayParseTreeStringPretty.gson                  thrpt    5   281.603 ?   3.272  ops/ms
EishayParseTreeStringPretty.jackson               thrpt    5   334.001 ?   7.753  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                thrpt    5   335.318 ?   4.304  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                thrpt    5   720.530 ?  24.529  ops/ms
EishayParseTreeUTF8Bytes.gson                     thrpt    5   283.461 ?   6.931  ops/ms
EishayParseTreeUTF8Bytes.jackson                  thrpt    5   410.794 ?   4.755  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1          thrpt    5   288.604 ?   4.451  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2          thrpt    5   604.689 ?  11.504  ops/ms
EishayParseTreeUTF8BytesPretty.gson               thrpt    5   271.348 ?   1.179  ops/ms
EishayParseTreeUTF8BytesPretty.jackson            thrpt    5   397.009 ?  18.105  ops/ms
EishayParseUTF8Bytes.fastjson1                    thrpt    5   675.879 ?   9.918  ops/ms
EishayParseUTF8Bytes.fastjson2                    thrpt    5  1055.207 ?  18.947  ops/ms
EishayParseUTF8Bytes.gson                         thrpt    5   294.944 ?   2.849  ops/ms
EishayParseUTF8Bytes.jackson                      thrpt    5   424.879 ?   6.990  ops/ms
EishayParseUTF8BytesPretty.fastjson1              thrpt    5   221.850 ?   1.679  ops/ms
EishayParseUTF8BytesPretty.fastjson2              thrpt    5   779.170 ?   7.959  ops/ms
EishayParseUTF8BytesPretty.gson                   thrpt    5   271.855 ?   2.566  ops/ms
EishayParseUTF8BytesPretty.jackson                thrpt    5   399.377 ?   7.783  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes              thrpt    5  1447.556 ?  10.952  ops/ms
EishayWriteBinary.hessian                         thrpt    5   365.460 ?   0.902  ops/ms
EishayWriteBinary.javaSerialize                   thrpt    5   232.902 ?   2.328  ops/ms
EishayWriteBinary.jsonb                           thrpt    5  2291.742 ?  30.548  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes  thrpt    5  1803.463 ?  27.040  ops/ms
EishayWriteBinaryArrayMapping.jsonb               thrpt    5  4445.556 ? 169.080  ops/ms
EishayWriteBinaryArrayMapping.kryo                thrpt    5  1548.904 ?   6.117  ops/ms
EishayWriteBinaryArrayMapping.protobuf            thrpt    5  1282.556 ?  32.903  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB          thrpt    5  1332.330 ?  13.550  ops/ms
EishayWriteBinaryAutoType.hessian                 thrpt    5   370.563 ?   0.455  ops/ms
EishayWriteBinaryAutoType.javaSerialize           thrpt    5   219.444 ?   1.496  ops/ms
EishayWriteString.fastjson1                       thrpt    5   596.439 ?   3.664  ops/ms
EishayWriteString.fastjson2                       thrpt    5  1349.019 ?   5.551  ops/ms
EishayWriteString.gson                            thrpt    5   317.697 ?  61.324  ops/ms
EishayWriteString.jackson                         thrpt    5   690.609 ?   7.621  ops/ms
EishayWriteStringTree.fastjson1                   thrpt    5   627.204 ?   5.842  ops/ms
EishayWriteStringTree.fastjson2                   thrpt    5  1099.278 ?   4.346  ops/ms
EishayWriteStringTree.gson                        thrpt    5   356.512 ?   2.864  ops/ms
EishayWriteStringTree.jackson                     thrpt    5   711.386 ?   2.841  ops/ms
EishayWriteUTF8Bytes.fastjson1                    thrpt    5   485.806 ?   1.646  ops/ms
EishayWriteUTF8Bytes.fastjson2                    thrpt    5  1458.968 ?   9.909  ops/ms
EishayWriteUTF8Bytes.gson                         thrpt    5   304.274 ?   0.799  ops/ms
EishayWriteUTF8Bytes.jackson                      thrpt    5   641.318 ?   6.921  ops/ms
```
# ecs.g8m.xlarge-jdk-17.0.7
```java
Benchmark                                          Mode  Cnt     Score     Error   Units
EishayParseBinary.fastjson2UTF8Bytes              thrpt    5  1061.480 ?  40.873  ops/ms
EishayParseBinary.hessian                         thrpt    5   229.909 ?   5.850  ops/ms
EishayParseBinary.javaSerialize                   thrpt    5    41.594 ?   0.080  ops/ms
EishayParseBinary.jsonb                           thrpt    5  2014.393 ?  89.214  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes  thrpt    5  1565.692 ?  23.470  ops/ms
EishayParseBinaryArrayMapping.jsonb               thrpt    5  2849.493 ? 257.909  ops/ms
EishayParseBinaryArrayMapping.kryo                thrpt    5  1439.197 ?  47.341  ops/ms
EishayParseBinaryArrayMapping.protobuf            thrpt    5  1147.054 ?  75.450  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB          thrpt    5  1503.654 ?  25.130  ops/ms
EishayParseBinaryAutoType.hessian                 thrpt    5   232.438 ?   3.381  ops/ms
EishayParseBinaryAutoType.javaSerialize           thrpt    5    41.483 ?   0.208  ops/ms
EishayParseString.fastjson1                       thrpt    5  1086.870 ?  19.451  ops/ms
EishayParseString.fastjson2                       thrpt    5  1079.957 ?  23.636  ops/ms
EishayParseString.gson                            thrpt    5   383.359 ?   1.286  ops/ms
EishayParseString.jackson                         thrpt    5   391.753 ?  10.528  ops/ms
EishayParseStringPretty.fastjson1                 thrpt    5   261.972 ?   4.956  ops/ms
EishayParseStringPretty.fastjson2                 thrpt    5   784.044 ?   9.520  ops/ms
EishayParseStringPretty.gson                      thrpt    5   356.599 ?   1.591  ops/ms
EishayParseStringPretty.jackson                   thrpt    5   357.179 ?   3.994  ops/ms
EishayParseTreeString.fastjson1                   thrpt    5   388.579 ?   8.138  ops/ms
EishayParseTreeString.fastjson2                   thrpt    5   742.456 ?  44.287  ops/ms
EishayParseTreeString.gson                        thrpt    5   300.448 ?   4.067  ops/ms
EishayParseTreeString.jackson                     thrpt    5   369.548 ?   4.999  ops/ms
EishayParseTreeStringPretty.fastjson1             thrpt    5   348.886 ?   3.240  ops/ms
EishayParseTreeStringPretty.fastjson2             thrpt    5   626.961 ?  13.378  ops/ms
EishayParseTreeStringPretty.gson                  thrpt    5   280.027 ?   4.231  ops/ms
EishayParseTreeStringPretty.jackson               thrpt    5   333.246 ?   5.318  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                thrpt    5   357.119 ?  11.521  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                thrpt    5   744.474 ?  33.343  ops/ms
EishayParseTreeUTF8Bytes.gson                     thrpt    5   287.445 ?   7.581  ops/ms
EishayParseTreeUTF8Bytes.jackson                  thrpt    5   425.046 ?  11.846  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1          thrpt    5   304.027 ?   4.061  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2          thrpt    5   617.203 ?  19.003  ops/ms
EishayParseTreeUTF8BytesPretty.gson               thrpt    5   275.588 ?   3.360  ops/ms
EishayParseTreeUTF8BytesPretty.jackson            thrpt    5   406.569 ?  12.289  ops/ms
EishayParseUTF8Bytes.fastjson1                    thrpt    5   823.433 ?  36.147  ops/ms
EishayParseUTF8Bytes.fastjson2                    thrpt    5  1082.319 ?  20.868  ops/ms
EishayParseUTF8Bytes.gson                         thrpt    5   288.797 ?   7.017  ops/ms
EishayParseUTF8Bytes.jackson                      thrpt    5   447.528 ?   8.058  ops/ms
EishayParseUTF8BytesPretty.fastjson1              thrpt    5   242.056 ?   3.154  ops/ms
EishayParseUTF8BytesPretty.fastjson2              thrpt    5   783.925 ?  10.897  ops/ms
EishayParseUTF8BytesPretty.gson                   thrpt    5   270.873 ?   3.440  ops/ms
EishayParseUTF8BytesPretty.jackson                thrpt    5   400.385 ?   6.046  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes              thrpt    5  1436.709 ?  31.906  ops/ms
EishayWriteBinary.hessian                         thrpt    5   341.313 ?   1.538  ops/ms
EishayWriteBinary.javaSerialize                   thrpt    5   213.313 ?   1.995  ops/ms
EishayWriteBinary.jsonb                           thrpt    5  2326.174 ?  50.104  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes  thrpt    5  1773.506 ?  31.041  ops/ms
EishayWriteBinaryArrayMapping.jsonb               thrpt    5  4639.577 ? 133.396  ops/ms
EishayWriteBinaryArrayMapping.kryo                thrpt    5  1531.842 ?   9.495  ops/ms
EishayWriteBinaryArrayMapping.protobuf            thrpt    5  1270.836 ?  29.857  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB          thrpt    5  1305.512 ?  22.060  ops/ms
EishayWriteBinaryAutoType.hessian                 thrpt    5   359.432 ?   0.968  ops/ms
EishayWriteBinaryAutoType.javaSerialize           thrpt    5   214.390 ?   1.265  ops/ms
EishayWriteString.fastjson1                       thrpt    5   566.381 ?   7.188  ops/ms
EishayWriteString.fastjson2                       thrpt    5  1325.282 ?  10.373  ops/ms
EishayWriteString.gson                            thrpt    5   217.381 ?   0.995  ops/ms
EishayWriteString.jackson                         thrpt    5   685.799 ?  18.398  ops/ms
EishayWriteStringTree.fastjson1                   thrpt    5   664.893 ?   9.099  ops/ms
EishayWriteStringTree.fastjson2                   thrpt    5  1084.069 ?   4.363  ops/ms
EishayWriteStringTree.gson                        thrpt    5   226.657 ?   1.081  ops/ms
EishayWriteStringTree.jackson                     thrpt    5   687.701 ?   3.620  ops/ms
EishayWriteUTF8Bytes.fastjson1                    thrpt    5   521.881 ?   3.000  ops/ms
EishayWriteUTF8Bytes.fastjson2                    thrpt    5  1431.852 ?  10.492  ops/ms
EishayWriteUTF8Bytes.gson                         thrpt    5   215.675 ?   1.527  ops/ms
EishayWriteUTF8Bytes.jackson                      thrpt    5   711.620 ?   5.280  ops/ms
```
# OrangePI5-jdk1.8.0_371
```java
Benchmark                                          Mode  Cnt     Score    Error   Units
EishayParseBinary.fastjson2UTF8Bytes              thrpt    5   588.589 ± 17.993  ops/ms
EishayParseBinary.hessian                         thrpt    5   134.642 ±  5.233  ops/ms
EishayParseBinary.javaSerialize                   thrpt    5    26.401 ±  0.896  ops/ms
EishayParseBinary.jsonb                           thrpt    5   983.412 ± 66.712  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes  thrpt    5   863.257 ± 31.629  ops/ms
EishayParseBinaryArrayMapping.jsonb               thrpt    5  1669.667 ± 32.148  ops/ms
EishayParseBinaryArrayMapping.kryo                thrpt    5   965.143 ± 41.204  ops/ms
EishayParseBinaryArrayMapping.protobuf            thrpt    5   674.502 ± 43.771  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB          thrpt    5   628.352 ± 12.340  ops/ms
EishayParseBinaryAutoType.hessian                 thrpt    5   133.951 ±  7.445  ops/ms
EishayParseBinaryAutoType.javaSerialize           thrpt    5    26.504 ±  1.387  ops/ms
EishayParseString.fastjson1                       thrpt    5   557.644 ± 40.212  ops/ms
EishayParseString.fastjson2                       thrpt    5   658.789 ± 31.796  ops/ms
EishayParseString.gson                            thrpt    5   218.434 ±  9.571  ops/ms
EishayParseString.jackson                         thrpt    5   262.431 ± 13.286  ops/ms
EishayParseStringPretty.fastjson1                 thrpt    5   155.232 ±  4.791  ops/ms
EishayParseStringPretty.fastjson2                 thrpt    5   521.999 ± 52.434  ops/ms
EishayParseStringPretty.gson                      thrpt    5   202.117 ±  5.813  ops/ms
EishayParseStringPretty.jackson                   thrpt    5   240.878 ±  6.802  ops/ms
EishayParseTreeString.fastjson1                   thrpt    5   216.497 ± 18.565  ops/ms
EishayParseTreeString.fastjson2                   thrpt    5   438.136 ± 23.592  ops/ms
EishayParseTreeString.gson                        thrpt    5   176.791 ±  8.795  ops/ms
EishayParseTreeString.jackson                     thrpt    5   233.052 ± 17.104  ops/ms
EishayParseTreeStringPretty.fastjson1             thrpt    5   205.844 ± 16.872  ops/ms
EishayParseTreeStringPretty.fastjson2             thrpt    5   384.140 ± 15.678  ops/ms
EishayParseTreeStringPretty.gson                  thrpt    5   167.332 ±  8.384  ops/ms
EishayParseTreeStringPretty.jackson               thrpt    5   224.636 ± 12.964  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                thrpt    5   176.874 ±  9.818  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                thrpt    5   406.135 ± 64.087  ops/ms
EishayParseTreeUTF8Bytes.gson                     thrpt    5   153.486 ±  5.718  ops/ms
EishayParseTreeUTF8Bytes.jackson                  thrpt    5   292.207 ± 10.864  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1          thrpt    5   147.891 ± 10.289  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2          thrpt    5   362.165 ± 17.399  ops/ms
EishayParseTreeUTF8BytesPretty.gson               thrpt    5   141.949 ±  9.358  ops/ms
EishayParseTreeUTF8BytesPretty.jackson            thrpt    5   245.528 ±  9.041  ops/ms
EishayParseUTF8Bytes.fastjson1                    thrpt    5   400.301 ± 29.140  ops/ms
EishayParseUTF8Bytes.fastjson2                    thrpt    5   584.183 ± 29.392  ops/ms
EishayParseUTF8Bytes.gson                         thrpt    5   153.752 ±  8.658  ops/ms
EishayParseUTF8Bytes.jackson                      thrpt    5   317.547 ± 25.488  ops/ms
EishayParseUTF8BytesPretty.fastjson1              thrpt    5   140.283 ±  6.469  ops/ms
EishayParseUTF8BytesPretty.fastjson2              thrpt    5   486.893 ± 21.575  ops/ms
EishayParseUTF8BytesPretty.gson                   thrpt    5   142.338 ±  7.797  ops/ms
EishayParseUTF8BytesPretty.jackson                thrpt    5   279.594 ±  8.837  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes              thrpt    5   841.020 ± 46.884  ops/ms
EishayWriteBinary.hessian                         thrpt    5   185.045 ± 18.020  ops/ms
EishayWriteBinary.javaSerialize                   thrpt    5   124.414 ±  6.163  ops/ms
EishayWriteBinary.jsonb                           thrpt    5  1068.958 ± 49.781  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes  thrpt    5  1015.154 ± 36.759  ops/ms
EishayWriteBinaryArrayMapping.jsonb               thrpt    5  1640.757 ± 61.450  ops/ms
EishayWriteBinaryArrayMapping.kryo                thrpt    5   820.359 ± 36.493  ops/ms
EishayWriteBinaryArrayMapping.protobuf            thrpt    5   712.158 ± 16.774  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB          thrpt    5   689.686 ± 27.705  ops/ms
EishayWriteBinaryAutoType.hessian                 thrpt    5   197.567 ± 47.674  ops/ms
EishayWriteBinaryAutoType.javaSerialize           thrpt    5   121.620 ±  6.751  ops/ms
EishayWriteString.fastjson1                       thrpt    5   296.902 ± 12.858  ops/ms
EishayWriteString.fastjson2                       thrpt    5   785.712 ± 34.183  ops/ms
EishayWriteString.gson                            thrpt    5   205.971 ±  9.479  ops/ms
EishayWriteString.jackson                         thrpt    5   393.356 ± 11.654  ops/ms
EishayWriteStringTree.fastjson1                   thrpt    5   332.468 ± 19.546  ops/ms
EishayWriteStringTree.fastjson2                   thrpt    5   498.008 ± 37.681  ops/ms
EishayWriteStringTree.gson                        thrpt    5   225.469 ± 18.135  ops/ms
EishayWriteStringTree.jackson                     thrpt    5   388.994 ± 16.533  ops/ms
EishayWriteUTF8Bytes.fastjson1                    thrpt    5   297.469 ± 16.435  ops/ms
EishayWriteUTF8Bytes.fastjson2                    thrpt    5   840.577 ± 40.029  ops/ms
EishayWriteUTF8Bytes.gson                         thrpt    5   175.450 ±  4.230  ops/ms
EishayWriteUTF8Bytes.jackson                      thrpt    5   403.985 ± 29.523  ops/ms
```
# OrangePI5-jdk-11.0.19
```java
Benchmark                                          Mode  Cnt     Score    Error   Units
EishayParseBinary.fastjson2UTF8Bytes              thrpt    5   671.359 ± 22.628  ops/ms
EishayParseBinary.hessian                         thrpt    5   130.996 ±  3.850  ops/ms
EishayParseBinary.javaSerialize                   thrpt    5    25.848 ±  0.962  ops/ms
EishayParseBinary.jsonb                           thrpt    5  1278.773 ± 62.654  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes  thrpt    5  1008.038 ± 25.847  ops/ms
EishayParseBinaryArrayMapping.jsonb               thrpt    5  1985.550 ± 86.488  ops/ms
EishayParseBinaryArrayMapping.kryo                thrpt    5   940.782 ± 39.479  ops/ms
EishayParseBinaryArrayMapping.protobuf            thrpt    5   712.588 ± 39.058  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB          thrpt    5   849.948 ± 24.417  ops/ms
EishayParseBinaryAutoType.hessian                 thrpt    5   132.140 ±  5.701  ops/ms
EishayParseBinaryAutoType.javaSerialize           thrpt    5    27.071 ±  0.101  ops/ms
EishayParseString.fastjson1                       thrpt    5   567.707 ± 19.950  ops/ms
EishayParseString.fastjson2                       thrpt    5   675.092 ± 20.979  ops/ms
EishayParseString.gson                            thrpt    5   225.041 ±  0.868  ops/ms
EishayParseString.jackson                         thrpt    5   261.703 ± 10.150  ops/ms
EishayParseStringPretty.fastjson1                 thrpt    5   160.572 ±  4.336  ops/ms
EishayParseStringPretty.fastjson2                 thrpt    5   523.165 ± 19.006  ops/ms
EishayParseStringPretty.gson                      thrpt    5   211.465 ±  7.031  ops/ms
EishayParseStringPretty.jackson                   thrpt    5   246.524 ±  1.877  ops/ms
EishayParseTreeString.fastjson1                   thrpt    5   238.305 ± 12.766  ops/ms
EishayParseTreeString.fastjson2                   thrpt    5   477.147 ± 10.597  ops/ms
EishayParseTreeString.gson                        thrpt    5   185.335 ±  3.949  ops/ms
EishayParseTreeString.jackson                     thrpt    5   240.754 ±  3.750  ops/ms
EishayParseTreeStringPretty.fastjson1             thrpt    5   190.107 ±  1.509  ops/ms
EishayParseTreeStringPretty.fastjson2             thrpt    5   404.412 ± 11.645  ops/ms
EishayParseTreeStringPretty.gson                  thrpt    5   177.179 ±  7.717  ops/ms
EishayParseTreeStringPretty.jackson               thrpt    5   228.966 ±  7.962  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                thrpt    5   207.474 ±  8.896  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                thrpt    5   479.636 ± 21.027  ops/ms
EishayParseTreeUTF8Bytes.gson                     thrpt    5   175.221 ±  4.591  ops/ms
EishayParseTreeUTF8Bytes.jackson                  thrpt    5   291.951 ±  5.202  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1          thrpt    5   165.093 ± 12.486  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2          thrpt    5   402.163 ±  5.619  ops/ms
EishayParseTreeUTF8BytesPretty.gson               thrpt    5   164.482 ±  7.591  ops/ms
EishayParseTreeUTF8BytesPretty.jackson            thrpt    5   267.135 ±  7.210  ops/ms
EishayParseUTF8Bytes.fastjson1                    thrpt    5   449.618 ±  7.325  ops/ms
EishayParseUTF8Bytes.fastjson2                    thrpt    5   683.318 ± 13.575  ops/ms
EishayParseUTF8Bytes.gson                         thrpt    5   175.227 ±  4.236  ops/ms
EishayParseUTF8Bytes.jackson                      thrpt    5   302.992 ±  9.713  ops/ms
EishayParseUTF8BytesPretty.fastjson1              thrpt    5   144.994 ±  0.292  ops/ms
EishayParseUTF8BytesPretty.fastjson2              thrpt    5   525.902 ± 22.991  ops/ms
EishayParseUTF8BytesPretty.gson                   thrpt    5   164.624 ±  6.118  ops/ms
EishayParseUTF8BytesPretty.jackson                thrpt    5   275.300 ±  4.554  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes              thrpt    5   916.664 ± 46.215  ops/ms
EishayWriteBinary.hessian                         thrpt    5   192.280 ± 17.104  ops/ms
EishayWriteBinary.javaSerialize                   thrpt    5   126.346 ±  5.042  ops/ms
EishayWriteBinary.jsonb                           thrpt    5  1380.106 ± 19.652  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes  thrpt    5  1085.526 ± 37.978  ops/ms
EishayWriteBinaryArrayMapping.jsonb               thrpt    5  2476.796 ± 55.819  ops/ms
EishayWriteBinaryArrayMapping.kryo                thrpt    5  1006.016 ±  3.009  ops/ms
EishayWriteBinaryArrayMapping.protobuf            thrpt    5   842.347 ± 11.888  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB          thrpt    5   793.571 ±  8.180  ops/ms
EishayWriteBinaryAutoType.hessian                 thrpt    5   181.195 ± 28.299  ops/ms
EishayWriteBinaryAutoType.javaSerialize           thrpt    5   131.472 ±  3.966  ops/ms
EishayWriteString.fastjson1                       thrpt    5   306.708 ±  8.807  ops/ms
EishayWriteString.fastjson2                       thrpt    5   843.320 ± 24.886  ops/ms
EishayWriteString.gson                            thrpt    5   205.008 ±  2.005  ops/ms
EishayWriteString.jackson                         thrpt    5   426.623 ± 17.162  ops/ms
EishayWriteStringTree.fastjson1                   thrpt    5   364.825 ±  8.687  ops/ms
EishayWriteStringTree.fastjson2                   thrpt    5   616.612 ± 13.584  ops/ms
EishayWriteStringTree.gson                        thrpt    5   219.445 ±  1.479  ops/ms
EishayWriteStringTree.jackson                     thrpt    5   442.558 ± 25.039  ops/ms
EishayWriteUTF8Bytes.fastjson1                    thrpt    5   290.285 ±  0.429  ops/ms
EishayWriteUTF8Bytes.fastjson2                    thrpt    5   915.255 ± 20.548  ops/ms
EishayWriteUTF8Bytes.gson                         thrpt    5   193.350 ±  0.874  ops/ms
EishayWriteUTF8Bytes.jackson                      thrpt    5   392.783 ±  1.052  ops/ms
```
# OrangePI5-jdk-17.0.7
```java
Benchmark                                          Mode  Cnt     Score     Error   Units
EishayParseBinary.fastjson2UTF8Bytes              thrpt    5   694.250 ±  41.043  ops/ms
EishayParseBinary.hessian                         thrpt    5   156.651 ±   8.241  ops/ms
EishayParseBinary.javaSerialize                   thrpt    5    29.553 ±   1.191  ops/ms
EishayParseBinary.jsonb                           thrpt    5  1379.213 ±  47.771  ops/ms
EishayParseBinaryArrayMapping.fastjson2UTF8Bytes  thrpt    5  1013.537 ±  46.870  ops/ms
EishayParseBinaryArrayMapping.jsonb               thrpt    5  1971.077 ±  80.893  ops/ms
EishayParseBinaryArrayMapping.kryo                thrpt    5   995.204 ±  27.609  ops/ms
EishayParseBinaryArrayMapping.protobuf            thrpt    5   846.844 ±  33.816  ops/ms
EishayParseBinaryAutoType.fastjson2JSONB          thrpt    5   843.149 ±  31.143  ops/ms
EishayParseBinaryAutoType.hessian                 thrpt    5   153.501 ±   5.234  ops/ms
EishayParseBinaryAutoType.javaSerialize           thrpt    5    29.153 ±   0.527  ops/ms
EishayParseString.fastjson1                       thrpt    5   802.211 ±  56.110  ops/ms
EishayParseString.fastjson2                       thrpt    5   692.677 ±  37.104  ops/ms
EishayParseString.gson                            thrpt    5   224.178 ±   6.353  ops/ms
EishayParseString.jackson                         thrpt    5   270.996 ±  11.035  ops/ms
EishayParseStringPretty.fastjson1                 thrpt    5   185.311 ±   4.916  ops/ms
EishayParseStringPretty.fastjson2                 thrpt    5   533.042 ±  23.216  ops/ms
EishayParseStringPretty.gson                      thrpt    5   208.133 ±  12.269  ops/ms
EishayParseStringPretty.jackson                   thrpt    5   250.140 ±   5.222  ops/ms
EishayParseTreeString.fastjson1                   thrpt    5   286.487 ±   7.105  ops/ms
EishayParseTreeString.fastjson2                   thrpt    5   485.692 ±   5.416  ops/ms
EishayParseTreeString.gson                        thrpt    5   178.733 ±   6.231  ops/ms
EishayParseTreeString.jackson                     thrpt    5   260.127 ±  10.984  ops/ms
EishayParseTreeStringPretty.fastjson1             thrpt    5   238.139 ±  10.463  ops/ms
EishayParseTreeStringPretty.fastjson2             thrpt    5   400.598 ±  10.072  ops/ms
EishayParseTreeStringPretty.gson                  thrpt    5   170.850 ±   8.424  ops/ms
EishayParseTreeStringPretty.jackson               thrpt    5   246.587 ±   5.470  ops/ms
EishayParseTreeUTF8Bytes.fastjson1                thrpt    5   244.426 ±   2.531  ops/ms
EishayParseTreeUTF8Bytes.fastjson2                thrpt    5   484.403 ±  23.456  ops/ms
EishayParseTreeUTF8Bytes.gson                     thrpt    5   170.812 ±   5.955  ops/ms
EishayParseTreeUTF8Bytes.jackson                  thrpt    5   310.724 ±   6.089  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson1          thrpt    5   204.633 ±  13.706  ops/ms
EishayParseTreeUTF8BytesPretty.fastjson2          thrpt    5   408.573 ±  26.860  ops/ms
EishayParseTreeUTF8BytesPretty.gson               thrpt    5   159.671 ±   8.971  ops/ms
EishayParseTreeUTF8BytesPretty.jackson            thrpt    5   288.804 ±   2.363  ops/ms
EishayParseUTF8Bytes.fastjson1                    thrpt    5   579.496 ±  29.612  ops/ms
EishayParseUTF8Bytes.fastjson2                    thrpt    5   689.944 ±  29.441  ops/ms
EishayParseUTF8Bytes.gson                         thrpt    5   168.129 ±   8.034  ops/ms
EishayParseUTF8Bytes.jackson                      thrpt    5   297.345 ±   7.471  ops/ms
EishayParseUTF8BytesPretty.fastjson1              thrpt    5   163.024 ±   6.431  ops/ms
EishayParseUTF8BytesPretty.fastjson2              thrpt    5   536.777 ±  28.643  ops/ms
EishayParseUTF8BytesPretty.gson                   thrpt    5   156.803 ±   3.801  ops/ms
EishayParseUTF8BytesPretty.jackson                thrpt    5   270.777 ±  10.788  ops/ms
EishayWriteBinary.fastjson2UTF8Bytes              thrpt    5   955.167 ±  39.585  ops/ms
EishayWriteBinary.hessian                         thrpt    5   211.040 ±  44.234  ops/ms
EishayWriteBinary.javaSerialize                   thrpt    5   129.421 ±   1.422  ops/ms
EishayWriteBinary.jsonb                           thrpt    5  1513.381 ±  82.144  ops/ms
EishayWriteBinaryArrayMapping.fastjson2UTF8Bytes  thrpt    5  1098.409 ±  68.144  ops/ms
EishayWriteBinaryArrayMapping.jsonb               thrpt    5  2569.268 ± 150.260  ops/ms
EishayWriteBinaryArrayMapping.kryo                thrpt    5  1036.738 ±   2.983  ops/ms
EishayWriteBinaryArrayMapping.protobuf            thrpt    5   921.774 ±  12.076  ops/ms
EishayWriteBinaryAutoType.fastjson2JSONB          thrpt    5   823.651 ±  45.635  ops/ms
EishayWriteBinaryAutoType.hessian                 thrpt    5   197.848 ±  26.093  ops/ms
EishayWriteBinaryAutoType.javaSerialize           thrpt    5   133.822 ±   6.417  ops/ms
EishayWriteString.fastjson1                       thrpt    5   361.511 ±   7.627  ops/ms
EishayWriteString.fastjson2                       thrpt    5   853.890 ±   2.169  ops/ms
EishayWriteString.gson                            thrpt    5   166.541 ±   5.902  ops/ms
EishayWriteString.jackson                         thrpt    5   430.296 ±   8.834  ops/ms
EishayWriteStringTree.fastjson1                   thrpt    5   365.345 ±  14.261  ops/ms
EishayWriteStringTree.fastjson2                   thrpt    5   609.755 ±  15.970  ops/ms
EishayWriteStringTree.gson                        thrpt    5   167.974 ±   7.495  ops/ms
EishayWriteStringTree.jackson                     thrpt    5   411.267 ±  13.966  ops/ms
EishayWriteUTF8Bytes.fastjson1                    thrpt    5   318.996 ±  15.857  ops/ms
EishayWriteUTF8Bytes.fastjson2                    thrpt    5   942.339 ±  23.569  ops/ms
EishayWriteUTF8Bytes.gson                         thrpt    5   161.263 ±   6.722  ops/ms
EishayWriteUTF8Bytes.jackson                      thrpt    5   435.127 ±  22.588  ops/ms
```
