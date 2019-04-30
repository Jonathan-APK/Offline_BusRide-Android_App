package com.mybusoffline.fypmybusoffline.UIClass;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import com.mybusoffline.fypmybusoffline.BusStop;
import com.mybusoffline.fypmybusoffline.Service.BusStopService;
import com.mybusoffline.fypmybusoffline.CustomAdapter.CustomSelectionAdapter;
import com.mybusoffline.fypmybusoffline.R;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by darks on 04-Jun-18.
 */

public class SelectNearbyBusList extends AppCompatActivity {

    Toolbar toolbar;
    String busNo;
    public static ArrayList<BusStop> filterList;
    public static ArrayList<BusStop> tempBusList;
    public static ArrayList<BusStop> originalTempBusList;
    public static ArrayAdapter<BusStop> busAdapter;
    ProgressBar spinner;
    TextView defaultNoListText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_nearbylist_layout);

        //TEXTVIEW FOR DEFAULT TEXT IF LIST EMPTY
        defaultNoListText = findViewById(R.id.noListText);

        //GET BUS NUMBER FROM PREVIOUS ACTIVITY
        busNo = (String) getIntent().getSerializableExtra("busno");

        //PROGRESSBAR FOR LISTVIEW
        spinner = findViewById(R.id.nearbyProgressBar);

        //SET SPINNER ANIMATION VISIBLE
        spinner.setVisibility(View.VISIBLE);

        //TOOLBAR AT THE TOP
        toolbar = findViewById(R.id.selectListToolbar1);
        toolbar.setTitle("BUS "+busNo);
        setSupportActionBar(toolbar);
        if(Build.VERSION.SDK_INT  >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.setElevation(10.f);
        }

        //ADD BACK BUTTON
        if(getSupportActionBar() != null) {
            //REMOVE TOOLBAR BOTTOM SHADOW
            getSupportActionBar().setElevation(0);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //RETRIEVE BUS ROUTE ARRAYLIST FROM PREVIOUS ACTIVITY (1 DIRECTION ONLY)
        originalTempBusList = (ArrayList<BusStop>) getIntent().getSerializableExtra("buslist");

        filterList = new ArrayList<>();
        tempBusList = new ArrayList<>();

        busAdapter = new CustomSelectionAdapter(this,filterList);
        ListView busListView = findViewById(R.id.busListView1);
        //IF LIST EMPTY, DISPLAY NO LIST MESSAGE
        busListView.setEmptyView(findViewById(R.id.noListText));
        busListView.setAdapter(busAdapter);

        //LISTVIEW ONCLICK LISTENER
        busListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Intent intent = new Intent();
                        intent.putExtra("startText",filterList.get(i).getDescription());
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }
        );

        //UPDATE LISTVIEW FOR NEARBY BUS-STOP
        updateListView();



    }



    //FOR BACK BUTTON
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if(id == android.R.id.home){
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    //TOOLBAR SEARCHVIEW
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_item, menu);
        MenuItem searchItem = menu.findItem(R.id.mySearch);
        MenuItem refreshItem = menu.findItem(R.id.myRefresh);
        SearchView searchview = (SearchView) searchItem.getActionView();

        //SEARCH BUTTON TOOLBAR ON QUERY LISTENER
        searchview.setOnQueryTextListener(new SearchView.OnQueryTextListener(){

              @Override
            public boolean onQueryTextSubmit(String s) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                busAdapter.getFilter().filter(s);
                return true;
            }
        });

        //ON ITEM CLICK FOR REFRESH BUTTON (TOOLBAR)
        refreshItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {

                //SET LISTVIEW AS EMPTY TO DISPLAY DEFAULT EMPTY-LIST TEXTS
                filterList.clear();
                SelectNearbyBusList.busAdapter.notifyDataSetChanged();

                //SET DEFAULT TEXT IF NO ITEM IN LISTVIEW AS EMPTY
                defaultNoListText.setText("");

                //SET SPINNER ANIMATION VISIBLE
                spinner.setVisibility(View.VISIBLE);

                //SET DELAY OF 2SEC FOR SPINNER ANIMATION
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        //SET SPINNER ANIMATION GONE
                        spinner.setVisibility(View.GONE);
                        //UPDATE LISTVIEW
                        updateListView();

                    }
                }, 2000);
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }




    //UPDATE LISTVIEW FOR NEARBY BUS-STOP
    public void updateListView(){

        Location myLocation = null;

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //PROMPT USER TO TURN ON GPS IF NOT ENABLED
        checkGPSOff(locationManager);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling

            return;
        }
        //GET LATEST GPS LOCATION
        myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);


        if(myLocation !=null) {

            Log.d("CL1", String.valueOf(myLocation.getLongitude()));

            //tempBusList =  BusStopService.busStopNearby(myLocation,originalTempBusList);
            filterList.clear();
            filterList.addAll((Collection<? extends BusStop>) BusStopService.busStopNearby(myLocation, originalTempBusList));

            //CLONE ARRAYLIST TO MAKE ANOTHER COPY WITHOUT SAME REFERENCE
            tempBusList = (ArrayList<BusStop>) filterList.clone();

            //UPDATE LISTVIEW
            SelectNearbyBusList.busAdapter.notifyDataSetChanged();

            //SET DEFAULT TEXT IF NO ITEM IN LISTVIEW DUE TO NO BUS STOP NEARBY
            defaultNoListText.setText("No Nearby Bus Stop Detected");
        }
        else{
            //SET DEFAULT TEXT IF NO ITEM IN LISTVIEW + NO GPS SIGNAL
            defaultNoListText.setText("Current Location Unknown. Please Try Again Later");

            //POPUP MESSAGE TO NOTIFY USER OF ARRIVAL
            AlertDialog.Builder warningMsg = new AlertDialog.Builder(this);
            warningMsg.setTitle("Current Location Unknown");
            warningMsg.setMessage("Current Location Unknown. You might be in a sheltered environment which resulted in the signal being weak. Please try again later. ");
            // Add the buttons
            warningMsg.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                }
            });
            warningMsg.show();
        }

        //SET SPINNER ANIMATION GONE
        spinner.setVisibility(View.GONE);


    }

    //PROMPT USER TO TURN ON GPS IF NOT ENABLED
    public void checkGPSOff(LocationManager locationManager){
        if ( !locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            // Call your Alert message
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final String action = Settings.ACTION_LOCATION_SOURCE_SETTINGS;
            final String message = "This app requires GPS to be turn on";

            builder.setMessage(message)
                    .setPositiveButton("Turn On",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int id) {

                                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    startActivity(intent);

                                    d.dismiss();
                                }
                            })
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int id) {
                                    d.cancel();
                                }
                            });
            builder.create().show();
        }
    }

}
