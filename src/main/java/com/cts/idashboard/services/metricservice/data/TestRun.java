
package com.cts.idashboard.services.metricservice.data;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalTime;
import java.util.Date;

@Data
@Document(collection = "source_alm_runs")
@CompoundIndex(name = "proj_run_index",
        def = "{'projectName': 1, 'testRunId': 1}",
        unique = true)
public class TestRun {
    @Id
    private String id;
    private String testRunId;
    private String testRunName;
    private String domainName;
    private String projectName;
    private long duration;
    private String cycle;
    private String path;
    private String attachment;
    private String host;
    private String draft;
    private String state;
    /*test-description*/
    private String testDescription;
    /*owner*/
    private String tester;
    private String comments;
    private String status;
    /*assign-rcyc*/
    private String assignedCycleId;
    private String assignedCycleName;
    /*Get Release from db*/
    private String releaseId;
    /*cycle-name*/
    private String testSetName;
    /*os-config*/
    private String osConfig;
    /*test-id*/
    private String testId;
    /*test-name*/
    private String testName;
    /*has-linkage*/
    private String hasLinkage;
    /*subtype-id*/
    private String subtypeId;
    /*cycle-id*/
    private String cycleId;
    /*ver-stamp*/
    private String verStamp;
    /*test-config-id*/
    private String testConfigId;
    /*test-instance*/
    private String testInstance;
    /*os-name*/
    private String osName;
    /*os-build*/
    private String osBuild;
    /*testcycl-name*/
    private String testCycleName;
    /*testcycl-id*/
    private String testCycleId;

    /*execution-date*/
    private Date executionDate;
    /*execution-time*/
    private LocalTime executionTime;
    /*last-modified*/
    private Date lastModified;
    private String environmentName;

}
