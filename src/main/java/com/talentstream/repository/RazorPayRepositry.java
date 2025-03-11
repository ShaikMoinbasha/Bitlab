package com.talentstream.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.talentstream.entity.RazorPayOrder;
 
 
public interface RazorPayRepositry extends JpaRepository<RazorPayOrder, String> {
 
    // Custom query method to find RazorPayOrder by recruiterId
	@Query("SELECT r FROM RazorPayOrder r WHERE r.jobRecruiter.recruiterId = :recruiterId AND r.isActive = true")
	List<RazorPayOrder> findPaymentDetails(@Param("recruiterId") Long recruiterId);
}