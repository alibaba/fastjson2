## 1. [EishayForyCompatibleParse](https://github.com/alibaba/fastjson2/blob/main/benchmark/src/main/java/com/alibaba/fastjson2/benchmark/eishay/EishayForyCompatibleParse.java)
这个场景是JSONB格式和Fory CompatibleMode反序列化性能比较。基于KeyValue的映射，对增加和删除字段的序列化结构都能有很好的兼容性。

| aliyun ecs spec | jdk version 	|	jsonb	|	fory |
|-----|-----|----------|-----|
| aliyun_ecs.c9a.large | jdk1.8.0_361	|	8602.723	|	8945.048 (103.98%) |
|  | jdk-11.0.20	|	10766.217	|	10156.689 (94.34%) |
|  | jdk-17.0.15	|	12043.344	|	12930.031 (107.36%) |
|  | jdk-21.0.7	|	11841.859	|	12877.465 (108.75%) |
|  | jdk-25	|	12542.093	|	13531.771 (107.89%) |
| [orangepi5p](http://www.orangepi.org/html/hardWare/computerAndMicrocontrollers/details/Orange-Pi-5-Pro.html) | jdk1.8.0_371	|	16501.904	|	15414.802 (93.41%) |
|  | jdk-11.0.19	|	2479.802	|	16595.226 (669.22%) |
|  | jdk-17.0.7	|	18773.969	|	18903.839 (100.69%) |
|  | jdk-21	|	18630.654	|	18086.891 (97.08%) |
|  | jdk-25	|	18951.647	|	11160.918 (58.89%) |

## 2. [EishayForyCompatibleWrite](https://github.com/alibaba/fastjson2/blob/main/benchmark/src/main/java/com/alibaba/fastjson2/benchmark/eishay/EishayForyCompatibleWrite.java)
这个场景是JSONB格式和Fory CompatibleMode序列化性能比较。基于KeyValue的映射，对增加和删除字段的序列化结构都能有很好的兼容性。

| aliyun ecs spec | jdk version 	|	jsonb	|	fory |
|-----|-----|----------|-----|
| aliyun_ecs.c9a.large | jdk1.8.0_361	|	11342.236	|	1190.421 (10.5%) |
|  | jdk-11.0.20	|	13542.476	|	1141.13 (8.43%) |
|  | jdk-17.0.15	|	16072.948	|	1087.016 (6.76%) |
|  | jdk-21.0.7	|	16781.657	|	1280.054 (7.63%) |
|  | jdk-25	|	16630.309	|	1170.471 (7.04%) |
| [orangepi5p](http://www.orangepi.org/html/hardWare/computerAndMicrocontrollers/details/Orange-Pi-5-Pro.html) | jdk1.8.0_371	|	18238.067	|	1527.07 (8.37%) |
|  | jdk-11.0.19	|	18656.239	|	1483.161 (7.95%) |
|  | jdk-17.0.7	|	22201.663	|	1520.018 (6.85%) |
|  | jdk-21	|	22098.839	|	1623.675 (7.35%) |
|  | jdk-25	|	20079.78	|	1935.564 (9.64%) |

## 3. [EishayParseBinary](https://github.com/alibaba/fastjson2/blob/main/benchmark/src/main/java/com/alibaba/fastjson2/benchmark/eishay/EishayParseBinary.java)
这个场景是二进制反序列化比较，JSONB格式、JSON UTF8编码(fastjson2UTF8Bytes)、hessian、javaSerialize的比较，用于[Apache dubbo](https://github.com/apache/dubbo)的用户选择二进制协议比较

| aliyun ecs spec | jdk version 	|	jsonb	|	fastjson2UTF8Bytes	|	hessian	|	javaSerialize |
|-----|-----|----------|----------|----------|-----|
| aliyun_ecs.c9a.large | jdk1.8.0_361	|	11188.33	|	5775.652 (51.62%)	|	1130.09 (10.1%)	|	188.648 (1.69%) |
|  | jdk-11.0.20	|	13238.103	|	6932.788 (52.37%)	|	1011.393 (7.64%)	|	193.846 (1.46%) |
|  | jdk-17.0.15	|	14462.824	|	8717.904 (60.28%)	|	1062.382 (7.35%)	|	219.888 (1.52%) |
|  | jdk-21.0.7	|	15131.084	|	8514.757 (56.27%)	|	1087.512 (7.19%)	|	203.715 (1.35%) |
|  | jdk-25	|	14839.795	|	8645.493 (58.26%)	|	1217.656 (8.21%)	|	228.462 (1.54%) |
| [orangepi5p](http://www.orangepi.org/html/hardWare/computerAndMicrocontrollers/details/Orange-Pi-5-Pro.html) | jdk1.8.0_371	|	15883.757	|	10782.779 (67.89%)	|	309.389 (1.95%)	|	328.317 (2.07%) |
|  | jdk-11.0.19	|	17351.442	|	11396.906 (65.68%)	|	316.251 (1.82%)	|	314.742 (1.81%) |
|  | jdk-17.0.7	|	17592.03	|	12385.74 (70.41%)	|	311.096 (1.77%)	|	327.528 (1.86%) |
|  | jdk-21	|	17552.183	|	13345.888 (76.04%)	|	352.142 (2.01%)	|	320.996 (1.83%) |
|  | jdk-25	|	17461.357	|	13816.917 (79.13%)	|	606.877 (3.48%)	|	343.131 (1.97%) |

## 4. [EishayParseBinaryArrayMapping](https://github.com/alibaba/fastjson2/blob/main/benchmark/src/main/java/com/alibaba/fastjson2/benchmark/eishay/EishayParseBinaryArrayMapping.java)
这个场景是二进制反序列化比较，JSONB格式（基于字段顺序映射）、kryo、protobuf的比较

| aliyun ecs spec | jdk version 	|	jsonb	|	kryo	|	protobuf |
|-----|-----|----------|----------|-----|
| aliyun_ecs.c9a.large | jdk1.8.0_361	|	13194.803	|	6813.671 (51.64%)	|	5172.481 (39.2%) |
|  | jdk-11.0.20	|	18379.756	|	6637.734 (36.11%)	|	5614.485 (30.55%) |
|  | jdk-17.0.15	|	20240.549	|	6262.305 (30.94%)	|	7656.42 (37.83%) |
|  | jdk-21.0.7	|	21006.163	|	6746.586 (32.12%)	|	7965.855 (37.92%) |
|  | jdk-25	|	22735.87	|	6518.09 (28.67%)	|	8631.036 (37.96%) |
| [orangepi5p](http://www.orangepi.org/html/hardWare/computerAndMicrocontrollers/details/Orange-Pi-5-Pro.html) | jdk1.8.0_371	|	15618.568	|	10518.81 (67.35%)	|	6555.65 (41.97%) |
|  | jdk-11.0.19	|	16986.179	|	10922.769 (64.3%)	|	6420.458 (37.8%) |
|  | jdk-17.0.7	|	14051.398	|	11520.78 (81.99%)	|	8622.81 (61.37%) |
|  | jdk-21	|	17191.666	|	10911.143 (63.47%)	|	11386.241 (66.23%) |
|  | jdk-25	|	17410.686	|	11609.556 (66.68%)	|	7862.675 (45.16%) |

## 5. [EishayParseBinaryAutoType](https://github.com/alibaba/fastjson2/blob/main/benchmark/src/main/java/com/alibaba/fastjson2/benchmark/eishay/EishayParseBinaryAutoType.java)
这个场景是带类型信息二进制反序列化比较，JSONB格式、JSON UTF8编码(fastjson2UTF8Bytes)、hessian、javaSerialize的比较，用于[Apache dubbo](https://github.com/apache/dubbo)的用户选择二进制协议比较

| aliyun ecs spec | jdk version 	|	fastjson2JSONB	|	hessian	|	javaSerialize |
|-----|-----|----------|----------|-----|
| aliyun_ecs.c9a.large | jdk1.8.0_361	|	5930.505	|	1114.352 (18.79%)	|	192.595 (3.25%) |
|  | jdk-11.0.20	|	6811.854	|	1045.428 (15.35%)	|	204.22 (3%) |
|  | jdk-17.0.15	|	6931.626	|	1074.302 (15.5%)	|	221.16 (3.19%) |
|  | jdk-21.0.7	|	6966.208	|	1080.145 (15.51%)	|	202.866 (2.91%) |
|  | jdk-25	|	7090.367	|	1205.429 (17%)	|	230.91 (3.26%) |
| [orangepi5p](http://www.orangepi.org/html/hardWare/computerAndMicrocontrollers/details/Orange-Pi-5-Pro.html) | jdk1.8.0_371	|	9449.041	|	292.83 (3.1%)	|	322.487 (3.41%) |
|  | jdk-11.0.19	|	10535.428	|	306.159 (2.91%)	|	317.23 (3.01%) |
|  | jdk-17.0.7	|	10831.787	|	314.293 (2.9%)	|	329.425 (3.04%) |
|  | jdk-21	|	10320.959	|	342.19 (3.32%)	|	322.009 (3.12%) |
|  | jdk-25	|	10751.437	|	577.288 (5.37%)	|	345.123 (3.21%) |

## 6. [EishayParseString](https://github.com/alibaba/fastjson2/blob/main/benchmark/src/main/java/com/alibaba/fastjson2/benchmark/eishay/EishayParseString.java)
这个场景是将没有格式化的JSON字符串反序列化为JavaBean对象，是最常用的场景，这个是fastjson1的强项。

| aliyun ecs spec | jdk version 	|	fastjson2	|	fastjson1	|	jackson	|	gson |
|-----|-----|----------|----------|----------|-----|
| aliyun_ecs.c9a.large | jdk1.8.0_361	|	6064.201	|	4712.568 (77.71%)	|	2094.029 (34.53%)	|	1823.249 (30.07%) |
|  | jdk-11.0.20	|	6770.551	|	3958.295 (58.46%)	|	1890.831 (27.93%)	|	1893.702 (27.97%) |
|  | jdk-17.0.15	|	8649.58	|	5625.006 (65.03%)	|	2027.598 (23.44%)	|	1937.096 (22.4%) |
|  | jdk-21.0.7	|	8716.54	|	5343.45 (61.3%)	|	1902.672 (21.83%)	|	1802.329 (20.68%) |
|  | jdk-25	|	8791.011	|	4415.924 (50.23%)	|	1934.222 (22%)	|	1740.02 (19.79%) |
| [orangepi5p](http://www.orangepi.org/html/hardWare/computerAndMicrocontrollers/details/Orange-Pi-5-Pro.html) | jdk-11.0.19	|	11987.054	|	6732.104 (56.16%)	|	2840.785 (23.7%)	|	3040.798 (25.37%) |
|  | jdk-17.0.7	|	13283.286	|	10086.671 (75.94%)	|	3104.356 (23.37%)	|	3077.203 (23.17%) |
|  | jdk-21	|	13898.838	|	9807.137 (70.56%)	|	2751.261 (19.79%)	|	2685.389 (19.32%) |
|  | jdk-25	|	14270.577	|	8105.345 (56.8%)	|	2826.597 (19.81%)	|	2540.681 (17.8%) |

## 7. [EishayParseStringPretty](https://github.com/alibaba/fastjson2/blob/main/benchmark/src/main/java/com/alibaba/fastjson2/benchmark/eishay/EishayParseStringPretty.java)
这个场景是将格式化过的JSON字符串反序列化为JavaBean对象

| aliyun ecs spec | jdk version 	|	fastjson2	|	fastjson1	|	jackson	|	gson |
|-----|-----|----------|----------|----------|-----|
| aliyun_ecs.c9a.large | jdk1.8.0_361	|	4848.32	|	1190.889 (24.56%)	|	1920.426 (39.61%)	|	1675.321 (34.55%) |
|  | jdk-11.0.20	|	4872.46	|	1025.124 (21.04%)	|	1759.341 (36.11%)	|	1684.102 (34.56%) |
|  | jdk-17.0.15	|	6249.328	|	1237.751 (19.81%)	|	1810.512 (28.97%)	|	1762.02 (28.2%) |
|  | jdk-21.0.7	|	6281.816	|	1192.305 (18.98%)	|	1725.034 (27.46%)	|	1645.427 (26.19%) |
|  | jdk-25	|	6223.434	|	1096.424 (17.62%)	|	1757.289 (28.24%)	|	1673.134 (26.88%) |
| [orangepi5p](http://www.orangepi.org/html/hardWare/computerAndMicrocontrollers/details/Orange-Pi-5-Pro.html) | jdk-11.0.19	|	2214.554	|	1941.95 (87.69%)	|	2683.328 (121.17%)	|	2767.164 (124.95%) |
|  | jdk-17.0.7	|	9553.948	|	2243.076 (23.48%)	|	2830.941 (29.63%)	|	2750.17 (28.79%) |
|  | jdk-21	|	9776.201	|	1852.959 (18.95%)	|	2543.349 (26.02%)	|	2450.565 (25.07%) |
|  | jdk-25	|	10382.797	|	1882.278 (18.13%)	|	2546.307 (24.52%)	|	2549.766 (24.56%) |

## 8. [EishayParseTreeString](https://github.com/alibaba/fastjson2/blob/main/benchmark/src/main/java/com/alibaba/fastjson2/benchmark/eishay/EishayParseTreeString.java)
这个场景是将没有格式化的JSON字符串解析为JSONObject或者HashMap，不涉及绑定JavaBean对象。

| aliyun ecs spec | jdk version 	|	fastjson2	|	fastjson1	|	jackson	|	gson |
|-----|-----|----------|----------|----------|-----|
| aliyun_ecs.c9a.large | jdk1.8.0_361	|	3648.77	|	2194.465 (60.14%)	|	2128.988 (58.35%)	|	1546.966 (42.4%) |
|  | jdk-11.0.20	|	3218.03	|	1660.139 (51.59%)	|	1891.398 (58.78%)	|	1514.659 (47.07%) |
|  | jdk-17.0.15	|	4418.982	|	2126.021 (48.11%)	|	2181.013 (49.36%)	|	1609.448 (36.42%) |
|  | jdk-21.0.7	|	4312.968	|	2114.617 (49.03%)	|	2169.48 (50.3%)	|	1581.449 (36.67%) |
|  | jdk-25	|	4606.633	|	1681.165 (36.49%)	|	2187.506 (47.49%)	|	1849.926 (40.16%) |
| [orangepi5p](http://www.orangepi.org/html/hardWare/computerAndMicrocontrollers/details/Orange-Pi-5-Pro.html) | jdk-11.0.19	|	6181.261	|	2918.517 (47.22%)	|	2466.261 (39.9%)	|	2323.479 (37.59%) |
|  | jdk-17.0.7	|	6545.096	|	3389.499 (51.79%)	|	3146.651 (48.08%)	|	2364.358 (36.12%) |
|  | jdk-21	|	6381.548	|	2791.473 (43.74%)	|	2652.231 (41.56%)	|	2190.424 (34.32%) |
|  | jdk-25	|	7125.125	|	2843.863 (39.91%)	|	3119.949 (43.79%)	|	2414.151 (33.88%) |

## 9. [EishayParseTreeStringPretty](https://github.com/alibaba/fastjson2/blob/main/benchmark/src/main/java/com/alibaba/fastjson2/benchmark/eishay/EishayParseTreeStringPretty.java)
这个场景是将格式化过的字符串解析为JSONObject或者HashMap，不涉及绑定JavaBean对象。

| aliyun ecs spec | jdk version 	|	fastjson2	|	fastjson1	|	jackson	|	gson |
|-----|-----|----------|----------|----------|-----|
| aliyun_ecs.c9a.large | jdk1.8.0_361	|	3173.646	|	1896.263 (59.75%)	|	1965.336 (61.93%)	|	1408.326 (44.38%) |
|  | jdk-11.0.20	|	2737.68	|	1387.108 (50.67%)	|	1778.02 (64.95%)	|	1399.959 (51.14%) |
|  | jdk-17.0.15	|	3456.091	|	1852.226 (53.59%)	|	1998.265 (57.82%)	|	1497.602 (43.33%) |
|  | jdk-21.0.7	|	3430.565	|	1854.473 (54.06%)	|	1986.614 (57.91%)	|	1466.012 (42.73%) |
|  | jdk-25	|	3636.591	|	1397.165 (38.42%)	|	2077.725 (57.13%)	|	1732.49 (47.64%) |
| [orangepi5p](http://www.orangepi.org/html/hardWare/computerAndMicrocontrollers/details/Orange-Pi-5-Pro.html) | jdk-11.0.19	|	5350.66	|	2194.466 (41.01%)	|	2398.208 (44.82%)	|	2179.932 (40.74%) |
|  | jdk-17.0.7	|	5515.79	|	3067.821 (55.62%)	|	2586.366 (46.89%)	|	2235.569 (40.53%) |
|  | jdk-21	|	5509.972	|	2553.196 (46.34%)	|	2501.099 (45.39%)	|	2190.263 (39.75%) |
|  | jdk-25	|	6073.72	|	2157.198 (35.52%)	|	2708.649 (44.6%)	|	2344.271 (38.6%) |

## 10. [EishayParseTreeUTF8Bytes](https://github.com/alibaba/fastjson2/blob/main/benchmark/src/main/java/com/alibaba/fastjson2/benchmark/eishay/EishayParseTreeUTF8Bytes.java)
这个场景是将没有格式化的JSON字符串UTF8编码的byte[]数组反序列化解析为JSONObject或者HashMap，不涉及绑定JavaBean对象。

| aliyun ecs spec | jdk version 	|	fastjson2	|	fastjson1	|	jackson	|	gson |
|-----|-----|----------|----------|----------|-----|
| aliyun_ecs.c9a.large | jdk1.8.0_361	|	2950.714	|	1882.926 (63.81%)	|	2541.121 (86.12%)	|	1362.479 (46.17%) |
|  | jdk-11.0.20	|	3759.954	|	1455.824 (38.72%)	|	2029.823 (53.99%)	|	1438.029 (38.25%) |
|  | jdk-17.0.15	|	4362.175	|	1790.931 (41.06%)	|	2388.952 (54.77%)	|	1571.939 (36.04%) |
|  | jdk-21.0.7	|	4464.321	|	1855.954 (41.57%)	|	2262.484 (50.68%)	|	1523.513 (34.13%) |
|  | jdk-25	|	4543.777	|	1562.611 (34.39%)	|	2424.809 (53.37%)	|	1776.684 (39.1%) |
| [orangepi5p](http://www.orangepi.org/html/hardWare/computerAndMicrocontrollers/details/Orange-Pi-5-Pro.html) | jdk1.8.0_371	|	5293.597	|	2428.021 (45.87%)	|	2578.819 (48.72%)	|	1904.232 (35.97%) |
|  | jdk-11.0.19	|	6177.365	|	2520.621 (40.8%)	|	2679.286 (43.37%)	|	2241.809 (36.29%) |
|  | jdk-17.0.7	|	6525.117	|	2966.281 (45.46%)	|	2919.975 (44.75%)	|	2301.858 (35.28%) |
|  | jdk-21	|	6332.568	|	2605.072 (41.14%)	|	3163.155 (49.95%)	|	2278.939 (35.99%) |
|  | jdk-25	|	7038.637	|	2651.804 (37.67%)	|	3066.19 (43.56%)	|	2424.123 (34.44%) |

## 11. [EishayParseTreeUTF8BytesPretty](https://github.com/alibaba/fastjson2/blob/main/benchmark/src/main/java/com/alibaba/fastjson2/benchmark/eishay/EishayParseTreeUTF8BytesPretty.java)
这个场景是将格式化过的字符串UTF8编码的byte[]数组反序列化解析为JSONObject或者HashMap，不涉及绑定JavaBean对象。

| aliyun ecs spec | jdk version 	|	fastjson2	|	fastjson1	|	jackson	|	gson |
|-----|-----|----------|----------|----------|-----|
| aliyun_ecs.c9a.large | jdk1.8.0_361	|	2569.656	|	1589.745 (61.87%)	|	2130.159 (82.9%)	|	1252.214 (48.73%) |
|  | jdk-11.0.20	|	2851.598	|	1239.713 (43.47%)	|	1974.436 (69.24%)	|	1341.147 (47.03%) |
|  | jdk-17.0.15	|	3485.495	|	1542.951 (44.27%)	|	2161.05 (62%)	|	1417.537 (40.67%) |
|  | jdk-21.0.7	|	3404.793	|	1603.87 (47.11%)	|	2152.352 (63.22%)	|	1395.399 (40.98%) |
|  | jdk-25	|	3595.192	|	1283.928 (35.71%)	|	2207.426 (61.4%)	|	1635.803 (45.5%) |
| [orangepi5p](http://www.orangepi.org/html/hardWare/computerAndMicrocontrollers/details/Orange-Pi-5-Pro.html) | jdk1.8.0_371	|	4436.952	|	2154.052 (48.55%)	|	2490.42 (56.13%)	|	1676.218 (37.78%) |
|  | jdk-11.0.19	|	5183.728	|	2012.69 (38.83%)	|	2796.327 (53.94%)	|	2152.661 (41.53%) |
|  | jdk-17.0.7	|	5482.01	|	2578.427 (47.03%)	|	3123.985 (56.99%)	|	2205.02 (40.22%) |
|  | jdk-21	|	5474.766	|	2201.901 (40.22%)	|	3040.952 (55.54%)	|	2094.248 (38.25%) |
|  | jdk-25	|	5941.158	|	2099.139 (35.33%)	|	3375.506 (56.82%)	|	2230.789 (37.55%) |

## 12. [EishayParseUTF8Bytes](https://github.com/alibaba/fastjson2/blob/main/benchmark/src/main/java/com/alibaba/fastjson2/benchmark/eishay/EishayParseUTF8Bytes.java)
这个场景是将没有格式化的JSON字符串UTF8编码的byte[]数组反序列化为JavaBean对象，是最常用的场景，这个是fastjson1的强项。

| aliyun ecs spec | jdk version 	|	fastjson2	|	fastjson1	|	jackson	|	gson |
|-----|-----|----------|----------|----------|-----|
| aliyun_ecs.c9a.large | jdk1.8.0_361	|	5426.063	|	3510.437 (64.7%)	|	2286.841 (42.15%)	|	1586.203 (29.23%) |
|  | jdk-11.0.20	|	6639.533	|	3087.981 (46.51%)	|	2116.383 (31.88%)	|	1758.77 (26.49%) |
|  | jdk-17.0.15	|	8468.489	|	3801.27 (44.89%)	|	2104.001 (24.85%)	|	1859.333 (21.96%) |
|  | jdk-21.0.7	|	8613.516	|	3803.973 (44.16%)	|	2041.481 (23.7%)	|	1686.253 (19.58%) |
|  | jdk-25	|	8804.908	|	3276.812 (37.22%)	|	2081.291 (23.64%)	|	1754.513 (19.93%) |
| [orangepi5p](http://www.orangepi.org/html/hardWare/computerAndMicrocontrollers/details/Orange-Pi-5-Pro.html) | jdk1.8.0_371	|	9150.874	|	6185.23 (67.59%)	|	3383.292 (36.97%)	|	2567.64 (28.06%) |
|  | jdk-11.0.19	|	11997.324	|	5761.652 (48.02%)	|	2980.889 (24.85%)	|	2797.682 (23.32%) |
|  | jdk-17.0.7	|	12763.948	|	7079.507 (55.46%)	|	3115.364 (24.41%)	|	2943.588 (23.06%) |
|  | jdk-21	|	13505.18	|	7238.456 (53.6%)	|	3034.248 (22.47%)	|	2574.388 (19.06%) |
|  | jdk-25	|	14039.733	|	6310.661 (44.95%)	|	3098.474 (22.07%)	|	2633.917 (18.76%) |

## 13. [EishayParseUTF8BytesPretty](https://github.com/alibaba/fastjson2/blob/main/benchmark/src/main/java/com/alibaba/fastjson2/benchmark/eishay/EishayParseUTF8BytesPretty.java)
这个场景是将格式化过的JSON字符串UTF8编码的byte[]数组反序列化为JavaBean对象

| aliyun ecs spec | jdk version 	|	fastjson2	|	fastjson1	|	jackson	|	gson |
|-----|-----|----------|----------|----------|-----|
| aliyun_ecs.c9a.large | jdk1.8.0_361	|	4162.023	|	1055.792 (25.37%)	|	2160.995 (51.92%)	|	1236.532 (29.71%) |
|  | jdk-11.0.20	|	5039.859	|	931.518 (18.48%)	|	1923.032 (38.16%)	|	1364.27 (27.07%) |
|  | jdk-17.0.15	|	6149.776	|	1063.152 (17.29%)	|	1924.825 (31.3%)	|	1461.15 (23.76%) |
|  | jdk-21.0.7	|	6068.195	|	1037.687 (17.1%)	|	1803.954 (29.73%)	|	1413.093 (23.29%) |
|  | jdk-25	|	6263.328	|	969.267 (15.48%)	|	1912.852 (30.54%)	|	1663.83 (26.56%) |
| [orangepi5p](http://www.orangepi.org/html/hardWare/computerAndMicrocontrollers/details/Orange-Pi-5-Pro.html) | jdk1.8.0_371	|	7314.999	|	1806.351 (24.69%)	|	3176.152 (43.42%)	|	1992.18 (27.23%) |
|  | jdk-11.0.19	|	8536.836	|	1769.101 (20.72%)	|	3126.417 (36.62%)	|	2156.179 (25.26%) |
|  | jdk-17.0.7	|	9125.195	|	2037.622 (22.33%)	|	3122.302 (34.22%)	|	2170.286 (23.78%) |
|  | jdk-21	|	9666.796	|	1730.807 (17.9%)	|	2866.917 (29.66%)	|	2088.273 (21.6%) |
|  | jdk-25	|	10134.057	|	1621.359 (16%)	|	2933.538 (28.95%)	|	2234.769 (22.05%) |

## 14. [EishayWriteBinary](https://github.com/alibaba/fastjson2/blob/main/benchmark/src/main/java/com/alibaba/fastjson2/benchmark/eishay/EishayWriteBinary.java)
这个场景是二进制序列化比较，JSONB格式、JSON UTF8编码(fastjson2UTF8Bytes)、hessian、javaSerialize的比较，用于[Apache dubbo](https://github.com/apache/dubbo)的用户选择二进制协议比较

| aliyun ecs spec | jdk version 	|	jsonb	|	msgpack	|	protobuf |
|-----|-----|----------|----------|-----|
| aliyun_ecs.c9a.large | jdk1.8.0_361	|	15853.625	|	2085.55 (13.16%)	|	6031.016 (38.04%) |
|  | jdk-11.0.20	|	17136.683	|	2244.055 (13.1%)	|	5250.021 (30.64%) |
|  | jdk-17.0.15	|	24115.104	|	2494.475 (10.34%)	|	6778.868 (28.11%) |
|  | jdk-21.0.7	|	22363.429	|	2307.225 (10.32%)	|	7320.482 (32.73%) |
|  | jdk-25	|	23138.576	|	2361.726 (10.21%)	|	6173.01 (26.68%) |
| [orangepi5p](http://www.orangepi.org/html/hardWare/computerAndMicrocontrollers/details/Orange-Pi-5-Pro.html) | jdk1.8.0_371	|	20674.039	|	2655.736 (12.85%)	|	7561.471 (36.57%) |
|  | jdk-11.0.19	|	11930.732	|	3277.368 (27.47%)	|	7342.469 (61.54%) |
|  | jdk-17.0.7	|	16094.062	|	3250.11 (20.19%)	|	9977.772 (62%) |
|  | jdk-21	|	23797.277	|	3039.73 (12.77%)	|	9793.232 (41.15%) |
|  | jdk-25	|	25327.565	|	3465.226 (13.68%)	|	8061.284 (31.83%) |

## 15. [EishayWriteBinaryArrayMapping](https://github.com/alibaba/fastjson2/blob/main/benchmark/src/main/java/com/alibaba/fastjson2/benchmark/eishay/EishayWriteBinaryArrayMapping.java)
这个场景是二进制序列化比较，JSONB格式（基于字段顺序映射）、kryo、protobuf的比较

| aliyun ecs spec | jdk version 	|	jsonb	|	kryo	|	protobuf |
|-----|-----|----------|----------|-----|
| aliyun_ecs.c9a.large | jdk1.8.0_361	|	18487.128	|	7324.007 (39.62%)	|	6416.465 (34.71%) |
|  | jdk-11.0.20	|	22377.726	|	7412.846 (33.13%)	|	6156.347 (27.51%) |
|  | jdk-17.0.15	|	24985.014	|	7828.587 (31.33%)	|	6973.69 (27.91%) |
|  | jdk-21.0.7	|	26096.81	|	6912.78 (26.49%)	|	6796.525 (26.04%) |
|  | jdk-25	|	26263.432	|	7008.218 (26.68%)	|	8232.827 (31.35%) |
| [orangepi5p](http://www.orangepi.org/html/hardWare/computerAndMicrocontrollers/details/Orange-Pi-5-Pro.html) | jdk1.8.0_371	|	21627.072	|	4365.175 (20.18%)	|	8033.316 (37.14%) |
|  | jdk-11.0.19	|	21526.55	|	5855.546 (27.2%)	|	9627.295 (44.72%) |
|  | jdk-17.0.7	|	18262.515	|	8223.353 (45.03%)	|	11339.395 (62.09%) |
|  | jdk-21	|	13275.103	|	7926.399 (59.71%)	|	9968.763 (75.09%) |
|  | jdk-25	|	27915.869	|	7641.541 (27.37%)	|	11625.279 (41.64%) |

## 16. [EishayWriteBinaryAutoType](https://github.com/alibaba/fastjson2/blob/main/benchmark/src/main/java/com/alibaba/fastjson2/benchmark/eishay/EishayWriteBinaryAutoType.java)
这个场景是带类型信息二进制序列化比较，JSONB格式、JSON UTF8编码(fastjson2UTF8Bytes)、hessian、javaSerialize的比较，用于[Apache dubbo](https://github.com/apache/dubbo)的用户选择二进制协议比较

| aliyun ecs spec | jdk version 	|	fastjson2JSONB	|	hessian	|	javaSerialize |
|-----|-----|----------|----------|-----|
| aliyun_ecs.c9a.large | jdk1.8.0_361	|	5938.997	|	1415.104 (23.83%)	|	1015.565 (17.1%) |
|  | jdk-11.0.20	|	6196.787	|	1383.138 (22.32%)	|	1027.782 (16.59%) |
|  | jdk-17.0.15	|	7714.324	|	1463.512 (18.97%)	|	1003.18 (13%) |
|  | jdk-21.0.7	|	8386.145	|	1465.062 (17.47%)	|	1051.572 (12.54%) |
|  | jdk-25	|	7941.963	|	1502.792 (18.92%)	|	1374.232 (17.3%) |
| [orangepi5p](http://www.orangepi.org/html/hardWare/computerAndMicrocontrollers/details/Orange-Pi-5-Pro.html) | jdk1.8.0_371	|	9836.302	|	1238.255 (12.59%)	|	1718.424 (17.47%) |
|  | jdk-11.0.19	|	10500.115	|	1071.407 (10.2%)	|	1906.033 (18.15%) |
|  | jdk-17.0.7	|	9689.205	|	1332.256 (13.75%)	|	1627.97 (16.8%) |
|  | jdk-21	|	11300.013	|	1264.18 (11.19%)	|	1786.785 (15.81%) |
|  | jdk-25	|	10362.924	|	1604.318 (15.48%)	|	2212.891 (21.35%) |

## 17. [EishayWriteString](https://github.com/alibaba/fastjson2/blob/main/benchmark/src/main/java/com/alibaba/fastjson2/benchmark/eishay/EishayWriteString.java)
这个场景是将JavaBean对象序列化为字符串

| aliyun ecs spec | jdk version 	|	fastjson2	|	fastjson1	|	jackson	|	gson |
|-----|-----|----------|----------|----------|-----|
| aliyun_ecs.c9a.large | jdk1.8.0_361	|	8269.355	|	2705.998 (32.72%)	|	3635.473 (43.96%)	|	1978.961 (23.93%) |
|  | jdk-11.0.20	|	8535.004	|	2442.975 (28.62%)	|	3342.863 (39.17%)	|	1679.815 (19.68%) |
|  | jdk-17.0.15	|	9405.786	|	2756.89 (29.31%)	|	3774.54 (40.13%)	|	2146.582 (22.82%) |
|  | jdk-21.0.7	|	8734.887	|	2607.946 (29.86%)	|	3470.902 (39.74%)	|	1965.928 (22.51%) |
|  | jdk-25	|	9432.129	|	2932.983 (31.1%)	|	3562.494 (37.77%)	|	1968.923 (20.87%) |
| [orangepi5p](http://www.orangepi.org/html/hardWare/computerAndMicrocontrollers/details/Orange-Pi-5-Pro.html) | jdk1.8.0_371	|	9427.171	|	4082.0 (43.3%)	|	5166.568 (54.81%)	|	2726.413 (28.92%) |
|  | jdk-11.0.19	|	12908.202	|	3819.078 (29.59%)	|	5459.018 (42.29%)	|	2454.24 (19.01%) |
|  | jdk-17.0.7	|	13228.06	|	4187.252 (31.65%)	|	5289.285 (39.99%)	|	3186.688 (24.09%) |
|  | jdk-21	|	14153.484	|	3996.012 (28.23%)	|	5008.952 (35.39%)	|	3065.61 (21.66%) |
|  | jdk-25	|	15693.205	|	4268.354 (27.2%)	|	5335.633 (34%)	|	2849.075 (18.15%) |

## 18. [EishayWriteStringTree](https://github.com/alibaba/fastjson2/blob/main/benchmark/src/main/java/com/alibaba/fastjson2/benchmark/eishay/EishayWriteStringTree.java)
这个场景是将JSONObject或者Map序列化为字符串

| aliyun ecs spec | jdk version 	|	fastjson2	|	fastjson1	|	jackson	|	gson |
|-----|-----|----------|----------|----------|-----|
| aliyun_ecs.c9a.large | jdk1.8.0_361	|	5636.505	|	3302.788 (58.6%)	|	3500.515 (62.1%)	|	2249.916 (39.92%) |
|  | jdk-11.0.20	|	5694.838	|	3111.0 (54.63%)	|	3398.454 (59.68%)	|	1843.114 (32.36%) |
|  | jdk-17.0.15	|	5530.142	|	3190.989 (57.7%)	|	3567.089 (64.5%)	|	2306.987 (41.72%) |
|  | jdk-21.0.7	|	5372.692	|	3228.142 (60.08%)	|	3467.709 (64.54%)	|	2391.345 (44.51%) |
|  | jdk-25	|	5425.473	|	3242.965 (59.77%)	|	3567.312 (65.75%)	|	2257.892 (41.62%) |
| [orangepi5p](http://www.orangepi.org/html/hardWare/computerAndMicrocontrollers/details/Orange-Pi-5-Pro.html) | jdk1.8.0_371	|	6418.353	|	4651.22 (72.47%)	|	5496.537 (85.64%)	|	3028.128 (47.18%) |
|  | jdk-11.0.19	|	7597.201	|	4791.397 (63.07%)	|	5488.834 (72.25%)	|	2664.421 (35.07%) |
|  | jdk-17.0.7	|	7149.025	|	5075.149 (70.99%)	|	5476.156 (76.6%)	|	3277.494 (45.85%) |
|  | jdk-21	|	8108.238	|	4749.52 (58.58%)	|	4677.695 (57.69%)	|	3769.189 (46.49%) |
|  | jdk-25	|	8865.037	|	4869.348 (54.93%)	|	5491.269 (61.94%)	|	3324.762 (37.5%) |

## 19. [EishayWriteUTF8Bytes](https://github.com/alibaba/fastjson2/blob/main/benchmark/src/main/java/com/alibaba/fastjson2/benchmark/eishay/EishayWriteUTF8Bytes.java)
这个场景是将JavaBean对象序列化为UTF8编码的Bytes

| aliyun ecs spec | jdk version 	|	fastjson2	|	fastjson1	|	jackson	|	gson |
|-----|-----|----------|----------|----------|-----|
| aliyun_ecs.c9a.large | jdk1.8.0_361	|	8337.031	|	2375.836 (28.5%)	|	3296.349 (39.54%)	|	1661.83 (19.93%) |
|  | jdk-11.0.20	|	10361.633	|	2285.934 (22.06%)	|	3301.914 (31.87%)	|	1648.352 (15.91%) |
|  | jdk-17.0.15	|	11781.308	|	2457.587 (20.86%)	|	3431.371 (29.13%)	|	2162.502 (18.36%) |
|  | jdk-21.0.7	|	11469.049	|	2299.381 (20.05%)	|	3345.314 (29.17%)	|	1956.991 (17.06%) |
|  | jdk-25	|	11632.149	|	2539.655 (21.83%)	|	3320.273 (28.54%)	|	1986.063 (17.07%) |
| [orangepi5p](http://www.orangepi.org/html/hardWare/computerAndMicrocontrollers/details/Orange-Pi-5-Pro.html) | jdk1.8.0_371	|	11042.402	|	5295.115 (47.95%)	|	5416.994 (49.06%)	|	2463.488 (22.31%) |
|  | jdk-11.0.19	|	14533.127	|	3531.318 (24.3%)	|	4822.588 (33.18%)	|	2466.636 (16.97%) |
|  | jdk-17.0.7	|	13536.042	|	3586.025 (26.49%)	|	5382.397 (39.76%)	|	3065.013 (22.64%) |
|  | jdk-21	|	14467.498	|	3575.991 (24.72%)	|	4878.064 (33.72%)	|	3012.332 (20.82%) |
|  | jdk-25	|	17819.652	|	5072.808 (28.47%)	|	5019.638 (28.17%)	|	2729.978 (15.32%) |

## 20. [EishayWriteUTF8BytesTree](https://github.com/alibaba/fastjson2/blob/main/benchmark/src/main/java/com/alibaba/fastjson2/benchmark/eishay/EishayWriteUTF8BytesTree.java)
这个场景是将JSONObject或者Map序列化为UTF8编码的Bytes

| aliyun ecs spec | jdk version 	|	fastjson2	|	jackson |
|-----|-----|----------|-----|
| aliyun_ecs.c9a.large | jdk1.8.0_361	|	6533.872	|	3749.494 (57.39%) |
|  | jdk-11.0.20	|	5803.815	|	2853.068 (49.16%) |
|  | jdk-17.0.15	|	5834.054	|	3254.392 (55.78%) |
|  | jdk-21.0.7	|	6349.639	|	3402.068 (53.58%) |
|  | jdk-25	|	6148.534	|	2990.174 (48.63%) |
| [orangepi5p](http://www.orangepi.org/html/hardWare/computerAndMicrocontrollers/details/Orange-Pi-5-Pro.html) | jdk1.8.0_371	|	6284.421	|	5769.138 (91.8%) |
|  | jdk-11.0.19	|	8261.192	|	4717.181 (57.1%) |
|  | jdk-17.0.7	|	5969.945	|	5356.705 (89.73%) |
|  | jdk-21	|	10347.36	|	5325.935 (51.47%) |
|  | jdk-25	|	8214.371	|	4359.848 (53.08%) |

