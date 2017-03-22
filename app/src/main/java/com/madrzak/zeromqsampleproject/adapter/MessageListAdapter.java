package com.madrzak.zeromqsampleproject.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.madrzak.zeromqsampleproject.R;

import java.util.ArrayList;
import java.util.List;

public class MessageListAdapter extends ArrayAdapter<String> {
    private static final String TAG = MessageListAdapter.class.getSimpleName();

    private ArrayList<String> list;
    Context context;

    // We expose the List so we can modify it from outside

    public MessageListAdapter(Context context, int resource, List<String> items) {
        super(context, resource, items);
        this.context = context;
        this.list = new ArrayList<>();
        this.list.addAll(items);
    }

    public void refresh(List<String> items) {
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
            v = vi.inflate(R.layout.list_item_simple, null);
        }

        String model = list.get(position);

        if (model != null) {
            TextView tv = (TextView) v.findViewById(R.id.tv);
            if (tv != null) {
                tv.setText(model);
            }
        }

        return v;
    }

    @Override
    public int getCount() {
        return list.size();
    }
}
