package com.example.travelistica;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.LocationRequest;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class LandingActivity extends AppCompatActivity {
    com.google.android.gms.location.LocationRequest locationRequest;
    private LocationSettingsRequest.Builder builder;
    private final int REQUEST_CODE=100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Widgets for login and register
        locationRequest=new com.google.android.gms.location.LocationRequest()
                //the fastest rate in ms a which the app receives location updates
                .setFastestInterval(1500)
                //the rate in ms a which the app receives location updates
                .setInterval(3000)
                .setPriority(LocationRequest.QUALITY_HIGH_ACCURACY);
        builder=new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        Task<LocationSettingsResponse> result=
                LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());
        //analyse the location settings of the device
        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    //nothing will happen if the gps is already enabled
                    task.getResult(ApiException.class);
                } catch (ApiException e) {
                    switch(e.getStatusCode()){
                        case LocationSettingsStatusCodes
                                //Attempt to solve the situation if locations is turned off
                                //The user will select if he/she gives access to location or not
                                //in case of approval location is turned on by the program
                                .RESOLUTION_REQUIRED:
                            try {
                                ResolvableApiException resolvableApiException=(ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(LandingActivity.this,REQUEST_CODE);
                            } catch (IntentSender.SendIntentException sendIntentException) {
                                sendIntentException.printStackTrace();
                            }catch(ClassCastException ex){

                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            break;

                    }
                }
            }
        });
        Button login = findViewById(R.id.btn_login);//identifying the login button from UI
        Button register = findViewById(R.id.btn_register);//identifying the register button from UI
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getBaseContext(), LoginActivity.class);//creating new intent
                startActivity(i);//starting login activity
            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getBaseContext(), RegisterActivity.class);//creating new intent
                startActivity(i);//starting register activity
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(REQUEST_CODE==100){
            if(resultCode==RESULT_OK)
                Toast.makeText(LandingActivity.this, "GPS is now enabled", Toast.LENGTH_LONG).show();
            if(resultCode==RESULT_CANCELED)
                System.exit(0);
        }
    }
}
