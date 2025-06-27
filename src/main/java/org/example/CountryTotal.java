package org.example;

// Simple class to hold country totals
public class CountryTotal {
    public String country;
    public double totalAmount;

    public CountryTotal() {}

    public CountryTotal(String country, double totalAmount) {
        this.country = country;
        this.totalAmount = totalAmount;
    }

    @Override
    public String toString() {
        return "CountryTotal{country='" + country + "', totalAmount=" + totalAmount + "}";
    }
}
