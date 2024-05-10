package com.cts.idashboard.services.metricservice.data;

import lombok.Builder;
import lombok.Getter;

import java.util.Date;

@Getter
@Builder
public class TrendingMetric {

    private Integer result;
    private Date month;
}
