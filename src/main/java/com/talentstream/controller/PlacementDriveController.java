package com.talentstream.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.talentstream.entity.PlacementDrive;
import com.talentstream.exception.JobNotFoundException;
import com.talentstream.exception.JobRecruiterNotFound;
import com.talentstream.service.PlacementDriveService;

@RestController
@RequestMapping("placement")
public class PlacementDriveController {
	
	@Autowired
	private PlacementDriveService driveService;
	
	@Autowired
	private static final Logger LOGGER=LoggerFactory.getLogger(PlacementDriveController.class);
	
	@PostMapping("/recruiter/drive/{recruiterId}")
	public ResponseEntity<Object> saveDrive( @PathVariable long recruiterId,@Valid  @RequestBody PlacementDrive drive ,BindingResult bindingResult) throws JobNotFoundException,JobRecruiterNotFound {
		if(bindingResult.hasErrors()) {
			
			//Storing the field and field Errors
			Map<String, String> errors=new LinkedHashMap<>();
			
			 bindingResult.getFieldErrors().forEach(fieldError -> {
	                String fieldName = fieldError.getField();
	                String errorMessage = fieldError.getDefaultMessage();
	                
	                errors.put(fieldName, errorMessage);
	         });
			 LOGGER.error("Validation errors occurred while saving Placement Drive.");
			 return ResponseEntity.badRequest().body(errors);
		}
		try {
	        PlacementDrive createdDrive = driveService.saveDrive(recruiterId, drive);
	       
	        
	        LOGGER.info("Save Drive Execuited {}",createdDrive);
	        return new ResponseEntity<>("Placement Drive Created Successfully", HttpStatus.CREATED); // 201 Created
	        
	    } catch (Exception e) {
	    	
	    	
	    	LOGGER.error(e.getMessage());
	       
	        return new ResponseEntity<>("An error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}
	
	@PutMapping("/recruiter/drive/{driveId}/updateDrive")
	public ResponseEntity<Object> updateDrive(@PathVariable long driveId,@Valid @RequestBody PlacementDrive drive, BindingResult bindingResult){
		if(bindingResult.hasErrors()) {
			//Storing the field and field Errors
			Map<String, String> errors=new LinkedHashMap<>();
		
			 bindingResult.getFieldErrors().forEach(fieldError -> {
	                String fieldName = fieldError.getField();
	                String errorMessage = fieldError.getDefaultMessage();
	                
	                errors.put(fieldName, errorMessage);
	         });
			 LOGGER.error("Validation errors occurred Updating Placement Drive.");
			 return ResponseEntity.badRequest().body(errors);
		}
		try {
			driveService.updateDrive(driveId, drive);
			return new ResponseEntity<>("Successfully Updated Drive",HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>("Some External Errors...",HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	

	@GetMapping("/recruiter/getDrive/{driveId}")
	public ResponseEntity<Object> getPlacementDrive(@PathVariable Long driveId) {
	  Optional<	PlacementDrive> getDriveById=driveService.getPlacementDriveByDriveId(driveId);
		if (getDriveById.isPresent()) {
			return new ResponseEntity<>(getDriveById, HttpStatus.OK);
		}
		return new ResponseEntity<>("No data found.."+ driveId, HttpStatus.NOT_FOUND);
	}
	
}
