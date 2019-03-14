package com.andit.e_wall;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.andit.e_wall.data_model.BoardModel;
import com.google.android.gms.maps.model.LatLng;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class CustomAdapter extends ArrayAdapter<BoardModel> implements View.OnClickListener{

    private List<BoardModel> dataSet;
    Context mContext;
    LatLng currentPosition;

    // View lookup cache
    private static class ViewHolder {
        TextView txtName;
        TextView txtId;
        TextView txtDistance;
    }

    public CustomAdapter(List<BoardModel> data, LatLng currentPos, Context context) {
        super(context, R.layout.raw_board, data);
        this.dataSet = data;
        this.mContext=context;
        this.currentPosition = currentPos;

    }

    public BoardModel getById(int position){
        return dataSet.get(position);
    }

    public List<BoardModel> getDepartures(){
        return dataSet;
    }

    @Override
    public void onClick(View v) {
        int position=(Integer) v.getTag();
        Object object= getItem(position);
        BoardModel dataModel=(BoardModel) object;


    }

    private int lastPosition = -1;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        BoardModel dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.raw_board, parent, false);
            viewHolder.txtName = convertView.findViewById(R.id.boardName);
            viewHolder.txtId = convertView.findViewById(R.id.boardId);
            viewHolder.txtDistance = convertView.findViewById(R.id.distance);

            result=convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

        Animation animation = AnimationUtils.loadAnimation(mContext, (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
        result.startAnimation(animation);
        lastPosition = position;

        viewHolder.txtName.setText(dataModel.getName());
        viewHolder.txtId.setText(String.valueOf(dataModel.getBoardId()));
        double distance = MapHelper.distance(currentPosition.latitude, dataModel.getLatitude(), currentPosition.longitude, dataModel.getLongitude(), (double)0, (double) 0)/1000;
        if(distance < 0.1) viewHolder.txtDistance.setTextColor(Color.GREEN);

        String distanceFormated = new DecimalFormat("0.00").format(distance);

        viewHolder.txtDistance.setText(distanceFormated + " Km");



        // Return the completed view to render on screen
        return convertView;
    }
}
