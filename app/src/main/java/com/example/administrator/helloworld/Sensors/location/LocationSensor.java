package com.example.administrator.helloworld.Sensors.location;

import android.content.Context;
import android.location.Criteria;
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
    private Location local;
    private Context context;

    public LocationSensor(Context context, LocationCallBack locationCallBack) {
        this.context = context;
        this.locationCallBack = locationCallBack;
    }
    /**
     * 注册定位监听器
     *
     * @return 是否支持
     */
    public Boolean registerLocation() {
        Boolean isLocation=false;
        if(locationManager==null) {
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        }
        // 查询条件
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE); // 定位的精准度
        criteria.setAltitudeRequired(false);          // 海拔信息是否关注
        criteria.setBearingRequired(false); // 对周围的事物是否关心
        criteria.setCostAllowed(true);  // 是否支持收费查询
        criteria.setPowerRequirement(Criteria.POWER_LOW); // 是否耗电
        criteria.setSpeedRequired(false); // 对速度是否关注
        // 获取最好的定位方式
        String provider = locationManager.getBestProvider(criteria, true); // true 代表从打开的设备中查找

        // 注册监听
        /**
         * provider:定位方式
         * minTime:定位时间   最少不能小于2000ms  (定位需要时间)
         * minDistance:最小距离位置更新  0代表不更新   按定位时间更新
         */
        if(provider==LocationManager.GPS_PROVIDER&&provider==LocationManager.NETWORK_PROVIDER){
            isLocation=true;
        }
        locationManager.requestLocationUpdates(provider, 0, 0, this);
        return isLocation;
    }

    /**
     * 注销方向监听器
     */
    public void unregisterLocation() {
        locationManager.removeUpdates(this);
        locationManager=null;
    }

    /**
     * 主动获取local
     * @return
     */
    public Location getLocal(){
        return this.local;
    }
    //当定位服务信息改变时
    @Override
    public void onLocationChanged(Location location) {
        this.local=location;
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
