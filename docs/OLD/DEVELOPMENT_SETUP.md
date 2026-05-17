# HƯỚNG DẪN CÀI ĐẶT MÔI TRƯỜNG PHÁT TRIỂN - PrecisionMail

Tài liệu này hướng dẫn cài đặt môi trường phát triển cho dự án **PrecisionMail** - một ứng dụng desktop email client xây dựng bằng JavaFX.

---

# MỤC LỤC

1. [Yêu cầu hệ thống](#1-yêu-cầu-hệ-thống---os-jdk-25-maven-git)  
2. [Cài đặt JDK 25](#2-cài-đặt-jdk-25---3-tùy-chọn-oracle-eclipse-temurin-amazon-corretto)  
3. [Cấu hình IntelliJ IDEA](#4-cấu-hình-intellij-idea---setup-ide-cho-javafx)  
4. [Clone và setup dự án](#5-clone-và-setup-dự-án---lệnh-maven-để-tải-dependencies)  
5. [Chạy ứng dụng](#6-chạy-ứng-dụng---3-cách-chạy-terminal-intellij-package)  
6. [Cấu hình Gmail](#7-cấu-hình-gmail---hướng-dẫn-setup-email-account)  
7. [Xử lý lỗi thường gặp](#8-xử-lý-lỗi---5-lỗi-thường-gặp-và-giải-pháp)  
8. [Cấu trúc dự án](#9-cấu-trúc-dự-án---mô-tả-thư-mục-và-vai-trò)  
9. [Danh sách dependencies](#10-danh-sách-dependencies---thư-viện-chính-dùng-trong-dự-án)  

---

## Yêu Cầu Hệ Thống

### Yêu cầu bắt buộc:

| Thành phần | Phiên bản | Ghi chú |
|-----------|-----------|--------|
| **OS** | Windows 10/11 (64-bit) | macOS 12+ hoặc Ubuntu 20.04+ tương tự |
| **JDK** | **25** | Phải là JDK (chứa JVM), không phải JRE |
| **Maven** | **3.8.0+** | Build tool |
| **Git** | 2.x | Quản lý source code |

### Công cụ hỗ trợ (khuyến nghị):

| Công cụ | Phiên bản | Lý do |
|---------|-----------|-------|
| **IntelliJ IDEA** | 2023.x+ | IDE tốt nhất cho JavaFX (Community Free) |
| **SQLite Browser** | Latest | Xem/sửa cơ sở dữ liệu SQLite |

---

## Cài Đặt JDK 25

### Bước 1: Tải JDK 25

**Lựa chọn 1: Oracle JDK (Chính thức)**
- Truy cập: https://jdk.java.net/25
- Chọn phiên bản phù hợp: Windows x64 (`.zip` hoặc `.msi`)

**Lựa chọn 2: Eclipse Temurin (OpenJDK - Khuyến nghị)**
- Truy cập: https://adoptium.net
- Chọn: Java 25 LTS
- Chọn: Windows x64

**Lựa chọn 3: Amazon Corretto**
- Truy cập: https://aws.amazon.com/corretto/

### Bước 2: Cài đặt JDK

**Nếu dùng `.msi` (Installer):**
1. Chạy file `.msi`
2. Chọn "Next" và hoàn thành cài đặt
3. JDK sẽ được cài vào `C:\Program Files\Java\jdk-25` (hoặc tương tự)

**Nếu dùng `.zip`:**
1. Giải nén file `.zip` vào thư mục, ví dụ: `C:\tools\jdk-25`
2. Lưu đường dẫn này

### Bước 3: Cấu hình biến môi trường

**Trên Windows:**

1. Mở: `Settings` → `System` → `Advanced system settings`
2. Nhấp: `Environment Variables`
3. Thêm biến `JAVA_HOME`:
   - Nhấp: `New` (dưới "System variables")
   - Variable name: `JAVA_HOME`
   - Variable value: `C:\Program Files\Java\jdk-25` (điều chỉnh theo nơi cài của bạn)
   - Nhấp: `OK`

4. Cập nhật biến `Path`:
   - Chọn `Path` (trong "System variables")
   - Nhấp: `Edit`
   - Nhấp: `New`
   - Thêm: `%JAVA_HOME%\bin`
   - Nhấp: `OK`

5. Nhấp: `OK` để đóng

**Kiểm tra cài đặt:**

Mở PowerShell/Command Prompt và chạy:

```bash
java -version
javac -version
```

Kết quả mong đợi:
```
openjdk version "25" ...
javac 25
```


---

## Clone Và Setup Dự Án

### Bước 1: Clone Repository

```bash
git clone https://github.com/your-org/PrecisionMail.git
cd PrecisionMail
```

### Bước 2: Tải Dependencies (Maven)

```bash
mvn clean install
```

Lệnh này sẽ:
- Xoá build cũ
- Tải tất cả dependencies từ Maven Central
- Biên dịch source code
- Chạy unit tests

**Thời gian:** 2-5 phút (tùy tốc độ mạng)

### Bước 3: Mở dự án trong IntelliJ

1. Mở IntelliJ IDEA
2. `File` → `Open`
3. Chọn thư mục `PrecisionMail` → `OK`
4. IntelliJ sẽ tự động detect `pom.xml` và cấu hình Maven project

---

## ▶Chạy Ứng Dụng

### Cách 1: Chạy từ Terminal

```bash
cd PrecisionMail
mvn clean javafx:run
```

### Cách 2: Chạy từ IntelliJ IDEA

1. **Chạy trực tiếp:**
   - Nhấp chuột phải vào file `Launcher.java`
   - Chọn: `Run 'Launcher.main()'`

2. **Chạy với Maven Plugin:**
   - Mở: `View` → `Tool Windows` → `Maven`
   - Chọn: `PrecisionMail` → `Plugins` → `javafx` → `javafx:run`
   - Nhấp đôi

### Cách 3: Build ứng dụng

```bash
mvn clean package
```

Sẽ tạo file JAR trong thư mục `target/`

---

## Cấu Hình Gmail (Tuỳ Chọn)

### Nếu muốn test gửi email thật:

1. **Chuẩn bị tài khoản Gmail:**
   - Bật xác thực hai yếu tố: https://myaccount.google.com/security
   - Tạo "App Password": https://myaccount.google.com/apppasswords
   - Chọn: Mail → Windows Computer
   - Sao chép mật khẩu ứng dụng (16 ký tự)

2. **Cấu hình trong ứng dụng:**
   - Mở PrecisionMail
   - Vào: `Settings` / `Add Account`
   - Email: `your-email@gmail.com`
   - Password: Dán mật khẩu ứng dụng vừa tạo
   - SMTP Server: `smtp.gmail.com`
   - Port: `587`
   - Bật: TLS/SSL

---

## Cấu Trúc Dự Án

```
PrecisionMail/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── nlu/fit/soft/gr5/precisionMail/
│   │   │       ├── Launcher.java (Entry point)
│   │   │       ├── controller/ (JavaFX Controllers)
│   │   │       ├── model/ (Data models)
│   │   │       ├── service/ (Business logic)
│   │   │       ├── dao/ (Database access)
│   │   │       └── util/ (Utilities)
│   │   └── resources/
│   │       └── nlu/fit/soft/gr5/precisionMail/
│   │           ├── view/ (FXML files)
│   │           ├── css/ (Stylesheets)
│   │           └── icons/ (Images)
│   └── test/
│       └── java/ (Unit tests)
├── document/ (Documentation)
├── pom.xml (Maven configuration)
├── emails.json (Sample data)
└── Query.sql (Database schema)
```

---

## Xử Lý Lỗi Thường Gặp

### 1. Lỗi: `java: release version 25 not supported`

**Nguyên nhân:** JDK version quá cũ

**Giải pháp:**
```bash
java -version  # Kiểm tra version
# Cập nhật JDK lên phiên bản 25
```

---

### 2. Lỗi: `Maven command not found`

**Nguyên nhân:** Biến môi trường `M2_HOME` hoặc `Path` chưa được cấu hình

**Giải pháp:**
```bash
# Kiểm tra biến môi trường
echo %M2_HOME%

# Cấu hình lại biến môi trường (xem phần trên)
# Restart PowerShell/Command Prompt
```

---

### 3. Lỗi: `[WARNING] Could not transfer artifact`

**Nguyên nhân:** Lỗi kết nối internet hoặc Maven repository

**Giải pháp:**
```bash
# Xoá cache Maven
rmdir "%USERPROFILE%\.m2\repository" /s /q

# Chạy lại
mvn clean install
```

---

### 4. Lỗi: `Cannot locate JavaFX runtime`

**Nguyên nhân:** JavaFX chưa được tải

**Giải pháp:**
```bash
# Chạy Maven để tải dependencies
mvn clean install

# Hoặc xoá cache và rebuild
mvn clean -DskipTests install
```

---

### 5. Lỗi: `IntelliJ không recognize JDK 25`

**Nguyên nhân:** IDE cấu hình JDK cũ

**Giải pháp:**
1. `File` → `Settings` → `Project Structure` → `SDKs`
2. Xoá JDK cũ
3. Nhấp `+` → `Add SDK` → `JDK`
4. Chọn đúng thư mục JDK 25
5. Restart IntelliJ

---

## Dependencies Chính

| Thư viện | Phiên bản | Mục đích |
|---------|-----------|---------|
| **JavaFX** | 25.0.3 | UI Desktop |
| **Jakarta Mail** | 2.0.4 | Gửi/nhận email (SMTP/IMAP) |
| **SQLite JDBC** | 3.50.3 | Cơ sở dữ liệu |
| **Jackson** | 2.19.0 | Xử lý JSON |
| **JUnit 5** | 5.12.1 | Unit testing |

---

## Checklist Cài Đặt Hoàn Thành

- [ ] JDK 25 đã cài đặt và cấu hình `JAVA_HOME`
- [ ] Maven đã cài đặt và cấu hình `M2_HOME`
- [ ] `java -version` chạy thành công
- [ ] `mvn -version` chạy thành công
- [ ] Repository đã được clone
- [ ] `mvn clean install` chạy thành công (0 errors)
- [ ] IntelliJ IDEA đã mở dự án
- [ ] `mvn clean javafx:run` hoặc `Launcher.java` chạy thành công
- [ ] Ứng dụng PrecisionMail mở được

---

## Cần Giúp Đỡ?

Nếu gặp lỗi:
1. Đọc lại phần "Xử lý lỗi thường gặp"
2. Kiểm tra thư mục `document/` để xem tài liệu khác
3. Xem logs trong IntelliJ hoặc terminal để tìm nguyên nhân

---

## Ghi Chú Bổ Sung

- **Virtual Threads:** JDK 25 hỗ trợ Virtual Threads (Project Loom), có thể được sử dụng để tối ưu hoá performance
- **Module System:** Dự án sử dụng Java Module System (`module-info.java`)
- **Database:** SQLite được lưu trong thư mục ứng dụng
- **Email Account:** Tài khoản Gmail cần bật 2FA và tạo App Password

---

**Phiên bản:** 1.0  
**Cập nhật lần cuối:** May 10, 2026  
**Tác giả:** Lê Hòa Phú
