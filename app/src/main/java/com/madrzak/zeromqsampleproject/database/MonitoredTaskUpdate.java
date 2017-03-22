
package com.madrzak.zeromqsampleproject.database;

import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * https://test-v5.over-c.net/javadoc/net/overc/monitor/bean/output/MonitorOutputBean.html
 */
@Getter
@Setter
public class MonitoredTaskUpdate {
    public final static String STRING_DATE_FORMAT =  "MMM d, yyyy h:mm:ss aaa";


    private Long monitorId;
    private List<Period> periods = new ArrayList<>();
    private List<Task> tasks = new ArrayList<>();


    private static transient Gson gson = new GsonBuilder()
            .setDateFormat(STRING_DATE_FORMAT)
            .setPrettyPrinting()
            .create();

    @Nullable
    public static MonitoredTaskUpdate fromJson(String json){
        try{
            return gson.fromJson(json, MonitoredTaskUpdate.class);
        } catch (Exception e){
            return null;
        }
    }

}
