package org.example;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Order {

    // Field names must match JSON exactly - no annotations needed
    public String order_id;
    public String timestamp;
    public String country;
    public double amount;

    // Default constructor is required for deserialization
    public Order() {}

    // Optional: Constructor for manual creation
    public Order(String order_id, String timestamp, String country, double amount) {
        this.order_id = order_id;
        this.timestamp = timestamp;
        this.country = country;
        this.amount = amount;
    }

    // Override toString for easy printing
    @Override
    public String toString() {
        return "Order{" +
                "order_id='" + order_id + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", country='" + country + '\'' +
                ", amount=" + amount +
                '}';
    }

}
