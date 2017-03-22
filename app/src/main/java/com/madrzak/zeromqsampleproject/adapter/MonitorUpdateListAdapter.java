package com.madrzak.zeromqsampleproject.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.madrzak.zeromqsampleproject.R;
import com.madrzak.zeromqsampleproject.database.DummyMonitorUpdate;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MonitorUpdateListAdapter extends ArrayAdapter<DummyMonitorUpdate> {
    private static final String TAG = MonitorUpdateListAdapter.class.getSimpleName();

    private ArrayList<DummyMonitorUpdate> list;
    Context context;
    private DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy HH:mm:ss");

    // We expose the List so we can modify it from outside

    public MonitorUpdateListAdapter(Context context, int resource, List<DummyMonitorUpdate> items) {
        super(context, resource, items);
        this.context = context;
        this.list = new ArrayList<>();
        this.list.addAll(items);
    }

    public void refresh(List<DummyMonitorUpdate> items) {
        this.list = new ArrayList<>();
        this.list.addAll(items);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(context);
            v = vi.inflate(R.layout.monitor_update_item, null);
        }

        DummyMonitorUpdate model = list.get(position);

        if (model != null) {
            TextView tvId = (TextView) v.findViewById(R.id.tvId);
            if (tvId != null) {
                tvId.setText(model.getMonitorId() + "");
            }

            TextView tvTime = (TextView) v.findViewById(R.id.tvTime);
            if (tvTime != null) {
                tvTime.setText(dateFormat.format(new Date(model.getTime())));
            }

            TextView tvTenant = (TextView) v.findViewById(R.id.tvTenant);
            if (tvTenant != null) {
                tvTenant.setText(model.getPublisher());
            }
        }

        return v;
    }

    @Override
    public int getCount() {
        return list.size();
    }
}
