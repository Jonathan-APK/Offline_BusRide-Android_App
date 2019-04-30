package com.mybusoffline.fypmybusoffline.UIClass;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.mybusoffline.fypmybusoffline.BusStop;
import com.mybusoffline.fypmybusoffline.Service.JsonModel;
import com.mybusoffline.fypmybusoffline.Service.JsonService;
import com.mybusoffline.fypmybusoffline.R;

import java.util.ArrayList;

/**
 * Created by darks on 27-May-18.
 */

public class SelectBusService extends AppCompatActivity {

    Toolbar toolbar;
    TextView trackText;
    AutoCompleteTextView autofield;
    Button next;
    ImageView dropDownImage;
    ArrayList<ArrayList<BusStop>> tempBusList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_bus_layout);

        //HIDE KEYBOARD WHEN TAP OUTSIDE TEXTVIEW
        findViewById(R.id.selectBusConstraintLayout).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                return true;
            }
        });

        //TEXTVIEW FOR "SELECT BUS SERVICE NO." / CHANGE FONT STYLE
        trackText = findViewById(R.id.selectBusText);
        Typeface font = Typeface.createFromAsset(getAssets(), "font/Baloo-Regular.ttf");
        trackText.setTypeface(font);

        //TOOLBAR AT THE TOP
        toolbar = findViewById(R.id.busLayoutToolbar);
        toolbar.setTitle("BUS SERVICE");
        setSupportActionBar(toolbar);

        if(Build.VERSION.SDK_INT  >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.setElevation(10.f);
        }

        //ADD BACK BUTTON
        if(getSupportActionBar() != null) {

            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //AUTOCOMPLETETEXTVIEW DROPDOWN FEATURE
        autofield = findViewById(R.id.selectBusTextview);
        dropDownImage = findViewById(R.id.dropdownArrowImage);
        autofield.setThreshold(1);

        ArrayAdapter<String> adapter= new ArrayAdapter<>(this,android.R.layout.simple_dropdown_item_1line, JsonModel.getBusServiceList(getApplicationContext()));
        autofield.setAdapter(adapter);

        dropDownImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                autofield.showDropDown();
            }
        });


        //"NEXT" BUTTON
        next = findViewById(R.id.selectBusNextBtn);

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

    //ONCLICK NEXT BUTTON
    public void selectBusNext(View view){

        //RETRIEVE LIST OF VALID BUS SERVICE FROM JSON FILE
        String[] tempList = JsonModel.getBusServiceList(getApplicationContext());
        boolean validBusNo = false;

        //CHECK FOR VALID BUS SERVICE
        for(int i=0; i<tempList.length; i++){

            if(tempList[i].equals(autofield.getText().toString())){
                validBusNo = true;
                break;
            }
        }

        //PROCESS TO NEXT PAGE IF VALID BUS NUMBER IS INPUTTED
        if(validBusNo){

           tempBusList = JsonService.retrieveBusRouteList(autofield.getText().toString(),getApplicationContext());

            //FOR 2 DIRECTION BUS ROUTE
            if (tempBusList.size() > 1) {
                Intent intent = new Intent(this, BusRouteDirection.class);
                intent.putExtra("buslist", tempBusList);
                intent.putExtra("busno",autofield.getText().toString());
                startActivity(intent);
            }
            //FOR 1 DIRECTION BUS ROUTE
            else{
                Intent intent = new Intent(this, TripOverview.class);
                intent.putExtra("buslist", tempBusList.get(0));
                intent.putExtra("description", "null");
                intent.putExtra("busno",autofield.getText().toString());
                startActivity(intent);

            }


        }
        //ALERT DIALOG IF INVALID BUS NUMBER IS INPUTTED
        else{
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Invalid Bus Number!!");
            alertDialog.setMessage("Please Input a Correct Bus Number.");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }

    }


}
