package com.talentstream.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.talentstream.entity.JobVisit;
import com.talentstream.service.VisitService;

@RestController

@RequestMapping("/jobVisit")
public class JobVisitController {
	
	@Autowired
	private VisitService visitService;
	
	@PostMapping("/applicant/track-visit")
	public ResponseEntity<String> trackVisit(@RequestBody JobVisit visitRequest) {
	    Long userId = visitRequest.getUserId(); // No need to box/unbox
	    Long jobId = visitRequest.getJobId();   // No need to box/unbox

	    boolean alreadyVisited = visitService.hasVisited(userId, jobId);

	    if (!alreadyVisited) {
	        visitService.incrementVisitCount(jobId);
	        visitService.recordVisit(userId, jobId);
	    }

	    return ResponseEntity.ok("Visit tracked successfully");
	}

}
