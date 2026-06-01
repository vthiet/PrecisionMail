# Test Environment Notes

Noi dung nay duoc tong hop tu source code va moi truong chay hien tai cua do an PrecisionMail.

## Can cu xac dinh moi truong

1. `pom.xml`
- Java build target: 21
- JavaFX: 25.0.3
- Jakarta Mail: 2.0.4
- SQLite JDBC: 3.50.3.0
- Logback: 1.5.18

2. `src/main/resources/example.application.properties`
- SMTP host: `smtp.gmail.com`
- SMTP port: `587`
- Su dung `security.aes.key`

3. Source code
- DB local: `precisionmail.db`
- Email history: `emails.json`
- Log file: `logs/system.log`

## Ban co the dan vao sheet nhu sau

1. Mo `document/test-environment.csv`
2. Copy toan bo noi dung
3. Dan vao sheet `Test enviroment`

## Luu y khi bao cao

Neu thay hoi "vi sao lai co 4 dong moi truong test", ban tra loi:

- Dong 1: may tinh thuc te de chay va test
- Dong 2: phan mem PrecisionMail va stack runtime
- Dong 3: moi truong dich vu ngoai phu thuoc de test chuc nang gui mail
- Dong 4: moi truong luu tru du lieu va log can co de test day du
