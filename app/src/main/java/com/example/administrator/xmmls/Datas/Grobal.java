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
    public static String token="***";   //地址
    public static String address="**";

    //tcp 发送来的指令
    public static boolean isSend=false;

}
