# Employee Payroll Calculator - Solution Documentation

An enterprise-grade, immutable payroll calculation engine designed and built to run on **Microsoft Build of OpenJDK 21** using **Apache Maven 3.10.0** and **JUnit 5**. This engine calculates gross salaries, maps multi-tier progressive tax brackets cleanly, and enforces conditional workflow gates for benefit deductions.

## 🚀 Build and Run Instructions

### Prerequisites
* **Java Development Kit:** Microsoft Build of OpenJDK (Verified on `21.0.13.0.2`)
* **Build System:** Apache Maven (Verified on `3.10.0`)

### Compilation and Packaging
To clean, compile, and package the application payload binaries, run:
```bash
mvn clean package
```

### Running Automated Test Suites
To execute the comprehensive JUnit 5 verification layer verifying all capping boundaries and tax segments:
```bash
mvn test
```

### Running the Demonstration Application
To fire up the payroll processing simulator containing the 6 sample employee configurations, run:
```bash
mvn exec:java
```

---

## 💡 Architectural Assumptions & Business Logic

### 1. Immutability & Defensive Data Strategies
* **Immutable Domain Entities:** Both `Employee` and `PaySlip` objects use `private final` fields. Once generated, records cannot be tampered with or modified.
* **Defensive Copying:** The `PaySlip` object maps standard dynamic deduction allocations using `Map.copyOf()`. This guards historical payslips against collection tampering from outer runtime frameworks.

### 2. Monetary Precision Safeguards
* Floating-point numbers (`double`, `float`) are structurally banned for tracking currency metrics due to precision calculation drop-offs. 
* All calculations use `BigDecimal` with a standard configuration of 2 decimal places using `RoundingMode.HALF_UP` (banker's rounding mechanism).
* String constructor inputs (`new BigDecimal("1000")`) are prioritized over double parameters to prevent double precision seed errors during tier checking.

### 3. Progressive Tax Structuring Engine
Taxes apply sequentially per individual monetary tier slice, meaning a single gross lump sum is parsed progressively rather than flatly:
* **Tier 1:** Up to $1,000 processed at **0%**
* **Tier 2:** Greater than $1,000 up to $3,000 processed at **10%** 
* **Tier 3:** Greater than $3,000 up to $5,000 processed at **20%**
* **Tier 4:** Anything remaining above $5,000 processed at **30%**

### 4. Bulk Payroll Tracking Adjustments
The required method signature `processMonthlyPayroll(List<Employee> employeeList)` was preserved to strictly meet the original guidelines. For generic standard operational runs, the framework infers standard full monthly metrics if units are not provided (e.g., standard capping of 160 hours down to 120 hours for part-time tracking, or a standard 20-day contractor sequence).

---

## 📊 Sample Output Demonstration

Running the demonstration routine maps out 6 distinct employee personas showcasing all variation pairings:

```text
====== INITIALIZING DEMONSTRATION PAYROLL GENERATOR ======

-------------------------------------------
PAYSLIP FOR: Alice Smith (FULL_TIME) [ID: FT-01]
Gross Pay:   $6000.00
Tax:         $900.00
Deductions:  {Health Insurance=150.00, Retirement=300.00, Union Dues=50.00}
Net Pay:     $4600.00
-------------------------------------------

-------------------------------------------
PAYSLIP FOR: Bob Jones (FULL_TIME) [ID: FT-02]
Gross Pay:   $4000.00
Tax:         $400.00
Deductions:  {Health Insurance=150.00}
Net Pay:     $3450.00
-------------------------------------------

-------------------------------------------
PAYSLIP FOR: Charlie Brown (PART_TIME) [ID: PT-01]
Gross Pay:   $2000.00
Tax:         $100.00
Deductions:  {Union Dues=50.00}
Net Pay:     $1850.00
-------------------------------------------

-------------------------------------------
PAYSLIP FOR: Diana Prince (PART_TIME) [ID: PT-02]
Gross Pay:   $3600.00
Tax:         $320.00
Deductions:  {Retirement=180.00}
Net Pay:     $3100.00
-------------------------------------------

-------------------------------------------
PAYSLIP FOR: Evan Wright (CONTRACTOR) [ID: CON-01]
Gross Pay:   $9000.00
Tax:         $1800.00
Deductions:  {Retirement=450.00}
Net Pay:     $6750.00
-------------------------------------------

-------------------------------------------
PAYSLIP FOR: Fiona Gallagher (CONTRACTOR) [ID: CON-02]
Gross Pay:   $7500.00
Tax:         $1350.00
Deductions:  {Union Dues=50.00}
Net Pay:     $6100.00
-------------------------------------------

====== BATCH PAYROLL EXECUTION COMPLETED SUCCESSFULLY ======
```