package com.cts.idashboard.services.metricservice.data;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "dashboards")

public class Dashboard extends BaseModel {
    @Id
    private String id;
    @NotBlank(message = "Name should not be empty/null")
    @Size(min = 4, message = "Name minimum characters should be '4' ")
    @Indexed(unique = true)
    private String name;
    @NotBlank(message = "ProjectName should not be empty/null")
    @Size(min = 4, message = "ProjectName minimum characters should be '4' ")
    private String projectName;
    private boolean active;
    private Boolean openAccess = false;
    private List<IDPageConfig> pages;
    private List<MetricResults> metrics;
    private String category;
    private String projectId;

}
