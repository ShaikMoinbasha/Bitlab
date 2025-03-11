package com.talentstream.repository;



import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
 
import com.talentstream.entity.Alerts;
 
public interface AlertsRepository extends JpaRepository<Alerts, Long>{
 
	
 
	List<Alerts> findByApplicantIdOrderByChangeDateDesc(long applicantId);
}