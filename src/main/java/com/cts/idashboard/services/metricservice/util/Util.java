
/*
 *    © [2021] Cognizant. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http:www.apache.orglicensesLICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package com.cts.idashboard.services.metricservice.util;

import com.cts.idashboard.services.metricservice.data.Cycle;
import com.cts.idashboard.services.metricservice.data.Release;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.time.zone.ZoneRulesException;
import java.util.Date;

/*
 * Util
 *
 * @author Cognizant
 */

@Slf4j
public class Util {
    private Util() {
    }

    public static String getUniqueId(String projectId, String componentId){
        return projectId+"P"+componentId;
    }

    public static Cycle getDefaultCycle(String domainName, String projectName, String releaseId){
        Cycle cycle = new Cycle();
        cycle.setCycleId("0000");
        cycle.setCycleName("DEFAULT");
        cycle.setDomainName(domainName);
        cycle.setProjectName(projectName);
        cycle.setReleaseId(releaseId);
        return cycle;
    }

    public static Release getDefaultRelease(String domainName, String projectName) {
        Release defaultRelease = new Release();
        defaultRelease.setReleaseId("0000");
        defaultRelease.setReleaseName("DEFAULT");
        defaultRelease.setDomainName(domainName);
        defaultRelease.setProjectName(projectName);
        return defaultRelease;
    }

    public static Date getDateFromString(String strDate) {
        if (StringUtils.isEmpty(strDate)) return null;
        try {
            if (strDate.length() == 10) {
                return Date.from(LocalDate.parse(strDate).atStartOfDay(ZoneId.systemDefault()).toInstant());
            } else if (strDate.length() == 19) {
                if (strDate.contains(" ")) strDate = strDate.replace(" ", "T");
                return Date.from(LocalDateTime.parse(strDate).atZone(ZoneId.systemDefault()).toInstant());
            }
        } catch (DateTimeParseException e) {
            log.error("Error while parsing");
        }
        catch (ZoneRulesException e){
            log.error("Error while parsing");
        }
        return null;
    }

    public static LocalTime getTimeFromString(String strTime) {
        try {
            return LocalTime.parse(strTime);
        } catch (DateTimeParseException e) {
            log.error("Error while parsing");
        }
        return null;
    }
}
