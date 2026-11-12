# 💰 Personal Finance Tracker (Pure Java Desktop GUI + MongoDB Atlas Cloud)

A modern, standalone desktop application built in **Pure Core Java (Java Swing)** that connects directly to **MongoDB Atlas Cloud** to manage personal finances, track income and expenses, and visualize cash flow.

---

## 🚀 Key Features

* **Real-time Financial Overview:**
  * **Net Balance** (Auto color-coded: Green for surplus, Red for deficit)
  * **Total Income**
  * **Total Expenses**
* **Visual Analytics & Donut / Pie Charts:**
  * Interactive category spending and income breakdown chart built using native `Graphics2D`.
  * Visual share percentages and descending category rank table.
* **Transaction Management:**
  * Record **Income** and **Expenses** with amount, category, date, payment method, and notes.
  * Smart categories: Food & Dining, Rent, Utilities, Salary, Freelance, Investments, etc.
  * Delete transactions with confirmation.
* **Search & Filters:**
  * Filter by Type (All / Income Only / Expense Only).
  * Filter by Category.
  * Keyword search across notes, payment methods, and categories.
* **MongoDB Atlas Cloud Sync & Environment Security:**
  * Direct NoSQL database integration using the official MongoDB Java Sync Driver.
  * **`.env` File Support:** Database URI and credentials are stored securely in `.env` (with `.gitignore` to prevent secret leaks).
  * In-app **"Configure Atlas"** dialog to test and paste your connection string on the fly.
  * **Offline Resilience:** If the cloud database is temporarily unreachable, it automatically maintains a local CSV backup so the user never loses data.
* **Data Export:**
  * One-click **"Export CSV"** to generate statements for accounting or spreadsheet analysis.

---

## 🛠 Tech Stack

* **Language:** Java (JDK 17 / 21 / 25)
* **GUI Toolkit:** Java Swing (`javax.swing`, `java.awt`, `Graphics2D`) with modern Windows styling
* **Database:** MongoDB Atlas (Cloud NoSQL)
* **Libraries:**
  * `mongodb-driver-sync-4.11.1.jar`
  * `mongodb-driver-core-4.11.1.jar`
  * `bson-4.11.1.jar`

---

## 📁 Project Structure

```text
Project/
├── bin/                             # Compiled .class files
├── lib/                             # MongoDB official Java driver JARs
│   ├── bson-4.11.1.jar
│   ├── mongodb-driver-core-4.11.1.jar
│   └── mongodb-driver-sync-4.11.1.jar
├── src/
│   └── com/finance/tracker/
│       ├── Main.java                # App Entry Point & Windows Look-and-Feel
│       ├── db/
│       │   └── MongoDBConnection.java # .env parser, Atlas connection, ping & timeouts
│       ├── model/
│       │   ├── Transaction.java     # Transaction entity model
│       │   ├── TransactionType.java # Enum (INCOME, EXPENSE)
│       │   └── Category.java        # Standardized expense/income categories
│       ├── dao/
│       │   └── TransactionDAO.java  # MongoDB CRUD operations & summary math
│       └── ui/
│           ├── MainFrame.java       # Main Dashboard (cards, filters, table, actions)
│           ├── AddTransactionDialog.java # Modal dialog for adding entries
│           ├── AnalyticsDialog.java # 2D Vector Pie/Donut Chart & Category breakdown
│           └── MongoSettingsDialog.java  # Live MongoDB Atlas connection manager
├── .env                             # Secret Atlas URI & Database settings (Ignored by Git)
├── .env.example                     # Public template for environment setup
├── .gitignore                       # Git ignore rules for secrets, bin/, and cache
├── compile.bat                      # 1-Click build script
├── run.bat                          # 1-Click run script
└── README.md                        # Documentation & viva explanation guide
```

---

## ⚡ How to Run

### Quick Start (Double Click)
1. Double-click **`run.bat`** to start the app immediately.
2. (Optional) If you modify any `.java` files, double-click **`compile.bat`** to recompile.

### From Command Line / Terminal
```powershell
# Compile
compile.bat

# Run
run.bat
```

---

## ☁ Configuring MongoDB Atlas Cloud via `.env`

You can configure your connection in two ways:

### Method A: Directly in `.env`
Edit the [`.env`](file:///d:/class/java/Project/.env) file in the project root:
```env
MONGO_URI=mongodb+srv://<your_username>:<your_password>@cluster0.xxxxx.mongodb.net/?retryWrites=true&w=majority
MONGO_DATABASE=personal_finance
```

### Method B: Live Through the Application GUI
1. Run `run.bat` and click **`☁ Configure Atlas`** at the bottom-right.
2. Paste your connection string.
3. Click **Test Connection** -> **Save & Connect**.
4. The application will test the connection and automatically save it into `.env` for you!

---

## 🎓 Academic / Project Highlights for Viva & Presentation

* **Security Best Practices:** Environment variable management using `.env` and `.gitignore` to avoid hardcoding database passwords.
* **OOP Principles Applied:**
  * **Encapsulation:** Private fields with getters/setters in `Transaction.java`.
  * **Abstraction & DAO Pattern:** `TransactionDAO` abstracts database communication away from GUI components.
  * **Polymorphism & Event-Driven Architecture:** Swing listeners (`ActionListener`, `SwingWorker`) for non-blocking asynchronous data fetching.
* **Native Computer Graphics:** Custom-rendered Pie / Donut vector chart using standard Java `Graphics2D` with anti-aliasing.
* **Separation of Concerns:** Model-View-DAO architecture keeps UI, business calculations, and database logic decoupled.
