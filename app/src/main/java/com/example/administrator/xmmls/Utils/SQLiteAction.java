package com.example.administrator.xmmls.Utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.administrator.xmmls.Models.MLSDATA;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * SQLite 数据库的操作，本地数据库
 * Created by Administrator on 2017/5/22.
 */
public class SQLiteAction extends SQLiteOpenHelper {
    public SQLiteAction(Context context, String name, SQLiteDatabase.CursorFactory factory,int version) {
        super(context, "mlsdata.db", null, 1);
    }

    /**
     * 经纬度坐标        |心率|速度|步数 |步频|体温|配速|步幅
     * 117.2323,24.23566|80  |60  |10023|10 |37.6|52  |68
     * 数据库第一次创建时被调用
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS MLSData(UserId VARCHAR(20),Log VARCHAR(20),Lat VARCHAR(20)," +
                "HeartRate INTEGER ,Speed Double , StepNum INTEGER ,Temperature Double,Pace INTEGER,Stride INTEGER,GetTime Date)");//加入userid防止，一台设备多账号登录

    }
    //软件版本号发生改变时调用
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //db.execSQL("ALTER TABLE MLSData ADD phone VARCHAR(12) NULL");
    }

    /**
     * 插入数据
     * @param data
     */
    public void save(MLSDATA data)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.execSQL("INSERT INTO MLSData(UserId,Log,Lat,HeartRate,Speed,StepNum,Temperature,Pace,Stride,GetTime) values(?,?,?,?,?,?,?,?,?,?)",
                    new String[]{data.getUserId(), data.getLog(), data.getLat(), data.getHeartRate() + "", data.getSpeed() + "",
                            data.getStepNum() + "", data.getTemperature() + "", data.getPace() + "", data.getStride() + "", data.getGetTime() + ""});
        }catch(Exception ex){
            ex.toString();
        }
    }

    /**
     * 删除数据
     * @param id
     */
    public void delete(Integer id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM MLSData WHERE .........",
                new String[]{id.toString()});
    }

    /**
     * 更新数据
     * @param data
     */
    public void update(MLSDATA data)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE MLSData SET UserId = ?,Log = ? WHERE ........",
                new String[]{data.getUserId(),data.getLog(),data.getLat()});
    }

    /**
     * 查找数据
     * @param UserId
     * @return
     */
    public MLSDATA find(String UserId)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor =  db.rawQuery("SELECT * FROM MLSData WHERE UserId = ? order by GetTime DESC ",
                new String[]{UserId});
        //存在数据才返回true
        if(cursor.moveToFirst())
        {
            String Log = cursor.getString(cursor.getColumnIndex("Log"));
            String Lat = cursor.getString(cursor.getColumnIndex("Lat"));
            int HeartRate =Integer.parseInt(cursor.getString(cursor.getColumnIndex("HeartRate")));
            double Speed = Double.parseDouble(cursor.getString(cursor.getColumnIndex("Speed")));
            int StepNum = Integer.parseInt(cursor.getString(cursor.getColumnIndex("StepNum")));
            double Temperature = Double.parseDouble(cursor.getString(cursor.getColumnIndex("Temperature")));
            int Pace = Integer.parseInt(cursor.getString(cursor.getColumnIndex("Pace")));
            int Stride = Integer.parseInt(cursor.getString(cursor.getColumnIndex("Stride")));
            Date GetTime = new Date(Date.parse(cursor.getString(cursor.getColumnIndex("GetTime"))));
            return new MLSDATA(UserId,Log,Lat,HeartRate,Speed,StepNum,Temperature,Pace,Stride,GetTime);
        }
        cursor.close();
        return null;
    }

    /**
     * 分页查询
     * @param offset
     * @param maxResult
     * @return
     */
    public List<MLSDATA> getScrollData(int offset, int maxResult)
    {
        List<MLSDATA> MlsDataList = new ArrayList<MLSDATA>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor =  db.rawQuery("SELECT * FROM MLSData ORDER BY GetTime DESC LIMIT ?,?",
                new String[]{String.valueOf(offset),String.valueOf(maxResult)});
        while(cursor.moveToNext())
        {
            String UserId = cursor.getString(cursor.getColumnIndex("UserId"));
            String Log = cursor.getString(cursor.getColumnIndex("Log"));
            String Lat = cursor.getString(cursor.getColumnIndex("Lat"));
            int HeartRate = cursor.getInt(cursor.getColumnIndex("HeartRate"));
            double Speed = cursor.getDouble(cursor.getColumnIndex("Speed"));
            int StepNum = cursor.getInt(cursor.getColumnIndex("StepNum"));
            int Temperature = cursor.getInt(cursor.getColumnIndex("Temperature"));
            int Pace = cursor.getInt(cursor.getColumnIndex("Pace"));
            int Stride = cursor.getInt(cursor.getColumnIndex("Stride"));
            String GetTime = cursor.getString(cursor.getColumnIndex("GetTime"));
            MlsDataList.add(new MLSDATA(UserId,Log,Lat,HeartRate,Speed,StepNum,Temperature,Pace,Stride,new Date(GetTime))) ;
        }
        cursor.close();
        return MlsDataList;
    }

    /**
     * 查询个数
     * @return
     */
    public long getCount()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor =  db.rawQuery("SELECT COUNT (*) FROM MLSData",null);
        cursor.moveToFirst();
        long result = cursor.getLong(0);
        cursor.close();
        return result;
    }
}
