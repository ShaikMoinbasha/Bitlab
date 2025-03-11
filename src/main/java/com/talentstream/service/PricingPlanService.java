package com.talentstream.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.talentstream.entity.PricingPlan;
import com.talentstream.repository.PricingPlanRepository;

@Service
public class PricingPlanService {

	@Autowired
	private PricingPlanRepository pricingplanRepo;

	public void savePlans(PricingPlan plan) {

		pricingplanRepo.save(plan);
	}

	public List<PricingPlan> getAllPlans() {
		return pricingplanRepo.findAll();
	}
}