package com.dks.geomaps;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.V;
import static com.google.android.gms.maps.CameraUpdateFactory.zoomTo;

public class NewMapActivity extends FragmentActivity implements OnMapReadyCallback{
    private FusedLocationProviderClient mFusedLocationClient;
    Location latestLocation;
    LocationRequest newLocationRequest;
    Marker currentLocationMarker;
    GoogleMap currentMap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_map);
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        Button b = (Button) findViewById(R.id.confirm_placement);
        b.setOnClickListener(new View.OnClickListener(){
            public void onClick(View V){
                AlertDialog alertBuilder = new AlertDialog.Builder(V.getContext())
                        .setTitle("Create new Event?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .show();
            }
        });
    }

    public void onPause(){
        super.onPause();

        if (mFusedLocationClient != null){
            mFusedLocationClient.removeLocationUpdates(mapLocationCallback);
        }
    }
    public void onMapReady(GoogleMap map){
        currentMap = map;
        currentMap.getUiSettings().setMapToolbarEnabled(false);
        newLocationRequest = new LocationRequest();
        newLocationRequest.setInterval(5000);
        newLocationRequest.setFastestInterval(5000);
        newLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mFusedLocationClient.requestLocationUpdates(newLocationRequest, mapLocationCallback, Looper.myLooper());
                currentMap.setMyLocationEnabled(true);
            }
            else{
                locationPermissionChecker();
            }
        }
        else{
            mFusedLocationClient.requestLocationUpdates(newLocationRequest, mapLocationCallback, Looper.myLooper());
            currentMap.setMyLocationEnabled(true);
        }

    }

    LocationCallback mapLocationCallback = new LocationCallback(){
        public void onLocationResult(LocationResult locationResult){
            for (Location location : locationResult.getLocations()) {
                Log.i("MapsActivity", "Location: " + location.getLatitude() + " " + location.getLongitude());
                latestLocation = location;
                if (currentLocationMarker != null){
                    currentLocationMarker.remove();
                }

                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());


                currentMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
            }
        };
    };


    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private void locationPermissionChecker(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){

                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs Location permission to function, pleace accept")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(NewMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();
            }
            else{
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, String permission[], int[] grantResults){

        switch(requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {

                if (grantResults.length > 0  && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mFusedLocationClient.requestLocationUpdates(newLocationRequest, mapLocationCallback, Looper.myLooper());
                        currentMap.setMyLocationEnabled(true);
                    }

                }
                else {
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;

            }

        }

    }


}
