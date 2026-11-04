package com.dyuksha.repository;

import com.dyuksha.model.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    List<ActivityLog> findTop15ByOrderByTimestampDesc();

    List<ActivityLog> findAllByOrderByTimestampDesc();
}

