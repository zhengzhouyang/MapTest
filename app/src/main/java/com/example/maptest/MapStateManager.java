package com.example.maptest;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Administrator on 2015/10/12.
 */
public class MapStateManager {

    private static final String LATITUDE="latitude";
    private static final String LONGITUDE="longitude";
    private static final String ZOOM="zoom";
    private static final String TILT="tilt";
    private static final String BEARING="bearing";
    private static final String MAPTYPE="MAPTYPE";

    private static final String PREFS_NAME="mapCameraState";

    private SharedPreferences mapStatePrefs;

    public MapStateManager(Context context)
    {
        //PREFS_NAME 是 key
        mapStatePrefs=context.getSharedPreferences(PREFS_NAME,Context.MODE_PRIVATE);
        //MODE_PRIVATE是指只有本程序才能使用这个数据？
    }

    public void saveMapState(GoogleMap map)
    {
        SharedPreferences.Editor editor=mapStatePrefs.edit();

        CameraPosition position=map.getCameraPosition();

        //把信息保存在preferences中，保存形式为kvp
        editor.putFloat(LATITUDE, (float) position.target.latitude);

        editor.putFloat(LONGITUDE, (float) position.target.longitude);

        editor.putFloat(ZOOM,  position.zoom);

        editor.putFloat(TILT, position.tilt);

        editor.putFloat(BEARING, position.bearing);

        editor.putInt(MAPTYPE, map.getMapType());

        editor.commit();//save the information in the sharedPreference

    }

    public CameraPosition getSavedCameraPosition()
    {
        double latitude = mapStatePrefs.getFloat(LATITUDE,0);
        //the app doesn't have the saved information
        if (latitude==0)
        {
            return null;
        }

        double longitude = mapStatePrefs.getFloat(LONGITUDE,0);

        LatLng target=new LatLng(latitude,longitude);

        float zoom=mapStatePrefs.getFloat(ZOOM,0);

        float tilt=mapStatePrefs.getFloat(TILT,0);

        float bearing=mapStatePrefs.getFloat(BEARING,0);

        CameraPosition position=new CameraPosition(target,zoom,tilt,bearing);

        return position;
    }

}
