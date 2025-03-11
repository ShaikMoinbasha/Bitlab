package com.talentstream.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
 
import com.fasterxml.jackson.annotation.JsonFormat;
 
@Entity
public class RazorPayOrder {
	
	@Id
	@Column(name = "order_id",nullable = false,updatable = false)
	private String orderId;
	
	
    @ManyToOne(fetch = FetchType.EAGER)  // Unidirectional relationship
	@JoinColumn(name = "recruiter_id", referencedColumnName = "recruiterId")  // Foreign key in RazorPayOrder pointing to JobRecruiter
    private JobRecruiter jobRecruiter;
	
	@Column(name = "order_amount",nullable = false)
	private Double orderAmount;
	
	@Column(name ="currency",nullable = false)
	private String currency;  
	
	@Column(name = "order_date",nullable = false,updatable = false)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
	private LocalDateTime orderDate;
	
	@Column(name = "order_status",nullable =false)
	private String orderStatus;
	
	@Column(columnDefinition = "BOOLEAN DEFAULT false")
	private boolean isActive;
 
	
	@Column(name = "created_at",nullable = false,updatable = false)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
	private LocalDateTime createdAt;
	
	@Column(name = "updated_at",nullable = true)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime updatedAt;
	
	
	public RazorPayOrder() {
		super();
	}
 
	public RazorPayOrder(String orderId, JobRecruiter jobRecruiter, Double orderAmount, String currency,
			LocalDateTime orderDate, String orderStatus, boolean isActive, LocalDateTime createdAt,
			LocalDateTime updatedAt) {
		super();
		this.orderId = orderId;
		this.jobRecruiter = jobRecruiter;
		this.orderAmount = orderAmount;
		this.currency = currency;
		this.orderDate = orderDate;
		this.orderStatus = orderStatus;
		this.isActive = isActive;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}
 
	public String getOrderId() {
		return orderId;
	}
 
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
 
	public JobRecruiter getJobRecruiter() {
		return jobRecruiter;
	}
 
	public void setJobRecruiter(JobRecruiter jobRecruiter) {
		this.jobRecruiter = jobRecruiter;
	}
 
	public Double getOrderAmount() {
		return orderAmount;
	}
 
	public void setOrderAmount(Double orderAmount) {
		this.orderAmount = orderAmount;
	}
 
	public String getCurrency() {
		return currency;
	}
 
	public void setCurrency(String currency) {
		this.currency = currency;
	}
 
	public LocalDateTime getOrderDate() {
		return orderDate;
	}
 
	public void setOrderDate(LocalDateTime orderDate) {
		this.orderDate = orderDate;
	}
 
	public String getOrderStatus() {
		return orderStatus;
	}
 
	public void setOrderStatus(String orderStatus) {
		this.orderStatus = orderStatus;
	}
 
	public boolean isActive() {
		return isActive;
	}
 
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
 
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
 
	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
 
	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}
 
	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
	
	
 
}
 