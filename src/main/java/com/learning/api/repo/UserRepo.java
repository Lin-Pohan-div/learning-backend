package com.learning.api.repo;

import com.learning.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

<<<<<<<< HEAD:src/main/java/com/learning/api/repo/UserRepository.java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
========
public interface UserRepo extends JpaRepository<User, Long> {
>>>>>>>> upstream/feature/develop:src/main/java/com/learning/api/repo/UserRepo.java
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
