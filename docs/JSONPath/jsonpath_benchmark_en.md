fastjson2 treats [JSONPath](https://alibaba.github.io/fastjson2/JSONPath/jsonpath_en) as a first-class citizen and has made many optimizations for it.

# Conclusion
In the test scenario below, fastjson2's performance is 4.5 times that of jayway.

* Test code: [JSONPathPerf.java](https://github.com/alibaba/fastjson2/blob/main/benchmark/src/main/java/com/alibaba/fastjson2/benchmark/jsonpath/JSONPathPerf.java)

# Test Data
```
Benchmark                            Mode  Cnt     Score    Error   Units
JSONPathPerf.fastjsonReaderAuthors  thrpt    5  1260.479 ± 46.356  ops/ms
JSONPathPerf.jaywayReadAuthors      thrpt    5   276.301 ±  2.447  ops/ms
```
