# FASTJSON2 Project Context

## Project Overview

FASTJSON2 is a high-performance Java JSON library. It is the next generation of the popular FASTJSON library, designed for speed and efficiency. Key features include:

- **High Performance:** Significantly faster than other popular JSON libraries like Jackson, Gson, and org.json.
- **Java Compatibility:** Supports JDK 8+ and newer features like Records and GraalVM Native Image.
- **Cross-Platform:** Works on both server and Android (8+) environments with a unified API.
- **Kotlin Support:** Provides idiomatic Kotlin extensions.
- **JSON Schema:** Includes support for JSON Schema validation.
- **Binary Format:** Introduces JSONB, a binary format for even faster serialization/deserialization.
- **Advanced JSONPath:** Supports SQL:2016 compliant JSONPath syntax.

The main Maven artifact is `com.alibaba.fastjson2:fastjson2`. The project is structured as a multi-module Maven build.

## Key Technologies and Architecture

- **Language:** Java (JDK 8+)
- **Build System:** Apache Maven
- **Core Concepts:**
  - `JSON`: Main entry point for parsing and serialization.
  - `JSONObject`: Represents a JSON object.
  - `JSONArray`: Represents a JSON array.
  - `JSONB`: Handles the binary JSONB format.
  - `JSONReader`: Low-level parser for JSON text and JSONB.
  - `JSONWriter`: Low-level generator for JSON text and JSONB.
  - `ObjectReader`: Handles deserialization of Java objects.
  - `ObjectWriter`: Handles serialization of Java objects.
  - `JSONPath`: For querying JSON data.
  - `JSONSchema`: For validating JSON data against a schema.

## Building and Running

### Prerequisites

- JDK 8 or higher
- Apache Maven 3.3.9 or higher

### Build Commands

- **Clean and Compile:**
  ```bash
  mvn clean compile
  ```
- **Run Tests:**
  ```bash
  mvn test
  ```
- **Build JAR (including sources and Javadoc for release):**
  ```bash
  mvn clean package
  # For a full release build:
  mvn clean deploy -DperformRelease=true
  ```
- **Install to Local Repository:**
  ```bash
  mvn clean install
  ```

The project uses Maven profiles for different build scenarios (e.g., generating sources, Javadoc, signing artifacts, code coverage).

## Development Conventions

- **Code Style:** The project uses Checkstyle for code formatting and style checks (configuration in `src/checkstyle/fastjson2-checks.xml`). The Maven `validate` phase enforces these rules.
- **Code Validation:** Any modified code must comply with the rules defined in `src/checkstyle/fastjson2-checks.xml`. Use `mvn validate` to verify that your changes pass the code style checks.
- **Testing:** JUnit 5 and Kotest are used for testing. Tests are located alongside the code in `src/test` directories within each module.
- **Modular Structure:**
  - `core`: The main fastjson2 library.
  - `extension-*`: Integrations with frameworks like Spring, Solon, JAX-RS.
  - `fastjson1-compatible`: Compatibility module for migrating from fastjson 1.x.
  - `kotlin`: Kotlin-specific extensions.
  - `benchmark`: Performance benchmarks.
  - `example-*`: Example projects demonstrating usage.
  - Test modules (`safemode-test`, `test-jdk17`, etc.) for specific scenarios.
- **Dependencies:** Dependencies are managed centrally in the parent `pom.xml`. Test dependencies are scoped appropriately.
- **Contributions:** Pull requests are welcome. Smaller, incremental changes are preferred. Issues should be reported with version information and steps to reproduce. See `CONTRIBUTING.md` for more details.