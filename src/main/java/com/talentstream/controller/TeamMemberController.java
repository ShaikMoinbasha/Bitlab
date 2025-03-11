package com.talentstream.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.talentstream.dto.TeamMemberDTO;
import com.talentstream.exception.CustomException;
import com.talentstream.service.TeamMemberService;
@RestController
@CrossOrigin("*")
@RequestMapping("/team")
public class TeamMemberController {
 
    @Autowired
    private TeamMemberService teamMemberService;
    
    
    private static final String NOT_FOUND_MESG=" not found";
    
    private static final String TEAM_ID="Team Member with ID ";
   
    @PostMapping("/add/{recruiterId}/team-members")
    public ResponseEntity<Object> addTeamMemberToRecruiter(
            @PathVariable Long recruiterId,
            @RequestBody TeamMemberDTO teamMember
    ) {
        try {
            TeamMemberDTO savedTeamMember = teamMemberService.addTeamMemberToRecruiter(recruiterId, teamMember);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedTeamMember);
        } catch (CustomException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Recruiter with ID " + recruiterId + NOT_FOUND_MESG);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add team member. Please try again.");
        }
    }
 
  
    
    @GetMapping("get/teammembers/{recruiterId}")
    public ResponseEntity<Object> getTeammembersByRecruiter(@PathVariable("recruiterId") long recruiterId) {
        try {
            List<TeamMemberDTO> teamMembers = teamMemberService.getTeammembersByRecruiter(recruiterId);
            if (teamMembers.isEmpty()) {
                return ResponseEntity.notFound().build();
            } else {
                return ResponseEntity.ok(teamMembers);
            }
        } catch (CustomException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Recruiter with ID " + recruiterId + NOT_FOUND_MESG);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to retrieve team members. Please try again.");
        }
    }
    
    @DeleteMapping("delete/{teamMemberId}")
    public ResponseEntity<Object> deleteTeamMember(@PathVariable Long teamMemberId) {
        try {
            teamMemberService.deleteTeamMemberById(teamMemberId);
            return ResponseEntity.ok(TEAM_ID+ teamMemberId + " deleted successfully");
        } catch (CustomException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(TEAM_ID + teamMemberId + NOT_FOUND_MESG);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete Team Member. Please try again.");
        }
    }
    
    @PutMapping("/{teamMemberId}/reset-password")
    public ResponseEntity<Object> resetPassword(
            @PathVariable Long teamMemberId,
            @RequestParam("newPassword") String newPassword
    ) {
        try {
            teamMemberService.resetTeamMemberPassword(teamMemberId, newPassword);
            return ResponseEntity.ok("Password for Team Member with ID " + teamMemberId + " reset successfully");
        } catch (CustomException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Team Member with ID " + teamMemberId + NOT_FOUND_MESG);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to reset password for Team Member. Please try again.");
        }
    }
 
}
    
    