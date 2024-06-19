package com.cts.idashboard.services.metricservice.services;

import com.cts.idashboard.services.metricservice.data.*;
import com.cts.idashboard.services.metricservice.data.project.CIQDashboardProject;
import com.cts.idashboard.services.metricservice.repos.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CalculateMetricsService {

    @Autowired
    DashboardRepository dashboardRepository;

    @Autowired
    CalculateZephyrMetrics calculateZephyrMetrics;

    @Autowired
    CalculateRallyMetrics calculateRallyMetrics;

    @Autowired
    CalculateBotsDefectsMetrics calculateBotsDefectsMetrics;

    @Autowired
    CalculateServiceNowIncidents calculateServiceNowIncidents;

    @Autowired
    CalculateJiraMetrics calculateJiraMetrics;

    @Autowired
    CalculateALMMetric calculateALMMetricService;

    @Autowired
    CalculateXrayMetrics calculateXrayMetrics;

    @Autowired
    CalculateJenkinsMetrics calculateJenkinsMetrics;


    @Autowired
    CalculateGitMetrics calculateGitMetrics;

    @Autowired
    CalculateZephyrScaleMetrics calculateZephyrScaleMetrics;

    @Autowired
    MetricsRepository metricsRepository;

    @Autowired
    MetricConfigRepository metricConfigRepository;

    @Autowired
    SchedulerRunsRepository schedulerRunsRepository;

    @Autowired
    CIQDashboardProjectRepository projectRepository;

    @Autowired
    LOBRepository lobRepository;

    @Autowired
    OrganizationRepository organizationRepository;

    public MetricResponse calculateMetrics(ProjectMetric projectMetric) throws Exception {

        //Optional<Dashboard> dashboard = dashboardRepository.findByProjectNameAndName(projectMetric.getProjectName(), projectMetric.getDashboardName());
        System.out.println("Layer id - "+projectMetric.getLayerId()+" LayerName - "+projectMetric.getCategory()+" dashboardId - "+projectMetric.getDashboardId());
        Optional<Dashboard> dashboard = dashboardRepository.findByIdAndProjectId(projectMetric.getDashboardId(), projectMetric.getLayerId());

        if (dashboard.isPresent()) {

            System.out.println("Dashboard exists with the name : "+dashboard.get().getName());
            Dashboard dashboardResult = dashboard.get();

            List<MetricResults> metrics = new ArrayList<>();
            MetricResponse metricResponse = new MetricResponse();
            List<CIQDashboardProject> projectsLinked = null;

            //Get linked projects for the org/lob
            if(projectMetric.getCategory().equalsIgnoreCase("org")){
                Optional<Organization> organization = organizationRepository.findById(projectMetric.getLayerId());
                if(organization.isPresent()){
                    projectsLinked = projectRepository.findByOrgId(projectMetric.getLayerId());
                }
            }
            else if(projectMetric.getCategory().equalsIgnoreCase("lob")){
                Optional<LOB> lob = lobRepository.findById(projectMetric.getLayerId());
                if(lob.isPresent()){
                    projectsLinked = projectRepository.findByLobId(projectMetric.getLayerId());
                }
            }

            // Setting linked project names to each tool
            if(projectMetric.getCategory().equalsIgnoreCase("org") || projectMetric.getCategory().equalsIgnoreCase("lob")){
                if(projectsLinked!=null && projectsLinked.size()>0){
                    HashMap<String,List<String>> projectLinkedToEachTool = new HashMap<>();
                    projectLinkedToEachTool  = getProjectsLinkedToOrgOrLob(projectsLinked);
                    System.out.println("Projects linked "+projectLinkedToEachTool);
                    if(projectLinkedToEachTool.containsKey("jira")){
                        projectMetric.setJiraProjects(projectLinkedToEachTool.get("jira").stream().distinct().collect(Collectors.toList()));
                    }
                    if(projectLinkedToEachTool.containsKey("zephyrscale")){
                        projectMetric.setZephyrScaleProjects(projectLinkedToEachTool.get("zephyrscale").stream().distinct().collect(Collectors.toList()));
                    }
                    if(projectLinkedToEachTool.containsKey("alm")){
                        projectMetric.setAlmProjects(projectLinkedToEachTool.get("alm").stream().distinct().collect(Collectors.toList()));
                    }
                    if(projectLinkedToEachTool.containsKey("zephyr")){
                        projectMetric.setZephyrProjects(projectLinkedToEachTool.get("zephyr").stream().distinct().collect(Collectors.toList()));
                    }
                    if(projectLinkedToEachTool.containsKey("rally")){
                        projectMetric.setRallyProjects(projectLinkedToEachTool.get("rally").stream().distinct().collect(Collectors.toList()));
                    }
                    if(projectLinkedToEachTool.containsKey("service")){
                        projectMetric.setServiceNowIncidentProjects(projectLinkedToEachTool.get("service").stream().distinct().collect(Collectors.toList()));
                    }
                    if(projectLinkedToEachTool.containsKey("bots")){
                        projectMetric.setBotsDefectsProjects(projectLinkedToEachTool.get("bots").stream().distinct().collect(Collectors.toList()));
                    }
                    if(projectLinkedToEachTool.containsKey("xray")){
                        projectMetric.setXrayProjects(projectLinkedToEachTool.get("xray").stream().distinct().collect(Collectors.toList()));
                    }
                    if(projectLinkedToEachTool.containsKey("jenkins")){
                        projectMetric.setJenkinsProjects(projectLinkedToEachTool.get("jenkins").stream().distinct().collect(Collectors.toList()));
                    }
                    if(projectLinkedToEachTool.containsKey("git")){
                        projectMetric.setGitProjects(projectLinkedToEachTool.get("git").stream().distinct().collect(Collectors.toList()));
                    }
                }
            }
            // Set projectNames present in PROJECT source tools
            else if(projectMetric.getCategory().equalsIgnoreCase("prj")){

                Optional<CIQDashboardProject> project = projectRepository.findById(projectMetric.getLayerId());
                if (project.isPresent() && project.get().getSourceTools() != null) {
                    System.out.println("Source tools projects :"+project.get().getSourceTools());
                    for (int i = 0; i < project.get().getSourceTools().size(); i++) {
                        Map<String, List<String>> obj = (Map<String, List<String>>) project.get().getSourceTools().get(i);
                        if (String.valueOf(obj.get("toolName")).toLowerCase().contains("jira")) {
                            projectMetric.setJiraProjects(obj.get("projectNames"));
                        }
                        if (String.valueOf(obj.get("toolName")).toLowerCase().contains("alm")) {
                            projectMetric.setAlmProjects(obj.get("projectNames"));
                        }
                        if (String.valueOf(obj.get("toolName")).toLowerCase().contains("zephyr") && !String.valueOf(obj.get("toolName")).toLowerCase().contains("zephyrscale")) {
                            projectMetric.setZephyrProjects(obj.get("projectNames"));
                        }
                        if (String.valueOf(obj.get("toolName")).toLowerCase().contains("rally")) {
                            projectMetric.setRallyProjects(obj.get("projectNames"));
                        }
                        if (String.valueOf(obj.get("toolName")).toLowerCase().contains("service")) {
                            projectMetric.setServiceNowIncidentProjects(obj.get("projectNames"));
                        }
                        if (String.valueOf(obj.get("toolName")).toLowerCase().contains("bots")) {
                            projectMetric.setBotsDefectsProjects(obj.get("projectNames"));
                        }
                        if (String.valueOf(obj.get("toolName")).toLowerCase().contains("xray")) {
                            projectMetric.setXrayProjects(obj.get("projectNames"));
                        }
                        if (String.valueOf(obj.get("toolName")).toLowerCase().contains("jenkins")) {
                            projectMetric.setJenkinsProjects(obj.get("projectNames"));
                        }
                        if (String.valueOf(obj.get("toolName")).toLowerCase().contains("git")) {
                            projectMetric.setGitProjects(obj.get("projectNames"));
                        }
                        if (String.valueOf(obj.get("toolName")).toLowerCase().contains("zephyrscale")) {
                            projectMetric.setZephyrScaleProjects(obj.get("projectNames"));
                        }
                    }
                }
                else{
                    System.out.println("Project not exists / Project Source tools is null");
                }
            }

            /*IDPageConfig selectedPage = dashboard.get().getPages().stream()
                    .filter(page -> projectMetric.getPageName().equals(page.getName()))
                    .findAny()
                    .orElseThrow(() -> new Exception("Page not found!!"));*/

            //System.out.println("Page exists with name : "+ selectedPage.getName());

            if(dashboard.get().getPages()!=null && dashboard.get().getPages().size()>0) {
                for (IDPageConfig page : dashboard.get().getPages()) {

                    List<IDItemConfig> metricItemList = new ArrayList<>();
                    System.out.println("PAGE NAME : "+page.getName());
                    for (IDItemConfig item : page.getItems()) {
                        if ("derived".equalsIgnoreCase(item.getMetricCategory())) {
                            metricItemList.add(item);
                        }
                    }

                    //System.out.println("ProjectMetric --------------- " + projectMetric);
                    for (IDItemConfig item : metricItemList) {

                        System.out.println("Calculation for the chart item : "+item.getId());
                        Optional<MetricConfig> metricConfig = metricConfigRepository.findByMetricName(item.getMetricName());

                        if (metricConfig.isPresent()) {
                            System.out.println("metricConfig --- *** --- " + metricConfig.get());

                            // Set custom function field values
                            projectMetric.setCustomFunction(metricConfig.get().getCustomFunction());
                            projectMetric.setCustomFunctionName(metricConfig.get().getCustomFunctionName());
                            projectMetric.setCustomParams(metricConfig.get().getCustomParams());

                            // Set grouping calculation field values
                            projectMetric.setGroupBy(metricConfig.get().getGroupBy());
                            projectMetric.setGrouping(metricConfig.get().getGrouping());
                            projectMetric.setGroupValue(metricConfig.get().getGroupValue());

                            // Set trending calculation field values
                            projectMetric.setTrendBy(metricConfig.get().getTrendBy());
                            projectMetric.setTrending(metricConfig.get().getTrending());
                            projectMetric.setTrendingField(metricConfig.get().getTrendingField());
                            projectMetric.setTrendCount(metricConfig.get().getTrendCount());

                            // Set metric formula calculation field values
                            projectMetric.setFormula(metricConfig.get().getFormula());
                            projectMetric.setFormulaParams(metricConfig.get().getFormulaParams());

                            // Set chart item id & tool name
                            projectMetric.setItemId(item.getId());
                            projectMetric.setToolName(metricConfig.get().getToolType());

                            //projectMetric.setPageName(selectedPage.getName());

                            System.out.println("ProjectMetric after setting metric config data--- *** --- " + projectMetric);
                            System.out.println("Metric tool ** " + metricConfig.get().getToolType().toLowerCase());

                            switch (metricConfig.get().getToolType().toLowerCase()) {
                                case "jira":
                                    System.out.println("***@@@ JIRA @@@***");
                                    try {
                                        boolean isRequired = isCalculationRequired(item.getMetricName(), projectMetric, "Jira");
                                        if (isRequired) {
                                            MetricResults metricsResult = calculateJiraMetrics.calculateJiraMetrics(item.getMetricName(), projectMetric, "JiraIssue");
                                            metrics.add(metricsResult);
                                            dashboardResult.setMetrics(metrics);
                                            metricResponse.setMessage("Calculation done with new data");
                                            metricResponse.setData(dashboardResult);
                                            metricResponse.setCalculated(true);
                                        } else {
                                            metricResponse.setMessage("No Calculation required");
                                            metricResponse.setData(null);
                                            metricResponse.setCalculated(false);
                                        }
                                    } catch (Exception e) {
                                        System.err.println(e);
                                        metricResponse.setMessage("Some Exception occurred");
                                        metricResponse.setData(null);
                                        metricResponse.setCalculated(false);
                                    }
                                    break;
                                case "alm":
                                    System.out.println("***@@@ ALM @@@***");
                                    try {
                                        boolean isRequired = isCalculationRequired(item.getMetricName(), projectMetric, "ALM");
                                        if (isRequired) {
                                            MetricResults metricsResult = calculateALMMetricService.calculateALMMetric(item.getMetricName(), projectMetric, "SourceAlmAssets");
                                            metrics.add(metricsResult);
                                            dashboardResult.setMetrics(metrics);
                                            metricResponse.setMessage("Calculation done with new data");
                                            metricResponse.setData(dashboardResult);
                                            metricResponse.setCalculated(true);
                                        } else {
                                            metricResponse.setMessage("No Calculation required");
                                            metricResponse.setData(null);
                                            metricResponse.setCalculated(false);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        metricResponse.setMessage("Some Exception occurred " + e.getStackTrace());
                                        metricResponse.setData(null);
                                        metricResponse.setCalculated(false);
                                    }
                                    break;
                                case "zephyr":
                                    System.out.println("***@@@ ZEPHYR @@@***");
                                    try {
                                        boolean isRequired = isCalculationRequired(item.getMetricName(), projectMetric, "Zephyr");
                                        if (isRequired) {
                                            MetricResults metricsResult = calculateZephyrMetrics.calculateZephyrMetrics(item.getMetricName(), projectMetric, "SourceZephyrData");
                                            metrics.add(metricsResult);
                                            dashboardResult.setMetrics(metrics);
                                            metricResponse.setMessage("Calculation done with new data");
                                            metricResponse.setData(dashboardResult);
                                            metricResponse.setCalculated(true);
                                        } else {
                                            metricResponse.setMessage("No Calculation required");
                                            metricResponse.setData(null);
                                            metricResponse.setCalculated(false);
                                        }
                                    } catch (Exception e) {
                                        System.err.println(e);
                                        metricResponse.setMessage("Some Exception occurred");
                                        metricResponse.setData(null);
                                        metricResponse.setCalculated(false);
                                    }
                                    break;
                                case "rally":
                                    System.out.println("***@@@ RALLY @@@***");
                                    try {
                                        boolean isRequired = isCalculationRequired(item.getMetricName(), projectMetric, "Rally");
                                        if (isRequired) {
                                            MetricResults metricsResult = calculateRallyMetrics.calculateRallyMetrics(item.getMetricName(), projectMetric, "SourceRallyData");
                                            metrics.add(metricsResult);
                                            dashboardResult.setMetrics(metrics);
                                            metricResponse.setMessage("Calculation done with new data");
                                            metricResponse.setData(dashboardResult);
                                            metricResponse.setCalculated(true);
                                        } else {
                                            metricResponse.setMessage("No Calculation required");
                                            metricResponse.setData(null);
                                            metricResponse.setCalculated(false);
                                        }
                                    } catch (Exception e) {
                                        System.err.println(e);
                                        metricResponse.setMessage("Some Exception occurred");
                                        metricResponse.setData(null);
                                        metricResponse.setCalculated(false);
                                    }
                                    break;
                                case "git":
                                    try {
                                        System.out.println("***@@@ GIT @@@***");
                                        projectMetric.setItemId(item.getId());
                                        //projectMetric.setPageName(selectedPage.getName());
                                        boolean isRequired = isCalculationRequired(item.getMetricName(), projectMetric, "Git");
                                        if (isRequired) {
                                            MetricResults metricsResult = calculateGitMetrics.calculateGitMetrics(item.getMetricName(), projectMetric, "SourceGitData");
                                            metrics.add(metricsResult);
                                            dashboardResult.setMetrics(metrics);
                                            metricResponse.setMessage("Calculation done with new data");
                                            metricResponse.setData(dashboardResult);
                                            metricResponse.setCalculated(true);
                                        } else {
                                            metricResponse.setMessage("No Calculation required");
                                            metricResponse.setData(null);
                                            metricResponse.setCalculated(false);
                                        }
                                    } catch (Exception e) {
                                        System.err.println(e);
                                        metricResponse.setMessage("Some Exception occurred");
                                        metricResponse.setData(null);
                                        metricResponse.setCalculated(false);
                                    }
                                    break;
                                case "service":
                                    System.out.println("***@@@ SERVICE NOW @@@***");
                                    try {
                                        boolean isRequired = isCalculationRequired(item.getMetricName(), projectMetric, "Rally");
                                        if (isRequired) {
                                            MetricResults metricsResult = calculateServiceNowIncidents.calculateServiceNowIncidentMetrics(item.getMetricName(), projectMetric, "SourceServiceNowIncidents");
                                            metrics.add(metricsResult);
                                            dashboardResult.setMetrics(metrics);
                                            metricResponse.setMessage("Calculation done with new data");
                                            metricResponse.setData(dashboardResult);
                                            metricResponse.setCalculated(true);
                                        } else {
                                            metricResponse.setMessage("No Calculation required");
                                            metricResponse.setData(null);
                                            metricResponse.setCalculated(false);
                                        }
                                    } catch (Exception e) {
                                        System.err.println(e);
                                        metricResponse.setMessage("Some Exception occurred");
                                        metricResponse.setData(null);
                                        metricResponse.setCalculated(false);
                                    }
                                    break;
                                case "bots":
                                    System.out.println("***@@@ BOTS @@@***");
                                    try {
                                        boolean isRequired = isCalculationRequired(item.getMetricName(), projectMetric, "bots");
                                        if (isRequired) {
                                            MetricResults metricsResult = calculateBotsDefectsMetrics.calculateBotsMetrics(item.getMetricName(), projectMetric, "SourceBotsDefects");
                                            metrics.add(metricsResult);
                                            dashboardResult.setMetrics(metrics);
                                            metricResponse.setMessage("Calculation done with new data");
                                            metricResponse.setData(dashboardResult);
                                            metricResponse.setCalculated(true);
                                        } else {
                                            metricResponse.setMessage("No Calculation required");
                                            metricResponse.setData(null);
                                            metricResponse.setCalculated(false);
                                        }
                                    } catch (Exception e) {
                                        System.err.println(e);
                                        metricResponse.setMessage("Some Exception occurred");
                                        metricResponse.setData(null);
                                        metricResponse.setCalculated(false);
                                    }

                                    break;
                                case "xray":
                                    System.out.println("***@@@ XRAY @@@***");
                                    try {
                                        boolean isRequired = isCalculationRequired(item.getMetricName(), projectMetric, "xray");
                                        if (isRequired) {
                                            MetricResults metricsResult = calculateXrayMetrics.calculateXrayMetrics(item.getMetricName(), projectMetric, "SourceXrayTests");
                                            metrics.add(metricsResult);
                                            dashboardResult.setMetrics(metrics);
                                            metricResponse.setMessage("Calculation done with new data");
                                            metricResponse.setData(dashboardResult);
                                            metricResponse.setCalculated(true);
                                        } else {
                                            metricResponse.setMessage("No Calculation required");
                                            metricResponse.setData(null);
                                            metricResponse.setCalculated(false);
                                        }
                                    } catch (Exception e) {
                                        System.err.println(e);
                                        metricResponse.setMessage("Some Exception occurred");
                                        metricResponse.setData(null);
                                        metricResponse.setCalculated(false);
                                    }

                                    break;
                                case "jenkins":
                                    System.out.println("***@@@ JENKINS @@@***");
                                    try {
                                        boolean isRequired = isCalculationRequired(item.getMetricName(), projectMetric, "jenkins");
                                        if (isRequired) {
                                            MetricResults metricsResult = calculateJenkinsMetrics.calculateJenkinsMetrics(item.getMetricName(), projectMetric, "SourceJenkinsBuild");
                                            metrics.add(metricsResult);
                                            dashboardResult.setMetrics(metrics);
                                            metricResponse.setMessage("Calculation done with new data");
                                            metricResponse.setData(dashboardResult);
                                            metricResponse.setCalculated(true);
                                        } else {
                                            metricResponse.setMessage("No Calculation required");
                                            metricResponse.setData(null);
                                            metricResponse.setCalculated(false);
                                        }
                                    } catch (Exception e) {
                                        System.err.println(e);
                                        metricResponse.setMessage("Some Exception occurred");
                                        metricResponse.setData(null);
                                        metricResponse.setCalculated(false);
                                    }

                                    break;
                                case "zephyrscale":
                                    System.out.println("***@@@ ZEPHYR SCALE @@@***");
                                    try {
                                        boolean isRequired = isCalculationRequired(item.getMetricName(), projectMetric, "ZephyrScale");
                                        if (isRequired) {
                                            MetricResults metricsResult = calculateZephyrScaleMetrics.calculateZephyrScaleMetrics(item.getMetricName(), projectMetric, "SourceZephyrScaleData");
                                            metrics.add(metricsResult);
                                            dashboardResult.setMetrics(metrics);
                                            metricResponse.setMessage("Calculation done with new data");
                                            metricResponse.setData(dashboardResult);
                                            metricResponse.setCalculated(true);
                                        } else {
                                            metricResponse.setMessage("No Calculation required");
                                            metricResponse.setData(null);
                                            metricResponse.setCalculated(false);
                                        }
                                    } catch (Exception e) {
                                        System.err.println(e);
                                        metricResponse.setMessage("Some Exception occurred");
                                        metricResponse.setData(null);
                                        metricResponse.setCalculated(false);
                                    }
                                    break;
                                default:
                                    System.err.println("No Tool found with name *** " + metricConfig.get().getToolType().toLowerCase());
                            }
                        }
                        System.out.println("*********************");
                    }
                }
            }
            return metricResponse;
        } else {
            throw new Exception("dashboard not exists");
        }
    }

    public HashMap<String, List<String>> getProjectsLinkedToOrgOrLob(List<CIQDashboardProject> projectsLinked){
        HashMap<String ,List<String>> data = new HashMap<>();
        for (CIQDashboardProject ciqDashboardProject : projectsLinked) {
            if (ciqDashboardProject.getSourceTools() != null) {
                for (int i = 0; i < ciqDashboardProject.getSourceTools().size(); i++) {
                    Map<String, List<String>> obj = (Map<String, List<String>>) ciqDashboardProject.getSourceTools().get(i);

                    if (String.valueOf(obj.get("toolName")).toLowerCase().contains("jira")) {
                        if(data.containsKey("jira")) {
                            data.get("jira").addAll(obj.get("projectNames"));
                        }
                        else{
                            data.put("jira",obj.get("projectNames"));
                        }
                    }
                    if (String.valueOf(obj.get("toolName")).toLowerCase().contains("alm")) {
                        if(data.containsKey("alm")) {
                            data.get("alm").addAll(obj.get("projectNames"));
                        }
                        else{
                            data.put("alm",obj.get("projectNames"));
                        }
                    }
                    if (String.valueOf(obj.get("toolName")).toLowerCase().contains("zephyr") && !String.valueOf(obj.get("toolName")).toLowerCase().contains("zephyrscale")) {
                        if(data.containsKey("zephyr")) {
                            data.get("zephyr").addAll(obj.get("projectNames"));
                        }
                        else{
                            data.put("zephyr",obj.get("projectNames"));
                        }
                    }
                    if (String.valueOf(obj.get("toolName")).toLowerCase().contains("rally")) {
                        if(data.containsKey("rally")) {
                            data.get("rally").addAll(obj.get("projectNames"));
                        }
                        else{
                            data.put("rally",obj.get("projectNames"));
                        }
                    }
                    if (String.valueOf(obj.get("toolName")).toLowerCase().contains("service")) {
                        if(data.containsKey("service")) {
                            data.get("service").addAll(obj.get("projectNames"));
                        }
                        else{
                            data.put("service",obj.get("projectNames"));
                        }
                    }
                    if (String.valueOf(obj.get("toolName")).toLowerCase().contains("bots")) {
                        if(data.containsKey("bots")) {
                            data.get("bots").addAll(obj.get("projectNames"));
                        }
                        else{
                            data.put("bots",obj.get("projectNames"));
                        }
                    }
                    if (String.valueOf(obj.get("toolName")).toLowerCase().contains("xray")) {
                        if(data.containsKey("xray")) {
                            data.get("xray").addAll(obj.get("projectNames"));
                        }
                        else{
                            data.put("xray",obj.get("projectNames"));
                        }
                    }
                    if (String.valueOf(obj.get("toolName")).toLowerCase().contains("jenkins")) {
                        if(data.containsKey("jenkins")) {
                            data.get("jenkins").addAll(obj.get("projectNames"));
                        }
                        else{
                            data.put("jenkins",obj.get("projectNames"));
                        }
                    }
                    if (String.valueOf(obj.get("toolName")).toLowerCase().contains("git")) {
                        if(data.containsKey("git")) {
                            data.get("git").addAll(obj.get("projectNames"));
                        }
                        else{
                            data.put("git",obj.get("projectNames"));
                        }
                    }
                    if (String.valueOf(obj.get("toolName")).toLowerCase().contains("zephyrscale")) {
                        if(data.containsKey("zephyrscale")) {
                            data.get("zephyrscale").addAll(obj.get("projectNames"));
                        }
                        else{
                            data.put("zephyrscale",obj.get("projectNames"));
                        }
                    }
                }
            }
        }
         return data;
    }

    public JSONObject getLastCalculatedMetricsForTool(String layerId, String dashboardId, String category) throws Exception {

        JSONObject calculatedMetrics = new JSONObject();
        List<CollectorRunScheduler> collectorRunSchedulers = schedulerRunsRepository.findAll();
        //List<MetricResults> metricResults = metricsRepository.findByProjectNameAndDashboardNameAndPageName(projectName, dashboardName, pageName);

        List<MetricResults> metricResults = metricsRepository.findByLayerIdAndDashboardId(layerId, dashboardId);

        if (!collectorRunSchedulers.isEmpty()) {
            calculatedMetrics.put("toolTypes", collectorRunSchedulers);
        } else {
            throw new Exception("Collector run for tools not found");
        }


        if (!metricResults.isEmpty()) {
//            List<String> lastCalculatedMetrics = metricResults.stream()
//                    .flatMap(p -> Stream.of("toolName : "+p.getToolType(),"dashboardName : "+ p.getDashboardName(), "lastCalculated : "+p.getLastCalculatedDate().toString()))
//                    .collect(Collectors.toList());
            List<JSONObject> lastCalculatedMetricsList = new ArrayList<>();
            for (MetricResults res : metricResults) {
                JSONObject calculatedMetricObj = new JSONObject();
                calculatedMetricObj.put("toolName", res.getToolType());
                //calculatedMetricObj.put("dashboardName", res.getDashboardName());
                calculatedMetricObj.put("dashboardId", res.getDashboardId());
                calculatedMetricObj.put("lastCalculated", res.getLastCalculatedDate());
                //calculatedMetricObj.put("pageName", res.getPageName());
                //calculatedMetricObj.put("projectName", res.getProjectName());
                calculatedMetricObj.put("layerId", res.getLayerId());
                calculatedMetricObj.put("layerName", res.getLayerName());

                lastCalculatedMetricsList.add(calculatedMetricObj);
            }
            calculatedMetrics.put("lastCalculatedDate", lastCalculatedMetricsList);
        } else {
            calculatedMetrics.put("lastCalculated", "");
        }

        System.out.println("calculatedMetrics --- " + calculatedMetrics);
        return calculatedMetrics;

    }

    public boolean isCalculationRequired(String metricName, ProjectMetric projectMetric, String toolName) {
        //System.out.println("metricName - " + metricName + " - dashboardName - " + projectMetric.getDashboard() + " - itemId - " + projectMetric.getItemId());
        System.out.println("metricName - " + metricName + " - dashboardId - " + projectMetric.getDashboardId() + " - itemId - " + projectMetric.getItemId());

        //Optional<MetricResults> metricResults = metricsRepository.findByMetricNameAndDashboardNameAndItemId(metricName, projectMetric.getDashboardName(), projectMetric.getItemId());
        Optional<MetricResults> metricResults = metricsRepository.findByMetricNameAndItemIdAndDashboardIdAndLayerId(metricName, projectMetric.getItemId(), projectMetric.getDashboardId(), projectMetric.getLayerId());
        Optional<CollectorRunScheduler> collectorRunScheduler = schedulerRunsRepository.findByToolNameIgnoreCase(toolName);

        boolean requireCalculation = true;
        if (collectorRunScheduler.isPresent() && metricResults.isPresent()) {
            System.out.println(metricResults.get() + " ^^^^^^ " + collectorRunScheduler.get());

            Instant lastCollectionUpdated = collectorRunScheduler.get().getLastUpdatedDate();
            Instant lastMetricCalculated = metricResults.get().getLastCalculatedDate();

            System.out.println("Check metric calculation happened before or after the updated collections");
            System.out.println("last collections updated "+ lastCollectionUpdated);
            System.out.println("last metric calculated "+ lastMetricCalculated);

            requireCalculation = lastCollectionUpdated.isAfter(lastMetricCalculated);

        } else if (!collectorRunScheduler.isPresent()) {
            System.out.println("collectorRunScheduler is not present for the metric tool- " + toolName);
            requireCalculation = false;

        } else if (!metricResults.isPresent()) {
            System.out.println("metricResults present : " + metricResults.isPresent());
            requireCalculation = true;
        }
        System.out.println("Is calculation required : "+ requireCalculation);
        return requireCalculation;
    }

}
