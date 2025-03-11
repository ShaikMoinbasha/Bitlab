package com.talentstream.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
 
import com.talentstream.entity.PricingPlan;
@Repository
public interface PricingPlanRepository extends JpaRepository<PricingPlan, Long>{
}
 
 