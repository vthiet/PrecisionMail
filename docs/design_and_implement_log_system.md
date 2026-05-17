# TÀI LIỆU THIẾT KẾ VÀ TRIỂN KHAI CHI TIẾT HỆ THỐNG GHI LOG (LOGBACK ENGINE)

**Mã tài liệu:** LOG-IMPL-SPEC-01
<br>
**Tiêu chuẩn áp dụng:** IEEE Std 830-1998 / IEEE Std 29148-2018\
<br>
**Trạng thái:** Đã đồng bộ theo triển khai hiện tại

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

## 3\. Ma trận ghi nhận Log theo triển khai hiện tại (Logging Matrix)

Dưới đây là ma trận hiện trạng của mã nguồn. Các message có thể chứa tham số động `{}` và được Logback render theo pattern chuẩn trong `logback.xml`.

| Mã Use Case  | Ngữ cảnh ghi Log (When)                            | Lớp thực thi (Where)                              | Cấp độ Log | Nội dung/nhóm thông điệp thực tế |
| :----------: | :------------------------------------------------: | :-----------------------------------------------: | :--------: | :------------------------------- |
| **Hệ thống** | Khởi chạy ứng dụng, bootstrap, lỗi không bắt được   | `Launcher.java`                                   | INFO/ERROR/WARN | `Application bootstrap started.`, `Application started successfully.`, `Unhandled runtime exception on thread={}`, `Application close rejected because an email send is still active.` |
| **Hệ thống** | Khởi tạo và migration CSDL                         | `DatabaseInitializer.java`, `DbUtil.java`         | INFO/DEBUG/ERROR | `Database initialization completed successfully.`, `Migrated accounts table to current schema.`, `Opened SQLite connection to {}.`, `Failed to open SQLite connection to {}.` |
| **Điều hướng UI** | Chuyển view, mở dialog cấu hình tài khoản      | `NavigationService.java`, `MainScreenController.java`, `MenuBarController.java`, `SideBarController.java` | INFO/ERROR | `Navigation requested to {}.`, `Center view changed to {}.`, `Failed to change center view to {}.` |
| **UC-01**    | Kiểm tra, lưu, tải cấu hình tài khoản mail          | `AddAccountDialogController.java`, `AccountServiceImpl.java`, `AccountDaoImpl.java`, `EmailUtil.java` | INFO/WARN/ERROR/DEBUG | `Mail-server test requested for username={}.`, `Mail-server test completed successfully for username={}.`, `Mail-server authentication failed for username={}.`, `Account configuration save requested for username={}.`, `Loaded {} account(s) from database.` |
| **UC-02**    | Soạn và gửi email ngay, kiểm tra người nhận/tệp đính kèm | `ComposeController.java`, `EmailServiceImpl.java`, `EmailDaoImpl.java`, `EmailUtil.java` | INFO/WARN/ERROR/DEBUG | `Send email rejected because recipient list is empty.`, `Attachment accepted. size={} bytes, totalAttachments={}.`, `Attachment rejected by client-side validation.`, `Email send requested. sender={}, recipients={}, attachments={}.`, `Email sent successfully. sender={}, recipients={}, attachments={}.` |
| **UC-03**    | Lên lịch gửi, bootstrap scheduler, retry khi gửi lỗi | `ComposeController.java`, `ScheduledEmailServiceImpl.java`, `ScheduledEmailDaoImpl.java` | INFO/WARN/ERROR | `Schedule email rejected because scheduled time is less than 60 seconds ahead.`, `Scheduled email created. taskId={}, sender={}, scheduledAt={}, recipients={}, attachments={}.`, `Scheduler bootstrap completed. pendingRecordCount={}.`, `Scheduled email network failure; retry registered. taskId={}, retryCount={}/{}.`, `Scheduled email send failed permanently. taskId={}.` |
| **UC-04**    | Xem, hủy, cập nhật hàng đợi gửi                     | `QueueController.java`, `QueueServiceImpl.java`, `ScheduledEmailServiceImpl.java` | INFO/WARN/ERROR | `Queue task cancellation failed. taskId={}.`, `Queue task update failed. taskId={}.`, `Queue loading failed.`, `Scheduled email cancelled. taskId={}.`, `Scheduled email rescheduled. taskId={}, newScheduledAt={}.` |
| **UC-05**    | Tra cứu lịch sử, xem chi tiết, xuất CSV             | `HistoryMailController.java`, `HistoryServiceImpl.java`, `EmailDaoImpl.java` | INFO/ERROR/WARN | `User queried history. Filter params: [Keyword Length: {}, Range: {} to {}]. Results returned: [{}] rows. Duration: [{}] ms`, `History CSV export failed.`, `History query failed.`, `History detail query failed. id={}.` |
| **UC-06**    | Mở màn hình log, đọc/lọc log, watch realtime, mở thư mục, xuất ZIP | `LogController.java`, `LogMonitoringServiceImpl.java` | DEBUG/WARN/ERROR | `Log monitoring UI initialized. Streaming last [{}] lines from file [{}].`, `Failed to read active log file.`, `Failed to filter active log file.`, `Failed to open log directory [{}].`, `Log export failed.`, `Could not start active log watcher.`, `Log file size [{}] MB exceeds safe load threshold of 10MB. Loading truncated view (last [{}] lines) to prevent OutOfMemoryError.` |
| **Thông báo UI** | Hiển thị alert lỗi/thông tin                  | `AlertUtil.java`                                  | WARN/INFO | `Showing error alert. title={}.`, `Showing info alert. title={}.` |

## 4\. Cơ chế bảo mật thông tin nhạy cảm (Data Masking System)

Để ngăn chặn tuyệt đối việc ghi nhận thông tin nhạy cảm của người dùng xuống tệp tin log thô dưới dạng văn bản thường, hệ thống áp dụng một lớp ghi đè `SecurePatternLayout` kế thừa từ `PatternLayout` của Logback để lọc dữ liệu tự động bằng các biểu thức chính quy (Regex) trước khi ghi xuống ổ đĩa.

### 4.1 Quy tắc làm mờ dữ liệu (Masking Rules)

1.  **Mật khẩu/Token:** Mọi cặp khóa-trị dạng `password=xxx`, `app password=xxx`, `appPassword=xxx`, `pass=xxx`, `token=xxx` hoặc `secret=xxx` với dấu phân cách `=` hoặc `:` sẽ được tự động chuyển thành chuỗi `[PROTECTED_PASSWORD]`.
2.  **Email khách hàng nhận/gửi:** Địa chỉ email khi ghi file và khi hiển thị ra UI được làm mờ theo quy tắc giữ tối đa 1-3 ký tự đầu phần local-part và thay phần còn lại bằng `***`, ví dụ `recipient@example.com` thành `rec***@example.com`.

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

Triển khai hiện tại dùng `%ex` trong pattern:

```text
[%d{yyyy-MM-dd HH:mm:ss.SSS}] [%level] [%thread] [%class:%line] - %msg%n%ex
```

Cách này ghi đầy đủ stacktrace khi logger nhận `Throwable`, phù hợp với mục tiêu hỗ trợ chẩn đoán lỗi trên máy trạm. Giới hạn tăng trưởng file được kiểm soát bằng `SizeAndTimeBasedRollingPolicy` với `maxFileSize=10MB`, `maxHistory=30` và `totalSizeCap=200MB`, thay vì cắt ngắn stacktrace bằng `%rEx`.
