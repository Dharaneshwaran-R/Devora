package com.mdm.mdm_backend.repository;

import com.mdm.mdm_backend.model.entity.EnrollmentToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EnrollmentTokenRepository extends JpaRepository<EnrollmentToken, Long> {

    Optional<EnrollmentToken> findByToken(String token);

    List<EnrollmentToken> findByEmployeeId(String employeeId);

    List<EnrollmentToken> findByStatus(String status);

    List<EnrollmentToken> findByExpiresAtBefore(LocalDateTime dateTime);

    Optional<EnrollmentToken> findByTokenAndStatus(String token, String status);
}
