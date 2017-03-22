package com.madrzak.zeromqsampleproject.appevent;

import com.madrzak.zeromqsampleproject.database.DummyMonitorUpdate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static rx.Observable.from;

/**
 * Created by Łukasz on 22/03/2017.
 */

public class AppEvents {
    private final static String TAG = AppEvents.class.getSimpleName();

    private static AppEvents instance = new AppEvents();

    private final Map<Class, ZmqEvent> zmq = new ConcurrentHashMap();

    public static AppEvents instance() {

        return instance;
    }

    public void subscribeToZmq(ZmqEvent subscriber) {

        zmq.put(subscriber.getClass(), subscriber);
    }

    public void unsubscribeFromZmq(ZmqEvent subscriber) {

        if (zmq.keySet().contains(subscriber.getClass())) {
            zmq.remove(subscriber.getClass());
        }
    }

    public void publishZmqEvent(DummyMonitorUpdate update) {
        from(zmq.values())
                .subscribe(subscriber -> new Thread(() -> subscriber.notify(update))
                        .start());
    }


}
