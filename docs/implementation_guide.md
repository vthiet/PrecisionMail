# HƯỚNG DẪN CẤU TRÚC VÀ TRÌNH BÀY PHẦN HIỆN THỰC MÃ NGUỒN (IMPLEMENTATION GUIDE)

Phần **Hiện thực (Implementation)** trong một báo cáo kỹ thuật theo chuẩn IEEE không đơn thuần là việc "copy-paste" mã nguồn vào tài liệu. Mục tiêu của phần này là chứng minh rằng bạn đã **chuyển đổi thành công các sơ đồ thiết kế (Tài liệu kiến trúc, Sơ đồ trình tự - Sequence, Sơ đồ lớp - Class Diagram) thành sản phẩm chạy được thực tế** một cách tối ưu và an toàn.

Dưới đây là 5 phần cốt lõi bạn bắt buộc phải trình bày trong chương này:

## 1\. Bản đồ ánh xạ Kiến trúc sang Cấu trúc thư mục (Class-to-Package Mapping)

Hội đồng chấm điểm luôn muốn thấy sự ngăn nắp, khoa học và tính module trong dự án của bạn. Bạn cần trình bày:

* **Sơ đồ cây thư mục (Project Directory Tree):** Thể hiện rõ việc phân rã các package theo mô hình kiến trúc phân tầng (Layered Architecture hoặc MVC).
* **Bảng ánh xạ (Mapping Table):** Đối soát trực tiếp các thực thể (Objects/Participants) trong sơ đồ trình tự (Sequence Diagram) tương ứng với các File/Class Java nào trong thực tế.

**Ví dụ minh họa:**

> "Để hiện thực hóa Sơ đồ trình tự của UC-01, hệ thống phân tách trách nhiệm thành các tệp tin mã nguồn rõ ràng trong gói com.emailsystem: ConfigView.fxml phụ trách hiển thị UI, ConfigController.java phụ trách bắt sự kiện giao diện, và CryptoService.java xử lý mã hóa bảo mật."

## 2\. Đặc tả các giải pháp công nghệ cốt lõi (Core Tech Implementation)

Đây là phần quan trọng nhất của chương. Bạn không cần đưa toàn bộ hàng nghìn dòng code vào, mà chỉ trích xuất các đoạn mã **thể hiện tư duy công nghệ vượt trội hoặc giải quyết các yêu cầu phi chức năng (NFR) cốt lõi**.

Đối với dự án Gửi mail JavaFX này, bạn cần tập trung viết sâu vào 3 giải pháp công nghệ sau:

### 2.1 Hiện thực giải pháp Bất đồng bộ (Asynchronous Blocking-free I/O)

* **Nội dung trình bày:** Giải thích cách bạn sử dụng **Virtual Threads (JDK 21)** hoặc JavaFX Task để đưa tác vụ kết nối mạng Socket (SMTP/IMAP) xuống nền ngầm.
* **Đoạn code minh họa:** Trích xuất đoạn khởi tạo Executors.newVirtualThreadPerTaskExecutor() và cách submit tác vụ kết nối mạng.
* **Lợi ích kỹ thuật:** UI luôn duy trì ở mức tần số quét ổn định $60 \text{ FPS}$, không gây hiện tượng đóng băng màn hình (Not Responding).

### 2.2 Hiện thực cơ chế Bảo mật liên kết phần cứng (Hardware-Bound Storage)

* **Nội dung trình bày:** Giải thích cách hệ thống bảo mật tệp CSDL SQLite cục bộ. Cách kết hợp thông số CPU ID, Motherboard UUID qua hàm băm $PBKDF2$ để sinh khóa đối xứng $AES\text{-}256$.
* **Đoạn code minh họa:** Hàm đọc thông số OS Command để lấy định danh phần cứng và hàm mã hóa mật khẩu ứng dụng.
* **Lợi ích kỹ thuật:** Chứng minh tệp DB SQLite nếu bị sao chép sang máy khác sẽ hoàn toàn vô hiệu.

### 2.3 Cơ chế định thời ngầm (Scheduler Engine Execution)

* **Nội dung trình bày:** Cách Scheduler Daemon hoạt động trên JVM để quét và kích hoạt email đến giờ G.
* **Đoạn code minh họa:** Hàm loop chạy ngầm đối chiếu thời gian hệ thống và cách tự giải phóng luồng khi tắt ứng dụng (Thread.setDaemon(true)).

## 3\. Hiện thực Cơ sở dữ liệu và Thư mục hệ thống (Database & Bootstrapping)

Bạn cần trình bày cách ứng dụng tự cấu trúc và khởi tạo khi người dùng mở ứng dụng lần đầu tiên (First-time initialization).

* **Thư mục lưu trữ tĩnh:** Chỉ rõ đường dẫn động theo từng Hệ điều hành (Sử dụng %APPDATA% trên Windows và \~/.config/ trên Linux).
* **Kịch bản SQL khởi tạo (Initial Schema DDL):** Trình bày bảng dữ liệu thực tế (bảng cấu hình, bảng hàng đợi, bảng lịch sử) kèm theo các ràng buộc dữ liệu (CHECK constraints) và chỉ mục tìm kiếm nhanh (INDEX).

## 4\. Các rào cản kỹ thuật và Giải pháp khắc phục (Hurdles & Resolutions)

Một báo cáo đạt điểm xuất sắc luôn có phần tự đánh giá về các khó khăn kỹ thuật phát sinh trong quá trình code thực tế và cách bạn đã vượt qua chúng. Điều này thể hiện kinh nghiệm thực chiến của người phát triển.

**Các rào cản tiêu biểu bạn có thể đưa vào báo cáo:**

1.  **Thread Pinning khi sử dụng Virtual Threads:** Thao tác ghi log đĩa cứng (đồng bộ) gây kẹt luồng vật lý của JVM.

    * *Giải pháp:* Tích hợp bộ bọc bất đồng bộ AsyncAppender của Logback để đưa I/O log vào một hàng đợi không chặn trên RAM.

2.  **Rò rỉ bộ nhớ (Memory Leak) từ WebView của JavaFX:** Thành phần kết xuất HTML tiêu thụ quá nhiều RAM sau nhiều lần mở xem chi tiết thư.

    * *Giải pháp:* Thực hiện gọi lệnh dọn dẹp, hủy tham chiếu đối tượng và gợi ý thu hồi bộ nhớ System.gc() ngay khi người dùng đóng popup.

3.  **Lỗi không tương thích định danh phần cứng trên môi trường Linux Sandbox:** Một số bản phân phối Linux chặn quyền chạy lệnh dmidecode lấy UUID.

    * *Giải pháp:* Viết cơ chế dự phòng (Fallback logic) lấy chuỗi Username của OS kết hợp kiến trúc chip (os.arch) làm định danh an toàn.

## 5\. Kết luận phần Hiện thực (Implementation Verdict)

Tóm tắt lại các chỉ số đạt được của sản phẩm sau khi đã hiện thực mã nguồn thành công:

* **Mức độ hoàn thành:** Đã hiện thực hóa bao nhiêu % các yêu cầu trong tài liệu BRD.
* **Độ bao phủ kiểm thử (Test Coverage):** Mã nguồn đã chạy thử nghiệm mượt mà trên các môi trường nào (Windows 10/11, Ubuntu 22.04 LTS).
* **Đánh giá hiệu năng thực tế:** Ứng dụng chạy mượt mà, RAM tiêu hao thực tế dưới mức $200\text{ MB}$ ngay cả khi xử lý các tệp đính kèm lớn sát ngưỡng giới hạn $25\text{ MB}$.
