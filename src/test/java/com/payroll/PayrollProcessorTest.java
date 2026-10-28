package com.payroll;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PayrollProcessorTest {

    private PayrollProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new PayrollProcessor();
    }

    @Test
    @DisplayName("Rule 1: Full-time gross pay must equal fixed monthly rate regardless of hours input")
    void testCalculateGrossPayFullTime() {
        Employee employee = new Employee("FT01", "Alice", EmployeeType.FULL_TIME, new BigDecimal("5000.00"), false, false);
        
        BigDecimal grossPay = processor.calculateGrossPay(employee, 160.0);
        
        assertEquals(0, new BigDecimal("5000.00").compareTo(grossPay));
    }

    @Test
    @DisplayName("Rule 1: Part-time gross pay tracks hourly rate within allowable thresholds")
    void testCalculateGrossPayPartTimeUnderCap() {
        Employee employee = new Employee("PT01", "Bob", EmployeeType.PART_TIME, new BigDecimal("20.00"), false, false);
        
        BigDecimal grossPay = processor.calculateGrossPay(employee, 80.0);
        
        assertEquals(0, new BigDecimal("1600.00").compareTo(grossPay));
    }

    @Test
    @DisplayName("Rule 1: Part-time gross pay must be strictly capped at 120 hours")
    void testCalculateGrossPayPartTimeOverCap() {
        Employee employee = new Employee("PT02", "Charlie", EmployeeType.PART_TIME, new BigDecimal("20.00"), false, false);
        
        // Passing 150 hours, but calculation must cap at 120 hours ($2,400.00)
        BigDecimal grossPay = processor.calculateGrossPay(employee, 150.0);
        
        assertEquals(0, new BigDecimal("2400.00").compareTo(grossPay));
    }

    @Test
    @DisplayName("Rule 1: Contractor gross pay must equal daily rate multiplied by days worked")
    void testCalculateGrossPayContractor() {
        Employee employee = new Employee("C01", "David", EmployeeType.CONTRACTOR, new BigDecimal("500.00"), false, false);
        
        BigDecimal grossPay = processor.calculateGrossPay(employee, 22.0);
        
        assertEquals(0, new BigDecimal("11000.00").compareTo(grossPay));
    }

    @Test
    @DisplayName("Rule 2: Progressive tax example match ($4,000 gross must equal exactly $400 tax)")
    void testCalculateTaxWorkedExample() {
        BigDecimal grossPay = new BigDecimal("4000.00");
        
        BigDecimal tax = processor.calculateTax(grossPay);
        
        // $1000 @ 0% = $0
        // $2000 @ 10% = $200
        // $1000 @ 20% = $200
        // Total = $400.00
        assertEquals(0, new BigDecimal("400.00").compareTo(tax));
    }

    @ParameterizedTest
    @CsvSource({
        "0.00,    0.00",    // Zero income
        "500.00,  0.00",    // Entirely inside Bracket 1 (0%)
        "1000.00, 0.00",    // Exact boundary of Bracket 1
        "1500.00, 50.00",   // $500 inside Bracket 2 (10%)
        "3000.00, 200.00",  // Exact boundary of Bracket 2
        "3500.00, 300.00",  // $500 inside Bracket 3 (20%)
        "5000.00, 600.00",  // Exact boundary of Bracket 3 ($200 + $400)
        "6000.00, 900.00"   // $1000 inside Bracket 4 (30%) -> $600 + $300
    })
    @DisplayName("Rule 2: Progressive tax system applies proper math across all structural brackets")
    void testCalculateTaxProgressiveBrackets(String grossStr, String expectedTaxStr) {
        BigDecimal grossPay = new BigDecimal(grossStr);
        BigDecimal expectedTax = new BigDecimal(expectedTaxStr);
        
        BigDecimal calculatedTax = processor.calculateTax(grossPay);
        
        assertEquals(0, expectedTax.compareTo(calculatedTax), 
            String.format("Gross %s expected tax %s but got %s", grossStr, expectedTaxStr, calculatedTax));
    }

    @Test
    @DisplayName("Rule 3: Health insurance deduction applies strictly to full-time staff")
    void testCalculateDeductionsHealthInsuranceGating() {
        Employee fullTime = new Employee("E1", "FT", EmployeeType.FULL_TIME, new BigDecimal("3000"), false, false);
        Employee partTime = new Employee("E2", "PT", EmployeeType.PART_TIME, new BigDecimal("25"), false, false);

        Map<String, BigDecimal> ftDeductions = processor.calculateDeductions(fullTime, new BigDecimal("3000"));
        Map<String, BigDecimal> ptDeductions = processor.calculateDeductions(partTime, new BigDecimal("2000"));

        assertTrue(ftDeductions.containsKey("Health Insurance"));
        assertEquals(0, new BigDecimal("150.00").compareTo(ftDeductions.get("Health Insurance")));
        assertFalse(ptDeductions.containsKey("Health Insurance"));
    }

    @Test
    @DisplayName("Rule 4: Retirement contribution tracks exactly 5% of gross if flag is enabled")
    void testCalculateDeductionsRetirementGating() {
        Employee withRetirement = new Employee("E1", "Alpha", EmployeeType.CONTRACTOR, new BigDecimal("500"), false, true);
        Employee noRetirement = new Employee("E2", "Beta", EmployeeType.CONTRACTOR, new BigDecimal("500"), false, false);
        BigDecimal gross = new BigDecimal("4000.00");

        Map<String, BigDecimal> ded1 = processor.calculateDeductions(withRetirement, gross);
        Map<String, BigDecimal> ded2 = processor.calculateDeductions(noRetirement, gross);

        // 5% of $4000 = $200.00
        assertTrue(ded1.containsKey("Retirement"));
        assertEquals(0, new BigDecimal("200.00").compareTo(ded1.get("Retirement")));
        assertFalse(ded2.containsKey("Retirement"));
    }

    @Test
    @DisplayName("Rule 5: Union dues are parsed flatly if member flag is validated")
    void testCalculateDeductionsUnionGating() {
        Employee unionMember = new Employee("E1", "U1", EmployeeType.CONTRACTOR, new BigDecimal("400"), true, false);
        Employee nonUnion = new Employee("E2", "U2", EmployeeType.CONTRACTOR, new BigDecimal("400"), false, false);

        Map<String, BigDecimal> ded1 = processor.calculateDeductions(unionMember, new BigDecimal("2000"));
        Map<String, BigDecimal> ded2 = processor.calculateDeductions(nonUnion, new BigDecimal("2000"));

        assertTrue(ded1.containsKey("Union Dues"));
        assertEquals(0, new BigDecimal("50.00").compareTo(ded1.get("Union Dues")));
        assertFalse(ded2.containsKey("Union Dues"));
    }

    @Test
    @DisplayName("Step 5a: Single generated PaySlip accurately balances Gross, Tax, Deductions, and Net values")
    void testGeneratePaySlipCorrectMathFlow() {
        // Full time employee: Gross $4000 (fixed rate)
        // Tax: $400 (worked example value)
        // Deductions: Health ($150) + Retirement (5% of $4000 = $200) + Union ($50) = $400
        // Expected Net: $4000 - $400 - $400 = $3200.00
        Employee employee = new Employee("FT-ALL", "Full Options", EmployeeType.FULL_TIME, new BigDecimal("4000.00"), true, true);
        
        PaySlip slip = processor.generatePaySlip(employee, 0); // Hours ignored for FT

        assertEquals(0, new BigDecimal("4000.00").compareTo(slip.getGrossPay()));
        assertEquals(0, new BigDecimal("400.00").compareTo(slip.getTaxAmount()));
        assertEquals(3, slip.getDeductions().size());
        assertEquals(0, new BigDecimal("3200.00").compareTo(slip.getNetPay()));
    }

    @Test
    @DisplayName("Step 5b: Bulk monthly batch processor returns a valid payslip index mapped to all items")
    void testProcessMonthlyPayrollBatchSize() {
        List<Employee> batch = List.of(
            new Employee("1", "A", EmployeeType.FULL_TIME, new BigDecimal("3000"), false, false),
            new Employee("2", "B", EmployeeType.PART_TIME, new BigDecimal("30"), false, false),
            new Employee("3", "C", EmployeeType.CONTRACTOR, new BigDecimal("400"), false, false)
        );

        List<PaySlip> slips = processor.processMonthlyPayroll(batch);

        assertNotNull(slips);
        assertEquals(3, slips.size());
    }
}
