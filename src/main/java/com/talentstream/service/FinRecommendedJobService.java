package com.talentstream.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.talentstream.dto.JobDTO;
import com.talentstream.dto.RecuriterSkillsDTO;
import com.talentstream.entity.ApplicantProfile;
import com.talentstream.entity.Job;
import com.talentstream.entity.RecuriterSkills;
import com.talentstream.exception.CustomException;
import com.talentstream.repository.ApplicantProfileRepository;
import com.talentstream.repository.ApplyJobRepository;
import com.talentstream.repository.JobRepository;
import com.talentstream.repository.SavedJobRepository;

@Service
public class FinRecommendedJobService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ApplicantProfileRepository applicantRepository;

    @Autowired
    private ApplyJobRepository applyJobRepository;

    @Autowired
    private SavedJobRepository savedJobRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final Logger logger = LoggerFactory.getLogger(FinRecommendedJobService.class);
    private static final String CACHE_KEY_PREFIX = "recommended_jobs:";

    /**
     * Counts the number of recommended jobs for an applicant.
     * Cached to reduce DB load.
     */
    @Cacheable(value = "recommendedJobCount", key = "#applicantId")
    public long countRecommendedJobsForApplicant(long applicantId) {
        try {
            Optional<ApplicantProfile> optionalApplicant = applicantRepository.findByApplicantIdWithSkills(applicantId);
            if (optionalApplicant.isEmpty()) {
                return 0;
            }

            ApplicantProfile applicant = optionalApplicant.get();
            return findJobsMatchingApplicantProfile(applicant).size();

        } catch (Exception e) {
            logger.error("Error while counting recommended jobs for applicant {}", applicantId, e);
            throw new CustomException("Error while counting recommended jobs", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Finds active recommended jobs for an applicant.
     * Implements Redis caching with expiration.
     */
//    public List<JobDTO> recommendJobsForApplicant(long applicantId, int page, int size) {
//        String cacheKey = CACHE_KEY_PREFIX + applicantId + ":" + page + ":" + size;
//
//        // Check if the cache has valid data
//        List<JobDTO> cachedJobs = (List<JobDTO>) redisTemplate.opsForValue().get(cacheKey);
//        if (cachedJobs != null) {
//            return cachedJobs;
//        }
//
//        // Fetch from database
//        ApplicantProfile applicantProfile = applicantRepository.findByApplicantId(applicantId);
//        if (applicantProfile == null) {
//            throw new CustomException("Applicant profile not found", HttpStatus.NOT_FOUND);
//        }
//
//        Set<String> skillNames = applicantProfile.getSkillsRequired().stream()
//                .map(skill -> skill.getSkillName().toLowerCase())
//                .collect(Collectors.toSet());
//
//        Set<String> preferredLocations = applicantProfile.getPreferredJobLocations();
//        Integer experience = null;
//
//        try {
//            experience = Integer.parseInt(applicantProfile.getExperience());
//        } catch (NumberFormatException e) {
//            logger.warn("Warning: Unable to parse experience as Integer");
//        }
//
//        Page<Job> jobPage = jobRepository.findJobsMatchingApplicantProfile(
//                applicantId,
//                skillNames,
//                preferredLocations,
//                experience,
//                applicantProfile.getSpecialization(),
//                PageRequest.of(page, size)
//        );
//
//        List<JobDTO> jobs = jobPage.getContent().stream().map(this::convertEntityToDTO).toList();
//
//        // Determine TTL based on the nearest close date
//        LocalDate today = LocalDate.now();
//        LocalDate closestExpiry = jobs.stream()
//                .map(JobDTO::getCreationDate)
//                .filter(date -> date.isAfter(today))
//                .min(LocalDate::compareTo)
//                .orElse(today.plusDays(1)); // Default 1-day expiration if no jobs exist
//
//        long ttl = Duration.between(today.atStartOfDay(), closestExpiry.atStartOfDay()).toSeconds();
//        redisTemplate.opsForValue().set(cacheKey, jobs, ttl, TimeUnit.SECONDS);
//
//        return jobs;
//    }
//
//    /**
//     * Finds jobs that match the applicant's profile.
//     */
//    public List<Job> findJobsMatchingApplicantProfile(ApplicantProfile applicantProfile) {
//        try {
//            Set<String> skillNames = applicantProfile.getSkillsRequired().stream()
//                    .map(skill -> skill.getSkillName().toLowerCase())
//                    .collect(Collectors.toSet());
//
//            Set<String> preferredLocations = applicantProfile.getPreferredJobLocations();
//            Integer experience = null;
//
//            try {
//                experience = Integer.parseInt(applicantProfile.getExperience());
//            } catch (NumberFormatException e) {
//                logger.warn("Warning: Unable to parse experience as Integer");
//            }
//
//            List<Object[]> result = jobRepository.findJobsMatchingApplicantProfile(
//                    applicantProfile.getApplicant().getId(),
//                    skillNames,
//                    preferredLocations,
//                    experience,
//                    applicantProfile.getSpecialization());
//
//            return result.stream().map(array -> {
//                Job job = (Job) array[0];
//                job.setIsSaved(array[1] != null ? (String) array[1] : "");
//                return job;
//            }).collect(Collectors.toList());
//
//        } catch (Exception e) {
//            logger.error("Error while finding recommended jobs", e);
//            throw new CustomException("Error while finding recommended jobs", HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    /**
//     * Converts a Job entity to DTO.
//     */
//    private JobDTO convertEntityToDTO(Job job) {
//        JobDTO jobDTO = new JobDTO();
//        jobDTO.setId(job.getId());
//        jobDTO.setJobTitle(job.getJobTitle());
//        jobDTO.setMinimumExperience(job.getMinimumExperience());
//        jobDTO.setMaximumExperience(job.getMaximumExperience());
//        jobDTO.setMinSalary(job.getMinSalary());
//        jobDTO.setMaxSalary(job.getMaxSalary());
//        jobDTO.setLocation(job.getLocation());
//        jobDTO.setEmployeeType(job.getEmployeeType());
//        jobDTO.setIndustryType(job.getIndustryType());
//        jobDTO.setMinimumQualification(job.getMinimumQualification());
//        jobDTO.setRecruiterId(job.getJobRecruiter().getRecruiterId());
//        jobDTO.setCompanyname(job.getJobRecruiter().getCompanyname());
//        jobDTO.setEmail(job.getJobRecruiter().getEmail());
//        jobDTO.setMobilenumber(job.getJobRecruiter().getMobilenumber());
//        jobDTO.setSpecialization(job.getSpecialization());
//        jobDTO.setDescription(job.getDescription());
//        jobDTO.setCreationDate(job.getCreationDate());
//        jobDTO.setIsSaved(job.getIsSaved());
//
//        Set<RecuriterSkillsDTO> skillsDTOList = job.getSkillsRequired().stream()
//                .map(this::convertSkillsEntityToDTO)
//                .collect(Collectors.toSet());
//        jobDTO.setSkillsRequired(skillsDTOList);
//        return jobDTO;
//    }
//
//    /**
//     * Converts a RecruiterSkills entity to DTO.
//     */
//    private RecuriterSkillsDTO convertSkillsEntityToDTO(RecuriterSkills skill) {
//        RecuriterSkillsDTO skillDTO = new RecuriterSkillsDTO();
//        skillDTO.setSkillName(skill.getSkillName());
//        return skillDTO;
//    }
//
//    /**
//     * Clears the cache manually when jobs are added/updated/deleted.
//     */
//    @CacheEvict(value = { "recommendedJobs", "recommendedJobCount" }, allEntries = true)
//    public void clearCache() {
//        logger.info("Cache cleared for recommended jobs.");
//    }
    // ✅ Caching job matching profile results
    @Cacheable(value = "jobsMatchingProfile", key = "#applicantProfile.applicant.id")
    
    public List<Job> findJobsMatchingApplicantProfile(ApplicantProfile applicantProfile) {
        try {
            Set<String> lowercaseApplicantSkillNames = applicantProfile.getSkillsRequired().stream()
                    .map(skill -> skill.getSkillName().toLowerCase())
                    .collect(Collectors.toSet());

            Set<String> preferredLocations = applicantProfile.getPreferredJobLocations();
            Integer experience = parseExperience(applicantProfile.getExperience());

            String specialization = applicantProfile.getSpecialization();

            logger.info("applicant id: {}", applicantProfile.getApplicant().getId());
            List<Object[]> result = jobRepository.findJobsMatchingApplicantProfile(
                    applicantProfile.getApplicant().getId(),
                    lowercaseApplicantSkillNames,
                    preferredLocations,
                    experience,
                    specialization);

            List<Job> matchingJobs = new ArrayList<>();
            for (Object[] array : result) {
                Job job = (Job) array[0];
                job.setIsSaved((String) array[1]);
                job.setIsSaved(job.getIsSaved() != null ? job.getIsSaved() : "");
                matchingJobs.add(job);
            }

            return matchingJobs;

        } catch (Exception e) {
            logger.error("Error while finding recommended jobs", e);
            throw new CustomException("Error while finding recommended jobs", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ✅ Caching recommended jobs
    @Cacheable(value = "recommendedJobs", key = "#applicantId")
    public List<JobDTO> recommendJobsForApplicant(long applicantId, int page, int size) {
        ApplicantProfile applicantProfile = applicantRepository.findByApplicantId(applicantId);

        if (applicantProfile == null) {
            throw new CustomException("Applicant profile not found", HttpStatus.NOT_FOUND);
        }

        Set<String> skillNames = applicantProfile.getSkillsRequired().stream()
                .map(skill -> skill.getSkillName().toLowerCase())
                .collect(Collectors.toSet());

        Set<String> preferredLocations = applicantProfile.getPreferredJobLocations();
        Integer experience = parseExperience(applicantProfile.getExperience());

        Page<Job> jobPage = jobRepository.findJobsMatchingApplicantProfile(
                applicantId,
                skillNames,
                preferredLocations,
                experience,
                applicantProfile.getSpecialization(),
                PageRequest.of(page, size)
        );

        return jobPage.getContent().stream()
                .map(this::convertEntityToDTO)
                .collect(Collectors.toList());
    }

    // ✅ Evict cache when applicant profile is updated
    @CacheEvict(value = {"jobsMatchingProfile", "recommendedJobs"}, key = "#applicantProfile.applicant.id")
    public void updateApplicantProfile(ApplicantProfile applicantProfile) {
        applicantRepository.save(applicantProfile);
        logger.info("Applicant profile updated. Cache evicted.");
    }

    // ✅ Evict all cached jobs when job data is updated
    @CacheEvict(value = {"jobsMatchingProfile", "recommendedJobs"}, allEntries = true)
    public void updateJob(Job job) {
        jobRepository.save(job);
        logger.info("Job details updated. Cache evicted.");
    }


    private Integer parseExperience(String experience) {
        try {
            return Integer.parseInt(experience);
        } catch (NumberFormatException e) {
            logger.warn("Warning: Unable to parse experience as Integer");
            return null;
        }
    }

    private JobDTO convertEntityToDTO(Job job) {
        JobDTO jobDTO = new JobDTO();
        jobDTO.setId(job.getId());
        jobDTO.setJobTitle(job.getJobTitle());
        jobDTO.setMinimumExperience(job.getMinimumExperience());
        jobDTO.setMaximumExperience(job.getMaximumExperience());
        jobDTO.setMinSalary(job.getMinSalary());
        jobDTO.setMaxSalary(job.getMaxSalary());
        jobDTO.setLocation(job.getLocation());
        jobDTO.setEmployeeType(job.getEmployeeType());
        jobDTO.setIndustryType(job.getIndustryType());
        jobDTO.setMinimumQualification(job.getMinimumQualification());
        jobDTO.setRecruiterId(job.getJobRecruiter().getRecruiterId());
        jobDTO.setCompanyname(job.getJobRecruiter().getCompanyname());
        jobDTO.setEmail(job.getJobRecruiter().getEmail());
        jobDTO.setMobilenumber(job.getJobRecruiter().getMobilenumber());
        jobDTO.setSpecialization(job.getSpecialization());
        jobDTO.setDescription(job.getDescription());
        jobDTO.setCreationDate(job.getCreationDate());
        jobDTO.setIsSaved(job.getIsSaved());

        Set<RecuriterSkillsDTO> skillsDTOList = job.getSkillsRequired().stream()
                .map(this::convertSkillsEntityToDTO)
                .collect(Collectors.toSet());
        jobDTO.setSkillsRequired(skillsDTOList);
        return jobDTO;
    }

    private RecuriterSkillsDTO convertSkillsEntityToDTO(RecuriterSkills skill) {
        RecuriterSkillsDTO skillDTO = new RecuriterSkillsDTO();
        skillDTO.setSkillName(skill.getSkillName());
        return skillDTO;
    }

}
