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
