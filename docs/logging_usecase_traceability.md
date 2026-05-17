# Logging Use Case Traceability

## Muc tieu

Tai lieu nay doi chieu use case "Ghi log he thong" voi implementation hien tai trong PrecisionMail.

## Mapping yeu cau -> source code

1. Ghi log vao tep cuc bo `logs/system.log`
- Cau hinh: [src/main/resources/logback.xml](../src/main/resources/logback.xml)
- Kieu ghi: `AsyncAppender` de khong chan luong chinh

2. Moi ban ghi co timestamp, muc do log, thread, mo ta
- Pattern: `%d{yyyy-MM-dd HH:mm:ss.SSS} [%level] [%thread] %logger{36} - %msg`
- Cau hinh: [src/main/resources/logback.xml](../src/main/resources/logback.xml)

3. Ghi log cac su kien quan trong
- Khoi dong app, init DB, runtime exception:
  [src/main/java/nlu/fit/soft/gr5/precisionMail/Launcher.java](../src/main/java/nlu/fit/soft/gr5/precisionMail/Launcher.java)
- Them tai khoan:
  [src/main/java/nlu/fit/soft/gr5/precisionMail/controller/dialog/AddAccountDialogController.java](../src/main/java/nlu/fit/soft/gr5/precisionMail/controller/dialog/AddAccountDialogController.java)
  [src/main/java/nlu/fit/soft/gr5/precisionMail/service/impl/AccountServiceImpl.java](../src/main/java/nlu/fit/soft/gr5/precisionMail/service/impl/AccountServiceImpl.java)
  [src/main/java/nlu/fit/soft/gr5/precisionMail/dao/impl/AccountDaoImpl.java](../src/main/java/nlu/fit/soft/gr5/precisionMail/dao/impl/AccountDaoImpl.java)
- Gui mail thuong:
  [src/main/java/nlu/fit/soft/gr5/precisionMail/controller/fxml/ComposeMailController.java](../src/main/java/nlu/fit/soft/gr5/precisionMail/controller/fxml/ComposeMailController.java)
  [src/main/java/nlu/fit/soft/gr5/precisionMail/service/impl/EmailServiceImpl.java](../src/main/java/nlu/fit/soft/gr5/precisionMail/service/impl/EmailServiceImpl.java)
  [src/main/java/nlu/fit/soft/gr5/precisionMail/util/EmailUtil.java](../src/main/java/nlu/fit/soft/gr5/precisionMail/util/EmailUtil.java)
- Len lich gui:
  [src/main/java/nlu/fit/soft/gr5/precisionMail/service/impl/ScheduledEmailServiceImpl.java](../src/main/java/nlu/fit/soft/gr5/precisionMail/service/impl/ScheduledEmailServiceImpl.java)
- Lich su email:
  [src/main/java/nlu/fit/soft/gr5/precisionMail/controller/fxml/HistoryMailController.java](../src/main/java/nlu/fit/soft/gr5/precisionMail/controller/fxml/HistoryMailController.java)
  [src/main/java/nlu/fit/soft/gr5/precisionMail/dao/impl/EmailDaoImpl.java](../src/main/java/nlu/fit/soft/gr5/precisionMail/dao/impl/EmailDaoImpl.java)
- Dieu huong man hinh:
  [src/main/java/nlu/fit/soft/gr5/precisionMail/service/NavigationService.java](../src/main/java/nlu/fit/soft/gr5/precisionMail/service/NavigationService.java)
  [src/main/java/nlu/fit/soft/gr5/precisionMail/controller/fxml/SideBarController.java](../src/main/java/nlu/fit/soft/gr5/precisionMail/controller/fxml/SideBarController.java)
  [src/main/java/nlu/fit/soft/gr5/precisionMail/controller/fxml/MenuBarController.java](../src/main/java/nlu/fit/soft/gr5/precisionMail/controller/fxml/MenuBarController.java)
  [src/main/java/nlu/fit/soft/gr5/precisionMail/controller/fxml/MainScreenController.java](../src/main/java/nlu/fit/soft/gr5/precisionMail/controller/fxml/MainScreenController.java)

4. Xu ly ngoai le theo use case
- Loi ghi doc DB/file duoc ghi `ERROR`
- Loi runtime khong bat duoc duoc dua vao `default uncaught exception handler`
- File log lon duoc rolling theo ngay + kich thuoc
- Mail len lich o qua khu bi tu choi va ghi `WARN`

5. Bao ve du lieu nhay cam
- Email trong log duoc mask qua helper:
  [src/main/java/nlu/fit/soft/gr5/precisionMail/util/LogHelper.java](../src/main/java/nlu/fit/soft/gr5/precisionMail/util/LogHelper.java)

## Diem chua hoan tat tuyet doi

1. Chua co retry workflow cho gui mail that bai khi mat mang.
2. Chua test build/runtime bang Maven trong moi truong hien tai vi thieu lenh `mvn`.
3. Chua gan ma su kien log rieng nhu `UC07`, `EX01`, `EX02`.
