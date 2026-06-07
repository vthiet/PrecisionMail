# PrecisionMail - Class Diagram các model chính

Sơ đồ dưới đây mô tả các model nghiệp vụ chính trong package
`nlu.fit.soft.gr5.precisionMail.model`.

```mermaid
classDiagram
    direction LR

    class Account {
        <<entity>>
        -Long id
        -String username
        -String password
        -String displayName
        -boolean primary
        -boolean passwordDecryptionFailed
        -MailServerConfig mailServerConfig
        -LocalDateTime createdAt
        +getUsername() String
        +getDisplayName() String
        +isPrimary() boolean
        +getMailServerConfig() MailServerConfig
    }

    class MailServerConfig {
        <<value object>>
        -String smtpHost
        -int smtpPort
        -String imapHost
        -int imapPort
        -SecurityMode smtpSecurityMode
        -SecurityMode imapSecurityMode
    }

    class Email {
        <<entity>>
        +Long id
        +String from
        +Set~String~ toLst
        +Set~String~ cc
        +Set~String~ bcc
        +String subject
        +String content
        +List~String~ attachments
        +LocalDateTime sentAt
        +EmailStatus status
        +String errorMessage
        +getFrom() String
    }

    class ScheduledEmail {
        <<entity>>
        +Long id
        +Account account
        +Email email
        +LocalDateTime scheduledAt
        +EmailStatus status
        +String errorMessage
        +int retryCount
        +LocalDateTime actualSentAt
    }

    class DraftEmail {
        <<entity>>
        +Long id
        +String senderEmail
        +String toRecipients
        +String ccRecipients
        +String bccRecipients
        +String subject
        +String body
        +String attachmentPaths
        +LocalDateTime lastSavedAt
        +LocalDateTime createdAt
        +LocalDateTime updatedAt
    }

    class RecipientGroup {
        <<entity>>
        +Long id
        +String name
        +String description
        +Set~String~ emails
        +Integer emailCount
        +LocalDateTime createdAt
        +LocalDateTime updatedAt
    }

    class EmailStatus {
        <<enumeration>>
        DRAFT
        QUEUED
        SCHEDULED
        SENDING
        SENT
        FAILED
        CANCELLED
        RETRY_PENDING
        MISSED
    }

    class SecurityMode {
        <<enumeration>>
        TLS
        SSL
    }

    class MailProviderPreset {
        <<enumeration>>
        CUSTOM
        GMAIL
        OUTLOOK
        YAHOO
        -String displayName
        -MailServerConfig config
        +hasConfig() boolean
        +getConfig() MailServerConfig
        +inferFrom(config) MailProviderPreset
    }

    Account "1" *-- "1" MailServerConfig : cấu hình máy chủ
    Account "0..1" <-- "0..*" ScheduledEmail : tài khoản gửi
    ScheduledEmail "1" *-- "1" Email : nội dung gửi
    Email --> EmailStatus : trạng thái
    ScheduledEmail --> EmailStatus : trạng thái hàng đợi
    MailServerConfig --> SecurityMode : bảo mật SMTP/IMAP
    MailProviderPreset --> MailServerConfig : tạo cấu hình mẫu
```

## Ghi chú quan hệ

- `Account` sở hữu một `MailServerConfig`; cấu hình này được lưu cùng bản ghi tài khoản.
- `ScheduledEmail` chứa nội dung `Email` và có thể tham chiếu một `Account`. Cột
  `scheduled_emails.account_id` trong SQLite cho phép giá trị `NULL`.
- `Email` đại diện lịch sử thư đã gửi, còn `ScheduledEmail` đại diện thư trong hàng
  đợi/lập lịch. Hai model dùng chung `EmailStatus`.
- `DraftEmail` và `RecipientGroup` hiện lưu địa chỉ email bằng `String`/`Set<String>`;
  chúng không có tham chiếu object hoặc khóa ngoại trực tiếp đến model khác.
- Các model hỗ trợ như `ConnectionTestResult`, `ConnectionTestProgress` và `LogEntry`
  không nằm trong phạm vi sơ đồ model nghiệp vụ chính.


Các file liên quan đến hiện thực **UC-06 – Theo dõi log hệ thống**:

**Hiện thực chính**
- [LogController.java](/home/vthiet/develop/java/PrecisionMail/src/main/java/nlu/fit/soft/gr5/precisionMail/controller/fxml/LogController.java): Điều khiển UI, tải/lọc log, xem stacktrace, mở thư mục và xuất ZIP.
- [LogMonitoringService.java](/home/vthiet/develop/java/PrecisionMail/src/main/java/nlu/fit/soft/gr5/precisionMail/service/LogMonitoringService.java): Khai báo API giám sát log.
- [LogMonitoringServiceImpl.java](/home/vthiet/develop/java/PrecisionMail/src/main/java/nlu/fit/soft/gr5/precisionMail/service/impl/LogMonitoringServiceImpl.java): Đọc, nhóm, lọc, theo dõi thời gian thực và xuất log.
- [LogEntry.java](/home/vthiet/develop/java/PrecisionMail/src/main/java/nlu/fit/soft/gr5/precisionMail/model/LogEntry.java): Parse bản ghi log và xử lý nội dung chi tiết/stacktrace.
- [LogSanitizer.java](/home/vthiet/develop/java/PrecisionMail/src/main/java/nlu/fit/soft/gr5/precisionMail/util/LogSanitizer.java): Che mật khẩu, token và email.
- [SecurePatternLayout.java](/home/vthiet/develop/java/PrecisionMail/src/main/java/nlu/fit/soft/gr5/precisionMail/util/SecurePatternLayout.java): Masking dữ liệu trước khi Logback ghi xuống file.
- [system-logs.fxml](/home/vthiet/develop/java/PrecisionMail/src/main/resources/nlu/fit/soft/gr5/precisionMail/view/include/center/system-logs.fxml): Giao diện màn hình log.
- [logback.xml](/home/vthiet/develop/java/PrecisionMail/src/main/resources/logback.xml): Cấu hình `system.log`, AsyncAppender, xoay vòng 10 MB và giới hạn 200 MB.

**Tích hợp và hỗ trợ**
- [AppExecutors.java](/home/vthiet/develop/java/PrecisionMail/src/main/java/nlu/fit/soft/gr5/precisionMail/infrastructure/async/AppExecutors.java): Thực thi I/O UC-06 bằng Virtual Thread.
- [SideBarController.java](/home/vthiet/develop/java/PrecisionMail/src/main/java/nlu/fit/soft/gr5/precisionMail/controller/fxml/SideBarController.java): Điều hướng tới màn hình log.
- [sidebar.fxml](/home/vthiet/develop/java/PrecisionMail/src/main/resources/nlu/fit/soft/gr5/precisionMail/view/include/sidebar.fxml): Nút “Log hệ thống”.
- [MainScreenController.java](/home/vthiet/develop/java/PrecisionMail/src/main/java/nlu/fit/soft/gr5/precisionMail/controller/fxml/MainScreenController.java): Đóng watcher khi rời màn hình log.
- [NavigationService.java](/home/vthiet/develop/java/PrecisionMail/src/main/java/nlu/fit/soft/gr5/precisionMail/service/NavigationService.java): Hỗ trợ chuyển màn hình.
- [AlertUtil.java](/home/vthiet/develop/java/PrecisionMail/src/main/java/nlu/fit/soft/gr5/precisionMail/util/AlertUtil.java): Hiển thị thông báo xuất log và lỗi.
- [module-info.java](/home/vthiet/develop/java/PrecisionMail/src/main/java/module-info.java): Khai báo JavaFX, Desktop, SLF4J và Logback.
- [pom.xml](/home/vthiet/develop/java/PrecisionMail/pom.xml): Dependency JavaFX, SLF4J, Logback và JUnit.

**Kiểm thử UC-06**
- [LogMonitoringServiceImplTest.java](/home/vthiet/develop/java/PrecisionMail/src/test/java/nlu/fit/soft/gr5/precisionMail/service/impl/LogMonitoringServiceImplTest.java)
- [LogEntryTest.java](/home/vthiet/develop/java/PrecisionMail/src/test/java/nlu/fit/soft/gr5/precisionMail/uc06/LogEntryTest.java)
- [LogSanitizerTest.java](/home/vthiet/develop/java/PrecisionMail/src/test/java/nlu/fit/soft/gr5/precisionMail/uc06/LogSanitizerTest.java)

**Tài liệu liên quan**
- [UC-SPEC-06-ieee-revised.md](/home/vthiet/develop/java/PrecisionMail/docs/UC-SPEC-06-ieee-revised.md)
- [UC_06.md](/home/vthiet/develop/java/PrecisionMail/docs/UC_06.md)
- [UC_06_Implementation.md](/home/vthiet/develop/java/PrecisionMail/docs/UC_06_Implementation.md)