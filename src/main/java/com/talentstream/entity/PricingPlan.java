package com.talentstream.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

@Entity
public class PricingPlan {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@NotNull(message = "Plan name is required")
	private String planName;
	private int databaseAccess;
	private int jobListings;
	private boolean emailCommunication;
	private boolean pushNotifications;
	private boolean candidateManagement;
	@NotNull(message = "Define validity in months")
	private String validity;
	private boolean whatsappSupport;
	private boolean dedicatedManager;
	private boolean customPrescreening;
	@NotNull(message = "Plan price is required")
	private Integer pricing;
	private String contactDetails;

	public PricingPlan() {

	}

	public PricingPlan(String planName, int databaseAccess, int jobListings, boolean emailCommunication,
			boolean pushNotifications, boolean candidateManagement, String validity, boolean whatsappSupport,
			boolean dedicatedManager, boolean customPrescreening, int pricing, String contactDetails) {
		this.planName = planName;
		this.databaseAccess = databaseAccess;
		this.jobListings = jobListings;
		this.emailCommunication = emailCommunication;
		this.pushNotifications = pushNotifications;
		this.candidateManagement = candidateManagement;
		this.validity = validity;
		this.whatsappSupport = whatsappSupport;
		this.dedicatedManager = dedicatedManager;
		this.customPrescreening = customPrescreening;
		this.pricing = pricing;
		this.contactDetails = contactDetails;
	}

	public String getPlanName() {
		return planName;
	}

	public void setPlanName(String planName) {
		this.planName = planName;
	}

	public int getDatabaseAccess() {
		return databaseAccess;
	}

	public void setDatabaseAccess(int databaseAccess) {
		this.databaseAccess = databaseAccess;
	}

	public int getJobListings() {
		return jobListings;
	}

	public void setJobListings(int jobListings) {
		this.jobListings = jobListings;
	}

	public boolean isEmailCommunication() {
		return emailCommunication;
	}

	public void setEmailCommunication(boolean emailCommunication) {
		this.emailCommunication = emailCommunication;
	}

	public boolean isPushNotifications() {
		return pushNotifications;
	}

	public void setPushNotifications(boolean pushNotifications) {
		this.pushNotifications = pushNotifications;
	}

	public boolean isCandidateManagement() {
		return candidateManagement;
	}

	public void setCandidateManagement(boolean candidateManagement) {
		this.candidateManagement = candidateManagement;
	}

	public String getValidity() {
		return validity;
	}

	public void setValidity(String validity) {
		this.validity = validity;
	}

	public boolean isWhatsappSupport() {
		return whatsappSupport;
	}

	public void setWhatsappSupport(boolean whatsappSupport) {
		this.whatsappSupport = whatsappSupport;
	}

	public boolean isDedicatedManager() {
		return dedicatedManager;
	}

	public void setDedicatedManager(boolean dedicatedManager) {
		this.dedicatedManager = dedicatedManager;
	}

	public boolean isCustomPrescreening() {
		return customPrescreening;
	}

	public void setCustomPrescreening(boolean customPrescreening) {
		this.customPrescreening = customPrescreening;
	}

	public int getPricing() {
		return pricing;
	}

	public void setPricing(int pricing) {
		this.pricing = pricing;
	}

	public String getContactDetails() {
		return contactDetails;
	}

	public void setContactDetails(String contactDetails) {
		this.contactDetails = contactDetails;
	}
}
