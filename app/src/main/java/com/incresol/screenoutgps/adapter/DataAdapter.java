package com.incresol.screenoutgps.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.incresol.screenoutgps.modal.Notifications;
import com.incresol.screenoutgps.R;

import java.util.ArrayList;

/**
 * Created by Incresol on 19-Dec-16.
 */

public class DataAdapter extends RecyclerView.Adapter<DataAdapter.ViewHolder> {
    private ArrayList<Notifications> notifications;
    private Context context;

    public DataAdapter(ArrayList<Notifications> notifications, Context context) {
        this.notifications = notifications;
        this.context = context;
    }

    @Override
    public DataAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.notifications_listrow_card, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DataAdapter.ViewHolder viewHolder, int i) {
        boolean readornot = notifications.get(i).isRead();
        viewHolder.titile.setText(notifications.get(i).getNotification());
        if (!readornot) {
            viewHolder.readNotify.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView titile;
        private FrameLayout readNotify;

        public ViewHolder(View view) {
            super(view);
            titile = (TextView) view.findViewById(R.id.notification_title);
            readNotify = (FrameLayout) view.findViewById(R.id.notyetread);
        }
    }
}
