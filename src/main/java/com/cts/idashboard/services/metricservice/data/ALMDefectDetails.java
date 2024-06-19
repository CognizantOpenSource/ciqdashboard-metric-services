
package com.cts.idashboard.services.metricservice.data;

import com.cts.idashboard.services.metricservice.util.DefectDetailsDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.util.List;

@Data
@JsonDeserialize(using = DefectDetailsDeserializer.class)
public class ALMDefectDetails {
    private List<Defect> defects;
    private int totalResults;
}
