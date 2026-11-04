package com.dyuksha.repository;

import com.dyuksha.model.Resource;
import com.dyuksha.model.ResourceStatus;
import com.dyuksha.model.ResourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link Resource} entities.
 */
@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {

    List<Resource> findAllByOrderByIdAsc();

    List<Resource> findByTypeOrderByIdAsc(ResourceType type);

    long countByStatus(ResourceStatus status);

    long countByTypeAndStatus(ResourceType type, ResourceStatus status);

    List<Resource> findByAssignedEmergencyId(Long emergencyId);
}

