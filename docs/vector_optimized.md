JDK 17中提供了[vector api](https://openjdk.org/jeps/426)，可以用SIMD来优化性能。

fastjson 2.0.56版本开始全面使用SWAR(SIMD within a register)来做SIMD优化，不再需要使用vector api.
