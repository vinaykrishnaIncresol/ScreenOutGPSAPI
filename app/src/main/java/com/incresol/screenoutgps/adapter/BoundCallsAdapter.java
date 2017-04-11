package com.incresol.screenoutgps.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.incresol.screenoutgps.R;
import com.incresol.screenoutgps.modal.BoundCalls;

import java.util.ArrayList;

public class BoundCallsAdapter extends RecyclerView.Adapter<BoundCallsAdapter.ViewHolder> {
    private ArrayList<BoundCalls> boundcalls;
    private Context context;

    public BoundCallsAdapter(ArrayList<BoundCalls> boundcalls, Context context) {
        this.boundcalls = boundcalls;
        this.context = context;
    }

    @Override
    public BoundCallsAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.boundlist_card_view, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(BoundCallsAdapter.ViewHolder viewHolder, int i) {
        viewHolder.name.setText(boundcalls.get(i).getName());
        viewHolder.number.setText(boundcalls.get(i).getNumber());
    }

    @Override
    public int getItemCount() {
        return boundcalls.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private TextView number;

        public ViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.boundlist_name);
            number = (TextView) view.findViewById(R.id.boundlist_numnber);
        }
    }
}
