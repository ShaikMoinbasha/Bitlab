package com.talentstream.controller;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.talentstream.dto.LoginDTO;
import com.talentstream.dto.RefreshTokenDTO;
import com.talentstream.dto.RegistrationDTO;
import com.talentstream.entity.Applicant;
import com.talentstream.entity.AuthenticationResponse;
import com.talentstream.entity.JobRecruiter;
import com.talentstream.entity.NewPasswordRequest;
import com.talentstream.entity.OtpVerificationRequest;
import com.talentstream.entity.PasswordRequest;
import com.talentstream.exception.CustomException;
import com.talentstream.repository.JobRecruiterRepository;
import com.talentstream.repository.RegisterRepository;
import com.talentstream.response.ResponseHandler;
import com.talentstream.service.EmailService;
import com.talentstream.service.JobRecruiterService;
import com.talentstream.service.JwtUtil;
import com.talentstream.service.MyUserDetailsService;
import com.talentstream.service.OtpService;
import com.talentstream.service.RegisterService;

@CrossOrigin("*")
@RestController
@RequestMapping("/applicant")
public class RegisterController {
	@Autowired
	private MyUserDetailsService myUserDetailsService;

	@Autowired
	private OtpService otpService;
	@Autowired
	private RegisterRepository registerRepo;

	@Autowired
	private JobRecruiterRepository recruiterRepository;

	@Autowired
	private RegisterService registerService;
	
	@Autowired
	private JwtUtil jwtUtil;

	private Map<String, Boolean> otpVerificationMap = new HashMap<>();
	

	public  final Logger logger = LoggerFactory.getLogger(RegisterController.class);
	@Autowired
	private AuthenticationManager authenticationManager;
	@Autowired
	private JwtUtil jwtTokenUtil;

	@Autowired
	private EmailService emailService;

	@Autowired
	RegisterService regsiterService;
	@Autowired
	JobRecruiterService recruiterService;
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	public RegisterController(RegisterService regsiterService) {
		this.regsiterService = regsiterService;

	}

	@PutMapping("/editApplicant/{applicantId}")
	public ResponseEntity<String> editApplicant(@PathVariable Long applicantId,
			@RequestBody RegistrationDTO updatedRegistrationDTO) {
		try {
			logger.info("Attempting to edit applicant with ID: {}", applicantId);
			ResponseEntity<String> response = registerService.editApplicant(applicantId, updatedRegistrationDTO);
			logger.info("Successfully edited applicant with ID: {}", applicantId);
			return response;
		} catch (Exception e) {
			logger.error("Error updating applicant with ID: {}", applicantId, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating applicant");
		}
	}

	@PostMapping("/saveApplicant")
	public ResponseEntity<String> register(@Valid @RequestBody RegistrationDTO registrationDTO,
			BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			// Handle validation errors
			Map<String, String> fieldErrors = new LinkedHashMap<>();

			bindingResult.getFieldErrors().forEach(fieldError -> {
				String fieldName = fieldError.getField();
				String errorMessage = fieldError.getDefaultMessage();

				// Append each field and its error message on a new line
				fieldErrors.merge(fieldName, errorMessage,
						(existingMessage, newMessage) -> existingMessage + "\n" + newMessage);
			});

			// Construct the response body with each field and its error message on separate
			// lines
			StringBuilder responseBody = new StringBuilder();
			fieldErrors.forEach((fieldName, errorMessage) -> responseBody.append(fieldName).append(": ")
					.append(errorMessage).append("\n"));
			logger.warn("Validation errors occurred during registering new applicant: {}", responseBody);
			return ResponseEntity.badRequest().body(responseBody.toString());
		}

		try {
			logger.info("Registering new applicant");
			return regsiterService.saveApplicant(registrationDTO);

		} catch (CustomException e) {
			logger.error("Custom exception while registering applicant: {}", e.getMessage(), e);
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch (Exception e) {

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error registering applicant");
		}
	}
	

	
	@PostMapping("/applicantLogin")
	public ResponseEntity<Object> login(@Valid @RequestBody LoginDTO loginDTO, BindingResult bindingResult)
	        throws Exception {
	    if (bindingResult.hasErrors()) {
	        Map<String, String> fieldErrors = new LinkedHashMap<>();

	        bindingResult.getFieldErrors().forEach(fieldError -> {
	            String fieldName = fieldError.getField();
	            String errorMessage = fieldError.getDefaultMessage();
	            fieldErrors.merge(fieldName, errorMessage,
	                    (existingMessage, newMessage) -> existingMessage + "\n" + newMessage);
	        });

	        StringBuilder responseBody = new StringBuilder();
	        fieldErrors.forEach((fieldName, errorMessage) ->
	                responseBody.append(fieldName).append(": ").append(errorMessage).append("\n"));

	        logger.warn("Validation errors occurred during login: {}", responseBody);
	        return ResponseEntity.badRequest().body(responseBody.toString());
	    }

	    try {
	        Applicant applicant = null;
	        logger.info("Attempting to login with email: {}", loginDTO.getEmail());

	        if (regsiterService.isGoogleSignIn(loginDTO)) {
	            // Handle Google Sign-In
	            logger.debug("Handling Google Sign-In for email: {}", loginDTO.getEmail());
	            applicant = regsiterService.googleSignIn(loginDTO.getEmail(), loginDTO.getUtmSource());

	            if (applicant != null) {
	                logger.debug("Google Sign-In successful for: {}", applicant.getEmail());
	            } else {
	                logger.warn("Google Sign-In failed. No applicant found for email: {}", loginDTO.getEmail());
	                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Google Sign-In failed. No account found.");
	            }
	        } else {
	            // Handle regular login
	            applicant = regsiterService.login(loginDTO.getEmail(), loginDTO.getPassword());

	            if (applicant == null) {
	                boolean emailExists = regsiterService.emailExists(loginDTO.getEmail());
	                if (emailExists) {
	                    logger.warn("Incorrect password for email: {}", loginDTO.getEmail());
	                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Incorrect password");
	                } else {
	                    logger.warn("No account found with email: {}", loginDTO.getEmail());
	                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                            .body("No account found with this email address");
	                }
	            }
	        }

	        try {
	            // Proceed with token creation if applicant is valid
	            return createAuthenticationToken(loginDTO, applicant);
	        } catch (Exception e) {
	            // Handle any exceptions during token creation
	            logger.error("Error while creating authentication token for email: {}", applicant.getEmail(), e);
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Authentication failed");
	        }

	    } catch (BadCredentialsException e) {
	        logger.error("Unauthorized access attempt for email: {}", loginDTO.getEmail(), e);
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect username or password");
	    } catch (Exception e) {
	        logger.error("Error during login for email: {}", loginDTO.getEmail(), e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error during login");
	    }
	}


	private ResponseEntity<Object> createAuthenticationToken(LoginDTO loginDTO, Applicant applicant) throws Exception {
		try {
			if (regsiterService.isGoogleSignIn(loginDTO)) {
				// Handle Google sign-in separately
				System.out.println("Now I am at token gen");
				UserDetails userDetails = myUserDetailsService.loadUserByUsername(applicant.getEmail());
				final String jwt = jwtTokenUtil.generateToken(userDetails.getUsername());
				final String refreshToken=jwtTokenUtil.generateRefreshToken(userDetails.getUsername());
				return ResponseHandler.generateResponse("Login successfully" + userDetails.getAuthorities(),
						HttpStatus.OK, new AuthenticationResponse(jwt,refreshToken), applicant.getEmail(), applicant.getName(),
						applicant.getId(), applicant.getMobilenumber(), applicant.getUtmSource());
			} else {
				// Regular login functionality
				authenticationManager.authenticate(
						new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword()));
				UserDetails userDetails = myUserDetailsService.loadUserByUsername(applicant.getEmail());
				final String jwt = jwtTokenUtil.generateToken(userDetails.getUsername());
				final String refreshToken=jwtTokenUtil.generateRefreshToken(userDetails.getUsername());
				return ResponseHandler.generateResponse("Login successfully" + userDetails.getAuthorities(),
						HttpStatus.OK, new AuthenticationResponse(jwt,refreshToken), applicant.getEmail(), applicant.getName(),
						applicant.getId(), applicant.getMobilenumber(), applicant.getUtmSource());
			}
		} catch (BadCredentialsException e) {
			throw new CustomException("Incorrect username or password", HttpStatus.UNAUTHORIZED);
		}

	}
	
	
	@PostMapping("/refreshToken")
	public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenDTO refreshTokenDTO) {
	    try {
	        boolean isValid = jwtUtil.validateRefreshToken(refreshTokenDTO.getToken(), refreshTokenDTO.getUsername());

	        if (isValid) {
	        	Map<String, String> response = new HashMap<>();
	            String regenerated = jwtUtil.generateToken(refreshTokenDTO.getUsername());
	            response.put("Jwt ", regenerated);
	        	return ResponseEntity.ok(response);
	            
	        } else {
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token is expired or invalid");
	        }
	    } catch (ExpiredJwtException | SignatureException e) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token: " + e.getMessage());
	    } catch (Exception e) {
	       
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error refreshing token: " + e.getMessage());
	    }
	}



	@PostMapping("/applicantsendotp")
	public ResponseEntity<String> sendOtp(@Valid @RequestBody Applicant request, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			// Handle validation errors
			Map<String, String> fieldErrors = new LinkedHashMap<>();

			bindingResult.getFieldErrors().forEach(fieldError -> {
				String fieldName = fieldError.getField();
				String errorMessage = fieldError.getDefaultMessage();

				// Append each field and its error message on a new line
				fieldErrors.merge(fieldName, errorMessage,
						(existingMessage, newMessage) -> existingMessage + "\n" + newMessage);
			});

			// Construct the response body with each field and its error message on separate
			// lines
			StringBuilder responseBody = new StringBuilder();
			fieldErrors.forEach((fieldName, errorMessage) -> responseBody.append(fieldName).append(": ")
					.append(errorMessage).append("\n"));
			logger.warn("Validation errors occurred during registering new applicant: {}", responseBody);
			return ResponseEntity.badRequest().body(responseBody.toString());
		}
		String userEmail = request.getEmail();
		String userMobile = request.getMobilenumber();
		logger.info("Attempting to send OTP to email: {} and mobile: {}", userEmail, userMobile);
		try {
			Applicant applicantByEmail = regsiterService.findByEmail(userEmail);
			Applicant applicantByMobile = regsiterService.findByMobilenumber(userMobile);
			JobRecruiter recruiterByEmail = findByEmail(userEmail);
			JobRecruiter recruiterByMobile = findByMobilenumber(userMobile);

			if (applicantByEmail == null && applicantByMobile == null && recruiterByEmail == null
					&& recruiterByMobile == null) {
				String otp = otpService.generateOtp(userEmail);
				emailService.sendOtpEmail(userEmail, otp);
				otpVerificationMap.put(userEmail, true);
				logger.info("OTP sent successfully to {}", userEmail);
				return ResponseEntity.ok("OTP sent to your email.");
			} else {
				if (applicantByEmail != null) {
					logger.warn("Email is already registered as an Applicant: {}", userEmail);
					throw new CustomException("Email is already registered as an Applicant.", null);
				} else if (recruiterByEmail != null) {
					logger.warn("Email is already registered as a Recruiter: {}", userEmail);
					throw new CustomException("Email is already registered as a Recruiter.", null);
				} else if (applicantByMobile != null) {
					logger.warn("Mobile number is already registered as an Applicant: {}", userMobile);
					throw new CustomException("Mobile number is already registered as an Applicant.", null);
				} else if (recruiterByMobile != null) {
					logger.warn("Mobile number is already registered as a Recruiter: {}", userMobile);
					throw new CustomException("Mobile number is already registered as a Recruiter.", null);
				} else {
					throw new CustomException("Email or mobile number is already registered.", null);
				}
			}
		} catch (CustomException e) {
			logger.error("Custom exception occurred: {}", e.getMessage());
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch (Exception e) {
			logger.error("Error sending OTP to email: {}", userEmail, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error sending OTP");
		}
	}

	@PostMapping("/forgotpasswordsendotp")
	public ResponseEntity<String> forgotSendOtp(@Valid @RequestBody Applicant applicant , BindingResult bindingResult) {

		if (bindingResult.hasErrors()) {
			// Handle validation errors
			Map<String, String> fieldErrors = new LinkedHashMap<>();

			bindingResult.getFieldErrors().forEach(fieldError -> {
				String fieldName = fieldError.getField();
				String errorMessage = fieldError.getDefaultMessage();

				// Append each field and its error message on a new line
				fieldErrors.merge(fieldName, errorMessage,
						(existingMessage, newMessage) -> existingMessage + "\n" + newMessage);
			});

			// Construct the response body with each field and its error message on separate
			// lines
			StringBuilder responseBody = new StringBuilder();
			fieldErrors.forEach((fieldName, errorMessage) -> responseBody.append(fieldName).append(": ")
					.append(errorMessage).append("\n"));
			logger.warn("Validation errors occurred during registering new applicant: {}", responseBody);
			return ResponseEntity.badRequest().body(responseBody.toString());
		}
		String userEmail = applicant.getEmail();
		logger.info("Sending OTP for password recovery to email: {}", userEmail);
		Applicant getApplicant = regsiterService.findByEmail(userEmail);
		System.out.println(applicant);
		
	
		if (getApplicant != null) {
			String otp = otpService.generateOtp(userEmail);
			emailService.sendOtpEmail(userEmail, otp);
			otpVerificationMap.put(userEmail, true);
			//Verification of Otp
			logger.info("OTP sent successfully to email: {}", userEmail);
			return ResponseEntity.ok("OTP sent successfully");
		} else {
			logger.warn("Email not found: {}", userEmail);
			return ResponseEntity.badRequest().body("Email not found.");
		}
	}

	@PostMapping("/applicantverify-otp")
	public ResponseEntity<String> verifyOtp(@Valid @RequestBody OtpVerificationRequest verificationRequest,
			BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			// Handle validation errors
			Map<String, String> fieldErrors = new LinkedHashMap<>();

			bindingResult.getFieldErrors().forEach(fieldError -> {
				String fieldName = fieldError.getField();
				String errorMessage = fieldError.getDefaultMessage();

				// Append each field and its error message on a new line
				fieldErrors.merge(fieldName, errorMessage,
						(existingMessage, newMessage) -> existingMessage + "\n" + newMessage);
			});

			// Construct the response body with each field and its error message on separate
			// lines
			StringBuilder responseBody = new StringBuilder();
			fieldErrors.forEach((fieldName, errorMessage) -> responseBody.append(fieldName).append(": ")
					.append(errorMessage).append("\n"));
			logger.warn("Validation errors occurred during registering new applicant: {}", responseBody);
			return ResponseEntity.badRequest().body(responseBody.toString());
		}
		try {
			String otp = verificationRequest.getOtp();
			String email = verificationRequest.getEmail();
			logger.info("Verifying OTP for email: {}, OTP: {}", email, otp);
			System.out.println(otp + email);

			if (otpService.validateOtp(email, otp)) {
				logger.info("OTP verified successfully for email: {}", email);
				return ResponseEntity.ok("OTP verified successfully");
			} else {
				logger.warn("Incorrect OTP or Timeout for email: {}", email);
				throw new CustomException("Incorrect OTP or Timeout.", HttpStatus.BAD_REQUEST);
			}
		} catch (CustomException e) {
			logger.error("OTP verification failed: {}", e.getMessage());
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		} catch (Exception e) {
			logger.error("Error verifying OTP for email: {}", e);
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error verifying OTP");
		}
	}

	@PostMapping("/applicantreset-password/{email}")
	public ResponseEntity<String> setNewPassword(@Valid @RequestBody NewPasswordRequest request,
			BindingResult bindingResult, @PathVariable String email) {
		if (bindingResult.hasErrors()) {
			// Handle validation errors
			Map<String, String> fieldErrors = new LinkedHashMap<>();

			bindingResult.getFieldErrors().forEach(fieldError -> {
				String fieldName = fieldError.getField();
				String errorMessage = fieldError.getDefaultMessage();

				// Append each field and its error message on a new line
				fieldErrors.merge(fieldName, errorMessage,
						(existingMessage, newMessage) -> existingMessage + "\n" + newMessage);
			});

			// Construct the response body with each field and its error message on separate
			// lines
			StringBuilder responseBody = new StringBuilder();
			fieldErrors.forEach((fieldName, errorMessage) -> responseBody.append(fieldName).append(": ")
					.append(errorMessage).append("\n"));
			logger.warn("Validation errors occurred during registering new applicant: {}", responseBody);
			return ResponseEntity.badRequest().body(responseBody.toString());
		}

		try {
			logger.info("Request to reset password for email: {}", email);

			String newpassword = request.getPassword();
			String confirmedPassword = request.getConfirmedPassword();

			if (email == null) {
				throw new CustomException("Email not found or is invalid.", HttpStatus.BAD_REQUEST);
			}

			Applicant applicant = regsiterService.findByEmail(email);
			if (applicant == null) {
				logger.error("No user found with email: {}", email);
				throw new CustomException("User not found.", HttpStatus.BAD_REQUEST);
			}

			applicant.setPassword(passwordEncoder.encode(newpassword));
			regsiterService.addApplicant(applicant);
			logger.info("Password reset successfully for email: {}", email);
			return ResponseEntity.ok("Password reset was done successfully");
		} catch (CustomException e) {
			logger.error("CustomException during password reset for email: {}: {}", email, e.getMessage());
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		} catch (Exception e) {
			logger.error("Error resetting password for email: {}", email, e);
			System.out.println(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error resetting password");
		}
	}

	@GetMapping("/viewApplicants")

	public ResponseEntity<List<Applicant>> getAllApplicants() {
		logger.info("Fetching all applicants");

		try {
			List<Applicant> applicants = regsiterService.getAllApplicants();
			return ResponseEntity.ok(applicants);
		} catch (Exception e) {
			logger.error("Failed to retrieve applicants", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}

	}

	@PostMapping("/applicantsignOut")
	public ResponseEntity<Void> signOut(@AuthenticationPrincipal Applicant user) {
		logger.info("Signing out user: {}", user.getEmail());
		try {
			SecurityContextHolder.clearContext();
			return ResponseEntity.noContent().build();
		} catch (Exception e) {
			logger.error("Error during sign out for user: {}", user.getEmail(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	public void setOtpService(OtpService otpService2) {
		otpService = otpService2;
		logger.debug("OTP Service set successfully");

	}

	@PostMapping("/authenticateUsers/{id}")
	public ResponseEntity<String> authenticateUser(@Valid @RequestBody PasswordRequest passwordRequest,
			BindingResult bindingResult, @PathVariable long id) {

		if (bindingResult.hasErrors()) {
			// Handle validation errors
			Map<String, String> fieldErrors = new LinkedHashMap<>();

			bindingResult.getFieldErrors().forEach(fieldError -> {
				String fieldName = fieldError.getField();
				String errorMessage = fieldError.getDefaultMessage();

				// Append each field and its error message on a new line
				fieldErrors.merge(fieldName, errorMessage,
						(existingMessage, newMessage) -> existingMessage + "\n" + newMessage);
			});

			// Construct the response body with each field and its error message on separate
			// lines
			StringBuilder responseBody = new StringBuilder();
			fieldErrors.forEach((fieldName, errorMessage) -> responseBody.append(fieldName).append(": ")
					.append(errorMessage).append("\n"));
			logger.warn("Validation errors occurred during registering new applicant: {}", responseBody);
			return ResponseEntity.badRequest().body(responseBody.toString());
		}

		logger.info("Authenticating user with ID: {}", id);

		String newpassword = passwordRequest.getNewPassword();
		String oldpassword = passwordRequest.getOldPassword();
		logger.info("Authentication result for user ID {}", id);
		String result1 = regsiterService.authenticateUser(id, oldpassword, newpassword);
		return ResponseEntity.ok(result1);
	}

	public JobRecruiter findByEmail(String userEmail) {
		try {
			
			logger.info(userEmail);

			return recruiterRepository.findByEmail(userEmail);

		} catch (Exception e) {

			throw new CustomException("Error finding applicant by email", HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	public JobRecruiter findByMobilenumber(String userMobile) {
		try {

			return recruiterRepository.findByMobilenumber(userMobile);

		} catch (Exception e) {

			throw new CustomException("Error finding applicant by Mobile Number", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/getApplicantById/{id}")
	public ResponseEntity<Applicant> getApplicantById(@PathVariable long id) {
		Applicant applicant = registerRepo.findById(id);
		return ResponseEntity.ok(applicant);
	}

	@GetMapping("/getResumeId/{id}")
	public ResponseEntity<String> getResumeIdByApplicantId(@PathVariable long id) {
		Applicant applicant = registerRepo.findById(id);
		if (applicant == null) {
			return ResponseEntity.notFound().build();
		}
		String resumeId = applicant.getResumeId();
		return ResponseEntity.ok(resumeId);
	}

}
