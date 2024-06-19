

package com.cts.idashboard.services.metricservice.data;

import com.cts.idashboard.services.metricservice.util.RequirementTypeDetailsDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.util.List;


@Data
@JsonDeserialize(using = RequirementTypeDetailsDeserializer.class)
public class ALMRequirementTypeDetails {
    private List<RequirementType> types;
}
