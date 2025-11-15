JDK 17 introduced the [vector api](https://openjdk.org/jeps/426), which can use SIMD to optimize performance.

Starting from version 2.0.60, fastjson2 comprehensively uses SWAR (SIMD within a register) for SIMD optimization and no longer needs to use the vector api.
