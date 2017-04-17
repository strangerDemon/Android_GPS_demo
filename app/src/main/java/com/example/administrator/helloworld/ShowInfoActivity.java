package com.example.administrator.helloworld;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * Created by Administrator on 2017/4/12.
 */
public class ShowInfoActivity extends AppCompatActivity {
    //ui 组件
    public TextView showinfo;
    //gsp定位服务
    LocationManager location;

    //数据
    public StringBuilder lastSend;//最后一次发送的信息
    public StringBuilder sb;//发送到显示面板的信息
    public static int count=0;//步数
    //网络
    public  Socket socket;//
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showinfo);
        showinfo= (TextView)findViewById(R.id.showinfo);
        progress();
    }
    //因为ui只能在主线程中修改，应该要用handler处理

    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            Bundle data = null;
            data = msg.getData();
            String show = "";
            switch (msg.what) {
                case 1:
                    show = (String) data.get("showinfo");
                    System.out.println(show);
                    showinfo.setText(show);
                    /*if(jsonString.trim().equals("1"))
                    {
                        System.out.println("短讯发送成功！");
                    }
                    else{
                        Toast toast = Toast.makeText(getApplicationContext(),
                                jsonString, Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }*/
                    break;
            }
            super.handleMessage(msg);
        }
    };
    //android.os.NetworkOnMainThreadException是说不要在主线程中访问网络，
    // 这个是android3.0版本开始就强制程序不能在主线程中访问网络，要把访问网络放在独立的线程中。
    //启动线程
    public void progress() {
        beginThread thread = new beginThread();
        thread.start();
    }
    //自定义线程操作
    class beginThread extends Thread {
        public void run() {
            //handler.sendEmptyMessage(0);
            try {
                createSocket();//创建连接
                getGPS();//获取gps信息
            } catch (Exception e) {
                //showinfo.setText(e.toString());
                Message message=new Message();
                Bundle bundle=new Bundle();
                bundle.putString("showinfo",e.toString());
                message.setData(bundle);
                message.what=1;
                handler.sendMessage(message);
            }
        }
    }

    //获取gps信息 ，经度、纬度、速度、高度、方向
    private void getGPS(){
        while(true){
            //获取定位服务，因为是系统的服务，因此不能new出来
            location=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
            //如果未启动gps服务 启动gps服务
            if(!location.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                Intent intent=new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(intent,0);
            }
            Looper.prepare();//相当于该线程Looper的初始化
            if(location.isProviderEnabled(LocationManager.GPS_PROVIDER)&&location.getLastKnownLocation(LocationManager.GPS_PROVIDER)!=null) {
                updateTextView("GPS_PROVIDER",location.getLastKnownLocation(LocationManager.GPS_PROVIDER));
                getSendData(location.getLastKnownLocation(LocationManager.GPS_PROVIDER));
                //LocationManager.GPS_PROVIDER 定位服务提供者
                //最小时间 单位 毫秒
                //最小距离 单位 米
                location.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, new LocationListener() {
                    //当定位服务信息改变时
                    @Override
                    public void onLocationChanged(Location location1) {
                        updateTextView("GPS_PROVIDER",location1);
                        getSendData(location1);
                    }
                    //状态改变时
                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {

                    }
                    //gsp locationProvider 可用时
                    @Override
                    public void onProviderEnabled(String provider) {
                        updateTextView("GPS_PROVIDER",location.getLastKnownLocation(provider));
                        getSendData(location.getLastKnownLocation(provider));
                    }
                    //gsp locationProvider 不可用时
                    @Override
                    public void onProviderDisabled(String provider) {
                        updateTextView("GPS_PROVIDER",null);
                        getSendData(null);
                    }
                });
            }else if(location.isProviderEnabled(LocationManager.NETWORK_PROVIDER)&&location.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)!=null){
                updateTextView("NETWORK_PROVIDER",location.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
                getSendData(location.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
                location.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, new LocationListener() {
                    //当定位服务信息改变时
                    @Override
                    public void onLocationChanged(Location location1) {
                        updateTextView("NETWORK_PROVIDER",location1);
                        getSendData(location1);
                    }
                    //状态改变时
                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {

                    }
                    //gsp locationProvider 可用时
                    @Override
                    public void onProviderEnabled(String provider) {
                        updateTextView("NETWORK_PROVIDER",location.getLastKnownLocation(provider));
                        getSendData(location.getLastKnownLocation(provider));
                    }
                    //gsp locationProvider 不可用时
                    @Override
                    public void onProviderDisabled(String provider) {
                        updateTextView("NETWORK_PROVIDER",null);
                        getSendData(null);
                    }
                });
            }else{
                updateTextView("PASSIVE_PROVIDER",location.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER));
                getSendData(location.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER));
                location.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 1000, 1, new LocationListener() {
                    //当定位服务信息改变时
                    @Override
                    public void onLocationChanged(Location location1) {
                        updateTextView("PASSIVE_PROVIDER",location1);
                        getSendData(location1);
                    }
                    //状态改变时
                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {

                    }
                    //gsp locationProvider 可用时
                    @Override
                    public void onProviderEnabled(String provider) {
                        updateTextView("PASSIVE_PROVIDER",location.getLastKnownLocation(provider));
                        getSendData(location.getLastKnownLocation(provider));
                    }
                    //gsp locationProvider 不可用时
                    @Override
                    public void onProviderDisabled(String provider) {
                        updateTextView("PASSIVE_PROVIDER",null);
                        getSendData(null);
                    }
                });
            }
            try {
                beginThread.sleep(1000);
            }catch(Exception ex){

            }
            Looper.loop();//Looper开始执行，注意该语句执行后，这个线程的其他操作就被阻塞，只能响应事件了。
        }
       /* //定位服务监听器，每隔1s，或者距离8更新获取一次
        location.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, new LocationListener() {
            //当定位服务信息改变时
            @Override
            public void onLocationChanged(Location location1) {
                updateTextView(location1);
                getSendData(location1);
            }
            //状态改变时
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }
            //gsp locationProvider 可用时
            @Override
            public void onProviderEnabled(String provider) {
                updateTextView(location.getLastKnownLocation(provider));
                getSendData(location.getLastKnownLocation(provider));
            }
            //gsp locationProvider 不可用时
            @Override
            public void onProviderDisabled(String provider) {
                updateTextView(null);
                getSendData(null);
            }
        });*/

    }
    //更新textview 上的显示
    private void updateTextView(String source,Location location){
        sb=new StringBuilder();
        if(location !=null){
            sb.append("来源"+source+"\n");
            sb.append(count+"当前定位服务信息：\n");
            sb.append("经度："+location.getLongitude()+"\n");//经度
            sb.append("纬度："+location.getLatitude()+"\n");//纬度
            sb.append("高度："+location.getAltitude()+"\n");//高度
            sb.append("速度："+location.getSpeed()+"\n");//速度
            sb.append("方向："+location.getBearing()+"\n");//方向
            sb.append("定位精度："+location.getAccuracy()+"\n");//定位精度
            sb.append("时间："+timedate(location.getTime()+"")+"\n");//时间
            sb.append("getElapsedRealtimeNanos："+location.getElapsedRealtimeNanos()+"\n");//时间
        }else{
            sb.append("数据为空");
        }
        //showinfo.setText(Html.fromHtml(sb.toString()));
        Message message=new Message();
        Bundle bundle=new Bundle();
        bundle.putString("showinfo",sb.toString());
        message.setData(bundle);
        message.what=1;
        handler.sendMessage(message);
    }
    //获取需要发送的信息数据 除了速度 坐标 其余都是假数据
    //数据格式：00006经纬度坐标        |心率|速度|步数 |步频|体温|配速|步幅
    //例如：    00006117.2323,24.23566|80  |60  |10023|10 |37.6|52  |68
    private void getSendData(Location location){
        lastSend=new StringBuilder();
        int bp=0;
        if(location !=null){
            if(location.getSpeed()!=0){
                bp= new Random().nextInt(3);
                count+=bp;
            }else{
                count++;
            }
            lastSend.append("00003"+location.getLongitude()+","+location.getLatitude());//经纬度
            //安静状态下，成人正常心率为60～100次/分钟，理想心率应为55～70次/分钟（运动员的心率较普通成人偏慢，一般为50次/分钟左右
            lastSend.append("|"+((int)(location.getSpeed()/6)+60+new Random().nextInt(5)));//心率
            lastSend.append("|"+location.getSpeed());//速度
            lastSend.append("|"+count);//步数
            lastSend.append("|"+bp);//步频
            lastSend.append(String.format("|" + (37 + (location.getSpeed() / 600.0))));//体温
            lastSend.append("|"+52);//配速
            lastSend.append("|"+68);//步幅
        }else{
        }
        Send(lastSend.toString());
    }

    //创建socket网络连接
    private void createSocket(){
        //1.创建客户端Socket，指定服务器地址和端口
        try {
            if(socket==null) {
                socket = new Socket("183.250.160.124", 8888);//
            }
        }catch(Exception ex){
            Message message=new Message();
            Bundle bundle=new Bundle();
            bundle.putString("showinfo","网络连接失败："+ex.toString());
            message.setData(bundle);
            message.what=1;
            handler.sendMessage(message);
            //showinfo.setText("网络连接失败："+ex.toString());
        }
    }

    private void closeSocket(){
        try {
            socket.close();
        }catch(Exception ex){
            showinfo.setText("连接关闭失败："+ex.toString());
        }
    }
    //发送数据
    private void Send(String data){
        try {
            //2.获取输出流，向服务器端发送信息
            OutputStream os = socket.getOutputStream();//字节输出流
            PrintWriter pw = new PrintWriter(os);//将输出流包装为打印流
            pw.write(data);
            pw.flush();
            //socket.shutdownOutput();//关闭输出流
        }catch(Exception ex){
            //showinfo.setText("传输数据失败："+ex.toString());
            Message message=new Message();
            Bundle bundle=new Bundle();
            bundle.putString("showinfo","传输数据失败："+ex.toString());
            message.setData(bundle);
            message.what=1;
            handler.sendMessage(message);
        }
    }
    //时间转换
    public static String timedate(String time) {
        SimpleDateFormat sdr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        @SuppressWarnings("unused")
        long lcc = Long.valueOf(time)+8*60*60*1000;//加8个小时 东八区
        String times = sdr.format(new Date(lcc));
        return times;
    }
}
