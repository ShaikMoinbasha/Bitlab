package com.talentstream.service;

import java.util.List;
import java.util.Optional;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.razorpay.Order;
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.talentstream.entity.JobRecruiter;
import com.talentstream.entity.RazorPayOrder;
import com.talentstream.exception.CustomException;
import com.talentstream.repository.JobRecruiterRepository;
import com.talentstream.repository.RazorPayRepositry;
import com.talentstream.util.RazorPayUtills;
 
@Service
public class RazorPayService {
 
	@Autowired
	private RazorPayRepositry payRepositry;
 
	@Autowired
	private RazorpayClient razorpayClient;

    @Value("${razorpay.secret}")
    private String razorPaySecret;
    
    private static  final  Logger logger = LoggerFactory.getLogger(RazorPayService.class);
    
    
    @Autowired
    private JobRecruiterRepository jobRecruiterRepository;
    public JobRecruiter getRecruiter(Long recruiterId) throws Exception{
    	Optional<JobRecruiter> jobRecruiter=jobRecruiterRepository.findById(recruiterId);
    	if(jobRecruiter.isPresent()) {
    		return jobRecruiter.get();
    	}
    	 throw new CustomException("JobRecruiter with ID " + recruiterId + " not found.", HttpStatus.NOT_FOUND);
    }
	public Order createOrder(Double amount, String currency,Long recruiterId) throws Exception {

		JSONObject orderRequest = new JSONObject();
		orderRequest.put("amount", amount*100); // amount in paise so we are multiply with 100
		orderRequest.put("currency", currency);
		orderRequest.put("payment_capture", 1);         
		
 
		return razorpayClient.orders.create(orderRequest);
 
	}

 
	public void saveOrder(RazorPayOrder razorPayOrder) {	   
	    payRepositry.save(razorPayOrder);
	}
 
	public boolean verifyPayment(String paymentId, String orderId, String razorpaySignature) {
		  String payload = orderId + '|' + paymentId;
		  try {
		     boolean verifySignature = RazorPayUtills.verifySignature(payload, razorpaySignature, razorPaySecret);
		     logger.info("Verified Signature: {}",verifySignature);
		     return verifySignature;
		  } catch (Exception e) {
		    e.printStackTrace();
		    return false;
		  }
		}

 
	public String getPaymentStatus(String payment_id) throws RazorpayException {
		Payment payment = razorpayClient.payments.fetch(payment_id);
      	return payment.get("status");
		
	}
 
 
	public Optional<RazorPayOrder> getOrderById(String orderId) throws RuntimeException {
		return payRepositry.findById(orderId);
	}
 
	public void updateOrderDetails(RazorPayOrder razorPayOrder) {
		payRepositry.save(razorPayOrder);
	}

 
    public  List<RazorPayOrder> getPaymentDetilsById(Long recruiterId) {
    	logger.info("Recruiter id:{} ",recruiterId);
        return payRepositry.findPaymentDetails(recruiterId);
    }
}

