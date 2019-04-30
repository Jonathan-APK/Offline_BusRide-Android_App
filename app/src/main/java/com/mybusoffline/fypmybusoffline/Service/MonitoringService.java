package com.mybusoffline.fypmybusoffline.Service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.mybusoffline.fypmybusoffline.BusStop;

import java.util.ArrayList;

/**
 * Created by darks on 19-Jun-18.
 */

//BACKGROUND SERVICE USED TO MONITOR THE BUS JOURNEY OF THE USER. TRACK AND UPDATE THE UI
public class MonitoringService extends Service {

    ArrayList<BusStop> routeList;
    int listStartIndex, listEndIndex;
    boolean loopServiceInitialStatus;
    Location location;
    boolean runningThread;
    String lastStopDesc;

    //BROADCAST RECEIVER FOR GPS COORDINATES UPDATE
    private BroadcastReceiver updateCoordinates = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //GET CURRENT LOCATION COORDINATES WHEN UPDATED
            location = intent.getParcelableExtra("currentLocation");
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        runningThread = true;

        //REGISTER LOCALBROADCAST RECEIVER FOR GPS UPDATES
        LocalBroadcastManager.getInstance(this).registerReceiver(updateCoordinates,
                new IntentFilter("currentGPS"));

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d("monitoring","start");

        super.onStartCommand(intent, flags, startId);

        //RETRIEVE ROUTELIST FROM UIACTIVITY
        routeList = (ArrayList<BusStop>) intent.getSerializableExtra("routelist");
        //RETRIEVE INDEX OF LAST STOP FOR THE BUS SERVICE (UNFILTERED)
        lastStopDesc =intent.getStringExtra("laststopdesc");
        Log.d("lastStopDesc", lastStopDesc);

        //INITIALISE VARIABLES
        location = null;
        //START INDEX OF THE LIST
        listStartIndex = 1;
        loopServiceInitialStatus = false;
        listEndIndex = routeList.size() -1;

        //SET DEFAULT GPS COORDINATES AS START STOP (ensure no null location value when service first started)
        location = new Location("startLocation");
        location.setLatitude(Double.parseDouble(routeList.get(0).getLatitude()));
        location.setLongitude(Double.parseDouble(routeList.get(0).getLongitude()));

        //IF START = DESTINATION STOP, FILTERED LIST TO EXCLUDE DESTINATION FIRST
        if (checkStartDestinationSame() && !loopServiceInitialStatus) {
            listEndIndex--;
        }
        Log.d("monitoring- listStart", String.valueOf(listStartIndex));
        Log.d("monitoring- listEnd", String.valueOf(listEndIndex));

        /////////////////////////////////
        /////LOOPING OF THE SERVICE//////
        /////////////////////////////////

        //PERFORM TASK EVERY 0.8 SEC
        new Thread(new Runnable(){
            public void run() {
                // TODO Auto-generated method stub
                while(true)
                {
                    try {
                        Thread.sleep(800);
                        //DETECT NEAREST BUS STOP FROM LIST WITHIN 24M FROM MY LOCATION
                        int closestStop = detectBusStop();
                        Log.d("monitoring- closestStop", String.valueOf(closestStop));

                        //IF THERE IS ANY STOP WITHIN 24M
                        if(closestStop != -1){

                            //FOR RETRIEVING SETTING CONFIGURATION (NO. OF STOP BEFORE DESTINATION TO NOTIFY USER)
                            SharedPreferences prefs = getSharedPreferences("setting", MODE_PRIVATE);

                            //SEND REMAINDER NOTIFICATION BASED ON SETTING
                            if(closestStop >= (routeList.size() -1 - prefs.getInt("notificationIndex",1)) && closestStop != (routeList.size() -1)){
                                Log.d("monitoring- reaching", closestStop + "-" +(routeList.size() - prefs.getInt("notificationIndex",1)));
                                postNotification("reaching", closestStop);
                            }
                            //IF ARRIVED AT DESTINATION, POST NOTIFICATION TO INFORM USER
                            else if(closestStop == (routeList.size() -1)){
                                //setCurrentLocationUI(closestStop);
                                Log.d("monitoring- reached", "234");
                                postNotification("reached", closestStop);
                            }

                            //UPDATE UI ON CURRENT STOP
                            setCurrentLocationUI(closestStop);
                            Log.d("Updating UI!", String.valueOf(closestStop));
                            //UPDATE FILTERED LIST
                            listStartIndex = closestStop +1;

                            //STOP THREAD AND SERVICE ONCE DESTINATION REACHED
                            if(listStartIndex > listEndIndex){
                                stopSelf();
                                return;
                            }

                            //IF LOOP SERVICE(START = END) AND UI UPDATED AT LEAST ONCE, UPDATE FILTERED LIST TO INCLUDE DESTINATION STOP
                            if(checkStartDestinationSame() && !loopServiceInitialStatus){
                                Log.d("monitoring- 2", "");
                                listEndIndex++;
                                loopServiceInitialStatus = true;
                            }
                        }

                        //STOP THREAD IF SERVICE IS DESTROYED
                        if (!runningThread) {
                            return;
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();

        //START_NOT_STICKY KILLS SERVICES AFTER APP DIED
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {

        runningThread = false;

        //UNREGISTER LOCALBROADCAST RECEIVER (UPDATECOORDINATES)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(updateCoordinates);

        super.onDestroy();
    }

    //DETECT IF USER IS NEAR A BUS STOP (FROM LIST) WITHIN 24m
    public int detectBusStop() {

        //tempIndex is the index of the upcoming stop
        int tempIndex, returnIndex = -1;
        //BUS STOP COORDINATES
        Location tempLocation1 = new Location("tempLocation");

        //USER LOCATION
        Location tempLocation2 = new Location("tempLocation");

        tempLocation2.set(location);
        tempIndex = listStartIndex;
        double tempDistance1 = -1, tempDistance2;
        Log.d("monitoring- start", String.valueOf(listStartIndex));
        Log.d("monitoring- end", String.valueOf(listEndIndex));

        //IF LEFT 1 MORE STOP (LAST STOP)
        if(listStartIndex == listEndIndex){

            //SET TEMPLOCATION1 COORDINATES AS LAST STOP
            tempLocation1.setLatitude(Double.parseDouble(routeList.get(listStartIndex).getLatitude()));
            tempLocation1.setLongitude(Double.parseDouble(routeList.get(listStartIndex).getLongitude()));
            tempDistance1 = (tempLocation2.distanceTo(tempLocation1));

        }
        //MORE THAN 1 STOP LEFT
        else {
            //FIND NEAREST BUS STOP FROM USER LOCATION (FROM LIST)
            for (int i = listStartIndex; i <= listEndIndex; i++) {

                //UPDATE TEMPLOCATION1 COORDINATES AS INDEX i BUS STOP
                tempLocation1.setLatitude(Double.parseDouble(routeList.get(i).getLatitude()));
                tempLocation1.setLongitude(Double.parseDouble(routeList.get(i).getLongitude()));

                //SET INITIAL DISTANCE TO COMPARE IF INDEX i IS 1ST IN THE LIST
                if (i == listStartIndex) {
                    tempDistance1 = (tempLocation2.distanceTo(tempLocation1));
                }
                //COMPARE DISTANCE TO FIND CLOSEST
                else {
                    tempDistance2 = (tempLocation2.distanceTo(tempLocation1));
                    if (tempDistance2 < tempDistance1) {
                        tempDistance1 = tempDistance2;
                        tempIndex = i;
                    }
                }
            }
        }

        Log.d("monitoring- DistNearest", String.valueOf(tempDistance1));

        //CHECK IF NEAREST STOP'S DISTANCE IS LESS THAN 30m
        if (tempDistance1 <= 30) {
            returnIndex = tempIndex;
        }
        //CHECK IF DISTANCE LESS THAN 100M FOR LAST STOP IN BUS SERVICE (BUS INTERCHANGE)
        else if(tempDistance1 <=100 && routeList.get(tempIndex).getDescription().equals(lastStopDesc)){
            returnIndex = tempIndex;
        }
        //CHECK IF DISTANCE LESS THAN 50M FOR LAST STOP IN THE ROUTE (EXCLUDE BUS INTERCHANGE)
        else if(tempDistance1 <=50 && listStartIndex == listEndIndex && !routeList.get(tempIndex).getDescription().equals(lastStopDesc)){
            returnIndex = tempIndex;
        }

        return returnIndex;
    }

    //CHECK IF START AND DESTINATION STOP ARE THE SAME
    public boolean checkStartDestinationSame() {

        boolean status = false;

        if (routeList.get(0).getBusStopCode().equals(routeList.get(routeList.size() - 1).getBusStopCode())) {
            status = true;
        }

        return status;
    }

    //UPDATE UI ON THE CURRENT BUS-STOP LOCATION OF THE USER
    public void setCurrentLocationUI(int index) {

        //BROADCAST MESSAGE TO ALL ACTIVITIES
        Intent intent = new Intent("setCurrentLocationUI");
        intent.putExtra("locationIndex",String.valueOf(index));
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        Log.d("monitor- setUI", String.valueOf(index));
    }

    //BROADCAST MESSAGE TO NOTIFY USER (1 MORE STOP TO DESTINATION)
    public void postNotification(String msg, int closestStop) {

        //BROADCAST MESSAGE TO ALL ACTIVITIES
        Intent intent = new Intent("postNotification");
        intent.putExtra("notification", msg);
        intent.putExtra("notificationIndex", closestStop);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        Log.d("monitor- POSTNOTI", msg);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
