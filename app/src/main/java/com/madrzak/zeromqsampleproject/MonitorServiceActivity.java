package com.madrzak.zeromqsampleproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.madrzak.zeromqsampleproject.adapter.MonitorUpdateListAdapter;
import com.madrzak.zeromqsampleproject.appevent.AppEvents;
import com.madrzak.zeromqsampleproject.appevent.ZmqEvent;
import com.madrzak.zeromqsampleproject.database.DummyMonitorUpdate;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Łukasz on 21/03/2017.
 */

public class MonitorServiceActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getSimpleName();

    private final String TENANT_ONE = "tcp://172.16.88.126:5556";
    private final String TENANT_TWO = "tcp://172.16.88.126:5557";
    private final String[] tenants = new String[]{TENANT_ONE, TENANT_TWO};

    private final DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy HH:mm:ss");


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

    private int currentTenant = 0;
    private Intent serviceIntent;

    ZmqEvent zmqEvent = dummyMonitorUpdate -> {
        processMsg(dummyMonitorUpdate);
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final LayoutInflater factory = getLayoutInflater();
        final View view = factory.inflate(R.layout.activity_many_topics, null);
        setContentView(view);

        ButterKnife.bind(this, view);

        adapter = new MonitorUpdateListAdapter(this, R.id.list, list);
        lvList.setAdapter(adapter);

        LocalPreferences localPreferences = LocalPreferences.getInstance(this);
        localPreferences.setCurrentTenant(TENANT_ONE);

        AppState.getInstance().setCurrentTenant(tenants[currentTenant]);

        startButton.setOnClickListener(view1 -> {
            serviceIntent = new Intent(this, WooMQService.class);
            this.startService(serviceIntent);
        });

        killButton.setOnClickListener(view1 -> {
            if (serviceIntent != null) {
                stopService(serviceIntent);
            }
        });

        btnChangeTenant.setOnClickListener(view1 -> {
            Toast.makeText(MonitorServiceActivity.this, "Changing tenant", Toast.LENGTH_SHORT).show();
            currentTenant++;
            localPreferences.setCurrentTenant(tenants[currentTenant % 2]);
            this.startService(new Intent(this, WooMQService.class));
        });



    }

    @Override
    protected void onResume() {
        super.onResume();
        AppEvents.instance().subscribeToZmq(zmqEvent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppEvents.instance().unsubscribeFromZmq(zmqEvent);
    }

    private void processMsg(DummyMonitorUpdate monitorUpdate) {
        list.add(monitorUpdate);
        if (list.size() > 20) {
            list.remove(0);
        }

        runOnUiThread(() -> {
            adapter.refresh(list);
        });
    }


}

