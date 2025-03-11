package com.talentstream.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.talentstream.entity.Job;
import com.talentstream.exception.CustomException;
import com.talentstream.repository.ApplicantRepository;
import com.talentstream.repository.JobRepository;
 
@Service
public class SearchForaJobService {
	@Autowired
    private ApplicantRepository applicantRepository;
 
    @Autowired
    private JobRepository jobRepository;
 
    @Cacheable(value = "jobsCache", key = "#applicantId + '-' + #skillName + '-' + #pageable.pageNumber")

    public Page<Job> searchJobsBySkillAndApplicant(long applicantId, String skillName, Pageable pageable) {
    	 try {
    
    		
    		 //place of applicant previously checking in applicant profile
    		
//             Optional<ApplicantProfile> applicantOptional = applicantProfileRepository.findById(applicantId);
 
    		// Optional<Applicant> applicantOptional = applicantRepository.findById(applicantId);
    		
    		 if (!applicantRepository.existsById(applicantId)) {
     	        throw new CustomException("Applicant not found", HttpStatus.NOT_FOUND);
     	    }
 
     	    return jobRepository.findJobsBySkillName(skillName, pageable);
         } catch (Exception e) {
             throw new CustomException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
         }
     }
}
 