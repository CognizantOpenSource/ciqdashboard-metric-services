package com.cts.idashboard.services.metricservice.data;

import lombok.*;
import org.json.simple.JSONObject;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Document("MetricConfig")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class MetricConfig {
    @Id
    private String id;
    private String metricName;
    private String category;
    private String calculationType;
    private String toolType;

    private String formula;
    private Map<String, JSONObject> formulaParams;

    private String customFunction;
    private String customFunctionName;
    private Object customParams;

    private String trending;
    private String trendBy;
    private String trendingField;
    private int trendCount;

    private String grouping;
    private String groupBy;
    private String groupValue;

}
