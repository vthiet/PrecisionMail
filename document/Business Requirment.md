# BUSINESS REQUIREMENTS DOCUMENT - BRD

**Tên dự án:** Ứng dụng Gửi Email Lên Lịch Độ Trễ Thấp (Low-Latency Scheduled Email Client)  
**Nền tảng công nghệ:** JDK 25, JavaFX, Jakarta Mail  
**Phiên bản:** 1.0  
**Ngày lập:** 22/04/2026

## 1. PROJECT OVERVIEW

Dự án nhằm xây dựng một ứng dụng Desktop (JavaFX) chuyên biệt cho việc gửi email tự động theo lịch trình. Điểm khác biệt cốt lõi của phần mềm là khả năng thiết lập sẵn kết nối đến máy chủ SMTP của Google, giúp việc kích hoạt gửi mail tại thời điểm đã lên lịch diễn ra với độ trễ (latency) thấp nhất có thể. Phần mềm hướng tới sự bảo mật, tiện dụng và hỗ trợ gửi mail hàng loạt.

## 2. FUNCTIONAL REQUIREMENTS - FR

### FR1. Account Management

- **FR1.1. Đăng nhập 1 lần:** Người dùng chỉ cần nhập Email và Google App Password trong lần sử dụng đầu tiên hoặc khi thêm tài khoản mới.
- **FR1.2. Lưu trữ bảo mật:** Ứng dụng phải lưu thông tin đăng nhập vào một tệp (file) local. Dữ liệu trong tệp phải được mã hóa.
- **FR1.3. Chọn đường dẫn lưu file:** Người dùng có quyền chọn thư mục/vị trí trên máy tính để lưu tệp thông tin đăng nhập này.
- **FR1.4. Đa tài khoản:** Ứng dụng cho phép lưu nhiều tài khoản và cung cấp giao diện để người dùng dễ dàng chuyển đổi qua lại (Switch accounts) giữa các tài khoản người gửi.
- **FR1.5. Giới hạn chiều gửi:** Ứng dụng chỉ sử dụng giao thức SMTP để gửi mail, không truy xuất hộp thư đến (No IMAP/POP3).

### FR2. Email Composition

- **FR2.1. Định dạng nội dung:** Chỉ hỗ trợ soạn thảo văn bản thuần túy (Plain Text).
- **FR2.2. Người nhận:** Cung cấp đầy đủ các trường To (Đến), CC (Đồng gửi) và BCC (Gửi ẩn danh).
- **FR2.3. Tệp đính kèm:**  Cho phép đính kèm tệp tin.
    - Quy tắc ràng buộc: Tối đa 10 tệp tin cho mỗi email.
    - Quy tắc ràng buộc: Tổng dung lượng các tệp đính kèm không vượt quá 25MB. Ứng dụng phải cảnh báo ngay khi người dùng chọn file vượt quá giới hạn.

### FR3. Bulk Sending

- **FR3.1. Nhập danh sách:** Hỗ trợ tính năng Import danh sách email người nhận từ tệp văn bản (.txt) hoặc tệp bảng tính (.csv).
- **FR3.2. Phân tích tệp:** Ứng dụng cần đọc và trích xuất hợp lệ các địa chỉ email từ tệp danh sách để nạp vào trường người nhận.

### FR4. Scheduling & Execution

- **FR4.1. Lên lịch:** Cho phép người dùng thiết lập ngày, giờ, phút, giây chính xác để gửi email.
- **FR4.2. Duy trì kết nối:** Khi người dùng bấm "Lên lịch/Chuẩn bị", hệ thống phải tạo sẵn và duy trì một kết nối (Session/Transport) mở tới máy chủ Google SMTP.
- **FR4.3. Kích hoạt trễ thấp:** Đúng thời điểm đã lên lịch, hệ thống đẩy gói dữ liệu đi ngay lập tức trên kết nối đã mở để tối thiểu hóa độ trễ.

### FR5. UI & Status

- **FR5.1. Trạng thái chờ (Loading/Waiting):** Trong thời gian chờ đến lịch gửi, giao diện phải hiển thị trạng thái đang chờ (có thể là đồng hồ đếm ngược hoặc loading spinner).
- **FR5.2. Khóa giao diện:** Khóa các nút gửi/sửa đổi khi tiến trình chờ gửi đã được kích hoạt để tránh gửi trùng lặp.

### FR6. Error Handling & Logging

- **FR6.1. Thông báo lỗi chi tiết:** Nếu gửi thất bại (sai pass, mất mạng, file quá lớn, SMTP server từ chối...), ứng dụng phải hiển thị hộp thoại pop-up giải thích nguyên nhân lỗi một cách chi tiết và dễ hiểu nhất.
- **FR6.2. Ghi Log hệ thống:** Mọi hoạt động (thời điểm đăng nhập, thời điểm tạo lịch, thời điểm gửi thành công/thất bại, nguyên nhân lỗi) phải được ghi vào một tệp log (.log hoặc .txt) lưu trên máy cục bộ để phục vụ việc kiểm tra (traceback).

## 3. NON-FUNCTIONAL REQUIREMENTS - NFR

- **NFR1. Security:** Tệp lưu trữ tài khoản local phải được mã hóa bằng thuật toán chuẩn (ví dụ: AES) để người dùng khác mở file không đọc được App Password.
- **NFR2. Performance:** Đảm bảo không bị timeout kết nối SMTP trong quá trình chờ (cần cơ chế keep-alive nếu thời gian chờ quá lâu). Quá trình đọc file .csv lớn (hàng trăm email) không làm đơ giao diện chính (UI Thread).
- **NFR3. Usability:** Giao diện JavaFX cần trực quan, rõ ràng, phản hồi ngay lập tức các thao tác của người dùng.

## 4. CONSTRAINTS

- Phụ thuộc vào chính sách của Google (Google giới hạn gửi khoảng 500 mail/ngày cho tài khoản cá nhân, cần lưu ý điều này cho tính năng Bulk Send).
- Yêu cầu máy tính người dùng phải cài đặt Java Runtime Environment (JRE) tương thích.
