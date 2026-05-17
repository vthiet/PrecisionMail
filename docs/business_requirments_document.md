# TÀI LIỆU YÊU CẦU NGHIỆP VỤ (BUSINESS REQUIREMENTS BRD DOCUMENT - BRD)

## DỰ ÁN: HỆ THỐNG GỬI MAIL TỰ ĐỘNG TRÊN DESKTOP (DESKTOP EMAIL SYSTEM)

**Mã tài liệu:** BRD-DES-01

**Tiêu chuẩn áp dụng:** IEEE Std 830-1998 / IEEE Std 29148-2018

**Trạng thái:** Phiên bản Cập nhật (Bổ sung Log & Môi trường Kỹ thuật)

## 1\. Giới thiệu (Introduction)

### 1.1 Mục đích (Purpose)

Tài liệu này xác định đầy đủ, chi tiết các yêu cầu nghiệp vụ, yêu cầu chức năng, phi chức năng và môi trường vận hành cho ứng dụng desktop gửi email được phát triển bằng công nghệ **JavaFX**. Tài liệu đóng vai trò là cơ sở cam kết giữa nhóm phát triển, đội kiểm thử (QA/QC) và các bên liên quan.

### 1.2 Phạm vi (Scope)

Hệ thống là một phần mềm Client-side chạy trên máy trạm của người dùng.

* **Bao gồm:** Cấu hình tài khoản, bộ soạn thảo email đa phương thức, bộ lập lịch tác vụ gửi (Scheduler), hệ thống lưu trữ lịch sử cục bộ và hệ thống ghi log vận hành chi tiết.
* **Không bao gồm:** Thiết lập Máy chủ Mail vật lý độc lập (Mail Server hosting).

### 1.3 Thuật ngữ và Viết tắt (Definitions and Acronyms)

* **JDK (Java Development Kit):** Bộ công cụ phát triển phần mềm Java.
* **JVM (Java Virtual Machine):** Máy ảo Java chịu trách nhiệm thông dịch bytecode để thực thi chương trình.
* **JavaFX:** Nền tảng đồ họa và thư viện UI được tích hợp hoặc đi kèm Java để phát triển Desktop App.
* **SLF4J / Logback:** Thư viện trừu tượng hóa và ghi log tiêu chuẩn trong hệ sinh thái Java.
* **SMTP / IMAP:** Giao thức gửi và nhận thư điện tử tương ứng.

## 2\. Mô tả tổng quan & Môi trường kỹ thuật (General Description & Environment)

### 2.1 Bối cảnh sản phẩm (Product Perspective)

Ứng dụng là một phần mềm độc lập (Standalone Application) tương thích đa nền tảng nhờ máy ảo Java (JVM). Ứng dụng tương tác với Mail Server từ xa qua Internet và lưu trữ cấu hình, lịch sử, log tại ổ đĩa cục bộ của máy trạm.

### 2.2 Môi trường phát triển và Môi trường thực thi chi tiết

Để đảm bảo tính nhất quán, hiệu năng ổn định và khả năng tương thích cao nhất, hệ thống được quy định chi tiết về môi trường như sau:

#### 2.2.1 Môi trường Phát triển (Development Environment)

* **Phiên bản JDK:** **Java SE Development Kit (JDK) 21 LTS (Long-Term Support)**.
    * *Lý do chọn:* Hỗ trợ các tính năng hiện đại như Virtual Threads (Project Loom) giúp xử lý đa luồng gửi mail tối ưu, tích hợp sẵn các công cụ đóng gói bản phân phối (jpackage) tự động đính kèm JRE mà không yêu cầu máy khách cài sẵn Java.
* **JavaFX SDK:** **JavaFX 21** (Tương thích đồng bộ với JDK 21).
* **Công cụ xây dựng (Build Tool):** Maven 3.9+ hoặc Gradle 8.x.

#### 2.2.2 Môi trường thực thi của Máy ảo Java (Target JVM Environment)

* **Phiên bản JVM:** **Java Virtual Machine (JVM) 21**.
* **Yêu cầu phân phối:** Ứng dụng phải được đóng gói kèm theo một Runtime JVM thu gọn (nhờ công cụ jlink / jpackage) tương ứng với hệ điều hành đích để người dùng cuối có thể nhấp đúp chạy trực tiếp (Portable/Installer) mà không cần tự cấu hình biến môi trường JAVA\_HOME.

#### 2.2.3 Hệ điều hành tương thích (Target Operating Systems)

Hệ thống bắt buộc phải hoạt động ổn định trên các phiên bản hệ điều hành sau:

* **Hệ điều hành Windows:**
    * **Windows 10 (64-bit, phiên bản 1909 / Build 18363 trở lên)**.
    * **Windows 11 (64-bit)**.
* **Hệ điều hành Linux:**
    * **Ubuntu Desktop 22.04 LTS (Jammy Jellyfish)**.
    * **Ubuntu Desktop 24.04 LTS (Noble Numbat)**.
    * **Debian GNU/Linux 11 / 12 (64-bit)**.
    * *Lưu ý về Linux:* Hệ thống đồ họa yêu cầu máy trạm có hỗ trợ X11 hoặc Wayland và cài đặt thư viện đồ họa cơ bản libgtk-3-0 trở lên để kết xuất giao diện JavaFX.

## 3\. Yêu cầu chức năng chi tiết (Specific Functional Requirements)

### 3.1 Nhóm yêu cầu: Quản lý Tài khoản & Gửi Mail (Email Core)

* **UR-01 (Cấu hình kết nối):** Thiết lập SMTP/IMAP Server, Port, TLS/SSL, Email và App Password (Mã hóa trước khi lưu).
* **UR-02 (Soạn thảo văn bản):** Hỗ trợ rich-text (HTML), đính kèm file (tự động kiểm tra kích thước tối đa 25MB).
* **UR-03 (Hàng đợi gửi):** Sử dụng Task/Service của JavaFX kết hợp ExecutorService để chạy bất đồng bộ, chống đơ giao diện.

### 3.2 Nhóm yêu cầu: Lên lịch gửi Email (Email Scheduling)

* **UR-04 (Thiết lập thời gian):** Lựa chọn chính xác thời điểm gửi thông qua DateTimePicker.
* **UR-05 (Quản lý trạng thái chờ):** Cho phép xem danh sách, cập nhật thông tin hoặc hủy lịch gửi trước thời điểm thực hiện ít nhất 1 phút.
* **UR-06 (Bảo trì lập lịch ngầm):** Cơ chế định thời hoạt động ổn định trên một luồng riêng biệt của JVM (Background Daemon Thread).

### 3.3 Nhóm yêu cầu: Ghi chép Lịch sử (Sent History & Archiving)

* **UR-07 (Tự động lưu trữ):** Ghi nhận tất cả thư đã gửi vào CSDL SQLite/H2 cục bộ.
* **UR-08 (Truy vấn lịch sử):** Hỗ trợ tìm kiếm theo người nhận, tiêu đề, thời gian và trạng thái gửi.

### 3.4 Nhóm yêu cầu: Ghi Log Hệ thống (System Logging & Auditing) - ***Yêu cầu bổ sung mới***

Hệ thống phải có cơ chế ghi nhận nhật ký (Log) chi tiết nhằm hỗ trợ giám sát, chẩn đoán lỗi phần cứng/phần mềm và bảo mật thông tin.

* **UR-09 (Cơ sở hạ tầng ghi Log):** Sử dụng thư viện tiêu chuẩn **SLF4J với Logback** làm nền tảng xử lý Log.
* **UR-10 (Phân cấp cấp độ Log - Log Levels):** Nhật ký phải được phân loại rõ ràng:
    * **INFO:** Ghi nhận sự kiện bình thường của hệ thống (Ví dụ: Ứng dụng khởi động, kết nối Mail Server thành công, tác vụ gửi thư hoàn tất).
    * **WARN:** Các cảnh báo tiềm ẩn lỗi nhưng không làm dừng ứng dụng (Ví dụ: Kết nối mạng chập chờn, vượt ngưỡng hạn mức gửi thư tạm thời).
    * **ERROR:** Các lỗi nghiêm trọng khiến tác vụ thất bại (Ví dụ: Lỗi xác thực tài khoản, không kết nối được DB cục bộ, lỗi định dạng tệp tin đính kèm).
    * **DEBUG:** Ghi lại thông tin chi tiết hỗ trợ lập trình viên gỡ lỗi (chỉ kích hoạt ở môi trường dev).
* **UR-11 (Định dạng dòng Log tiêu chuẩn):** Mỗi dòng log được xuất ra phải tuân thủ đúng định dạng:

```text
[YYYY-MM-DD HH:mm:ss.SSS] [LOG_LEVEL] [THREAD_NAME] [CLASS_NAME:LINE_NUMBER] - MESSAGE [EXCEPTION_STACKTRACE]
```

* **UR-12 (Cơ chế xoay vòng Log - Log Rotation & Storage):**
    * File log phải được lưu trữ tại thư mục cục bộ của ứng dụng: `${user.home}/.precisionmail/logs/system.log` trên Windows và Linux.
    * Áp dụng chính sách xoay vòng: Mỗi tệp log dung lượng tối đa là 10MB. Khi vượt ngưỡng, hệ thống tự động nén thành file .gz và tạo tệp mới.
    * Thời gian lưu trữ log tối đa là 30 ngày hoặc tổng dung lượng bộ nhớ log không quá 200MB. Hệ thống sẽ tự động xóa log cũ nhất khi vượt ngưỡng.
* **UR-13 (Nội dung ghi log bảo mật):** **Tuyệt đối nghiêm cấm** ghi trực tiếp mật khẩu, mã PIN hoặc nội dung nhạy cảm của email vào tệp log dưới dạng văn bản thường (Plaintext). Mật khẩu chỉ được phép hiển thị dưới dạng chuỗi đã được che giấu (\*\*\*\*\*\*\*\*).

## 4\. Yêu cầu phi chức năng (Non-Functional Requirements)

### 4.1 Hiệu năng (Performance)

* Khởi động ứng dụng và hiển thị màn hình chính trong vòng dưới 3 giây.
* Xử lý đa luồng (Multi-threading) bằng cách sử dụng Virtual Threads của Java 21 giúp hệ thống có thể quản lý đồng thời lên tới 100 lịch trình gửi thư song song mà không gây quá tải tài nguyên máy trạm.

### 4.2 Bảo mật (Security)

* Toàn bộ dữ liệu nhạy cảm trong CSDL (như App Password) phải được mã hóa bằng thuật toán **AES-256** với khóa được tạo động dựa trên thông số phần cứng của máy trạm (Hardware-bound encryption).

### 4.3 Khả năng phục hồi và Dự phòng lỗi (Reliability & Robustness)

* **Ghi nhận sự cố (Crash Logs):** Trong trường hợp ứng dụng bị dừng đột ngột (Fatal Error / Out of Memory), JVM phải được cấu hình để sinh file Dump hoặc log lỗi hệ thống ra tệp riêng (hs\_err\_pid.log) để phục vụ công tác sửa lỗi.
* Khi kết nối Internet bị mất giữa chừng, Scheduler phải tạm dừng hàng đợi, đánh dấu trạng thái "Chờ kết nối lại" (Retry Pending) thay vì hủy bỏ tác vụ gửi của người dùng.

## 5\. Ma trận kiểm soát yêu cầu (Requirement Traceability Matrix - RTM)

| ID Yêu cầu | Loại yêu cầu | Mô tả tóm tắt         | Độ ưu tiên | Môi trường liên quan            |
| :--------: | :----------: | :-------------------: | :--------: | :-----------------------------: |
| **UR-01**  | Chức năng    | Cấu hình tài khoản    | Cao        | JDK 21 / JVM 21                 |
| **UR-03**  | Chức năng    | Gửi mail phi block UI | Cao        | JDK 21 / JavaFX                 |
| **UR-06**  | Chức năng    | Lập lịch ngầm         | Trung bình | JVM 21 Background Threads       |
| **UR-10**  | Chức năng    | Ghi log phân cấp      | Cao        | Logback Library / Local Storage |
| **UR-12**  | Chức năng    | Xoay vòng Log         | Trung bình | Windows & Linux File Systems    |
