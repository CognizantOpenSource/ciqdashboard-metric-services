package com.cts.idashboard.services.metricservice.data;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "ciqDashbordOrg")
@Data
public class Organization extends BaseModel {
    @Id
    private String id;

    private String organizationName;
    private String organizationDesc;
}
