package com.example.administrator.xmmls.Datas;

import com.example.administrator.xmmls.Models.User;
import com.example.administrator.xmmls.Utils.SQLiteAction;

/**
 * 用户的信息,全局类变量
 * Created by Administrator on 2017/5/17.
 */
public  class Grobal {
    //全局变量
    public static User user;

    public static String UserId;

    public static String GameId;

    public static SQLiteAction sqLiteAction;

    //全局静态常量
    //验证码
    public static String TOKEN="dGltZT0xNDc5Mzg1NjgyMzY0Jm51bT1TUTZJMyZhY2Nlc3NUb2tlbj10cFJBQ0dqOTdPSk1nZG5MUlllTVQzSVRoL090RDB6SSZ2ZXJzaW9uPXYxLjAmYXBwSWQ9aW5pdCZwbGF0Zm9ybT1hbmRyb2lkJnBob25lVVVJRD01YjJkMjk1MzNkNmY0MzM2OGExNzJhNmRhMjk3ZGE1ZA==";
    //地址
    public static String ADDRESS="http://www.ztgis.com:8883/xmtdt.asmx";
    //厦门经纬度大致范围
    public static double MIN_XM_LNG=117.9;

    public static double MAX_XM_LNG=118.3;

    public static double MIN_XM_LAT=24.4;

    public static double MAX_XM_LAT=24.8;
    //每次最大叠加
    public static int LENGTH=8;
    //tcp 发送来的指令
    public static boolean isSend=false;



}
