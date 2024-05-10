
package com.cts.idashboard.services.metricservice.data;

import com.cts.idashboard.services.metricservice.util.ReleaseDetailsDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.util.List;


@Data
@JsonDeserialize(using = ReleaseDetailsDeserializer.class)
public class ALMReleaseDetails {
    private List<Release> releases;
    private int totalResults;
}
