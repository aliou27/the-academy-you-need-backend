package com.theacademyyouneed.the_academy_you_need_backend.repository;

import com.theacademyyouneed.the_academy_you_need_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByFirstNameAndLastName(String firstName, String lastName);

    @Query("SELECT u FROM User u WHERE u.emailVerified = true AND u.role = 'ADMIN'")
    Optional<User> findFirstAdmin();

}