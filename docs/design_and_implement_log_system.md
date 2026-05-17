# TÀI LIỆU THIẾT KẾ VÀ TRIỂN KHAI CHI TIẾT HỆ THỐNG GHI LOG (LOGBACK ENGINE)

**Mã tài liệu:** LOG-IMPL-SPEC-01
<br>
**Tiêu chuẩn áp dụng:** IEEE Std 830-1998 / IEEE Std 29148-2018\
<br>
**Trạng thái:** Sẵn sàng Triển khai (Production-Ready)

## 1\. Giới thiệu và Hạ tầng ghi Log (Introduction & Architecture)

Tài liệu này đặc tả chi tiết cách thức cấu hình và triển khai hạ tầng ghi log (Logging Subsystem) cho ứng dụng Desktop Email System sử dụng bộ đôi thư viện tiêu chuẩn **SLF4J** (Simple Logging Facade for Java) và **Logback** làm động cơ thực thi chính.

### 1.1 Mục tiêu hệ thống Log

1.  **Giám sát (Monitoring):** Theo dõi trạng thái hoạt động của hệ thống thời gian thực mà không làm gián đoạn UI.
2.  **Gỡ lỗi (Auditing/Debugging):** Cung cấp đầy đủ Stacktrace và thông tin ngữ cảnh để nhanh chóng cô lập và sửa lỗi phát sinh trên máy trạm của khách hàng.
3.  **Bảo mật (Data Security):** Tuyệt đối tuân thủ tiêu chuẩn bảo vệ dữ liệu, không ghi đè mật khẩu thô hoặc nội dung thư riêng tư của người dùng ra đĩa cứng.

## 2\. Cấu hình tệp tin Logback (logback.xml)

Tệp tin cấu hình này được đóng gói trong thư mục `src/main/resources/logback.xml` của dự án Maven. Nó thiết lập 2 appender chính: **RollingFile** (ghi tệp tin tự xoay vòng) và **Async** (bọc bất đồng bộ để tránh chặn I/O luồng chính).

``` xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <property name="LOG_DIR" value="${user.home}/.precisionmail/logs"/>
    <property name="LOG_FILE" value="${LOG_DIR}/system.log"/>

    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_FILE}</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>${LOG_DIR}/system.%d{yyyy-MM-dd}.%i.log.gz</fileNamePattern>
            <maxFileSize>10MB</maxFileSize>
            <maxHistory>30</maxHistory>
            <totalSizeCap>200MB</totalSizeCap>
        </rollingPolicy>
        <encoder class="ch.qos.logback.core.encoder.LayoutWrappingEncoder">
            <layout class="nlu.fit.soft.gr5.precisionMail.util.SecurePatternLayout">
                <pattern>[%d{yyyy-MM-dd HH:mm:ss.SSS}] [%level] [%thread] [%class:%line] - %msg%n%ex</pattern>
            </layout>
        </encoder>
    </appender>

    <appender name="ASYNC_FILE" class="ch.qos.logback.classic.AsyncAppender">
        <queueSize>2048</queueSize>
        <discardingThreshold>0</discardingThreshold>
        <neverBlock>true</neverBlock>
        <includeCallerData>true</includeCallerData>
        <appender-ref ref="FILE"/>
    </appender>

    <root level="INFO">
        <appender-ref ref="ASYNC_FILE"/>
    </root>

    <logger name="nlu.fit.soft.gr5.precisionMail" level="DEBUG"/>
</configuration>

```

### 2.1 Đường dẫn lưu trữ vật lý tương thích Hệ điều hành

Đường dẫn lưu trữ đang được cấu hình thống nhất theo `user.home` để JavaFX, Logback và `LogMonitoringServiceImpl` cùng tham chiếu một vị trí:

* **Hệ điều hành Windows/Linux:** `${user.home}/.precisionmail/logs/`
  \**(Ví dụ Linux: `/home/<username>/.precisionmail/logs/system.log`; ví dụ Windows: `C:\Users\<Username>\.precisionmail\logs\system.log`)*

## 3\. Ma trận ghi nhận Log chi tiết (Logging Matrix)

Dưới đây là ma trận đặc tả chi tiết: **Khi nào ghi log**, **Ghi ở lớp/module nào**, **Sử dụng cấp độ log nào** và **Ghi những thông tin cụ thể gì** cho từng Use Case.

| Mã Use Case  | Ngữ cảnh ghi Log (When)                   | Lớp thực thi (Where)         | Cấp độ Log (Level) | Nội dung thông điệp chuẩn hóa (What)                                                                                                                 |
| :----------: | :---------------------------------------: | :--------------------------: | :----------------: | :--------------------------------------------------------------------------------------------------------------------------------------------------: |
| **Hệ thống** | Khởi chạy ứng dụng thành công             | `Launcher.java` / `App.java` | **INFO**           | "Application started successfully on JVM \[21\] and OS \[Windows/Linux\]."                                                                           |
| **Hệ thống** | Tắt ứng dụng an toàn                      | `Launcher.java`              | **INFO**           | "Shutdown hook triggered. Releasing resources, shutting down scheduler and threads cleanly."                                                         |
| **Hệ thống** | Gặp lỗi treo máy đột ngột / Tràn bộ nhớ   | `UncaughtExceptionHandler`   | **ERROR**          | "Fatal error detected in thread \[Thread\_Name\]. System state captured. Exception: \[Stacktrace\]"                                                  |
| **UC-01**    | Bắt đầu kiểm tra kết nối                  | `EmailService.java`          | **INFO**           | "Initiating test connection handshake to SMTP Host: \[host\], Port: \[port\], Protocol: \[SSL/TLS\]."                                                |
| **UC-01**    | Kết nối Mail Server thành công            | `EmailService.java`          | **INFO**           | "Handshake successful. SMTP Server responded: \[Response\_Code\_Message\] for masked sender \[us\*\*\*@domain.com\]."                                |
| **UC-01**    | Sai tài khoản/mật khẩu ứng dụng           | `EmailService.java`          | **WARN**           | "Authentication failed for mail user: \[us\*\*\*@domain.com\]. Code \[535\]. Reason: Invalid credentials."                                           |
| **UC-01**    | Mất mạng hoặc Timeout kết nối             | `EmailService.java`          | **WARN**           | "Connection timeout to Mail Server \[host:port\] after \[10\] seconds. Network is unreachable."                                                      |
| **UC-01**    | Lưu thông số cấu hình thành công          | `ConfigController.java`      | **INFO**           | "Configuration successfully encrypted using AES-256 and written to local database."                                                                  |
| **UC-01**    | Lỗi ghi Database SQLite/H2                | `ConfigController.java`      | **ERROR**          | "Failed to write configuration to SQLite database. Exception: \[SQLException\_Stacktrace\]"                                                          |
| **UC-02**    | Người dùng bấm nút "Gửi ngay"             | `ComposeController.java`     | **INFO**           | "User triggered 'Send Now' command. Initializing message compilation."                                                                               |
| **UC-02**    | Người dùng đính kèm file                  | `ComposeController.java`     | **DEBUG**          | "File selected: \[filename\], Size: \[size\] bytes. Total aggregated attachment size: \[total\_size\] bytes."                                        |
| **UC-02**    | File đính kèm vượt quá                    | `ComposeController.java`     | **WARN**           | "Attachment rejected. Reason: Aggregated size exceeds maximum threshold limit of 25MB."                                                              |
| **UC-02**    | Truyền tải email thành công               | `EmailService.java`          | **INFO**           | "Email transmitted successfully. Sender: \[sen\*\*\*@domain.com\], Subject Length: \[X\], Attachments count: \[Y\], SMTP Code: \[250\]."             |
| **UC-02**    | Lỗi từ chối từ Mail Server                | `EmailService.java`          | **ERROR**          | "SMTP Server rejected the mail transmission. Error Code: \[Code\]. Reason: \[SMTP\_ErrorMessage\]"                                                   |
| **UC-03**    | Lên lịch gửi thành công                   | `ScheduleController.java`    | **INFO**           | "Scheduled task created. Task ID: \[X\], Scheduled Target Time: \[T\_target\]. Database status set to PENDING."                                      |
| **UC-03**    | Lịch gửi không hợp lệ                     | `ScheduleController.java`    | **WARN**           | "Scheduling rejected. Reason: Target time \[T\_target\] is within the past or under the 1-minute minimum lock interval."                             |
| **UC-03**    | Bộ định thời quét và kích hoạt gửi        | `SchedulerEngine.java`       | **INFO**           | "Scheduler triggered task execution. Task ID: \[X\]. Spawning virtual thread."                                                                       |
| **UC-03**    | Phát hiện có tác vụ bị trễ hạn khi mở app | `BootstrapProcess.java`      | **WARN**           | "Startup sweep detected missed scheduled email tasks. Task ID: \[X\], Original target \[T\_target\]. Processing recovery policy: \[Skip/Send\_Now\]" |
| **UC-03**    | Gửi lại tự động khi mất mạng              | `SchedulerEngine.java`       | **WARN**           | "Connection failed for scheduled Task ID: \[X\]. Retrying task execution. Attempt \[Y/3\] in 300 seconds."                                           |
| **UC-04**    | Người dùng mở tab hàng đợi                | `QueueController.java`       | **DEBUG**          | "User requested queue list. Fetched \[X\] records with status \[PENDING\] from database."                                                            |
| **UC-04**    | Người dùng hủy lịch gửi thành công        | `QueueController.java`       | **INFO**           | "User manually cancelled scheduled email. Task ID: \[X\]. State transitioned from PENDING to CANCELLED."                                             |
| **UC-04**    | Vi phạm quy tắc khóa thời gian giây       | `QueueController.java`       | **WARN**           | "Cancellation denied for Task ID: \[X\]. Reason: Delta time \[Delta\_T\] is under the 60-second execution lock threshold."                           |
| **UC-05**    | Người dùng tra cứu lịch sử                | `HistoryController.java`     | **INFO**           | "History query executed. Filter criteria: \[Keyword length: X, Date range: T\_start to T\_end\]. Rows returned: \[Y\] in \[Z\] ms."                  |
| **UC-05**    | Người dùng xuất báo cáo CSV               | `HistoryController.java`     | **INFO**           | "Export history report initiated. Output path: \[destination\_path\]. Total rows converted: \[X\]."                                                  |
| **UC-06**    | Người dùng xem Log hệ thống               | `LogController.java`         | **DEBUG**          | "Log monitoring UI initialized. Streaming last \[1000\] lines from file `system.log`."                                                              |
| **UC-06**    | File log hiển thị quá lớn                 | `LogMonitoringService.java`  | **WARN**           | "Log file size \[size\] MB exceeds safe load threshold of 10MB. Loading truncated view (last 1000 lines) to prevent OutOfMemoryError."               |

## 4\. Cơ chế bảo mật thông tin nhạy cảm (Data Masking System)

Để ngăn chặn tuyệt đối việc ghi nhận thông tin nhạy cảm của người dùng xuống tệp tin log thô dưới dạng văn bản thường, hệ thống áp dụng một lớp ghi đè `SecurePatternLayout` kế thừa từ `PatternLayout` của Logback để lọc dữ liệu tự động bằng các biểu thức chính quy (Regex) trước khi ghi xuống ổ đĩa.

### 4.1 Quy tắc làm mờ dữ liệu (Masking Rules)

1.  **Mật khẩu/Token:** Mọi cặp khóa-trị dạng `password=xxx`, `pass=xxx` hoặc chuỗi ký tự mật khẩu thô sẽ được tự động chuyển thành chuỗi `[PROTECTED_PASSWORD]`.
2.  **Email khách hàng nhận thư:** Địa chỉ email khi hiển thị ra log phải làm mờ phần ký tự định danh chính để bảo vệ thông tin riêng tư cá nhân.

### 4.2 Lớp Java xử lý Masking thực tế (`SecurePatternLayout.java`)

Lớp Java thực tế nằm trong gói `nlu.fit.soft.gr5.precisionMail.util` để xử lý chặn ký tự nhạy cảm:

``` java
package nlu.fit.soft.gr5.precisionMail.util;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class SecurePatternLayout extends PatternLayout {
    @Override
    public String doLayout(ILoggingEvent event) {
        return LogSanitizer.sanitize(super.doLayout(event));
    }
}

```

## 5\. Tối ưu hóa hiệu năng & Khả năng chịu tải (Performance & Robustness)

Để đảm bảo hệ thống Log hoạt động mượt mà, không tiêu tốn quá nhiều CPU/RAM của máy trạm và không xung đột với cơ chế đa luồng mới của JDK 21, hệ thống bắt buộc phải áp dụng các tiêu chí vận hành sau:

### 5.1 Giải phóng Thread Pinning khi sử dụng Virtual Threads

Do hệ thống sử dụng Virtual Threads (JDK 21) để thực thi các nghiệp vụ mạng ngầm, việc ghi log trực tiếp xuống file text (I/O ghi đĩa) có thể dẫn đến hiện tượng **Thread Pinning** (Luồng ảo bị kẹt chặt vào luồng vật lý của hệ điều hành do thao tác I/O đồng bộ).

* **Giải pháp xử lý:** Toàn bộ các câu lệnh ghi tệp tin log bắt buộc phải đi qua lớp trung gian **AsyncAppender** của Logback. Lúc này, luồng nghiệp vụ chỉ làm nhiệm vụ đẩy log vào một hàng đợi không chặn (Lock-free Ring Buffer), luồng ghi đĩa thực tế sẽ do một Worker Thread chạy ngầm xử lý độc lập, giúp giải phóng hoàn toàn luồng ảo để tiếp tục gửi email.

### 5.2 Xử lý Stacktrace thông minh

Để tránh việc file log bị phình to đột biến bởi các Stacktrace lỗi dài vô tận của Java, trong tệp cấu hình `logback.xml` chúng ta áp dụng ký hiệu `%rEx` để thiết lập cơ chế giới hạn dòng lỗi. Chỉ in tối đa dòng lỗi tiêu biểu của Exception trừ khi gặp lỗi nghiêm trọng cấp độ FATAL liên quan đến hỏng tệp tin DB hoặc sập luồng hệ thống.
