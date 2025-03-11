package com.talentstream.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.talentstream.dto.JobDTO;
import com.talentstream.dto.RecuriterSkillsDTO;
import com.talentstream.dto.ScreeningAnswerDTO;
import com.talentstream.entity.Applicant;
import com.talentstream.entity.Job;
import com.talentstream.entity.JobRecruiter;
import com.talentstream.entity.JobSearchCriteria;
import com.talentstream.entity.JobSpecifications;
import com.talentstream.entity.RecuriterSkills;
import com.talentstream.entity.SavedJob;
import com.talentstream.entity.ScreeningAnswer;
import com.talentstream.entity.ScreeningQuestion;
import com.talentstream.exception.CustomException;
import com.talentstream.exception.JobNotFoundException;
import com.talentstream.repository.ApplicantRepository;
import com.talentstream.repository.CompanyProfileRepository;
import com.talentstream.repository.JobRecruiterRepository;
import com.talentstream.repository.JobRepository;
import com.talentstream.repository.PlacementRepository;
import com.talentstream.repository.RecuriterSkillsRepository;
import com.talentstream.repository.SavedJobRepository;
import com.talentstream.repository.ScreeningAnswerRepository;



@Service
public class JobService {

	private final CompanyProfileRepository companyProfileRepository;

	@Autowired
	private JobRepository jobRepository;
	@Autowired
	private RecuriterSkillsRepository skillsRepository;

	@Autowired
	private JobRecruiterRepository jobRecruiterRepository;

	@Autowired
	private SavedJobRepository savedJobRepository;

	@Autowired
	private ScreeningAnswerRepository screeningAnswerRepository;

	@Autowired
	private ApplicantRepository applicantRepository;

	@Autowired
	private PlacementRepository placementRepository;
	
	 private static  final  Logger logger = LoggerFactory.getLogger(JobService.class);
	 
	 
	 private static final String JOB_NOT_FOUND="Job not found";
	

	@Autowired
	public JobService(JobRepository jobRepository, RecuriterSkillsRepository skillsRepository,
			CompanyProfileRepository companyProfileRepository) {
		this.jobRepository = jobRepository;
		this.skillsRepository = skillsRepository;
		this.companyProfileRepository = companyProfileRepository;
	}

	public List<Job> searchJobs(JobSearchCriteria searchCriteria) {
		try {
			Page<Job> jobPage = jobRepository.findAll(JobSpecifications.searchJobs(searchCriteria.getSkillName(),
					searchCriteria.getJobTitle(), searchCriteria.getLocation(), searchCriteria.getIndustryType(),
					searchCriteria.getEmployeeType(), searchCriteria.getMinimumQualification(),
					searchCriteria.getSpecialization()), Pageable.unpaged());

			return jobPage.getContent();
		} catch (Exception e) {
			throw new CustomException("Error while searching jobs", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public List<Job> getJobsByPromoteState(long applicantId, String promote) {
		List<SavedJob> savedJobs = savedJobRepository.findByApplicantId(applicantId);
		logger.info("Saved jobs size: {}", savedJobs.size());

		List<Object[]> result = jobRepository.findByPromote(applicantId, promote);
		List<Job> matchingJobs = new ArrayList<>();
		List<Long> savedJobsIds = new ArrayList<>();

		for (SavedJob saved : savedJobs) {
			logger.info("Saved Jobs Id {} " ,saved.getJob().getId());
			savedJobsIds.add(saved.getJob().getId());
		}

		for (Object[] array : result) {
			Job job = (Job) array[0];
			job.setIsSaved((String) array[1]);

			if (!savedJobsIds.contains(job.getId())) {
				
				logger.info("geting Job id: {}", job.getId());
				matchingJobs.add(job);
			}

		}

		return matchingJobs;
	}

	public ResponseEntity<String> saveJob(JobDTO jobDTO, Long jobRecruiterId) {
		try {
			JobRecruiter jobRecruiter = jobRecruiterRepository.findByRecruiterId(jobRecruiterId);
			if (jobRecruiter != null) {
				Job job = convertDTOToEntity(jobDTO);
				job.setJobRecruiter(jobRecruiter);

				// Set screening questions
				if (jobDTO.getScreeningQuestions() != null) {
					Set<ScreeningQuestion> screeningQuestions = jobDTO.getScreeningQuestions().stream()
							.map(questionDTO -> {
								ScreeningQuestion question = new ScreeningQuestion();
								question.setQuestionText(questionDTO.getQuestionText());
								question.setJob(job);
								return question;
							}).collect(Collectors.toSet());
					job.setScreeningQuestions(screeningQuestions);
				}

				jobRepository.save(job);
				return ResponseEntity.status(HttpStatus.OK).body("Job saved successfully.");
			} else {
				throw new CustomException("JobRecruiter with ID " + jobRecruiterId + " not found.",
						HttpStatus.NOT_FOUND);
			}
		} catch (CustomException ce) {
			throw ce;
		} catch (Exception e) {
			throw new CustomException("Error while saving job", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public Job getJobById(Long jobId) {
		try {
			return jobRepository.findById(jobId).orElse(null);
		} catch (Exception e) {
			throw new CustomException("Error while retrieving job by ID", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public List<Job> getAllJobs() {
		try {
			return jobRepository.findAll();
		} catch (Exception e) {
			throw new CustomException("Error while retrieving all jobs", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public List<Job> getJobsByRecruiter(Long jobRecruiterId) {
		try {
			logger.info("before find job recruiter");
			return jobRepository.findByJobRecruiterId(jobRecruiterId);
		} catch (Exception e) {
			throw new CustomException("Error while retrieving jobs by recruiter ID", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public long countJobsByRecruiterId(Long recruiterId) {
		try {
			return jobRepository.countJobsByRecruiterId(recruiterId);
		} catch (Exception e) {
			throw new CustomException("Error while counting jobs by recruiter ID", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private Job convertDTOToEntity(JobDTO jobDTO) {
		Job job = new Job();
		job.setJobTitle(jobDTO.getJobTitle());
		job.setMinimumExperience(jobDTO.getMinimumExperience());
		job.setMaximumExperience(jobDTO.getMaximumExperience());
		job.setMinSalary(jobDTO.getMinSalary());
		job.setMaxSalary(jobDTO.getMaxSalary());
		job.setLocation(jobDTO.getLocation());
		job.setEmployeeType(jobDTO.getEmployeeType());
		job.setIndustryType(jobDTO.getIndustryType());
		job.setMinimumQualification(jobDTO.getMinimumQualification());
		job.setSpecialization(jobDTO.getSpecialization());
		job.setSkillsRequired(convertSkillsDTOToEntity(jobDTO.getSkillsRequired()));
	
		job.setDescription(jobDTO.getDescription());
		job.setCreationDate(jobDTO.getCreationDate());

		
		if (jobDTO.getScreeningQuestions() != null) {
			Set<ScreeningQuestion> screeningQuestions = jobDTO.getScreeningQuestions().stream().map(questionDTO -> {
				ScreeningQuestion question = new ScreeningQuestion();
				question.setQuestionText(questionDTO.getQuestionText());
				question.setJob(job);
				return question;
			}).collect(Collectors.toSet());
			job.setScreeningQuestions(screeningQuestions);
		}
		job.setJobURL(jobDTO.getJobURL());
		return job;
	}

	private Set<RecuriterSkills> convertSkillsDTOToEntity(Set<RecuriterSkillsDTO> skillsRequired) {
		return skillsRequired.stream().map(skillDTO -> {
			RecuriterSkills skill = new RecuriterSkills();
			skill.setSkillName(skillDTO.getSkillName());
			return skill;
		}).collect(Collectors.toSet());
	}

	public void changeJobStatus(Long jobId, String newStatus) {
		try {
			Job job = jobRepository.findById(jobId)
					.orElseThrow(() -> new CustomException(JOB_NOT_FOUND, HttpStatus.NOT_FOUND));

			// Validate newStatus (optional, depending on your requirements)

			job.setStatus(newStatus.toLowerCase());
			// saved job needs to removed.
			Job job1 = jobRepository.findById(jobId).orElse(null);
		

			if (job1 == null) {
				throw new CustomException("Applicant or Job not found", HttpStatus.INTERNAL_SERVER_ERROR);
			}

			List<SavedJob> savedJobs = savedJobRepository.findByJob(job1);

			savedJobRepository.deleteAll(savedJobs);

			jobRepository.save(job);
		} catch (CustomException ce) {
			throw ce;
		} catch (Exception e) {
			throw new CustomException("Error changing job status", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public List<Job> getActiveJobsForRecruiter(Long recruiterId) {
		return jobRepository.findJobsByRecruiterAndStatus(recruiterId, "active");
	}

	public long countInActiveJobs(Long recruiterId) {
		JobRecruiter jobRecruiter = jobRecruiterRepository.findByRecruiterId(recruiterId);
		if (jobRecruiter == null) {
			logger.info("recruiter not found : {} " , recruiterId);
			throw new CustomException("recruiter not found : " + recruiterId, HttpStatus.NOT_FOUND);
		}
		try {
			logger.info("inactive job count is {} " , jobRepository.countInActiveJobsByRecruiterId(recruiterId));
			return jobRepository.countInActiveJobsByRecruiterId(recruiterId);
		} catch (Exception e) {
			throw new CustomException("Error while counting inactive jobs for recruiter id",
					HttpStatus.INTERNAL_SERVER_ERROR);

		}
	}

	public List<Job> getInactiveJobsForRecruiter(Long recruiterId) {
		return jobRepository.findJobsByRecruiterAndStatus(recruiterId, "inactive");
	}

	public String getJobStatus(Long jobId) {
		Optional<Job> optionalJob = jobRepository.findById(jobId);

		if (optionalJob.isPresent()) {
			Job job = optionalJob.get();
			return job.getStatus(); // Assuming the status is a field in the Job entity
		} else {
			throw new CustomException(JOB_NOT_FOUND, HttpStatus.NOT_FOUND);
		}
	}

	public ResponseEntity<String> editJob(JobDTO jobDTO, Long jobId) {
		Optional<Job> existingJobOptional = jobRepository.findById(jobId);

		if (existingJobOptional.isPresent()) {
			Job existingJob = existingJobOptional.get();

			// Update the job details with the new values from jobDTO
			existingJob.setJobTitle(jobDTO.getJobTitle());
			existingJob.setMinimumExperience(jobDTO.getMinimumExperience());
			existingJob.setMaximumExperience(jobDTO.getMaximumExperience());
			existingJob.setMinSalary(jobDTO.getMinSalary());
			existingJob.setMaxSalary(jobDTO.getMaxSalary());
			existingJob.setLocation(jobDTO.getLocation());
			existingJob.setEmployeeType(jobDTO.getEmployeeType());
			existingJob.setIndustryType(jobDTO.getIndustryType());
			existingJob.setMinimumQualification(jobDTO.getMinimumQualification());
			existingJob.setSpecialization(jobDTO.getSpecialization());
		
			existingJob.setDescription(jobDTO.getDescription());

			Set<RecuriterSkills> updatedSkills = new HashSet<>();
			for (RecuriterSkillsDTO skillDTO : jobDTO.getSkillsRequired()) {
				RecuriterSkills skill = new RecuriterSkills();
				skill.setSkillName(skillDTO.getSkillName());
				
				updatedSkills.add(skill);
			}
			existingJob.setSkillsRequired(updatedSkills);

			jobRepository.save(existingJob);

			return ResponseEntity.ok("Job updated successfully.");
		} else {
			throw new CustomException(JOB_NOT_FOUND, HttpStatus.NOT_FOUND);
		}
	}

	public String cloneJob(Long jobId, Long jobRecruiterId) {
		// Fetch the details of the existing job
		Job existingJob = jobRepository.findById(jobId)
				.orElseThrow(() -> new CustomException(JOB_NOT_FOUND, HttpStatus.NOT_FOUND));
		JobRecruiter jobRecruiter = jobRecruiterRepository.findByRecruiterId(jobRecruiterId);

		// Create a new Job entity by copying the details of the existing job
		Job clonedJob = new Job();
		// Copy details like title, description, salary, etc.
		// Assign a new ID to the cloned job
		clonedJob.setId(null); // This will generate a new ID automatically
		clonedJob.setJobTitle(existingJob.getJobTitle());
		clonedJob.setMaximumExperience(existingJob.getMaximumExperience());
		clonedJob.setMinimumExperience(existingJob.getMinimumExperience());
		clonedJob.setMaxSalary(existingJob.getMaxSalary());
		clonedJob.setMinSalary(existingJob.getMinSalary());
		clonedJob.setLocation(existingJob.getLocation());
		clonedJob.setEmployeeType(existingJob.getEmployeeType());
		clonedJob.setIndustryType(existingJob.getIndustryType());
		clonedJob.setMinimumQualification(existingJob.getMinimumQualification());
		clonedJob.setSpecialization(existingJob.getSpecialization());
		// Update skillsRequired - Assuming jobDTO has a similar structure as Job
		Set<RecuriterSkills> updatedSkills = new HashSet<>();
		for (RecuriterSkills skillDTO : existingJob.getSkillsRequired()) {
			RecuriterSkills skill = new RecuriterSkills();
			skill.setSkillName(skillDTO.getSkillName());
			// skill.setMinimumExperience(skillDTO.getMinimumExperience());
			updatedSkills.add(skill);
		}
		clonedJob.setSkillsRequired(updatedSkills);
		clonedJob.setDescription(existingJob.getDescription());
		clonedJob.setJobRecruiter(jobRecruiter);
		// Save the cloned job to the database
		logger.info("About to saved");
		try {
			jobRepository.save(clonedJob);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		logger.info("jOB sAVED");
		// Convert the saved cloned job entity to DTO and return
		return "Job Reposted successfully";
	}

	@Transactional
	public void saveScreeningAnswers(Long applicantId, Long jobId, List<ScreeningAnswerDTO> screeningAnswerDTOs) {
		if (screeningAnswerDTOs == null || screeningAnswerDTOs.isEmpty()) {
			throw new IllegalArgumentException("Screening answers cannot be null or empty");
		}
		Applicant applicant = applicantRepository.findById(applicantId)
				.orElseThrow(() -> new CustomException("Applicant not found", HttpStatus.NOT_FOUND));
		Job job = jobRepository.findById(jobId)
				.orElseThrow(() -> new CustomException(JOB_NOT_FOUND, HttpStatus.NOT_FOUND));

		for (ScreeningAnswerDTO screeningAnswerDTO : screeningAnswerDTOs) {
			ScreeningQuestion question = job.getScreeningQuestions().stream()
					.filter(q -> q.getId().equals(screeningAnswerDTO.getQuestionId())).findFirst()
					.orElseThrow(() -> new CustomException("Screening question not found for the specified job",
							HttpStatus.NOT_FOUND));

			ScreeningAnswer screeningAnswer = new ScreeningAnswer();
			screeningAnswer.setApplicant(applicant);
			screeningAnswer.setScreeningQuestion(question);
			screeningAnswer.setAnswerText(screeningAnswerDTO.getAnswerText());

			screeningAnswerRepository.save(screeningAnswer);
		}
	}

	public Job saveJob(Job job) throws JobNotFoundException {

		JobRecruiter recruiter = jobRecruiterRepository.findById(job.getJobRecruiter().getRecruiterId())
				.orElseThrow(() -> new JobNotFoundException("User not found"));

		List<Job> jobs = new ArrayList<>();
		jobs.add(job);
		recruiter.setJobs(jobs);
		job.setJobRecruiter(recruiter);
		return jobRepository.save(job);
	}

}
