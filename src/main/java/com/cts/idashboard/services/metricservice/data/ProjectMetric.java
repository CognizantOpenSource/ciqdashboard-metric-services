package com.cts.idashboard.services.metricservice.data;


import lombok.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ProjectMetric {

    private String projectName;
    private String pageName;
    private String userName;
    private String dashboardName;
    private String itemId;
    private String metricName;
    private String toolName;
    private String almClassType;

    private String customFunction;
    private String customFunctionName;
    private Object customParams;

    private String groupBy;
    private String grouping;
    private String groupValue;

    private String trending;
    private String trendBy;
    private int trendCount;
    private String trendingField;

    private String formula;
    private Map<String, JSONObject> formulaParams;

    private List<String> jiraProjects;
    private List<String> almProjects;
    private List<String> gitProjects;
    private List<String> jenkinsProjects;
    private List<String> zephyrProjects;
    private List<String> rallyProjects;
    private List<String> serviceNowIncidentProjects;
    private List<String> botsDefectsProjects;
    private List<String> xrayProjects;
    private List<String> zephyrScaleProjects;

    private double metricValue;
    private JSONArray metricValues;
    private List<TrendingMetric> trendingMetric = new ArrayList<>();

    private String layerId;
    private String dashboardId;
    private String category;

}
