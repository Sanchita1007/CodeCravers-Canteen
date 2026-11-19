# CodeCravers – Software Testing Documentation

## 1. Project Information

- **Project:** CodeCravers Canteen Food Ordering System
- **University:** Galgotias University
- **Programme:** B.Tech CSE (AIML)
- **Project Type:** Java Web Based Application
- **Testing Method:** Manual Functional Testing
- **Testing Status:** Completed for the listed live functionality checks

## 2. Testing Objectives

- Verify student and administrator functionality.
- Verify cloud database connectivity.
- Validate food ordering and billing operations.
- Verify role-based access control.
- Identify and resolve application errors.

## 3. Testing Environment

| Component | Technology |
|---|---|
| Operating System | Windows 11 |
| Backend | Java 21 and Jakarta Servlets |
| Application Server | Apache Tomcat 10.1 |
| Local Database | MySQL |
| Cloud Database | TiDB Cloud |
| Database Connectivity | JDBC |
| Deployment | Render and Docker |

**Live Website:** https://codecravers-canteen.onrender.com/

## 4. Live Functional Test Cases

| Test ID | Feature | Input / Action | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|
| TC01 | Database connectivity | Open live application | Connect to cloud database | Connection successful | PASS |
| TC02 | Food menu | Open menu | Display database food items | Live menu displayed | PASS |
| TC03 | Registration | Enter new student details | Create account | Account created | PASS |
| TC04 | Student login | Enter valid credentials | Login successful | Student logged in | PASS |
| TC05 | Shopping cart | Add food items | Display items and calculate total | Items and total displayed | PASS |
| TC06 | Checkout | Add valid database food items | Enable Place Order button | Checkout enabled after clearing stale cart | PASS |
| TC07 | Order placement | Submit test order | Place order successfully | Order placed | PASS |
| TC08 | Bill generation | Complete order | Generate bill | Bill displayed | PASS |
| TC09 | Order history | Open My Orders | Display previous orders | Order history displayed | PASS |
| TC10 | Admin login | Enter admin credentials | Authenticate administrator | Admin logged in | PASS |
| TC11 | Admin dashboard | Open Admin page | Display management options | Dashboard displayed | PASS |
| TC12 | Add food item | Add Chocolate Shake at Rs. 80 | New item appears in menu | Item displayed | PASS |
| TC13 | Update food price | Change price to Rs. 100 | Updated price displayed | Price updated | PASS |
| TC14 | Remove food item | Remove Chocolate Shake | Item no longer available | Item removed from available menu | PASS |
| TC15 | Unauthorized access | Access Admin as a student | Deny access | Access denied | PASS |

## 5. Additional Local Negative Testing

| Test ID | Feature | Input / Action | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|
| NT01 | Invalid login | Enter incorrect credentials | Reject login | Login denied | PASS |
| NT02 | Empty cart | Attempt checkout without items | Prevent checkout | Checkout prevented | PASS |
| NT03 | Unauthorized access | Open Admin as a student | Return HTTP 403 | Access denied | PASS |

## 6. Bugs Identified and Resolved

| Bug ID | Problem | Solution | Status |
|---|---|---|---|
| BUG01 | Tomcat shutdown port warnings | Disabled shutdown port in Dockerfile | Resolved |
| BUG02 | HTTP 503: Database unavailable | Configured TiDB Cloud and Render environment variables | Resolved with BUG03 fix |
| BUG03 | JDBC connectivity failure | Added explicit MySQL JDBC driver loading | Resolved |
| BUG04 | Place Order button disabled | Cleared stale sample-menu cart data and added fresh database items | Resolved during retesting |

## 7. Live Testing Summary

| Metric | Result |
|---|---|
| Total live test cases | 15 |
| Passed | 15 |
| Failed after retesting | 0 |
| Pass percentage | 100% |
| Testing method | Manual |

## 8. Pending Testing

- Formal mobile responsiveness testing
- Automated unit testing
- Performance and load testing
- Comprehensive security testing

## 9. Conclusion

The CodeCravers Canteen Food Ordering System successfully passed all 15 documented manual live functionality tests after troubleshooting and retesting.

The student module, administrator module, cloud database connectivity, order placement, automatic billing and role-based access control were tested successfully.

These results demonstrate the functionality of the tested features and do not represent a comprehensive security or performance certification.

---

**Prepared by Team CodeCravers | Galgotias University**
