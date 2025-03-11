package com.talentstream.entity;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
 
 
public class CreateOrderRequest {
	@NotNull(message = "Please Provide Amount To Create Order")
	@Positive(message ="Amount Cannot be negetive" )
	private Double amount;
	@NotNull(message = "Please Provide Valid Recruiter Id to Create Order")
	private Long recruiter_id;

	public CreateOrderRequest(Double amount, Long recruiter_id) {
		super();
		this.amount = amount;
		this.recruiter_id = recruiter_id;
	}
	public CreateOrderRequest() {
		super();
	}
	public Double getAmount() {
		return amount;
	}
	public void setAmount(Double amount) {
		this.amount = amount;
	}
	public Long getRecruiter_id() {
		return recruiter_id;
	}
	public void setRecruiter_id(Long recruiter_id) {
		this.recruiter_id = recruiter_id;
	}
	@Override
	public String toString() {
		return "CreateOrderRequest [amount=" + amount + ", recruiter_id=" + recruiter_id + "]";
	}

}

