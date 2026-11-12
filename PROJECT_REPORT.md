# 📑 ACADEMIC PROJECT REPORT

## PROJECT TITLE: PERSONAL FINANCE TRACKER
**Course / Subject:** Object-Oriented Programming with Java  
**Domain:** Financial Technology / Desktop Application Development  
**Technology Stack:** Core Java (JDK 25), Java Swing, Java 2D Graphics, MongoDB Atlas (Cloud NoSQL), Official MongoDB Sync Driver  
**Version Control:** Git & GitHub ([Repository Link](https://github.com/Pratik-kr21/Personal-Finance-Tracker.git))  

---

## 1. ABSTRACT
Managing personal finances, budgeting, and tracking expenditures is an essential daily requirement. The **Personal Finance Tracker** is an interactive desktop application developed in pure Java using the Swing GUI toolkit. It provides real-time tracking of income and expenses, categorical classification, budget threshold monitoring with visual progress indicators, and custom 2D vector data visualization. The system connects directly to a cloud-hosted MongoDB Atlas NoSQL database for reliable, scalable data persistence while maintaining offline resilience through a local fallback storage mechanism. Strict security protocols are implemented, ensuring credentials and connection strings are managed via isolated environment configurations without exposure in the codebase.

---

## 2. OBJECTIVES & SCOPE
* **User-Centric Transaction Recording:** Enable recording of income and expense transactions with date, category, amount, payment method, and descriptive notes.
* **Dynamic Budgeting System:** Allow users to set and adjust monthly spending limits directly within the GUI, backed by visual progress indicators and color-coded threshold alerts (Safe, Caution, and Exceeded).
* **Cloud Data Persistence:** Integrate with MongoDB Atlas using the official Java Sync Driver for cloud synchronization.
* **Offline Fault Tolerance:** Maintain uninterrupted usability through automatic local cache fallback whenever network connectivity is unavailable.
* **Native Data Visualization:** Render custom pie and donut charts using Java 2D (`Graphics2D`) vector graphics without third-party chart dependencies.
* **Security & Secret Isolation:** Prevent exposure of database credentials by utilizing `.env` variable management and automated credential masking.
* **Export Capabilities:** Provide one-click data export to Comma-Separated Values (`.csv`) for external accounting and spreadsheet software compatibility.

---

## 3. SYSTEM REQUIREMENTS

### Hardware Requirements
* **Processor:** 1.8 GHz dual-core or higher (x86_64 or ARM64)
* **RAM:** Minimum 2 GB (4 GB recommended)
* **Storage:** 150 MB free disk space (includes JDK and libraries)
* **Display:** Minimum resolution 1024 x 768

### Software Requirements
* **Operating System:** Windows 10/11, macOS, or Linux
* **Java Development Kit (JDK):** JDK 17, 21, or 25
* **Database:** MongoDB Atlas (Cloud) or local MongoDB instance (v4.4+)
* **Dependencies (Bundled in `lib/`):**
  * `bson-4.11.1.jar`
  * `mongodb-driver-core-4.11.1.jar`
  * `mongodb-driver-sync-4.11.1.jar`

---

## 4. SYSTEM ARCHITECTURE & DESIGN

The application is structured following the **Model-View-DAO (Data Access Object)** architectural pattern, enforcing separation between user interface components, business logic, and database persistence.

```
+-------------------------------------------------------------------------+
|                              PRESENTATION LAYER (GUI)                   |
|  MainFrame  |  AddTransactionDialog  |  AnalyticsDialog  | SettingsDialog|
+-------------------------------------------------------------------------+
                                    │
                                    ▼
+-------------------------------------------------------------------------+
|                               DAO / SERVICE LAYER                       |
|                          TransactionDAO.java                            |
|    - CRUD Operations                 - Balance & Totals Calculation      |
|    - Dynamic Filtering               - Offline CSV Synchronization      |
+-------------------------------------------------------------------------+
                    │                                   │
                    ▼ (When Connected)                  ▼ (When Offline)
+---------------------------------------+   +-----------------------------+
|          DATABASE LAYER               |   |     LOCAL FALLBACK CACHE    |
|       MongoDB Atlas Cloud             |   |    transactions_local.csv   |
|   (Cluster: collections/transactions) |   +-----------------------------+
+---------------------------------------+
```

---

## 5. DATABASE DESIGN (MongoDB Atlas NoSQL)

### Database: `personal_finance`
### Collection: `transactions`

Documents stored in the collection adhere to the following schema:

```json
{
  "_id": ObjectId("67320b9c3e218204b7e80a12"),
  "type": "EXPENSE",
  "category": "Food & Dining",
  "amount": 45.50,
  "date": "2026-11-12",
  "paymentMethod": "Credit Card",
  "note": "Lunch meeting with colleagues"
}
```

### Collection Schema Definition:
| Field | Type | Description |
| :--- | :--- | :--- |
| `_id` | `ObjectId` | Primary Key uniquely generated by MongoDB |
| `type` | `String` | Transaction type: `INCOME` or `EXPENSE` |
| `category` | `String` | Categorical classification (e.g., Salary, Rent, Groceries) |
| `amount` | `Double` | Transaction monetary value (strictly positive) |
| `date` | `String` | Date formatted as ISO `YYYY-MM-DD` |
| `paymentMethod`| `String` | Mode of payment (Cash, Card, Bank Transfer, UPI, etc.) |
| `note` | `String` | User notes or description |

---

## 6. KEY MODULES & IMPLEMENTATION

### Module 1: Graphical User Interface (`ui` package)
* **`MainFrame.java`:** The primary dashboard comprising real-time summary cards, dynamic type/category/search filters, transaction table with custom color rendering, and action toolbars.
* **`AddTransactionDialog.java`:** Modal input form featuring real-time category updates depending on transaction type selection, field validations, and date defaulting.
* **`AnalyticsDialog.java`:** Interactive analytics window utilizing Java `Graphics2D` to render an anti-aliased 2D donut vector chart alongside a sorted breakdown table.
* **`MongoSettingsDialog.java`:** Live cloud configuration interface allowing users to test, verify, and store MongoDB Atlas connection strings on the fly.

### Module 2: Persistence & Data Access (`dao` & `db` packages)
* **`MongoDBConnection.java`:** Singleton connection manager managing connection pool initialization, server timeouts, cluster ping health checks, and regex-based password masking (`maskSensitiveInfo()`).
* **`TransactionDAO.java`:** Implements CRUD logic, calculating aggregate statistics (net balance, total income, total expense, category totals), and seamlessly persisting data to local storage when cloud connectivity drops.

### Module 3: Domain Models (`model` package)
* **`Transaction.java`:** Core business entity encapsulating all attributes of an entry with standard getters, setters, and date formatting constants.
* **`TransactionType.java`:** Enumeration standardizing transaction classification.
* **`Category.java`:** Centralized catalog of standard expense and income classifications.

---

## 7. SECURITY IMPLEMENTATION
1. **Zero Secret Hardcoding:** No database credentials, cluster URLs, or passwords are hardcoded within source code files.
2. **Environment Variable Isolation:** Secrets are read dynamically at runtime from an isolated `.env` configuration file or system environment variables.
3. **Repository Cleanliness:** A comprehensive `.gitignore` configuration guarantees `.env` and compiled classes are never committed to version control.
4. **Log & UI Credential Masking:** All connection strings containing credentials are automatically scrubbed via regex masks before being displayed in error dialogues or printed to system output streams.

---

## 8. TESTING & RESULTS

| Test Case ID | Test Description | Expected Result | Actual Result | Status |
| :--- | :--- | :--- | :--- | :--- |
| **TC-01** | Add new Income transaction with valid fields | Transaction stored in MongoDB & balance increases | Stored in Atlas; table and balance updated immediately | **PASSED** |
| **TC-02** | Add transaction with negative or non-numeric amount | Validation alert displayed; record rejected | Prompt shows "Amount must be greater than zero" | **PASSED** |
| **TC-03** | Delete selected transaction | Record removed from MongoDB and UI table | Record deleted; totals updated | **PASSED** |
| **TC-04** | Monthly budget spending threshold alert | Progress bar turns red when spending exceeds 100% | Bar turns crimson red with "⚠️ Over by: $XX" | **PASSED** |
| **TC-05** | Cloud offline disconnection test | App falls back to local CSV storage without crashing | Status turns yellow; local CSV maintains operations | **PASSED** |
| **TC-06** | Export financial statement to CSV | File generated in spreadsheet-compatible format | CSV statement written successfully | **PASSED** |

---

## 9. CONCLUSION & FUTURE ENHANCEMENTS

### Conclusion
The **Personal Finance Tracker** demonstrates a production-grade implementation of Core Java principles, event-driven desktop GUI design, and cloud database integration. The application emphasizes usability, security, fault tolerance, and clear architectural modularity.

### Future Enhancements
1. **Multi-User Authentication:** Implement encrypted login authentication allowing multiple household members to maintain independent ledgers.
2. **Recurring Transactions Engine:** Background scheduling for automatic recording of monthly subscriptions and recurring salary credits.
3. **Automated PDF Reports:** Generate formatted PDF statement summaries using an embedded document generation library.
4. **Predictive Analytics:** Incorporate simple moving-average statistical models to forecast upcoming monthly expenditures.
