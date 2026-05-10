I'll analyze the PrecisionMail project's architecture by examining its key files and structure.   

Read [](file:///home/vthiet/IdeaProjects/PrecisionMail/pom.xml#1-1), lines 1 to 60

Read [](file:///home/vthiet/IdeaProjects/PrecisionMail/document/business_requirements.md#1-1), lines 1 to 50

Read [](file:///home/vthiet/IdeaProjects/PrecisionMail/README.md)

Read [](file:///home/vthiet/IdeaProjects/PrecisionMail/src/main/java/nlu/fit/soft/gr5/precisionMail/Launcher.java)

Read [](file:///home/vthiet/IdeaProjects/PrecisionMail/src/main/java/nlu/fit/soft/gr5/precisionMail/model/Email.java)

Read [](file:///home/vthiet/IdeaProjects/PrecisionMail/src/main/java/nlu/fit/soft/gr5/precisionMail/service/EmailService.java)

Read [](file:///home/vthiet/IdeaProjects/PrecisionMail/src/main/java/nlu/fit/soft/gr5/precisionMail/controller/fxml/MainScreenController.java)

Read [](file:///home/vthiet/IdeaProjects/PrecisionMail/src/main/java/nlu/fit/soft/gr5/precisionMail/service/AccountService.java)

Read [](file:///home/vthiet/IdeaProjects/PrecisionMail/document/event_bus.md#1-1), lines 1 to 50

Read [](file:///home/vthiet/IdeaProjects/PrecisionMail/src/main/java/nlu/fit/soft/gr5/precisionMail/dao/AccountDao.java)

Read [](file:///home/vthiet/IdeaProjects/PrecisionMail/src/main/java/nlu/fit/soft/gr5/precisionMail/util/CryptoUtil.java)

Read [](file:///home/vthiet/IdeaProjects/PrecisionMail/src/main/java/nlu/fit/soft/gr5/precisionMail/util/DbUtil.java)

## System Architecture Overview: PrecisionMail

Here's a comprehensive architecture analysis of your email scheduling application:

### **1. Project Purpose**
A low-latency **scheduled email client** desktop application (JavaFX) designed to:
- Send emails via Google SMTP at precise, scheduled times
- Support multiple email accounts with encrypted storage
- Handle bulk email sending with file imports
- Minimize network latency with pre-established connections

---

### **2. Technology Stack**
```
GUI Framework:        JavaFX 25.0.3 (with FXML templates)
Mail Protocol:        Jakarta Mail 2.0.4 (SMTP only)
Database:             SQLite 3.50.3 (embedded)
JSON Serialization:   Jackson 2.19.0
Build Tool:           Maven
Java Version:         JDK 25 (Java Modules enabled)
Testing:              JUnit Jupiter 5.12.1
```

---

### **3. Architectural Layers**

#### **Layer 1: Presentation (View + Controller)**
```
src/main/resources/view/
├── MainScreen.fxml                 (Root container with BorderPane)
└── include/
    ├── sidebar.fxml
    ├── menubar.fxml
    ├── compose-mail.fxml
    └── center/

src/main/java/controller/
├── MainScreenController            (Listens for navigation changes)
├── MenuBarController               (Publishes navigation events)
├── SideBarController
├── ComposeMailController           (Email composition UI)
├── HistoryMailController           (View sent emails)
├── dialog/
│   └── AddAccountDialogController  (Account setup dialog)
└── AuthController                  (Authentication flows)
```

**Key Pattern**: **Event Bus (Publisher-Subscriber)**
- `NavigationService` acts as a **Singleton Event Bus**
- Controllers (Publishers) call `NavigationService.navigateTo()`
- `MainScreenController` (Subscriber) listens via `setNavigationListener()`
- Decouples UI components — Controllers don't need direct references to each other

---

#### **Layer 2: Business Logic (Service Layer)**
```
service/
├── AccountService (Interface)
│   └── impl/AccountServiceImpl        (Account CRUD operations)
├── EmailService (Interface)
│   └── impl/EmailServiceImpl          (Email sending & retrieval)
├── NavigationService (Singleton)     (Event Bus for UI routing)
└── LoadAccountService                (Async account loading)
```

**Responsibilities:**
- **AccountService**: Manage user credentials, add/delete/switch accounts
- **EmailService**: Send emails via SMTP, save email history, retrieve sent emails
- **NavigationService**: Coordinate view changes across the app
- **LoadAccountService**: Background loading (async) of account data

---

#### **Layer 3: Data Access (DAO Layer)**
```
dao/
├── AccountDao (Interface)
│   └── impl/AccountDaoImpl           (SQLite queries for accounts)
├── EmailDao (Interface)
│   └── impl/EmailDaoImpl             (SQLite queries for emails)
```

**Pattern**: **Repository Pattern**
- Abstracts database operations
- JDBC-based queries to SQLite
- Separates database logic from business logic

---

#### **Layer 4: Models (Domain Objects)**
```
model/
├── Account                          (Email credentials)
└── Email                            (Email metadata & content)
```

**Email Model Structure:**
```java
Email {
  id: Long
  from: String
  toLst: Set<String>              // To recipients
  cc: Set<String>                 // CC recipients
  bcc: Set<String>                // BCC recipients
  subject: String
  content: String                 // Plain text only
  attachments: List<String>       // File paths
  sentAt: LocalDateTime           // Scheduled send time
}
```

---

#### **Layer 5: Utilities**
```
util/
├── DbUtil                           (SQLite connection pooling)
├── CryptoUtil                       (Password encryption/decryption)
├── EmailUtil                        (Email validation, parsing)
├── AlertUtil                        (Error/notification dialogs)
└── AppLoaderUtil                    (Resource loading, FXML initialization)
```

---

### **4. Data Flow Diagram**

```
User Interaction (UI)
        ↓
    Controller (Event Handler)
        ↓
    NavigationService (Event Bus)
        ↓
    Service Layer (Business Logic)
        ↓
    DAO Layer (Repository)
        ↓
    SQLite Database / Gmail SMTP
```

**Example: Compose & Send Email**
1. User clicks "Send" in `ComposeMailController`
2. Controller calls `EmailService.send(Account, Email)`
3. `EmailService` validates email (via `EmailUtil`)
4. `EmailService` establishes SMTP connection via `jakarta.mail`
5. Email is sent to Gmail's server
6. Record saved to SQLite via `EmailDao`

---

### **5. Database Schema**
```sql
CREATE TABLE accounts (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL,          -- Email address
    password TEXT NOT NULL,          -- Encrypted Google App Password
    created_at TEXT NOT NULL         -- ISO 8601 timestamp
);
```

---

### **6. Key Design Patterns**

| Pattern | Where | Purpose |
|---------|-------|---------|
| **Singleton** | NavigationService | Single instance for app-wide event routing |
| **MVC** | Controllers + FXML + Models | Separation of concerns for UI |
| **Repository/DAO** | DAO Layer | Abstract database operations |
| **Service Layer** | Service interfaces | Centralize business logic |
| **Event Bus** | NavigationService | Decouple UI navigation |
| **Factory** | FXMLLoader in util | Dynamic FXML loading |

---

### **7. Key Features**

✅ **Account Management**
- Multiple Google accounts with encrypted passwords
- Account switching via sidebar/dropdown
- Secure credential storage in SQLite

✅ **Email Composition**
- Plain text email drafting
- Support for To, CC, BCC fields
- File attachments (max 10 files, 25MB total)
- Blocked file types (.exe, .bat, .cmd, .vbs)

✅ **Bulk Sending**
- Import recipient lists (.txt, .csv)
- Regex-based email extraction
- Pre-scheduled batch sending

✅ **Scheduling & Execution**
- Set precise send times (within 2 hours)
- Sleep/Wake mechanism to pre-establish SMTP connection
- Minimal latency on send trigger

✅ **UI/UX**
- Countdown timer during scheduled sends
- Disabled controls while sending (prevent duplicates)
- Detailed error notifications

---

### **8. Deployment & Configuration**

**Files:**
- `application.properties` — Runtime configuration (database path, SMTP settings)
- `example.application.properties` — Template for setup
- emails.json — Sample email data
- Query.sql — Database schema and seed queries

**Entry Point:**
- Launcher.java — Initializes DB and starts JavaFX app

---

### **9. Security Considerations**

🔒 **Encryption**: Passwords stored encrypted in SQLite (via `CryptoUtil`)  
🔐 **Gmail OAuth**: Uses Google App Passwords (not plain SMTP auth)  
🛡️ **File Validation**: Blocks dangerous executables  
🔌 **SMTP Only**: No IMAP/POP3 (read-only sends to minimize attack surface)

---

### **10. Scalability Notes**

- **Modular**: Easy to add new services (SMS, Slack notifications)
- **Event-Driven**: NavigationService allows adding new features without refactoring UI
- **Database-Ready**: SQLite can be swapped for PostgreSQL/MySQL in DAO implementations
- **Multi-Account**: Architecture supports unlimited accounts per user

This architecture follows **clean layering** with clear separation of concerns, making it maintainable and extensible.