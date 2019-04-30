package com.mybusoffline.fypmybusoffline.UIClass;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mybusoffline.fypmybusoffline.BusStop;
import com.mybusoffline.fypmybusoffline.R;

import java.util.ArrayList;

/**
 * Created by darks on 29-May-18.
 */

public class BusRouteDirection  extends AppCompatActivity {

    Toolbar toolbar;
    TextView direction;
    TextView directionSource1;
    TextView directionSource2;
    TextView directionDestination2;
    TextView directionDestination1;
    ImageView arrow1;
    ImageView arrow2;
    String busNo;
    ArrayList<ArrayList<BusStop>> tempBusList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_direction_layout);

        directionSource1 = findViewById(R.id.source1);
        directionSource2 = findViewById(R.id.source2);
        directionDestination2 = findViewById(R.id.destination2);
        directionDestination1 = findViewById(R.id.destination1);
        arrow1 = findViewById(R.id.arrow1);
        arrow2 = findViewById(R.id.arrow2);

        //TEXTVIEW FOR "SELECT BUS SERVICE DIRECTION" / CHANGE FONT STYLE
        direction = findViewById(R.id.busDirectionText);
        Typeface font = Typeface.createFromAsset(getAssets(), "font/Baloo-Regular.ttf");
        direction.setTypeface(font);

        //TOOLBAR AT THE TOP
        busNo = (String) getIntent().getSerializableExtra("busno");
        toolbar = findViewById(R.id.directionToolbar);
        toolbar.setTitle("Bus Service: "+busNo);
        setSupportActionBar(toolbar);

        if(Build.VERSION.SDK_INT  >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.setElevation(10.f);
        }

        //ADD BACK BUTTON
        if(getSupportActionBar() != null) {

            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //SET TEXTVIEW FOR DIRECTION DETAILS
        tempBusList = (ArrayList<ArrayList<BusStop>>) getIntent().getSerializableExtra("buslist");
        int listSize = tempBusList.get(0).size();

        directionSource1.setText(tempBusList.get(0).get(0).getDescription());
        directionDestination1.setText(tempBusList.get(0).get(listSize-1).getDescription());

        listSize = tempBusList.get(1).size();

        directionSource2.setText(tempBusList.get(1).get(0).getDescription());
        directionDestination2.setText(tempBusList.get(1).get(listSize-1).getDescription());

        //ONCLICKLISTENER FOR DIRECTION ARROW 1
        arrow1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                //PASS BUSNO AND ARRAYLIST<BUSLIST> FOR DIRECTION 1
                Intent intent = new Intent(getApplicationContext(), TripOverview.class);
                intent.putExtra("buslist", tempBusList.get(0));
                intent.putExtra("description", "null");
                intent.putExtra("busno",busNo);
                startActivity(intent);

                Log.d("DIRECTION","1");
            }
        });

        //ONCLICKLISTENER FOR DIRECTION ARROW 2
        arrow2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                //PASS BUSNO AND ARRAYLIST<BUSLIST> FOR DIRECTION 2
                Intent intent = new Intent(getApplicationContext(), TripOverview.class);
                intent.putExtra("buslist", tempBusList.get(1));
                intent.putExtra("description", "null");
                intent.putExtra("busno",busNo);
                startActivity(intent);

                Log.d("DIRECTION","2");

            }
        });

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

}

