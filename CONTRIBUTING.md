# Contributing to FASTJSON 2

Thank you for your interest in contributing to FASTJSON 2! This guide will help you get started.

## Table of Contents

- [Ways to Contribute](#ways-to-contribute)
- [Development Environment](#development-environment)
- [Building the Project](#building-the-project)
- [Running Tests](#running-tests)
- [Code Style](#code-style)
- [Creating a Pull Request](#creating-a-pull-request)
- [Reporting Issues](#reporting-issues)
- [Project Structure](#project-structure)

## Ways to Contribute

- **Bug Reports** - Found a bug? [Open an issue](https://github.com/alibaba/fastjson2/issues) with steps to reproduce.
- **Bug Fixes** - Submit a PR with a fix and a test that validates it.
- **Feature Requests** - Propose new features via issues for discussion before implementation.
- **Documentation** - Improve or translate documentation. Both English and Chinese docs are maintained.
- **Performance** - Identify bottlenecks and submit benchmarks or optimizations.
- **Code Review** - Review open PRs and provide constructive feedback.

## Development Environment

### Prerequisites

- **JDK 8+** (JDK 17+ recommended for full test coverage)
- **Maven 3.6+** (or use the included Maven wrapper `./mvnw`)
- **Git**

### Setup

1. Fork the repository on GitHub.
2. Clone your fork:
   ```bash
   git clone https://github.com/<your-username>/fastjson2.git
   cd fastjson2
   ```
3. Add the upstream remote:
   ```bash
   git remote add upstream https://github.com/alibaba/fastjson2.git
   ```
4. Create a feature branch:
   ```bash
   git checkout -b feature/my-improvement
   ```

## Building the Project

**Standard build:**

```bash
./mvnw clean package
```

**Build with Javadoc and Dokka generation:**

```bash
./mvnw -V --no-transfer-progress -Pgen-javadoc -Pgen-dokka clean package
```

**Build a specific module (faster iteration):**

```bash
./mvnw -pl core clean package
```

## Running Tests

The CI pipeline runs tests on multiple JDK versions (8, 11, 17, 21, 25) and operating systems (Ubuntu, Windows, macOS). Before submitting a PR, please run:

**1. Standard test:**

```bash
./mvnw clean test
```

**2. Reflect mode test (validates non-ASM code paths):**

```bash
./mvnw -Dfastjson2.creator=reflect clean test
```

**3. Checkstyle validation:**

```bash
./mvnw validate
```

**Run tests in a specific module:**

```bash
./mvnw -pl core test
```

**Run a specific test class:**

```bash
./mvnw -pl core -Dtest=JSONTest test
```

## Code Style

FASTJSON 2 uses Checkstyle to enforce coding standards. The configuration is in `src/checkstyle/fastjson2-checks.xml`.

Key conventions:

- **Indentation**: 4 spaces (no tabs).
- **Line length**: Follow the existing patterns in the file you're modifying.
- **Naming**: Standard Java naming conventions (camelCase for methods/fields, PascalCase for classes).
- **Imports**: No wildcard imports. Organize imports alphabetically.
- **Tests**: Use JUnit 5. Place tests in the corresponding `src/test/java` directory.
- **Kotlin**: Follow standard Kotlin conventions for the `kotlin` module.

Run `./mvnw validate` to check for style violations before committing.

## Creating a Pull Request

### Before You Start

- For significant changes, open an issue first to discuss the approach.
- Check existing issues and PRs to avoid duplication.

### PR Guidelines

1. **Keep it small and focused.** A PR with one clear purpose is easier to review and merge. If you have multiple independent improvements, submit separate PRs.

2. **Include tests.** Every bug fix should include a regression test. New features should include unit tests covering key scenarios.

3. **Don't break existing tests.** Run both standard and reflect mode tests locally.

4. **Follow commit message conventions:**
   - Use descriptive commit messages that explain _why_, not just _what_.
   - Examples:
     - `fix: JSONPath nested array filter returning incorrect results`
     - `feat: add support for JDK 25 records`
     - `docs: improve Spring integration guide`

5. **Update documentation** if your change affects the public API or user-facing behavior.

### PR Checklist

- [ ] Code compiles without errors: `./mvnw clean package`
- [ ] All tests pass: `./mvnw clean test`
- [ ] Reflect mode tests pass: `./mvnw -Dfastjson2.creator=reflect clean test`
- [ ] Checkstyle passes: `./mvnw validate`
- [ ] New/changed functionality has test coverage
- [ ] Documentation updated if applicable

### Review Process

- A maintainer will review your PR and may request changes.
- Address review feedback by pushing additional commits (not force-pushing).
- Once approved, a maintainer will merge the PR.

## Reporting Issues

Well-written bug reports help us fix issues faster. When reporting, please include:

1. **FASTJSON 2 version** you are using.
2. **JDK version** and operating system.
3. **Minimal reproduction code** - a self-contained test case that demonstrates the issue.
4. **Expected behavior** vs. **actual behavior**.
5. **Stack traces or log output** if applicable.

### Security Issues

For security vulnerabilities, do **not** open a public issue. Instead, report via [https://security.alibaba.com](https://security.alibaba.com). See [SECURITY.md](SECURITY.md) for details.

### Tips

- Search [existing issues](https://github.com/alibaba/fastjson2/issues) before creating a new one.
- If you find an existing issue that matches yours, add a comment or reaction instead of opening a duplicate.
- Remove any sensitive information (usernames, passwords, IPs) from your examples. Use `"REDACTED"` as a placeholder.

## Project Structure

```
fastjson2/
├── core/                        # Core JSON library (JDK 8+)
├── kotlin/                      # Kotlin extension functions
├── extension/                   # Base extensions (Arrow, ClickHouse, etc.)
├── extension-spring5/           # Spring 5.x integration
├── extension-spring6/           # Spring 6.x integration
├── extension-solon/             # Solon framework integration
├── extension-jaxrs/             # JAX-RS integration
├── fastjson1-compatible/        # Fastjson 1.x API compatibility layer
├── codegen/                     # Compile-time code generation (APT)
├── benchmark/                   # JMH performance benchmarks
├── example-spring-test/         # Spring 5 example project
├── example-spring6-test/        # Spring 6 example project
├── example-solon-test/          # Solon example project
├── safemode-test/               # SafeMode test suite
├── android-test/                # Android compatibility tests
├── test-jdk17/                  # JDK 17 feature tests (Records, etc.)
├── test-jdk25/                  # JDK 25 feature tests
├── docs/                        # Documentation
└── src/checkstyle/              # Checkstyle configuration
```

### Key Source Directories in `core/`

| Package | Purpose |
|---------|---------|
| `com.alibaba.fastjson2` | Top-level API: `JSON`, `JSONB`, `JSONObject`, `JSONArray`, `JSONReader`, `JSONWriter`, `JSONPath` |
| `com.alibaba.fastjson2.reader` | Object deserialization: `ObjectReader`, `FieldReader`, creator implementations |
| `com.alibaba.fastjson2.writer` | Object serialization: `ObjectWriter`, `FieldWriter`, creator implementations |
| `com.alibaba.fastjson2.annotation` | Annotations: `@JSONField`, `@JSONType`, `@JSONCreator`, `@JSONCompiler` |
| `com.alibaba.fastjson2.filter` | Serialization filters: `ValueFilter`, `NameFilter`, `PropertyFilter`, etc. |
| `com.alibaba.fastjson2.schema` | JSON Schema validation |
| `com.alibaba.fastjson2.support.csv` | CSV reader/writer support |
| `com.alibaba.fastjson2.util` | Internal utilities |

Thank you for contributing to FASTJSON 2!
