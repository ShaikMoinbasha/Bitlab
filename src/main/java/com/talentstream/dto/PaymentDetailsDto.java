package com.talentstream.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

public class PaymentDetailsDto {
	
	private String orderId;
	private Long recruiterId;
	private Double amount;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
	private LocalDateTime orderDate;
	private String orderStatus;
	private Boolean isActive;
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	public Long getRecruiterId() {
		return recruiterId;
	}
	public void setRecruiterId(Long recruiterId) {
		this.recruiterId = recruiterId;
	}
	public Double getAmount() {
		return amount;
	}
	public void setAmount(Double amount) {
		this.amount = amount;
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
	public Boolean getIsActive() {
		return isActive;
	}
	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}
	public PaymentDetailsDto(String orderId, Long recruiterId, Double amount, LocalDateTime orderDate,
			String orderStatus, Boolean isActive) {
		super();
		this.orderId = orderId;
		this.recruiterId = recruiterId;
		this.amount = amount;
		this.orderDate = orderDate;
		this.orderStatus = orderStatus;
		this.isActive = isActive;
	}
	public PaymentDetailsDto() {
		super();
	}
	
	

}
