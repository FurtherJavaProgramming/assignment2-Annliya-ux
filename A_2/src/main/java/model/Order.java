package model;

import java.sql.Timestamp;
import java.util.List;

public class Order {
    private int orderId;
    private String username;
    private double finalPrice;
    private String status;
    private Timestamp orderDatetime;
    private List<OrderDetail> orderDetails;

    public Order(String username, double finalPrice, String status, Timestamp orderDatetime, List<OrderDetail> orderDetails) {
        this.username = username;
        this.finalPrice = finalPrice;
        this.status = status;
        this.orderDatetime = orderDatetime;
        this.orderDetails = orderDetails;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getUsername() {
        return username;
    }

    public double getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(double finalPrice) {
        this.finalPrice = finalPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<OrderDetail> getOrderDetails() {
        return orderDetails;
    }

    public void setOrderDetails(List<OrderDetail> orderDetails) {
        this.orderDetails = orderDetails;
    }
    
    public Timestamp getOrderDatetime() {
        return orderDatetime;
    }

    public void setOrderDatetime(Timestamp orderDatetime) {
        this.orderDatetime = orderDatetime;
    }
    
    public void addOrderDetail(OrderDetail orderDetail) {
        this.orderDetails.add(orderDetail);
        this.finalPrice += orderDetail.getTotalPrice();
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", username='" + username + '\'' +
                ", finalPrice=" + finalPrice +
                ", status='" + status + '\'' +
                ", orderDetails=" + orderDetails +
                '}';
    }
}
