package com.cts.idashboard.services.metricservice.services;

import com.cts.idashboard.services.metricservice.data.MetricResults;
import com.cts.idashboard.services.metricservice.data.ProjectMetric;
import com.cts.idashboard.services.metricservice.data.SourceZephyrData;
import com.cts.idashboard.services.metricservice.repos.*;
import com.fathzer.soft.javaluator.DoubleEvaluator;
import com.fathzer.soft.javaluator.StaticVariableSet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
public class CalculateZephyrMetrics {

    @Autowired
    MetricsRepository metricsRepository;

    @Autowired
    MongoOperations mongoOperation;

    @Autowired
    MetricFunctions metricFunctions;

   @Autowired
   TrendDateFormatter trendDateFormatter;

    @Autowired
    CustomFunctions customFunctions;


    public MetricResults calculateZephyrMetrics(String metricName, ProjectMetric projectMetric,String SourceZephyrData) throws Exception {

        int calculatedValue = 0;
        MetricResults metrics = null;
        JSONArray metricValues = new JSONArray();

        if(projectMetric.getCustomFunction().equalsIgnoreCase("Yes")) {
            System.out.println("*** Custom function calculation ***");
            projectMetric = customFunctions.executeCustomFunction(projectMetric, SourceZephyrData);
        }
        else if(projectMetric.getTrending().equalsIgnoreCase("Yes")){
            System.out.println("*** TrendBy calculation ***");
            System.out.println("TrendBy value : "+projectMetric.getTrendBy());
            projectMetric = trendDateFormatter.trendDateFormatMethod(metricName,projectMetric,SourceZephyrData);

        }
        else if (projectMetric.getGrouping().equalsIgnoreCase("Yes")) {
            System.out.println("*** Grouping calculation *** ");
            System.out.println("GroupBy value : "+projectMetric.getGroupBy());
            List<String> distinctData = null;
            if (!(projectMetric.getGroupValue().equalsIgnoreCase("All"))) {
                distinctData = Arrays.asList(projectMetric.getGroupValue().split(","));
            } else {
                distinctData =mongoOperation.findDistinct(projectMetric.getGroupBy(), SourceZephyrData.class, String.class);
            }
            System.out.println("Grouping values ** " + distinctData);
            for (String distinctValue : distinctData) {
                System.out.println("Calculation for the group value : "+distinctValue);
                calculatedValue = calculateMetricValue(metricName, distinctValue, projectMetric);
                JSONObject evaluatedResult = new JSONObject();
                evaluatedResult.put("groupName", distinctValue);
                evaluatedResult.put("result", calculatedValue);
                metricValues.add(evaluatedResult);
            }
            projectMetric.setMetricValues(metricValues);
        }
        else {
            System.out.println("Basic metric calculation without trending or grouping -- *** --");
            calculatedValue = calculateMetricValue(metricName, null, projectMetric);
        }

        metrics = saveMetricResult(metricName, calculatedValue, projectMetric);
        return metrics;
    }

    public int calculateMetricValue(String metricName, String distinctValue, ProjectMetric projectMetric) throws Exception {

        String formula = projectMetric.getFormula();
        Map<String, JSONObject> formulaParams = projectMetric.getFormulaParams();
        System.out.println("formula in method- " + formula);
        DoubleEvaluator eval = new DoubleEvaluator();
        StaticVariableSet<Double> variableSet = new StaticVariableSet<Double>();
        Double result = 0.0;

        String isGrouping = "No";

        if(distinctValue!= null){
            isGrouping ="Yes";
        }

        if(formula!=null && formulaParams!=null) {
            for (Map.Entry<String, JSONObject> map : formulaParams.entrySet()) {
                System.out.println(map.getKey() + " " + map.getValue());
                Map<String, JSONObject> valueObjectMap = map.getValue();
                System.out.println("valueMap - " + valueObjectMap);
                for (Map.Entry<String, JSONObject> valObj : valueObjectMap.entrySet()) {
                    System.out.println(valObj.getKey() + " ****** " + valObj.getValue());
                    Map<String, String> valueMap = valObj.getValue();

                    System.out.println("value Map -- ** -- " + valueMap);

                    if (valObj.getKey().equals("count")) {
                        Long valKey = metricFunctions.count("SourceZephyrData", valueMap.entrySet(), projectMetric, "No", isGrouping, null, null, distinctValue);
                        variableSet.set(map.getKey(), Double.valueOf(valKey));
                    } else if (valObj.getKey().equals("countAnd")) {
                        Long valKey = metricFunctions.countAnd("SourceZephyrData", valueMap.entrySet(), projectMetric, "No", isGrouping, null, null, distinctValue);
                        variableSet.set(map.getKey(), Double.valueOf(valKey));
                    } else if (valObj.getKey().equals("sum")) {
                        Double valKey = metricFunctions.sum("SourceZephyrData", valueMap.entrySet(), projectMetric, "No", isGrouping, null, null, distinctValue);
                        variableSet.set(map.getKey(), valKey);
                    } else if (valObj.getKey().equals("sumAnd")) {
                        Double valKey = metricFunctions.sumAnd("SourceZephyrData", valueMap.entrySet(), projectMetric, "No", isGrouping, null, null, distinctValue);
                        variableSet.set(map.getKey(), valKey);
                    } else if (valObj.getKey().equals("avg")) {
                        Double valKey = metricFunctions.avg("SourceZephyrData", valueMap.entrySet(), projectMetric, "No", isGrouping, null, null, distinctValue);
                        variableSet.set(map.getKey(), valKey);
                    } else if (valObj.getKey().equals("avgAnd")) {
                        Double valKey = metricFunctions.avgAnd("SourceZephyrData", valueMap.entrySet(), projectMetric, "No", isGrouping, null, null, distinctValue);
                        variableSet.set(map.getKey(), valKey);
                    } else if (valObj.getKey().equals("sumISO")) {
                        Double valKey = metricFunctions.sumIS0("SourceZephyrData", valueMap.entrySet(), projectMetric, "No", isGrouping, null, null, distinctValue);
                        variableSet.set(map.getKey(), valKey);
                    } else if (valObj.getKey().equals("sumISOAnd")) {
                        Double valKey = metricFunctions.sumIS0And("SourceZephyrData", valueMap.entrySet(), projectMetric, "No", isGrouping, null, null, distinctValue);
                        variableSet.set(map.getKey(), valKey);
                    } else if (valObj.getKey().equals("sumISODiff")) {
                        Double valKey = metricFunctions.sumIS0Diff("SourceZephyrData", valueMap.entrySet(), projectMetric, "No", isGrouping, null, null, distinctValue);
                        variableSet.set(map.getKey(), valKey);
                    } else if (valObj.getKey().equals("sumISODiffAnd")) {
                        Double valKey = metricFunctions.sumIS0DiffAnd("SourceZephyrData", valueMap.entrySet(), projectMetric, "No", isGrouping, null, null, distinctValue);
                        variableSet.set(map.getKey(), valKey);
                    } else if (valObj.getKey().equals("max")) {
                        Long valKey = metricFunctions.max("SourceZephyrData", valueMap.entrySet(), projectMetric, "No", isGrouping, null, null, distinctValue);
                        variableSet.set(map.getKey(), valKey.doubleValue());
                    } else if (valObj.getKey().equals("maxAnd")) {
                        Long valKey = metricFunctions.maxAnd("SourceZephyrData", valueMap.entrySet(), projectMetric, "No", isGrouping, null, null, distinctValue);
                        variableSet.set(map.getKey(), valKey.doubleValue());
                    } else if (valObj.getKey().equals("min")) {
                        Long valKey = metricFunctions.min("SourceZephyrData", valueMap.entrySet(), projectMetric, "No", isGrouping, null, null, distinctValue);
                        variableSet.set(map.getKey(), valKey.doubleValue());
                    } else if (valObj.getKey().equals("minAnd")) {
                        Long valKey = metricFunctions.minAnd("SourceZephyrData", valueMap.entrySet(), projectMetric, "No", isGrouping, null, null, distinctValue);
                        variableSet.set(map.getKey(), valKey.doubleValue());
                    } else{
                        System.err.println("Metric function not exists with name : "+valObj.getKey());
                        variableSet.set(map.getKey(), Double.valueOf(0));
                    }
                }
            }
            result = eval.evaluate(formula, variableSet);
        }

        if(result.isInfinite()) {
            System.err.println("Formula output is infinity. Please check the formula");
        }
        else if(result.isNaN()){
            System.err.println("Formula output is not a number. Please check the formula & formula params");
        }

        System.out.println("Formula result value ** " + result);
        return (int)Math.round(result);
    }

    public MetricResults saveMetricResult(String metricName, int metricValue, ProjectMetric projectMetric) {

        //Optional<MetricResults> metricResults = metricsRepository.findByMetricNameAndDashboardNameAndItemId(metricName, projectMetric.getDashboardName(), projectMetric.getPageName(), projectMetric.getItemId());
        Optional<MetricResults> metricResults = metricsRepository.findByMetricNameAndItemIdAndDashboardIdAndLayerId(metricName, projectMetric.getItemId(), projectMetric.getDashboardId(), projectMetric.getLayerId());
        System.out.println("Metric results "+metricResults);

        if (metricResults.isPresent()) {
            MetricResults metricResult = metricResults.get();
            metricResult.setMetricName(metricName);
            metricResult.setToolType("Zephyr");
            //metricResult.setProjectName(projectMetric.getProjectName());
            //metricResult.setPageName(projectMetric.getPageName());
            //metricResult.setDashboardName(projectMetric.getDashboardName());
            metricResult.setItemId(projectMetric.getItemId());
            metricResult.setDashboardId(projectMetric.getDashboardId());
            metricResult.setLayerId(projectMetric.getLayerId());
            metricResult.setLayerName(projectMetric.getCategory());
            metricResult.setGrouping(projectMetric.getGrouping());
            metricResult.setTrending(projectMetric.getTrending());
            metricResult.setCustomFunction(projectMetric.getCustomFunction());

            if (projectMetric.getCustomFunction().equalsIgnoreCase("Yes")) {
                metricResult.setMetricValues(projectMetric.getMetricValues());
                metricResult.setCustomFunction(projectMetric.getCustomFunction());
                metricResult.setCustomFunctionName(projectMetric.getCustomFunctionName());
                if(projectMetric.getCustomParams()!=null){
                    metricResult.setCustomParams(projectMetric.getCustomParams());
                }
            }
            else if (projectMetric.getGrouping().equalsIgnoreCase("Yes")) {
                metricResult.setMetricValues(projectMetric.getMetricValues());
                metricResult.setGroupBy(projectMetric.getGroupBy());
                metricResult.setGrouping(projectMetric.getGrouping());
                metricResult.setGroupValue(projectMetric.getGroupValue());
            }
            else if(projectMetric.getTrending().equalsIgnoreCase("Yes")){
                metricResult.setTrendingMetricValues(projectMetric.getTrendingMetric());
                metricResult.setTrendCount(projectMetric.getTrendCount());
                metricResult.setTrendBy(projectMetric.getTrendBy());
                metricResult.setTrendingField(projectMetric.getTrendingField());
            }
            else {
                metricResult.setMetricValue(metricValue);
            }
            metricResult.setLastCalculatedDate(Instant.now());
            System.out.println("Metric results updated ** "+metricResult);
            return metricsRepository.save(metricResult);
        }
        else {
            MetricResults metricResult = new MetricResults();
            metricResult.setMetricName(metricName);
            metricResult.setToolType("Zephyr");
            //metricResult.setProjectName(projectMetric.getProjectName());
            //metricResult.setPageName(projectMetric.getPageName());
            //metricResult.setDashboardName(projectMetric.getDashboardName());
            metricResult.setDashboardId(projectMetric.getDashboardId());
            metricResult.setItemId(projectMetric.getItemId());
            metricResult.setLayerId(projectMetric.getLayerId());
            metricResult.setLayerName(projectMetric.getCategory());
            metricResult.setGrouping(projectMetric.getGrouping());
            metricResult.setTrending(projectMetric.getTrending());
            metricResult.setCustomFunction(projectMetric.getCustomFunction());

            if (projectMetric.getCustomFunction().equalsIgnoreCase("Yes")) {
                metricResult.setMetricValues(projectMetric.getMetricValues());
                metricResult.setCustomFunction(projectMetric.getCustomFunction());
                metricResult.setCustomFunctionName(projectMetric.getCustomFunctionName());
                if(projectMetric.getCustomParams()!=null){
                    metricResult.setCustomParams(projectMetric.getCustomParams());
                }
            }
            else if (projectMetric.getGrouping().equalsIgnoreCase("Yes")) {
                metricResult.setMetricValues(projectMetric.getMetricValues());
                metricResult.setGroupBy(projectMetric.getGroupBy());
                metricResult.setGrouping(projectMetric.getGrouping());
                metricResult.setGroupValue(projectMetric.getGroupValue());
            }
            else if(projectMetric.getTrending().equalsIgnoreCase("Yes")){
                metricResult.setTrendingMetricValues(projectMetric.getTrendingMetric());
                metricResult.setTrendCount(projectMetric.getTrendCount());
                metricResult.setTrendBy(projectMetric.getTrendBy());
                metricResult.setTrendingField(projectMetric.getTrendingField());
            }
            else {
                metricResult.setMetricValue(metricValue);
            }

            //metricResult.setCreatedDate(Instant.now());
            metricResult.setLastCalculatedDate(Instant.now());
            System.out.println("Metric results created ** "+metricResult);
            return metricsRepository.save(metricResult);
        }
    }

}
