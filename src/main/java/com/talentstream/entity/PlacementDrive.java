package com.talentstream.entity;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonBackReference;

enum ExamMode {
	ONLINE, OFFLINE
}

@Entity
//@Data
public class PlacementDrive {
	
	private static final Logger LOGGER=LoggerFactory.getLogger(PlacementDrive.class);

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "recruiter_id")
	private JobRecruiter jobRecruiters;

	@NotNull(message = " drive Name is missing the data")
	private String driveName;

	private Date fromDate;
	private Date toDate;

	private String startTime;
	private String endTime;

	@Enumerated(EnumType.STRING)
	private ExamMode mode;

	@Length(min = 15, max = 200)
	private String gudlines;

	@Length(min = 15, max = 200)
	private String interviewProccess;

	@OneToMany(mappedBy = "placementDrive", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonBackReference
	private List<Job> jobs;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public JobRecruiter getJobRecruiters() {
		return jobRecruiters;
	}

	public void setJobRecruiters(JobRecruiter jobRecruiters) {
		
		LOGGER.info("Job recruiter is:{} ",jobRecruiters);
		this.jobRecruiters = jobRecruiters;
	}

	public String getDriveName() {
		return driveName;
	}

	public void setDriveName(String driveName) {
		this.driveName = driveName;
	}

	public Date getFromDate() {
		return fromDate;
	}

	public void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
	}

	public Date getToDate() {
		return toDate;
	}

	public void setToDate(Date toDate) {
		this.toDate = toDate;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public ExamMode getMode() {
		return mode;
	}

	public void setMode(ExamMode mode) {
		this.mode = mode;
	}

	public String getGudlines() {
		return gudlines;
	}

	public void setGudlines(String gudlines) {
		this.gudlines = gudlines;
	}

	public String getInterviewProccess() {
		return interviewProccess;
	}

	public void setInterviewProccess(String interviewProccess) {
		this.interviewProccess = interviewProccess;
	}

	public List<Job> getJobs() {
		return jobs;
	}

	public void setJobs(List<Job> jobs) {
		this.jobs = jobs;
	}

	@Override
	public String toString() {
		return "PlacementDrive{" + "id=" + id + ", driveName='" + driveName + '\'' + ", fromDate=" + fromDate
				+ ", toDate=" + toDate + ", startTime='" + startTime + '\'' + ", endTime='" + endTime + '\'' + ", mode="
				+ mode + ", gudlines='" + gudlines + '\'' + ", interviewProccess='" + interviewProccess + '\''
				+ ", jobRecruiters=" + (jobRecruiters != null ? jobRecruiters.getRecruiterId() : "null")
				+ ", jobsCount=" + (jobs != null ? jobs.size() : 0) + '}';
	}

}
