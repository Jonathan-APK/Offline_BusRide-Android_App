package com.mybusoffline.fypmybusoffline.CustomAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.util.Log;

import com.mybusoffline.fypmybusoffline.BusStop;
import com.mybusoffline.fypmybusoffline.R;
import com.mybusoffline.fypmybusoffline.SearchFilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by darks on 04-Jun-18.
 */

public class CustomSelectionAdapter extends ArrayAdapter<BusStop> implements Filterable,SectionIndexer {

    SearchFilter mBusFilter;
    Context c;

    HashMap<String, Integer> mapIndex;
    String[] sections;
    List<String> distance;

    public CustomSelectionAdapter(Context context, ArrayList<BusStop> busInfo){
        super(context, R.layout.custom_selection_row, busInfo);
        c = context;

        //GET LIST OF DISTANCE FOR ROUUTE
        List<String> temp = new ArrayList<>();
        for(int i=0; i<busInfo.size();i++){
            temp.add(busInfo.get(i).getDistance());
        }

        this.distance = temp;
        mapIndex = new LinkedHashMap<String, Integer>();

        //FILTER DECIMAL PLACE FOR DISTANCE
        for (int x = 0; x < distance.size(); x++) {
            String dist = distance.get(x);
            String ch = dist.replaceAll("\\.0*$", "") +" km";


            // HashMap will prevent duplicates
            mapIndex.put(ch, x);

        }

        Set<String> sectionLetters = mapIndex.keySet();

        //CREATE A LIST TO SORT (IF NEEDED)
        ArrayList<String> sectionList = new ArrayList<String>(sectionLetters);

        Log.d("sectionList", sectionList.toString());
        //Collections.sort(sectionList);

        sections = new String[sectionList.size()];

        sectionList.toArray(sections);


    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //INSTANTIATE LAYOUT XML
        LayoutInflater busInflater = LayoutInflater.from(getContext());

        View customView = busInflater.inflate(R.layout.custom_selection_row, parent, false);

        //SET TEXT INFO FOR EACH ROW
        BusStop busInfo = getItem(position);
        TextView busCode = customView.findViewById(R.id.rowSelectionBusCodeText);
        TextView busStopName = customView.findViewById(R.id.rowSelectionBusStopNameText);
        TextView roadName = customView.findViewById(R.id.rowSelectionRoadNameText);

        //SET TEXT FOR TEXTVIEW
        busCode.setText(busInfo.getBusStopCode());
        busStopName.setText(busInfo.getDescription());
        roadName.setText(busInfo.getRoadName());

        return customView;

    }

    @Override
    public Filter getFilter() {
        if (mBusFilter == null)
            mBusFilter = new SearchFilter(c);

        return mBusFilter;
    }


    @Override
    public Object[] getSections() {
        return sections;
    }

    @Override
    public int getPositionForSection(int i) {

        return mapIndex.get(sections[i]);
    }

    @Override
    public int getSectionForPosition(int i) {
        return 0;
    }
}

