package com.mybusoffline.fypmybusoffline.UIClass;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;

import com.mybusoffline.fypmybusoffline.BusStop;
import com.mybusoffline.fypmybusoffline.CustomAdapter.CustomSelectionAdapter;
import com.mybusoffline.fypmybusoffline.Service.JsonService;
import com.mybusoffline.fypmybusoffline.R;

import java.util.ArrayList;

/**
 * Created by darks on 04-Jun-18.
 */

public class SelectAllBusList extends AppCompatActivity{

    Toolbar toolbar;
    String busNo;
    String startPoint;
    public static ArrayList<BusStop> tempBusList;
    public static ArrayList<BusStop> originalTempBusList;
    public static ArrayAdapter<BusStop> busAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_alllist_layout);

        //RETRIEVE OBJECT FROM PREVIOUS ACTIVITY
        busNo = (String) getIntent().getSerializableExtra("busno");
        startPoint = getIntent().getStringExtra("startpoint");

        //TOOLBAR AT THE TOP
        toolbar = findViewById(R.id.selectListToolbar2);
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

        //FILTER LIST BASED ON STARTING BUS STOP SO LIST ONLY SHOWS BUS STOP AFTER THE START STOP
        originalTempBusList = JsonService.filterList(startPoint, null, originalTempBusList);

        tempBusList = new ArrayList<BusStop>(originalTempBusList.size());

        //CLONE ARRAYLIST TO MAKE ANOTHER COPY WITHOUT SAME REFERENCE
        tempBusList = (ArrayList<BusStop>) originalTempBusList.clone();

        //SETUP LISTVIEW
        busAdapter = new CustomSelectionAdapter(this,tempBusList);
        ListView busListView = findViewById(R.id.busListView2);
        busListView.setAdapter(busAdapter);

        //LISTVIEW ONCLICK LISTENER
        busListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Intent intent = new Intent();
                        intent.putExtra("endText",tempBusList.get(i).getDescription());
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }
        );
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
        MenuItem item = menu.findItem(R.id.mySearch);
        SearchView searchview = (SearchView) item.getActionView();
        menu.findItem(R.id.myRefresh).setVisible(false);

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
        return super.onCreateOptionsMenu(menu);
    }



}
