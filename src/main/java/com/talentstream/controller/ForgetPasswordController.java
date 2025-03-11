package com.talentstream.controller;

import java.util.HashMap;
import java.util.Map;
import com.talentstream.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.talentstream.entity.JobRecruiter;
import com.talentstream.entity.NewPasswordRequest;
import com.talentstream.entity.OtpVerificationRequest;
import com.talentstream.entity.ResetPasswordRequest;
import com.talentstream.service.EmailService;
import com.talentstream.service.JobRecruiterService;
import com.talentstream.service.OtpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@RestController
@CrossOrigin("*")
@RequestMapping("/forgotpassword")
public class ForgetPasswordController {
	@Autowired
    private OtpService otpService;

    @Autowired
    private JobRecruiterService jobRecruiterService; 

    @Autowired
    private EmailService emailService;
    
    private static final String ERROR_OTP="Error verifying OTP";
    
    private static final String ERROR_RES_PASS="Error resetting password";
    
        @Autowired
    private PasswordEncoder passwordEncoder;
        
        private Map<String, Boolean> otpVerificationMap = new HashMap<>();
        private static final Logger logger = LoggerFactory.getLogger(ForgetPasswordController.class);
    @PostMapping("/recuritersend-otp")
    public ResponseEntity<String> sendOtp(@RequestBody ResetPasswordRequest request) {
    	 String userEmail = request.getEmail();
         JobRecruiter jobRecruiter = jobRecruiterService.findByEmail(userEmail);
         if (jobRecruiter == null) {
        	 logger.error("Email {} is not registered or not found.", userEmail);
             throw new CustomException("Email is not registered or not found.", HttpStatus.BAD_REQUEST);
         }
         String otp = otpService.generateOtp(userEmail);
         emailService.sendOtpEmail(userEmail, otp);
         otpVerificationMap.put(userEmail, true);
         logger.info("OTP sent successfully for email {}", userEmail);
         return ResponseEntity.ok("OTP sent successfully");
    }
    
    
    @PostMapping("/recuriterverify-otp")
    public ResponseEntity<String> verifyOtp(
       @RequestBody  OtpVerificationRequest verificationRequest
    ) {
    	 String otp = verificationRequest.getOtp();
         String email = verificationRequest.getEmail();

         try {
             if (otpService.validateOtp(email, otp)) {
            	 logger.info("OTP verified successfully for email {}", email);
                 return ResponseEntity.ok("OTP verified successfully");
             } else {
            	 logger.error("Incorrect OTP or Time Out for email {}", email);
                 throw new CustomException("Incorrect OTP or Time Out", HttpStatus.BAD_REQUEST);
             }
         } catch (CustomException ce) {
        	  logger.error(ERROR_OTP, ce);
             return ResponseEntity.status(ce.getStatus()).body(ce.getMessage());
         } catch (Exception e) {
        	 logger.error(ERROR_OTP, e);
             throw new CustomException(ERROR_OTP, HttpStatus.INTERNAL_SERVER_ERROR);
         }
    }


    @PostMapping("/recuriterreset-password/set-new-password/{email}")
    public ResponseEntity<String> setNewPassword(@RequestBody NewPasswordRequest request, @PathVariable String email) {
        try {
            if (email == null || email.isBlank()) {
                logger.error("Email is missing in the request.");
                throw new CustomException("Email is required.", HttpStatus.BAD_REQUEST);
            }

            String newPassword = request.getPassword();
            String confirmedPassword = request.getConfirmedPassword();

            JobRecruiter jobRecruiter = jobRecruiterService.findByEmail(email);
            if (jobRecruiter == null) {
                logger.error("User not found for email: {}", email);
                throw new CustomException("User not found.", HttpStatus.BAD_REQUEST);
            }

            if (newPassword == null || confirmedPassword == null || newPassword.isBlank() || confirmedPassword.isBlank()) {
                logger.error("Password fields are missing for email: {}", email);
                throw new CustomException("Password fields cannot be empty.", HttpStatus.BAD_REQUEST);
            }

            if (!newPassword.equals(confirmedPassword)) {
                logger.error("Passwords do not match for email: {}", email);
                throw new CustomException("Passwords do not match.", HttpStatus.BAD_REQUEST);
            }

            jobRecruiter.setPassword(passwordEncoder.encode(newPassword));
            jobRecruiterService.addRecruiter(jobRecruiter);
            
            logger.info("Password reset was successful for email: {}", email);
            return ResponseEntity.ok("Password reset was successful");

        } catch (CustomException ce) {
            logger.error("Error resetting password: {},{}", ce.getMessage(), ERROR_RES_PASS);
            return ResponseEntity.status(ce.getStatus()).body(ce.getMessage());

        } catch (Exception e) {
            logger.error("Unexpected error while resetting password: {},{}", e.getMessage(), ERROR_RES_PASS);
            throw new CustomException("An unexpected error occurred while resetting password.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
