package com.example.administrator.helloworld.Sensors.location;

import android.content.Context;
import android.location.*;
import android.os.Bundle;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

/**
 * 阿西吧，Location不是传感器，暂时无法做到一监听到数据的变化就做出反应 暂时处理方式为遍历请求 失败 go dead
 * Created by Administrator on 2017/4/20.
 */
public class LocationSensor implements LocationListener{

    private static final String TAG = "LocationSensor";
    private static LocationManager locationManager;
    private LocationCallBack locationCallBack;
    private StatusCallBack statusCallBack;
    private Location local;
    private Context context;

    public LocationSensor(Context context, LocationCallBack locationCallBack,StatusCallBack statusCallBack) {
        this.context = context;
        this.locationCallBack = locationCallBack;
        this.statusCallBack=statusCallBack;
    }
    private List<GpsSatellite> numSatelliteList = new ArrayList<GpsSatellite>(); // 卫星信号
    /**
     * 卫星状态监听器
     */
    private final GpsStatus.Listener statusListener = new GpsStatus.Listener() {
        public void onGpsStatusChanged(int event) { // GPS状态变化时的回调，如卫星数
            GpsStatus status = locationManager.getGpsStatus(null); // 取当前状态
            updateGpsStatus(event, status);
            statusCallBack.Status(numSatelliteList);
        }
    };

    private void updateGpsStatus(int event, GpsStatus status) {
        if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
            int maxSatellites = status.getMaxSatellites();
            Iterator<GpsSatellite> it = status.getSatellites().iterator();
            numSatelliteList.clear();
            int count = 0;
            while (it.hasNext() && count <= maxSatellites) {
                GpsSatellite s = it.next();
                numSatelliteList.add(s);
                count++;
            }

        }
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
        if(LocationManager.GPS_PROVIDER.equals(provider)||LocationManager.NETWORK_PROVIDER.equals(provider)){
            isLocation=true;
        }
        locationManager.requestLocationUpdates(provider, 0, 0, this);
        locationManager.addGpsStatusListener(statusListener); // 注册状态信息回调
        this.getLastKnownLocation(provider);
        return isLocation;
    }

    public Location getLastKnownLocation(String provider) {
        if(locationManager==null) {
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        }
        //
        //locationManager.requestLocationUpdates(provider, 0, 0, this);
        int state=numSatelliteList.size();//可用卫星数量
        locationManager.removeUpdates(this);
        if(state>3){
            locationManager.requestLocationUpdates("gps", 0, 0, this);
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null) {//邮箱gps
                locationCallBack.Location(locationManager.getLastKnownLocation("gps"));
                return locationManager.getLastKnownLocation("gps");
            } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) && locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) != null) {
                locationCallBack.Location(locationManager.getLastKnownLocation("network"));
                return locationManager.getLastKnownLocation("network");
            } else {//passive 可能相差甚远
                locationCallBack.Location(locationManager.getLastKnownLocation("passive"));
                return locationManager.getLastKnownLocation("passive");
            }
        }else{
            locationManager.requestLocationUpdates("network", 0, 0, this);
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) && locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) != null) {
                locationCallBack.Location(locationManager.getLastKnownLocation("network"));
                return locationManager.getLastKnownLocation("network");
            } else {//passive 可能相差甚远
                locationCallBack.Location(locationManager.getLastKnownLocation("passive"));
                return locationManager.getLastKnownLocation("passive");
            }
        }
       /* if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null) {//邮箱gps
            locationCallBack.Location(locationManager.getLastKnownLocation("gps"));
            return locationManager.getLastKnownLocation("gps");
        } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) && locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) != null) {
            locationCallBack.Location(locationManager.getLastKnownLocation("network"));
            return locationManager.getLastKnownLocation("network");
        } else *//*if (locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER) && locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER) != null)*//* {//passive 可能相差甚远
            locationCallBack.Location(locationManager.getLastKnownLocation("passive"));
            return locationManager.getLastKnownLocation("passive");
        } *//*else{
            if(locationManager.getLastKnownLocation(provider)!=null) {
                locationCallBack.Location(locationManager.getLastKnownLocation(provider));
                return locationManager.getLastKnownLocation(provider);
            }
            locationCallBack.Location(null);
            return null;
        }*/
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
