package com.cts.idashboard.services.metricservice.repos;

import com.cts.idashboard.services.metricservice.data.SourceTools;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SourceToolsRepository extends MongoRepository<SourceTools, String> {

    Optional<SourceTools> getByToolNameIgnoreCase(String toolName);

}
