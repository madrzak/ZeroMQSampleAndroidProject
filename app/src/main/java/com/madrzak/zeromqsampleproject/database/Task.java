package com.madrzak.zeromqsampleproject.database;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by turlough on 04/12/16.
 */
@Getter
@Setter
public class Task {
    private Long id;
    private Long userId;
    private Date startDate;
    private Date endDate;
    private Long folderId;
    private Long periodId;
    private PeriodStatus status;
}
