# Technology Stack & License

## Project Information

| Field        | Value                     |
|--------------|---------------------------|
| Project Name | **PrecisionMail**         |
| Group ID     | `nlu.fit.soft.gr5`        |
| Artifact ID  | `PrecisionMail`           |
| Version      | `1.0-SNAPSHOT`            |
| Build Tool   | Apache Maven              |

---

## Core Technology

| Technology   | Version | Description                              |
|--------------|---------|------------------------------------------|
| **Java**     | 21 (LTS) | Primary programming language, compiled with `maven-compiler-plugin` |
| **JavaFX**   | 25.0.3  | UI framework for desktop application     |
| **Maven**    | —       | Dependency management & build automation |

---

## Dependencies

### UI Framework

| Library              | Group ID        | Version | License       | Description                          |
|----------------------|-----------------|---------|---------------|--------------------------------------|
| `javafx-controls`    | `org.openjfx`   | 25.0.3  | GPL v2 + CE   | JavaFX UI controls (buttons, tables, etc.) |
| `javafx-fxml`        | `org.openjfx`   | 25.0.3  | GPL v2 + CE   | FXML support for declarative UI      |
| `javafx-web`         | `org.openjfx`   | 25.0.3  | GPL v2 + CE   | Embedded WebView component           |

### Email

| Library               | Group ID               | Version | License          | Description                            |
|-----------------------|------------------------|---------|------------------|----------------------------------------|
| `jakarta.mail`        | `org.eclipse.angus`    | 2.0.4   | EPL 2.0 / EDL 1.0 | Jakarta Mail API implementation (SMTP, IMAP, POP3) |
| `angus-activation`    | `org.eclipse.angus`    | 2.0.2   | EPL 2.0 / EDL 1.0 | Jakarta Activation support for MIME types |

### Data Serialization

| Library                        | Group ID                          | Version | License    | Description                          |
|--------------------------------|-----------------------------------|---------|------------|--------------------------------------|
| `jackson-databind`             | `com.fasterxml.jackson.core`      | 2.19.0  | Apache 2.0 | JSON serialization / deserialization |
| `jackson-datatype-jsr310`      | `com.fasterxml.jackson.datatype`  | 2.19.0  | Apache 2.0 | Java 8+ Date/Time type support for Jackson |

### Database

| Library       | Group ID        | Version   | License    | Description                    |
|---------------|-----------------|-----------|------------|--------------------------------|
| `sqlite-jdbc` | `org.xerial`    | 3.50.3.0  | Apache 2.0 | SQLite JDBC driver for embedded local database |

### Logging

| Library           | Group ID           | Version | License    | Description                          |
|-------------------|--------------------|---------|------------|--------------------------------------|
| `slf4j-api`       | `org.slf4j`        | 2.0.17  | MIT        | Logging facade / abstraction API     |
| `logback-classic` | `ch.qos.logback`   | 1.5.18  | EPL 1.0 / LGPL 2.1 | Logging implementation backend  |

### Testing

| Library                   | Group ID               | Version | Scope  | License    | Description                     |
|---------------------------|------------------------|---------|--------|------------|---------------------------------|
| `junit-jupiter-api`       | `org.junit.jupiter`    | 5.12.1  | test   | EPL 2.0    | JUnit 5 testing API             |
| `junit-jupiter-engine`    | `org.junit.jupiter`    | 5.12.1  | test   | EPL 2.0    | JUnit 5 test execution engine   |

---

## Build Plugins

| Plugin                    | Group ID               | Version | Description                                    |
|---------------------------|------------------------|---------|------------------------------------------------|
| `maven-compiler-plugin`   | `org.apache.maven.plugins` | 3.13.0 | Compiles Java source with source/target level 21 |
| `javafx-maven-plugin`     | `org.openjfx`          | 0.0.8   | Builds and runs JavaFX app (`mvn javafx:run`)  |

---

## License Summary

| License                        | Used By                                                                 |
|--------------------------------|-------------------------------------------------------------------------|
| **GPL v2 with Classpath Exception** | JavaFX (`javafx-controls`, `javafx-fxml`, `javafx-web`)           |
| **Apache License 2.0**         | Jackson (`jackson-databind`, `jackson-datatype-jsr310`), SQLite JDBC   |
| **EPL 2.0 / EDL 1.0**          | Eclipse Angus Mail (`jakarta.mail`, `angus-activation`)                |
| **MIT**                        | SLF4J (`slf4j-api`)                                                     |
| **EPL 1.0 / LGPL 2.1**         | Logback (`logback-classic`)                                             |
| **EPL 2.0**                    | JUnit 5 (`junit-jupiter-api`, `junit-jupiter-engine`)                  |

> **Note:** This project is developed under the context of `nlu.fit.soft.gr5` (Trường Đại Học Nông Lâm TPHCM, nhóm 5 khoa CNTT, môn Công Nghệ Phần Mềm). No explicit project-level license has been declared in `pom.xml`.

---

## Quick Start

```bash
# Chạy ứng dụng
mvn clean javafx:run

# Chạy unit tests
mvn test
```