package com.mybusoffline.fypmybusoffline.Service;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.WindowManager;

/**
 * Created by darks on 07-Jun-18.
 */

//GET GPS COORDINATES OF THE USER. RECEIVE REAL-TIME UPDATE OF THE USER GPS COORDINATES
public class GPSService extends Service{

    private LocationManager locationManager;
    private LocationListener locationListener;

    private void locationManager() {

        Log.d("LocationManager", "initializeLocationManager");
        if (locationManager == null) {

            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            locationListener = new LocationListener();

            //CHECK SDK VERSION
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Log.d("GPS update", "build true");

                if (!(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                    Log.d("GPS update2", "build true");

                    //REQUEST LOCATION UPDATE
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, locationListener);

                }
            }

        }
    }


    //LISTEN FOR CHANGED TO GPS COORDINATES
    private class LocationListener implements android.location.LocationListener{

        @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            //CHECK IF GPS HAS CHANGED
            @Override
            public void onLocationChanged(Location location) {
                String coordinates = location.getLatitude() + " ," + location.getLongitude();

                //BROADCAST GPS LOCATION TO ALL ACTIVITY
                Intent intent = new Intent("currentGPS");
                intent.putExtra("currentLocation", location);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                Log.d("GPS update3", coordinates);

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            //IF GPS IS TURNED OFF
            @Override
            public void onProviderDisabled(String s) {

            }

    }

    @Override
    public void onCreate() {
        Log.d("gpsservicestart", "onStartCommand");
        locationManager();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.d("start", "onStartCommand");
        super.onStartCommand(intent, flags, startId);

        //START_NOT_STICKY KILLS SERVICES AFTER APP DIED
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy()
    {
        Log.d("GPSSservice Died", "onDestroy");
        super.onDestroy();
        if (locationManager != null) {
                try {
                    locationManager.removeUpdates(locationListener);
                } catch (Exception ex) {
                    Log.d("gps exception", "fail to remove location listners, ignore", ex);
                }
            }
        }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
