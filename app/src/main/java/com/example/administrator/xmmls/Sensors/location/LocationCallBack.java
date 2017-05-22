package com.example.administrator.xmmls.Sensors.location;

import android.location.Location;
import com.baidu.mapapi.map.MyLocationData;

/**
 * Created by Administrator on 2017/4/20.
 */
public interface LocationCallBack {
    /**
     * location 定位服务
     */
    void Location(Location location, Boolean isChange);

    /**
     * baidu de return
     * @param location
     * @param isChange
     */
    void BaiduLocation(MyLocationData location, Boolean isChange);
}
