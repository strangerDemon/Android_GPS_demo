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

    //计算走了多少米 5s前的数据
    private static double totalTrip=0;//行程
    private int loopTime=5;//多少秒执行一次
    private static double lastLat;//上一次计算的经度
    private static double lastLng;//上一次计算的纬度

    //判断数据是否是费数据
    private int count=1;
    private double length=8;

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
                //上一个计算的经纬度的初始化
                lastLat=loca.getLatitude();
                lastLng=loca.getLongitude();
            }
            gps=positionUtil.gcj_To_Gps84(locData.latitude,locData.longitude);
            //数据过滤，和上一个数据差距过大，去掉
            if(isUnUsed(gps.getWgLat(),gps.getWgLon(),loca.getLatitude(),loca.getLongitude())){
                count++;
                length*=count;
                return;
            }
            loca.setLatitude(gps.getWgLat());
            loca.setLongitude(gps.getWgLon());
            loca.setSpeed((float)countTrip(loca.getLatitude(),loca.getLongitude(),lastLat,lastLng)/5);
            loca.setAccuracy(locData.accuracy);
            loca.setProvider("baidu");
            locationCallBack.Location(loca,!isFirstLoc);
            statusCallBack.StatusNum(locData.satellitesNum);
            if(loopTime==0){
                loopTime=5;
                totalTrip+=countTrip(loca.getLatitude(),loca.getLongitude(),lastLat,lastLng);
                lastLat=loca.getLatitude();
                lastLng=loca.getLongitude();
            }else{
                loopTime--;//循环
            }
        }

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }

    public void getLastKnownLocation() {
        locationCallBack.BaiduLocation(locData,!isFirstLoc);
        statusCallBack.StatusNum(locData.satellitesNum);
    }

    /**
     * 获取总行程
     * @return
     */
    public double getTotalTrip(){
        return totalTrip;
    }

    /**
     * 计算经纬度的距离
     * @return
     */
    private double countTrip(double nowLat,double nowLng,double lastLat,double lastLng){
        if(lastLat==0.0||lastLng==0.0){
            return 0.0;
        }
        double EARTH_RADIUS = 6378.137;//地球半径
        double radLat1 = rad(nowLat);
        double radLat2 = rad(lastLat);
        double a = radLat1 - radLat2;
        double b = rad(nowLng) - rad(lastLng);
        if(a==0&&b==0){
            return 0.0;
        }
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a/2),2) +
                    Math.cos(radLat1)*Math.cos(radLat2)*Math.pow(Math.sin(b/2),2)));
        s = s * EARTH_RADIUS;
        s = (Math.round(s * 100)/10)/10.0;//保存2位小数
        return s;
    }
    private double rad(double d)
    {
        return d * Math.PI / 180.0;
    }

    /**
     * 时候是废数据
     */
    private boolean isUnUsed(double nowLat,double nowLng,double lastLat,double lastLng){
        if(countTrip(nowLat,nowLng,lastLat,lastLng)>length){
            return true;
        }
        return false;
    }
    /**
     * 销毁
     */
    public void onDestroy() {
        // 退出时销毁定位
        mLocClient.stop();
    }


}
