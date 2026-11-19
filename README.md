# 🍔 CodeCravers – Canteen Food Ordering System

## 📖 About the Project

CodeCravers is a Java-based web application developed to digitalize the traditional college canteen food ordering process. It provides students with a convenient platform to browse food items, place orders, generate bills and view their order history.

The system also provides an admin dashboard to manage food items and prices.

This project is developed by a team of three B.Tech CSE (AIML) students at Galgotias University.

## 🌐 Live Website

https://codecravers-canteen.onrender.com/

## 🎯 Project Objectives

- Digitalize the traditional canteen ordering process.
- Reduce waiting time and manual work.
- Provide a user-friendly ordering experience.
- Automate bill generation and order management.
- Integrate frontend and backend technologies with a relational database.
- Provide separate access for students and administrators.

## ✨ Key Features

### Student Module

- Student registration
- Student login
- Food menu browsing
- Shopping cart
- Checkout and order placement
- Automatic bill generation
- Order history

### Admin Module

- Admin authentication
- Admin dashboard
- Add new food items
- Update food item prices
- Remove food items from the available menu
- Manage the student-facing menu

### Security Features

- Role-based access control
- Password hashing using PBKDF2
- Prepared statements
- Server-side input validation
- Session-based authentication
- CSRF protection
- Environment-based database credentials

## 🛠️ Technology Stack

| Technology | Purpose |
|---|---|
| HTML5 | Webpage structure |
| CSS3 | Styling and layout |
| JavaScript | Frontend interactivity |
| Java 21 | Backend programming |
| Jakarta Servlets 6.0 | Request and response handling |
| JDBC | Database connectivity |
| MySQL | Database management |
| Apache Tomcat 10.1.60 | Application server |
| Maven | Build and dependency management |
| Docker | Deployment configuration |
| GitHub | Version control |
| Render | Online hosting |

## 🏗️ System Architecture

```
Student / Administrator
          |
          v
Frontend (HTML, CSS, JavaScript)
          |
          v
Apache Tomcat Server
          |
          v
Java Servlets (Backend)
          |
          v
JDBC (Database Connectivity)
          |
          v
MySQL Database
```

The frontend handles user interactions, while Java Servlets process requests, execute business logic and communicate with the MySQL database through JDBC.

## 📁 Important Project Files

| File | Description |
|---|---|
| index.html | Website homepage |
| menu.html | Food menu |
| cart.html | Shopping cart |
| site.css | Website styling |
| CanteenServlet.java | Backend application logic |
| DatabaseConnection.java | MySQL database connectivity |
| PasswordUtil.java | Password hashing utility |
| HelloServlet.java | Servlet connectivity testing |
| web.xml | Web application configuration |
| pom.xml | Maven build configuration |
| Dockerfile | Deployment configuration |
| README.md | Project documentation |
| PROJECT_REPORT_AND_VIVA.md | Project report and viva preparation |

## 🗄️ Database Design

**Database name:** canteen_db

The application uses the following documented database tables:

| Table | Purpose |
|---|---|
| users | Stores user accounts and roles |
| food_items | Stores food details and prices |
| orders | Stores order information |
| order_items | Stores individual items within orders |

The database is connected to the Java backend using JDBC and MySQL Connector/J.

The database schema and physical foreign-key constraints are pending final documentation.

## ⚙️ Installation and Setup

### Prerequisites

- Java Development Kit 21
- Apache Tomcat 10.1.60
- MySQL Server
- MySQL Connector/J 8.4.0
- Apache Maven
- Git

### 1. Clone the Repository

```
git clone https://github.com/Sanchita1007/CodeCravers-Canteen.git
cd CodeCravers-Canteen
```

### 2. Configure the Database

Create the MySQL database:

```sql
CREATE DATABASE IF NOT EXISTS canteen_db;
USE canteen_db;
```

The application tables must be created using the project database schema before running the application. The complete schema is pending upload.

### 3. Configure Environment Variables

| Variable | Value |
|---|---|
| CANTEEN_DB_URL | jdbc:mysql://localhost:3306/canteen_db |
| CANTEEN_DB_USER | Your MySQL username |
| CANTEEN_DB_PASSWORD | Your MySQL password |

Configure these variables locally before starting Tomcat. Never commit actual database passwords to GitHub.

### 4. Configure Apache Tomcat

- Configure JAVA_HOME and CATALINA_HOME.
- Configure the database environment variables.
- Ensure MySQL Server is running.
- Place MySQL Connector/J in the Tomcat lib directory for the local manual deployment setup.

### 5. Build and Deploy

Build the application using Maven:

```
mvn clean package
```

Deploy the generated WAR file to Apache Tomcat using Canteen as the application context.

Start Apache Tomcat and open:

http://localhost:8080/Canteen/

## 🎨 UI/UX Design

The user interface is designed around the following principles:

- Simplicity
- Consistency
- Visual hierarchy
- User-friendly navigation
- Readability
- Clear presentation of food items and prices
- Adaptive layouts using CSS media queries

Desktop and mobile responsiveness testing will be documented separately.

## 🧪 Software Testing

The following results were recorded during local development testing.

| Test Case | Expected Result | Status |
|---|---|---|
| Student registration | Account successfully created | Passed |
| Valid login | User successfully logged in | Passed |
| Invalid login | Access denied | Passed |
| Food menu | Database food items displayed | Passed |
| Add to cart | Food item added to cart | Passed |
| Empty cart checkout | Checkout prevented | Passed |
| Order placement | Order successfully placed | Passed |
| Bill generation | Bill displayed | Passed |
| Order history | Previous orders displayed | Passed |
| Admin login | Admin dashboard accessible | Passed |
| Add food item | New food item displayed | Passed |
| Update food price | Updated price reflected | Passed |
| Remove food item | Item removed from student menu | Passed |
| Unauthorized admin access | HTTP 403 Forbidden | Passed |

These results refer to local testing. The published repository and hosted application require final synchronization and verification.

## 💡 Innovation and Extra Effort

- Centralized food menu management
- Role-based student and administrator access
- Automated bill generation
- Student order history
- Database-driven food menu
- Online deployment configuration

## 🚀 Future Scope

- Online payment integration
- Real-time order status tracking
- QR code-based ordering
- Email notifications
- Food ratings and feedback
- Sales analytics dashboard
- AI-based food recommendations

## 👥 Development Team

**Team Name:** CodeCravers

**University:** Galgotias University

**Programme:** B.Tech CSE (Artificial Intelligence and Machine Learning)

**Team Size:** 3 Members

## 📄 Project Documentation

For additional project details and viva preparation, refer to [PROJECT_REPORT_AND_VIVA.md](PROJECT_REPORT_AND_VIVA.md).

## 📌 Project Status

- Frontend development completed
- Backend development completed
- Local database connectivity tested
- Student module tested
- Admin module tested
- 

---

Developed by Team CodeCravers | Galgotias University

