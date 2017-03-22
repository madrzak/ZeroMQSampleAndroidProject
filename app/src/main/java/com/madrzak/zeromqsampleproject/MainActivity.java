package com.madrzak.zeromqsampleproject;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.madrzak.zeromqsampleproject.adapter.MessageListAdapter;
import com.madrzak.zeromqsampleproject.database.MonitoredTaskUpdate;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getSimpleName();

    private String ADDRESS = "tcp://test-v5.over-c.net:8571";
    private String TOPIC = "A";
    private DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy HH:mm:ss");

    @BindView(R.id.list)
    ListView lvList;

    @BindView(R.id.killButton)
    Button killButton;

    MessageListAdapter adapter;
    List<String> list = new ArrayList<>();

    Thread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final LayoutInflater factory = getLayoutInflater();
        final View view = factory.inflate(R.layout.activity_main, null);
        setContentView(view);

        ButterKnife.bind(this, view);

        adapter = new MessageListAdapter(this, R.id.list, list);
        lvList.setAdapter(adapter);

        thread = new Thread(() -> {
            ZContext zContext = new ZContext();
            ZMQ.Socket socket = zContext.createSocket(ZMQ.SUB);
            socket.connect(ADDRESS);

            socket.subscribe(TOPIC.getBytes());

            while (true && !Thread.interrupted()) {
                String t = socket.recvStr();
                String msg = socket.recvStr();
                Log.i(TAG, "Msg received: " + t + " - " + msg);
                processMsg(msg);
            }
        });

        killButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                thread.interrupt();
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (!thread.isAlive()) {
            thread.start();
        } else {
            Log.w(TAG, "Thread already running");

        }
    }

    private void processMsg(String json) {

        try {
            MonitoredTaskUpdate taskUpdate = MonitoredTaskUpdate.fromJson(json);
            if(taskUpdate != null) {
                String item = dateFormat.format(new Date()) + "\n" +
                        taskUpdate.getMonitorId() + "\n" +
                        taskUpdate.getPeriods().get(0).getEndDate();
                list.add(item);

                runOnUiThread(() -> {
                    adapter.refresh(list);
                });
            }

        } catch (Throwable e) {
            Log.e(TAG, e.toString());
        }
    }
}
