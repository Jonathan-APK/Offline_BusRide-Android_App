package com.mybusoffline.fypmybusoffline;

import android.content.Context;
import android.widget.Filter;

import com.mybusoffline.fypmybusoffline.UIClass.SelectAllBusList;
import com.mybusoffline.fypmybusoffline.UIClass.SelectNearbyBusList;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by darks on 05-Jun-18.
 */
public class SearchFilter extends Filter {

    Context c;

    public SearchFilter(Context c) {
        this.c = c;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {

        // Create a FilterResults object
        FilterResults results = new FilterResults();
        ArrayList<BusStop> tempList = new ArrayList<>();


       //IF SEARCH STRING IS EMPTY OR LENGTH 0
        if (constraint == null || constraint.length() == 0) {

            if(c instanceof SelectNearbyBusList){

              tempList = SelectNearbyBusList.tempBusList;
            }
            else if(c instanceof SelectAllBusList){

                tempList = SelectAllBusList.originalTempBusList;

            }

            results.values = tempList;
            results.count =  tempList.size();
        }
        else {

            ArrayList<BusStop> filteredList = new ArrayList<BusStop>();

            if(c instanceof SelectNearbyBusList) {

                tempList = SelectNearbyBusList.tempBusList;
            }
            else if(c instanceof  SelectAllBusList) {

                tempList = SelectAllBusList.originalTempBusList;

            }

            //LOOP THROUGH ARRAYLIST
            for (int i=0; i< tempList.size(); i++) {

                //FIND OBJECT THAT CONTAINS SEARCH STRING
                if (tempList.get(i).getDescription().toUpperCase().contains( constraint.toString().toUpperCase()) ||
                        tempList.get(i).getBusStopCode().toUpperCase().contains( constraint.toString().toUpperCase()) ||
                        tempList.get(i).getRoadName().toUpperCase().contains( constraint.toString().toUpperCase())) {

                    filteredList.add(tempList.get(i));
                }
            }

            //SET FILTERED OBJECT AND SIZE
            results.values = filteredList;
            results.count = filteredList.size();
        }

        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {

        if(c instanceof SelectNearbyBusList) {

            //CLEAR AND RE-ADD THE DATA INTO THE TEMP LIST
            SelectNearbyBusList.filterList.clear();
            SelectNearbyBusList.filterList.addAll((Collection<? extends BusStop>) results.values);

            //REFRESH ADAPTER
            SelectNearbyBusList.busAdapter.notifyDataSetChanged();
        }
        else if(c instanceof  SelectAllBusList) {

            //CLEAR AND RE-ADD THE DATA INTO THE TEMP LIST
            SelectAllBusList.tempBusList.clear();
            SelectAllBusList.tempBusList.addAll((Collection<? extends BusStop>) results.values);

            //REFRESH ADAPTER
            SelectAllBusList.busAdapter.notifyDataSetChanged();
        }


    }
}