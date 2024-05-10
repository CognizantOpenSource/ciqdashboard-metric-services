package com.cts.idashboard.services.metricservice.repos;

import com.cts.idashboard.services.metricservice.data.CollectorRunScheduler;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SchedulerRunsRepository extends MongoRepository<CollectorRunScheduler, String> {
    Optional<CollectorRunScheduler> findByToolNameIgnoreCase(String toolName);
}
