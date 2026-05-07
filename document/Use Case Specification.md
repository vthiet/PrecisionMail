# System Use Cases & Specifications
**Dự án:** Ứng dụng Gửi Email Lên Lịch Độ Trễ Thấp (Low-Latency Scheduled Email Client)

## 1. Use Case Tổng (Overall Use Case Diagram)

Dưới đây là biểu đồ Use Case tổng thể của hệ thống, thể hiện các tương tác chính của người dùng với ứng dụng.

```mermaid
flowchart LR

    User([User])

    subgraph PrecisionMail_Client["PrecisionMail Client"]

        UC1([UC01: Đăng nhập / Thêm tài khoản])
        UC2([UC02: Chuyển đổi tài khoản])
        UC3([UC03: Soạn thảo Email])
        UC4([UC04: Import danh sách Bulk])
        UC5([UC05: Lên lịch gửi])
        UC6([UC06: Ghi Log & Xử lý lỗi])

        UC4 -. extend .-> UC3
        UC5 -. include .-> UC3
        UC1 -. include .-> UC6
        UC5 -. include .-> UC6
    end

    User --> UC1
    User --> UC2
    User --> UC3
    User --> UC4
    User --> UC5
```

---

## 2. Đặc tả chi tiết và Sequence Diagram

### UC01: Đăng nhập / Thêm tài khoản (Add Account)

**1. Đặc tả**
- **Use Case ID:** UC01
- **Use Case Name:** Đăng nhập / Thêm tài khoản
- **Actor(s):** User
- **Priority:** High
- **Trigger:** Người dùng mở ứng dụng lần đầu hoặc chọn tính năng "Thêm tài khoản mới".
- **Pre-Condition:** Ứng dụng đang chạy bình thường.
- **Post-Condition:** Thông tin tài khoản được mã hóa và lưu trữ an toàn dưới dạng file. Tài khoản được thêm vào danh sách sẵn sàng sử dụng.
- **Basic Flow:**
  1. Người dùng nhập Email và Google App Password.
  2. Người dùng chỉ định đường dẫn lưu file thông tin trên máy tính.
  3. Người dùng bấm xác nhận.
  4. Hệ thống kiểm tra tính hợp lệ của định dạng Email và độ dài Password.
  5. Hệ thống mã hóa thông tin.
  6. Hệ thống lưu tệp thông tin xuống đường dẫn đã chọn.
  7. Hệ thống thông báo thành công và thêm tài khoản vào danh sách.
- **Alternative Flow:**
  - *File đã tồn tại:* Ở bước 6, nếu tại đường dẫn đã có file thông tin, hệ thống hỏi người dùng có muốn ghi đè hoặc thêm vào danh sách hiện tại không.
- **Exception Flow:**
  - *Thiếu quyền ghi:* Nếu hệ thống không có quyền ghi file tại đường dẫn, hiển thị lỗi "Access Denied" và yêu cầu chọn thư mục khác.
- **Business Rules:** (FR1.1) Chỉ yêu cầu nhập App Password, không hỗ trợ mật khẩu thường. (FR1.5) Chỉ xác thực SMTP, không quan tâm IMAP/POP3.
- **Non-Functional Requirement:** (NFR1) Dữ liệu lưu xuống file phải được mã hóa chuẩn (ví dụ: AES) để đảm bảo bảo mật.

**2. Sequence Diagram**
```mermaid
sequenceDiagram
    actor User
    participant UI
    participant AuthController
    participant CryptoUtils
    participant FileIO
    
    User->>UI: Nhập Email, App Password, Path
    User->>UI: Click "Lưu tài khoản"
    UI->>AuthController: validate(email, pass, path)
    
    alt Validation Failed
        AuthController-->>UI: Lỗi định dạng/Thiếu thông tin
        UI-->>User: Hiển thị lỗi
    else Validation Passed
        AuthController->>CryptoUtils: encrypt(App Password)
        CryptoUtils-->>AuthController: Encrypted Password
        AuthController->>FileIO: saveToFile(email, encryptedPass, path)
        
        alt Có lỗi File IO (Quyền truy cập)
            FileIO-->>AuthController: IOException
            AuthController-->>UI: Hiển thị lỗi Access Denied
            UI-->>User: Cảnh báo chọn thư mục khác
        else Thành công
            FileIO-->>AuthController: Success
            AuthController-->>UI: Tài khoản được thêm
            UI-->>User: Thông báo thành công
        end
    end
```

---

### UC02: Chuyển đổi tài khoản (Switch Account)

**1. Đặc tả**
- **Use Case ID:** UC02
- **Use Case Name:** Chuyển đổi tài khoản
- **Actor(s):** User
- **Priority:** Medium
- **Trigger:** Người dùng muốn gửi email từ một địa chỉ khác đã lưu trong hệ thống.
- **Pre-Condition:** Hệ thống đã lưu ít nhất 2 tài khoản hợp lệ.
- **Post-Condition:** Phiên làm việc hiện tại sử dụng thông tin của tài khoản mới được chọn.
- **Basic Flow:**
  1. Người dùng mở danh sách tài khoản thả xuống (Dropdown/List).
  2. Người dùng chọn tài khoản muốn sử dụng.
  3. Hệ thống tiến hành đọc file của tài khoản đó, giải mã lấy thông tin.
  4. Hệ thống cập nhật tài khoản đang active trên giao diện.
- **Alternative Flow:** None.
- **Exception Flow:**
  - *Lỗi giải mã/Mất file:* Nếu file bị xóa hoặc bị sửa đổi trái phép (giải mã thất bại), hệ thống báo lỗi và yêu cầu đăng nhập lại tài khoản đó.
- **Business Rules:** (FR1.4) Ứng dụng hỗ trợ đa tài khoản để chuyển đổi nhanh chóng.
- **Non-Functional Requirement:** Quá trình giải mã và chuyển đổi phải diễn ra mượt mà, không block UI chính.

**2. Sequence Diagram**
```mermaid
sequenceDiagram
    actor User
    participant UI
    participant AccountManager
    participant CryptoUtils
    participant FileIO
    
    User->>UI: Chọn tài khoản B từ danh sách
    UI->>AccountManager: switchAccount(accountId)
    AccountManager->>FileIO: readFile(accountPath)
    FileIO-->>AccountManager: Encrypted Data
    AccountManager->>CryptoUtils: decrypt(Encrypted Data)
    
    alt Giải mã thất bại (Corrupt file)
        CryptoUtils-->>AccountManager: DecryptionException
        AccountManager-->>UI: Thông báo file hỏng, yêu cầu đăng nhập lại
        UI-->>User: Cảnh báo
    else Giải mã thành công
        CryptoUtils-->>AccountManager: Plain App Password
        AccountManager->>AccountManager: Set Active Account
        AccountManager-->>UI: Cập nhật giao diện (Active = B)
        UI-->>User: Chuyển tài khoản thành công
    end
```

---

### UC03: Soạn thảo Email (Compose Email)

**1. Đặc tả**
- **Use Case ID:** UC03
- **Use Case Name:** Soạn thảo Email
- **Actor(s):** User
- **Priority:** High
- **Trigger:** Người dùng nhập các trường thông tin trên màn hình chính.
- **Pre-Condition:** Đã có tài khoản active.
- **Post-Condition:** Các thông tin gửi (To, CC, BCC, Subject, Content, Attachments) hợp lệ và sẵn sàng.
- **Basic Flow:**
  1. Người dùng nhập địa chỉ vào trường To, CC, BCC.
  2. Người dùng điền Subject và nội dung Content (Plain text).
  3. Người dùng chọn chức năng đính kèm tệp và duyệt tệp từ máy tính.
  4. Hệ thống kiểm tra dung lượng và số lượng tệp.
  5. Tệp được thêm vào danh sách hiển thị trên giao diện.
- **Alternative Flow:**
  - Người dùng xóa tệp đã đính kèm khỏi danh sách.
- **Exception Flow:**
  - Nếu tổng dung lượng > 25MB hoặc số file > 10, hệ thống từ chối file đó và hiển thị Pop-up cảnh báo ngay lập tức.
- **Business Rules:** (FR2.1) Nội dung chỉ là Plain Text. (FR2.3) Tối đa 10 tệp, tổng dung lượng <= 25MB.
- **Non-Functional Requirement:** Dung lượng tệp được tính toán nhanh, không load toàn bộ tệp vào RAM khi kiểm tra size.

**2. Sequence Diagram**
```mermaid
sequenceDiagram
    actor User
    participant UI
    participant AttachmentValidator
    
    User->>UI: Nhập văn bản (To, Subject, Content)
    User->>UI: Chọn tệp đính kèm (File A)
    UI->>AttachmentValidator: validate(File A, CurrentFilesList)
    
    alt Số lượng > 10 HOẶC Tổng Size > 25MB
        AttachmentValidator-->>UI: Return False (Kèm lý do)
        UI-->>User: Pop-up cảnh báo dung lượng/số lượng
    else Hợp lệ
        AttachmentValidator-->>UI: Return True
        UI->>UI: Cập nhật danh sách đính kèm
        UI-->>User: Hiển thị File A trên UI
    end
```

---

### UC04: Import danh sách (Bulk Import)

**1. Đặc tả**
- **Use Case ID:** UC04
- **Use Case Name:** Import danh sách người nhận
- **Actor(s):** User
- **Priority:** Medium
- **Trigger:** Người dùng click nút "Import danh sách" trên giao diện.
- **Pre-Condition:** Không có.
- **Post-Condition:** Các địa chỉ email hợp lệ từ tệp được nạp vào trường To hoặc BCC.
- **Basic Flow:**
  1. Người dùng chọn file (.txt hoặc .csv) từ máy tính.
  2. Hệ thống phân tích (parse) nội dung file trên Background Thread.
  3. Trích xuất tất cả các chuỗi có định dạng email hợp lệ.
  4. Lọc trùng lặp.
  5. Điền kết quả vào trường văn bản tương ứng trên giao diện.
- **Alternative Flow:**
  - Người dùng hủy việc chọn file ở hộp thoại.
- **Exception Flow:**
  - Nếu file rỗng hoặc không tìm thấy bất kỳ email hợp lệ nào, thông báo lỗi: "Không tìm thấy dữ liệu hợp lệ".
- **Business Rules:** (FR3.1) Hỗ trợ .txt, .csv. (FR3.2) Cần lọc và validate định dạng email.
- **Non-Functional Requirement:** (NFR2) File lớn (hàng trăm dòng) không được làm đơ UI, quá trình parse phải chạy dưới nền (Task/Thread).

**2. Sequence Diagram**
```mermaid
sequenceDiagram
    actor User
    participant UI
    participant CSVParser (Background)
    
    User->>UI: Click Import & Chọn File
    UI->>UI: Hiển thị Loading...
    UI->>CSVParser: Bắt đầu đọc file (Async)
    CSVParser->>CSVParser: Đọc từng dòng & Regex trích xuất
    CSVParser->>CSVParser: Lọc trùng lặp
    
    alt File trống / Không có Email
        CSVParser-->>UI: Danh sách rỗng
        UI-->>User: Báo lỗi "Không tìm thấy email"
    else Có Email hợp lệ
        CSVParser-->>UI: List<String> emails
        UI->>UI: Cập nhật Textbox (To/BCC)
        UI-->>User: Thông báo Import thành công & Cập nhật UI
    end
```

---

### UC05: Lên lịch gửi (Schedule & Low-Latency Send)

**1. Đặc tả**
- **Use Case ID:** UC05
- **Use Case Name:** Lên lịch gửi trễ thấp
- **Actor(s):** User, System Time
- **Priority:** Critical
- **Trigger:** Người dùng ấn nút "Lên lịch/Chuẩn bị" sau khi chọn ngày giờ.
- **Pre-Condition:** Nội dung email hợp lệ (Có người nhận, tiêu đề), tài khoản active đang sẵn sàng, kết nối mạng bình thường.
- **Post-Condition:** Email được gửi đi thành công với độ trễ thấp nhất tại đúng thời điểm đã định.
- **Basic Flow:**
  1. Người dùng chọn ngày, giờ chính xác và ấn "Chuẩn bị gửi".
  2. Hệ thống khóa các nút thao tác trên UI, hiển thị đếm ngược (Waiting).
  3. Hệ thống tạo kết nối (Session) tới Google SMTP Server ngay lập tức và xác thực (Pre-connect).
  4. Hệ thống duy trì kết nối (Keep-alive) nếu cần.
  5. Khi giờ hệ thống khớp với giờ đã lên lịch, hệ thống đẩy MimeMessage (payload) đi ngay lập tức.
  6. Nhận phản hồi thành công, đóng kết nối, mở khóa UI và báo thành công.
- **Alternative Flow:**
  - Người dùng bấm "Hủy lệnh" trước khi đến giờ. Kết nối bị đóng, giao diện mở khóa.
- **Exception Flow:**
  - Lỗi kết nối ban đầu (Sai pass, block cổng 587): Báo lỗi ngay lập tức ở bước 3, không đưa vào trạng thái chờ.
  - Lỗi đứt mạng trong lúc chờ: Ghi log, thử retry, nếu thất bại báo Pop-up (FR6.1).
- **Business Rules:** (FR4.2) Phải tạo sẵn kết nối để đạt (FR4.3) độ trễ thấp nhất. (FR5.2) Khóa UI để tránh thao tác sai.
- **Non-Functional Requirement:** (NFR2) Không bị timeout kết nối SMTP. Phản ứng gửi (trigger) phải ngay lập tức tính bằng mili-giây.

**2. Sequence Diagram**
```mermaid
sequenceDiagram
    actor User
    participant UI
    participant Scheduler
    participant SMTPTransport
    participant GoogleServer
    
    User->>UI: Chọn giờ & Bấm "Lên lịch"
    UI->>UI: Khóa UI, Hiện đếm ngược
    UI->>Scheduler: scheduleJob(emailData, triggerTime)
    
    Scheduler->>SMTPTransport: Pre-connect (Tạo Session)
    SMTPTransport->>GoogleServer: Connect & Authenticate
    
    alt Kết nối ban đầu lỗi (Sai Pass, No Internet)
        GoogleServer-->>SMTPTransport: Auth Failed / Timeout
        SMTPTransport-->>Scheduler: Exception
        Scheduler-->>UI: Hủy lệnh
        UI-->>User: Báo lỗi & Mở khóa UI
    else Pre-connect thành công
        GoogleServer-->>SMTPTransport: Connected
        
        loop Đợi đến triggerTime
            Scheduler->>Scheduler: Tick (Kiểm tra thời gian)
        end
        
        Scheduler->>SMTPTransport: Kích hoạt!
        SMTPTransport->>GoogleServer: Send Message Payload
        
        alt Gửi thành công
            GoogleServer-->>SMTPTransport: 250 OK
            SMTPTransport->>SMTPTransport: Close Transport
            SMTPTransport-->>Scheduler: Success
            Scheduler-->>UI: Hoàn tất
            UI-->>User: Hiện "Gửi thành công", Mở UI
        else Lỗi kết nối giữa chừng
            GoogleServer-->>SMTPTransport: Exception/Disconnect
            SMTPTransport-->>Scheduler: Lỗi truyền tải
            Scheduler-->>UI: Lỗi
            UI-->>User: Hiện Pop-up lỗi chi tiết
        end
    end
```
