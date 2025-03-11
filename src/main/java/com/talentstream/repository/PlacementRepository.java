package com.talentstream.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.talentstream.entity.PlacementDrive;

@Repository
public interface PlacementRepository extends JpaRepository<PlacementDrive, Long> {

}
