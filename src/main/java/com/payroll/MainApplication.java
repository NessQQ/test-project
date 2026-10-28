package com.payroll;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MainApplication {
    public static void main(String[] args) {
        PayrollProcessor processor = new PayrollProcessor();
        List<PaySlip> generatedPaySlips = new ArrayList<>();

        System.out.println("====== INITIALIZING DEMONSTRATION PAYROLL GENERATOR ======\n");

        // 1. Full-time with all deductions active (Gross $6000, Health, Union, 5% Retirement)
        Employee emp1 = new Employee("FT-01", "Alice Smith", EmployeeType.FULL_TIME, 
                new BigDecimal("6000.00"), true, true);
        generatedPaySlips.add(processor.generatePaySlip(emp1, 0)); // hours/days irrelevant for FT

        // 2. Full-time with no options checked (Gross $4000 -> Expected Tax $400, Only Health Insurance applies)
        Employee emp2 = new Employee("FT-02", "Bob Jones", EmployeeType.FULL_TIME, 
                new BigDecimal("4000.00"), false, false);
        generatedPaySlips.add(processor.generatePaySlip(emp2, 0));

        // 3. Part-time within standard limits (80 hours @ $25.00 = Gross $2000) with Union membership
        Employee emp3 = new Employee("PT-01", "Charlie Brown", EmployeeType.PART_TIME, 
                new BigDecimal("25.00"), true, false);
        generatedPaySlips.add(processor.generatePaySlip(emp3, 80));

        // 4. Part-time triggers Hour-Cap limits (140 hours input -> clamps to 120 hours @ $30 = Gross $3600) + Retirement
        Employee emp4 = new Employee("PT-02", "Diana Prince", EmployeeType.PART_TIME, 
                new BigDecimal("30.00"), false, true);
        generatedPaySlips.add(processor.generatePaySlip(emp4, 140));

        // 5. Contractor with Retirement enabled (20 days @ $450.00 = Gross $9000)
        Employee emp5 = new Employee("CON-01", "Evan Wright", EmployeeType.CONTRACTOR, 
                new BigDecimal("450.00"), false, true);
        generatedPaySlips.add(processor.generatePaySlip(emp5, 20));

        // 6. Contractor with Union membership only (15 days @ $500.00 = Gross $7500)
        Employee emp6 = new Employee("CON-02", "Fiona Gallagher", EmployeeType.CONTRACTOR, 
                new BigDecimal("500.00"), true, false);
        generatedPaySlips.add(processor.generatePaySlip(emp6, 15));


        // Print final generated results outputs to standard console out
        for (PaySlip slip : generatedPaySlips) {
            System.out.println(slip);
            System.out.println(); // Adds extra space for premium readability
        }

        System.out.println("====== BATCH PAYROLL EXECUTION COMPLETED SUCCESSFULLY ======");
    }
}
