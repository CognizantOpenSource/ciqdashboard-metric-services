package com.cts.idashboard.services.metricservice.repos;

import com.cts.idashboard.services.metricservice.data.MetricResults;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;


public interface MetricsRepository extends MongoRepository<MetricResults, String> {

    Optional<MetricResults> findByMetricNameAndDashboardIdAndItemId(String metricName, String dashboardId, String itemId);

    Optional<MetricResults> findByMetricNameAndItemId(String metricName, String itemId);

    List<MetricResults> findByProjectNameAndDashboardNameAndPageName(String projectName, String dashboardName, String pageName);


    List<MetricResults> findByLayerIdAndDashboardId(String layerId, String dashboardId);
    Optional<MetricResults> findByMetricNameAndItemIdAndDashboardIdAndLayerId(String metricName, String itemId, String dashboardId, String layerId);


}
