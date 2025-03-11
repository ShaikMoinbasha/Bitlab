package com.talentstream.service;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.talentstream.exception.CustomException;
import com.talentstream.dto.JobRecruiterDTO;
import com.talentstream.entity.JobRecruiter;
import com.talentstream.repository.JobRecruiterRepository;
import com.talentstream.repository.RegisterRepository;

@Service
public class JobRecruiterService {
	
	    private PasswordEncoder passwordEncoder;	    
	   @Autowired
        JobRecruiterRepository recruiterRepository;
	   @Autowired
	   RegisterRepository applicantRepository;
	   
	   private static final Logger LOGGER=LoggerFactory.getLogger(JobRecruiterService.class);
 
	   public JobRecruiterService(JobRecruiterRepository recruiterRepository, PasswordEncoder passwordEncoder) {
	        this.recruiterRepository = recruiterRepository;
	        this.passwordEncoder = passwordEncoder;
	    }
 
	   public ResponseEntity<String> saveRecruiter(JobRecruiterDTO recruiterDTO) {
	    	JobRecruiter recruiter=convertToEntity(recruiterDTO);
	    	try {
	           
	            if (recruiterRepository.existsByEmail(recruiter.getEmail()) || applicantRepository.existsByEmail(recruiter.getEmail())) {
	                throw new CustomException("Failed to register/Email already exists", HttpStatus.BAD_REQUEST);
	            }
	            if(recruiterRepository.existsByMobilenumber(recruiter.getMobilenumber())||applicantRepository.existsByMobilenumber(recruiter.getMobilenumber()))
	            {
	            	throw new CustomException("Mobile number already existed ,enter new mobile number",null);
	            }
	            LOGGER.info("befor edncoind pwd");
	            recruiter.setPassword(passwordEncoder.encode(recruiter.getPassword()));
	            LOGGER.info("after edncoind pwd ");
	            recruiterRepository.save(recruiter);
	            LOGGER.info("after edncoind pwd and saving");
	            return ResponseEntity.ok("Recruiter registered successfully");
	        } catch (CustomException e) {
	            throw e;
	        } catch (Exception e) {
	            throw new CustomException("Error registering recruiter", HttpStatus.INTERNAL_SERVER_ERROR);
	        }
	    }
	   private JobRecruiter convertToEntity(JobRecruiterDTO recruiterDTO) {
	        JobRecruiter recruiter = new JobRecruiter();

	        recruiter.setCompanyname(recruiterDTO.getCompanyname());
	        recruiter.setMobilenumber(recruiterDTO.getMobilenumber());
	        recruiter.setEmail(recruiterDTO.getEmail());
	        recruiter.setPassword(recruiterDTO.getPassword());
	        recruiter.setRoles(recruiterDTO.getRoles());        
	
	        return recruiter;
	    }
    public JobRecruiter login(String email, String password) {
    	JobRecruiter recruiter = recruiterRepository.findByEmail(email);
        if (recruiter != null && passwordEncoder.matches(password, recruiter.getPassword())) {
            return recruiter;
        } else {
        	
       	 return null;
        }
   }
  
   public boolean emailExists(String email) {
       return recruiterRepository.existsByEmail(email);
   }
    
    public JobRecruiter findById(Long id) {
    	return recruiterRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("JobRecruiter not found for ID: " + id));
        
    }
    public List<JobRecruiterDTO> getAllJobRecruiters() {
        try {
            List<JobRecruiter> jobRecruiters = recruiterRepository.findAll();
            return jobRecruiters.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new CustomException("Error retrieving job recruiters", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    public void updatePassword(String userEmail, String newPassword) {
        JobRecruiter jobRecruiter = recruiterRepository.findByEmail(userEmail);
        if (jobRecruiter != null) {
            jobRecruiter.setPassword(newPassword);
            recruiterRepository.save(jobRecruiter);
        } else {
            throw new EntityNotFoundException("JobRecruiter not found for email: " + userEmail);
        }
    }
 
	public JobRecruiter findByEmail(String userEmail) {
			return recruiterRepository.findByEmail(userEmail);
	}
 
	public void addRecruiter(JobRecruiter jobRecruiter) {
		recruiterRepository.save(jobRecruiter);
			}
	
	
	private JobRecruiterDTO convertToDTO(JobRecruiter recruiter) {
        JobRecruiterDTO recruiterDTO = new JobRecruiterDTO();
    
        recruiterDTO.setCompanyname(recruiter.getCompanyname());
        recruiterDTO.setMobilenumber(recruiter.getMobilenumber());
        recruiterDTO.setEmail(recruiter.getEmail());
        recruiterDTO.setPassword(recruiter.getPassword());
        recruiterDTO.setRoles(recruiter.getRoles());
 
        return recruiterDTO;
    }
	
	public String authenticateRecruiter(Long id, String oldPassword, String newPassword) {
	    try {
	        // ✅ Check if the recruiter exists before accessing fields
	        JobRecruiter opUser = recruiterRepository.findByRecruiterId(id);
	        if (opUser == null) {
	            return "Recruiter not found with ID: " + id;
	        }

	        // ✅ Check if the old password is correct
	        if (!passwordEncoder.matches(oldPassword, opUser.getPassword())) {
	            return "Your old password does not match our records";
	        }

	        // ✅ Ensure the new password is different from the old one
	        if (passwordEncoder.matches(newPassword, opUser.getPassword())) {
	            return "Your new password should not be the same as the old password";
	        }

	        // ✅ Update the password and save changes
	        opUser.setPassword(passwordEncoder.encode(newPassword));
	        recruiterRepository.save(opUser);

	        return "Password updated successfully";

	    } catch (Exception e) {
	    	LOGGER.error("Error updating password for recruiter ID: {}", id, e);
	        return "An error occurred while updating the password";
	    }
	}

	}

 

	
