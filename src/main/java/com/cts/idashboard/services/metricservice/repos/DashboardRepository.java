package com.cts.idashboard.services.metricservice.repos;

import com.cts.idashboard.services.metricservice.data.Dashboard;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;


public interface DashboardRepository extends MongoRepository<Dashboard, String> {


    Optional<Dashboard> findByProjectNameAndName(String projectName, String name);

    Optional<Dashboard> findByIdAndProjectId(String id, String projectId);
}
