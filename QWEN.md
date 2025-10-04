# Qwen Code Context for fastjson2

## Project Overview

This directory contains the source code for **fastjson2**, a high-performance Java library for processing JSON. It is the next generation of the popular `fastjson` library (v1), offering significant performance improvements and enhanced features while addressing security concerns present in v1.

Key features of fastjson2 include:
- **High Performance:** Outperforms other popular JSON libraries like Jackson and Gson.
- **Modern JDK Support:** Compatible with JDK 8 and newer versions, including optimizations for newer features.
- **JSONPath Support:** Advanced querying capabilities using SQL:2016 compliant JSONPath syntax.
- **Cross-Platform:** Supports Android 8+.
- **Kotlin Integration:** Dedicated module for seamless use with Kotlin.
- **JSON Schema:** Support for validating JSON against schemas.
- **JSONB Format:** Support for a binary JSON format for even faster processing.
- **Framework Extensions:** Modules for integration with Spring Framework, Solon, and JAX-RS.

The project is structured as a multi-module Maven project.

## Key Files and Directories

- `pom.xml`: The root Maven Project Object Model (POM) file defining the parent project and managing dependencies and build configuration for all submodules.
- `README.md`: The main project documentation in Chinese, detailing features, usage, and modules.
- `README_EN.md`: The English version of the main project documentation.
- `CONTRIBUTING.md`: Guidelines for contributing to the project.
- `core/`: The main `fastjson2` library module.
- `kotlin/`: Module providing Kotlin-specific extensions and support.
- `extension/`: Core extensions for features like Arrow, ClickHouse, etc.
- `extension-spring5/`, `extension-spring6/`, `extension-solon/`, `extension-jaxrs/`: Modules for integrating fastjson2 with various web frameworks.
- `fastjson1-compatible/`: Module for compatibility with fastjson v1.x.
- `benchmark/`: Contains JMH benchmarks for performance testing.
- `example-*`: Example projects demonstrating usage with different frameworks.
- `docs/`: Additional documentation files.

## Building and Running

This is a Java/Maven project. The primary build tool is Maven.

### Prerequisites

- Java JDK 8 or higher.
- Apache Maven 3.3.9 or higher.

### Standard Maven Commands

1.  **Clean:** Remove build artifacts.
    ```bash
    mvn clean
    ```

2.  **Compile:** Compile the source code.
    ```bash
    mvn compile
    ```

3.  **Test:** Run unit and integration tests.
    ```bash
    mvn test
    ```

4.  **Package:** Package the compiled code into JAR files.
    ```bash
    mvn package
    ```

5.  **Install:** Install the package into the local Maven repository.
    ```bash
    mvn install
    ```

6.  **Validate:** Check the project for correctness and adherence to coding standards (Checkstyle, Modernizer).
    ```bash
    mvn validate
    ```

7.  **Build Everything:** Clean, compile, test, and package.
    ```bash
    mvn clean package
    ```

### Running Benchmarks

To run the JMH benchmarks:
1.  Navigate to the `benchmark` directory or run from the root:
    ```bash
    cd benchmark
    mvn clean package
    java -jar target/fastjson2-benchmarks.jar
    ```
    Or from the root:
    ```bash
    mvn clean package -pl benchmark -am
    java -jar benchmark/target/fastjson2-benchmarks.jar
    ```

## Development Conventions

- **Language:** Java (Primary), Kotlin (for `kotlin` module).
- **Build Tool:** Apache Maven.
- **Testing:** Unit and integration tests are written using JUnit 5 and Kotest. Test dependencies are managed in the root `pom.xml`.
- **Code Style:** The project uses Checkstyle for code formatting and style checks. The configuration is located in `src/checkstyle/fastjson2-checks.xml`. The `validate` phase enforces these rules.
- **Modernizer:** The build process includes the Modernizer Maven plugin to detect uses of legacy Java APIs.
- **Contributing:** Follow the guidelines in `CONTRIBUTING.md`. Ensure `mvn validate` and `mvn test` pass before submitting pull requests. Pull requests should be small and focused.