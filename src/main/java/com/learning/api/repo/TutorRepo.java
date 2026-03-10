package com.learning.api.repo;

import com.learning.api.entity.Tutor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TutorRepo extends JpaRepository<Tutor, Long> {
}