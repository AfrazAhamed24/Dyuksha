package com.dyuksha.repository;

import com.dyuksha.model.Emergency;
import com.dyuksha.model.EmergencyStatus;
import com.dyuksha.model.Severity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link Emergency} entities.
 */
@Repository
public interface EmergencyRepository extends JpaRepository<Emergency, Long> {

    /** All emergencies sorted newest-first. */
    List<Emergency> findAllByOrderByReportedAtDesc();

    /** Most recent N emergencies. */
    List<Emergency> findTop10ByOrderByReportedAtDesc();

    /** Count emergencies that are NOT in the given status (e.g., not RESOLVED → active). */
    long countByStatusNot(EmergencyStatus status);

    /** Count emergencies matching severity AND not in the given status. */
    long countBySeverityAndStatusNot(Severity severity, EmergencyStatus status);
}

