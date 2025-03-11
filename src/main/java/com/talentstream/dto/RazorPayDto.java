package com.talentstream.dto;


public class RazorPayDto {
	
	private String orderId;
	private Long recruiteId;
	
	
	public RazorPayDto() {
		super();
	}


	public RazorPayDto(String orderId, Long recruiteId) {
		super();
		this.orderId = orderId;
		this.recruiteId = recruiteId;
	}


	public String getOrderId() {
		return orderId;
	}


	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}


	public Long getRecruiteId() {
		return recruiteId;
	}


	public void setRecruiteId(Long recruiteId) {
		this.recruiteId = recruiteId;
	}
	
	
	
	

}

