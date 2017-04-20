package com.example.administrator.helloworld.location;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

/**
 * 阿西吧，Location不是传感器，暂时无法做到一监听到数据的变化就做出反应 暂时处理方式为遍历请求 失败 go dead
 * Created by Administrator on 2017/4/20.
 */
public class LocationSensor implements LocationListener{

    private static final String TAG = "LocationSensor";
    private static LocationManager locationManager;
    private LocationCallBack locationCallBack;
    private Context context;

    public LocationSensor(Context context, LocationCallBack locationCallBack) {
        this.context = context;
        this.locationCallBack = locationCallBack;
        registerLocation();
    }
    /**
     * 注册定位监听器
     *
     * @return 是否支持
     */
    public void registerLocation() {
        if(locationManager==null) {
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        }
        locationManager.removeUpdates(this);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null) {
            onLocationChanged(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
        } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) && locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) != null) {
            onLocationChanged(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
        } else {
            onLocationChanged(locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER));
        }

    }

    //当定位服务信息改变时
    @Override
    public void onLocationChanged(Location location) {
        locationCallBack.Location(location);
    }
    //状态改变时
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }
    //gsp locationProvider 可用时
    @Override
    public void onProviderEnabled(String provider) {
    }
    //gsp locationProvider 不可用时
    @Override
    public void onProviderDisabled(String provider) {
    }
}
