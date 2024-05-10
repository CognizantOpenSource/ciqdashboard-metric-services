
package com.cts.idashboard.services.metricservice.data;

import com.cts.idashboard.services.metricservice.util.RequirementDetailsDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.util.List;

@Data
@JsonDeserialize(using = RequirementDetailsDeserializer.class)
public class ALMRequirementDetails {
    private List<Requirement> requirements ;
    private int totalResults;
}
