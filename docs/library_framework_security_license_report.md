# Báo cáo thư viện, framework, lỗ hổng và giấy phép

## PrecisionMail

| Trường | Giá trị |
| --- | --- |
| Mã tài liệu | `PM-DEP-SEC-LIC-001` |
| Phiên bản tài liệu | `1.0` |
| Ngày chốt dữ liệu | `07/06/2026` |
| Dự án | `nlu.fit.soft.gr5:PrecisionMail:1.0-SNAPSHOT` |
| Nguồn phiên bản | `pom.xml`, Maven dependency tree đã resolve |
| Nguồn lỗ hổng | OSV, GitHub Security Advisory, NVD và advisory của nhà cung cấp |
| Mục đích | Trình bày công nghệ, dependency, rủi ro bảo mật và nghĩa vụ giấy phép |

> Báo cáo là ảnh chụp tại ngày chốt dữ liệu. CVE, phiên bản vá và điều khoản giấy phép có thể thay đổi; cần quét lại trước mỗi lần phát hành.

## 1. Tóm tắt trình bày

### Slide 1 - Kiến trúc công nghệ

PrecisionMail là ứng dụng desktop Java 25, dùng JavaFX và AtlantaFX cho giao diện, SQLite cho dữ liệu cục bộ, Eclipse Angus cho SMTP/IMAP, Jackson cho JSON, Apache POI cho Excel và SLF4J/Logback cho logging.

### Slide 2 - Kết quả kiểm kê

| Nhóm | Số lượng |
| --- | ---: |
| Dependency khai báo trực tiếp | 14 |
| Dependency trực tiếp dùng khi chạy ứng dụng | 12 |
| Dependency trực tiếp chỉ dùng kiểm thử | 2 |
| Advisory được OSV phát hiện trên runtime dependency đã resolve | 4 |
| Dependency runtime cần ưu tiên xử lý | `commons-lang3`, `jackson-core`, `logback-core` |

JavaFX tạo thêm artifact theo hệ điều hành, ví dụ classifier `linux`. Các artifact này là biến thể native của cùng module, không phải thư viện nghiệp vụ độc lập.

### Slide 3 - Kết luận bảo mật

- Không phát hiện advisory từ OSV cho đa số dependency runtime tại phiên bản hiện dùng.
- Phát hiện 4 advisory trên 3 artifact runtime.
- Chưa thấy đường khai thác trực tiếp từ xa trong luồng hiện tại của PrecisionMail.
- Vẫn cần nâng phiên bản vì các artifact hiện tại nằm trong dải bị ảnh hưởng.
- Cần bổ sung quét dependency tự động vào CI và chặn phát hành khi có lỗ hổng nghiêm trọng có khả năng khai thác.

### Slide 4 - Kết luận giấy phép

- Các dependency sử dụng giấy phép nguồn mở phổ biến và nhìn chung cho phép phân phối ứng dụng.
- Khi phát hành bản cài đặt, phải kèm thông báo bản quyền, nội dung giấy phép và file `NOTICE` khi giấy phép yêu cầu.
- JavaFX dùng GPL v2 với Classpath Exception; Classpath Exception cho phép liên kết với mã ứng dụng mà không buộc toàn bộ ứng dụng phải chuyển sang GPL.
- Project PrecisionMail chưa khai báo giấy phép riêng trong `pom.xml`; cần quyết định trước khi phát hành công khai.

## 2. Framework và nền tảng chính

| Thành phần | Phiên bản | Vai trò | Phạm vi | Giấy phép chính |
| --- | --- | --- | --- | --- |
| Java / JDK | 25 | Ngôn ngữ và runtime | Build + runtime | Phụ thuộc bản phân phối JDK; OpenJDK thường là GPL v2 + Classpath Exception |
| Apache Maven Wrapper | Maven `3.9.6` | Build và quản lý dependency | Build | Apache License 2.0 |
| JavaFX | `25.0.3` | Framework giao diện desktop, FXML, WebView | Runtime | GPL v2 + Classpath Exception |
| AtlantaFX Base | `2.1.0` | Theme và control bổ sung cho JavaFX | Runtime | MIT |
| SQLite | Được đóng gói trong `sqlite-jdbc 3.50.3.0` | Cơ sở dữ liệu nhúng | Runtime | SQLite Public Domain; wrapper JDBC Apache 2.0 |

## 3. Dependency khai báo trực tiếp

| Nhóm | Maven coordinate | Phiên bản | Scope | Mục đích trong ứng dụng | Giấy phép |
| --- | --- | ---: | --- | --- | --- |
| UI | `io.github.mkpaz:atlantafx-base` | `2.1.0` | compile | Theme sáng/tối và control JavaFX | MIT |
| UI | `org.openjfx:javafx-controls` | `25.0.3` | compile | Button, TableView, form và layout control | GPL v2 + Classpath Exception |
| UI | `org.openjfx:javafx-fxml` | `25.0.3` | compile | Nạp giao diện FXML | GPL v2 + Classpath Exception |
| UI | `org.openjfx:javafx-web` | `25.0.3` | compile | WebView và HTML email preview | GPL v2 + Classpath Exception |
| Office | `org.apache.poi:poi-ooxml` | `5.4.0` | compile | Tạo báo cáo Excel hàng đợi | Apache 2.0 |
| Email | `org.eclipse.angus:jakarta.mail` | `2.0.4` | compile | Gửi mail SMTP và kiểm tra IMAP | EPL 2.0 / GPL v2 + CPE / EDL 1.0 |
| Email | `org.eclipse.angus:angus-activation` | `2.0.2` | compile | MIME, content handler và attachment | EDL 1.0 |
| JSON | `com.fasterxml.jackson.core:jackson-databind` | `2.19.0` | compile | Mapping JSON sang object Java | Apache 2.0 |
| JSON | `com.fasterxml.jackson.datatype:jackson-datatype-jsr310` | `2.19.0` | compile | Hỗ trợ Java Date/Time trong Jackson | Apache 2.0 |
| Database | `org.xerial:sqlite-jdbc` | `3.50.3.0` | compile | JDBC driver cho SQLite cục bộ | Apache 2.0 |
| Logging | `org.slf4j:slf4j-api` | `2.0.17` | compile | Logging facade | MIT |
| Logging | `ch.qos.logback:logback-classic` | `1.5.18` | compile | Logging backend, rolling file và async appender | EPL 1.0 / LGPL 2.1 |
| Testing | `org.junit.jupiter:junit-jupiter-api` | `5.12.1` | test | API viết unit test | EPL 2.0 |
| Testing | `org.junit.jupiter:junit-jupiter-engine` | `5.12.1` | test | Engine chạy unit test | EPL 2.0 |

## 4. Dependency gián tiếp quan trọng

| Dependency đã resolve | Phiên bản | Được kéo theo bởi | Vai trò | Giấy phép |
| --- | ---: | --- | --- | --- |
| `org.openjfx:javafx-base` | `25.0.3` | JavaFX | Property, binding, collection | GPL v2 + Classpath Exception |
| `org.openjfx:javafx-graphics` | `25.0.3` | JavaFX Controls | Scene graph và rendering | GPL v2 + Classpath Exception |
| `org.openjfx:javafx-media` | `25.0.3` | JavaFX Web | Media support | GPL v2 + Classpath Exception |
| `org.openjfx:jdk-jsobject` | `25.0.3` | JavaFX Web | JavaScript bridge | GPL v2 + Classpath Exception |
| `org.apache.poi:poi` | `5.4.0` | POI OOXML | Core Office document API | Apache 2.0 |
| `org.apache.poi:poi-ooxml-lite` | `5.4.0` | POI OOXML | OOXML schemas | Apache 2.0 |
| `org.apache.xmlbeans:xmlbeans` | `5.3.0` | POI OOXML | XML binding | Apache 2.0 |
| `org.apache.commons:commons-compress` | `1.27.1` | POI OOXML | Đọc/ghi định dạng nén | Apache 2.0 |
| `org.apache.commons:commons-lang3` | `3.16.0` | Commons Compress | Utility cho Java core API | Apache 2.0 |
| `commons-io:commons-io` | `2.18.0` | POI OOXML | Tiện ích I/O | Apache 2.0 |
| `commons-codec:commons-codec` | `1.17.1` | POI | Codec và digest | Apache 2.0 |
| `org.apache.commons:commons-collections4` | `4.4` | POI | Collection utilities | Apache 2.0 |
| `org.apache.commons:commons-math3` | `3.6.1` | POI | Hàm toán học | Apache 2.0 |
| `com.zaxxer:SparseBitSet` | `1.3` | POI | Bit set tiết kiệm bộ nhớ | Apache 2.0 |
| `com.github.virtuald:curvesapi` | `1.08` | POI OOXML | Curve API dùng trong Office chart | BSD 3-Clause |
| `org.apache.logging.log4j:log4j-api` | `2.24.3` | POI | Logging API của POI | Apache 2.0 |
| `jakarta.activation:jakarta.activation-api` | `2.1.3` | Eclipse Angus | Activation API | EPL 2.0 / GPL v2 + Classpath Exception |
| `com.fasterxml.jackson.core:jackson-core` | `2.19.0` | Jackson Databind | JSON streaming parser | Apache 2.0 |
| `com.fasterxml.jackson.core:jackson-annotations` | `2.19.0` | Jackson Databind | Annotation JSON mapping | Apache 2.0 |
| `ch.qos.logback:logback-core` | `1.5.18` | Logback Classic | Core logging implementation | EPL 1.0 / LGPL 2.1 |

## 5. Lỗ hổng được phát hiện

### 5.1 Tổng hợp

| Mã advisory | Dependency bị ảnh hưởng | Phiên bản hiện tại | Mức độ công bố | Phiên bản vá tối thiểu | Đánh giá trong PrecisionMail |
| --- | --- | ---: | --- | ---: | --- |
| `CVE-2025-48924` / `GHSA-j288-q9x7-2f5v` | `commons-lang3` | `3.16.0` | Medium, CVSS 3.1: `5.3` | `3.18.0` | Khả năng khai thác thấp; ứng dụng không gọi trực tiếp `ClassUtils.getClass(...)` |
| `GHSA-72hv-8253-57qq` | `jackson-core` | `2.19.0` | Medium, CVSS 4.0: `6.9` | `2.21.1` hoặc nhánh `2.18.6` | Khả năng khai thác thấp hiện tại; code không dùng non-blocking/async parser |
| `CVE-2025-11226` / `GHSA-25qh-j22f-pwp8` | `logback-core` | `1.5.18` | Medium, CVSS 4.0: `5.9` | `1.5.19` | Điều kiện khai thác chính chưa có: dependency tree không chứa Spring hoặc Janino |
| `CVE-2026-1225` / `GHSA-qqpg-mvqg-649v` | `logback-core` | `1.5.18` | Low, CVSS 4.0: `1.8` | `1.5.25` | Yêu cầu kẻ tấn công có quyền sửa cấu hình và class nguy hiểm đã có trên classpath |

### 5.2 Chi tiết và khả năng khai thác

#### CVE-2025-48924 - Apache Commons Lang

`ClassUtils.getClass(...)` có thể gây `StackOverflowError` khi xử lý chuỗi tên lớp rất dài. `commons-lang3 3.16.0` được kéo theo gián tiếp bởi `commons-compress 1.27.1`.

- Tác động lý thuyết: từ chối dịch vụ.
- Hiện trạng ứng dụng: không import hoặc gọi trực tiếp Commons Lang.
- Hành động: override `commons-lang3` lên ít nhất `3.18.0`.

#### GHSA-72hv-8253-57qq - Jackson Core async parser

Non-blocking JSON parser có thể bỏ qua giới hạn chiều dài số, dẫn đến tiêu thụ bộ nhớ hoặc CPU.

- Tác động lý thuyết: từ chối dịch vụ khi parse JSON có số cực dài.
- Hiện trạng ứng dụng: không tìm thấy lời gọi `createNonBlocking*`; luồng hiện tại không dùng async parser.
- Hành động: đồng bộ cả Jackson BOM hoặc các module Jackson lên phiên bản vá tương thích, ưu tiên `2.21.1` trở lên.

#### CVE-2025-11226 và CVE-2026-1225 - Logback Core

Hai advisory liên quan đến xử lý file cấu hình Logback và khả năng khởi tạo/thực thi lớp từ classpath.

- Tác động lý thuyết: thực thi hoặc khởi tạo mã/lớp khi cấu hình logging bị kiểm soát.
- Hiện trạng ứng dụng: dùng `src/main/resources/logback.xml` đóng gói sẵn; dependency tree không có Spring hoặc Janino.
- Rủi ro còn lại: người có quyền sửa package, classpath hoặc file cấu hình đã có quyền cục bộ đáng kể.
- Hành động: nâng `logback-classic` và `logback-core` đồng bộ lên ít nhất `1.5.25`.

### 5.3 Dependency không có advisory trong lần quét

OSV không trả về advisory cho các phiên bản đã quét còn lại, gồm JavaFX `25.0.3`, AtlantaFX `2.1.0`, POI `5.4.0`, XMLBeans `5.3.0`, Commons Compress `1.27.1`, Commons IO `2.18.0`, Jakarta Mail `2.0.4`, SQLite JDBC `3.50.3.0`, SLF4J `2.0.17` và các module Jackson khác.

Điều này chỉ có nghĩa là không có advisory khớp trong nguồn quét tại ngày chốt dữ liệu; không phải cam kết phần mềm không có lỗ hổng.

## 6. Chi tiết giấy phép và nghĩa vụ

| Giấy phép | Cho phép chính | Nghĩa vụ khi phân phối | Thành phần tiêu biểu |
| --- | --- | --- | --- |
| Apache License 2.0 | Sử dụng, sửa đổi, phân phối, thương mại | Giữ copyright/license; kèm `NOTICE` nếu upstream có; ghi nhận thay đổi; không dùng trademark trái phép | POI, Jackson, SQLite JDBC, Apache Commons |
| MIT | Sử dụng, sửa đổi, phân phối, thương mại | Giữ copyright và nội dung giấy phép | AtlantaFX, SLF4J |
| BSD 3-Clause | Sử dụng, sửa đổi, phân phối | Giữ copyright, điều kiện và disclaimer; không dùng tên tác giả để quảng bá | CurvesAPI |
| GPL v2 + Classpath Exception | Liên kết thư viện với ứng dụng độc quyền hoặc giấy phép khác | Khi phân phối JavaFX/OpenJDK phải kèm license và thông báo tương ứng; sửa trực tiếp code GPL vẫn chịu nghĩa vụ GPL | JavaFX, thường gặp ở OpenJDK |
| EPL 2.0 | Sử dụng và phân phối; copyleft ở phạm vi module EPL đã sửa | Cung cấp source của phần EPL đã sửa khi phân phối theo EPL; giữ notice/license | JUnit, Angus Mail, Activation API |
| EDL 1.0 | Giấy phép kiểu BSD, cho phép sử dụng và phân phối rộng | Giữ copyright, điều kiện và disclaimer | Eclipse Angus |
| EPL 1.0 / LGPL 2.1 | Logback cho phép chọn một trong hai giấy phép | Giữ license/notice; nếu chọn LGPL và sửa thư viện, phải cung cấp phần sửa theo LGPL | Logback |
| Public Domain | Không áp đặt điều kiện bản quyền thông thường | Nên giữ ghi nhận nguồn; kiểm tra quy định tại quốc gia phân phối | SQLite engine |

### 6.1 Checklist tuân thủ khi phát hành

- Tạo thư mục `THIRD-PARTY-LICENSES/` trong gói phát hành.
- Kèm bản sao giấy phép của tất cả dependency runtime.
- Kèm file `NOTICE` của Apache/Jackson/Angus và các dự án có cung cấp `NOTICE`.
- Ghi rõ tên thư viện, phiên bản, URL dự án và giấy phép trong `THIRD-PARTY-NOTICES.md`.
- Không xóa copyright header hoặc thông báo bản quyền trong thư viện.
- Nếu sửa trực tiếp JavaFX, Logback, Angus hoặc module EPL/LGPL/GPL, đánh giá lại nghĩa vụ cung cấp source.
- Khai báo giấy phép của chính PrecisionMail trong `pom.xml` và repository trước khi phát hành công khai.

## 7. Kế hoạch xử lý đề xuất

| Ưu tiên | Hành động | Lý do | Tiêu chí hoàn thành |
| --- | --- | --- | --- |
| P1 | Nâng Logback lên ít nhất `1.5.25` | Xử lý hai advisory ở `logback-core` | Dependency tree không còn `logback-core 1.5.18`; test logging đạt |
| P1 | Nâng đồng bộ Jackson lên nhánh có bản vá | Xử lý bypass giới hạn số của async parser | OSV không còn báo `GHSA-72hv-8253-57qq`; test JSON đạt |
| P2 | Override `commons-lang3` lên ít nhất `3.18.0` | Xử lý `CVE-2025-48924` | Dependency tree resolve phiên bản đã vá |
| P2 | Thêm OWASP Dependency-Check hoặc OSV Scanner vào CI | Ngăn tài liệu và dependency bị lỗi thời | Pipeline tạo báo cáo và fail theo policy |
| P2 | Tạo third-party notices cho bản phát hành | Tuân thủ giấy phép | Gói phát hành chứa license và notice |
| P3 | Đồng bộ các tài liệu dependency cũ | Tránh sai phiên bản như POI `5.2.3` so với thực tế `5.4.0` | Chỉ còn một nguồn báo cáo phiên bản được duy trì |

## 8. Phương pháp kiểm tra

### 8.1 Lệnh kiểm kê

```bash
./mvnw dependency:tree -Dverbose
./mvnw dependency:list -DincludeScope=runtime
./mvnw help:effective-pom
```

### 8.2 Phạm vi quét lỗ hổng

- Đối chiếu tọa độ Maven và phiên bản runtime đã resolve với OSV.
- Đọc advisory chi tiết từ GitHub Security Advisory/NVD/nhà cung cấp.
- Tìm kiếm API hoặc cấu hình dễ bị ảnh hưởng trong source code.
- Không thực hiện khai thác lỗ hổng trên môi trường production.
- Không quét lỗ hổng của hệ điều hành, JDK distribution hoặc native library nằm ngoài Maven dependency tree.

## 9. Nguồn tham khảo

- Maven Central: <https://repo.maven.apache.org/maven2/>
- OSV API: <https://osv.dev/>
- NVD: <https://nvd.nist.gov/>
- OpenJFX: <https://openjfx.io/>
- OpenJDK GPL v2 + Classpath Exception: <https://openjdk.org/legal/gplv2+ce.html>
- AtlantaFX: <https://github.com/mkpaz/atlantafx>
- Apache POI: <https://poi.apache.org/>
- Apache Commons Security: <https://commons.apache.org/security.html>
- Jackson Core Security Advisory: <https://github.com/FasterXML/jackson-core/security/advisories/GHSA-72hv-8253-57qq>
- Logback releases và security fixes: <https://github.com/qos-ch/logback/releases>
- Eclipse Angus Mail: <https://eclipse-ee4j.github.io/angus-mail/>
- SQLite JDBC: <https://github.com/xerial/sqlite-jdbc>
- SPDX License List: <https://spdx.org/licenses/>
