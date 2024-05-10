package com.cts.idashboard.services.metricservice.services;


import com.cts.idashboard.services.metricservice.data.*;
import com.cts.idashboard.services.metricservice.repos.SourceToolsRepository;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Component
public class CustomFunctions {

    @Autowired
    MongoOperations mongoOperation;

    @Autowired
    SourceToolsRepository sourceToolsRepository;

    public ProjectMetric executeCustomFunction(ProjectMetric projectMetric, String className) throws Exception {

        System.out.println("** Custom function - "+projectMetric.getCustomFunctionName()+" **");

        if(projectMetric.getCustomFunctionName().equalsIgnoreCase("jira_defect_ageing")){
            projectMetric = jiraDefectAgeing(projectMetric);
        }
        else if(projectMetric.getCustomFunctionName().equalsIgnoreCase("jira_defect_ageing_by_drops")){
            projectMetric = jiraDefectAgeingByDrops(projectMetric);
        }
        else if(projectMetric.getCustomFunctionName().equalsIgnoreCase("jira_defect_ageing_per_dev")){
            projectMetric = jiraDefectAgeingPerDeveloper(projectMetric);
        }
        else if(projectMetric.getCustomFunctionName().equalsIgnoreCase("jira_defect_ageing_by_drops_all_status")){
            projectMetric = jiraDefectAgeingByDropsAllStatus(projectMetric);
        }
        else if(projectMetric.getCustomFunctionName().equalsIgnoreCase("jira_defect_ageing_by_priority")){
            projectMetric = jiraDefectAgeingByPriority(projectMetric);
        }
        else if(projectMetric.getCustomFunctionName().equalsIgnoreCase("jira_open_closed_defect_count")){
            projectMetric = jiraOpenClosedDefectsCount(projectMetric);
        }
        else if(projectMetric.getCustomFunctionName().equalsIgnoreCase("custom_stc_manual")){
            projectMetric = customStcManual(projectMetric);
        }
        else if(projectMetric.getCustomFunctionName().equalsIgnoreCase("custom_table_stc")) {
            projectMetric = customTableStc(projectMetric);
        }
        return projectMetric;
    }

    public ProjectMetric jiraDefectAgeing(ProjectMetric projectMetric) throws Exception{

        String[] timePeriods = {"Last 07 days","Last 15 days","Last 30 days","Last 45 days","Last 60 days"};
        JSONArray metricValues = new JSONArray();
        Date toDate = Date.from(Instant.now());
        Date fromDate = Date.from(Instant.now());
        JSONObject dateList = new JSONObject();

        for(String period: timePeriods) {
            if (period.equalsIgnoreCase("last 07 days")) {
                fromDate = Date.from(Instant.now().minus(7, ChronoUnit.DAYS));
            }
            else if (period.equalsIgnoreCase("last 15 days")) {
                toDate = fromDate;
                fromDate = Date.from(Instant.now().minus(15, ChronoUnit.DAYS));
            }
            else if (period.equalsIgnoreCase("last 30 days")) {
                toDate = fromDate;
                fromDate = Date.from(Instant.now().minus(30, ChronoUnit.DAYS));
            }
            else if (period.equalsIgnoreCase("last 45 days")) {
                toDate = fromDate;
                fromDate = Date.from(Instant.now().minus(45, ChronoUnit.DAYS));
            }
            else if (period.equalsIgnoreCase("last 60 days")) {
                toDate = fromDate;
                fromDate = Date.from(Instant.now().minus(60, ChronoUnit.DAYS));
            }
            List<Date> date = new ArrayList<>();
            date.add(fromDate);
            date.add(toDate);
            dateList.put(period,date);
        }

        for(String period: timePeriods) {
            System.out.println("*** TIME PERIOD : "+period+" ***");
            List<String> distinctPriorityValues = mongoOperation.findDistinct("priority", JiraIssue.class, String.class);
            JSONArray series =new JSONArray();

            for(String priority : distinctPriorityValues){
                List<Date> datesPeriodList = (List<Date>) dateList.get(period);
                fromDate = datesPeriodList.get(0);
                toDate = datesPeriodList.get(1);

                Query query = new Query();
                Criteria criteria = null;

                // Applying date filter
                criteria = Criteria.where("created").gte(fromDate).lte(toDate);
                if(criteria !=null){
                    query.addCriteria(criteria);
                }

                // Applying defect ageing filter
                criteria = Criteria.where("statusCategoryName").ne("Done");
                if(criteria !=null){
                    query.addCriteria(criteria);
                }

                // Applying priority filter
                criteria = Criteria.where("priority").is(priority);
                if(criteria !=null){
                    query.addCriteria(criteria);
                }

                // Applying filter conditions
                if(projectMetric.getCustomParams()!=null){
                    Map<String, Object> valueSet = (Map<String, Object>) projectMetric.getCustomParams();
                    for (Map.Entry<String, Object> val : valueSet.entrySet()) {
                        if(!(((val.getValue()).getClass())== String.class)) {
                            criteria = Criteria.where(val.getKey()).is(val.getValue());
                            query.addCriteria(criteria);
                        }
                        else {
                            List<String> ls = Arrays.asList(String.valueOf(val.getValue()).split(";"));
                            System.out.println("List data - " + ls);
                            criteria = Criteria.where(val.getKey()).in(ls);
                            query.addCriteria(criteria);
                        }
                    }
                }

                JSONObject evaluatedResult1 = new JSONObject();
                long value = 0L;
                value = Math.round(defectAgeingFormula(query, JiraIssue.class));

                System.out.println("Defect ageing value for priority "+priority+" with time period "+period+" is : "+value);

                evaluatedResult1.put("name", priority);
                evaluatedResult1.put("value", value);
                evaluatedResult1.put("label", priority);
                evaluatedResult1.put("link", null);
                evaluatedResult1.put("children", null);
                evaluatedResult1.put("series", null);

                series.add(evaluatedResult1);
            }

            JSONObject evaluatedResult = new JSONObject();
            evaluatedResult.put("name", period);
            evaluatedResult.put("value", 0);
            evaluatedResult.put("label", period);
            evaluatedResult.put("link", null);
            evaluatedResult.put("children", null);
            evaluatedResult.put("series", series);

            metricValues.add(evaluatedResult);
        }
        projectMetric.setMetricValues(metricValues);
        return projectMetric;
    }

    public ProjectMetric jiraDefectAgeingByDrops(ProjectMetric projectMetric) throws Exception{

        String[] timePeriods = {"Last 07 days","Last 15 days","Last 30 days","Last 45 days","Last 60 days"};
        JSONArray metricValues = new JSONArray();
        Date toDate = Date.from(Instant.now());
        Date fromDate = Date.from(Instant.now());
        JSONObject dateList = new JSONObject();

        for(String period: timePeriods) {
            if (period.equalsIgnoreCase("last 07 days")) {
                fromDate = Date.from(Instant.now().minus(7, ChronoUnit.DAYS));
            }
            else if (period.equalsIgnoreCase("last 15 days")) {
                toDate = fromDate;
                fromDate = Date.from(Instant.now().minus(15, ChronoUnit.DAYS));
            }
            else if (period.equalsIgnoreCase("last 30 days")) {
                toDate = fromDate;
                fromDate = Date.from(Instant.now().minus(30, ChronoUnit.DAYS));
            }
            else if (period.equalsIgnoreCase("last 45 days")) {
                toDate = fromDate;
                fromDate = Date.from(Instant.now().minus(45, ChronoUnit.DAYS));
            }
            else if (period.equalsIgnoreCase("last 60 days")) {
                toDate = fromDate;
                fromDate = Date.from(Instant.now().minus(60, ChronoUnit.DAYS));
            }
            List<Date> date = new ArrayList<>();
            date.add(fromDate);
            date.add(toDate);
            dateList.put(period,date);
        }

        for(String period: timePeriods) {
            JSONArray series =new JSONArray();
            System.out.println("*** TIME PERIOD : "+period+" ***");
            List<String> distinctPriorityValues = mongoOperation.findDistinct("priority", JiraIssue.class, String.class);

            for(String priority : distinctPriorityValues){
                List<Date> datesPeriodList = (List<Date>) dateList.get(period);
                fromDate = datesPeriodList.get(0);
                toDate = datesPeriodList.get(1);

                Query query = new Query();
                Criteria criteria = null;

                // Applying date filter
                criteria = Criteria.where("created").gte(fromDate).lte(toDate);
                if(criteria !=null){
                    query.addCriteria(criteria);
                }

                // Applying filter conditions
                if(projectMetric.getCustomParams()!=null){
                    Map<String, Object> valueSet = (Map<String, Object>) projectMetric.getCustomParams();
                    for (Map.Entry<String, Object> val : valueSet.entrySet()) {
                        if(!(((val.getValue()).getClass())== String.class)) {
                            criteria = Criteria.where(val.getKey()).is(val.getValue());
                            query.addCriteria(criteria);
                        }
                        else {
                            List<String> ls = Arrays.asList(String.valueOf(val.getValue()).split(";"));
                            System.out.println("List data - " + ls);
                            criteria = Criteria.where(val.getKey()).in(ls);
                            query.addCriteria(criteria);
                        }
                    }
                }

                // Applying defect ageing filter
                criteria = Criteria.where("statusCategoryName").ne("Done");
                if(criteria !=null){
                    query.addCriteria(criteria);
                }

                // Applying priority filter
                criteria = Criteria.where("priority").is(priority);
                if(criteria !=null){
                    query.addCriteria(criteria);
                }

                JSONObject evaluatedResult1 = new JSONObject();
                long value = 0L;
                value = Math.round(defectAgeingFormula(query, JiraIssue.class));

                System.out.println("Defect ageing value for priority "+priority+" with time period "+period+" is : "+value);

                evaluatedResult1.put("name", priority);
                evaluatedResult1.put("value", value);
                evaluatedResult1.put("label", priority);
                evaluatedResult1.put("link", null);
                evaluatedResult1.put("children", null);
                evaluatedResult1.put("series", null);

                series.add(evaluatedResult1);
            }

            JSONObject evaluatedResult = new JSONObject();
            evaluatedResult.put("name", period);
            evaluatedResult.put("value", 0);
            evaluatedResult.put("label", period);
            evaluatedResult.put("link", null);
            evaluatedResult.put("children", null);
            evaluatedResult.put("series", series);

            metricValues.add(evaluatedResult);
        }
        projectMetric.setMetricValues(metricValues);
        return projectMetric;
    }

    public ProjectMetric jiraDefectAgeingPerDeveloper(ProjectMetric projectMetric) throws Exception{

        String[] timePeriods = {"a.Less than 1 day","b.2 to 3 days","c.More than 5 days"};
        JSONArray metricValues = new JSONArray();
        Date toDate = Date.from(Instant.now());
        Date fromDate = Date.from(Instant.now());
        JSONObject dateList = new JSONObject();

        for(String period: timePeriods) {
            if (period.equalsIgnoreCase("a.Less than 1 day")) {
                fromDate = Date.from(Instant.now().minus(1, ChronoUnit.DAYS));
            }
            else if (period.equalsIgnoreCase("b.2 to 3 days")) {
                toDate = Date.from(Instant.now().minus(2, ChronoUnit.DAYS));
                fromDate = Date.from(Instant.now().minus(3, ChronoUnit.DAYS));
            }
            else if (period.equalsIgnoreCase("c.More than 5 days")) {
                fromDate = new Date(Long.MIN_VALUE);
                toDate = Date.from(Instant.now().minus(5, ChronoUnit.DAYS));
            }
            List<Date> date = new ArrayList<>();
            date.add(fromDate);
            date.add(toDate);
            dateList.put(period,date);
        }

        for(String period: timePeriods) {
            JSONArray series =new JSONArray();
            System.out.println("*** TIME PERIOD : "+period+" ***");
            Query query1 = new Query();

            List<Date> datesPeriodList = (List<Date>) dateList.get(period);
            fromDate = datesPeriodList.get(0);
            toDate = datesPeriodList.get(1);

            Criteria criteria1 = null;

            // Applying project filter
            criteria1 = Criteria.where("projectName").is("CMS");
            if(criteria1 !=null){
                query1.addCriteria(criteria1);
            }

            // Applying filter conditions
            if(projectMetric.getCustomParams()!=null){
                Map<String, Object> valueSet = (Map<String, Object>) projectMetric.getCustomParams();
                for (Map.Entry<String, Object> val : valueSet.entrySet()) {
                    if(!(((val.getValue()).getClass())== String.class)) {
                        criteria1 = Criteria.where(val.getKey()).is(val.getValue());
                        query1.addCriteria(criteria1);
                    }
                    else {
                        List<String> ls = Arrays.asList(String.valueOf(val.getValue()).split(";"));
                        System.out.println("List data - " + ls);
                        criteria1 = Criteria.where(val.getKey()).in(ls);
                        query1.addCriteria(criteria1);
                    }
                }
            }

            // Applying defect ageing filter
            criteria1 = Criteria.where("statusCategoryName").ne("Done");
            if(criteria1 !=null){
                query1.addCriteria(criteria1);
            }

            List<String> distinctAssigneeNames = mongoOperation.findDistinct(query1,"assigneeName", JiraIssue.class, String.class);

            // Applying date filter
            criteria1 = Criteria.where("created").gte(fromDate).lte(toDate);
            if(criteria1 !=null){
                query1.addCriteria(criteria1);
            }

            // Applying defect ageing filter
            criteria1 = Criteria.where("assigneeName").exists(true);
            if(criteria1 !=null){
                query1.addCriteria(criteria1);
            }

            System.out.println("Query1 -"+query1);
            long val1 = 0;

            val1 = mongoOperation.count(query1,JiraIssue.class);

            for(String assigneeName : distinctAssigneeNames){

                Query query = new Query();
                Criteria criteria = null;

                // Applying date filter
                criteria = Criteria.where("created").gte(fromDate).lte(toDate);
                if(criteria !=null){
                    query.addCriteria(criteria);
                }

                // Applying project filter
                criteria = Criteria.where("projectName").is("CMS");
                if(criteria !=null){
                    query.addCriteria(criteria);
                }

                // Applying filter conditions
                if(projectMetric.getCustomParams()!=null){
                    Map<String, Object> valueSet = (Map<String, Object>) projectMetric.getCustomParams();
                    for (Map.Entry<String, Object> val : valueSet.entrySet()) {
                        if(!(((val.getValue()).getClass())== String.class)) {
                            criteria = Criteria.where(val.getKey()).is(val.getValue());
                            query.addCriteria(criteria);
                        }
                        else {
                            List<String> ls = Arrays.asList(String.valueOf(val.getValue()).split(";"));
                            System.out.println("List data - " + ls);
                            criteria = Criteria.where(val.getKey()).in(ls);
                            query.addCriteria(criteria);
                        }
                    }
                }

                // Applying defect ageing filter
                criteria = Criteria.where("statusCategoryName").ne("Done");
                if(criteria !=null){
                    query.addCriteria(criteria);
                }

                // Applying assignee filter
                criteria = Criteria.where("assigneeName").is(assigneeName);
                if(criteria !=null){
                    query.addCriteria(criteria);
                }

                JSONObject evaluatedResult1 = new JSONObject();
                long value = 0;
                value = Math.round(defectAgeingFormula(query, JiraIssue.class));

                System.out.println("Defect ageing value for assigneeName "+assigneeName+" with time period "+period+" is : "+value);

                evaluatedResult1.put("name", assigneeName);
                evaluatedResult1.put("value", value);
                evaluatedResult1.put("label", assigneeName);
                evaluatedResult1.put("link", null);
                evaluatedResult1.put("children", null);
                evaluatedResult1.put("series", null);

                series.add(evaluatedResult1);
            }

            JSONObject evaluatedResult = new JSONObject();
            evaluatedResult.put("name", period);
            evaluatedResult.put("value", val1);
            evaluatedResult.put("label", period);
            evaluatedResult.put("link", null);
            evaluatedResult.put("children", null);
            evaluatedResult.put("series", series);

            metricValues.add(evaluatedResult);
        }
        projectMetric.setMetricValues(metricValues);
        return projectMetric;
    }

    public ProjectMetric jiraDefectAgeingByDropsAllStatus(ProjectMetric projectMetric) throws Exception{

        JSONArray metricValues = new JSONArray();
        HashMap<String,HashMap<String,Integer>> finalData = new HashMap<String, HashMap<String,Integer>>();
        List<String> distinctPriorityValues = mongoOperation.findDistinct("priority", JiraIssue.class, String.class);

        for(String priority : distinctPriorityValues) {
            System.out.println("Priority - "+ priority);
            Criteria criteria1 = null;
            Criteria criteria2 = null;

            if (criteria1 == null) {
                criteria1 = Criteria.where("statusCategoryName").is("Done");
            } else {
                criteria1.and("statusCategoryName").is("Done");
            }

            if (criteria2 == null) {
                criteria2 = Criteria.where("statusCategoryName").in("In Progress", "To Do");
            } else {
                criteria2.and("statusCategoryName").in("In Progress","To Do");
            }

            if (criteria1 == null) {
                criteria1 = Criteria.where("priority").is(priority);
            } else {
                criteria1.and("priority").is(priority);
            }

            if (criteria2 == null) {
                criteria2 = Criteria.where("priority").is(priority);
            } else {
                criteria2.and("priority").is(priority);
            }

            // Applying filter conditions
            if(projectMetric.getCustomParams()!=null){
                Map<String, Object> valueSet = (Map<String, Object>) projectMetric.getCustomParams();
                for (Map.Entry<String, Object> val : valueSet.entrySet()) {
                    if(!(((val.getValue()).getClass())== String.class)) {
                        if(criteria1==null) {
                            criteria1 = Criteria.where(val.getKey()).is(val.getValue());
                        }
                        else{
                            criteria1.and(val.getKey()).is(val.getValue());
                        }
                        if(criteria2==null) {
                            criteria2 = Criteria.where(val.getKey()).is(val.getValue());
                        }
                        else{
                            criteria2.and(val.getKey()).is(val.getValue());
                        }
                    }
                    else {
                        List<String> ls = Arrays.asList(String.valueOf(val.getValue()).split(";"));
                        System.out.println("List data - " + ls);
                        if(criteria1==null) {
                            criteria1 = Criteria.where(val.getKey()).in(ls);
                        }
                        else{
                            criteria1.and(val.getKey()).in(ls);
                        }
                        if(criteria2==null) {
                            criteria2 =Criteria.where(val.getKey()).in(ls);
                        }
                        else{
                            criteria2.and(val.getKey()).in(ls);
                        }
                    }
                }
            }

            Date currentDate = Date.from(Instant.now());
            List<HashMap> list1 = null;
            List<HashMap> list2 = null;
            Class collectionClass = null;
            collectionClass = Class.forName("com.cts.idashboard.services.metricservice.data." + "JiraIssue");
            HashMap<String,Integer> data = new HashMap<>();
            data.put("a.Under 5 days",0);
            data.put("b.Under 10 days",0);
            data.put("c.Under 15 days",0);
            data.put("d.Under 20 days",0);
            data.put("e.Greater than 20 days",0);

            try {
                list1 = mongoOperation.aggregate(newAggregation(collectionClass,
                                match(criteria1),
                                project("resolutiondate", "created").andExpression("toInt((resolutionDate-created)/(3600*1000*24))").as("diff"),
                                group("diff").count().as("res1")),
                        HashMap.class).getMappedResults();

                list2 = mongoOperation.aggregate(newAggregation(collectionClass,
                                match(criteria2),
                                project("created").andExpression("toInt(([0]-created)/(3600*1000*24))", currentDate).as("diff"),
                                group("diff").count().as("res1")),

                        HashMap.class).getMappedResults();
            }
            catch(Exception e){
                list1 =null;
                list2 = null;
            }

            System.out.println("List1 - "+list1);
            System.out.println("List2 - "+list2);

            if(list1!=null) {
                list1.stream().filter((a) -> a.get("_id")!=null && (int) a.get("_id") <= 5).forEach(b -> data.put("a.Under 5 days", data.get("a.Under 5 days") + (int) b.get("res1")));
                list1.stream().filter((a) -> a.get("_id")!=null && (int) a.get("_id") > 5 && (int) a.get("_id") <= 10).forEach(b -> data.put("b.Under 10 days", data.get("b.Under 10 days") + (int) b.get("res1")));
                list1.stream().filter((a) -> a.get("_id")!=null && (int) a.get("_id") > 10 && (int) a.get("_id") <= 15).forEach(b -> data.put("c.Under 15 days", data.get("c.Under 15 days") + (int) b.get("res1")));
                list1.stream().filter((a) -> a.get("_id")!=null && (int) a.get("_id") > 15 && (int) a.get("_id") <= 20).forEach(b -> data.put("d.Under 20 days", data.get("d.Under 20 days") + (int) b.get("res1")));
                list1.stream().filter((a) -> a.get("_id")!=null && (int) a.get("_id") > 20).forEach(b -> data.put("e.Greater than 20 days", data.get("e.Greater than 20 days") + (int) b.get("res1")));
            }

            if(list2!=null) {
                list2.stream().filter((a) -> a.get("_id")!=null && (int) a.get("_id") <= 5).forEach(b -> data.put("a.Under 5 days", data.get("a.Under 5 days") + (int) b.get("res1")));
                list2.stream().filter((a) -> a.get("_id")!=null && (int) a.get("_id") > 5 && (int) a.get("_id") <= 10).forEach(b -> data.put("b.Under 10 days", data.get("b.Under 10 days") + (int) b.get("res1")));
                list2.stream().filter((a) -> a.get("_id")!=null && (int) a.get("_id") > 10 && (int) a.get("_id") <= 15).forEach(b -> data.put("c.Under 15 days", data.get("c.Under 15 days") + (int) b.get("res1")));
                list2.stream().filter((a) -> a.get("_id")!=null && (int) a.get("_id") > 15 && (int) a.get("_id") <= 20).forEach(b -> data.put("d.Under 20 days", data.get("d.Under 20 days") + (int) b.get("res1")));
                list2.stream().filter((a) -> a.get("_id")!=null && (int) a.get("_id") > 20).forEach(b -> data.put("e.Greater than 20 days", data.get("e.Greater than 20 days") + (int) b.get("res1")));
            }

            System.out.println("DATA OUTPUT - "+data);
            finalData.put(priority,data);
        }

        String[] periods = {"a.Under 5 days","b.Under 10 days","c.Under 15 days","d.Under 20 days","e.Greater than 20 days"};

        for(String period : periods){
            JSONArray series =new JSONArray();
            for(String priority : distinctPriorityValues){
                JSONObject evaluatedResult1 = new JSONObject();
                evaluatedResult1.put("name", priority);
                evaluatedResult1.put("value", finalData.get(priority).get(period));
                evaluatedResult1.put("label", priority);
                evaluatedResult1.put("link", null);
                evaluatedResult1.put("children", null);
                evaluatedResult1.put("series", null);
                series.add(evaluatedResult1);
            }

            JSONObject evaluatedResult = new JSONObject();
            evaluatedResult.put("name", period);
            evaluatedResult.put("value", 0);
            evaluatedResult.put("label", period);
            evaluatedResult.put("link", null);
            evaluatedResult.put("children", null);
            evaluatedResult.put("series", series);

            metricValues.add(evaluatedResult);
        }
        projectMetric.setMetricValues(metricValues);
        return projectMetric;
    }

    public ProjectMetric jiraDefectAgeingByPriority(ProjectMetric projectMetric) throws Exception{

        JSONArray metricValues = new JSONArray();
        Integer period = 0;

        // Applying filter conditions
        if(projectMetric.getCustomParams()!=null){
            Map<String, Object> valueSet = (Map<String, Object>) projectMetric.getCustomParams();
            for (Map.Entry<String, Object> val : valueSet.entrySet()) {
                if (val.getKey().equals("period")) {
                    period = (Integer) val.getValue();
                    break;
                }
            }
        }

         if(period!=null || period>0) {
            int trendCount = period;

            if (trendCount > 10) {
                trendCount = 10;
            }

            System.out.println("Period - "+ period);

            for (int i = period - 1; i >= 0; i--) {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                Date startDate = new Date();
                Date endDate = new Date();
                startDate.setMonth(startDate.getMonth() - i);
                endDate.setMonth(endDate.getMonth() - i);
                int lastDayOfMonth = getLastDayOf(endDate.getMonth(), endDate.getYear());
                startDate.setDate(01);
                startDate.setHours(00);
                startDate.setMinutes(00);
                startDate.setSeconds(00);
                endDate.setDate(lastDayOfMonth);
                endDate.setHours(23);
                endDate.setSeconds(59);
                String month_name = formatter.format(startDate);
                Date isoFormat = formatter.parse(month_name);
                //dateList.add(isoFormat);

                System.out.println("Start date - "+startDate);
                System.out.println("EndDate - "+endDate);

                Criteria criteria1 = null;
                Criteria criteria2 = null;

                if(projectMetric.getCustomParams()!=null){
                    Map<String, Object> valueSet = (Map<String, Object>) projectMetric.getCustomParams();
                    for (Map.Entry<String, Object> val : valueSet.entrySet()) {
                        if (val.getKey().equals("period")) {
                            continue;
                        }
                        else if(!(((val.getValue()).getClass())== String.class)) {
                            if(criteria1==null) {
                                criteria1 = Criteria.where(val.getKey()).is(val.getValue());
                            }
                            else{
                                criteria1.and(val.getKey()).is(val.getValue());
                            }
                            if(criteria2==null) {
                                criteria2 = Criteria.where(val.getKey()).is(val.getValue());
                            }
                            else{
                                criteria2.and(val.getKey()).is(val.getValue());
                            }
                        }
                        else {
                            List<String> ls = Arrays.asList(String.valueOf(val.getValue()).split(";"));
                            System.out.println("List data - " + ls);
                            if(criteria1==null) {
                                criteria1 = Criteria.where(val.getKey()).in(ls);
                            }
                            else{
                                criteria1.and(val.getKey()).in(ls);
                            }
                            if(criteria2==null) {
                                criteria2 =Criteria.where(val.getKey()).in(ls);
                            }
                            else{
                                criteria2.and(val.getKey()).in(ls);
                            }
                        }
                    }
                }

                if (criteria1 == null) {
                    criteria1 = Criteria.where("created").gte(startDate).lte(endDate);
                } else {
                    criteria1.and("created").gte(startDate).lte(endDate);
                }

                if (criteria2 == null) {
                    criteria2 = Criteria.where("created").gte(startDate).lte(endDate);
                } else {
                    criteria2.and("created").gte(startDate).lte(endDate);
                }

                if (criteria1 == null) {
                    criteria1 = Criteria.where("statusCategoryName").is("Done");
                } else {
                    criteria1.and("statusCategoryName").is("Done");
                }

                if (criteria2 == null) {
                    criteria2 = Criteria.where("statusCategoryName").in("In Progress", "To Do");
                } else {
                    criteria2.and("statusCategoryName").in("In Progress", "To Do");
                }


                List<HashMap> list1 = new ArrayList<>();
                List<HashMap> list2 = new ArrayList<>();
                Class collectionClass = null;

                collectionClass = Class.forName("com.cts.idashboard.services.metricservice.data." + "JiraIssue");

                try {
                    Date currentDate = Date.from(Instant.now());

                    list1 =  mongoOperation.aggregate(newAggregation(collectionClass,
                                    match(criteria1),
                                    project("resolutiondate", "created").andExpression("toDouble((resolutionDate-created)/(1000*3600*24))").as("diff"),
                                    group().sum("diff").as("sum")),
                            HashMap.class).getMappedResults();

                    list2 = mongoOperation.aggregate(newAggregation(collectionClass,
                                    match(criteria2),
                                    project("created").andExpression("toDouble(([0]-created)/(1000*3600*24))", currentDate).as("diff"),
                                    group().sum("diff").as("sum")),
                            HashMap.class).getMappedResults();

                } catch (Exception e) {
                    list1 = null;
                    list2 = null;
                }


                Query query1 = new Query();
                Query query2 = new Query();
                query1.addCriteria(criteria1);
                query2.addCriteria(criteria2);

                long val1 = 0L;
                long val2 = 0L;
                double val3 = 0.0;
                double val4 = 0.0;


                val1 = mongoOperation.count(query1,JiraIssue.class);
                val2 = mongoOperation.count(query2,JiraIssue.class);

                if (list1!=null && list1.size() > 0)
                    val3 = (double)(list1.get(0).get("sum"));

                if (list2!=null && list2.size() > 0)
                    val4 = (double)(list2.get(0).get("sum"));

                System.out.println("Val1 - " + val1);
                System.out.println("Val2 - " + val2);
                System.out.println("Val3 - " + val3);
                System.out.println("Val4 - " + val4);

                SimpleDateFormat outputDateFormat = new SimpleDateFormat("EEE MMM dd hh:mm:ss z yyyy");
                String outputDateString = outputDateFormat.format(isoFormat);

                Double avg =0.0;

                try {
                    avg = (val3 + val4) / (val1 + val2);
                }
                catch (Exception e){
                    avg =0.0;
                }

                JSONObject evaluatedResult = new JSONObject();
                evaluatedResult.put("name", outputDateString);
                evaluatedResult.put("value", avg);
                evaluatedResult.put("label", outputDateString);
                evaluatedResult.put("link", null);
                evaluatedResult.put("children", null);
                evaluatedResult.put("series", null);

                metricValues.add(evaluatedResult);

            }
        }
        projectMetric.setMetricValues(metricValues);
        return projectMetric;
    }

    public ProjectMetric jiraOpenClosedDefectsCount(ProjectMetric projectMetric) throws Exception{

        Query query1 = new Query();
        Query query2 = new Query();
        Criteria criteria1 = null;
        Criteria criteria2 = null;
        JSONArray metricValues = new JSONArray();
        criteria1 = Criteria.where("statusCategoryName").in("In Progress", "To Do");
        if(criteria1 !=null){
            query1.addCriteria(criteria1);
        }
        criteria2 = Criteria.where("statusCategoryName").in("Done");
        if(criteria2 !=null){
            query2.addCriteria(criteria2);
        }

        // Applying filter conditions
        if(projectMetric.getCustomParams()!=null){
            Map<String, Object> valueSet = (Map<String, Object>) projectMetric.getCustomParams();
            for (Map.Entry<String, Object> val : valueSet.entrySet()) {
                if(!(((val.getValue()).getClass())== String.class)) {
                    criteria1 = Criteria.where(val.getKey()).is(val.getValue());
                    if(criteria1 !=null){
                        query1.addCriteria(criteria1);
                    }
                    criteria2 = Criteria.where(val.getKey()).is(val.getValue());
                    if(criteria2 !=null){
                        query2.addCriteria(criteria2);
                    }
                }
                else {
                    List<String> ls = Arrays.asList(String.valueOf(val.getValue()).split(";"));
                    System.out.println("List data - " + ls);
                    criteria1 = Criteria.where(val.getKey()).in(ls);
                    if(criteria1 !=null){
                        query1.addCriteria(criteria1);
                    }
                    criteria2 = Criteria.where(val.getKey()).in(ls);
                    if(criteria2 !=null){
                        query2.addCriteria(criteria2);
                    }
                }
            }
        }

        System.out.println("Query1 - "+query1);
        System.out.println("Query1 - "+query2);
        long val1 = 0L;
        long val2 = 0L;
        val1 = mongoOperation.count(query1,JiraIssue.class);
        val2 = mongoOperation.count(query2,JiraIssue.class);

        JSONObject evaluatedResult1 = new JSONObject();
        evaluatedResult1.put("name", "Open");
        evaluatedResult1.put("value", val1);
        evaluatedResult1.put("label", "Open");
        evaluatedResult1.put("link", null);
        evaluatedResult1.put("children", null);
        evaluatedResult1.put("series", null);

        JSONObject evaluatedResult2 = new JSONObject();
        evaluatedResult2.put("name", "Closed");
        evaluatedResult2.put("value", val2);
        evaluatedResult2.put("label", "Closed");
        evaluatedResult2.put("link", null);
        evaluatedResult2.put("children", null);
        evaluatedResult2.put("series", null);

        metricValues.add(evaluatedResult1);
        metricValues.add(evaluatedResult2);
        projectMetric.setMetricValues(metricValues);
        return projectMetric;
    }


    public ProjectMetric customStcManual(ProjectMetric projectMetric) throws Exception{

        JSONArray metricValues = new JSONArray();
        Query query = new Query();
        Criteria criteria = null;
        Integer period = 0;

        // Applying filter conditions
        if(projectMetric.getCustomParams()!=null){
            Map<String, Object> valueSet = (Map<String, Object>) projectMetric.getCustomParams();
            for (Map.Entry<String, Object> val : valueSet.entrySet()) {
                if (val.getKey().equals("period")) {
                    period = (Integer) val.getValue();
                }
                else if(!(((val.getValue()).getClass())== String.class)) {
                    criteria = Criteria.where(val.getKey()).is(val.getValue());
                    query.addCriteria(criteria);
                }
                else {
                    List<String> ls = Arrays.asList(String.valueOf(val.getValue()).split(";"));
                    System.out.println("List data - " + ls);
                    criteria = Criteria.where(val.getKey()).in(ls);
                    query.addCriteria(criteria);
                }
            }
        }

        List<Date> dateList = new ArrayList<>();

        if(period!=null || period>0) {
            int trendCount = period;

            if (trendCount > 10) {
                trendCount = 10;
            }

            for (int i = period - 1; i >= 0; i--) {
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
                Date isoFormat = formatter.parse(month_name);
                dateList.add(isoFormat);
            }
        }

        System.out.println("QUERY - "+query);
        List<SourceManualData> data = mongoOperation.find(query,SourceManualData.class);

        for(SourceManualData data1 : data){
            Date date = getISODate(data1.getCalculatedDate());
            System.out.println(date);
            if(period!=0 && !dateList.isEmpty()) {
                if (dateList.contains(date)) {
                    SimpleDateFormat outputDateFormat = new SimpleDateFormat("EEE MMM dd hh:mm:ss z yyyy");
                    String outputDateString = outputDateFormat.format(date);
                    JSONObject evaluatedResult1 = new JSONObject();
                    evaluatedResult1.put("name", outputDateString);
                    evaluatedResult1.put("value", Double.valueOf(data1.getCalculatedValue()));
                    evaluatedResult1.put("label", outputDateString);
                    evaluatedResult1.put("link", null);
                    evaluatedResult1.put("children", null);
                    evaluatedResult1.put("series", null);

                    metricValues.add(evaluatedResult1);
                }
            }
            else{
                SimpleDateFormat outputDateFormat = new SimpleDateFormat("EEE MMM dd hh:mm:ss z yyyy");
                String outputDateString = outputDateFormat.format(date);
                JSONObject evaluatedResult1 = new JSONObject();
                evaluatedResult1.put("name", outputDateString);
                evaluatedResult1.put("value", Double.valueOf(data1.getCalculatedValue()));
                evaluatedResult1.put("label", outputDateString);
                evaluatedResult1.put("link", null);
                evaluatedResult1.put("children", null);
                evaluatedResult1.put("series", null);
                metricValues.add(evaluatedResult1);
            }
        }
        projectMetric.setMetricValues(metricValues);
        return projectMetric;
    }

    public ProjectMetric customTableStc(ProjectMetric projectMetric) throws Exception{

        JSONArray metricValues = new JSONArray();

        JSONObject evaluatedResult1 = new JSONObject();
        evaluatedResult1.put("Metrics", "% defect leakage to BAT");
        evaluatedResult1.put("Jun23", "50%");
        evaluatedResult1.put("Jul23", "25%");
        evaluatedResult1.put("Aug23", "20%");

        JSONObject evaluatedResult2 = new JSONObject();
        evaluatedResult2.put("Metrics", "% defect leakage to Production");
        evaluatedResult2.put("Jun23", "75%");
        evaluatedResult2.put("Jul23", "35%");
        evaluatedResult2.put("Aug23", "10%");

        JSONObject evaluatedResult3 = new JSONObject();
        evaluatedResult3.put("Metrics", "Schedule variance");
        evaluatedResult3.put("Jun23", "80%");
        evaluatedResult3.put("Jul23", "35%");
        evaluatedResult3.put("Aug23", "20%");

        metricValues.add(evaluatedResult1);
        metricValues.add(evaluatedResult2);
        metricValues.add(evaluatedResult3);

        projectMetric.setMetricValues(metricValues);
        return projectMetric;
    }

    public double defectAgeingFormula(Query query, Class collectionClass) throws Exception{
        Long valKey = mongoOperation.count(query, collectionClass);
        System.out.println("Collection used for query : "+collectionClass);
        System.out.println("Query applied : "+query);
        System.out.println("Count value : "+valKey);
        return valKey;
    }

    public Criteria projectFilter(Class collectionClass, ProjectMetric projectMetric) {

        Criteria criteria = null;
        String projectField = null;

        Optional<SourceTools> sourceTools = sourceToolsRepository.getByToolNameIgnoreCase(projectMetric.getToolName());
        if(sourceTools.isPresent()){
            projectField = sourceTools.get().getProjectField();
        }
        else{
            System.err.println("Source tool not exists with tool name : "+ projectMetric.getToolName());
        }

        if(collectionClass!=null) {
            if (collectionClass.toString().toLowerCase().contains("jira")) {
                System.out.println("Jira Project Names : " + projectMetric.getJiraProjects());
                if (projectField != null && projectMetric.getJiraProjects() != null) {
                    criteria = Criteria.where(projectField).in(projectMetric.getJiraProjects());
                }
            } else if (collectionClass.toString().toLowerCase().contains("alm")) {
                System.out.println("ALM Project Names : " + projectMetric.getAlmProjects());
                if (projectField != null && projectMetric.getAlmProjects() != null) {
                    criteria = Criteria.where(projectField).in(projectMetric.getAlmProjects());
                }
            } else if (collectionClass.toString().toLowerCase().contains("rally")) {
                System.out.println("Rally Project Names : " + projectMetric.getRallyProjects());
                if (projectField != null && projectMetric.getRallyProjects() != null) {
                    criteria = Criteria.where(projectField).in(projectMetric.getRallyProjects());
                }
            } else if (collectionClass.toString().toLowerCase().contains("zephyr")) {
                if (collectionClass.toString().toLowerCase().contains("zephyrscale")) {
                    System.out.println("Zephyr scale Project Names : " + projectMetric.getZephyrScaleProjects());
                    if (projectField != null && projectMetric.getZephyrScaleProjects() != null) {
                        criteria = Criteria.where(projectField).in(projectMetric.getZephyrScaleProjects());
                    }
                } else {
                    System.out.println("zephyr Project Names : " + projectMetric.getZephyrProjects());
                    if (projectField != null && projectMetric.getZephyrProjects() != null) {
                        criteria = Criteria.where(projectField).in(projectMetric.getZephyrProjects());
                    }
                }
            } else if (collectionClass.toString().toLowerCase().contains("service")) {
                System.out.println("Service now Project Names : " + projectMetric.getServiceNowIncidentProjects());
                if (projectField != null && projectMetric.getServiceNowIncidentProjects() != null) {
                    criteria = Criteria.where(projectField).in(projectMetric.getServiceNowIncidentProjects());
                }
            } else if (collectionClass.toString().toLowerCase().contains("bots")) {
                System.out.println("Bot Project Names : " + projectMetric.getBotsDefectsProjects());
                if (projectField != null && projectMetric.getBotsDefectsProjects() != null) {
                    criteria = Criteria.where(projectField).in(projectMetric.getBotsDefectsProjects());
                }
            } else if (collectionClass.toString().toLowerCase().contains("xray")) {
                System.out.println(" XRayProject Names : " + projectMetric.getXrayProjects());
                if (projectField != null && projectMetric.getXrayProjects() != null) {
                    criteria = Criteria.where(projectField).in(projectMetric.getXrayProjects());
                }
            } else if (collectionClass.toString().toLowerCase().contains("git")) {
                System.out.println(" Git Project Names : " + projectMetric.getGitProjects());
                if (projectField != null && projectMetric.getGitProjects() != null) {
                    criteria = Criteria.where(projectField).in(projectMetric.getGitProjects());
                }
            } else if (collectionClass.toString().toLowerCase().contains("jenkins")) {
                System.out.println("Jenkins Project Names : " + projectMetric.getJenkinsProjects());
                if (projectField != null && projectMetric.getJenkinsProjects() != null) {
                    criteria = Criteria.where(projectField).in(projectMetric.getJenkinsProjects());
                }
            } else {
                System.err.println("No tool found for the collection class - " + collectionClass);
            }
        }
        else{
            System.err.println("Collection class is null");
        }

        return criteria;
    }

    public Date getISODate(String dateString) throws ParseException {
        String[] val = dateString.split("-");
        String month = val[0];
        String year = "20"+val[1];
        String dateFormat = "1-"+month+"-"+year;
        System.out.println(dateFormat);
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
        Date date = formatter.parse(dateFormat);
        return date;
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
