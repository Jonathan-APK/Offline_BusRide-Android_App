package com.mybusoffline.fypmybusoffline.CustomAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mybusoffline.fypmybusoffline.BusStop;
import com.mybusoffline.fypmybusoffline.R;

import java.util.ArrayList;

/**
 * Created by darks on 04-Jun-18.
 */

public class CustomOverviewAdapter extends ArrayAdapter<BusStop>{

    int[] imageSrc;
    ImageView image;

    public CustomOverviewAdapter(Context context, ArrayList<BusStop> busInfo, int[] image){
        super(context, R.layout.custom_overview_row, busInfo);
        imageSrc = image;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //INSTANTIATE LAYOUT XML
        LayoutInflater busInflater = LayoutInflater.from(getContext());

        View customView = busInflater.inflate(R.layout.custom_overview_row, parent, false);

        //SET TEXT INFO FOR EACH ROW
        BusStop busInfo = getItem(position);
        image = customView.findViewById(R.id.rowOverviewImage);
        TextView busCode = customView.findViewById(R.id.rowOverviewBusCodeText);
        TextView busStopName = customView.findViewById(R.id.rowOverviewBusStopNameText);
        TextView roadName = customView.findViewById(R.id.rowOverviewRoadNameText);

        //SET TEXT FOR TEXTVIEW
        busCode.setText(busInfo.getBusStopCode());
        busStopName.setText(busInfo.getDescription());
        roadName.setText(busInfo.getRoadName());

        //SET IMAGE VIEW
        if(position == 0){
            //SET IMAGE FOR START POINT
            image.setImageResource(imageSrc[0]);
        }
        else if(position == getCount()-1){
            //SET IMAGE FOR END POINT
            image.setImageResource(imageSrc[2]);
        }
        else{
            //SET IMAGE FOR POINT IN BETWEEN START AND END POINT
            image.setImageResource(imageSrc[1]);
        }

        return customView;

    }

}

