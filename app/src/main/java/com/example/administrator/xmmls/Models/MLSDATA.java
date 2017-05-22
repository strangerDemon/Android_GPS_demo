package com.example.administrator.xmmls.Models;

import java.util.Date;

/**
 * 马拉松数据对象
 * Created by Administrator on 2017/5/22.
 */
public class MLSDATA {
    //UserId VARCHAR(20) PRIMARY KEY AUTOINCREMENT,Log VARCHAR(20),Lat VARCHAR(20),HeartRate INTEGER ,Speed INTEGER , StepNum INTEGER ,Temperature Double,Pace INTEGER,Stride INTEGER,GetTime Date)");
    //账号ID
    private String UserId;
    //经度
    private String Log;
    //纬度
    private String Lat;
    //心率
    private int HeartRate;
    //速度
    private double Speed;
    //步数
    private int StepNum;
    //体温
    private double Temperature;
    //配速
    private int Pace;
    //步幅
    private int Stride;
    //获取时间
    private Date GetTime;
    //无参构造
    public MLSDATA(){

    }
    //全参构造
    public MLSDATA(String userId, String log, String lat, int heartRate, double speed, int stepNum, double temperature, int pace, int stride, Date getTime) {
        UserId = userId;
        Log = log;
        Lat = lat;
        HeartRate = heartRate;
        Speed = speed;
        StepNum = stepNum;
        Temperature = temperature;
        Pace = pace;
        Stride = stride;
        GetTime = getTime;
    }

    /**
     * setting
     * getting
     */

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public String getLog() {
        return Log;
    }

    public void setLog(String log) {
        Log = log;
    }

    public String getLat() {
        return Lat;
    }

    public void setLat(String lat) {
        Lat = lat;
    }

    public int getHeartRate() {
        return HeartRate;
    }

    public void setHeartRate(int heartRate) {
        HeartRate = heartRate;
    }

    public double getSpeed() {
        return Speed;
    }

    public void setSpeed(double speed) {
        Speed = speed;
    }

    public int getStepNum() {
        return StepNum;
    }

    public void setStepNum(int stepNum) {
        StepNum = stepNum;
    }

    public double getTemperature() {
        return Temperature;
    }

    public void setTemperature(double temperature) {
        Temperature = temperature;
    }

    public int getPace() {
        return Pace;
    }

    public void setPace(int pace) {
        Pace = pace;
    }

    public int getStride() {
        return Stride;
    }

    public void setStride(int stride) {
        Stride = stride;
    }

    public Date getGetTime() {
        return GetTime;
    }

    public void setGetTime(Date getTime) {
        GetTime = getTime;
    }
}
