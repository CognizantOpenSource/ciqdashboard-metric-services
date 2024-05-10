package com.cts.idashboard.services.metricservice.repos;

import com.cts.idashboard.services.metricservice.data.Dashboard;
import com.cts.idashboard.services.metricservice.data.LOB;
import com.cts.idashboard.services.metricservice.data.project.CIQDashboardProject;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface LOBRepository extends MongoRepository<LOB, String> {

    Optional<LOB> findByLobName(String name);
}
