/*
 *   © [2021] Cognizant. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package com.cts.idashboard.services.metricservice.repos;

import com.cts.idashboard.services.metricservice.data.project.CIQDashboardProject;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

/**
 * CIQDashboardProjectRepository
 * @author Cognizant
 */

public interface CIQDashboardProjectRepository extends MongoRepository<CIQDashboardProject, String> {

    //CIQDashboardProject findByName(String projectName);
    List<CIQDashboardProject> findByLobId(String id);

    List<CIQDashboardProject> findByOrgId(String id);

    Optional<CIQDashboardProject> findByName(String projectName);

}
