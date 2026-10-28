package com.payroll;

import java.math.BigDecimal;
import java.util.Objects;

public class Employee {
    private final String id;
    private final String name;
    private final EmployeeType employeeType;
    private final BigDecimal payRate;
    private final boolean isUnionMember;
    private final boolean hasRetirement;

    // Constructor to initialize an immutable Employee object
    public Employee(String id, String name, EmployeeType employeeType, BigDecimal payRate,
                    boolean isUnionMember, boolean hasRetirement) {
        // Validation ensures bad data never enters your pipeline
        if (payRate == null || payRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Pay rate cannot be null or negative");
        }
        this.id = Objects.requireNonNull(id, "ID cannot be null");
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        this.employeeType = Objects.requireNonNull(employeeType, "Employee type cannot be null");
        this.payRate = payRate;
        this.isUnionMember = isUnionMember;
        this.hasRetirement = hasRetirement;
    }

    // Standard Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public EmployeeType getEmployeeType() { return employeeType; }
    public BigDecimal getPayRate() { return payRate; }
    public boolean isUnionMember() { return isUnionMember; }
    public boolean hasRetirement() { return hasRetirement; }

    // Overridden for reliable verification in JUnit tests
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        return isUnionMember == employee.isUnionMember &&
                hasRetirement == employee.hasRetirement &&
                Objects.equals(id, employee.id) &&
                Objects.equals(name, employee.name) &&
                employeeType == employee.employeeType &&
                // Using compareTo for BigDecimal ignores scale mismatches (e.g., 10.0 vs 10.00)
                (payRate != null && employee.payRate != null && payRate.compareTo(employee.payRate) == 0);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, employeeType, payRate, isUnionMember, hasRetirement);
    }

    // Overridden to print readable logs instead of memory references during test failures
    @Override
    public String toString() {
        return "Employee{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", employeeType=" + employeeType +
                ", payRate=" + payRate +
                ", isUnionMember=" + isUnionMember +
                ", hasRetirement=" + hasRetirement +
                '}';
    }
}
