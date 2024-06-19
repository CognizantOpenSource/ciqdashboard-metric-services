package com.cts.idashboard.services.metricservice.data;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class MetricResponse {
    private String message;
    private Dashboard data;
    private boolean calculated;
}
