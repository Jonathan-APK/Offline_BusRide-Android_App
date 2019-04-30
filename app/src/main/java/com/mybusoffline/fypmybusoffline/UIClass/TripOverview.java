package com.mybusoffline.fypmybusoffline.UIClass;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.mybusoffline.fypmybusoffline.BusStop;
import com.mybusoffline.fypmybusoffline.CustomAdapter.CustomOverviewAdapter;
import com.mybusoffline.fypmybusoffline.Service.JsonService;
import com.mybusoffline.fypmybusoffline.R;

import java.util.ArrayList;

/**
 * Created by darks on 31-May-18.
 */

public class TripOverview extends AppCompatActivity {

    Toolbar toolbar;
    EditText startText;
    EditText endText;
    TextView distanceText;
    TextView noOfStopsText;
    ArrayList<BusStop> tempBusList;
    ArrayList<BusStop> overViewList;
    ArrayList<BusStop> routeList;
    ArrayAdapter<BusStop> busAdapter;
    String busNo;
    Button startJourney;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trip_overview_layout);

        startText = findViewById(R.id.startPointText);
        endText = findViewById(R.id.endPointText);
        distanceText = findViewById(R.id.distText);
        noOfStopsText = findViewById(R.id.noOfStopText);
        startJourney = findViewById(R.id.startJourneyBtn);

        //RETRIEVE BUS ROUTE ARRAYLIST FROM PREVIOUS ACTIVITY (1 DIRECTION ONLY)
        tempBusList = (ArrayList<BusStop>) getIntent().getSerializableExtra("buslist");

        //SET START TEXT IF USER SCAN QR CODE
        if(!getIntent().getStringExtra("description").equals("null")){
            startText.setText(getIntent().getStringExtra("description"));
        }

        //TOOLBAR AT THE TOP
        busNo = (String) getIntent().getSerializableExtra("busno");
        toolbar = findViewById(R.id.tripOverviewToolbar);
        toolbar.setTitle("BUS " + busNo);
        setSupportActionBar(toolbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.setElevation(10.f);
        }

        //ADD BACK BUTTON
        if (getSupportActionBar() != null) {
            //REMOVE TOOLBAR BOTTOM SHADOW
            getSupportActionBar().setElevation(0);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //SETUP LISTVIEW
        overViewList = new ArrayList<>();
        int[] imageSrc = {R.drawable.ptp4, R.drawable.ptp5, R.drawable.ptp6};
        busAdapter = new CustomOverviewAdapter(this, overViewList, imageSrc);
        ListView busListView = findViewById(R.id.overviewListView);
        busListView.setAdapter(busAdapter);

        //ONCLICK LISTENER FOR START POINT EDITTEXT
        startText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SelectNearbyBusList.class);
                intent.putExtra("buslist", tempBusList);
                intent.putExtra("busno", busNo);
                startActivityForResult(intent, 1);
            }
        });

        //ONCLICK LISTENER FOR END POINT EDITTEXT
        endText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SelectAllBusList.class);
                intent.putExtra("startpoint", startText.getText().toString());
                intent.putExtra("buslist", tempBusList);
                intent.putExtra("busno", busNo);
                startActivityForResult(intent, 2);
            }
        });
    }

    //FOR BACK BUTTON
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    //WHEN COMING BACK FROM PREVIOUS ACTIVITY (PASS TEXT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                startText.setText(data.getStringExtra("startText"));
                endText.setText(null);
                distanceText.setText("Distance: - km");
                noOfStopsText.setText("No. of Stops: -");

                //CLEAR LISTVIEW
                overViewList.clear();
                busAdapter.notifyDataSetChanged();

                //HIDE 'START JOURNEY' BUTTON
                startJourney.setVisibility(View.INVISIBLE);


            }
        } else if (requestCode == 2) {
            if (resultCode == RESULT_OK) {
                endText.setText(data.getStringExtra("endText"));
            }
        }

        //IF STARTTEXT & ENDTEXT IS NOT EMPTY
        if (startText.getText().toString().length() > 0 && endText.getText().toString().length() > 0) {

            //CALCULATE NO OF STOPS AND DISTANCE FOR THE TRIP BASED ON INPUT CHOSEN
            String tempDist = "Distance: " + JsonService.calculateRouteDistance(tempBusList, startText.getText().toString(), endText.getText().toString()) + " km";
            String tempStops = "No. of Stops: " + JsonService.calculateNoOfStops(tempBusList, startText.getText().toString(), endText.getText().toString());

            //SET TEXTVIEW FOR DISTANCE AND STOPS
            distanceText.setText(tempDist);
            noOfStopsText.setText(tempStops);

            //DISPLAY LISTVIEW FOR OVERVIEW OF JOURNEY, CLEAR AND RE-ADD ARRAYLIST, UPDATE ADAPTER
            overViewList.clear();
            routeList = JsonService.filterList(startText.getText().toString(), endText.getText().toString(), tempBusList);
            overViewList.addAll(routeList);
            busAdapter.notifyDataSetChanged();

            //SHOW 'START JOURNEY' BUTTON
            startJourney.setVisibility(View.VISIBLE);
        }
    }

    //ONCLICK LISTENER FOR 'START JOURNEY' BUTTON
    public void startJourney(View view) {

        Intent intent = new Intent(this, BusMonitoring.class);
        intent.putExtra("routelist", routeList);
        intent.putExtra("busno", busNo);
        intent.putExtra("laststopdesc", tempBusList.get(tempBusList.size() -1).getDescription());

        startActivity(intent);

    }
}
