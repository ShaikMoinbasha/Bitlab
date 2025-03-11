package com.talentstream.controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.talentstream.dto.JobDTO;
import com.talentstream.entity.Job;
import com.talentstream.exception.CustomException;
import com.talentstream.service.SavedJobService;

@RestController
@RequestMapping("/savedjob")
public class SavedJobController {
    final ModelMapper modelMapper = new ModelMapper();
    @Autowired
    private SavedJobService savedJobService;
   

    @PostMapping("/applicants/savejob/{applicantId}/{jobId}")
    public ResponseEntity<String> saveJobForApplicant(
            @PathVariable long applicantId,
            @PathVariable long jobId) {
        try {
            savedJobService.saveJobForApplicant(applicantId, jobId);
            return ResponseEntity.ok("Job saved successfully for the applicant.");
        } catch (CustomException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving job for the applicant.");
        }
    }

    @GetMapping("/getSavedJobs/{applicantId}")
    public ResponseEntity<Page<JobDTO>> getSavedJobsForApplicantAndJob(
            @PathVariable long applicantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<Job> savedJobsPage = savedJobService.getSavedJobsForApplicant(applicantId, page, size);
 
            if (savedJobsPage.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
 
            Page<JobDTO> savedJobsDTOPage = savedJobsPage.map(job -> {
                JobDTO jobDTO = modelMapper.map(job, JobDTO.class);
                jobDTO.setCompanyname(job.getJobRecruiter().getCompanyname());
                jobDTO.setEmail(job.getJobRecruiter().getEmail());
                jobDTO.setRecruiterId(job.getJobRecruiter().getRecruiterId());
                return jobDTO;
            });
 
            return ResponseEntity.ok(savedJobsDTOPage);
        } catch (CustomException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Page.empty());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Page.empty());
        }
    }

    @GetMapping("/countSavedJobs/{applicantId}")
    public ResponseEntity<?> countSavedJobsForApplicant(@PathVariable long applicantId) {
        try {
            long count = savedJobService.countSavedJobsForApplicant(applicantId);
            return ResponseEntity.ok(count);
        } catch (CustomException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }

    @DeleteMapping("/applicants/deletejob/{applicantId}/{jobId}")
    public ResponseEntity<String> deleteSavedJobForApplicant(
            @PathVariable long applicantId,
            @PathVariable long jobId) {
        try {
            savedJobService.deleteSavedJobForApplicant(applicantId, jobId);
            return ResponseEntity.ok("Job deleted successfully for the applicant.");
        } catch (CustomException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting saved job for the applicant.");
        }
    }

}