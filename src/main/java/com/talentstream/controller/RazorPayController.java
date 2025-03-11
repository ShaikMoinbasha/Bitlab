	package com.talentstream.controller;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.razorpay.Order;
import com.razorpay.RazorpayException;
import com.talentstream.dto.PaymentDetailsDto;
import com.talentstream.dto.RazorPayDto;
import com.talentstream.entity.CreateOrderRequest;
import com.talentstream.entity.JobRecruiter;
import com.talentstream.entity.RazorPayOrder;
import com.talentstream.entity.VerifyPaymentRequest;
import com.talentstream.exception.ErrorResponse;
import com.talentstream.response.SuccessResponseHandler;
import com.talentstream.service.RazorPayService;

@RestController
@RequestMapping("/razorPay")
public class RazorPayController {
	private static final Logger logger = LoggerFactory.getLogger(RazorPayController.class);

	@Autowired
	private RazorPayService razorPayService;

	// Post API to Create Payment Order and inserting order details into database
	@PostMapping("/createOrder")
	public ResponseEntity<Object> createOrder(@Valid @RequestBody CreateOrderRequest createOrderDto,
			BindingResult bindingResult) {

		logger.info("Retrieved all jobs successfully.");

		try {

			if (bindingResult.hasErrors()) {
				Map<String, String> errors = new LinkedHashMap<>();
				bindingResult.getFieldErrors().forEach(fieldError -> {
					String fieldName = fieldError.getField();
					String errorMessage = fieldError.getDefaultMessage();
					errors.put(fieldName, errorMessage);
				});
				return ResponseEntity.badRequest().body(errors);
			}
			JobRecruiter job = razorPayService.getRecruiter(createOrderDto.getRecruiter_id());

			logger.info("Calling RazorPay Service method to create order with details {}", createOrderDto);
			Order order = razorPayService.createOrder(createOrderDto.getAmount(), "INR",
					createOrderDto.getRecruiter_id());
			if (order == null) {
				return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
						new ErrorResponse("Unable to create order", HttpStatus.SERVICE_UNAVAILABLE.value(), "Failed"));
			}

			logger.info("New Order Created Successfully Inserting data into Table");
			String orderId = order.get("id");
			LocalDateTime now = LocalDateTime.now();
			RazorPayOrder razorPayOrder = new RazorPayOrder();

			razorPayOrder.setOrderId(orderId);
			razorPayOrder.setJobRecruiter(job);
			razorPayOrder.setOrderAmount(createOrderDto.getAmount());
			razorPayOrder.setCurrency(order.get("currency"));
			razorPayOrder.setOrderStatus(order.get("status"));
			razorPayOrder.setCreatedAt(now);

			razorPayOrder.setOrderDate(
					((Date) order.get("created_at")).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());

			logger.info("Calling save order method to store data into db");
			razorPayService.saveOrder(razorPayOrder);

			RazorPayDto razorPayDto = new RazorPayDto(orderId, createOrderDto.getRecruiter_id());

			logger.info("Returning success response {}", razorPayDto);
			return ResponseEntity.status(HttpStatus.CREATED)
					.body(new SuccessResponseHandler(HttpStatus.OK.value(), razorPayDto));

		} catch (Exception e) {
			logger.error("Some error occured", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorResponse("Internal Error Creating Payment Order: " + e.getMessage(),
							HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server"));
		}
	}

	// Post API To Verify Payment Details and Update Status
	@PostMapping("/verifyPayment")
	public ResponseEntity<Object> verifyPayment(@Valid @RequestBody VerifyPaymentRequest paymentDetails,
			BindingResult bindingResult) {

		logger.info("Coming to verify payment Controller With Request body", paymentDetails);

		try {

			if (bindingResult.hasErrors()) {
				Map<String, String> errors = new LinkedHashMap<>();
				bindingResult.getFieldErrors().forEach(fieldError -> {
					String fieldName = fieldError.getField();
					String errorMessage = fieldError.getDefaultMessage();
					errors.put(fieldName, errorMessage);
				});
				return ResponseEntity.badRequest().body(errors);
			}

			logger.info("Calling service method to verify payment .");

			boolean verifyPayment = razorPayService.verifyPayment(paymentDetails.getPaymentId(),
					paymentDetails.getOrderId(), paymentDetails.getSignature());

			if (verifyPayment) {

				logger.info("Payment verified Successfully updating ordr status created to captured");
				LocalDateTime now = LocalDateTime.now();

				String paymentStatus = razorPayService.getPaymentStatus(paymentDetails.getPaymentId());
				Optional<RazorPayOrder> razorPayOrder = razorPayService.getOrderById(paymentDetails.getOrderId());
				if (razorPayOrder.isPresent()) {
					RazorPayOrder order = razorPayOrder.get();
					order.setOrderStatus(paymentStatus);
					order.setUpdatedAt(now);
					order.setActive(true);
					razorPayService.updateOrderDetails(order);
					logger.info("Returning Success Response");
					return ResponseEntity.status(HttpStatus.OK)
							.body(new SuccessResponseHandler(HttpStatus.OK.value(), "Payment Verify Successfully"));
				}
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ErrorResponse("No Payment Order Found", HttpStatus.NOT_FOUND.value(), "Not Found"));

			}

			logger.info("Returning Error Response");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(new ErrorResponse("Payment Verification Failed Please Check Order Id, Payment Id and Signature", HttpStatus.NOT_FOUND.value(), "Failed"));

		} catch (RazorpayException e) {
			logger.error("Some error occured", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorResponse("Internal Error While Verifying Payment Order",
							HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server"));
		}
	}

	// Get API to get payment details based on Recruiter id
	@GetMapping("/getPaymentDetail/{recruiterId}")
	public ResponseEntity<Object> getPaymentDetailById(@PathVariable Long recruiterId) {
		try {

			logger.info("Get Request to fetch payment details on id: ", recruiterId);

			if (recruiterId == null) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(new ErrorResponse("Please Provide Valid Recruiter Id", HttpStatus.BAD_REQUEST.value(), "Failed"));
			}

			List<RazorPayOrder> details = razorPayService.getPaymentDetilsById(recruiterId);

			if (!details.isEmpty()) {
				logger.info("Payment Details Fetched Successfully returning details to client" + details);
				
				
				PaymentDetailsDto paymentDetails=new PaymentDetailsDto();
				paymentDetails.setOrderId(details.get(0).getOrderId());
				paymentDetails.setRecruiterId(details.get(0).getJobRecruiter().getRecruiterId());
				paymentDetails.setAmount(details.get(0).getOrderAmount());
				paymentDetails.setOrderStatus(details.get(0).getOrderStatus());
				paymentDetails.setOrderDate(details.get(0).getOrderDate());
				paymentDetails.setIsActive(details.get(0).isActive());
				
				return ResponseEntity.status(HttpStatus.OK)
						.body(new SuccessResponseHandler(HttpStatus.OK.value(), paymentDetails));
			}

			logger.info("Payment details not found for recruiter id: ", recruiterId);
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new ErrorResponse("No Active Payment Order Found For Recruiter: "+recruiterId, HttpStatus.NOT_FOUND.value(), "Not Found"));
		} catch (Exception e) {
			logger.error("Internal server error occurred while retrieving all jobs.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorResponse("Internal Error Getting Payment Order" + e.getMessage(),
							HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server"));
		}
	}

}
