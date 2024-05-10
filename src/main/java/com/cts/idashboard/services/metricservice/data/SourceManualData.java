package com.cts.idashboard.services.metricservice.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "source_manual_data")
public class SourceManualData {

    @Id
    private String id;
    private String projectName;
    private String dropName;
    private String level;
    private String metricName;
    private String description;
    private String calculatedDate;
    private String calculatedValue;
    private String updatedBy;
    private String updatedDate;

}
