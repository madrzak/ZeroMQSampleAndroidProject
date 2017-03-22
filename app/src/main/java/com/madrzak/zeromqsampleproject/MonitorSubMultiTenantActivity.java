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
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Łukasz on 21/03/2017.
 */

public class MonitorSubMultiTenantActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getSimpleName();

    private String ADDRESS = "tcp://172.16.88.126:5556";
    private String TOPIC = "";
    private DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy HH:mm:ss");

    @BindView(R.id.list)
    ListView lvList;

    @BindView(R.id.btnTenant1)
    Button btnTenant1;

    @BindView(R.id.btnTenant2)
    Button btnTenant2;

    MonitorUpdateListAdapter adapter;
    List<DummyMonitorUpdate> list = new ArrayList<>();

    Thread thread;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final LayoutInflater factory = getLayoutInflater();
        final View view = factory.inflate(R.layout.activity_multi_tenant, null);
        setContentView(view);

        ButterKnife.bind(this, view);

        adapter = new MonitorUpdateListAdapter(this, R.id.list, list);
        lvList.setAdapter(adapter);
    }


    @Override
    protected void onResume() {
        super.onResume();

        final ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket subscriber = context.socket(ZMQ.SUB);

        thread = new Thread(() -> {
            //  Socket to talk to server
            System.out.println("Collecting updates from monitor server");

            subscriber.connect(ADDRESS);
            subscriber.subscribe(TOPIC.getBytes());

            while (!Thread.currentThread().interrupted()) {
                //  Use trim to remove the tailing '0' character

                try {
                    String string = subscriber.recvStr(0).trim();
                    StringTokenizer sscanf = new StringTokenizer(string, " ");
                    String publisher = String.valueOf(sscanf.nextToken());
                    Long monitorId = Long.valueOf(sscanf.nextToken());
                    Long time = Long.valueOf(sscanf.nextToken());

                    Date date = new Date(time);

                    System.out.println("got update: " + publisher + " " + monitorId + " " + dateFormat.format(date));
                    processMsg(publisher, monitorId, time);
                } catch (ZMQException e) {
                    Log.e(TAG, "killing zmq " + e.toString());
                    if (e.getErrorCode() == ZMQ.Error.ETERM.getCode()) {
                        Log.e(TAG, "killing zmq " + e.toString());
                        break;
                    }
                }
            }

            subscriber.close();

        });

        btnTenant1.setOnClickListener(view1 -> {
            Toast.makeText(MonitorSubMultiTenantActivity.this, "Changing tenant", Toast.LENGTH_SHORT).show();

            new Thread(() -> {

                System.out.println("W: interrupt received, killing server…");
                context.term();
                try {
                    thread.interrupt();
                    thread.join();
                } catch (InterruptedException e) {

                }

            }).start();
        });

        if (!thread.isAlive()) {
            thread.start();
        } else {
            Log.w(TAG, "Thread already running");
        }
    }

    private void processMsg(String publisher, Long monitorId, Long time) {
        DummyMonitorUpdate monitorUpdate = new DummyMonitorUpdate(monitorId, publisher, time);

        list.add(monitorUpdate);
        if (list.size() > 20) {
            list.remove(0);
        }

        runOnUiThread(() -> {
            adapter.refresh(list);
        });
    }

}

