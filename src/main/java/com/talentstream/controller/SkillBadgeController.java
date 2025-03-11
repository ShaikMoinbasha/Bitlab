package com.talentstream.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.talentstream.dto.ApplicantSkillBadgeDTO;
import com.talentstream.dto.ApplicantSkillBadgeRequestDTO;
import com.talentstream.service.SkillBadgeService;

@RestController
@RequestMapping("/skill-badges")
public class SkillBadgeController {

    @Autowired
    private SkillBadgeService skillBadgeService;
    
    @PostMapping("/save")
    public ResponseEntity<String> saveSkillBadge(@RequestBody ApplicantSkillBadgeRequestDTO request) {
    	return skillBadgeService.saveApplicantSkillBadge(
            request.getApplicantId(),
            request.getSkillBadgeName(), // Assuming service method needs skillBadgeName
            request.getStatus()
        );
    	
    }
    
    @GetMapping("/{id}/skill-badges")
    public ResponseEntity<ApplicantSkillBadgeDTO> getApplicantSkillBadges(@PathVariable Long id) {
    	
    	
        return skillBadgeService.getApplicantSkillBadges(id);
    }
    
    
}
