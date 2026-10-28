package com.payroll;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

public class PaySlip {
    private final Employee employee;
    private final BigDecimal grossPay;
    private final BigDecimal taxAmount;
    private final Map<String, BigDecimal> deductions;
    private final BigDecimal netPay;

    public PaySlip(Employee employee, BigDecimal grossPay, BigDecimal taxAmount,
                   Map<String, BigDecimal> deductions, BigDecimal netPay) {
        this.employee = Objects.requireNonNull(employee, "Employee cannot be null");
        this.grossPay = Objects.requireNonNull(grossPay, "Gross pay cannot be null");
        this.taxAmount = Objects.requireNonNull(taxAmount, "Tax amount cannot be null");
        this.netPay = Objects.requireNonNull(netPay, "Net pay cannot be null");
        
        // Defensive copy prevents downstream code from modifying deductions after creation
        this.deductions = deductions != null ? Map.copyOf(deductions) : Map.of();
    }

    public Employee getEmployee() { return employee; }
    public BigDecimal getGrossPay() { return grossPay; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public Map<String, BigDecimal> getDeductions() { return deductions; }
    public BigDecimal getNetPay() { return netPay; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaySlip paySlip = (PaySlip) o;
        
        // Using compareTo for financial values to ignore scale/trailing zero mismatches
        return Objects.equals(employee, paySlip.employee) &&
                grossPay.compareTo(paySlip.grossPay) == 0 &&
                taxAmount.compareTo(paySlip.taxAmount) == 0 &&
                netPay.compareTo(paySlip.netPay) == 0 &&
                // Map equality works seamlessly if values evaluate as numerically identical
                areDeductionMapsEqual(this.deductions, paySlip.deductions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(employee, grossPay, taxAmount, deductions, netPay);
    }

    private boolean areDeductionMapsEqual(Map<String, BigDecimal> map1, Map<String, BigDecimal> map2) {
        if (map1.size() != map2.size()) return false;
        for (Map.Entry<String, BigDecimal> entry : map1.entrySet()) {
            BigDecimal val2 = map2.get(entry.getKey());
            if (val2 == null || entry.getValue().compareTo(val2) != 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return String.format(
            "-------------------------------------------\n" +
            "PAYSLIP FOR: %s (%s) [ID: %s]\n" +
            "Gross Pay:   $%s\n" +
            "Tax:         $%s\n" +
            "Deductions:  %s\n" +
            "Net Pay:     $%s\n" +
            "-------------------------------------------",
            employee.getName(), employee.getEmployeeType(), employee.getId(),
            grossPay, taxAmount, deductions, netPay
        );
    }
}
