package com.payroll;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PayrollProcessor {

    // Global rounding configuration: 2 decimal places, rounding half-up (standard banking style)
    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    private static final double MAX_PART_TIME_HOURS = 120.0;
    
    // Using string constructors for perfect precision constants
    private static final BigDecimal RETIREMENT_RATE = new BigDecimal("0.05");
    private static final BigDecimal HEALTH_INSURANCE_FLAT = new BigDecimal("150.00");
    private static final BigDecimal UNION_DUES_FLAT = new BigDecimal("50.00");

    /**
     * Rule 1: Calculate Gross Pay based on employee type.
     */
    public BigDecimal calculateGrossPay(Employee employee, double hoursOrDays) {
        Objects.requireNonNull(employee, "Employee cannot be null");
        BigDecimal rate = employee.getPayRate();

        BigDecimal gross = switch(employee.getEmployeeType()) {
            // Full-time gets a fixed monthly rate regardless of hours worked
            case FULL_TIME -> rate;
            // Part-time hours are capped at 120 hours max per month
            case PART_TIME -> rate.multiply(BigDecimal.valueOf(Math.min(hoursOrDays, MAX_PART_TIME_HOURS)));
            // Contractor pays daily rate x days worked
            case CONTRACTOR -> rate.multiply(BigDecimal.valueOf(hoursOrDays));
        };

        return gross.setScale(SCALE, ROUNDING_MODE);
    }

    /**
     * Rule 2: Progressive Tax Bracket Calculation.
     * Worked example test match: $4,000 gross should yield exactly $400.00 tax.
     */
    public BigDecimal calculateTax(BigDecimal grossPay) {
        if (grossPay == null || grossPay.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(SCALE, ROUNDING_MODE);
        }

        BigDecimal totalTax = BigDecimal.ZERO;

        // Bracket 1: Up to $1000 @ 0%
        // Bracket 2: $1000 to $3000 @ 10% (Cap span: $2000)
        // Bracket 3: $3000 to $5000 @ 20% (Cap span: $2000)
        // Bracket 4: Above $5000 @ 30%

        // Bracket 2 processing
        if (grossPay.compareTo(new BigDecimal("1000")) > 0) {
            BigDecimal taxableInBracket = grossPay.min(new BigDecimal("3000"))
                    .subtract(new BigDecimal("1000"));
            totalTax = totalTax.add(taxableInBracket.multiply(new BigDecimal("0.10")));
        }

        // Bracket 3 processing
        if (grossPay.compareTo(new BigDecimal("3000")) > 0) {
            BigDecimal taxableInBracket = grossPay.min(new BigDecimal("5000"))
                    .subtract(new BigDecimal("3000"));
            totalTax = totalTax.add(taxableInBracket.multiply(new BigDecimal("0.20")));
        }

        // Bracket 4 processing
        if (grossPay.compareTo(new BigDecimal("5000")) > 0) {
            BigDecimal taxableInBracket = grossPay.subtract(new BigDecimal("5000"));
            totalTax = totalTax.add(taxableInBracket.multiply(new BigDecimal("0.30")));
        }

        return totalTax.setScale(SCALE, ROUNDING_MODE);
    }

    /**
     * Rules 3, 4, 5: Dynamic Deductions Processing
     */
    public Map<String, BigDecimal> calculateDeductions(Employee employee, BigDecimal grossPay) {
        Objects.requireNonNull(employee, "Employee cannot be null");
        
        // LinkedHashMap keeps items in insertion order for nicer printing
        Map<String, BigDecimal> deductions = new LinkedHashMap<>();

        // Rule 3: Health Insurance ($150 flat for FULL_TIME only)
        if (employee.getEmployeeType() == EmployeeType.FULL_TIME) {
            deductions.put("Health Insurance", HEALTH_INSURANCE_FLAT.setScale(SCALE, ROUNDING_MODE));
        }

        // Rule 4: Retirement Contribution (5% of gross if optional flag is true)
        if (employee.hasRetirement() && grossPay != null) {
            BigDecimal retirement = grossPay.multiply(RETIREMENT_RATE).setScale(SCALE, ROUNDING_MODE);
            deductions.put("Retirement", retirement);
        }

        // Rule 5: Union Dues ($50 flat for union members)
        if (employee.isUnionMember()) {
            deductions.put("Union Dues", UNION_DUES_FLAT.setScale(SCALE, ROUNDING_MODE));
        }

        return deductions;
    }

    /**
     * Step 5a: Generate a single Payslip
     */
    public PaySlip generatePaySlip(Employee employee, double hoursOrDays) {
        BigDecimal grossPay = calculateGrossPay(employee, hoursOrDays);
        BigDecimal taxAmount = calculateTax(grossPay);
        Map<String, BigDecimal> deductionsMap = calculateDeductions(employee, grossPay);

        // Sum all dynamic deductions
        BigDecimal totalDeductions = BigDecimal.ZERO;
        for (BigDecimal value : deductionsMap.values()) {
            totalDeductions = totalDeductions.add(value);
        }

        // Net Pay = Gross - Tax - Total Deductions
        BigDecimal netPay = grossPay.subtract(taxAmount).subtract(totalDeductions).setScale(SCALE, ROUNDING_MODE);

        return new PaySlip(employee, grossPay, taxAmount, deductionsMap, netPay);
    }

    /**
     * Step 5b: Process a bulk monthly batch payroll
     * Strictly conforms to specs: processMonthlyPayroll(employeeList)
     * For non-FULL_TIME employees, we fallback to standard full monthly output thresholds 
     * (160 hours for part-time, 20 days for contractors) if no custom variance map is used.
     */
    public List<PaySlip> processMonthlyPayroll(List<Employee> employeeList) {
        Objects.requireNonNull(employeeList, "Employee list cannot be null");
        List<PaySlip> paySlips = new ArrayList<>();
        
        for (Employee employee : employeeList) {
            double defaultUnits = switch (employee.getEmployeeType()) {
                case FULL_TIME -> 0.0; // Full time pay ignores this value completely
                case PART_TIME -> 160.0; // Will be safely capped to 120.0 via calculateGrossPay
                case CONTRACTOR -> 20.0; // Standard 4-week work month
            };
            paySlips.add(generatePaySlip(employee, defaultUnits));
        }
        return paySlips;
    }
}
