package com.mybusoffline.fypmybusoffline.UIClass;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.Result;
import com.mybusoffline.fypmybusoffline.BusStop;
import com.mybusoffline.fypmybusoffline.Service.GPSService;
import com.mybusoffline.fypmybusoffline.Service.JsonModel;
import com.mybusoffline.fypmybusoffline.Service.JsonService;
import com.mybusoffline.fypmybusoffline.R;

import org.w3c.dom.Text;

import java.util.ArrayList;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class MainActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private Toolbar toolbar;
    private static final int REQUEST_CAMERA = 1;
    private ZXingScannerView scannerView;
    private PackageManager pm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //GET PACKAGE MANAGER
        pm = getPackageManager();

        //TEXTVIEW FOR "Track your Journey" / CHANGE FONT STYLE
        changeTextFont();

        //SETUP TOOLBAR AT THE TOP
        toolbarSetup();

        //CHECK AND PROMPT USER TO TURN ON GPS IF NOT ENABLED
        checkGPSOn();
    }

    //TEXTVIEW FOR "Track your Journey" / CHANGE FONT STYLE
    public void changeTextFont(){

        TextView trackText = findViewById(R.id.trackText);
        Typeface font = Typeface.createFromAsset(getAssets(), "font/Baloo-Regular.ttf");
        trackText.setTypeface(font);
    }

    //SETUP TOOLBAR AT THE TOP
    public void toolbarSetup(){

        toolbar = findViewById(R.id.mainToolbar);
        toolbar.setTitle("BUSRIDE");
        setSupportActionBar(toolbar);

        if(Build.VERSION.SDK_INT  >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.setElevation(10.f);
        }

    }

    //CHECK AND PROMPT USER TO TURN ON GPS IF NOT ENABLED
    public void checkGPSOn(){

        //PRE-CHECK SDK VERSION FOR GPS SERVICES
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d("GPS update0", "build true");

                //REQUEST PERMISSION FROM USER
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.INTERNET
                }, 10);
            }

        }

        //PROMPT USER TO TURN ON GPS IF NOT ENABLED
        final LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
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
                                    startService(new Intent(getApplicationContext(), GPSService.class));

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
        else{
            Log.d("startGPS","1");
            startService(new Intent(this, GPSService.class));
        }
    }

    //ONCLICK SELECT BUS SERVICE BUTTON
    public void selectBusGo(View view){

        startActivity(new Intent(this, SelectBusService.class));
    }

    //ONCLICK SCAN QR CODE BUTTON
    public void scanQRCode(View view){

        scannerView = new ZXingScannerView(this);
        setContentView(scannerView);

        //CHECK IF PHONE HAS CAMERA SUPPORT
        if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if (checkPermission()) {
                    if(scannerView == null) {
                        scannerView = new ZXingScannerView(this);
                        setContentView(scannerView);
                    }
                    scannerView.setResultHandler(this);
                    scannerView.startCamera();
                }
                else{
                    requestPermission();
                }
            }
        }
        else{
            //POPUP MESSAGE TO NOTIFY USER OF NO CAMERA SUPPORT
            AlertDialog.Builder warningMsg = new AlertDialog.Builder(this);
            warningMsg.setTitle("No Camera Support");
            warningMsg.setMessage("No camera detected on device");
            // Add the buttons
            warningMsg.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    //DO NOTHING
                }
            });
            warningMsg.show();
        }
    }

    //CHECK PERMISSION FOR CAMERA
    private boolean checkPermission(){
        return (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);
    }

    //REQUEST PERMISSION FOR CAMERA ACCESS
    private void requestPermission(){
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
    }

    //RESULT FROM PERMISSION REQUEST FOR CAMERA ACCESS
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA:
                if (grantResults.length > 0) {

                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    //IF CAMERA REQUEST GRANTED
                    if (cameraAccepted){
                        Toast.makeText(getApplicationContext(), "Permission Granted, Now you can access camera", Toast.LENGTH_LONG).show();
                        //START CAMERA
                        scannerView.setResultHandler(this);
                        scannerView.startCamera();
                     //REQUEST DENIED, PROMPT ANOTHER MESSAGE
                    }else {
                        Toast.makeText(getApplicationContext(), "Permission Denied, You cannot access camera", Toast.LENGTH_LONG).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                                showMessageOKCancel("You need to allow access to both the permissions");
                            }
                        }
                    }
                }
                break;
        }
    }

    //DISPLAY ALERT MESSAGE TO ASK USER TO ALLOW PERMISSION
    private void showMessageOKCancel(String message) {
        new android.support.v7.app.AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK",  new DialogInterface.OnClickListener() {
                    @Override
                    //IF USER TAP OK, REQUEST CAMERA PERMISSION AGAIN
                    public void onClick(DialogInterface dialog, int which) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            //REQUEST CAMERA PERMISSION
                            requestPermissions(new String[]{Manifest.permission.CAMERA},
                                    REQUEST_CAMERA);
                        }
                    }
                })
                //IF USER PRSS CANCEL, GO BACK TO PREVIOUS PAGE
                .setNegativeButton("Cancel",  new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            //GO BACK PREVIOUS ACTIVITY
                            onBackPressed();
                        }
                    }
                })
                .create()
                .show();
    }

    //HANDLE ACTION AFTER SCANNING QR CODE
    //VALID QRCODE FORMAT: BUSRIDE:Description:BusStopCode;BusNo:direction;BusNo:direction
    @Override
    public void handleResult(Result result) {

        final String myResult = result.getText();
        Log.d("QRCodeScanner", result.getText());
        Log.d("QRCodeScanner", result.getBarcodeFormat().toString());

        //DELIMIT STRING FROM QR CODE
        final String[] qrData = myResult.split(";");

        //CHECK IF QR CODE IS VALID
        if(checkValidQrCode(qrData)){

            //STORE BUS STOP DESCRIPTION & CODE
            String busDescription = qrData[0].split(":")[1];
            String busCode = qrData[0].split(":")[2];

            //STORE LIST OF BUSNO AND DIRECTION
            final ArrayList<String> tempBusNo = new ArrayList<>();
            final ArrayList<String> tempDirection = new ArrayList<>();

            //RETRIEVE LIST OF BUSNO AND DIRECTION FROM QR CODE
            for(int i=1; i<qrData.length; i++){
                String[] temp = qrData[i].split(":");
                tempBusNo.add(temp[0]);
                tempDirection.add(temp[1]);
            }

            //REFRESH CONTENTVIEW AND SET TOOLBAR TEXT
            refreshLayout();

            //SHOW DIALOG MESSAGE LISTING ALL BUS NO. AT THE BUS STOP
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            //CUSTOM TITLE DIALOG VIEW
            LayoutInflater inflater = getLayoutInflater();
            View view=inflater.inflate(R.layout.custom_qrcode_dialog, null);
            builder.setCustomTitle(view);

            //SET TEXT FOR CUSTOM DIALOG VIEW (BUS DESCRIPTION AND BUS STOP CODE)
            TextView busDescriptionText = view.findViewById(R.id.dialogBusDescriptionText);
            TextView busStopCodeText = view.findViewById(R.id.dialogBusStopCode);
            busDescriptionText.setText(busDescription);
            busStopCodeText.setText(busCode);

            //SET BUS SERVICE NO. LIST FOR DIALOG MESSAGE
            builder.setItems(tempBusNo.toArray(new CharSequence[tempBusNo.size()]),
                    new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int index) {

                            //IF SELECTED BUS NO. HAS A JSON FILE CONTAINING ROUTE INFORMATION
                            if(JsonService.checkValidJsonFile(tempBusNo.get(index),getApplicationContext())){

                                ArrayList<ArrayList<BusStop>> tempBusList = JsonService.retrieveBusRouteList(tempBusNo.get(index),getApplicationContext());

                                //CHECK FOR 2-DIRECTION BUS ROUTE
                                if (tempBusList.size() > 1) {

                                    int tempIndex = Integer.parseInt(tempDirection.get(index));

                                    //tempIndex = 1 MEANS THAT BUS STOP BELONGS TO DIRECTION 1, SO REMOVE DIRECTION 2 FROM THE LIST
                                    if (tempIndex == 1) {
                                        tempBusList.remove(1);
                                    } else {
                                        tempBusList.remove(0);
                                    }

                                }
                                //MOVE TO NEXT ACTIVITY & PASS DATA
                                Intent intent = new Intent(getApplicationContext(), TripOverview.class);
                                intent.putExtra("buslist", tempBusList.get(0));
                                intent.putExtra("description", qrData[0].split(":")[1]);
                                intent.putExtra("busno",tempBusNo.get(index));
                                startActivityForResult(intent, 1);

                            }
                            //BUS NO. SELECTED HAS NO JSON FILE IN THE APPLICATION
                            else{
                                AlertDialog invalidServiceNoDialog = new AlertDialog.Builder(MainActivity.this).create();
                                invalidServiceNoDialog.setTitle("Invalid Bus Service!!");
                                invalidServiceNoDialog.setMessage("Please try again.");
                                invalidServiceNoDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                                scannerView.resumeCameraPreview(MainActivity.this);

                                            }
                                        });
                                invalidServiceNoDialog.show();
                            }
                        }
                    });


            builder.show();
        }
        else{
            AlertDialog invalidQRCodeDialog = new AlertDialog.Builder(this).create();
            invalidQRCodeDialog.setTitle("Invalid QR Code!!");
            invalidQRCodeDialog.setMessage("Please try again.");
            invalidQRCodeDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            scannerView.resumeCameraPreview(MainActivity.this);

                        }
                    });
            invalidQRCodeDialog.show();
        }


    }

    //CHECK FOR VALID QR CODE (FOR BUS SERVICE ONLY)
    public boolean checkValidQrCode(String[] data){

        boolean status = false;

        String[] tempData = data[0].split(":");

        //KEYWORD FOR CHECKING VALID QRCODE = "BUSRIDER"
        if(tempData[0].equals("BUSRIDE") && tempData[2].matches(".*\\d+.*")){

           status = true;
        }
        return status;

    }

    //WHEN COMING BACK FROM PREVIOUS ACTIVITY (UPDATE CONTENTVIEW TO MAIN ACTIVITY INSTEAD OF CAMERAVIEW)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {

            //REFRESH CONTENTVIEW AND SET TOOLBAR TEXT
            refreshLayout();
        }
    }


    //FOR BACK PHYSICAL BUTTON ON-PRESS
    @Override
    public void onBackPressed() {

        //REFRESH CONTENTVIEW AND SET TOOLBAR TEXT
        refreshLayout();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //scannerView.stopCamera();
    }

    //REFRESH CONTENTVIEW AND SET TOOLBAR TEXT
    public void refreshLayout(){

        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.mainToolbar);
        toolbar.setTitle("BUSRIDE");
        setSupportActionBar(toolbar);
    }
}
