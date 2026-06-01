# Test Case Notes

Bo test case trong `test-cases.csv` duoc viet theo implementation hien tai trong source code, khong theo dac ta cu 100%.

## Diem lech quan trong giua tai lieu va source

1. Tai khoan duoc luu trong SQLite `precisionmail.db`, khong luu file cau hinh account chung.
2. Lich su email duoc luu trong `emails.json`, khong luu bang `emails` trong SQLite.
3. Chuyen doi tai khoan khong giai ma khi user click tung item; service tai danh sach account va giai ma truoc.
4. Len lich gui hien tai dung `ScheduledExecutorService` va gui tai thoi diem trigger, chua co co che pre-connect SMTP sat gio gui nhu mo ta BRD.
5. Khi gui mail thuong hoac mail len lich that bai trong background thread, UI hien tai chu yeu ghi log; khong tra popup loi chi tiet tu luong nen ve cho nguoi dung.
6. Subject rong van duoc phep gui, vi source khong co validate subject.
7. UI len lich hien tai chi cho chon `Date + Hour + Minute`, chua cho chon giay.

## Cach su dung

1. Mo file `document/test-cases.csv`.
2. Copy toan bo noi dung vao sheet `Test cases`.
3. Cot `Trang thai(Pass/Fail)` hien dang de `Not Run` vi chua thuc thi manual tren may/co tai khoan Gmail that.
4. Khi demo, ban chay lai tung case quan trong roi doi cot trang thai thanh `Pass` hoac `Fail`.
