package com.madrzak.zeromqsampleproject;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.madrzak.zeromqsampleproject.adapter.MonitorUpdateListAdapter;
import com.madrzak.zeromqsampleproject.database.DummyMonitorUpdate;

import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Łukasz on 21/03/2017.
 */

public class MonitorOneSubManyTopicsActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getSimpleName();

    //    private String ADDRESS = "tcp://172.16.88.126:5557";
    private final String TENANT_ONE = "tcp://192.168.1.6:5556";
    private final String TENANT_TWO = "tcp://192.168.1.6:5557";
    private final String[] tenants = new String[]{TENANT_ONE, TENANT_TWO};
    private final String TOPIC_M = "M";
    private final String TOPIC_R = "R";
    private final String TOPIC_A = "A";
    private final DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy HH:mm:ss");

    private int currentTenant = 0;

    @BindView(R.id.list)
    ListView lvList;

    @BindView(R.id.killButton)
    Button killButton;

    @BindView(R.id.startButton)
    Button startButton;

    @BindView(R.id.btnChangeTenant)
    Button btnChangeTenant;

    MonitorUpdateListAdapter adapter;
    List<DummyMonitorUpdate> list = new ArrayList<>();

    Thread thread;
    ZMQ.Context context;
    ZMQ.Socket subscriber;

    private Boolean socketRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final LayoutInflater factory = getLayoutInflater();
        final View view = factory.inflate(R.layout.activity_many_topics, null);
        setContentView(view);

        ButterKnife.bind(this, view);

        adapter = new MonitorUpdateListAdapter(this, R.id.list, list);
        lvList.setAdapter(adapter);


        killButton.setOnClickListener(view1 -> {
            if (!socketRunning) {
                Toast.makeText(MonitorOneSubManyTopicsActivity.this, "ZMQ not running", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(MonitorOneSubManyTopicsActivity.this, "Killing ZMQ", Toast.LENGTH_SHORT).show();
            killZmqSocket();
        });

        startButton.setOnClickListener(view1 -> {
            if (socketRunning) {
                Toast.makeText(MonitorOneSubManyTopicsActivity.this, "ZMQ already running", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(MonitorOneSubManyTopicsActivity.this, "Starting ZMQ Socket", Toast.LENGTH_SHORT).show();
            startSocket();
        });

        btnChangeTenant.setOnClickListener(view1 -> {
            Toast.makeText(MonitorOneSubManyTopicsActivity.this, "Changing tenant", Toast.LENGTH_SHORT).show();
            restartZmqSocket();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void processMsg(String msg) {

        StringTokenizer sscanf = new StringTokenizer(msg, " ");
        String publisher = String.valueOf(sscanf.nextToken());
        Long monitorId = Long.valueOf(sscanf.nextToken());
        Long time = Long.valueOf(sscanf.nextToken());

        DummyMonitorUpdate monitorUpdate = new DummyMonitorUpdate(monitorId, publisher, time);

        list.add(monitorUpdate);
        if (list.size() > 20) {
            list.remove(0);
        }

        runOnUiThread(() -> {
            adapter.refresh(list);
        });
    }

    private void startSocket() {
        socketRunning = true;
        context = ZMQ.context(1);
        thread = newZmqThread();

        thread.start();
    }

    private Thread newZmqThread() {
        return new Thread(() -> {
            //  Socket to talk to server
            System.out.println("Collecting updates from monitor server " + tenants[currentTenant%2]);
            subscriber = context.socket(ZMQ.SUB);

            subscriber.connect(tenants[currentTenant%2]);
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
                        //Log.e(TAG, "killing zmq " + e.toString());
                        break;
                    }
                }
            }

            subscriber.close();

        });
    }

    private void killZmqSocket() {
        new Thread(() -> {
            socketRunning = false;
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
                currentTenant++;
                startSocket();
            } catch (InterruptedException e) {

            }
        }).start();
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

