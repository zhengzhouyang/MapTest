package com.example.maptest;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;

import java.io.IOException;
import java.util.List;


public class Maptest extends AppCompatActivity implements
        View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        NavigationView.OnNavigationItemSelectedListener{

    private static final int GPS_ERRORDIALOG_REQUEST = 9001;

    private GoogleMap mMap;

    private EditText inputLocation = null;

    private static final String TAG="map";

    private static final String TAG_CURRENT_LOCATION="Current Location";

    private static final String TAG_INDOOR_MAP="Indoor Map";

    GoogleApiClient mGoogleApiClient;

    //set the default init position programmatically
    private static final double LA_LAT = 34.017905,
            LA_LNG = -118.314273;

    private static final int defaultZoom = 15;
    private static final int indoorMapZoom=18;

    private static final double INDOOR_LAT=38.649077,
            INDOOR_LNG=-121.518155;

    Marker marker;

    Marker markMyLocation;

    LocationRequest request;

    private static boolean isIntroduced=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!isIntroduced)
        {
            System.out.println(isIntroduced);
            isIntroduced=true;
            startFlowAnimation(null);
        }


        if (servicesOK()) {

            setContentView(R.layout.activity_main);//fragment layout

            if (initMap()) {

                Toast.makeText(this, "Ready for the map", Toast.LENGTH_SHORT).show();

                gotoLocation(LA_LAT, LA_LNG, defaultZoom);

                mGoogleApiClient = new GoogleApiClient.Builder(this)
                        .addApi(LocationServices.API)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .build();



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



        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.actionbar_background,null));

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar ,R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //右下角的menu
        buildFAB();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public void startFlowAnimation(View v){
        Intent intent = new Intent(this, FlowAnimation.class);
        startActivity(intent);
    }

    private void buildFAB()//右下动画菜单
    {
        ImageView iconActionButton= new ImageView(this);

        iconActionButton.setImageResource(R.drawable.ic_action_new);

        FloatingActionButton actionButton=new FloatingActionButton.Builder(this)
                .setContentView(iconActionButton)
//                .setBackgroundDrawable(R.drawable.selector_button_red)
                .build();

        ImageView currentLocation=new ImageView(this);

        currentLocation.setImageResource(R.drawable.ic_action_test1);

        ImageView indoorMap=new ImageView(this);

        indoorMap.setImageResource(R.drawable.ic_action_test2);

        SubActionButton.Builder itemBuilder = new SubActionButton.Builder(this);

        SubActionButton buttonCurrentLocation=itemBuilder.setContentView(currentLocation).build();

        SubActionButton buttonIndoorMap=itemBuilder.setContentView(indoorMap).build();

        buttonCurrentLocation.setTag(TAG_CURRENT_LOCATION);

        buttonIndoorMap.setTag(TAG_INDOOR_MAP);

        buttonCurrentLocation.setOnClickListener(this);

        buttonIndoorMap.setOnClickListener(this);

        FloatingActionMenu actionMenu=new FloatingActionMenu.Builder(this)
                .addSubActionView(buttonCurrentLocation)
                .addSubActionView(buttonIndoorMap)
                .attachTo(actionButton)
                .build();

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

    private void gotoIndoorLocation()
    {
        LatLng ll = new LatLng(INDOOR_LAT, INDOOR_LNG);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, indoorMapZoom);
        mMap.moveCamera(update);
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
    public void geoLocate(String Location) throws IOException {
//        hideSoftKeyboard(v);

//        inputLocation = (EditText) findViewById(R.id.Location);
//
//        String Location = inputLocation.getText().toString();

        //这个package通过用户输入的地名或者邮编得到地址的latitude和longitude
        Geocoder gc = new Geocoder(this);

        List<Address> list = gc.getFromLocationName(Location, 1);

        Address add = list.get(0);

        String locality = add.getLocality();

        Toast.makeText(this, locality, Toast.LENGTH_LONG).show();

        double lat = add.getLatitude();

        double lng = add.getLongitude();

        gotoLocation(lat, lng, defaultZoom);

        setMarker(locality,lat,lng);

    }

    //hide the visible keyboard when user uses the map
    private void hideSoftKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    private void setMarker(String locality,double lat,double lng)
    {
        if(marker!=null)
        {
            marker.remove();
        }

        //对地名进行标记
        MarkerOptions options = new MarkerOptions()
                .title(locality)
                .position(new LatLng(lat, lng));

//        .icon(BitmapDescriptorFactory.defaultMarker());//运用google 自带的marker

        /*
        * If I want to change the color of the marker, please use the code blowe
        * MarkerOptions options = new MarkerOptions()
                .title(locality)
                .position(new LatLng(lat, lng))
                .icon(BitmapDescriptionFactory.defaultMarker(
                BitmapDescriptionFactory.HUE_BLUE));
        */

        marker = mMap.addMarker(options);// update the marker

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
        MenuItem searchItem = (MenuItem) menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat
                .getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                try {
                    geoLocate(query);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
    }


    //menu 字体颜色设置，以后找另外方法

    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
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
            default:
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();

        //remove the LocationRequest to save power
        if(request!=null)
        {
            request.setExpirationDuration(1);//1毫秒後request失效
            Toast.makeText(this,"Request service is closed",Toast.LENGTH_SHORT).show();
        }
        //当程序停止时，不需要再实时进行更新
        mGoogleApiClient.disconnect();
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

        mGoogleApiClient.connect();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMap=null;
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

        //LocationRequest is used to location update
        request = LocationRequest.create();

        //高精度
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        /*
        這是三個精度不同的區別
        PRIORITY_HIGH_ACCURACY consumes a lot of power but accuracy, every 5 seconds
        PRIORITY_NO_POWER will not trigger location updates but will receive location triggerred by other application
        PRIORITY_BALANCED_POWER_ACCURACY updates location every 1 minutes or even longer
         */

        //5000 毫秒更新一次位置信息
        request.setInterval(5000);

        request.setFastestInterval(1000);

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, request, this);

//        float zoomValue=mMap.getCameraPosition().zoom;

//        String value=String.valueOf(zoomValue);

//        Toast.makeText(this,value,Toast.LENGTH_LONG).show();

        //Log.d(TAG,value);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this,"connection suspended",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        String msg="Location:"+location.getLatitude()+","+location.getLongitude();

        double lat=location.getLatitude();

        double lng=location.getLongitude();

        gotoLocation(lat, lng);

        setMyCurrentLocationMarker(lat,lng);

        Toast.makeText(this,msg,Toast.LENGTH_LONG).show();

        float zoomValue=mMap.getCameraPosition().zoom;

        Thread test=null;

        if(zoomValue>=19)
        {
            test=new Thread(new deteceIbeacon());

            test.run();

            Toast.makeText(this,TAG,Toast.LENGTH_SHORT).show();
        }else
        {
            if(test!=null)
            {
                try {
                    test.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        String value=String.valueOf(zoomValue);

        Toast.makeText(this,value,Toast.LENGTH_LONG).show();
    }

    private void setMyCurrentLocationMarker(double lat,double lng)
    {
        if(markMyLocation!=null)
        {
            markMyLocation.remove();
        }

        MarkerOptions option = new MarkerOptions()
                .position(new LatLng(lat,lng))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_currentlocation));;

        markMyLocation=mMap.addMarker(option);

    }

    @Override
    public void onClick(View v) {
        if(v.getTag().equals(TAG_CURRENT_LOCATION))
        {
            mGoogleApiClient.connect();
            Toast.makeText(this,"Go To Current Location",Toast.LENGTH_LONG).show();
            gotoCurrentLocation();
        }else if(v.getTag().equals(TAG_INDOOR_MAP))
        {
            mGoogleApiClient.disconnect();
            Toast.makeText(this,"Go To Indoor Map",Toast.LENGTH_LONG).show();
            gotoIndoorLocation();
        }
    }


    class deteceIbeacon implements Runnable
    {
        @Override
        public void run() {
            Toast.makeText(Maptest.this,"Thread start",Toast.LENGTH_SHORT).show();
        }
    }
}
