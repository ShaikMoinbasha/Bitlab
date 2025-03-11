package com.talentstream.dto;

import java.util.List;
import java.util.Set;

import com.talentstream.entity.ApplicantSkillBadge;
import com.talentstream.entity.ApplicantSkills;

public class ApplicantSkillBadgeDTO {

	    
	    
	    private Set<ApplicantSkills> skillsRequired;
	    
	    private List<ApplicantSkillBadge> applicantSkillBadges;
	    
	   

		public Set<ApplicantSkills> getSkillsRequired() {
			return skillsRequired;
		}

		public void setSkillsRequired(Set<ApplicantSkills> skillsRequired) {
			this.skillsRequired = skillsRequired;
		}

		public List<ApplicantSkillBadge> getApplicantSkillBadges() {
			return applicantSkillBadges;
		}

		public void setApplicantSkillBadges(List<ApplicantSkillBadge> applicantSkillBadges) {
			this.applicantSkillBadges = applicantSkillBadges;
		}
	    
	    

	    
}
