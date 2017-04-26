package com.example.administrator.helloworld.Utils;

import android.content.Context;

/**
 * thread get service data about check login|service close|service ad
 * Created by Administrator on 2017/4/26.
 */
public class GetServiceData {
    private static final String TAG = "LocationSensor";
    private static ServiceThread serviceThread;
    MySocket mySocket;
    GetServiceDataCallBack getServiceDataCallBack;
    Context context;
    String readData="";
    public GetServiceData(Context context, GetServiceDataCallBack getServiceDataCallBack){
        this.context=context;
        this.getServiceDataCallBack=getServiceDataCallBack;
        try {
            mySocket = new MySocket();
            mySocket.createSocket();
        }catch(Exception ex){
            ex.toString();
        }
        if(serviceThread==null){
            serviceThread=new ServiceThread();
            serviceThread.start();
        }
    }

    class ServiceThread extends Thread {
        @Override
        public void run() {
            while(true){
                readData= mySocket.readData();
                if(!readData.isEmpty()) {
                    getServiceDataCallBack.ServiceData(readData);
                }
                try {
                    sleep(1000);
                }catch(Exception ex){}
            }
        }
    };

}
