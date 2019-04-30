package com.mybusoffline.fypmybusoffline.Service;

import android.content.Context;
import android.content.res.AssetManager;

import com.mybusoffline.fypmybusoffline.BusStop;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by darks on 15-May-18.
 */

//
public class JsonService {

    public static ArrayList<ArrayList<BusStop>> retrieveBusRouteList(String serviceNo, Context context){

        JSONObject obj = JsonModel.getBusRouteJson(serviceNo,context);
        ArrayList<ArrayList<BusStop>> busRouteList = new ArrayList<>();
        ArrayList<BusStop> directionList = new  ArrayList<>();
        String name, dist, desc, code, latitude, longitude;

        try {

            JSONArray arr = obj.getJSONArray("direction 1");

            //RETRIEVE INFO FOR DIRECTION 1
            for (int i = 0; i < arr.length(); i++){
                JSONObject jsonObject=arr.getJSONObject(i);

                name=jsonObject.getString("Description");
                dist=jsonObject.getString("Distance");
                desc=jsonObject.getString("RoadName");
                code=jsonObject.getString("BusStopCode");
                latitude=jsonObject.getString("Latitude");
                longitude=jsonObject.getString("Longitude");
                directionList.add(new BusStop(dist,code,name,desc,latitude,longitude));

            }
            //ADD ARRAYLIST TO BUSROUTELIST
            busRouteList.add(directionList);

            //GET DIRECTION 2 INFO IF ANY
            if(checkDoubleDirection(obj)){

                //CLEAR USED ARRAYLIST
                directionList = new  ArrayList<>();
                arr = obj.getJSONArray("direction 2");

                //RETRIEVE INFO FOR DIRECTION 2
                for (int i = 0; i < arr.length(); i++){
                    JSONObject jsonObject=arr.getJSONObject(i);

                    name=jsonObject.getString("Description");
                    dist=jsonObject.getString("Distance");
                    desc=jsonObject.getString("RoadName");
                    code=jsonObject.getString("BusStopCode");
                    latitude=jsonObject.getString("Latitude");
                    longitude=jsonObject.getString("Longitude");
                    directionList.add(new BusStop(dist,code,name,desc,latitude,longitude));

                }
                //ADD ARRAYLIST TO BUSROUTELIST
                busRouteList.add(directionList);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


        return busRouteList;
    }

    //CHECK IF BUS SERVICE HAS 2 DIRECTION
    private static boolean checkDoubleDirection(JSONObject obj){

        boolean result = false;

        if(obj.has("direction 2"))
            result = true;


        return result;

    }

    //CHECK IF BUS SERVICE NO. IS VALID
    public static boolean checkValidJsonFile(String serviceNo, Context context){

        boolean result = false;

        AssetManager mg = context.getAssets();
        String path = "json/" + serviceNo + ".json";
        try {
            InputStream is = mg.open(path);
            //file exists so do something with it
            is.close();
            result = true;
        } catch (IOException ex) {
            //file does not exist
        }

        return result;
    }

    //CALCULATE DISTANCE BETWEEN 2 BUS STOPS
    public static String calculateRouteDistance(ArrayList<BusStop> routeList, String startStop, String endStop){

        String startDist = null, endDist = null;

        for(BusStop c: routeList){

            if(c.getDescription().toLowerCase().equals(startStop.toLowerCase())){
                startDist = c.getDistance();
            }

            if(c.getDescription().toLowerCase().equals(endStop.toLowerCase())){
                endDist = c.getDistance();
            }
        }

        //CALCULATE DIFF FROM BOTH DISTANCES AND RETURN VALUE AS STRING
        return String.format("%.1f",(Float.valueOf(endDist) - Float.valueOf(startDist)));

    }

    //CALCULATE NO OF STOPS TILL DESTINATION
    public static String calculateNoOfStops(ArrayList<BusStop> routeList, String startStop, String endStop){

        int tempstart =0, tempEnd=0;

        for(int i = 0; i<routeList.size(); i++){

            if(routeList.get(i).getDescription().toLowerCase().equals(startStop.toLowerCase())){
               tempstart=i;
            }

            if(routeList.get(i).getDescription().toLowerCase().equals(endStop.toLowerCase())){
                tempEnd =i;
            }
        }

        return Integer.toString(tempEnd - tempstart);
    }

    //FILTER LIST BASED ON STARTING BUS STOP SO LIST ONLY SHOWS BUS STOP AFTER THE START STOP
    public static ArrayList<BusStop> filterList(String startPoint, String endPoint, ArrayList<BusStop> busList){

        int startIndex =0, endIndex =0;
        ArrayList<BusStop> tempBusList = busList;

        //USER INDICATED A START POINT ONLY
        if(startPoint.length() >0 && endPoint ==null){

            for(int i =0; i<busList.size(); i++){

                if(startPoint.toLowerCase().equals(busList.get(i).getDescription().toLowerCase())){
                    startIndex = i;
                    break;
                }
            }
            tempBusList = new ArrayList<>(busList.subList(startIndex+1,busList.size()));
        }
        //USER INDICATED BOTH START POINT AND END POINT
        else if (startPoint.length() >0 && endPoint.length() >0){
            for(int i =0; i<busList.size(); i++){

                if(startPoint.toLowerCase().equals(busList.get(i).getDescription().toLowerCase())){
                    startIndex = i;
                }
                else if(endPoint.toLowerCase().equals(busList.get(i).getDescription().toLowerCase())){
                    endIndex = i;
                }

            }
            tempBusList = new ArrayList<>(busList.subList(startIndex,endIndex));
            tempBusList.add(busList.get(endIndex));

        }

        return tempBusList;
    }

}
