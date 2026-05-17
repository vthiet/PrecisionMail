# TÀI LIỆU PHÂN TÍCH & THIẾT KẾ (ANALYSIS & DESIGN DOCUMENT)

## 1. Use Case Diagram tổng thể 
```mermaid
graph LR
%% Định nghĩa các Actor
    actor_user["👤 Người dùng"]
    actor_server["🖥️ Mail Server<br>(SMTP/IMAP)"]

%% System Boundary
    subgraph boundary ["Hệ thống Gửi Mail Desktop)"]
        uc_config(["UC-01: Cấu hình kết nối<br>Mail Server"])
        uc_send(["UC-02: Soạn thảo và Gửi Email"])
        uc_schedule(["UC-03: Lên lịch gửi Email"])
        uc_queue(["UC-04: Quản lý hàng đợi Email"])
        uc_history(["UC-05: Tra cứu lịch sử gửi"])
        uc_logs(["UC-06: Theo dõi Log hệ thống"])
    end

%% Kết nối Actor -> UC
    actor_user --- uc_config
    actor_user --- uc_send
    actor_user --- uc_schedule
    actor_user --- uc_queue
    actor_user --- uc_history
    actor_user --- uc_logs

%% Kết nối UC -> Hệ thống bên ngoài
    uc_config --- actor_server
    uc_send --- actor_server
    uc_schedule --- actor_server
```

## 2. System Architecture

```mermaid
graph TD
    %% Định nghĩa Style chung
    classDef presentation fill:#f9f9f9,stroke:#333,stroke-width:2px;
    classDef business fill:#dae8fc,stroke:#6c8ebf,stroke-width:2px;
    classDef data fill:#d5e8d4,stroke:#82b366,stroke-width:2px;
    classDef external fill:#fff2cc,stroke:#d6b656,stroke-width:2px;

    %% TẦNG GIAO DIỆN VÀ ĐIỀU KHIỂN (PRESENTATION & CONTROLLER LAYER)
    subgraph Presentation_Layer [Tầng Giao diện & Điều khiển - JavaFX]
        UI[JavaFX UI: FXML, CSS, Views<br/>MainView, ConfigView, HistoryView]
        Ctrl[JavaFX Controllers<br/>Xử lý sự kiện UI & Điều hướng]
        UI <--> |Binding / Events| Ctrl
    end
    class Presentation_Layer presentation;

    %% TẦNG NGHIỆP VỤ LÕI (CORE BUSINESS LOGIC LAYER)
    subgraph Business_Layer [Tầng Nghiệp vụ Lõi - JDK 21 Ecosystem]
        direction TB
        VT_Executor{Virtual Threads<br/>Executor - JEP 444}
        
        Email_Svc[Email Service<br/>Cấu hình & Kết nối]
        Sched_Svc[Scheduler Service<br/>Lập lịch tác vụ ngầm]
        Crypto_Svc[Crypto Service<br/>Mã hóa AES-256]
        Log_Mngr[Logging Subsystem<br/>SLF4J / Logback]
        
        VT_Executor --> |Quản lý luồng ngầm| Email_Svc
        VT_Executor --> |Quản lý luồng ngầm| Sched_Svc
    end
    class Business_Layer business;

    %% TẦNG DỮ LIỆU CỤ CỤC BỘ (LOCAL DATA LAYER)
    subgraph Data_Layer [Tầng Dữ liệu Cục bộ]
        DB[(Local DB<br/>SQLite / H2 Database)]
        LogFiles[/Tệp tin Log hệ thống<br/>Rolling File Appender/]
    end
    class Data_Layer data;

    %% THÀNH PHẦN NGOÀI HỆ THỐNG (EXTERNAL SERVICES & OS)
    subgraph External_Layer [Hạ tầng & Dịch vụ bên ngoài]
        OS_Env[Hệ điều hành máy trạm<br/>Windows 10/11 or Linux LTS]
        Mail_Server[Mail Server đám mây<br/>Gmail, Outlook - SMTP/IMAP]
    end
    class External_Layer external;

    %% LIÊN KẾT GIỮA CÁC TẦNG TRONG KIẾN TRÚC
    Ctrl --> |Gọi dịch vụ bất đồng bộ| VT_Executor
    
    Email_Svc --> |Xác thực & Lưu tài khoản| Crypto_Svc
    Crypto_Svc --> |Ghi dữ liệu bảo mật| DB
    Sched_Svc --> |Tra cứu hàng đợi / Cập nhật lịch sử| DB
    
    %% Ghi Log từ các module
    Email_Svc & Sched_Svc & Crypto_Svc -.-> |Ghi nhận sự kiện| Log_Mngr
    Log_Mngr -.-> |Xuất tệp tin cục bộ| LogFiles

    %% Tương tác hạ tầng mạng và OS
    Presentation_Layer --> |Chạy trên môi trường đồ họa| OS_Env
    DB & LogFiles --> |Lưu trữ trên File System| OS_Env
    Email_Svc ==> |Giao thức bảo mật SSL/TLS| Mail_Server
```

### 1. Tầng Giao diện (Presentation Layer)

* Sử dụng mô hình **MVC (Model-View-Controller)** tích hợp sẵn của JavaFX.
* `UI` chịu trách nhiệm render giao diện bằng tệp cấu hình cấu trúc FXML và định dạng CSS.
* `Controllers` đóng vai trò tiếp nhận sự kiện kích hoạt từ chuột/bàn phím của người dùng, đóng gói dữ liệu và chuyển giao xuống tầng xử lý nghiệp vụ mà không trực tiếp can thiệp vào logic mạng hay cơ sở dữ liệu.

### 2. Tầng Nghiệp vụ Lõi (Business Logic Layer)

* **Virtual Threads (Hạ tầng luồng ảo - JEP 444 của JDK 21):** Điểm mấu chốt công nghệ. Do việc kết nối mạng (SMTP/IMAP) và truy vấn I/O từ ổ đĩa là các tác vụ gây nghẽn (Blocking I/O), hệ thống sử dụng Virtual Threads để xử lý song song hàng nghìn tác vụ gửi mail cùng lúc mà không làm tiêu tốn tài nguyên phần cứng của máy trạm và tuyệt đối không gây đóng băng giao diện chính.
* **Crypto Service:** Đảm bảo nguyên tắc an toàn thông tin. Lớp này nhận mật khẩu thô từ Controller, áp dụng thuật toán đối xứng mã hóa để đảm bảo dữ liệu ghi xuống đĩa luôn ở trạng thái an toàn.

### 3. Tầng Dữ liệu Cục bộ (Local Data Layer)

* **Local DB:** Sử dụng một kiến trúc cơ sở dữ liệu nhúng nhẹ (Embedded) như SQLite hoặc H2. Giúp ứng dụng chạy độc lập (Standalone) hoàn toàn, không cần người dùng cài đặt thêm hệ quản trị cơ sở dữ liệu phức tạp (như MySQL hay SQL Server).
* **Log Subsystem (Hệ thống Log ghi nhận):** Tách biệt việc lưu dữ liệu nghiệp vụ (trong DB) và dữ liệu vận hành (ra file text .log có cơ chế xoay vòng tự động - Rolling).

### 4. Ranh giới hệ thống & Tác nhân ngoài (System Boundary)

* Ứng dụng đóng vai trò là một **Email Client**. Ranh giới của hệ thống kết thúc tại điểm phát tín hiệu mạng Socket kết nối qua TLS/SSL tới cổng `465` hoặc `587` của các `Mail Server` bên ngoài quốc tế.

