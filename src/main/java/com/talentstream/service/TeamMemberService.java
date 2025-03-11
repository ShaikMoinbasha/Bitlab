package com.talentstream.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.talentstream.dto.TeamMemberDTO;
import com.talentstream.entity.JobRecruiter;
import com.talentstream.entity.TeamMember;
import com.talentstream.exception.CustomException;
import com.talentstream.repository.JobRecruiterRepository;
import com.talentstream.repository.TeamMemberRepository;
 
 
@Service
public class TeamMemberService {
	
    @Autowired
    private TeamMemberRepository teamMemberRepository;
    @Autowired
    private JobRecruiterRepository recruiterRepository;
    
    private static final Logger logger = LoggerFactory.getLogger(TeamMemberService.class);
    
    private static final String NOT_FOUND_MESG=" not found.";
    
    public TeamMemberDTO addTeamMemberToRecruiter(Long recruiterId, TeamMemberDTO teamMemberDTO) {
        try {
        	logger.info(teamMemberDTO.getName());
 
            JobRecruiter recruiter = recruiterRepository.findById(recruiterId)
                    .orElseThrow(() -> new CustomException("Recruiter with ID " + recruiterId + NOT_FOUND_MESG, HttpStatus.NOT_FOUND));
 
            TeamMember teamMember = new TeamMember();
            teamMember.setName(teamMemberDTO.getName());
            teamMember.setRole(teamMemberDTO.getRole());
            teamMember.setEmail(teamMemberDTO.getEmail());
            teamMember.setPassword(teamMemberDTO.getPassword());
            teamMember.setRecruiter(recruiter);
            TeamMember savedTeamMember = teamMemberRepository.save(teamMember);
            TeamMemberDTO savedTeamMemberDTO = new TeamMemberDTO();
            savedTeamMemberDTO.setId(savedTeamMember.getId());
            savedTeamMemberDTO.setName(savedTeamMember.getName());
            savedTeamMemberDTO.setRole(savedTeamMember.getRole());
            savedTeamMemberDTO.setEmail(savedTeamMember.getEmail());
            savedTeamMemberDTO.setPassword(savedTeamMember.getPassword());
            return savedTeamMemberDTO;
        } catch (Exception e) {
            throw new CustomException("Failed to add team member. Please try again.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    public List<TeamMemberDTO> getTeammembersByRecruiter(long recruiterId) {
        List<TeamMember> teamMembers = teamMemberRepository.findByJobRecruiterId(recruiterId);
 
        if (teamMembers.isEmpty()) {
            throw new CustomException("No team members found for Recruiter with ID " + recruiterId, HttpStatus.NOT_FOUND);
        }
 
        return teamMembers.stream()
                .map(obj -> {
                    TeamMemberDTO teamMemberDTO = new TeamMemberDTO();
                    teamMemberDTO.setId(obj.getId());
                    teamMemberDTO.setName(obj.getName());
                    teamMemberDTO.setRole(obj.getRole());
                    teamMemberDTO.setEmail(obj.getEmail());
                    teamMemberDTO.setPassword(obj.getPassword());
                    return teamMemberDTO;
                })
                .collect(Collectors.toList());
    }
  
    public void deleteTeamMemberById(Long teamMemberId) {
        if (teamMemberRepository.existsById(teamMemberId)) {
            teamMemberRepository.deleteById(teamMemberId);
        } else {
            throw new CustomException("Team Member with ID " + teamMemberId +NOT_FOUND_MESG, HttpStatus.NOT_FOUND);
        }
    }
    
    public void resetTeamMemberPassword(Long teamMemberId, String newPassword) {
        TeamMember teamMember = teamMemberRepository.findById(teamMemberId)
                .orElseThrow(() -> new CustomException("Team Member with ID " + teamMemberId + NOT_FOUND_MESG, HttpStatus.NOT_FOUND));
 
        teamMember.setPassword(newPassword);
        teamMemberRepository.save(teamMember);
    }
}