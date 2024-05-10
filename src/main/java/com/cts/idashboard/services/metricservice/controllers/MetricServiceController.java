package com.cts.idashboard.services.metricservice.controllers;

import com.cts.idashboard.services.metricservice.data.MetricConfig;
import com.cts.idashboard.services.metricservice.data.MetricResponse;
import com.cts.idashboard.services.metricservice.data.ProjectMetric;
import com.cts.idashboard.services.metricservice.services.CalculateMetricsService;
import com.cts.idashboard.services.metricservice.services.MetricConfigService;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/metrics")
public class MetricServiceController {

    @Autowired
    MetricConfigService service;

    @Autowired
    CalculateMetricsService calculateMetricsService;

    @PostMapping("/save-metric-config")
    public MetricConfig saveMetricConfig(@RequestBody MetricConfig metricConfig) {
        return service.saveMetricConfig(metricConfig);
    }

    @PutMapping("/update-metric-config")
    public MetricConfig updateMetricConfig(@RequestBody MetricConfig metricConfig) throws Exception {
        return service.updateMetricConfig(metricConfig);
    }

    @PostMapping("/calculate-metrics")
    public MetricResponse calculateMetrics(@RequestBody ProjectMetric projectMetric) throws Exception {
        return calculateMetricsService.calculateMetrics(projectMetric);
    }

    @GetMapping("/get-last-calculated-metrics/{layerId}/dashboard/{dashboardId}/page/{pageName}")
    public JSONObject getLastCalculatedMetrics(@PathVariable("layerId") String layerId, @PathVariable("dashboardId") String dashboardId, @PathVariable("pageName") String pageName, @RequestParam("category") String category) throws Exception {
        return calculateMetricsService.getLastCalculatedMetricsForTool(layerId, dashboardId, category);
    }

    @GetMapping("/metric-config")
    public List<MetricConfig> getAllMetricConfigs() {
        return service.getAllMetricConfigs();
    }

    @GetMapping("/metric-config/{id}")
    public MetricConfig getAllMetricConfigById(@PathVariable("id") String id) throws Exception {
        return service.getAllMetricConfigById(id);
    }

    @GetMapping("/metric-names")
    public List<String> getMetricName() {
        return service.getMetricNames();
    }

    @DeleteMapping("/metric-config/{id}")
    public JSONObject deleteMetricConfigById(@PathVariable("id") String id) throws Exception {
        return service.deleteMultipleMetrics(id);
    }

    @DeleteMapping("/metric-config")
    public JSONObject deleteMultipleMetrics(@RequestParam("id") String id) throws Exception {
        return service.deleteMultipleMetrics(id);
    }

}
