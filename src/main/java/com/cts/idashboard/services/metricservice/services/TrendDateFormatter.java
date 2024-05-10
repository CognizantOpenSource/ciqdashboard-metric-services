package com.cts.idashboard.services.metricservice.services;

import com.cts.idashboard.services.metricservice.data.ProjectMetric;
import com.cts.idashboard.services.metricservice.data.TrendingMetric;
import com.fathzer.soft.javaluator.DoubleEvaluator;
import com.fathzer.soft.javaluator.StaticVariableSet;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class TrendDateFormatter {

    @Autowired
    MetricFunctions metricFunctions;

    public ProjectMetric trendDateFormatMethod(String metricName, ProjectMetric projectMetric, String classType) throws Exception {

        int calclulatedValue = 0;
        List<TrendingMetric> metricValues = new ArrayList<>();
        System.out.println("trending field :"+projectMetric.getTrendingField());

        if(projectMetric.getTrendingField().equalsIgnoreCase("months")) {
            int trendCount = projectMetric.getTrendCount() - 1;

            if(trendCount > 10){
                trendCount = 10;
            }

           for(int i=trendCount;i>=0;i--){
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                Date currentDate = new Date();
                Date currentEndDate = new Date();
                currentDate.setMonth(currentDate.getMonth() - i);
                currentEndDate.setMonth(currentEndDate.getMonth() - i);
                int lastDayOfMonth = getLastDayOf(currentEndDate.getMonth(), currentEndDate.getYear());
                currentDate.setDate(01);
                currentDate.setHours(00);
                currentDate.setMinutes(00);
                currentDate.setSeconds(00);
                currentEndDate.setDate(lastDayOfMonth);
                currentEndDate.setHours(23);
                currentEndDate.setMinutes(59);
                currentEndDate.setSeconds(59);
                String month_name = formatter.format(currentDate);
                calclulatedValue = calculateMetricValueforMonth(metricName, month_name, currentDate, currentEndDate, projectMetric,classType);
                System.out.println("calclulatedValue :: " + calclulatedValue);
                Date isoFormat = formatter.parse(month_name);
                metricValues.add( TrendingMetric.builder().month(isoFormat).result(calclulatedValue).build());
                System.out.println("Months: "+metricValues);
            }
        }

        if(projectMetric.getTrendingField().equalsIgnoreCase("days")) {
            int trendCount = projectMetric.getTrendCount() - 1;

            if(trendCount > 10){
                trendCount = 10;
            }

            for(int i=trendCount;i>=0;i--){
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                Date currentDate = new Date();
                Date currentEndDate = new Date();
                currentDate.setDate(currentDate.getDate() - i);
                currentEndDate.setDate(currentEndDate.getDate() - i);
                currentDate.setHours(00);
                currentDate.setMinutes(00);
                currentDate.setSeconds(00);
                currentEndDate.setHours(23);
                currentEndDate.setMinutes(59);
                currentEndDate.setSeconds(59);
                String month_name = formatter.format(currentDate);
                calclulatedValue = calculateMetricValueforMonth(metricName, month_name, currentDate, currentEndDate, projectMetric,classType);
                Date isoFormat = formatter.parse(month_name);
                metricValues.add( TrendingMetric.builder().month(isoFormat).result(calclulatedValue).build());
                System.out.println("Days: "+metricValues);
            }

        }

        if(projectMetric.getTrendingField().equalsIgnoreCase("weeks")) {
            int trendCount = projectMetric.getTrendCount() - 1;

            if(trendCount > 10){
                trendCount = 10;
            }

            for(int i=trendCount;i>=0;i--){
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                Date currentDate = new Date();
                Date currentEndDate = new Date();
                Integer weekCount = i*7;
                Integer weekcountforEndDate = weekCount-6;

                currentDate.setDate(currentDate.getDate() - weekCount);
                currentEndDate.setDate(currentEndDate.getDate() - weekcountforEndDate);
                currentDate.setHours(00);
                currentDate.setMinutes(00);
                currentDate.setSeconds(00);
                currentEndDate.setHours(23);
                currentEndDate.setMinutes(59);
                currentEndDate.setSeconds(59);
                System.out.println("startDate"+currentDate);
                System.out.println("EndDate"+currentEndDate);
                String month_name = formatter.format(currentDate);
                String month_name2 = formatter.format(currentEndDate);
                calclulatedValue = calculateMetricValueforMonth(metricName, month_name, currentDate, currentEndDate, projectMetric,classType);
                System.out.println("calclulatedValue :: " + calclulatedValue);
                System.out.println();
                Date isoFormat = formatter.parse(month_name);
                metricValues.add( TrendingMetric.builder().month(isoFormat).result(calclulatedValue).build());
                System.out.println("Weeks: "+metricValues);
            }
        }

        if(projectMetric.getTrendingField().equalsIgnoreCase("years")) {
            int trendCount = projectMetric.getTrendCount() - 1;

            if(trendCount > 10){
                trendCount = 10;
            }

            for(int i=trendCount;i>=0;i--){
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                Date currentDate = new Date();
                Date currentEndDate = new Date();
                currentDate.setYear(currentDate.getYear() - i);
                currentEndDate.setYear(currentEndDate.getYear() - i);
                currentDate.setMonth(00);
                currentDate.setDate(01);
                currentEndDate.setMonth(11);
                currentEndDate.setDate(31);
                currentDate.setHours(00);
                currentDate.setMinutes(00);
                currentDate.setSeconds(00);
                currentEndDate.setHours(23);
                currentEndDate.setMinutes(59);
                currentEndDate.setSeconds(59);
                String month_name = formatter.format(currentDate);
                calclulatedValue = calculateMetricValueforMonth(metricName, month_name, currentDate, currentEndDate, projectMetric,classType);
                System.out.println("calclulatedValue :: " + calclulatedValue);
                Date isoFormat = formatter.parse(month_name);
                metricValues.add( TrendingMetric.builder().month(isoFormat).result(calclulatedValue).build());
                System.out.println("Years: "+metricValues);
            }
        }

        projectMetric.setTrendingMetric(metricValues);
        return projectMetric;
    }


    public int calculateMetricValueforMonth(String metricName, String monthDate, Date startDate, Date endDate, ProjectMetric projectMetric, String classType) throws Exception {

        String formula = projectMetric.getFormula();
        Map<String, JSONObject> formulaParams = projectMetric.getFormulaParams();
        System.out.println("formula in method- " + formula);
        DoubleEvaluator eval = new DoubleEvaluator();
        StaticVariableSet<Double> variableSet = new StaticVariableSet<Double>();
        Double result = 0.0;

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
                        Long valKey = metricFunctions.count(classType, valueMap.entrySet(), projectMetric, "Yes", "No", startDate, endDate, null);
                        variableSet.set(map.getKey(), Double.valueOf(valKey));
                    } else if (valObj.getKey().equals("countAnd")) {
                        Long valKey = metricFunctions.countAnd(classType, valueMap.entrySet(), projectMetric, "Yes", "No", startDate, endDate, null);
                        variableSet.set(map.getKey(), Double.valueOf(valKey));
                    } else if (valObj.getKey().equals("sum")) {
                        Double valKey = metricFunctions.sum(classType, valueMap.entrySet(), projectMetric, "Yes", "No", startDate, endDate, null);
                        variableSet.set(map.getKey(), valKey);
                    } else if (valObj.getKey().equals("sumAnd")) {
                        Double valKey = metricFunctions.sumAnd(classType, valueMap.entrySet(), projectMetric, "Yes", "No", startDate, endDate, null);
                        variableSet.set(map.getKey(), valKey);
                    } else if (valObj.getKey().equals("avg")) {
                        Double valKey = metricFunctions.avg(classType, valueMap.entrySet(), projectMetric, "Yes", "No", startDate, endDate, null);
                        variableSet.set(map.getKey(), valKey);
                    } else if (valObj.getKey().equals("avgAnd")) {
                        Double valKey = metricFunctions.avgAnd(classType, valueMap.entrySet(), projectMetric, "Yes", "No", startDate, endDate, null);
                        variableSet.set(map.getKey(), valKey);
                    } else if (valObj.getKey().equals("sumISO")) {
                        Double valKey = metricFunctions.sumIS0(classType, valueMap.entrySet(), projectMetric, "Yes", "No", startDate, endDate, null);
                        variableSet.set(map.getKey(), valKey);
                    } else if (valObj.getKey().equals("sumISOAnd")) {
                        Double valKey = metricFunctions.sumIS0And(classType, valueMap.entrySet(), projectMetric, "Yes", "No", startDate, endDate, null);
                        variableSet.set(map.getKey(), valKey);
                    } else if (valObj.getKey().equals("sumISODiff")) {
                        Double valKey = metricFunctions.sumIS0Diff(classType, valueMap.entrySet(), projectMetric, "Yes", "No", startDate, endDate, null);
                        variableSet.set(map.getKey(), valKey);
                    } else if (valObj.getKey().equals("sumISODiffAnd")) {
                        Double valKey = metricFunctions.sumIS0DiffAnd(classType, valueMap.entrySet(), projectMetric, "Yes", "No", startDate, endDate, null);
                        variableSet.set(map.getKey(), valKey);
                    } else if (valObj.getKey().equals("max")) {
                        Long valKey = metricFunctions.max(classType, valueMap.entrySet(), projectMetric, "Yes", "No", startDate, endDate, null);
                        variableSet.set(map.getKey(), valKey.doubleValue());
                    } else if (valObj.getKey().equals("maxAnd")) {
                        Long valKey = metricFunctions.maxAnd(classType, valueMap.entrySet(), projectMetric, "Yes", "No", startDate, endDate, null);
                        variableSet.set(map.getKey(), valKey.doubleValue());
                    } else if (valObj.getKey().equals("min")) {
                        Long valKey = metricFunctions.min(classType, valueMap.entrySet(), projectMetric, "Yes", "No", startDate, endDate, null);
                        variableSet.set(map.getKey(), valKey.doubleValue());
                    } else if (valObj.getKey().equals("minAnd")) {
                        Long valKey = metricFunctions.minAnd(classType, valueMap.entrySet(), projectMetric, "Yes", "No", startDate, endDate, null);
                        variableSet.set(map.getKey(), valKey.doubleValue());
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

    public static int getLastDayOf(int month, int year) {
        switch (month) {
            case Calendar.APRIL:
            case Calendar.JUNE:
            case Calendar.SEPTEMBER:
            case Calendar.NOVEMBER:
                return 30;
            case Calendar.FEBRUARY:
                if (year % 4 == 0) {
                    return 29;
                }
                return 28;
            default:
                return 31;
        }
    }

}
