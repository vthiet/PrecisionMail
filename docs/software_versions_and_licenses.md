# Phiên bản phần mềm và license thư viện

Tài liệu này liệt kê phiên bản phần mềm, thư viện và plugin đang được sử dụng trong project **PrecisionMail** tại thời điểm kiểm tra source code hiện tại.

## Thông tin project

| Thành phần | Giá trị |
| --- | --- |
| Project | `nlu.fit.soft.gr5:PrecisionMail` |
| Version | `1.0-SNAPSHOT` |
| Ngôn ngữ | Java |
| Java source/target | `25` |
| Build tool | Apache Maven Wrapper |
| Maven distribution | `3.9.6` |
| Maven Wrapper | `3.1.0` |

## Thư viện khai báo trực tiếp trong `pom.xml`

| Nhóm | Thư viện | Group ID | Version | Scope | License |
| --- | --- | --- | --- | --- | --- |
| UI | `javafx-controls` | `org.openjfx` | `25.0.3` | compile | GPL v2 with Classpath Exception |
| UI | `javafx-fxml` | `org.openjfx` | `25.0.3` | compile | GPL v2 with Classpath Exception |
| UI | `javafx-web` | `org.openjfx` | `25.0.3` | compile | GPL v2 with Classpath Exception |
| Office document | `poi-ooxml` | `org.apache.poi` | `5.2.3` | compile | Apache License 2.0 |
| Email | `jakarta.mail` | `org.eclipse.angus` | `2.0.4` | compile | EPL 2.0 / EDL 1.0 |
| Email | `angus-activation` | `org.eclipse.angus` | `2.0.2` | compile | EPL 2.0 / EDL 1.0 |
| JSON | `jackson-databind` | `com.fasterxml.jackson.core` | `2.19.0` | compile | Apache License 2.0 |
| JSON | `jackson-datatype-jsr310` | `com.fasterxml.jackson.datatype` | `2.19.0` | compile | Apache License 2.0 |
| Database | `sqlite-jdbc` | `org.xerial` | `3.50.3.0` | compile | Apache License 2.0 |
| Logging | `slf4j-api` | `org.slf4j` | `2.0.17` | compile | MIT License |
| Logging | `logback-classic` | `ch.qos.logback` | `1.5.18` | compile | EPL 1.0 / LGPL 2.1 |
| Testing | `junit-jupiter-api` | `org.junit.jupiter` | `5.12.1` | test | EPL 2.0 |
| Testing | `junit-jupiter-engine` | `org.junit.jupiter` | `5.12.1` | test | EPL 2.0 |

## Thư viện phụ thuộc gián tiếp khi build

Các thư viện dưới đây được Maven resolve thông qua dependency trực tiếp ở trên.

| Thư viện | Group ID | Version | Scope | License |
| --- | --- | --- | --- | --- |
| `javafx-base` | `org.openjfx` | `25.0.3` | compile | GPL v2 with Classpath Exception |
| `javafx-graphics` | `org.openjfx` | `25.0.3` | compile | GPL v2 with Classpath Exception |
| `javafx-media` | `org.openjfx` | `25.0.3` | compile | GPL v2 with Classpath Exception |
| `poi` | `org.apache.poi` | `5.2.3` | compile | Apache License 2.0 |
| `poi-ooxml-lite` | `org.apache.poi` | `5.2.3` | compile | Apache License 2.0 |
| `commons-codec` | `commons-codec` | `1.15` | compile | Apache License 2.0 |
| `commons-math3` | `org.apache.commons` | `3.6.1` | compile | Apache License 2.0 |
| `SparseBitSet` | `com.zaxxer` | `1.2` | compile | Apache License 2.0 |
| `xmlbeans` | `org.apache.xmlbeans` | `5.1.1` | compile | Apache License 2.0 |
| `commons-compress` | `org.apache.commons` | `1.21` | compile | Apache License 2.0 |
| `commons-io` | `commons-io` | `2.11.0` | compile | Apache License 2.0 |
| `curvesapi` | `com.github.virtuald` | `1.07` | compile | BSD 3-Clause License |
| `log4j-api` | `org.apache.logging.log4j` | `2.18.0` | compile | Apache License 2.0 |
| `commons-collections4` | `org.apache.commons` | `4.4` | compile | Apache License 2.0 |
| `jakarta.activation-api` | `jakarta.activation` | `2.1.3` | compile | EPL 2.0 / GPL v2 with Classpath Exception |
| `jackson-annotations` | `com.fasterxml.jackson.core` | `2.19.0` | compile | Apache License 2.0 |
| `jackson-core` | `com.fasterxml.jackson.core` | `2.19.0` | compile | Apache License 2.0 |
| `logback-core` | `ch.qos.logback` | `1.5.18` | compile | EPL 1.0 / LGPL 2.1 |
| `opentest4j` | `org.opentest4j` | `1.3.0` | test | Apache License 2.0 |
| `junit-platform-commons` | `org.junit.platform` | `1.12.1` | test | EPL 2.0 |
| `junit-platform-engine` | `org.junit.platform` | `1.12.1` | test | EPL 2.0 |
| `apiguardian-api` | `org.apiguardian` | `1.1.2` | test | Apache License 2.0 |

## Maven build plugins

| Plugin | Group ID | Version | License |
| --- | --- | --- | --- |
| `maven-compiler-plugin` | `org.apache.maven.plugins` | `3.13.0` | Apache License 2.0 |
| `javafx-maven-plugin` | `org.openjfx` | `0.0.8` | Apache License 2.0 |
| `maven-surefire-plugin` | `org.apache.maven.plugins` | `3.2.5` | Apache License 2.0 |

## Ghi chú

- Danh sách phiên bản trực tiếp được đối chiếu từ `pom.xml` sau khi project chuyển sang JDK 25 và JavaFX 25.
- Danh sách dependency gián tiếp cần được resolve lại bằng JDK 25 khi môi trường local/CI đã cài JDK 25.
- Project hiện chưa khai báo license riêng trong `pom.xml`.
- Nếu thêm, xóa hoặc nâng cấp dependency trong `pom.xml`, cần cập nhật lại tài liệu này.
