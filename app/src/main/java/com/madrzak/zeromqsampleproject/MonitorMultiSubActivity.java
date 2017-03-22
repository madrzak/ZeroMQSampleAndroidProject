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

public class MonitorMultiSubActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getSimpleName();

//    private String ADDRESS = "tcp://172.16.88.126:5557";
    private String ADDRESS_1 = "tcp://192.168.1.6:5556";
    private String ADDRESS_2 = "tcp://192.168.1.6:5557";
    private String TOPIC = "";
    private DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy HH:mm:ss");

    @BindView(R.id.list)
    ListView lvList;

    @BindView(R.id.killButton)
    Button killButton;

    @BindView(R.id.startButton)
    Button startButton;

    MonitorUpdateListAdapter adapter;
    List<DummyMonitorUpdate> list = new ArrayList<>();

    ZMQ.Context context;

    Thread thread1;
    ZMQ.Socket subscriber1;

    Thread thread2;
    ZMQ.Socket subscriber2;

    private Boolean zmqRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final LayoutInflater factory = getLayoutInflater();
        final View view = factory.inflate(R.layout.activity_weather_sub, null);
        setContentView(view);

        ButterKnife.bind(this, view);

        adapter = new MonitorUpdateListAdapter(this, R.id.list, list);
        lvList.setAdapter(adapter);


        killButton.setOnClickListener(view1 -> {
            Toast.makeText(MonitorMultiSubActivity.this, "Killing ZMQ", Toast.LENGTH_SHORT).show();
            new Thread(() -> {
                if (!zmqRunning) {
                    return;
                }
                zmqRunning = false;
                System.out.println("W: interrupt received, killing server…");
                context.term();
                subscriber1.close();
                subscriber2.close();
                try {
                    thread1.interrupt();
                    thread1.join();
                    thread2.interrupt();
                    thread2.join();
                } catch (InterruptedException e) {

                }
            }).start();
        });

        startButton.setOnClickListener(view1 -> {
            Toast.makeText(MonitorMultiSubActivity.this, "Starting ZMQ Socket", Toast.LENGTH_SHORT).show();
            startSocket();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
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

    private void startSocket() {
        if (zmqRunning) {
            return;
        }
        zmqRunning = true;
        context = ZMQ.context(1);
        thread1 = zmqSub1();
        thread1.start();

        thread2 = zmqSub2();
        thread2.start();
    }

    private Thread zmqSub1() {
        return new Thread(() -> {
            //  Socket to talk to server
            System.out.println("Collecting updates from monitor server");
            subscriber1 = context.socket(ZMQ.SUB);

            subscriber1.connect(ADDRESS_1);
            subscriber1.subscribe(TOPIC.getBytes());

            while (!Thread.currentThread().interrupted()) {
                //  Use trim to remove the tailing '0' character
                try {
                    String string = subscriber1.recvStr(0).trim();
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

            subscriber1.close();

        });
    }
    private Thread zmqSub2() {
        return new Thread(() -> {
            //  Socket to talk to server
            System.out.println("Collecting updates from monitor server");
            subscriber2 = context.socket(ZMQ.SUB);

            subscriber2.connect(ADDRESS_2);
            subscriber2.subscribe(TOPIC.getBytes());

            while (!Thread.currentThread().interrupted()) {
                //  Use trim to remove the tailing '0' character
                try {
                    String string = subscriber2.recvStr(0).trim();
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

            subscriber2.close();

        });
    }
}

