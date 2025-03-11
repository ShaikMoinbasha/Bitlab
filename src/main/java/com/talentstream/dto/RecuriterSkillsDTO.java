package com.talentstream.dto;

import java.io.Serializable;

public class RecuriterSkillsDTO implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private String skillName;

	public String getSkillName() {
		return skillName;
	}

	public void setSkillName(String skillName) {
		this.skillName = skillName;
	}

}
