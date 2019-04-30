package com.mybusoffline.fypmybusoffline.CustomAdapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mybusoffline.fypmybusoffline.BusStop;
import com.mybusoffline.fypmybusoffline.R;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by darks on 25-Jun-18.
 */

//CUSTOM ADAPTOR FOR BUS MONITORING UI
public class CustomMonitorAdaptor extends RecyclerView.Adapter<CustomMonitorAdaptor.ViewHolder> {

    private ArrayList<BusStop> routeList = new ArrayList<>();
    private int[] imageList;
    private boolean[] setBackground;
    private boolean[] setPinImage;
    Context myContext;

    public CustomMonitorAdaptor(ArrayList<BusStop> routeList, int[] imageList, Context myContext) {
        this.routeList = routeList;
        this.imageList = imageList;
        this.myContext = myContext;

        //INITIAL SETUP FOR SETTING BACKGROUND SHAPE FOR ROW
        setBackground = new boolean[routeList.size()];
        Arrays.fill(setBackground, false);
        setBackground[0] = true;

        //INITIAL SETUP FOR SETTING PIN BACKGROUND IMAGE SHAPE FOR ROW
        setPinImage = new boolean[routeList.size()];
        Arrays.fill(setPinImage, false);
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_monitoring_row, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(CustomMonitorAdaptor.ViewHolder holder, int position) {
        Log.d("rowindexformonitoring", String.valueOf(position) + " - " + setBackground[position]);

        //SET PIN BACKGROUND VIEW
        if(holder.getAdapterPosition() == 0){
            //SET IMAGE FOR START POINT
            holder.pinImage.setImageResource(imageList[4]);
            holder.indicatorImage.setImageResource(android.R.color.transparent);
        }
        else if(holder.getAdapterPosition() == routeList.size()-1 && !setPinImage[holder.getAdapterPosition()]){
            //SET IMAGE FOR END POINT (WHITE GREY, UNTRAVELED)
            holder.pinImage.setImageResource(imageList[2]);
            holder.indicatorImage.setImageResource(android.R.color.transparent);

        }
        else if(holder.getAdapterPosition() == routeList.size()-1 && setPinImage[holder.getAdapterPosition()]){
            //SET IMAGE FOR END POINT (ALL GREY, TRAVELED)
            holder.pinImage.setImageResource(imageList[5]);
            holder.indicatorImage.setImageResource(android.R.color.transparent);

        }
        else if(!setPinImage[holder.getAdapterPosition()]){
            //SET IMAGE FOR POINT IN BETWEEN START AND END POINT (WHITE GREY, UNTRAVELED)
            holder.pinImage.setImageResource(imageList[1]);
            holder.indicatorImage.setImageResource(android.R.color.transparent);

        }
        else{
            //SET IMAGE FOR POINT IN BETWEEN START AND END POINT (ALL GREY, TRAVELED)
            holder.pinImage.setImageResource(imageList[3]);
            holder.indicatorImage.setImageResource(android.R.color.transparent);

        }

        //SET ROUNDED BORDER BACKGROUND
        if(setBackground[holder.getAdapterPosition()]){
            //SET BACKGROUND IMAGE
            Log.d("rowindexformonitoring-3", String.valueOf(holder.getAdapterPosition()) + " - " + setBackground[holder.getAdapterPosition()]);
            holder.itemView.setBackgroundResource(R.drawable.rounded_edge);

            //SET CURRENT PIN ICON FOR ROW
            holder.indicatorImage.setImageResource(R.drawable.currentpin);
        }
        else{
            //REMOVE BACKGROUND IMAGE
            Log.d("rowindexformonitoring-2", String.valueOf(holder.getAdapterPosition()) + " - " + setBackground[holder.getAdapterPosition()]);
            holder.itemView.setBackgroundResource(0);
            holder.indicatorImage.setImageResource(android.R.color.transparent);


        }

        //SET NEXT STOP IMAGE FOR SELECTED ROW
        //ENSURE NO INDEXOUTOFBOUND ERROR CAUSE BY (setBackground[holder.getAdapterPosition() - 1) && ENSURE CURRENT INDEX NOT MORE THAN LAST STOP
        if(holder.getAdapterPosition() !=0 && holder.getAdapterPosition() < routeList.size()) {

            //CHECK FOR CURRENT STOP INDEX TO IDENTIFY NEXT ROW TO DISPLAY IMAGE
            if (setBackground[holder.getAdapterPosition() - 1]) {
                Log.d("NEXTSTOP IMAGE2","---");

                //SET NEXT STOP ICON FOR ROW
                holder.indicatorImage.setImageResource(R.drawable.nextstop);
            }
        }

        holder.busCodeText.setText(routeList.get(holder.getAdapterPosition()).getBusStopCode());
        holder.busDescText.setText(routeList.get(holder.getAdapterPosition()).getDescription());
    }

    @Override
    public int getItemCount() {
        return routeList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView pinImage;
        ImageView indicatorImage;
        TextView busCodeText;
        TextView busDescText;

        public ViewHolder(View itemView) {
            super(itemView);
            indicatorImage = itemView.findViewById(R.id.rowMonitorIndicator);
            pinImage = itemView.findViewById(R.id.rowMonitorImage);
            busCodeText = itemView.findViewById(R.id.rowMonitorBusCodeText);
            busDescText = itemView.findViewById(R.id.rowMonitorBusStopNameText);
        }
    }

    //GETTER METHOD
    public boolean[] getSetBackground() {
        return setBackground;
    }

    //SETTER METHOD
    public void setSetBackground(boolean[] setBackground) {
        this.setBackground = setBackground;
    }

    //GETTER METHOD
    public boolean[] getSetPinImage() {
        return setPinImage;
    }

    //SETTER METHOD
    public void setSetPinImage(boolean[] setPinImage) {
        this.setPinImage = setPinImage;
    }



}
