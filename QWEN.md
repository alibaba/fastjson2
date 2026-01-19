# Qwen Code Context for fastjson2

## Project Overview

This directory contains the source code for **fastjson2**, a high-performance Java library for processing JSON. It is the next generation of the popular `fastjson` library (v1), offering significant performance improvements and enhanced features while addressing security concerns present in v1.

Fastjson2 is designed for applications that require high-throughput JSON processing, making it ideal for microservices, web APIs, and data-intensive applications. The library provides a simple API for JSON serialization and deserialization while maintaining excellent performance characteristics.

Key features of fastjson2 include:
- **High Performance:** Outperforms other popular JSON libraries like Jackson and Gson in most common use cases.
- **Modern JDK Support:** Compatible with JDK 8 and newer versions, including optimizations leveraging newer JVM features.
- **JSONPath Support:** Advanced querying capabilities using SQL:2016 compliant JSONPath syntax for complex data extraction.
- **Cross-Platform Compatibility:** Supports Android 8+ and other constrained environments.
- **Kotlin Integration:** Dedicated module providing idiomatic Kotlin support with coroutines and extension functions.
- **JSON Schema Validation:** Built-in support for validating JSON against schemas for data integrity.
- **JSONB Format:** Support for a binary JSON format enabling even faster serialization/deserialization.
- **Framework Extensions:** Seamless integration modules for Spring Framework (5 & 6), Solon, and JAX-RS.

The project is structured as a multi-module Maven project to allow selective inclusion of only the required components.

## Key Files and Directories

### Root Directory Files
- `pom.xml`: The root Maven Project Object Model (POM) file defining the parent project and managing dependencies and build configuration for all submodules.
- `README.md`: The main project documentation in Chinese, detailing features, usage, and modules.
- `README_EN.md`: The English version of the main project documentation.
- `CONTRIBUTING.md`: Guidelines for contributing to the project.
- `CODE_OF_CONDUCT.md`: Code of conduct for contributors and maintainers.
- `SECURITY.md`: Security policy and reporting guidelines.
- `.editorconfig`: Defines coding style standards for different editors and IDEs.
- `.gitattributes`: Git attributes configuration for consistent line endings and file handling.
- `.gitignore`: Specifies files and directories to be ignored by Git.
- `mvnw` / `mvnw.cmd`: Maven wrapper scripts for building the project without requiring Maven installation.

### Core Modules
- `core/`: The main `fastjson2` library module containing the core JSON parsing and serialization functionality.
- `kotlin/`: Module providing Kotlin-specific extensions and idiomatic support for Kotlin developers.
- `fastjson1-compatible/`: Module offering backward compatibility with fastjson v1.x for easier migration.

### Extension Modules
- `extension/`: Core extensions for specialized data formats and frameworks (Arrow, ClickHouse, etc.).
- `extension-spring5/`: Integration module for Spring Framework 5.
- `extension-spring6/`: Integration module for Spring Framework 6.
- `extension-solon/`: Integration module for Solon framework.
- `extension-jaxrs/`: Integration module for JAX-RS standard.

### Documentation and Examples
- `docs/`: Additional documentation files.
- `example-*`: Example projects demonstrating usage with different frameworks and scenarios.

### Tools and Testing
- `benchmark/`: Contains JMH benchmarks for performance testing and comparison.
- `android-test/`: Android-specific tests to ensure compatibility.

## Environment Requirements

The project requires compatibility with multiple JDK versions. Ensure the following environment variables are set for different JDK versions:

- `JAVA8_HOME` - For JDK 8 compatibility
- `JAVA11_HOME` - For JDK 11 compatibility
- `JAVA17_HOME` - For JDK 17 compatibility
- `JAVA21_HOME` - For JDK 21 compatibility
- `JAVA25_HOME` - For JDK 25 compatibility

## Building and Running

### Code Quality

All modifications must comply with the coding standards. To save build time, run the following command after code changes to perform code formatting and style validation:

```bash
mvn validate
```

### Build Instructions

The project supports building with different JDK versions and different object creation strategies. Use the appropriate environment variable to select the JDK version:

```bash
# Example for using JDK 11
export JAVA_HOME=$JAVA11_HOME
```

Then execute the following commands to build:

```bash
# Default build - uses asm dynamic bytecode generation for ObjectReader/ObjectWriter
mvn clean package

# Alternative build - validates behavior without asm dynamic bytecode generation
# Ensures code can be ported to environments that don't support dynamic bytecode,
# such as Android, GraalVM native-image, etc.
mvn clean package -Dfastjson2.creator=reflect
```

### Testing

To run the complete test suite:

```bash
mvn test
```

To run specific tests:

```bash
# Run tests matching a pattern
mvn test -Dtest="*TestClassName*"
```

### Performance Testing

To run the benchmarks:

```bash
cd benchmark
mvn clean package
java -jar target/benchmark.jar
```
