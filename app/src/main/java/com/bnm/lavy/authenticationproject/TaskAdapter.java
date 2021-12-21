package com.bnm.lavy.authenticationproject;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class TaskAdapter extends BaseAdapter {
    ArrayList<Task> objects;
    Context context;
    public TaskAdapter(Context context,  ArrayList<Task> objects) {

        this.objects = objects;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // create visual item of ListView
        View view = convertView;
        if ( view == null)
                view = ((Activity)context).getLayoutInflater().inflate(R.layout.task_item_layout, parent, false);

        // connection to the ListView item's fields
        TextView tvTaskText = view.findViewById(R.id.tvTaskText);
        TextView tvPriority = view.findViewById(R.id.tvPriority);
        TextView tvDeadLine = view.findViewById(R.id.tvDeadLine);

        // connects to the current ListView Item
        Task task = objects.get(position);

        // sets the values of the current ListView item
        tvTaskText.setText(task.getContent());
        tvDeadLine.setText(task.getDeadLine());
        if ( task.isHiPriority()) {
            tvPriority.setText( "!");
            tvPriority.setBackgroundColor(Color.RED);
        }
        else
        {
            tvPriority.setText("");
            tvPriority.setBackgroundColor(Color.parseColor("#50A4FA"));
        }
        return view;
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public Task getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }



}
