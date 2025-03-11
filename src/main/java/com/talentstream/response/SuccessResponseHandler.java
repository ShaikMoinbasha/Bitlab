package com.talentstream.response;

public class SuccessResponseHandler {
	
	
	private Integer status;
	private Object data;
	
	public SuccessResponseHandler(Integer status,Object data) {
		this.status=status;
		this.data=data;
	}
 
	public Integer getStatus() {
		return status;
	}
 
	public void setStatus(Integer status) {
		this.status = status;
	}
 
	public Object getData() {
		return data;
	}
 
	public void setData(Object data) {
		this.data = data;
	}
	
	
}
 
