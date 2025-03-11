package com.talentstream.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.talentstream.dto.LoginDTO;
import com.talentstream.dto.RegistrationDTO;
import com.talentstream.dto.ResumeRegisterDto;
import com.talentstream.entity.Applicant;
import com.talentstream.exception.CustomException;
import com.talentstream.repository.JobRecruiterRepository;
import com.talentstream.repository.RegisterRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class RegisterService {

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private JobRecruiterRepository recruiterRepository;

	@Autowired
	RegisterRepository applicantRepository;

	private static final String RESUME_ID = "User resume ID: ";

	@Autowired
	private RestTemplate restTemplate;
	
	private Random random = new Random();
	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	
	private final Logger logger=LoggerFactory.getLogger(RegisterService.class);

	public RegisterService(RegisterRepository applicantRepository) {
		this.applicantRepository = applicantRepository;
	}

	public Applicant login(String email, String password) {
		logger.info("Login is Mached  {}",email);
		try {
			Applicant applicant = applicantRepository.findByEmail(email);

		    
			if (applicant != null && passwordEncoder.matches(password, applicant.getPassword())) {
				applicant.setUtmSource("Not first time");
			
				return applicant;
			} else {
				System.out.println("Password incorrect...");
				return null;
			}
		} catch (Exception e) {
			
			
			logger.error(e.getMessage());
			return null;
		}
	}

	public boolean isGoogleSignIn(LoginDTO loginDTO) {
		// Check if password is null or empty
		return loginDTO.getPassword() == null || loginDTO.getPassword().isEmpty();
	}



	public Applicant googleSignIn(String email, String utmSource) {
		Applicant applicant = null;

		try {
			applicant = applicantRepository.findByEmail(email);

			if (applicant == null) {
				// If the applicant does not exist, create a new one
				Applicant newApplicant = new Applicant();
				newApplicant.setEmail(email);
				newApplicant.setUtmSource(utmSource);
				
				// Generate a random number as the password
				String randomPassword = generateRandomPassword();
				newApplicant.setPassword(passwordEncoder.encode(randomPassword));

				// Save the new applicant
				Applicant applicant1 = applicantRepository.save(newApplicant);
				applicant1.setUtmSource("first time");
				logger.info(RESUME_ID);
				ResumeRegisterDto resume = new ResumeRegisterDto();


				String firstName = UUID.randomUUID().toString().replaceAll("[^a-z0-9._-]", "").substring(0, 10);
				resume.setName(firstName);
				String randomString = UUID.randomUUID().toString().replaceAll("[^a-z0-9._-]", "").substring(0, 10);
				String username = firstName + randomString;
				resume.setUsername(username);
				resume.setEmail(applicant1.getEmail().toLowerCase());
				resume.setPassword(applicant1.getPassword());
				resume.setLocale("en-US");
				// Prepare headers
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);


				// Create HttpEntity with headers and resume body
				HttpEntity<ResumeRegisterDto> requestEntity = new HttpEntity<>(resume, headers);

				// Define the endpoint URL
				String resumeRegisterUrl = "https://resume.bitlabs.in:5173/api/auth/register";

				try {

					// Make POST request
					ResponseEntity<String> response = restTemplate.postForEntity(resumeRegisterUrl, requestEntity,
							String.class);

					// Parse the JSON response
					Gson gson = new Gson();
					JsonObject jsonResponse = gson.fromJson(response.getBody(), JsonObject.class);

					// Access the nested ID field
					String userId = jsonResponse.getAsJsonObject("user").get("id").getAsString();

					// Print the ID
					logger.info(RESUME_ID ,userId);
					applicant1.setResumeId(userId);
					applicantRepository.save(applicant1);

				} catch (Exception e) {
				    logger.error(e.getMessage());
				}

				return applicant1;
			} else {
				applicant.setUtmSource("not first time");
				return applicant;
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		logger.info("Checking");
		logger.info("Able to return applicant");
		logger.info(applicant != null ? applicant.getEmail() : "Applicant is null");
		return applicant;
	}

	private String generateRandomPassword() {
		// Generate a random 6-digit password

		int randomPassword = 100000 + random.nextInt(900000);
		return String.valueOf(randomPassword);
	}



	public Applicant findById(Long id) {
		try {
			return applicantRepository.findById(id);
		} catch (EntityNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new CustomException("Error finding applicant by ID", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public Applicant findByEmail(String userEmail) {
		try {
			logger.info(userEmail);
			return applicantRepository.findByEmail(userEmail);

		} catch (Exception e) {

			throw new CustomException("Error finding applicant by email", HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	public Applicant findByMobilenumber(String userMobile) {
		try {

			return applicantRepository.findByMobilenumber(userMobile);

		} catch (Exception e) {

			throw new CustomException("Error finding applicant by Mobile Number", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public List<Applicant> getAllApplicants() {
		try {
			return applicantRepository.findAll();
		} catch (Exception e) {
			throw new CustomException("Error retrieving applicants", HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	public void updatePassword(String userEmail, String newPassword) {
		try {
			Applicant applicant = applicantRepository.findByEmail(userEmail);
			if (applicant != null) {
				applicant.setPassword(passwordEncoder.encode(newPassword));
				applicantRepository.save(applicant);
			} else {
				throw new EntityNotFoundException("Applicant not found for email: " + userEmail);
			}
		} catch (EntityNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new CustomException("Error updating password", HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@Transactional
	public ResponseEntity<String> saveApplicant(RegistrationDTO registrationDTO) {
		try {
			Applicant applicant = mapRegistrationDTOToApplicant(registrationDTO);

			// Check if email or mobile number already exists
			if (applicantRepository.existsByEmail(applicant.getEmail())
					|| recruiterRepository.existsByEmail(applicant.getEmail())) {
				throw new CustomException("Email already registered", HttpStatus.BAD_REQUEST);
			}
			if (applicantRepository.existsByMobilenumber(applicant.getMobilenumber())
					|| recruiterRepository.existsByMobilenumber(applicant.getMobilenumber())) {
				throw new CustomException("Mobile number already exists", HttpStatus.BAD_REQUEST);
			}

			// Encrypt password
			applicant.setPassword(passwordEncoder.encode(applicant.getPassword()));

			// Save applicant
			Applicant savedApplicant = applicantRepository.save(applicant);

			// Generate username
			SecureRandom randomsec = new SecureRandom();
			String randomString = Long.toHexString(randomsec.nextLong());
			String username = savedApplicant.getName().split("\\s+")[0].toLowerCase() + randomString;

			// Prepare Resume DTO
			ResumeRegisterDto resume = new ResumeRegisterDto();
			resume.setName(savedApplicant.getName());
			resume.setUsername(username);
			resume.setEmail(savedApplicant.getEmail().toLowerCase());
			resume.setPassword(savedApplicant.getPassword());
			resume.setLocale("en-US");

			// Set Headers
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<ResumeRegisterDto> requestEntity = new HttpEntity<>(resume, headers);

			// Call Resume API
			String resumeRegisterUrl = "https://resume.bitlabs.in:5173/api/auth/register";
			ResponseEntity<String> response = restTemplate.postForEntity(resumeRegisterUrl, requestEntity,
					String.class);

			// Parse Response
			Gson gson = new Gson();
			JsonObject jsonResponse = gson.fromJson(response.getBody(), JsonObject.class);
			if (jsonResponse.has("user") && jsonResponse.getAsJsonObject("user").has("id")) {
				String userId = jsonResponse.getAsJsonObject("user").get("id").getAsString();
				savedApplicant.setResumeId(userId);
				applicantRepository.save(savedApplicant);
			}

			return ResponseEntity.ok("Applicant registered successfully");

		} catch (CustomException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch (Exception e) {
			logger.error("Error registering applicant", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error registering applicant");
		}
	}

	// Check if the email exists in the database
	public boolean emailExists(String email) {
		return applicantRepository.existsByEmail(email);
	}

	public void addApplicant(Applicant applicant) {
		try {
			applicantRepository.save(applicant);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			throw new CustomException("Error adding applicant", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private Applicant mapRegistrationDTOToApplicant(RegistrationDTO registrationDTO) {
		Applicant applicant = new Applicant();
		applicant.setName(registrationDTO.getName());
		applicant.setEmail(registrationDTO.getEmail());
		applicant.setMobilenumber(registrationDTO.getMobilenumber());
		applicant.setPassword(registrationDTO.getPassword());
		applicant.setUtmSource(registrationDTO.getUtmSource());
		//adding current Date time
        LocalDateTime now = LocalDateTime.now();
        applicant.setCreatedAt(now);
		return applicant;
	}
	
	


	public String authenticateUser(long id, String oldPassword, String newPassword) {

		try {

			Applicant opUser = applicantRepository.findById(id);
			logger.warn(opUser.getPassword());

			if (opUser!=null) {
				if (passwordEncoder.matches(oldPassword, opUser.getPassword())) 
				{
					if (passwordEncoder.matches(newPassword, opUser.getPassword())) 
					{
						return "your new password should not be same as old password";
					}
					opUser.setPassword(passwordEncoder.encode(newPassword));
					applicantRepository.save(opUser);
					return "Password updated and stored";
				} 
				else 
				{
					return "Your old password not matching with data base password";
				}
			} 
			else 
			{
				return "User not found with given id";
			}
		}
		catch (Exception e) {

			e.printStackTrace();

			return "user not found with this given id";
		}

	}

	public ResponseEntity<String> editApplicant(Long applicantId, RegistrationDTO updatedRegistrationDTO) {
		try {

			Applicant existingApplicantOpt = applicantRepository.findById(applicantId);

			if (existingApplicantOpt!=null) {
			    Applicant existingApplicant = existingApplicantOpt; // Unwrap the Optional

			    // Update fields if they are not null
			    if (updatedRegistrationDTO.getName() != null) {
			        existingApplicant.setName(updatedRegistrationDTO.getName());
			    }

			    if (updatedRegistrationDTO.getMobilenumber() != null) {
			        existingApplicant.setMobilenumber(updatedRegistrationDTO.getMobilenumber());
			    }

			    if (updatedRegistrationDTO.getEmail() != null) {
			        existingApplicant.setEmail(updatedRegistrationDTO.getEmail()); // Fixed the email update
			    }

			    if (updatedRegistrationDTO.getPassword() != null) {
			        existingApplicant.setPassword(passwordEncoder.encode(updatedRegistrationDTO.getPassword()));
			    }

			    applicantRepository.save(existingApplicant); // Save the updated applicant

			    return ResponseEntity.ok("Applicant updated successfully");
			} else {
			    return ResponseEntity.badRequest().body("Applicant not found with id: " + applicantId);
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating applicant");
		}
	}

}
