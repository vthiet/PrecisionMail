# TEST CASE CHO DO AN PRECISIONMAIL

## 1. Huong dan dua vao Word

Khong nen dan nguyen file CSV 7 cot vao Word vi bang se qua rong va rat kho doc.

Cach trinh bay hop ly:

1. Moi use case la mot muc rieng.
2. Moi test case trinh bay bang bang 5 cot:
   - `TC ID`
   - `Muc tieu kiem thu`
   - `Tien de`
   - `Buoc thuc hien`
   - `Ket qua mong doi`
3. Neu can dep hon nua, tach moi test case thanh mot bang nho 2 cot:
   - Cot trai: ten truong
   - Cot phai: noi dung

Khuyen nghi format trong Word:

- Kho giay: `A4`
- Huong giay: `Landscape`
- Font: `Times New Roman`
- Co chu:
  - Tieu de: `13` dam
  - Noi dung bang: `11`
- Cell margin nho
- Can le tren cho noi dung trong bang
- Khoang cach dong: `1.0` hoac `1.15`

---

## 2. Test case rut gon de dua vao Word

### UC01. Them tai khoan

| TC ID | Muc tieu kiem thu | Tien de | Buoc thuc hien | Ket qua mong doi |
|---|---|---|---|---|
| UC01-01 | Kiem tra bo trong email va password | Da mo hop thoai Add Account | 1. De trong 2 truong. 2. Bam Save. | Hien loi `Email and Password cannot be empty.`; khong luu DB. |
| UC01-02 | Kiem tra email sai dinh dang | Da mo Add Account | 1. Nhap `abc`. 2. Nhap app password hop le. 3. Bam Save. | Hien loi `Email invalid.` |
| UC01-03 | Kiem tra app password duoi 16 ky tu | Da mo Add Account | 1. Nhap email hop le. 2. Nhap password 15 ky tu. 3. Bam Save. | Hien loi `Google App Password must have at least 16 characters.` |
| UC01-04 | Them tai khoan hop le | DB hoat dong; co `security.aes.key` | 1. Nhap email hop le. 2. Nhap app password hop le. 3. Bam Save. | Bao thanh cong; dong dialog; luu du lieu vao bang `accounts`; password duoc ma hoa. |
| UC01-05 | Cap nhat tai khoan da ton tai | DB da co email can test | 1. Nhap lai cung email. 2. Nhap password moi. 3. Bam Save. | Khong tao ban ghi trung; cap nhat app password moi. |
| UC01-06 | Loi cau hinh AES | Thieu `security.aes.key` | 1. Lam sai cau hinh. 2. Thu luu tai khoan. | Hien `Cannot save account...`; khong luu du lieu. |
| UC01-07 | Loi DB khi luu tai khoan | SQLite khong kha dung | 1. Lam DB bi khoa/khong ghi duoc. 2. Luu tai khoan. | Hien loi Save Error; khong luu account. |

### UC02. Chuyen doi tai khoan

| TC ID | Muc tieu kiem thu | Tien de | Buoc thuc hien | Ket qua mong doi |
|---|---|---|---|---|
| UC02-01 | Tu dong nap danh sach account | DB co it nhat 1 account | 1. Mo ung dung. 2. Vao man hinh compose. | Menu tai khoan duoc load nen; tu dong chon account dau tien. |
| UC02-02 | Chuyen sang account khac | DB co it nhat 2 account | 1. Mo menu From. 2. Chon account thu 2. | Account active thay doi theo lua chon. |
| UC02-03 | He thong khong co account nao | Bang `accounts` rong | 1. Mo compose-mail. | Khong co account de chon; Send va Schedule se bi chan khi bam. |
| UC02-04 | Tu reload sau khi them account moi | Compose screen dang mo | 1. Mo Add Account. 2. Them account moi. | Danh sach account tren compose duoc reload tu dong. |
| UC02-05 | Nap account co mat khau ma hoa loi | DB co `encrypt_app_password` bi sua tay | 1. Sua du lieu ma hoa. 2. Mo compose. | Danh sach van load; account co the loi khi gui thuc te. |

### UC03. Soan va gui email

| TC ID | Muc tieu kiem thu | Tien de | Buoc thuc hien | Ket qua mong doi |
|---|---|---|---|---|
| UC03-01 | Khong co nguoi nhan | Mo compose-mail | 1. De trong To/CC/BCC. | Nut Send bi disable. |
| UC03-02 | Dia chi nguoi nhan sai dinh dang | Da co currentAccount | 1. Nhap `abc` vao To. 2. Bam Send. | Hien loi dia chi khong hop le; khong gui. |
| UC03-03 | Chi gui bang CC/BCC | Da co currentAccount | 1. De trong To. 2. Nhap CC hop le. 3. Bam Send. | He thong van cho gui. |
| UC03-04 | Gui khi chua chon tai khoan | currentAccount = null | 1. Nhap recipient hop le. 2. Bam Send. | Hien `Please select an account before sending email.` |
| UC03-05 | Dinh kem file hop le | Mo compose-mail | 1. Chon file `.pdf` nho hon 25MB. | File duoc them vao danh sach; cap nhat so luong va dung luong. |
| UC03-06 | Chan file nguy hiem | Mo compose-mail | 1. Chon file `.exe`. | He thong tu choi file va hien canh bao. |
| UC03-07 | Gioi han 10 file dinh kem | Da co 10 file hop le | 1. Thu them file thu 11. | He thong tu choi file thu 11. |
| UC03-08 | Gioi han tong 25MB | Da co tong dung luong gan 25MB | 1. Thu them file lam vuot 25MB. | He thong tu choi file moi. |
| UC03-09 | Xoa file dinh kem | Da co it nhat 1 file | 1. Bam `x` tai file da dinh kem. | File bi xoa khoi danh sach; thong tin dung luong cap nhat lai. |
| UC03-10 | Gui mail hop le | Co account dung; co internet | 1. Nhap recipient hop le. 2. Nhap subject va content. 3. Bam Send. | He thong day request gui sang background worker; form duoc clear; neu gui thanh cong thi luu vao `emails.json`. |
| UC03-11 | Attachment bi xoa truoc luc gui | Da attach file roi xoa file khoi o dia | 1. Gui mail. | Luong gui bo qua file khong ton tai; khong lam crash he thong. |
| UC03-12 | Subject rong | Co account hop le | 1. Nhap recipient hop le. 2. De trong Subject. 3. Bam Send. | Code hien tai van cho submit request gui. |
| UC03-13 | Sai app password | Co account nhung password sai | 1. Gui mail bang account sai password. | Luong nen ghi log loi; UI khong tra popup loi chi tiet tu background worker. |

### UC04. Import danh sach nguoi nhan

| TC ID | Muc tieu kiem thu | Tien de | Buoc thuc hien | Ket qua mong doi |
|---|---|---|---|---|
| UC04-01 | Import file txt hop le | Da mo compose-mail | 1. Bam Import List. 2. Chon file txt chua email hop le. | Email duoc dua vao o To; hien thong bao thanh cong. |
| UC04-02 | Import vao o BCC | Da hien o BCC va dang focus | 1. Focus vao BCC. 2. Import file email. | Danh sach email duoc dien vao BCC. |
| UC04-03 | Loai bo email trung lap | Da mo compose-mail | 1. Import file co email trung. | Moi email chi xuat hien 1 lan. |
| UC04-04 | Tach email tu file du lieu hon hop | Da mo compose-mail | 1. Import file co text va email chen lan. | Regex trich duoc cac email hop le. |
| UC04-05 | File rong/khong co email hop le | Da mo compose-mail | 1. Import file rong hoac sai du lieu. | Hien `Khong tim thay du lieu hop le.` |
| UC04-06 | Huy chon file import | Da mo compose-mail | 1. Bam Import List. 2. Bam Cancel. | Khong thay doi du lieu tren form. |
| UC04-07 | Loi doc file import | File khong doc duoc | 1. Chon file loi. | Hien `Cannot read recipient file.` |

### UC05. Len lich gui email

| TC ID | Muc tieu kiem thu | Tien de | Buoc thuc hien | Ket qua mong doi |
|---|---|---|---|---|
| UC05-01 | Chua co account active | currentAccount = null | 1. Nhap recipient. 2. Bam Schedule send. | Hien `Please select an account before scheduling email.` |
| UC05-02 | Thoi gian nam trong qua khu | Co currentAccount | 1. Chon gio nho hon hien tai. 2. Bam Schedule send. | Hien `Scheduled time must be in the future.` |
| UC05-03 | Thoi gian vuot qua 2 gio toi | Co currentAccount | 1. Chon gio > now + 2h. 2. Bam Schedule send. | Hien `Scheduled time must be within the next 2 hours.` |
| UC05-04 | Recipient khong hop le | Co currentAccount | 1. Nhap recipient sai. 2. Len lich gui. | Hien loi dia chi khong hop le; khong tao lich. |
| UC05-05 | Len lich hop le | Co account dung; thoi gian hop le | 1. Nhap mail hop le. 2. Chon gio gui. 3. Bam Schedule send. | He thong tao lich, khoa form, doi nut thanh `Cancel schedule`, thong bao thanh cong. |
| UC05-06 | Huy lich truoc gio gui | Dang co lich chua chay | 1. Bam lai nut Cancel schedule. | Lich bi huy; UI duoc mo khoa. |
| UC05-07 | Mo khoa UI sau khi job ket thuc | Dang co lich hop le | 1. Cho job chay xong. | UI duoc mo khoa lai; `pendingScheduledJob` duoc reset. |
| UC05-08 | Loi gui mail len lich | Account sai password hoac mat mang | 1. Tao lich gui. 2. Cho den gio. | Job nen ghi log loi; UI mo khoa sau khi ket thuc. |

### UC06. Lich su gui va logging

| TC ID | Muc tieu kiem thu | Tien de | Buoc thuc hien | Ket qua mong doi |
|---|---|---|---|---|
| UC06-01 | Luu lich su email sau khi gui thanh cong | SMTP gui thanh cong | 1. Gui 1 email hop le. 2. Mo `emails.json`. | File `emails.json` co them ban ghi moi. |
| UC06-02 | Hien thi danh sach mail da gui | `emails.json` co du lieu | 1. Mo man hinh History. | ListView hien subject va nguoi gui. |
| UC06-03 | Xem chi tiet mail trong lich su | History da load danh sach | 1. Chon 1 email. | Hien dung Subject, From, To, Date, Content. |
| UC06-04 | `emails.json` hong dinh dang | File JSON bi sua sai | 1. Mo man hinh History. | He thong ghi log ERROR, tra danh sach rong, khong crash. |
| UC06-05 | Ghi log he thong | Ung dung dang chay | 1. Thuc hien Add Account, Send, Schedule. 2. Mo `logs/system.log`. | Co ban ghi log day du timestamp, level, thread va message. |
| UC06-06 | Che giau email nhay cam trong log | Co thao tac lien quan email | 1. Thuc hien gui hoac them account. 2. Mo log. | Email trong log duoc mask boi `LogHelper`. |

---

## 3. Ghi chu dua vao bao cao

Nen viet them 1 doan ngan duoi bang:

`Cac test case duoc xay dung dua tren source code thuc te cua he thong PrecisionMail. Trong qua trinh doi chieu, nhom nhan thay implementation hien tai co mot so diem khac voi dac ta ban dau, nhu viec luu tai khoan trong SQLite, luu lich su gui trong emails.json, va co che len lich gui hien tai chua pre-connect SMTP truoc thoi diem trigger. Vi vay, bo test case duoc dieu chinh de phan anh dung hanh vi cua phien ban phan mem hien tai.`
