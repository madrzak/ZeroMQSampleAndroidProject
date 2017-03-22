package com.madrzak.zeromqsampleproject;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.madrzak.zeromqsampleproject.appevent.AppEvents;
import com.madrzak.zeromqsampleproject.database.DummyMonitorUpdate;

import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

import java.util.StringTokenizer;

public class WooMQService extends Service {
    protected final String TAG = WooMQService.class.getSimpleName();

    private final String TOPIC_M = "M";
    private final String TOPIC_R = "R";
    private final String TOPIC_A = "A";

    private LocalPreferences localPreferences;

    Thread thread;
    ZMQ.Context context;
    ZMQ.Socket subscriber;
    String tenant;

    public WooMQService() {
    }

    public IBinder onBind(Intent intent) {
        Log.v(TAG, "onBind");
        return null;
    }

    @Override
    public void onCreate() {

        super.onCreate();
        Log.v(TAG, "onCreate");

        tenant = LocalPreferences.getInstance(getApplicationContext()).getCurrentTenant();
        localPreferences = LocalPreferences.getInstance();
        startSocket();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        Log.v(TAG, "onStartCommand");

        if (!tenant.equals(localPreferences.getCurrentTenant())) {
            Log.v(TAG, "tenant changed - restart sockets");
            restartZmqSocket();
        }

        return super.onStartCommand(intent,flags, startId);

    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "onDestroy");
        super.onDestroy();

        killZmqSocket();
    }


    private void startSocket() {
        context = ZMQ.context(1);
        thread = newZmqThread();

        thread.start();
    }

    private Thread newZmqThread() {
        return new Thread(() -> {
            //  Socket to talk to server
            System.out.println("Collecting updates from monitor server " + tenant);
            subscriber = context.socket(ZMQ.SUB);

            subscriber.connect(tenant);
            subscriber.subscribe(TOPIC_M.getBytes());
            subscriber.subscribe(TOPIC_R.getBytes());
            subscriber.subscribe(TOPIC_A.getBytes());

            while (!Thread.currentThread().interrupted()) {
                try {
                    String t = subscriber.recvStr();
                    String msg = subscriber.recvStr();
//                Log.i(TAG, "Msg received: " + t + " - " + msg);

                    if (t.equals(TOPIC_A)) {
                        processAck(msg);
                    } else if (t.equals(TOPIC_M)) {
                        processMonitorUpdate(msg);
                    } else if (t.equals(TOPIC_R)) {
                        processRevision(msg);
                    }
                    processMsg(msg);
                } catch (ZMQException e) {
                    Log.e(TAG, "killing zmq " + e.toString());
                    if (e.getErrorCode() == ZMQ.Error.ETERM.getCode()) {
                        break;
                    }
                }
            }
            subscriber.close();
        });
    }

    private void killZmqSocket() {
        new Thread(() -> {
            System.out.println("W: interrupt received, killing server…");
            context.term();
            subscriber.close();
            try {
                thread.interrupt();
                thread.join();
            } catch (InterruptedException e) {

            }
        }).start();
    }

    private void restartZmqSocket() {
        new Thread(() -> {
            System.out.println("W: interrupt received, killing server…");
            context.term();
            subscriber.close();
            try {
                thread.interrupt();
                thread.join();
                tenant = localPreferences.getCurrentTenant();
                startSocket();
            } catch (InterruptedException e) {

            }
        }).start();
    }

    private void processMsg(String msg) {

        StringTokenizer sscanf = new StringTokenizer(msg, " ");
        String publisher = String.valueOf(sscanf.nextToken());
        Long monitorId = Long.valueOf(sscanf.nextToken());
        Long time = Long.valueOf(sscanf.nextToken());

        DummyMonitorUpdate monitorUpdate = new DummyMonitorUpdate(monitorId, publisher, time);
        AppEvents.instance().publishZmqEvent(monitorUpdate);
    }

    private void processAck(String json) {
        Log.i(TAG, "processAck: " + json);
    }

    private void processRevision(String json) {
        Log.i(TAG, "processRevision: " + json);
    }

    private void processMonitorUpdate(String json) {
        Log.i(TAG, "processMonitorUpdate: " + json);
    }

}
