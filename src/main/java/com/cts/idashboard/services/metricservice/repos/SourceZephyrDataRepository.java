package com.cts.idashboard.services.metricservice.repos;

import com.cts.idashboard.services.metricservice.data.SourceZephyrData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SourceZephyrDataRepository extends MongoRepository<SourceZephyrData, String> {


}
