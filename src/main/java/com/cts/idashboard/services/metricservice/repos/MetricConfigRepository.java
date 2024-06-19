package com.cts.idashboard.services.metricservice.repos;

import com.cts.idashboard.services.metricservice.data.MetricConfig;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


public interface MetricConfigRepository extends MongoRepository<MetricConfig, String> {
    Optional<MetricConfig> findByMetricName(String metricName);
}
