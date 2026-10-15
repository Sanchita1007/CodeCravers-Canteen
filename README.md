# CodeCravers Canteen Food Ordering System

A beginner-friendly Java web application for a college canteen. It uses HTML, CSS,
JavaScript, Java Servlets, JDBC, MySQL, and Apache Tomcat 10.1.

## Current implementation

This project addresses the time and coordination problems of manual canteen
ordering by providing a menu, a browser cart, and Java server routes for
student accounts and order handling. The current code aims to let students
browse items and prepare orders, and to let authorized admins manage foods and
review orders.

- Orange, cream, and charcoal homepage, searchable menu, and browser shopping
  cart.
- The menu code requests the existing `canteen_db.food_items` data. Its 24 sample
  dishes are clearly identified as examples when the database is unavailable
  or has no food rows; sample dishes have no MySQL IDs and cannot be checked
  out as database-backed food.
- Servlet code is present for student registration/login with salted
  PBKDF2-HMAC-SHA256 password hashes.
- Servlet code is present for student checkout, server-side price and
  availability checks, order transactions, order history, and bills.
- Admin servlet routes are present for food changes, availability, recent
  orders, and status updates. These actions require an `ADMIN` session.
- There is no separate kitchen login in the confirmed existing schema: the
  current `users.role` enum supports only `STUDENT` and `ADMIN`. Admins can view
  orders and set the confirmed preparation statuses.
- The admin status choices are limited to `PLACED`, `ACCEPTED`, and `PREPARING`,
  the values confirmed for the existing database. Other enum values are not
  assumed.
- `ONLINE` is a simulated payment choice. The project does not collect or send
  real payment details.

The Java/JDBC account, ordering, and admin routes have not been tested with
MySQL in the current environment; see the verification section below.

## Main modules and workflow

The static homepage and menu link to the menu, cart, login, orders, and admin
routes. The menu stores selected items in browser `localStorage` using the
existing `cart` key. Database-backed foods also carry a `foodId`; sample menu
items intentionally do not. At checkout, the servlet is designed to accept
food IDs and quantities, look up current prices from MySQL, and save the order
and its items together. Students' order queries are restricted to their
session user ID; admin changes check the session role.

The confirmed schema relationships used by the queries are `orders.user_id` to
`users.user_id`, `order_items.order_id` to `orders.order_id`, and
`order_items.food_id` to `food_items.food_id`. The actual MySQL foreign-key
constraints were not independently inspected. A Mermaid architecture diagram
and ER diagram are in
[`PROJECT_REPORT_AND_VIVA.md`](./PROJECT_REPORT_AND_VIVA.md).

## Existing project layout

```text
Canteen/
  index.html
  menu.html
  cart.html
  site.css
  pom.xml
  README.md
  PROJECT_REPORT_AND_VIVA.md
  REVIEW_1_PRESENTATION.pptx
  CodeCravers_Review_1.pptx
  WEB-INF/
    web.xml
    classes/
      CanteenServlet.java
      DatabaseConnection.java
      PasswordUtil.java
      HelloServlet.java
      *.class (current deployed compilation output)
    lib/
      mysql-connector-j-8.4.0.jar
```

The Java sources are kept in the existing deployed `WEB-INF/classes` folder
rather than a conventional `src/main/java` folder. The Maven project uses that
folder as its source directory, compiles to the configured build folder
outside `webapps`, and includes the static pages, `site.css`, and `web.xml` in
the WAR. The running expanded webapp has the POM-declared Connector/J jar in
`WEB-INF/lib`. Maven was not available for a WAR packaging verification.

## Existing database

The application expects the existing database named `canteen_db`, with these
confirmed columns:

- `users`: `user_id`, `name`, `email`, `password`, `role`, `created_at`
- `food_items`: `food_id`, `name`, `description`, `price`, `available`
- `orders`: `order_id`, `user_id`, `total_amount`, `payment_method`, `status`,
  `created_at`
- `order_items`: `order_item_id`, `order_id`, `food_id`, `quantity`, `price`

This project does not create, drop, or alter these tables. No setup SQL is
included because the database already exists. The implementation relies on
`orders.order_id` being an auto-increment key that MySQL can return after insert.

## Build requirements

- JDK 11 or newer. The code is compiled to Java 11 bytecode.
- Apache Tomcat 10.1.
- Apache Maven 3.8 or newer.
- MySQL Server with the existing `canteen_db` database.

Tomcat 10.1 provides Jakarta Servlet 6.0. Maven therefore marks the Servlet API
as `provided`; it is not copied into the application. MySQL Connector/J is a
runtime dependency and is included in the built WAR under `WEB-INF/lib`.

## Database connection settings

The app reads these environment variables; it never stores the password in code:

- `CANTEEN_DB_URL`, normally `jdbc:mysql://localhost:3306/canteen_db`
- `CANTEEN_DB_USER`, your local MySQL account name
- `CANTEEN_DB_PASSWORD`, that account's password

In a PowerShell window used only to start Tomcat, set the URL and user first.
For the password, use the secure prompt below (do not put a real password in
this README, Java source, or a command you save):

```powershell
$env:CANTEEN_DB_URL = 'jdbc:mysql://localhost:3306/canteen_db'
$env:CANTEEN_DB_USER = Read-Host 'MySQL username'
$secret = Read-Host 'MySQL password' -AsSecureString
$pointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secret)
try {
    $env:CANTEEN_DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($pointer)
} finally {
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($pointer)
}
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot'
$env:CATALINA_HOME = 'C:\apache-tomcat-10.1.60'
& "$env:CATALINA_HOME\bin\startup.bat"
```

Environment variables are inherited by child processes, so start Tomcat from
the same PowerShell window after setting them. Restart Tomcat after changing
settings. Do not paste secrets into chat or commit them.

## Build and deploy

From PowerShell:

```powershell
Set-Location 'C:\apache-tomcat-10.1.60\webapps\Canteen'
mvn clean package
```

The WAR is written outside the current deployed app:

```text
C:\apache-tomcat-10.1.60\build\CodeCraversCanteen\CodeCraversCanteen.war
```

Deploy it under its own context name so the existing `/Canteen` directory is
not overwritten:

```powershell
Copy-Item 'C:\apache-tomcat-10.1.60\build\CodeCraversCanteen\CodeCraversCanteen.war' `
  'C:\apache-tomcat-10.1.60\webapps\CodeCraversCanteen.war'
```

Tomcat deploys the WAR at `http://localhost:8080/CodeCraversCanteen/`. Maven was
not available during the current coding session, so this build and WAR
deployment have not yet been verified.

## First admin account

Registration always creates a `STUDENT`. To grant admin access, register the
intended account, then use MySQL Workbench while signed into your own database
account to change only that account's `role` to `ADMIN`. Confirm the email
carefully before running the update. This is a database data change; this app
does not make that change automatically and does not contain a hardcoded admin
password. Do not share the account password. In Workbench, first confirm the
account:

```sql
SELECT user_id, name, email, role
FROM users
WHERE email = 'the-exact-email-you-registered';
```

Only after confirming that it is the intended account, update that one unique
email:

```sql
UPDATE users
SET role = 'ADMIN'
WHERE email = 'the-exact-email-you-registered';
```

Do not run the update with a blank or guessed email.

The app expects new passwords saved by this project to use its PBKDF2 format.
Existing account password formats were not verified. If old passwords are
plain text or use another hash format, the app intentionally will not accept
them as though they were a secure PBKDF2 hash.

## Manual test checklist

1. Open the homepage, menu, and cart on desktop and a narrow/mobile viewport.
2. Confirm live food rows and availability appear when MySQL is configured.
3. Register a student and verify that a repeated email receives an error.
4. Log in with the new student; try a wrong password and verify it is rejected.
5. Add menu items to the cart; change quantities, remove an item, and clear it.
6. Place a cash order; confirm the bill has the server-calculated total.
7. Try refreshing/replaying the order submission; it should not create a second
   order using the same one-time checkout token.
8. Open order history and confirm it lists only the logged-in student's orders.
9. Register a separate account and grant it `ADMIN` in Workbench. Verify that
   it can add/edit food, mark availability, view orders, and select only the
   confirmed order statuses.
10. Log in as a student and try `/app/admin`; access should be denied.

## Verification status for this workspace

- Java source compiled with `javac --release 11` against Tomcat's Servlet API.
- Password hashing was checked: the right password verified and a wrong one
  did not.
- Tomcat served the homepage, menu, cart, registration form, and login form.
- Student-only and admin-only pages redirected an unauthenticated visitor to
  login.
- Browser checks confirmed that all 24 sample dishes render, all four category
  filters show the expected counts (8, 8, 4, and 4), search finds a dish, and
  adding an item preserves the `cart` storage format. Quantity increase/decrease,
  item removal, clear-cart, empty-cart, and total updates were also checked.
- Homepage, menu, cart, login, and registration had no horizontal overflow at
  390 pixels, and their navigation labels and shared theme were present. This
  does not replace a full visual review or assistive-technology check.
- The menu API returned `503` because no database settings were configured.
- Maven is unavailable, so WAR packaging has not been verified. The
  POM-declared Connector/J 8.4.0 jar was available in the local cache, was
  added to this expanded webapp, and its JDBC driver class-load test passed.
  Registration, actual MySQL queries, checkout, and admin updates have not
  been tested against the database.
- Responsive CSS breakpoints are present, but a full visual review on desktop
  and mobile devices remains to be done.

## Review 1 readiness

The application uses a simple two-part structure: static pages and the shared
stylesheet are served by Tomcat, and `/app/*` requests are handled by
`CanteenServlet`. The servlet reads the existing database through
`DatabaseConnection` and JDBC. The cart itself is stored in browser
`localStorage` until checkout.

The Mermaid architecture and ER diagrams, a concise Review 1 checklist, and a
slide-by-slide presentation outline are in
[`PROJECT_REPORT_AND_VIVA.md`](./PROJECT_REPORT_AND_VIVA.md).
The current editable, 16-slide Review 1 deck is
[`CodeCravers_Review_1.pptx`](./CodeCravers_Review_1.pptx). Fill in team,
roll-number, university, and department placeholders before presenting. Its UI
screenshots show the actual local homepage, sample-fallback menu, and empty
cart; database-backed behavior is explicitly marked unverified.


