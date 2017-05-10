package com.example.administrator.xmmarathon.Sensors.template;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * Created by Administrator on 2017/4/20.
 */
public class TemplateSensor implements SensorEventListener {
    private static final String TAG = "TemplateSensor";
    private SensorManager sensorManager;
    private TemplateCallBack templateCallBack;
    private Context context;

    public TemplateSensor(Context context, TemplateCallBack templateCallBack) {
        this.context = context;
        this.templateCallBack = templateCallBack;
    }


    /**
     * 注册方向监听器
     *
     * @return 是否支持
     */
    public Boolean registerTemplate() {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        boolean isAvailable = sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_TEMPERATURE),
                SensorManager.SENSOR_DELAY_GAME);
        if (isAvailable) {
            Log.i(TAG, "温度传感器可用！");
        } else {
            Log.i(TAG, "问度传感器不可用！");
        }
        return isAvailable;
    }

    /**
     * 注销方向监听器
     */
    public void unregisterTemplate() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        templateCallBack.Template(event.values[0]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
