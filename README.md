# PrecisionMail

PrecisionMail is a JavaFX desktop email client for configuring mail accounts, composing email, scheduling delivery, tracking the send queue, browsing sent history, and monitoring application logs. The project is implemented as a Maven application using Java 25, JavaFX, Jakarta Mail, SQLite, SLF4J, and Logback.

## Features

- Configure SMTP/IMAP account settings with SSL/TLS support.
- Validate mail server credentials before saving account configuration.
- Encrypt application passwords before writing them to SQLite.
- Compose rich-text HTML email with To/Cc/Bcc and attachments.
- Send email asynchronously without blocking the JavaFX UI thread.
- Schedule email delivery with persistent queue storage and automatic bootstrap on application startup.
- Retry scheduled sends on transient network failure.
- View, edit, cancel, and inspect queued email tasks.
- Search sent history, view email detail, open original attachments, and export CSV.
- Monitor system logs and export technical log files.

## Tech Stack

| Area | Technology |
| --- | --- |
| Language | Java 25 |
| UI | JavaFX 25 |
| Build | Maven Wrapper |
| Mail | Jakarta Mail / Eclipse Angus |
| Database | SQLite JDBC |
| JSON | Jackson |
| Logging | SLF4J + Logback |
| Async | Java Virtual Threads, `CompletableFuture`, `ScheduledExecutorService` |

## Requirements

- JDK 25.
- Internet access for live SMTP/IMAP connection tests and email sending.
- A mail account with an application password enabled, for example Gmail App Password.
- Linux, Windows, or another desktop OS supported by JavaFX.

## Configuration

The application reads `application.properties` from the runtime classpath. If it is missing, it falls back to `example.application.properties`.

Create `src/main/resources/application.properties` from the example file:

```properties
# Mail defaults
mail.host=smtp.gmail.com
mail.port=587
mail.username=yourEmail
mail.password=yourAppPassword

# Required for AES/GCM password encryption
security.aes.key=replace-with-your-own-secret
```

The saved mail account password is encrypted before being stored in `precisionmail.db`. Do not commit real credentials or production encryption keys.

## Run

Use the Maven wrapper from the project root:

```bash
./mvnw clean javafx:run
```

On Windows:

```powershell
.\mvnw.cmd clean javafx:run
```

The JavaFX Maven plugin already passes the required native-access options:

```text
--enable-native-access=javafx.graphics
--enable-native-access=ALL-UNNAMED
```

## Build

Compile the project:

```bash
./mvnw clean compile
```

Package the application:

```bash
./mvnw clean package
```

Run tests, when test classes are available:

```bash
./mvnw test
```

## Project Structure

```text
src/main/java/nlu/fit/soft/gr5/precisionMail
├── controller/          JavaFX controllers
├── dao/                 DAO interfaces
├── dao/impl/            SQLite DAO implementations
├── infrastructure/      Async executor and database bootstrap
├── model/               Domain models and enums
├── service/             Service interfaces
├── service/impl/        Business logic implementations
└── util/                Mail, crypto, DB, logging, and validation helpers

src/main/resources
├── logback.xml
├── example.application.properties
└── nlu/fit/soft/gr5/precisionMail
    ├── css/
    └── view/
```

## Main Use Cases

| Use Case | Description | Main implementation |
| --- | --- | --- |
| UC-01 | Configure mail server connection | `AddAccountDialogController`, `AccountServiceImpl`, `EmailUtil`, `CryptoUtil` |
| UC-02 | Compose and send email | `ComposeController`, `EmailServiceImpl`, `EmailDaoImpl` |
| UC-03 | Schedule email delivery | `ComposeController`, `ScheduledEmailServiceImpl`, `ScheduledEmailDaoImpl` |
| UC-04 | Manage queued email | `QueueController`, `QueueServiceImpl`, `ScheduledEmailServiceImpl` |
| UC-05 | Search sent history | `HistoryMailController`, `HistoryServiceImpl`, `EmailDaoImpl` |
| UC-06 | Monitor system logs | `LogController`, `LogMonitoringServiceImpl`, `logback.xml` |

## Data Storage

PrecisionMail uses a local SQLite database named `precisionmail.db`. On startup, `DatabaseInitializer` creates or updates the required tables:

- `accounts`: encrypted account and mail server configuration.
- `scheduled_emails`: persistent send queue for scheduled tasks.
- `sent_emails`: sent and failed email history.

SQLite WAL mode and foreign keys are enabled when a connection is opened.

## Logging

Logback writes application logs under `logs/`. The logging configuration is defined in `src/main/resources/logback.xml` and includes rolling-file behavior. Sensitive values such as raw passwords are not logged; email addresses are masked in operational log messages.

## Documentation

Project documentation is stored in `docs/`:

- `business_requirments_document.md`: business requirements.
- `analysis_and_design_document.md`: analysis and design overview.
- `implementation_guide.md`: implementation-report guide.
- `UC_01.md` to `UC_06.md`: use case specifications.
- `UC_01_Implementation.md` to `UC_06_Implementation.md`: implementation reports.
- `UC_01_Testing.md`: UC-01 test cases.
- `design_and_implement_log_system.md`: logging design and implementation.

## Notes

- Live connection and send tests require real mail credentials and network access.
- Attachment files are referenced by local path; the application does not copy raw attachment content into SQLite.
- The current database file and runtime logs are local development artifacts and should not be treated as source documentation.

## Credits

Icon assets are sourced from [Flaticon](https://www.flaticon.com/).
