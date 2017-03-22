
package com.madrzak.zeromqsampleproject.database;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * https://test-v5.over-c.net/javadoc/net/overc/monitor/bean/output/PeriodStateBean.html
 */
@Getter
@Setter
public class Period {

    private Long id;
    private Long monitorId;
    private Date startDate;
    private Date endDate;
    private Date intervalStartDate;
    private Date intervalEndDate;
    private PeriodStatus status;
    private Boolean securityPatrol = false;
    private List<Long> allSensors = new ArrayList<>();
    private List<ScannedSensor> scannedSensors = new ArrayList<>();
    private Integer averageScheduleComplete = 0;
    private Integer periodComplete = 0;

    boolean isRunning(Date date) {

        if (date.compareTo(startDate) < 0)
            return false;
        if (date.compareTo(endDate) >= 0)
            return false;

        return true;
    }

//    public boolean isRunning() {
//        return isRunning(new Date());
//    }
//
//    public boolean isSamePeriodAs(Period other) {
//        if (other == null) return false;
//        if (!startDate.equals(other.getStartDate())) return false;
//        if (!endDate.equals(other.getEndDate())) return false;
//        return true;
//    }
//
//    public String getLastActivity() {
//        if (scannedSensors.size() == 0) {
//            return "None";
//        } else {
//            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
//            return sdf.format(scannedSensors.get(scannedSensors.size() - 1).getDate());
//        }
//    }

}
