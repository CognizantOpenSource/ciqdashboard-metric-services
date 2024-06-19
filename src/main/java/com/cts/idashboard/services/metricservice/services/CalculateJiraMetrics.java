package com.cts.idashboard.services.metricservice.services;

import com.cts.idashboard.services.metricservice.data.*;
import com.cts.idashboard.services.metricservice.repos.*;
import com.fathzer.soft.javaluator.DoubleEvaluator;
import com.fathzer.soft.javaluator.StaticVariableSet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Component
public class CalculateJiraMetrics {

    @Autowired
    MetricConfigRepository metricRepo;

    @Autowired
    JiraIssueRepository jiraIssueRepository;

    @Autowired
    MetricsRepository metricsRepository;

    @Autowired
    DashboardRepository dashboardRepository;

    @Autowired
    SchedulerRunsRepository schedulerRunsRepository;

    @Autowired
    MongoOperations mongoOperation;

    @Autowired
    MetricFunctions metricFunctions;

    @Autowired
    CustomFunctions customFunctions;

    @Autowired
    TrendDateFormatter trendDateFormatter;


    public boolean isCalculationRequired(String metricName, ProjectMetric projectMetric) {
        //System.out.println("metricName - " + metricName + " - dashboardName - " + projectMetric.getDashboardName() + " - itemId - " + projectMetric.getItemId());
        System.out.println("metricName - " + metricName + " - dashboardId - " + projectMetric.getDashboardId() + " - itemId - " + projectMetric.getItemId());

        //Optional<MetricResults> metricResults = metricsRepository.findByMetricNameAndDashboardNameAndPageNameAndItemId(metricName, projectMetric.getDashboardName(), projectMetric.getPageName(), projectMetric.getItemId());
        //Optional<MetricResults> metricResults = metricsRepository.findByMetricNameAndDashboardNameAndItemId(metricName, projectMetric.getDashboardName(), projectMetric.getItemId());

        Optional<MetricResults> metricResults = metricsRepository.findByMetricNameAndDashboardIdAndItemId(metricName, projectMetric.getDashboardId(), projectMetric.getItemId());
        Optional<CollectorRunScheduler> collectorRunScheduler = schedulerRunsRepository.findByToolNameIgnoreCase("Jira");
        boolean requireCalculation = false;
        if (collectorRunScheduler.isPresent() && metricResults.isPresent()) {
            Instant lastJiraCollectionUpdated = collectorRunScheduler.get().getLastUpdatedDate();
            Instant lastMetricCalculated = metricResults.get().getLastCalculatedDate();

            requireCalculation = lastJiraCollectionUpdated.isAfter(lastMetricCalculated);

        } else if (!collectorRunScheduler.isPresent()) {
            requireCalculation = false;

        } else if (!metricResults.isPresent()) {
            requireCalculation = true;
        }
        return requireCalculation;
    }

    public MetricResults calculateJiraMetrics(String metricName, ProjectMetric projectMetric,String JiraIssue) throws Exception {

        int calculatedValue = 0;
        MetricResults metrics = null;
        JSONArray metricValues = new JSONArray();

        if(projectMetric.getCustomFunction()!=null && projectMetric.getCustomFunction().equalsIgnoreCase("Yes")) {
            System.out.println("*** Custom function calculation ***");
            projectMetric = customFunctions.executeCustomFunction(projectMetric, JiraIssue);
        }
        else if(projectMetric.getTrending()!=null && projectMetric.getTrending().equalsIgnoreCase("Yes")){
            System.out.println("*** TrendBy calculation ***");
            System.out.println("TrendBy value : "+projectMetric.getTrendBy());
            projectMetric = trendDateFormatter.trendDateFormatMethod(metricName,projectMetric,JiraIssue);
        }
        else if (projectMetric.getGrouping()!=null && projectMetric.getGrouping().equalsIgnoreCase("Yes")) {
            System.out.println("*** Grouping calculation *** ");
            System.out.println("GroupBy value : "+projectMetric.getGroupBy());
            List<String> distinctData = null;
            if (!(projectMetric.getGroupValue().equalsIgnoreCase("All"))) {
                distinctData = Arrays.asList(projectMetric.getGroupValue().split(","));
            } else {
                distinctData =mongoOperation.findDistinct(projectMetric.getGroupBy(), JiraIssue.class, String.class);
            }
            System.out.println("Grouping values ** " + distinctData);

            for (String distinctValue : distinctData) {
                System.out.println("Calculation for the group value : " + distinctValue);
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
            isGrouping="Yes";
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
                        Long valKey = metricFunctions.count("JiraIssue", valueMap.entrySet(), projectMetric, "No", isGrouping, null, null, distinctValue);
                        variableSet.set(map.getKey(), Double.valueOf(valKey));
                    } else if (valObj.getKey().equals("countAnd")) {
                        Long valKey = metricFunctions.countAnd("JiraIssue", valueMap.entrySet(), projectMetric, "No", isGrouping, null, null, distinctValue);
                        variableSet.set(map.getKey(), Double.valueOf(valKey));
                    } else if (valObj.getKey().equals("sum")) {
                        Double valKey = metricFunctions.sum("JiraIssue", valueMap.entrySet(), projectMetric, "No", isGrouping, null, null, distinctValue);
                        variableSet.set(map.getKey(), valKey);
                    } else if (valObj.getKey().equals("sumAnd")) {
                        Double valKey = metricFunctions.sumAnd("JiraIssue", valueMap.entrySet(), projectMetric, "No", isGrouping, null, null, distinctValue);
                        variableSet.set(map.getKey(), valKey);
                    } else if (valObj.getKey().equals("avg")) {
                        Double valKey = metricFunctions.avg("JiraIssue", valueMap.entrySet(), projectMetric, "No", isGrouping, null, null, distinctValue);
                        variableSet.set(map.getKey(), valKey);
                    } else if (valObj.getKey().equals("avgAnd")) {
                        Double valKey = metricFunctions.avgAnd("JiraIssue", valueMap.entrySet(), projectMetric, "No", isGrouping, null, null, distinctValue);
                        variableSet.set(map.getKey(), valKey);
                    } else if (valObj.getKey().equals("sumISO")) {
                        Double valKey = metricFunctions.sumIS0("JiraIssue", valueMap.entrySet(), projectMetric, "No", isGrouping, null, null, distinctValue);
                        variableSet.set(map.getKey(), valKey);
                    } else if (valObj.getKey().equals("sumISOAnd")) {
                        Double valKey = metricFunctions.sumIS0And("JiraIssue", valueMap.entrySet(), projectMetric, "No", isGrouping, null, null, distinctValue);
                        variableSet.set(map.getKey(), valKey);
                    } else if (valObj.getKey().equals("sumISODiff")) {
                        Double valKey = metricFunctions.sumIS0Diff("JiraIssue", valueMap.entrySet(), projectMetric, "No", isGrouping, null, null, distinctValue);
                        variableSet.set(map.getKey(), valKey);
                    } else if (valObj.getKey().equals("sumISODiffAnd")) {
                        Double valKey = metricFunctions.sumIS0DiffAnd("JiraIssue", valueMap.entrySet(), projectMetric, "No", isGrouping, null, null, distinctValue);
                        variableSet.set(map.getKey(), valKey);
                    } else if (valObj.getKey().equals("max")) {
                        Long valKey = metricFunctions.max("JiraIssue", valueMap.entrySet(), projectMetric, "No", isGrouping, null, null, distinctValue);
                        variableSet.set(map.getKey(), valKey.doubleValue());
                    } else if (valObj.getKey().equals("maxAnd")) {
                        Long valKey = metricFunctions.maxAnd("JiraIssue", valueMap.entrySet(), projectMetric, "No", isGrouping, null, null, distinctValue);
                        variableSet.set(map.getKey(), valKey.doubleValue());
                    } else if (valObj.getKey().equals("min")) {
                        Long valKey = metricFunctions.min("JiraIssue", valueMap.entrySet(), projectMetric, "No", isGrouping, null, null, distinctValue);
                        variableSet.set(map.getKey(), valKey.doubleValue());
                    } else if (valObj.getKey().equals("minAnd")) {
                        Long valKey = metricFunctions.minAnd("JiraIssue", valueMap.entrySet(), projectMetric, "No", isGrouping, null, null, distinctValue);
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

        if (metricResults.isPresent()) {
            System.out.println("Metric results "+metricResults);
            MetricResults metricResult = metricResults.get();
            metricResult.setMetricName(metricName);
            metricResult.setToolType("Jira");
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

            if (projectMetric.getCustomFunction()!=null && projectMetric.getCustomFunction().equalsIgnoreCase("Yes")) {
                metricResult.setMetricValues(projectMetric.getMetricValues());
                metricResult.setCustomFunction(projectMetric.getCustomFunction());
                metricResult.setCustomFunctionName(projectMetric.getCustomFunctionName());
                if(projectMetric.getCustomParams()!=null){
                    metricResult.setCustomParams(projectMetric.getCustomParams());
                }
            }
            else if (projectMetric.getGrouping()!=null && projectMetric.getGrouping().equalsIgnoreCase("Yes")) {
                metricResult.setMetricValues(projectMetric.getMetricValues());
                metricResult.setGroupBy(projectMetric.getGroupBy());
                metricResult.setGrouping(projectMetric.getGrouping());
                metricResult.setGroupValue(projectMetric.getGroupValue());
            }
            else if(projectMetric.getTrending()!=null && projectMetric.getTrending().equalsIgnoreCase("Yes")){
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
            metricResult.setToolType("Jira");
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

            if (projectMetric.getCustomFunction()!=null && projectMetric.getCustomFunction().equalsIgnoreCase("Yes")) {
                metricResult.setMetricValues(projectMetric.getMetricValues());
                metricResult.setCustomFunction(projectMetric.getCustomFunction());
                metricResult.setCustomFunctionName(projectMetric.getCustomFunctionName());
                if(projectMetric.getCustomParams()!=null){
                    metricResult.setCustomParams(projectMetric.getCustomParams());
                }
            }

            else if (projectMetric.getGrouping()!=null && projectMetric.getGrouping().equalsIgnoreCase("Yes")) {
                metricResult.setMetricValues(projectMetric.getMetricValues());
                metricResult.setGroupBy(projectMetric.getGroupBy());
                metricResult.setGrouping(projectMetric.getGrouping());
                metricResult.setGroupValue(projectMetric.getGroupValue());
            }

            else if(projectMetric.getTrending()!=null && projectMetric.getTrending().equalsIgnoreCase("Yes")){
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


//    public MetricResults calculateJiraMetrics(String metricName, ProjectMetric projectMetric) throws Exception {
//
//        Optional<MetricConfig> metricConfig = metricRepo.findByMetricName(metricName);
//        if (metricConfig.isPresent()) {
//            String formula = metricConfig.get().getFormula();
//
//            int statusName = (int) jiraIssueRepository.countByStatusNameAndIssueTypeName("Rejected", "Bug");
//            int issueType = (int) jiraIssueRepository.countByIssueTypeName("Bug");
//            Expression expression = new ExpressionBuilder(formula)
//                    .variables("StatusName", "IssueTypeName")
//                    .build()
//                    .setVariable("StatusName", statusName)
//                    .setVariable("IssueTypeName", issueType);
//            int result = (int) expression.evaluate();
//            System.out.println("result after eval -- " + result);
//            MetricResults metrics = saveMetricResult(metricName, result, projectMetric);
//            return metrics;
//
//        } else {
//            throw new Exception("Metric not Found !! ");
//        }
//
//    }

    /*public MetricResults saveMetricResult(String metricName, int rejectionRate, ProjectMetric projectMetric) {
        Optional<MetricResults> metricResults = metricsRepository.findByMetricNameAndDashboardNameAndPageNameAndItemId(metricName, projectMetric.getDashboardName(), projectMetric.getPageName(), projectMetric.getItemId());
        if (metricResults.isPresent()) {
            MetricResults metricResult = metricResults.get();
            metricResult.setMetricName(metricName);
            metricResult.setMetricValue(rejectionRate);
            metricResult.setToolType("Jira");
            metricResult.setProjectName(projectMetric.getProjectName());
            metricResult.setItemId(projectMetric.getItemId());
            metricResult.setPageName(projectMetric.getPageName());
            metricResult.setDashboardName(projectMetric.getDashboardName());
            metricResult.setLastCalculatedDate(Instant.now());
            return metricsRepository.save(metricResult);
        } else {
            MetricResults metricResult = new MetricResults();
            metricResult.setMetricName(metricName);
            metricResult.setMetricValue(rejectionRate);
            metricResult.setToolType("Jira");
            metricResult.setProjectName(projectMetric.getProjectName());
            metricResult.setItemId(projectMetric.getItemId());
            metricResult.setPageName(projectMetric.getPageName());
            metricResult.setDashboardName(projectMetric.getDashboardName());
            metricResult.setLastCalculatedDate(Instant.now());
            return metricsRepository.save(metricResult);

        }
    }*/
}

