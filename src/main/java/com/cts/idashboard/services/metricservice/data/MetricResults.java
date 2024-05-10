package com.cts.idashboard.services.metricservice.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.json.simple.JSONArray;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;


@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "source_derived")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MetricResults extends BaseModel {
    @Id
    private String id;
    private String toolType;

    private String projectName;
    private String dashboardName;
    private String pageName;
    private String itemId;

    private String customFunction;
    private String customFunctionName;
    private Object customParams;

    private String grouping;
    private String groupBy;
    private String groupValue;

    private String trending;
    private String trendBy;
    private String trendingField;
    private Object trendCount;

    private String metricName;
    private Object metricValue;
    private JSONArray metricValues;
    private List<TrendingMetric> trendingMetricValues;

    private Instant lastCalculatedDate;

    private String layerName;
    private String layerId;
    private String dashboardId;
    
}
