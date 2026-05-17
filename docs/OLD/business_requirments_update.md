# TÀI LIỆU YÊU CẦU NGHIỆP VỤ (BRD)

## HỆ THỐNG GỬI MAIL TỰ ĐỘNG TRÊN DESKTOP (DESKTOP EMAIL SYSTEM)

**Tiêu chuẩn áp dụng:** Định dạng cấu trúc theo chuẩn IEEE (Khung quản lý yêu cầu)

---

## 1. Giới thiệu (Introduction)

### 1.1 Mục đích (Purpose)

Tài liệu này xác định các yêu cầu nghiệp vụ cốt lõi cho ứng dụng desktop gửi email được phát triển trên nền tảng **JavaFX**. Mục tiêu là cung cấp một công cụ độc lập, hiệu năng cao, cho phép người dùng cấu hình tài khoản cá nhân, soạn thảo, lưu trữ và thiết lập lịch trình gửi email tự động một cách chính xác.

### 1.2 Phạm vi (Scope)

Ứng dụng sẽ hoạt động như một Email Client cục bộ trên môi trường Desktop.

* **Bao gồm:** Giao diện cấu hình giao thức (SMTP/IMAP), trình soạn thảo văn bản, bộ quản lý hàng đợi gửi tin (Queue), cơ sở dữ liệu lưu trữ lịch sử cục bộ và bộ lập lịch (Scheduler).
* **Không bao gồm:** Dịch vụ máy chủ mail riêng (Mail Server), hệ thống quản lý chiến dịch marketing quy mô lớn (Mass Email Marketing) yêu cầu hạ tầng cloud.

### 1.3 Thuật ngữ và Viết tắt (Definitions and Acronyms)

* **JavaFX:** Nền tảng đồ họa và bộ công cụ để phát triển ứng dụng Desktop bằng Java.
* **SMTP (Simple Mail Transfer Protocol):** Giao thức truyền tải thư điện tử đơn giản để gửi mail.
* **IMAP (Internet Message Access Protocol):** Giao thức truy cập thư điện tử để đồng bộ/tải mail về máy.
* **Scheduler:** Bộ tác vụ ngầm chịu trách nhiệm kích hoạt gửi mail theo thời gian định sẵn.

---

## 2. Mô tả tổng quan (General Description)

### 2.1 Bối cảnh sản phẩm (Product Perspective)

Ứng dụng là một phần mềm Desktop độc lập (Standalone Application), tương thích đa nền tảng (Windows, Linux, macOS) nhờ máy ảo Java (JVM). Hệ thống tương tác trực tiếp với các Mail Server phổ biến (Gmail, Outlook, Yahoo) thông qua các giao thức bảo mật tiêu chuẩn.

### 2.2 Các chức năng chính (Product Functions)

* Cấu hình và kết nối tài khoản Email qua giao thức bảo mật.
* Soạn thảo và gửi email (hỗ trợ đính kèm tệp và định dạng HTML).
* Lên lịch trình gửi email tự động theo thời gian cụ thể.
* Tự động lưu trữ email vào danh mục "Đã gửi" (Sent).
* Tra cứu, quản lý và xem lại lịch sử trạng thái của các email.

### 2.3 Đặc điểm người dùng (User Characteristics)

Người dùng là nhân viên văn phòng, quản trị viên hoặc cá nhân có nhu cầu tối ưu hóa quy trình giao tiếp qua email, cần sự riêng tư và bảo mật dữ liệu ngay trên máy tính cá nhân thay vì sử dụng các dịch vụ web bên thứ ba.

### 2.4 Giới hạn và Giả định (Constraints & Assumptions)

* **Giới hạn:** Ứng dụng phụ thuộc vào kết nối Internet của máy trạm và chính sách hạn chế số lượng mail gửi ra (Rate Limit) của từng nhà cung cấp Mail Server (vớ dụ: giới hạn của Gmail).
* **Giả định:** Máy trạm của người dùng đã cài đặt môi trường chạy Java phù hợp (JRE/JDK) hoặc ứng dụng được đóng gói kèm Runtime độc lập.

---

## 3. Yêu cầu chức năng chi tiết (Specific Functional Requirements)

Hệ thống được phân rã thành các nhóm yêu cầu nghiệp vụ cốt lõi sau:

### 3.1 Nhóm yêu cầu: Quản lý Tài khoản & Gửi Mail (Email Core)

* **UR-01 (Cấu hình kết nối):** Hệ thống phải cho phép người dùng thiết lập thông số kết nối bao gồm: SMTP Server, Port, Email, Ứng dụng mã khóa (App Password) và phương thức mã hóa (SSL/TLS).
* **UR-02 (Soạn thảo văn bản):** Hệ thống phải cung cấp giao diện soạn thảo hỗ trợ tiêu đề (Subject), nội dung (Body text/HTML) và đính kèm tệp tin (Attachment) với dung lượng tối đa tùy thuộc vào Mail Server.
* **UR-03 (Xử lý hàng đợi):** Hệ thống phải có cơ chế xếp hàng (Queue) để xử lý gửi email tuần tự, tránh tình trạng ứng dụng bị treo (Freeze UI) khi đang thực hiện tác vụ gửi.

### 3.2 Nhóm yêu cầu: Lên lịch gửi Email (Email Scheduling)

* **UR-04 (Thiết lập thời gian):** Người dùng có thể chọn chính xác ngày, giờ (Gi giờ:Phút, Ngày/Tháng/Năm) để hệ thống tự động kích hoạt gửi thư.
* **UR-05 (Quản lý hàng đợi lịch trình):** Hệ thống phải hiển thị danh sách các email đang "Chờ gửi" (Pending). Người dùng có quyền chỉnh sửa nội dung hoặc hủy bỏ lịch trình trước giờ kích hoạt tối thiểu 1 phút.
* **UR-06 (Xử lý ngầm):** Khi đến thời gian định sẵn, một luồng xử lý ngầm (Background Thread) phải tự động thực thi tác vụ gửi mà không cần sự can thiệp của con người (với điều kiện ứng dụng đang bật).

### 3.3 Nhóm yêu cầu: Lưu trữ & Xem lịch sử (Sent History & Archiving)

* **UR-07 (Tự động lưu trữ):** Ngay sau khi một email được gửi đi (hoặc gửi thất bại), hệ thống phải tự động ghi nhận và lưu thông tin vào cơ sở dữ liệu cục bộ (ví dụ: SQLite hoặc H2 Database).
* **UR-08 (Cấu trúc thông tin lịch sử):** Bản ghi lịch sử phải bao gồm đầy đủ các trường thông tin:
* Người nhận (To/Cc/Bcc)
* Tiêu đề (Subject)
* Thời gian khởi tạo & Thời gian thực tế gửi
* Trạng thái (Thành công / Thất bại / Đang chờ)
* Chi tiết lỗi (nếu có)


* **UR-09 (Bộ lọc và Tìm kiếm):** Người dùng có thể tìm kiếm lịch sử theo từ khóa (Tiêu đề, Người nhận) hoặc lọc theo trạng thái và khoảng thời gian.

---

## 4. Yêu cầu phi chức năng (Non-Functional Requirements)

### 4.1 Hiệu năng (Performance)

* Thời gian phản hồi của giao diện (UI Response Time) không vượt quá 0.5 giây cho các thao tác click, chuyển tab.
* Hệ thống phải xử lý mượt mà tác vụ gửi ngầm mà không chiếm dụng quá 15% năng lực xử lý của CPU và 200MB RAM trên máy trạm tiêu chuẩn.

### 4.2 Bảo mật (Security)

> **Quy định bảo mật bắt buộc:** Ứng dụng không được lưu trữ mật khẩu gốc dưới dạng văn bản thuần (Plaintext). Mọi thông tin cấu hình nhạy cảm như Mật khẩu ứng dụng (App Password) hoặc Token xác thực phải được mã hóa bằng thuật toán mã hóa bất đối xứng hoặc AES trước khi lưu xuống bộ nhớ cục bộ.

### 4.3 Tính khả dụng (Usability)

* Giao diện thiết kế theo ngôn ngữ hiện đại của JavaFX (sử dụng CSS tùy biến), bố cục trực quan, hỗ trợ hiển thị tốt ở các độ phân giải màn hình phổ biến (HD, Full HD).
* Cung cấp thông báo đẩy (Notification) hoặc thanh trạng thái (Status Bar) rõ ràng để người dùng biết tiến độ gửi mail.