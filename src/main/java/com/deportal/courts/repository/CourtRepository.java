package com.deportal.courts.repository;

import com.deportal.courts.entity.CourtEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourtRepository extends JpaRepository<CourtEntity, String> {

    boolean existsByNameIgnoreCase(String name);
}
