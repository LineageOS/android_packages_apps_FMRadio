package com.cyanogenmod.fmradio.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.cyanogenmod.fmradio.R;

import java.util.ArrayList;

/**
 * User: Pedro Veloso
 */
public final class StationsAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private ArrayList<String> stations = null;

    @SuppressWarnings("unchecked")
    public StationsAdapter(Context context, ArrayList<String> stations) {
        mInflater = LayoutInflater.from(context);
        this.stations = (ArrayList<String>) stations.clone();
    }

    public int getCount() {
        return stations.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.station_item, null, false);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.tv_station);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.name.setText(stations.get(position));

        return convertView;
    }

    class ViewHolder {
        TextView name;
    }

}

