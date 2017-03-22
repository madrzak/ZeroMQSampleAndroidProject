package com.madrzak.zeromqsampleproject.database;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by New User on 29/12/2016.
 */
@Getter
@Setter
public class ScannedSensor {
    Date date;
    Long sensorId;
    Long userId;
}
