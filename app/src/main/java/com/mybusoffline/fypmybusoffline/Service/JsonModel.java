package com.mybusoffline.fypmybusoffline.Service;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by darks on 15-May-18.
 */

public class JsonModel {

    public static JSONObject getBusRouteJson(String serviceNo, Context page){

        String filePath = "json/" + serviceNo + ".json";

        try {

            InputStream is = page.getAssets().open(filePath);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String json = new String(buffer,"UTF-8");
            //Log.d("JSON Content -1",json);

            return new JSONObject(json);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return new JSONObject();
    }

    //GET LIST OF BUS SERVICE NO. AVAILABLE
    public static String[] getBusServiceList(Context page) {
        String[] list = null;
        try {
            String [] tempList = page.getAssets().list("json");

            for (int i=0; i<tempList.length;i++) {

                tempList[i] = tempList[i].substring(0, tempList[i].lastIndexOf("."));

                //Log.d("JsonListNames", tempList[i]);
            }

            list = tempList;


        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }
}
