package com.mybusoffline.fypmybusoffline.UIClass;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.mybusoffline.fypmybusoffline.BusStop;
import com.mybusoffline.fypmybusoffline.CustomAdapter.CustomMonitorAdaptor;
import com.mybusoffline.fypmybusoffline.Service.MonitoringService;
import com.mybusoffline.fypmybusoffline.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by darks on 15-Jun-18.
 */

public class BusMonitoring extends AppCompatActivity {

    Toolbar toolbar;
    TextView nextStopText;
    TextView busDescText;
    TextView roadNameText;
    TextView busCodeText;
    TextView remainingStopNoText;
    TextView busNoTextview;
    String busNo;
    ArrayList<BusStop> routeList;
    RecyclerView recyclerView;
    CustomMonitorAdaptor adapter;
    public static final String CHANNEL_1_ID = "channel1";
    public static final String CHANNEL_2_ID = "channel2";
    private NotificationManagerCompat notificationManager;
    boolean dialogShown;
    String lastStopDesc;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bus_monitoring_layout);
        nextStopText = findViewById(R.id.busMonitoringNextStopTextView);
        busDescText = findViewById(R.id.busMonitoringBusDescTextView);
        roadNameText = findViewById(R.id.busMonitoringRoadNameTextView);
        busCodeText = findViewById(R.id.busMonitoringBusCodeTextView);
        remainingStopNoText = findViewById(R.id.busMonitoringRemainingStopNoTextView);
        busNoTextview = findViewById(R.id.busNoTextView);
        notificationManager = NotificationManagerCompat.from(this);
        dialogShown = false;

        //RETRIEVE DATA FROM PREVIOUS ACTIVITY
        busNo = (String) getIntent().getSerializableExtra("busno");
        routeList = (ArrayList<BusStop>) getIntent().getSerializableExtra("routelist");
        lastStopDesc =getIntent().getStringExtra("laststopdesc");

        //SET TEXT FOR TEXTVIEW
        String serviceNo = "BUS " +  busNo;
        busNoTextview.setText(serviceNo);
        remainingStopNoText.setText(String.valueOf(routeList.size() -1));
        busCodeText.setText(routeList.get(1).getBusStopCode());
        busDescText.setText(routeList.get(1).getDescription());
        roadNameText.setText(routeList.get(1).getRoadName());

        //TEXT UNDERLINE FOR 'NEXT STOP'
        nextStopText.setPaintFlags(nextStopText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        //TOOLBAR AT THE TOP
        toolbar = findViewById(R.id.busMonitoringToolbar);
        //toolbar.setTitle("119");
        setSupportActionBar(toolbar);

        if(Build.VERSION.SDK_INT  >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.setElevation(10.f);
        }

        //SETUP RECYCLERVIEW LIST
        initRecyclerView();

        //REGISTER LOCAL BROADCAST RECEIVER
        LocalBroadcastManager.getInstance(this).registerReceiver(updateRowUi,
                new IntentFilter("setCurrentLocationUI"));
        LocalBroadcastManager.getInstance(this).registerReceiver(postNotification,
                new IntentFilter("postNotification"));

        //REGISTER RECEIVER FOR GPS OFF
        registerReceiver(gpsOffReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));

        //CREATE NOTIFICATION CHANNEL
        createNotificationChannels();

        //START MONITORING SERVICE
        Intent serviceIntent = new Intent(this,MonitoringService.class);
        serviceIntent.putExtra("routelist", routeList);
        serviceIntent.putExtra("laststopdesc",lastStopDesc);
        startService(serviceIntent);

        //ADD BACK BUTTON
        if(getSupportActionBar() != null) {
            //REMOVE TOOLBAR BOTTOM SHADOW
            getSupportActionBar().setElevation(0);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            this.getSupportActionBar().setHomeAsUpIndicator(android.R.drawable.ic_input_delete);

        }

    }

    //BACK PHYSICAL BUTTON ON ANDROID
    @Override
    public void onBackPressed() {

        //PROMPT ALERT WARNING WHEN PRESSED BACK
        AlertDialog.Builder warningMsg = new AlertDialog.Builder(this);
        warningMsg.setTitle("End Monitoring?");
        warningMsg.setMessage("Are you sure you want to end monitoring service?");
        // Add the buttons
        warningMsg.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                //RETURN TO PREVIOUS PAGE
                BusMonitoring.super.onBackPressed();
            }
        });

        //DO NOTHING
        warningMsg.setNegativeButton("No",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });
        warningMsg.show();
    }

    //FOR ON TOUCH ITEM ON TOOLBAR
    //UNREGISTER RECEIVER & STOP SERVICE ARE DONE AT ONDESTROY()
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                //PROMPT ALERT WARNING WHEN PRESSED BACK
                AlertDialog.Builder warningMsg = new AlertDialog.Builder(this);
                warningMsg.setTitle("End Monitoring?");
                warningMsg.setMessage("Are you sure you want to end monitoring service?");
                // Add the buttons
                warningMsg.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        //RETURN TO PREVIOUS PAGE
                        finish();
                    }
                });

                //DO NOTHING
                warningMsg.setNegativeButton("No",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
                warningMsg.show();
                break;

            //ONTOUCH SETTING ICON
            case R.id.setting:

                //SETUP SHARED PREFS FOR STORING INT
                SharedPreferences prefs = getSharedPreferences("setting", MODE_PRIVATE);

                //NUMBERPICKER WITH MIN, MAX, DEFAULT VALUE
                final NumberPicker numberPicker = new NumberPicker(getApplicationContext());
                numberPicker.setMinValue(1);
                numberPicker.setMaxValue(4);
                //SET VALUE (DEFAULT 2)
                numberPicker.setValue(prefs.getInt("notificationIndex",2));

                //PROMPT ALERT WARNING WHEN PRESSED BACK
                AlertDialog.Builder settingDialog = new AlertDialog.Builder(this,AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                settingDialog.setTitle("Notification Reminder - Alighting");
                settingDialog.setMessage("Number Of Stops Before Destination");
                // Add the buttons
                settingDialog.setPositiveButton("Set", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        //SAVE INT INTO SHARED PREFS
                        SharedPreferences.Editor prefsEditor = getSharedPreferences("setting", MODE_PRIVATE).edit();
                        prefsEditor.putInt("notificationIndex", numberPicker.getValue());
                        prefsEditor.apply();

                    }
                });

                //DO NOTHING
                settingDialog.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
                settingDialog.setView(numberPicker);
                settingDialog.show();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;

    }

    //INITIAL SETUP FOR RECYCLERVIEW
    public void initRecyclerView(){

        int[] imageSrc = {R.drawable.ptp4, R.drawable.ptp5, R.drawable.ptp6,R.drawable.ptp7,R.drawable.ptp8,R.drawable.ptp9};
        recyclerView = findViewById(R.id.monitoring_recycler_view);
        adapter = new CustomMonitorAdaptor(routeList, imageSrc , this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    //UPDATE BACKGROUND FOR ROW
    private BroadcastReceiver updateRowUi = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String index = intent.getStringExtra("locationIndex");

            int removeIndex = 0;
            boolean[] tempBackgoundBorder = adapter.getSetBackground();
            boolean[] tempPinBackground = adapter.getSetPinImage();

            //CHECK WHICH BACKGROUND HAS BEEN SET PREVIOUSLY TO GET ITS INDEX
            for(int i=0; i< tempBackgoundBorder.length; i++){
                if(tempBackgoundBorder[i]){
                    removeIndex = i;
                }
            }

            //UPDATE BACKGROUND FOR PIN
            Arrays.fill(tempPinBackground, false);

            //SET GREY PIN IMAGE FOR THOSE BEFORE CURRENT STOP AND CURRENT STOP
            for(int i=0; i< Integer.parseInt(index)+1; i++){

                tempPinBackground[i] = true;
            }


            Log.d("monitor- setUI (index)", index);

            String remainingStop = Integer.toString(routeList.size() -1 - Integer.parseInt(index));

            //UPDATE TEXTVIEW FOR REMAINING STOP NUMBER
            remainingStopNoText.setText(remainingStop);

            //UPDATE TEXTVIEW FOR NEXT STOP INFO IF USER NOT AT LAST STOP
            if(routeList.size()-1 != Integer.parseInt(index)){
                busDescText.setText(routeList.get(Integer.parseInt(index)+1).getDescription());
                roadNameText.setText(routeList.get(Integer.parseInt(index)+1).getRoadName());
                busCodeText.setText(routeList.get(Integer.parseInt(index)+1).getBusStopCode());
            }

            //UPDATE BACKGROUND FOR ROUNDED BORDER
            Arrays.fill(tempBackgoundBorder, false);

           /* //ENSURE NO INDEX OUT OF BOUND ERROR WHEN UPDATING NEXT STOP UI
            if(Integer.parseInt(index)  < routeList.size() -1) {

            }*/
            tempBackgoundBorder[Integer.parseInt(index)] = true;
            adapter.setSetBackground(tempBackgoundBorder);
            adapter.setSetPinImage(tempPinBackground);
            adapter.notifyItemChanged(removeIndex);

            for(int i=0; i<=Integer.parseInt(index); i++){
                adapter.notifyItemChanged(i);
            }

            //UPDATE ROW WITH NEXT STOP IMAGE
            if(Integer.parseInt(index) < routeList.size() -1) {
                Log.d("NEXTSTOP IMAGE", String.valueOf(Integer.parseInt(index+1)));
                adapter.notifyItemChanged(Integer.parseInt(index)+1);
            }
            recyclerView.scrollToPosition(Integer.parseInt(index));

        }
    };

    //POST NOTIFICATION MESSAGE
    private BroadcastReceiver postNotification = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {


            // Get extra data included in the Intent
            String msg = intent.getStringExtra("notification");
            int index = intent.getIntExtra("notificationIndex", -1);
            Log.d("postNotificationHERE",msg);

            if(msg.equals("reaching")){
                Log.d("postNotification","reaching");

                Intent activityIntent = new Intent(getApplicationContext(), BusMonitoring.class);
                activityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(),0,activityIntent,0);

                Notification notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_1_ID)
                        .setSmallIcon(R.drawable.bus)
                        .setContentTitle("BUSRIDE")
                        .setContentText("Next Stop - "+routeList.get(index+1).getDescription() + ". (Remaining Stop: "+
                                Integer.toString(routeList.size() - 1 - index)+")")
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                        .setContentIntent(contentIntent)
                        .build();

                notificationManager.notify(1, notification);

                //POPUP MESSAGE TO ALERT USER OF REMAINING STOP
                displayAlert("arriving", Integer.toString(routeList.size() - 1 - index),routeList.get(index+1).getDescription());

                //VIBRATION EFFECT
                //CHECK IF NEXT STOP IS DESTINATION
                if(routeList.size() - 1 - index ==1){
                    vibrateEffort("long");
                }
                else{
                    vibrateEffort("short");
                }
            }
            else if(msg.equals("reached")){
                Log.d("postNotification","reached");

                Intent activityIntent = new Intent(getApplicationContext(), BusMonitoring.class);
                activityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(),0,activityIntent,0);

                Notification notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_2_ID)
                        .setSmallIcon(R.drawable.bus)
                        .setContentTitle("BUSRIDE")
                        .setContentText("Destination Arrived - " + routeList.get(routeList.size() -1).getDescription())
                        .setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                        .setContentIntent(contentIntent)
                        .build();

                notificationManager.notify(2, notification);

                //POPUP MESSAGE TO ALERT USER OF ARRIVAL
                displayAlert("destinationarrived",null,null);

                //UPDATE TEXTFIELD TO DESTINATION ARRIVED FOR BUS DESC TEXT
                busDescText.setText("Destination Arrived");
                roadNameText.setText("");
                busCodeText.setText("");

                //VIBRATION EFFECT
                vibrateEffort("short");
            }

        }
    };

    //NOTIFICATION CHANNELS FOR NOTIFICATION MESSAGE
    private void createNotificationChannels(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            NotificationChannel channel1 = new NotificationChannel(
                    CHANNEL_1_ID,"Channel 1", NotificationManager.IMPORTANCE_HIGH);
            channel1.setDescription("Reaching");
            channel1.enableVibration(true);
            channel1.enableLights(true);

            NotificationChannel channel2 = new NotificationChannel(
                    CHANNEL_2_ID,"Channel 2", NotificationManager.IMPORTANCE_HIGH);
            channel2.setDescription("Destination Arrived");
            channel2.enableVibration(true);
            channel2.enableLights(true);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);
            manager.createNotificationChannel(channel2);

        }
    }

    //NOTIFICATION TO TURN ON GPS
    private BroadcastReceiver gpsOffReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("off9", String.valueOf(dialogShown));

            final LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);

            if (!manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) && intent.getAction().matches(LocationManager.PROVIDERS_CHANGED_ACTION) && !dialogShown ) {

                    dialogShown = true;

                    // Call your Alert message
                    final AlertDialog.Builder builder = new AlertDialog.Builder(BusMonitoring.this);
                    final String message = "This app requires GPS to be turn on";

                    builder.setMessage(message)
                            .setPositiveButton("Turn On",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface d, int id) {

                                            dialogShown = false;
                                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                            startActivity(intent);
                                            d.dismiss();
                                        }
                                    })
                            .setNegativeButton("Cancel",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface d, int id) {
                                            dialogShown = false;
                                            d.cancel();
                                        }
                                    });
                    builder.create().show();
            }
        }
    };

    //FOR TOOLBAR SETTING OPTION
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_setting, menu);
        return true;
    }

    //CREATE VIBRATE EFFECT
    private void vibrateEffort(String duration){

        final Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long[] mVibratePattern = new long[]{0, 2000, 4000, 2000, 4000, 2000};

       if(duration.equals("long")){
           //VIBRATE EFFECT
           if (Build.VERSION.SDK_INT >= 26) {
               vibe.vibrate(VibrationEffect.createWaveform(mVibratePattern,0));

               //DELAY 14SEC THEN CANCEL VIBRATION
               new Handler().postDelayed(new Runnable() {
                   @Override
                   public void run() {
                     vibe.cancel();
                   }
               }, 14000);

               Log.d("VIBRATE","1");

           }
           else{
               vibe.vibrate(mVibratePattern,-1);
           }
       }
       else{

           //VIBRATE EFFECT
           if (Build.VERSION.SDK_INT >= 26) {
               vibe.vibrate(VibrationEffect.createOneShot(2000, VibrationEffect.DEFAULT_AMPLITUDE));
               Log.d("VIBRATE","2");

           }
           else{
               vibe.vibrate(2000);
           }
       }


    }

    //POPUP ALERT MESSAGE
    private void displayAlert(String type, String stopLeft, String stopName)
    {
        if(type.equals("destinationarrived")) {

            //POPUP MESSAGE TO NOTIFY USER OF ARRIVAL
            AlertDialog.Builder warningMsg = new AlertDialog.Builder(this);
            warningMsg.setTitle("Destination Arrived");
            warningMsg.setMessage("You have arrived at your destination stop!");
            // Add the buttons
            warningMsg.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                }
            });
            warningMsg.show();
        }
        //ARRIVING AND APP NOT IN FOREGROUND
        else if (type.equals("arriving") && appInForeground(this)){
            //POPUP MESSAGE TO NOTIFY USER OF ARRIVAL
            AlertDialog.Builder warningMsg = new AlertDialog.Builder(this);
            warningMsg.setTitle("Remaining Stop: "+stopLeft);
            warningMsg.setMessage("Next Stop: "+stopName);
            // Add the buttons
            warningMsg.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                }
            });
            warningMsg.show();
        }
    }

    //CHECK IF APP IN FOREGROUND
    private boolean appInForeground(@NonNull Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = activityManager.getRunningAppProcesses();
        if (runningAppProcesses == null) {
            return false;
        }

        for (ActivityManager.RunningAppProcessInfo runningAppProcess : runningAppProcesses) {
            if (runningAppProcess.processName.equals(context.getPackageName()) &&
                    runningAppProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //UNREGISTER RECEIVER
        LocalBroadcastManager.getInstance(this).unregisterReceiver(updateRowUi);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(postNotification);
        unregisterReceiver(gpsOffReceiver);

        //STOP MONITORING SERVICE
        stopService(new Intent(this, MonitoringService.class));

        //REMOVE ALL PAST NOTIFICATION
        notificationManager.cancelAll();

    }
}
