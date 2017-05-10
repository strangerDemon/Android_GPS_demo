package com.example.administrator.xmmarathon.Sensors.location;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.location.Location;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.*;

/**
 * 此demo用来展示如何结合定位SDK实现定位，
 */
public class BaiduLocation implements SensorEventListener{

    // 定位相关
    LocationClient mLocClient;
    public MyLocationListenner myListener = new MyLocationListenner();
    private Double lastX = 0.0;
    private int mCurrentDirection = 0;
    private double mCurrentLat = 0.0;
    private double mCurrentLon = 0.0;
    private float mCurrentAccracy = 0;
    private LocationCallBack locationCallBack;
    private StatusCallBack statusCallBack;
    private MyLocationData locData;
    //
    boolean isFirstLoc = true; // 是否首次定位
    //百度地位偏差计算
    PositionUtil positionUtil;
    Gps gps;
    Location loca;

    public BaiduLocation(Context context, LocationCallBack locationCallBack, StatusCallBack statusCallBack) {
        this.locationCallBack=locationCallBack;
        this.statusCallBack=statusCallBack;

        positionUtil=new PositionUtil();
        loca=new Location("gps");
        // 定位初始化
        mLocClient = new LocationClient(context);
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("gcj02"); // bd09ll设置坐标类型,会偏移，只能百度地图使用
        option.setScanSpan(1000);
        mLocClient.setLocOption(option);
        mLocClient.start();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        double x = sensorEvent.values[SensorManager.DATA_X];
        if (Math.abs(x - lastX) > 1.0) {
            mCurrentDirection = (int) x;
            locData = new MyLocationData.Builder()
                    .accuracy(mCurrentAccracy)
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(mCurrentDirection).latitude(mCurrentLat)
                    .longitude(mCurrentLon).build();
        }
        lastX = x;
        locationCallBack.BaiduLocation(locData,!isFirstLoc);
        statusCallBack.StatusNum(locData.satellitesNum);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    /**
     * 定位SDK监听函数
     * 162： 请求串密文解析失败。162错误一般是.so文件加载失败引起的。
     */
    public class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null) {
                return;
            }
            mCurrentLat = location.getLatitude();
            mCurrentLon = location.getLongitude();
            mCurrentAccracy = location.getRadius();
            locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(mCurrentDirection).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            if (isFirstLoc) {
                isFirstLoc = false;
            }
            gps=positionUtil.gcj_To_Gps84(locData.latitude,locData.longitude);
            loca.setLatitude(gps.getWgLat());
            loca.setLongitude(gps.getWgLon());
            loca.setSpeed(locData.speed);
            loca.setAccuracy(locData.accuracy);
            loca.setProvider("baidu");
            locationCallBack.Location(loca,!isFirstLoc);
            statusCallBack.StatusNum(locData.satellitesNum);
        }

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }

    public void getLastKnownLocation() {
        locationCallBack.BaiduLocation(locData,!isFirstLoc);
        statusCallBack.StatusNum(locData.satellitesNum);
    }
    /**
     * 销毁
     */
    public void onDestroy() {
        // 退出时销毁定位
        mLocClient.stop();
    }


}
