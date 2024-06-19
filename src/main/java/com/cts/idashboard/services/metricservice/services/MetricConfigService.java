package com.cts.idashboard.services.metricservice.services;

import com.cts.idashboard.services.metricservice.data.MetricConfig;
import com.cts.idashboard.services.metricservice.repos.MetricConfigRepository;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class MetricConfigService {
    @Autowired
    MetricConfigRepository metricConfigRepo;

    @Autowired
    MongoTemplate mongoTemplate;

    public MetricConfig saveMetricConfig(MetricConfig metricConfig) {
        return metricConfigRepo.save(metricConfig);
//        return mongoTemplate.save(metricConfig);
    }

    public MetricConfig updateMetricConfig(MetricConfig metricConfig) throws Exception {
        Optional<MetricConfig> existingMetricConfig = metricConfigRepo.findById(metricConfig.getId());
        if (existingMetricConfig.isPresent()) {
            MetricConfig modifiedMetricConfig = mergeObjects(metricConfig, existingMetricConfig.get());
            return metricConfigRepo.save(modifiedMetricConfig);
        }
        throw new Exception("Metric Not found for update");
    }

//    public MetricConfig updateMetricConfig(MetricConfig metricConfig) throws Exception {
//        MetricConfig existingMetricConfig = mongoTemplate.findById(metricConfig.getId(), MetricConfig.class);
//        if (existingMetricConfig != null) {
//            MetricConfig modifiedMetricConfig = mergeObjects(metricConfig, existingMetricConfig);
//            return mongoTemplate.save(modifiedMetricConfig);
//        }
//        throw new Exception("Metric Not found for update");
//    }

    private MetricConfig mergeObjects(MetricConfig modifiedMetric, MetricConfig existingMetric)
            throws InstantiationException, IllegalAccessException {
        Field[] fields = modifiedMetric.getClass().getDeclaredFields();
        MetricConfig returnValue = modifiedMetric.getClass().newInstance();

        Arrays.stream(fields).forEach(field -> {
            ReflectionUtils.makeAccessible(field);
            Object value1;
            try {
                value1 = field.get(modifiedMetric);
                Object value2 = field.get(existingMetric);
                Object value = (value1 != null) ? value1 : value2;
                field.set(returnValue, value);
            } catch (IllegalArgumentException | IllegalAccessException e) {
//                log.error("[CIQDModelService - mergeObjects() ---] Exception  - " + ExceptionUtils.getStackTrace(e));
//                throw new CIQDModelException("Error Occurred while updating Model",
//                        ExceptionCode.INTERNAL_SERVER_ERROR);

            }
        });
        return returnValue;
    }

    public List<MetricConfig> getAllMetricConfigs() {

        return metricConfigRepo.findAll();
    }

    public List<String> getMetricNames() {
        return mongoTemplate.findDistinct("metricName", MetricConfig.class, String.class);
    }

    public MetricConfig getAllMetricConfigById(String id) throws Exception {
        Optional<MetricConfig> metric = metricConfigRepo.findById(id);
        if (metric.isPresent()) {
            return metric.get();
        } else {
            throw new Exception("Metric not found!");
        }
    }

    public String deleteMetricById(String id) throws Exception {
        Optional<MetricConfig> metric = metricConfigRepo.findById(id);
        if (metric.isPresent()) {
            metricConfigRepo.deleteById(metric.get().getId());
            return "Success";
        } else {
            throw new Exception("Metric not found!");
        }
    }

    public JSONObject deleteMultipleMetrics(String id) throws Exception {
        try {
            String[] ids = id.split(",");

            for (String str : ids) {
                metricConfigRepo.deleteById(str);
            }
            JSONObject result=new JSONObject();
            result.put("status","success");
            return result;
        }catch(Exception e){
            throw new Exception(e);
        }

    }
}
