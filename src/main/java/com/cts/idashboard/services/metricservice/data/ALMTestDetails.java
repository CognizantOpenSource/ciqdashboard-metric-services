
package com.cts.idashboard.services.metricservice.data;

import com.cts.idashboard.services.metricservice.util.TestDetailsDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.util.List;


@Data
@JsonDeserialize(using = TestDetailsDeserializer.class)
public class ALMTestDetails {
    private List<Test> tests;
    private int totalResults;
}
