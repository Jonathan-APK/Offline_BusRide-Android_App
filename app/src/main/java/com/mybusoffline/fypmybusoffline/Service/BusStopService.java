package com.mybusoffline.fypmybusoffline.Service;

import android.location.Location;
import android.util.Log;

import com.mybusoffline.fypmybusoffline.BusStop;

import java.util.ArrayList;

/**
 * Created by darks on 07-Jun-18.
 */

public class BusStopService {

    //FIND ALL BUS-STOP WITHIN 650M RADIUS OF THE USER'S LOCATION
    public static ArrayList<BusStop> busStopNearby(Location loc, ArrayList<BusStop> busList){

        ArrayList<BusStop> tempBuslist = new ArrayList<>();
        Location tempLocation = new Location("busStopLocation");

        //ENSURE THE LAST STOP IN THE ROUTE DOES NOT APPEAR IN THE NEARBY LIST (busList.size()-1)
        for(int i=0; i< busList.size()-1; i++){

            tempLocation.setLatitude(Double.parseDouble(busList.get(i).getLatitude()));
            tempLocation.setLongitude(Double.parseDouble(busList.get(i).getLongitude()));

            //DISTANCE LESS THAN 0.65KM
            if(loc.distanceTo(tempLocation) <= 650){
                Log.d("radius3", String.valueOf(loc.distanceTo(tempLocation)));

                tempBuslist.add(busList.get(i));
            }
        }

        return tempBuslist;
    }

}
