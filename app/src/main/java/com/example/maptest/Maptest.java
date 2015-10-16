package com.example.maptest;

import android.app.Dialog;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;


public class Maptest extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener{

    private static final int GPS_ERRORDIALOG_REQUEST = 9001;
    private GoogleMap mMap;
    private EditText inputLocation = null;

    GoogleApiClient mGoogleApiClient;

    //set the default init position programmatically
    private static final double LA_LAT = 34.017905,
            LA_LNG = -118.314273;

    private static final int defaultZoom = 15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (servicesOK()) {

            setContentView(R.layout.activity_map);//fragment layout

            if (initMap()) {

                Toast.makeText(this, "Ready for the map", Toast.LENGTH_SHORT).show();

                gotoLocation(LA_LAT, LA_LNG, defaultZoom);

                mGoogleApiClient = new GoogleApiClient.Builder(this)
                        .addApi(LocationServices.API)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .build();

                mGoogleApiClient.connect();

                //get my current location
//                mMap.setMyLocationEnabled(true);

            } else {
                Toast.makeText(this, "Map is not ready", Toast.LENGTH_SHORT).show();
            }

            /*
            这是用另一种方式来实现map
            mMapView=(MapView)findViewById(R.id.map1);
            mMapView.onCreate(savedInstanceState);
            //map view has the same life cycle to the activity,so I have to override the onResume/onPause....x`
            */
        } else {
            setContentView(R.layout.activity_maptest);
        }
    }

    //used to check whether google play service is set up properly
    public boolean servicesOK() {
        int isAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        //Everything is OK
        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (GooglePlayServicesUtil.isUserRecoverableError(isAvailable)) {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(isAvailable, this, GPS_ERRORDIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "can't connect to google play service", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private void gotoLocation(double lat, double lng) {
        LatLng ll = new LatLng(lat, lng);
        CameraUpdate update = CameraUpdateFactory.newLatLng(ll);
        mMap.moveCamera(update);
    }

    //have zoom value
    private void gotoLocation(double lat, double lng, int zoom) {
        LatLng ll = new LatLng(lat, lng);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, zoom);
        mMap.moveCamera(update);
    }

    //Through Class Geocoder to go to the position according to the user input
    public void geoLocate(View v) throws IOException {
        hideSoftKeyboard(v);

        inputLocation = (EditText) findViewById(R.id.Location);

        String Location = inputLocation.getText().toString();

        //这个package通过用户输入的地名或者邮编得到地址的latitude和longitude
        Geocoder gc = new Geocoder(this);

        List<Address> list = gc.getFromLocationName(Location, 1);

        Address add = list.get(0);

        String locality = add.getLocality();

        Toast.makeText(this, locality, Toast.LENGTH_LONG).show();

        double lat = add.getLatitude();

        double lng = add.getLongitude();

        gotoLocation(lat, lng, defaultZoom);

        //对地名进行标记
        MarkerOptions options = new MarkerOptions()
                .title(locality)
                .position(new LatLng(lat, lng));

        mMap.addMarker(options);

    }

    //hide the visible keyboard when user uses the map
    private void hideSoftKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    //get a reference of the map object
    private boolean initMap() {
        if (mMap == null) {
            //如果map為空值，對其进行实例化
            SupportMapFragment mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            //get the instance of mMap
            mMap = mapFrag.getMap();
        }
        return (mMap != null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.mapTypeNone:
                mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                break;
            case R.id.mapTypeNormal:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.mapTypeHybrid:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            case R.id.mapTypeTerrian:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case R.id.mapTypeSatellite:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.gotoCurrentLocation:
                gotoCurrentLocation();
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        MapStateManager mpr = new MapStateManager(this);
        //save the information when activity stops
        mpr.saveMapState(mMap);

    }

    @Override
    protected void onResume() {
        super.onResume();

        MapStateManager mpr = new MapStateManager(this);

        CameraPosition position = mpr.getSavedCameraPosition();

        if (position != null) {
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);

            mMap.moveCamera(update);
        }

    }

    private void gotoCurrentLocation() {
        //get the lastest location
        Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (currentLocation == null) {
            Toast.makeText(this, "Current Location isn't available", Toast.LENGTH_SHORT).show();
        } else {
            LatLng ll = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, defaultZoom);

            mMap.animateCamera(update);
        }
    }

    //I have to redesign the interface and implement new buttonListener to realize update function
    @Override
    public void onConnected(Bundle bundle) {
        Toast.makeText(this, "connected to location services", Toast.LENGTH_SHORT).show();

        LocationRequest request = LocationRequest.create();

        //高精度
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        //5000 毫秒更新一次位置信息
        request.setInterval(5000);

        request.setFastestInterval(1000);

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, request, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        String msg="Location:"+location.getLatitude()+","+location.getLongitude();
        Toast.makeText(this,msg,Toast.LENGTH_LONG).show();
    }
}
