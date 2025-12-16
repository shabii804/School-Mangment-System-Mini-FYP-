# 🎓 School Management System (Java)

A **professional, file-based School Management System** developed in **Java (Swing + Core Java)** as a **Software Engineering academic project**. The system manages **students, teachers, classes, subjects, attendance, marks, and fee records** using a structured CSV-based data storage approach.

This project demonstrates strong understanding of **OOP concepts, data persistence, modular design, and role-based access control** without using external databases.

---

## 📌 Project Overview

The School Management System is designed to automate and simplify daily school operations. It provides **separate dashboards for Admin, Teachers, and Students**, ensuring secure access and proper data handling.

All data is stored locally using **CSV files**, making the project lightweight, portable, and easy to understand for academic evaluation.

---

## 👥 User Roles & Features

### 🔐 Admin Panel

* Admin authentication (password stored securely in file)
* Create, update, and delete **students**
* Create, update, and delete **teachers**
* Manage **classes (1–10)**
* Add, update, and delete **subjects** per class
* Set and update **class fees**
* Assign subjects to teachers (with exclusivity validation)
* View student lists class-wise
* Issue Leavig certicte 
* Full control over system data
* Attendence record and marks record
* Analyse student and Techers
* Fee reciption and fee histroy

### 👨‍🏫 Teacher Panel

* Secure teacher login
* View assigned classes and subjects
* Mark **daily attendance** (Present / Absent)
* Enter and update **student marks (0–100)**
* View student performance
* View Assign class
* Assignment validation (max 3 subjects per teacher)

### 🎒 Student Panel

* Secure student login
* View personal profile
* View attendance percentage
* View attendence histroy
* View subject-wise marks
* View fee status (paid / pending)
* Change password

---

## 💾 Data Storage Design

The system uses **CSV files** stored in a local `data/` directory.

| File Name      | Purpose                                                    |
| -------------- | ---------------------------------------------------------- |
| `classes.csv`  | Stores class name, subjects, and fee                       |
| `students.csv` | Stores student profile, attendance, marks, and fee history |
| `teachers.csv` | Stores teacher profile and assigned subjects               |
| `admin.txt`    | Stores admin password                                      |

### 🔐 Encoded Data Formats

* **Attendance:** `yyyy-MM-dd~P^yyyy-MM-dd~A`
* **Marks:** `Math:90^English:85`
* **Teacher Assignment:** `Class~Sub1|Sub2;Class2~Sub3`
* **Fee History:** `date~amount~classFee~note`

---

## 🧠 Core Functional Highlights

* CSV parsing with quote handling
* Auto-generated unique IDs (1–1000)
* Fee overpayment prevention
* Attendance percentage calculation
* Subject assignment exclusivity
* Cascading updates (subjects → marks → teachers)
* Data persistence across sessions

---

## 🛠 Technologies Used

* **Java (JDK 8+)**
* **Java Swing** (GUI)
* **Core Java** (Collections, File Handling)
* **CSV File Storage**
* **OOP Principles**

---

## 📂 Project Structure

```
School-Management-System/
│
├── src/
│   ├── DataStore.java        # Central data handling & persistence
│   ├── LoginScreen.java      # Login interface
│   ├── AdminDashboard.java   # Admin operations
│   ├── TeacherDashboard.java # Teacher operations
│   ├── StudentDashboard.java # Student operations
│
├── data/
│   ├── classes.csv
│   ├── students.csv
│   ├── teachers.csv
│   └── admin.txt
│
├── README.md
└── .gitignore
```

---

## ▶️ How to Run the Project

### Requirements

* Java JDK 8 or higher
* Any Java IDE (IntelliJ IDEA / Eclipse / NetBeans)

### Steps

1. Clone the repository:

   ```bash
   git clone https://github.com/shabii804/School-Management-System.git
   ```
2. Open the project in your IDE
3. Compile and run the `LoginScreen.java` file
4. The `data/` folder will be auto-created on first run

---

## 🔑 Default Login Credentials

| Role    | ID          | Password        |
| ------- | ----------- | --------------- |
| Admin   | —           | `1234`         |
| Teacher | Assigned ID | As set by admin |
| Student | Assigned ID | As set by admin |

> Admin password can be changed and is stored in `admin.txt`

---

## 📐 Software Engineering Concepts Used

* Object-Oriented Programming (OOP)
* Encapsulation & Abstraction
* File-based persistence
* Modular architecture
* Input validation & error handling
* Separation of concerns

---

## 🚀 Future Enhancements

* Database integration (MySQL / SQLite)
* Role-based authorization with encryption
* PDF report generation
* Parent portal
* Analytics dashboard
* Web-based version (Spring Boot)

---

## 👨‍💻 Author

**Shoaib Arshad**
Software Engineering Student
Riphah Internatinal University, Islamabad
---

## 📄 License

This project is developed for **educational purposes** and may be reused or modified for learning and academic pro
