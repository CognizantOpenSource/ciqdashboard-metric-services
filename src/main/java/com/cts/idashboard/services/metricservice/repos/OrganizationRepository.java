package com.cts.idashboard.services.metricservice.repos;

import com.cts.idashboard.services.metricservice.data.Organization;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface OrganizationRepository extends MongoRepository<Organization, String> {

    Optional<Organization> findByOrganizationName(String name);
}
