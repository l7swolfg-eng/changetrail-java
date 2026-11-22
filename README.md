# ChangeTrail

ChangeTrail is a Java command-line tool that analyzes source code dependencies to identify which test files may be affected by a code change.

Instead of rerunning every test in a project, ChangeTrail helps developers identify potentially relevant tests by tracing dependencies between Java classes.

## Features

- Recursively discovers Java source files
- Extracts Java imports and package declarations
- Builds a directed dependency graph
- Uses breadth-first search (BFS) to find affected files
- Detects basic same-package class references
- Identifies affected test-source files in Maven projects
- Provides a command-line interface for analyzing changes

## Technologies

- Java 21
- Apache Maven
- JUnit 5
- Java NIO
- Graph algorithms (BFS)

## How It Works

ChangeTrail represents Java files as vertices in a directed graph.

If one class depends on another, the graph stores a reverse dependency edge. This allows the application to determine which files may be affected when a class changes.

For example:

PaymentService → PaymentProcessor → OrderService → OrderServiceTest

If PaymentService changes, ChangeTrail traverses the graph to identify the dependent files and reports OrderServiceTest as a potentially affected test.

## Getting Started

### Requirements

- Java 21 or newer
- Apache Maven 3.9+

### Build

Clone the repository and run:

```bash
mvn clean test
```

Then compile:

```bash
mvn compile
```

### Run

```bash
java -cp target/classes dev.changetrail.Main \
  <project-directory> <changed-file>
```

The changed-file path can be relative to the specified project directory.

### Example

The repository includes a sample Java project under `examples/shop-demo`.

Run:

```bash
java -cp target/classes dev.changetrail.Main \
  examples/shop-demo \
  src/main/java/com/shop/payments/PaymentService.java
```

Example output:

```text
ChangeTrail - Test Impact Analysis

Changed file:
  src/main/java/com/shop/payments/PaymentService.java

Affected files:
  src/main/java/com/shop/orders/OrderService.java
  src/main/java/com/shop/payments/PaymentProcessor.java
  src/test/java/com/shop/tests/OrderServiceTest.java

Affected test files:
  src/test/java/com/shop/tests/OrderServiceTest.java

Total affected tests: 1
```

## Testing

The project uses JUnit 5 for unit and integration testing.

Tests cover:

- Java file discovery
- Import and package extraction
- Dependency graph construction
- BFS traversal and circular dependencies
- Test file identification
- End-to-end test impact analysis

Run all tests using:

```bash
mvn test
```

## Current Limitations

ChangeTrail is a lightweight static analysis tool, not a full Java compiler or test runner.

Current limitations include:

- Only conventional Maven source directories are supported.
- Import analysis uses simple line-based parsing.
- Same-package references use text matching, which can produce false positives.
- Some dependencies, including wildcard imports, inheritance, and dynamically resolved references, may be missed.
- Results identify potentially affected test-source files; tests are not executed automatically.

## Future Improvements

- Use JavaParser for more accurate source analysis
- Support additional Maven modules and source roots
- Detect changed files automatically using Git
- Export dependency graphs for visualization
- Improve test selection accuracy

## License

This project is licensed under the MIT License. See LICENSE for details.
