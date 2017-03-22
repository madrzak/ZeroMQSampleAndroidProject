package com.madrzak.zeromqsampleproject;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.madrzak.zeromqsampleproject.adapter.MessageListAdapter;

import org.zeromq.ZMQ;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WeatherSubActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getSimpleName();

    private String ADDRESS = "tcp://172.16.88.126:5556";
    private String TOPIC = "M";
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
        final View view = factory.inflate(R.layout.activity_weather_sub, null);
        setContentView(view);

        ButterKnife.bind(this, view);

        adapter = new MessageListAdapter(this, R.id.list, list);
        lvList.setAdapter(adapter);


        thread = new Thread(() -> {
            ZMQ.Context context = ZMQ.context(1);

            //  Socket to talk to server
            System.out.println("Collecting updates from weather server");
            ZMQ.Socket subscriber = context.socket(ZMQ.SUB);
            subscriber.connect(ADDRESS);

            subscriber.subscribe("".getBytes());

            //  Process 100 updates
            int update_nbr;
            long total_temp = 0;
            for (update_nbr = 0; update_nbr < 100; update_nbr++) {
                //  Use trim to remove the tailing '0' character
                String string = subscriber.recvStr(0).trim();

                StringTokenizer sscanf = new StringTokenizer(string, " ");
                int zipcode = Integer.valueOf(sscanf.nextToken());
                int temperature = Integer.valueOf(sscanf.nextToken());
                int relhumidity = Integer.valueOf(sscanf.nextToken());

                total_temp += temperature;
                System.out.println("temp + " + temperature);
            }

            System.out.println("Average temperature for ' was " + (int) (total_temp / update_nbr));

            subscriber.close();
            context.term();
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

        list.add(json);

        runOnUiThread(() -> {
            adapter.refresh(list);
        });
    }

}
