fastjson2将 [JSONPath](https://alibaba.github.io/fastjson2/jsonpath_cn) 作为一等公民，针对jsonpath做了很多优化。

# 结论
在如下测试场景中，fastjson2的性能是jayway的4.5倍

* 测试代码：[JSONPathPerf.java](https://github.com/alibaba/fastjson2/blob/main/benchmark/src/main/java/com/alibaba/fastjson2/benchmark/jsonpath/JSONPathPerf.java)

# 测试数据
```
Benchmark                            Mode  Cnt     Score    Error   Units
JSONPathPerf.fastjsonReaderAuthors  thrpt    5  1260.479 ± 46.356  ops/ms
JSONPathPerf.jaywayReadAuthors      thrpt    5   276.301 ±  2.447  ops/ms
```
