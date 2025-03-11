
package com.talentstream.service;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.talentstream.entity.Applicant;
import com.talentstream.entity.JobRecruiter;
import com.talentstream.repository.JobRecruiterRepository;
import com.talentstream.repository.RegisterRepository;

@Service
@Primary
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private JobRecruiterRepository recruiterRepository;

    @Autowired
    private RegisterRepository applicantRepository;
    
    private static  final  Logger logger = LoggerFactory.getLogger(MyUserDetailsService.class);

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // First, try to find a JobRecruiter by email
        JobRecruiter jobRecruiter = recruiterRepository.findByEmail(username);
        
        if (jobRecruiter != null) {
          	logger.info("Recruiter");
            return new User(
                jobRecruiter.getEmail(),
                jobRecruiter.getPassword(),
                Arrays.stream(jobRecruiter.getRoles().split(","))
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList())
            );
        }
        
        // If not found, try to find an ApplicantRegistration by email
        Applicant applicant = applicantRepository.findByEmail(username);
        
        if (applicant != null) {
        	logger.info("Applicant");
            return new User(
                applicant.getEmail(),
                applicant.getPassword(),
                Arrays.stream(applicant.getRoles().split(","))
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList())
            );
        }

        // Neither a recruiter nor an applicant with this email was found
        throw new UsernameNotFoundException("User not found with email: " + username);
    }
}

