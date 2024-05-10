package com.cts.idashboard.services.metricservice.services;

import com.cts.idashboard.services.metricservice.data.ProjectMetric;
import com.cts.idashboard.services.metricservice.data.SourceTools;
import com.cts.idashboard.services.metricservice.repos.SourceToolsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Component
public class MetricFunctions {

    @Autowired
    MongoOperations mongoOperation;

    @Autowired
    SourceToolsRepository sourceToolsRepository;

    public long count(String className, Set<Map.Entry<String, String>> valueSet, ProjectMetric projectMetric, String isTrending, String isGrouping, Date startDate, Date endDate, String distinctValue) throws Exception {

        Class collectionClass = null;
        Long valKey = 0L;

        try {
            collectionClass = Class.forName("com.cts.idashboard.services.metricservice.data." + className);

            Query query = new Query();
            String isProjectsEmpty = "Yes";

            Criteria criteria = null;
            for (Map.Entry<String, String> val : valueSet) {
                System.out.println("Value map -- *** -- " + val.getValue());

                if (val.getKey().equals("collection")) {
                    String pojoName = getClassName(val.getValue());
                    collectionClass = Class.forName("com.cts.idashboard.services.metricservice.data." + pojoName);
                }
            }

            // Applying project filter criteria
            criteria = projectFilter(collectionClass, projectMetric);
            if (criteria != null) {
                if (!(isGrouping.equals("Yes") && projectMetric.getGroupBy().equals("projectName"))) {
                    query.addCriteria(criteria);
                }
                isProjectsEmpty = "No";
            }

            // Applying trendBy or GroupBy criteria
            if (isGrouping.equals("No") && isTrending.equals("Yes")) {
                criteria = Criteria.where(projectMetric.getTrendBy()).gte(startDate).lte(endDate);
                query.addCriteria(criteria);
            } else if (isGrouping.equals("Yes") && isTrending.equals("No")) {
                criteria = Criteria.where(projectMetric.getGroupBy()).in(distinctValue);
                query.addCriteria(criteria);
            }
            System.out.println("Query applied - "+query);
            if (isProjectsEmpty.equals("No")) {
                valKey = mongoOperation.count(query, collectionClass);
            } else{
                valKey = 0L;
                System.err.println("Project filter is null. Setting calculation value to 0");
            }
        }
        catch(ClassNotFoundException e){
            System.err.println("Class not exists with name - "+collectionClass);
        }

        System.out.println("Count value * " + valKey);
        return valKey;
    }

    public long countAnd(String className, Set<Map.Entry<String, String>> valueSet, ProjectMetric projectMetric, String isTrending, String isGrouping, Date startDate, Date endDate, String distinctValue) throws Exception {

        Class collectionClass = null;
        Long valKey = 0L;

        try {
            collectionClass = Class.forName("com.cts.idashboard.services.metricservice.data." + className);

            Query query = new Query();
            String isProjectsEmpty = "Yes";

            Criteria criteria = null;
            for (Map.Entry<String, String> val : valueSet) {
                System.out.println("Value map -- *** -- " + val.getValue());

                if (val.getKey().equals("collection")) {
                    String pojoName = getClassName(val.getValue());
                    collectionClass = Class.forName("com.cts.idashboard.services.metricservice.data." + pojoName);
                } else {
                    System.out.println(((Object) val.getValue()).getClass());
                    if(!((((Object)val.getValue()).getClass())== String.class)) {
                        criteria = Criteria.where(val.getKey()).is(val.getValue());
                        query.addCriteria(criteria);
                    }
                    else {
                        List<String> ls = Arrays.asList(val.getValue().split(";"));
                        System.out.println("List data - " + ls);
                        criteria = Criteria.where(val.getKey()).in(ls);
                        query.addCriteria(criteria);
                    }
                }
            }

            // Applying project filter criteria
            criteria = projectFilter(collectionClass, projectMetric);
            if (criteria != null) {
                if (!(isGrouping.equals("Yes") && projectMetric.getGroupBy().equals("projectName"))) {
                    query.addCriteria(criteria);
                }
                isProjectsEmpty = "No";
            }

            // Applying trendBy or GroupBy criteria
            if (isGrouping.equals("No") && isTrending.equals("Yes")) {
                criteria = Criteria.where(projectMetric.getTrendBy()).gte(startDate).lte(endDate);
                query.addCriteria(criteria);
            } else if (isGrouping.equals("Yes") && isTrending.equals("No")) {
                criteria = Criteria.where(projectMetric.getGroupBy()).in(distinctValue);
                query.addCriteria(criteria);
            }

            System.out.println("Query applied - "+query);
            if (isProjectsEmpty.equals("No")) {
                valKey = mongoOperation.count(query, collectionClass);
            }else{
                valKey = 0L;
                System.err.println("Project filter is null. Setting calculation value to 0");
            }
        }
        catch(ClassNotFoundException e){
            System.err.println("Class not exists with name - "+collectionClass);
        }
        System.out.println("CountAnd value * "+valKey);
        return valKey;
    }

    public Double sum(String className, Set<Map.Entry<String, String>> valueSet, ProjectMetric projectMetric, String isTrending, String isGrouping, Date startDate, Date endDate, String distinctValue)throws Exception {

        Class collectionClass = null;
        Double valKey = 0d;
        String fieldName = null;
        Criteria criteria = null;
        List<HashMap> res = null;

        try {
            collectionClass = Class.forName("com.cts.idashboard.services.metricservice.data." + className);

            for (Map.Entry<String, String> val : valueSet) {
                System.out.println("Value map -- *** -- " + val.getValue());
                if (val.getKey().equals("collection")){
                    String pojoName = getClassName(val.getValue());
                    collectionClass = Class.forName("com.cts.idashboard.services.metricservice.data." + pojoName);
                }else if (val.getKey().equals("sum_field")) {
                    fieldName = val.getValue();
                }
            }

            // Applying project filter criteria
            if (criteria == null)
                criteria = projectFilter(collectionClass, projectMetric);
            else
                criteria.andOperator(projectFilter(collectionClass, projectMetric));

            // Applying trendBy or GroupBy criteria
            if (isGrouping.equals("No") && isTrending.equals("Yes")) {
                if (criteria == null)
                    criteria = Criteria.where(projectMetric.getTrendBy()).gte(startDate).lte(endDate);
                else
                    criteria.and(projectMetric.getTrendBy()).gte(startDate).lte(endDate);
            } else if (isGrouping.equals("Yes") && isTrending.equals("No")) {
                if (criteria == null)
                    criteria = Criteria.where(projectMetric.getGroupBy()).in(distinctValue);
                else
                    criteria.and(projectMetric.getGroupBy()).in(distinctValue);
            }

            if(fieldName!=null) {
                if (criteria == null) {
                    System.out.println("Criteria is null");
                    res = mongoOperation.aggregate(newAggregation(
                                    collectionClass,
                                    group().sum(fieldName).as("Sum_value")),
                            HashMap.class).getMappedResults();
                } else {
                    System.out.println("Criteria is not null");
                    res = mongoOperation.aggregate(newAggregation(
                                    collectionClass,
                                    match(criteria),
                                    group().sum(fieldName).as("Sum_value")),
                            HashMap.class).getMappedResults();
                }
            }
            if (res!=null && res.size() > 0)
                valKey = Double.valueOf(res.get(0).get("Sum_value").toString());
        }
        catch(ClassNotFoundException e){
            System.err.println("Class not exists with name - "+collectionClass);
        }

        System.out.println("Sum value ** "+valKey);
        return valKey;
    }

    public Double sumAnd(String className, Set<Map.Entry<String, String>> valueSet, ProjectMetric projectMetric, String isTrending, String isGrouping, Date startDate, Date endDate, String distinctValue) throws Exception {

        Class collectionClass = null;
        Double valKey = 0d;
        String fieldName = null;
        Criteria criteria = null;
        List<HashMap> res = null;

        try {
            collectionClass = Class.forName("com.cts.idashboard.services.metricservice.data." + className);

            for (Map.Entry<String, String> val : valueSet) {
                System.out.println("Value map -- *** -- " + val.getValue());

                if (val.getKey().equals("collection")) {
                    String pojoName = getClassName(val.getValue());
                    collectionClass = Class.forName("com.cts.idashboard.services.metricservice.data." + pojoName);
                } else if (val.getKey().equals("sum_field")) {
                    fieldName = val.getValue();
                } else {
                    if (criteria == null)
                        criteria = Criteria.where(val.getKey()).is(val.getValue());
                    else
                        criteria.and(val.getKey()).is(val.getValue());
                }
            }

            // Applying project filter criteria
            if (criteria == null)
                criteria = projectFilter(collectionClass, projectMetric);
            else
                criteria.andOperator(projectFilter(collectionClass, projectMetric));

            // Applying trendBy or GroupBy criteria
            if (isGrouping.equals("No") && isTrending.equals("Yes")) {
                if (criteria == null)
                    criteria = Criteria.where(projectMetric.getTrendBy()).gte(startDate).lte(endDate);
                else
                    criteria.and(projectMetric.getTrendBy()).gte(startDate).lte(endDate);
            } else if (isGrouping.equals("Yes") && isTrending.equals("No")) {
                if (criteria == null)
                    criteria = Criteria.where(projectMetric.getGroupBy()).in(distinctValue);
                else
                    criteria.and(projectMetric.getGroupBy()).in(distinctValue);
            }
            if(fieldName!=null) {
                if (criteria == null) {
                    res = mongoOperation.aggregate(newAggregation(
                                    collectionClass,
                                    group().sum(fieldName).as("Sum_And_value")),
                            HashMap.class).getMappedResults();
                } else {
                    res = mongoOperation.aggregate(newAggregation(
                                    collectionClass,
                                    match(criteria),
                                    group().sum(fieldName).as("Sum_And_value")),
                            HashMap.class).getMappedResults();
                }
            }

            if (res!=null && res.size() > 0)
                valKey = Double.valueOf(res.get(0).get("Sum_And_value").toString());
        }
        catch(ClassNotFoundException e){
            System.err.println("Class not exists with name - "+collectionClass);
        }

        System.out.println("SumAnd value ** "+valKey);
        return valKey;
    }

    public double avg(String className, Set<Map.Entry<String, String>> valueSet, ProjectMetric projectMetric, String isTrending, String isGrouping, Date startDate, Date endDate, String distinctValue) throws Exception {

        Class collectionClass = null;
        Double valKey = 0.0;
        String fieldName = null;
        Criteria criteria = null;
        List<HashMap> res = null;

        try {
            collectionClass = Class.forName("com.cts.idashboard.services.metricservice.data." + className);

            for (Map.Entry<String, String> val : valueSet) {
                System.out.println("Value map -- *** -- " + val.getValue());
                if (val.getKey().equals("collection")){
                    String pojoName = getClassName(val.getValue());
                    collectionClass = Class.forName("com.cts.idashboard.services.metricservice.data." + pojoName);
                }else if (val.getKey().equals("avg_field")) {
                    fieldName = val.getValue();
                }
            }

            // Applying project filter criteria
            if (criteria == null)
                criteria = projectFilter(collectionClass, projectMetric);
            else
                criteria.andOperator(projectFilter(collectionClass, projectMetric));

            // Applying trendBy or GroupBy criteria
            if (isGrouping.equals("No") && isTrending.equals("Yes")) {
                if (criteria == null)
                    criteria = Criteria.where(projectMetric.getTrendBy()).gte(startDate).lte(endDate);
                else
                    criteria.and(projectMetric.getTrendBy()).gte(startDate).lte(endDate);
            } else if (isGrouping.equals("Yes") && isTrending.equals("No")) {
                if (criteria == null)
                    criteria = Criteria.where(projectMetric.getGroupBy()).in(distinctValue);
                else
                    criteria.and(projectMetric.getGroupBy()).in(distinctValue);
            }

            if(fieldName!=null) {
                if (criteria == null) {
                    res = mongoOperation.aggregate(newAggregation(
                                    collectionClass,
                                    group().avg(fieldName).as("Avg_value")),
                            HashMap.class).getMappedResults();
                } else {
                    res = mongoOperation.aggregate(newAggregation(
                                    collectionClass,
                                    match(criteria),
                                    group().avg(fieldName).as("Avg_value")),
                            HashMap.class).getMappedResults();
                }
            }

            if (res!=null && res.size() > 0)
                valKey = Double.valueOf(res.get(0).get("Avg_value").toString());
        }
        catch(ClassNotFoundException e){
            System.err.println("Class not exists with name - "+collectionClass);
        }

        System.out.println("Avg value ** "+valKey);
        return valKey;
    }

    public double avgAnd(String className, Set<Map.Entry<String, String>> valueSet, ProjectMetric projectMetric, String isTrending, String isGrouping, Date startDate, Date endDate, String distinctValue) throws Exception {

        Class collectionClass = null;
        Double valKey = 0.0;
        String fieldName = null;
        Criteria criteria = null;
        List<HashMap> res = null;

        try {
            collectionClass = Class.forName("com.cts.idashboard.services.metricservice.data." + className);

            for (Map.Entry<String, String> val : valueSet) {
                System.out.println("Value map -- *** -- " + val.getValue());

                if (val.getKey().equals("collection")) {
                    String pojoName = getClassName(val.getValue());
                    collectionClass = Class.forName("com.cts.idashboard.services.metricservice.data." + pojoName);
                } else if (val.getKey().equals("avg_field")) {
                    fieldName = val.getValue();
                } else {
                    if (criteria == null)
                        criteria = Criteria.where(val.getKey()).is(val.getValue());
                    else
                        criteria.and(val.getKey()).is(val.getValue());
                }
            }

            // Applying project filter criteria
            if (criteria == null)
                criteria = projectFilter(collectionClass, projectMetric);
            else
                criteria.andOperator(projectFilter(collectionClass, projectMetric));

            // Applying trendBy or GroupBy criteria
            if (isGrouping.equals("No") && isTrending.equals("Yes")) {
                if (criteria == null)
                    criteria = Criteria.where(projectMetric.getTrendBy()).gte(startDate).lte(endDate);
                else
                    criteria.and(projectMetric.getTrendBy()).gte(startDate).lte(endDate);
            } else if (isGrouping.equals("Yes") && isTrending.equals("No")) {
                if (criteria == null)
                    criteria = Criteria.where(projectMetric.getGroupBy()).in(distinctValue);
                else
                    criteria.and(projectMetric.getGroupBy()).in(distinctValue);
            }

            if(fieldName!=null) {
                if (criteria == null) {
                    res = mongoOperation.aggregate(newAggregation(
                                    collectionClass,
                                    group().avg(fieldName).as("Avg_And_value")),
                            HashMap.class).getMappedResults();
                } else {
                    res = mongoOperation.aggregate(newAggregation(
                                    collectionClass,
                                    match(criteria),
                                    group().avg(fieldName).as("Avg_And_value")),
                            HashMap.class).getMappedResults();
                }
            }

            if (res!=null && res.size() > 0)
                valKey = Double.valueOf(res.get(0).get("Avg_And_value").toString());
        }
        catch(ClassNotFoundException e){
            System.err.println("Class not exists with name - "+collectionClass);
        }

        System.out.println("AvgAnd value ** " + valKey);
        return valKey;
    }


    public double sumIS0(String className, Set<Map.Entry<String, String>> valueSet, ProjectMetric projectMetric, String isTrending, String isGrouping, Date startDate, Date endDate, String distinctValue) throws Exception {

        Class collectionClass = null;
        Long totalValue = null;
        String fieldName = null;
        Criteria criteria = null;
        List<HashMap> res = null;
        Double valKey = 0.0;

        try {
            collectionClass = Class.forName("com.cts.idashboard.services.metricservice.data." + className);

            for (Map.Entry<String, String> val : valueSet) {
                System.out.println("Value map -- *** -- " + val.getValue());
                if (val.getKey().equals("collection")){
                    String pojoName = getClassName(val.getValue());
                    collectionClass = Class.forName("com.cts.idashboard.services.metricservice.data." + pojoName);
                }else if (val.getKey().equals("sum_field")) {
                    fieldName = val.getValue();
                }
            }

            // Applying project filter criteria
            if (criteria == null)
                criteria = projectFilter(collectionClass, projectMetric);
            else
                criteria.andOperator(projectFilter(collectionClass, projectMetric));

            // Applying trendBy or GroupBy criteria
            if (isGrouping.equals("No") && isTrending.equals("Yes")) {
                if (criteria == null)
                    criteria = Criteria.where(projectMetric.getTrendBy()).gte(startDate).lte(endDate);
                else
                    criteria.and(projectMetric.getTrendBy()).gte(startDate).lte(endDate);
            } else if (isGrouping.equals("Yes") && isTrending.equals("No")) {
                if (criteria == null)
                    criteria = Criteria.where(projectMetric.getGroupBy()).in(distinctValue);
                else
                    criteria.and(projectMetric.getGroupBy()).in(distinctValue);
            }

            if(fieldName!=null) {
                if (criteria == null) {
                    res = mongoOperation.aggregate(newAggregation(
                                    collectionClass,
                                    group().sum(fieldName).as("Sum_ISO_value")),
                            HashMap.class).getMappedResults();
                } else {
                    res = mongoOperation.aggregate(newAggregation(
                                    collectionClass,
                                    match(criteria),
                                    group().sum(fieldName).as("Sum_ISO_value")),
                            HashMap.class).getMappedResults();
                }
            }

            if (res!=null && res.size() > 0) {
                totalValue = Long.valueOf(res.get(0).get("Sum_ISO_value").toString());
                valKey = (double)totalValue / (3600);
            }
        }
        catch(ClassNotFoundException e){
            System.err.println("Class not exists with name - "+collectionClass);
        }

        System.out.println("SumISO value in hours ** " + valKey);
        return valKey;
    }

    public double sumIS0And(String className, Set<Map.Entry<String, String>> valueSet, ProjectMetric projectMetric, String isTrending, String isGrouping, Date startDate, Date endDate, String distinctValue) throws Exception {

        Class collectionClass = null;
        Long totalValue = null;
        String fieldName = null;
        Criteria criteria = null;
        List<HashMap> res = null;
        Double valKey = 0.0;

        try {
            collectionClass = Class.forName("com.cts.idashboard.services.metricservice.data." + className);

            for (Map.Entry<String, String> val : valueSet) {
                System.out.println("Value map -- *** -- " + val.getValue());
                if (val.getKey().equals("collection")) {
                    String pojoName = getClassName(val.getValue());
                    collectionClass = Class.forName("com.cts.idashboard.services.metricservice.data." + pojoName);
                } else if (val.getKey().equals("sum_field")) {
                    fieldName = val.getValue();
                } else {
                    if (criteria == null)
                        criteria = Criteria.where(val.getKey()).is(val.getValue());
                    else
                        criteria.and(val.getKey()).is(val.getValue());
                }
            }

            // Applying project filter criteria
            if (criteria == null)
                criteria = projectFilter(collectionClass, projectMetric);
            else
                criteria.andOperator(projectFilter(collectionClass, projectMetric));

            // Applying trendBy or GroupBy criteria
            if (isGrouping.equals("No") && isTrending.equals("Yes")) {
                if (criteria == null)
                    criteria = Criteria.where(projectMetric.getTrendBy()).gte(startDate).lte(endDate);
                else
                    criteria.and(projectMetric.getTrendBy()).gte(startDate).lte(endDate);
            } else if (isGrouping.equals("Yes") && isTrending.equals("No")) {
                if (criteria == null)
                    criteria = Criteria.where(projectMetric.getGroupBy()).in(distinctValue);
                else
                    criteria.and(projectMetric.getGroupBy()).in(distinctValue);
            }

            if(fieldName!=null) {
                if (criteria == null) {
                    res = mongoOperation.aggregate(newAggregation(
                                    collectionClass,
                                    group().sum(fieldName).as("Sum_ISO_And_value")),
                            HashMap.class).getMappedResults();
                } else {
                    res = mongoOperation.aggregate(newAggregation(
                                    collectionClass,
                                    match(criteria),
                                    group().sum(fieldName).as("Sum_ISO_And_value")),
                            HashMap.class).getMappedResults();
                }
            }

            if (res!=null && res.size() > 0) {
                totalValue = Long.valueOf(res.get(0).get("Sum_ISO_And_value").toString());
                valKey = (double)totalValue / (3600);
            }
        }
        catch(ClassNotFoundException e){
            System.err.println("Class not exists with name - "+collectionClass);
        }

        System.out.println("SumISOAnd value in hours:" + valKey);
        return valKey;
    }

    public double sumIS0Diff(String className, Set<Map.Entry<String, String>> valueSet, ProjectMetric projectMetric, String isTrending, String isGrouping, Date startDate, Date endDate, String distinctValue) throws Exception {

        Class collectionClass = null;
        Long totalValue = 0L;
        String field1 = null;
        String field2 = null;
        Criteria criteria = null;
        List<HashMap> res = null;
        Double valKey = 0.0;

        try {
            collectionClass = Class.forName("com.cts.idashboard.services.metricservice.data." + className);

            for (Map.Entry<String, String> val : valueSet) {
                System.out.println("Value map -- *** -- " + val.getValue());
                if (val.getKey().equals("collection")){
                    String pojoName = getClassName(val.getValue());
                    collectionClass = Class.forName("com.cts.idashboard.services.metricservice.data." + pojoName);
                }else if (val.getKey().equals("start")) {
                    field1 = val.getValue();
                }
                else if (val.getKey().equals("end")) {
                    field2 = val.getValue();
                }
            }

            // Applying project filter criteria
            if (criteria == null)
                criteria = projectFilter(collectionClass, projectMetric);
            else
                criteria.andOperator(projectFilter(collectionClass, projectMetric));

            // Applying trendBy or GroupBy criteria
            if (isGrouping.equals("No") && isTrending.equals("Yes")) {
                if (criteria == null)
                    criteria = Criteria.where(projectMetric.getTrendBy()).gte(startDate).lte(endDate);
                else
                    criteria.and(projectMetric.getTrendBy()).gte(startDate).lte(endDate);
            } else if (isGrouping.equals("Yes") && isTrending.equals("No")) {
                if (criteria == null)
                    criteria = Criteria.where(projectMetric.getGroupBy()).in(distinctValue);
                else
                    criteria.and(projectMetric.getGroupBy()).in(distinctValue);
            }

            if(field1!=null && field2!=null) {
                if (criteria == null) {
                    res = mongoOperation.aggregate(newAggregation(
                                    collectionClass,
                                    project("id").andExpression(field2 + "-" + field1).as("diff"),
                                    group().sum("diff").as("sum_ISO_Diff_value")),
                            HashMap.class).getMappedResults();
                } else {
                    res = mongoOperation.aggregate(newAggregation(
                                    collectionClass,
                                    match(criteria),
                                    project("id").andExpression(field2 + "-" + field1).as("diff"),
                                    group().sum("diff").as("sum_ISO_Diff_value")),
                            HashMap.class).getMappedResults();
                }
            }

            if (res!=null && res.size() > 0) {
                totalValue = Long.valueOf(res.get(0).get("sum_ISO_Diff_value").toString());
                valKey = (double)totalValue / (1000 * 3600);
            }
        }
        catch(ClassNotFoundException e){
            System.err.println("Class not exists with name - "+collectionClass);
        }

        System.out.println("SumISODiff value in hours ** :" + valKey);
        return valKey;
    }


    public double sumIS0DiffAnd(String className, Set<Map.Entry<String, String>> valueSet, ProjectMetric projectMetric, String isTrending, String isGrouping, Date startDate, Date endDate, String distinctValue) throws Exception {

        Class collectionClass = null;
        Long totalValue = 0L;
        String field1 = null;
        String field2 = null;
        Criteria criteria = null;
        List<HashMap> res = null;
        Double valKey = 0.0;

        try {
            collectionClass = Class.forName("com.cts.idashboard.services.metricservice.data." + className);

            for (Map.Entry<String, String> val : valueSet) {
                System.out.println("Value map -- *** -- " + val.getValue());

                if (val.getKey().equals("collection")){
                    String pojoName = getClassName(val.getValue());
                    collectionClass = Class.forName("com.cts.idashboard.services.metricservice.data." + pojoName);
                }else if (val.getKey().equals("start")) {
                    field1 = val.getValue();
                }
                else if (val.getKey().equals("end")) {
                    field2 = val.getValue();
                }
                else {
                    if (criteria == null)
                        criteria = Criteria.where(val.getKey()).is(val.getValue());
                    else
                        criteria.and(val.getKey()).is(val.getValue());
                }
            }

            // Applying project filter criteria
            if (criteria == null)
                criteria = projectFilter(collectionClass, projectMetric);
            else
                criteria.andOperator(projectFilter(collectionClass, projectMetric));

            // Applying trendBy or GroupBy criteria
            if (isGrouping.equals("No") && isTrending.equals("Yes")) {
                if (criteria == null)
                    criteria = Criteria.where(projectMetric.getTrendBy()).gte(startDate).lte(endDate);
                else
                    criteria.and(projectMetric.getTrendBy()).gte(startDate).lte(endDate);
            } else if (isGrouping.equals("Yes") && isTrending.equals("No")) {
                if (criteria == null)
                    criteria = Criteria.where(projectMetric.getGroupBy()).in(distinctValue);
                else
                    criteria.and(projectMetric.getGroupBy()).in(distinctValue);
            }

            if(field1!=null && field2!=null) {
                if (criteria == null) {
                    res = mongoOperation.aggregate(newAggregation(
                                    collectionClass,
                                    project("id").andExpression(field2 + "-" + field1).as("diff"),
                                    group().sum("diff").as("sum_ISO_Diff_And_value")),
                            HashMap.class).getMappedResults();
                } else {
                    res = mongoOperation.aggregate(newAggregation(
                                    collectionClass,
                                    match(criteria),
                                    project("id").andExpression(field2 + "-" + field1).as("diff"),
                                    group().sum("diff").as("sum_ISO_Diff_And_value")),
                            HashMap.class).getMappedResults();
                }
            }

            if (res!=null && res.size() > 0) {
                totalValue = Long.valueOf(res.get(0).get("sum_ISO_Diff_And_value").toString());
                valKey = (double)totalValue / (1000 * 3600);
            }
        }
        catch(ClassNotFoundException e){
            System.err.println("Class not exists with name - "+collectionClass);
        }

        System.out.println("SumISODiffAnd value in hours ** " + valKey);

        return valKey;
    }

    public long max(String className, Set<Map.Entry<String, String>> valueSet, ProjectMetric projectMetric, String isTrending, String isGrouping, Date startDate, Date endDate, String distinctValue) throws Exception {

        Class collectionClass = null;
        Long valKey = 0L;
        String fieldName = null;
        Criteria criteria = null;
        List<HashMap> res = null;

        try {
            collectionClass = Class.forName("com.cts.idashboard.services.metricservice.data." + className);

            for (Map.Entry<String, String> val : valueSet) {
                System.out.println("Value map -- *** -- " + val.getValue());

                if (val.getKey().equals("collection")){
                    String pojoName = getClassName(val.getValue());
                    collectionClass = Class.forName("com.cts.idashboard.services.metricservice.data." + pojoName);
                }else if (val.getKey().equals("max_field")) {
                    fieldName = val.getValue();
                }
            }

            // Applying project filter criteria
            if (criteria == null)
                criteria = projectFilter(collectionClass, projectMetric);
            else
                criteria.andOperator(projectFilter(collectionClass, projectMetric));

            // Applying trendBy or GroupBy criteria
            if (isGrouping.equals("No") && isTrending.equals("Yes")) {
                if (criteria == null)
                    criteria = Criteria.where(projectMetric.getTrendBy()).gte(startDate).lte(endDate);
                else
                    criteria.and(projectMetric.getTrendBy()).gte(startDate).lte(endDate);
            } else if (isGrouping.equals("Yes") && isTrending.equals("No")) {
                if (criteria == null)
                    criteria = Criteria.where(projectMetric.getGroupBy()).in(distinctValue);
                else
                    criteria.and(projectMetric.getGroupBy()).in(distinctValue);
            }

            if(fieldName!=null) {
                if (criteria == null) {
                    res = mongoOperation.aggregate(newAggregation(
                                    collectionClass,
                                    group().max(fieldName).as("Max_value")),
                            HashMap.class).getMappedResults();
                } else {
                    res = mongoOperation.aggregate(newAggregation(
                                    collectionClass,
                                    match(criteria),
                                    group().max(fieldName).as("Max_value")),
                            HashMap.class).getMappedResults();
                }
            }

            if (res!=null && res.size() > 0)
                valKey = Long.valueOf(res.get(0).get("Max_value").toString());
        }
        catch(ClassNotFoundException e){
            System.err.println("Class not exists with name - "+collectionClass);
        }

        System.out.println("Max value ** "+valKey);
        return valKey;
    }

    public long maxAnd(String className, Set<Map.Entry<String, String>> valueSet, ProjectMetric projectMetric, String isTrending, String isGrouping, Date startDate, Date endDate, String distinctValue) throws Exception {

        Class collectionClass = null;
        Long valKey = 0L;
        String fieldName = null;
        Criteria criteria = null;
        List<HashMap> res = null;

        try {
            collectionClass = Class.forName("com.cts.idashboard.services.metricservice.data." + className);

            for (Map.Entry<String, String> val : valueSet) {
                System.out.println("Value map -- *** -- " + val.getValue());
                if (val.getKey().equals("collection")){
                    String pojoName = getClassName(val.getValue());
                    collectionClass = Class.forName("com.cts.idashboard.services.metricservice.data." + pojoName);
                }else if (val.getKey().equals("max_field")) {
                    fieldName = val.getValue();
                } else {
                    if (criteria == null)
                        criteria = Criteria.where(val.getKey()).is(val.getValue());
                    else
                        criteria.and(val.getKey()).is(val.getValue());
                }
            }

            // Applying project filter criteria
            if (criteria == null)
                criteria = projectFilter(collectionClass, projectMetric);
            else
                criteria.andOperator(projectFilter(collectionClass, projectMetric));

            // Applying trendBy or GroupBy criteria
            if (isGrouping.equals("No") && isTrending.equals("Yes")) {
                if (criteria == null)
                    criteria = Criteria.where(projectMetric.getTrendBy()).gte(startDate).lte(endDate);
                else
                    criteria.and(projectMetric.getTrendBy()).gte(startDate).lte(endDate);
            } else if (isGrouping.equals("Yes") && isTrending.equals("No")) {
                if (criteria == null)
                    criteria = Criteria.where(projectMetric.getGroupBy()).in(distinctValue);
                else
                    criteria.and(projectMetric.getGroupBy()).in(distinctValue);
            }

            if(fieldName!=null) {
                if (criteria == null) {
                    res = mongoOperation.aggregate(newAggregation(
                                    collectionClass,
                                    group().max(fieldName).as("Max_And_value")),
                            HashMap.class).getMappedResults();
                } else {
                    res = mongoOperation.aggregate(newAggregation(
                                    collectionClass,
                                    match(criteria),
                                    group().max(fieldName).as("Max_And_value")),
                            HashMap.class).getMappedResults();
                }
            }

            if (res!=null && res.size() > 0)
                valKey = Long.valueOf(res.get(0).get("Max_And_value").toString());
        }
        catch(ClassNotFoundException e){
            System.err.println("Class not exists with name - "+collectionClass);
        }

        System.out.println("MaxAnd value ** "+valKey);
        return valKey;
    }


    public long min(String className, Set<Map.Entry<String, String>> valueSet, ProjectMetric projectMetric, String isTrending, String isGrouping, Date startDate, Date endDate, String distinctValue) throws Exception {

        Class collectionClass = null;
        Long valKey = 0L;
        String fieldName = null;
        Criteria criteria = null;
        List<HashMap> res = null;

        try {
            collectionClass = Class.forName("com.cts.idashboard.services.metricservice.data." + className);

            for (Map.Entry<String, String> val : valueSet) {
                System.out.println("Value map -- *** -- " + val.getValue());

                if (val.getKey().equals("collection")){
                    String pojoName = getClassName(val.getValue());
                    collectionClass = Class.forName("com.cts.idashboard.services.metricservice.data." + pojoName);
                }else if (val.getKey().equals("min_field")) {
                    fieldName = val.getValue();
                }
            }

            // Applying project filter criteria
            if (criteria == null)
                criteria = projectFilter(collectionClass, projectMetric);
            else
                criteria.andOperator(projectFilter(collectionClass, projectMetric));

            // Applying trendBy or GroupBy criteria
            if (isGrouping.equals("No") && isTrending.equals("Yes")) {
                if (criteria == null)
                    criteria = Criteria.where(projectMetric.getTrendBy()).gte(startDate).lte(endDate);
                else
                    criteria.and(projectMetric.getTrendBy()).gte(startDate).lte(endDate);
            } else if (isGrouping.equals("Yes") && isTrending.equals("No")) {
                if (criteria == null)
                    criteria = Criteria.where(projectMetric.getGroupBy()).in(distinctValue);
                else
                    criteria.and(projectMetric.getGroupBy()).in(distinctValue);
            }

            if(fieldName!=null) {
                if (criteria == null) {
                    res = mongoOperation.aggregate(newAggregation(
                                    collectionClass,
                                    group().min(fieldName).as("Min_value")),
                            HashMap.class).getMappedResults();
                } else {
                    res = mongoOperation.aggregate(newAggregation(
                                    collectionClass,
                                    match(criteria),
                                    group().min(fieldName).as("Min_value")),
                            HashMap.class).getMappedResults();
                }
            }

            if (res!=null && res.size() > 0)
                valKey = Long.valueOf(res.get(0).get("Min_value").toString());
        }
        catch(ClassNotFoundException e){
            System.err.println("Class not exists with name - "+collectionClass);
        }

        System.out.println("Min value ** "+valKey);
        return valKey;
    }

    public long minAnd(String className, Set<Map.Entry<String, String>> valueSet, ProjectMetric projectMetric, String isTrending, String isGrouping, Date startDate, Date endDate, String distinctValue) throws Exception {

        Class collectionClass = null;
        Long valKey = 0L;
        String fieldName = null;
        Criteria criteria = null;
        List<HashMap> res = null;

        try {
            collectionClass = Class.forName("com.cts.idashboard.services.metricservice.data." + className);

            for (Map.Entry<String, String> val : valueSet) {
                System.out.println("Value map -- *** -- " + val.getValue());
                if (val.getKey().equals("collection")){
                    String pojoName = getClassName(val.getValue());
                    collectionClass = Class.forName("com.cts.idashboard.services.metricservice.data." + pojoName);
                }else if (val.getKey().equals("min_field")) {
                    fieldName = val.getValue();
                } else {
                    if (criteria == null)
                        criteria = Criteria.where(val.getKey()).is(val.getValue());
                    else
                        criteria.and(val.getKey()).is(val.getValue());
                }
            }

            // Applying project filter criteria
            if (criteria == null)
                criteria = projectFilter(collectionClass, projectMetric);
            else
                criteria.andOperator(projectFilter(collectionClass, projectMetric));

            // Applying trendBy or GroupBy criteria
            if (isGrouping.equals("No") && isTrending.equals("Yes")) {
                if (criteria == null)
                    criteria = Criteria.where(projectMetric.getTrendBy()).gte(startDate).lte(endDate);
                else
                    criteria.and(projectMetric.getTrendBy()).gte(startDate).lte(endDate);
            } else if (isGrouping.equals("Yes") && isTrending.equals("No")) {
                if (criteria == null)
                    criteria = Criteria.where(projectMetric.getGroupBy()).in(distinctValue);
                else
                    criteria.and(projectMetric.getGroupBy()).in(distinctValue);
            }

            if(fieldName!=null) {
                if (criteria == null) {
                    res = mongoOperation.aggregate(newAggregation(
                                    collectionClass,
                                    group().min(fieldName).as("Min_And_value")),
                            HashMap.class).getMappedResults();
                } else {
                    res = mongoOperation.aggregate(newAggregation(
                                    collectionClass,
                                    match(criteria),
                                    group().min(fieldName).as("Min_And_value")),
                            HashMap.class).getMappedResults();
                }
            }

            if (res!=null && res.size() > 0)
                valKey = Long.valueOf(res.get(0).get("Min_And_value").toString());
        }
        catch(ClassNotFoundException e){
            System.err.println("Class not exists with name - "+collectionClass);
        }

        System.out.println("MinAnd value ** "+valKey);
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
            } else if (collectionClass.toString().toLowerCase().contains("servicenow")) {
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

    public String getClassName(String collectionName){

        String className = null;

        if(collectionName.toLowerCase().contains("jira")) {
            className = "JiraIssue";
        }
        else if(collectionName.toLowerCase().contains("alm")){
            className = "SourceAlmAssets";
        }
        else if(collectionName.toLowerCase().contains("zephyr") && !collectionName.toLowerCase().contains("zephyrscale")){
            className = "SourceZephyrData";
        }
        else if(collectionName.toLowerCase().contains("zephyrscale")){
            className = "SourceZephyrScaleData";
        }
        else if(collectionName.toLowerCase().contains("jenkins")){
            className = "SourceJenkinsBuild";
        }
        else if(collectionName.toLowerCase().contains("rally")){
            className = "SourceRallyData";
        }
        else if(collectionName.equalsIgnoreCase("git")){
            className = "SourceGitData";
        }
        else if(collectionName.toLowerCase().contains("service")){
            className = "SourceServiceNowIncidents";
        }
        else if(collectionName.toLowerCase().contains("xray")){
            className = "SourceXrayTests";
        }
        else if(collectionName.toLowerCase().contains("bots")){
            className = "SourceBotsDefects";
        }

        return className;
    }

}
