package com.learning.api.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.learning.api.entity.Tutor;

@Repository
public interface TutorRepo extends JpaRepository<Tutor, Long> {
	
}
