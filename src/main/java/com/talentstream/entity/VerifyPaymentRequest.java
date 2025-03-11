package com.talentstream.entity;

import javax.validation.constraints.NotBlank;

public class VerifyPaymentRequest {
	
	@NotBlank(message = "Please Provide Payment Id To Verify Payment Details")
	private String paymentId;
	
	@NotBlank(message = "Please Provide Order Id To Verify Payment Details")
	private String orderId;
	
	@NotBlank(message = "Please Provide Signature To Verify Payment Details")
	private String signature;
	
	
	public VerifyPaymentRequest(String paymentId, String orderId, String signature) {
		super();
		this.paymentId = paymentId;
		this.orderId = orderId;
		this.signature = signature;
	}
	public String getPaymentId() {
		return paymentId;
	}
	public void setPaymentId(String paymentId) {
		this.paymentId = paymentId;
	}
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	public String getSignature() {
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}
	public VerifyPaymentRequest() {
		super();
	}
	
	
 
}
 
 