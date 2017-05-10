package com.example.administrator.xmmarathon.Sensors.location;

import android.location.GpsSatellite;

import java.util.List;

/**
 * 卫星位置
 * Created by Administrator on 2017/5/2.
 */
public interface StatusCallBack {
    /**
     * 返回卫星列表
     * @param numSatelliteList
     */
    void Status(List<GpsSatellite> numSatelliteList);
    /**
     * 返回卫星num
     * @param num
     */
    void StatusNum(int num);
}
