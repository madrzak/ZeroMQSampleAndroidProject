package com.madrzak.zeromqsampleproject.database;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by Łukasz on 21/03/2017.
 */
@Getter
@Setter
public class DummyMonitorUpdate {
    private long monitorId;
    private String publisher;
    private long time;

    public DummyMonitorUpdate(long monitorId, String publisher, long time) {
        this.monitorId = monitorId;
        this.publisher = publisher;
        this.time = time;
    }
}
