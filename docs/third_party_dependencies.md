# Third-Party Dependencies

This document lists all third-party libraries used in **PrecisionMail** (`nlu.fit.soft.gr5:PrecisionMail:1.0-SNAPSHOT`), including version, license, and purpose.

---

## 1. JavaFX — UI Framework

### javafx-controls
| Field      | Detail |
|------------|--------|
| Group ID   | `org.openjfx` |
| Version    | `25.0.3` |
| Scope      | compile |
| License    | [GPL v2 with Classpath Exception](https://openjdk.org/legal/gplv2+ce.html) |
| Homepage   | https://openjfx.io |
| Description | Provides standard UI controls such as Button, TextField, TableView, ListView, ComboBox, etc. Used to build the main application interface. |

---

### javafx-fxml
| Field      | Detail |
|------------|--------|
| Group ID   | `org.openjfx` |
| Version    | `25.0.3` |
| Scope      | compile |
| License    | [GPL v2 with Classpath Exception](https://openjdk.org/legal/gplv2+ce.html) |
| Homepage   | https://openjfx.io |
| Description | Enables FXML-based declarative UI design. Allows UI layouts to be defined in `.fxml` files and loaded at runtime via `FXMLLoader`. |

---

### javafx-web
| Field      | Detail |
|------------|--------|
| Group ID   | `org.openjfx` |
| Version    | `25.0.3` |
| Scope      | compile |
| License    | [GPL v2 with Classpath Exception](https://openjdk.org/legal/gplv2+ce.html) |
| Homepage   | https://openjfx.io |
| Description | Provides an embedded `WebView` component powered by WebKit. Used to render HTML email content inside the application. |

---

## 2. Jakarta Mail — Email Protocol

### jakarta.mail (Eclipse Angus)
| Field      | Detail |
|------------|--------|
| Group ID   | `org.eclipse.angus` |
| Version    | `2.0.4` |
| Scope      | compile |
| License    | [EPL 2.0](https://www.eclipse.org/legal/epl-2.0/) / [EDL 1.0](https://www.eclipse.org/org/documents/edl-v10.php) |
| Homepage   | https://eclipse-ee4j.github.io/angus-mail |
| Description | Full implementation of the Jakarta Mail API. Supports sending and receiving emails via SMTP, IMAP, and POP3 protocols. |

---

### angus-activation
| Field      | Detail |
|------------|--------|
| Group ID   | `org.eclipse.angus` |
| Version    | `2.0.2` |
| Scope      | compile |
| License    | [EPL 2.0](https://www.eclipse.org/legal/epl-2.0/) / [EDL 1.0](https://www.eclipse.org/org/documents/edl-v10.php) |
| Homepage   | https://eclipse-ee4j.github.io/angus-activation |
| Description | Jakarta Activation implementation. Required by Jakarta Mail for handling MIME type detection and content data handlers in email attachments. |

---

## 3. Jackson — JSON Processing

### jackson-databind
| Field      | Detail |
|------------|--------|
| Group ID   | `com.fasterxml.jackson.core` |
| Version    | `2.19.0` |
| Scope      | compile |
| License    | [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0) |
| Homepage   | https://github.com/FasterXML/jackson-databind |
| Description | Core JSON serialization and deserialization library. Used to convert Java objects to/from JSON for data persistence and configuration handling. |

---

### jackson-datatype-jsr310
| Field      | Detail |
|------------|--------|
| Group ID   | `com.fasterxml.jackson.datatype` |
| Version    | `2.19.0` |
| Scope      | compile |
| License    | [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0) |
| Homepage   | https://github.com/FasterXML/jackson-modules-java8 |
| Description | Jackson module that adds support for Java 8+ Date/Time types (`LocalDate`, `LocalDateTime`, `ZonedDateTime`, etc.). Required when serializing/deserializing date fields in email metadata. |

---

## 4. SQLite JDBC — Local Database

### sqlite-jdbc
| Field      | Detail |
|------------|--------|
| Group ID   | `org.xerial` |
| Version    | `3.50.3.0` |
| Scope      | compile |
| License    | [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0) |
| Homepage   | https://github.com/xerial/sqlite-jdbc |
| Description | JDBC driver for SQLite. Enables embedded, file-based relational database storage with no external server required. Used for local storage of emails, contacts, and application data. |

---

## 5. Logging

### slf4j-api
| Field      | Detail |
|------------|--------|
| Group ID   | `org.slf4j` |
| Version    | `2.0.17` |
| Scope      | compile |
| License    | [MIT License](https://opensource.org/licenses/MIT) |
| Homepage   | https://www.slf4j.org |
| Description | Simple Logging Facade for Java. Provides a common logging abstraction layer, decoupling application code from the underlying logging implementation. |

---

### logback-classic
| Field      | Detail |
|------------|--------|
| Group ID   | `ch.qos.logback` |
| Version    | `1.5.18` |
| Scope      | compile |
| License    | [EPL 1.0](https://www.eclipse.org/legal/epl-v10.html) / [LGPL 2.1](https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html) |
| Homepage   | https://logback.qos.ch |
| Description | Native implementation of the SLF4J API. Serves as the actual logging backend, outputting structured logs to console or file with configurable patterns and levels. |

---

## 6. Testing *(scope: test)*

### junit-jupiter-api
| Field      | Detail |
|------------|--------|
| Group ID   | `org.junit.jupiter` |
| Version    | `5.12.1` |
| Scope      | **test** |
| License    | [EPL 2.0](https://www.eclipse.org/legal/epl-2.0/) |
| Homepage   | https://junit.org/junit5 |
| Description | JUnit 5 API for writing unit tests. Provides annotations (`@Test`, `@BeforeEach`, `@AfterEach`, etc.) and assertion utilities. |

---

### junit-jupiter-engine
| Field      | Detail |
|------------|--------|
| Group ID   | `org.junit.jupiter` |
| Version    | `5.12.1` |
| Scope      | **test** |
| License    | [EPL 2.0](https://www.eclipse.org/legal/epl-2.0/) |
| Homepage   | https://junit.org/junit5 |
| Description | JUnit 5 test execution engine. Responsible for discovering and running tests written with `junit-jupiter-api` at build time via Maven Surefire. |

---

## Summary Table

| Library                      | Version    | Scope   | License                  |
|------------------------------|------------|---------|--------------------------|
| `javafx-controls`            | 25.0.3     | compile | GPL v2 + CE              |
| `javafx-fxml`                | 25.0.3     | compile | GPL v2 + CE              |
| `javafx-web`                 | 25.0.3     | compile | GPL v2 + CE              |
| `jakarta.mail`               | 2.0.4      | compile | EPL 2.0 / EDL 1.0        |
| `angus-activation`           | 2.0.2      | compile | EPL 2.0 / EDL 1.0        |
| `jackson-databind`           | 2.19.0     | compile | Apache 2.0               |
| `jackson-datatype-jsr310`    | 2.19.0     | compile | Apache 2.0               |
| `sqlite-jdbc`                | 3.50.3.0   | compile | Apache 2.0               |
| `slf4j-api`                  | 2.0.17     | compile | MIT                      |
| `logback-classic`            | 1.5.18     | compile | EPL 1.0 / LGPL 2.1       |
| `junit-jupiter-api`          | 5.12.1     | test    | EPL 2.0                  |
| `junit-jupiter-engine`       | 5.12.1     | test    | EPL 2.0                  |

> **Total:** 12 third-party dependencies (10 runtime, 2 test-only)