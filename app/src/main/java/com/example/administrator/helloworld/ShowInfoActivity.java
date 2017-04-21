package com.example.administrator.helloworld;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import com.example.administrator.helloworld.location.LocationCallBack;
import com.example.administrator.helloworld.location.LocationSensor;
import com.example.administrator.helloworld.orient.OrientSensor;
import com.example.administrator.helloworld.step.*;
import com.example.administrator.helloworld.orient.OrientCallBack;
import com.example.administrator.helloworld.template.TemplateCallBack;
import com.example.administrator.helloworld.template.TemplateSensor;
import com.example.administrator.helloworld.util.MySocket;
import com.example.administrator.helloworld.util.SensorUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * Created by Administrator on 2017/4/12.
 */
public class ShowInfoActivity extends AppCompatActivity implements StepCallBack, OrientCallBack ,LocationCallBack,TemplateCallBack {
    //ui 组件
    public TextView showinfo;
    //gsp定位服务
    LocationManager location;
    //线程
    static beginThread thread;
    //循环
    boolean threadLoop=true;
    //数据
    public StringBuilder lastSend;//最后一次发送的信息
    public StringBuilder sb;//发送到显示面板的信息
    public static int count=0;//步数
    public static int orientNum=0;//方向
    //网络socket
    public static MySocket socket;//
    //控制线程休眠
    private boolean suspendFlag = false;

    //dialog提醒一次
    public boolean gpsOpenDialog=true;
    public boolean netOpenDialog=true;
    //计步器
    private TextView step;
    private TextView orient;
    private TextView locationview;
    private TextView template;

    private StepSensorBase stepSensor; // 计步传感器
    private OrientSensor orientSensor; // 方向传感器
    private LocationSensor locationSensor;//定位
    private TemplateSensor templateSensor;//温度
    //关闭线程
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showinfo);
        showinfo= (TextView)findViewById(R.id.showinfo);
        step = (TextView) findViewById(R.id.step);
        orient = (TextView) findViewById(R.id.orient);
        //locationview=(TextView)findViewById(R.id.location);
        template=(TextView)findViewById(R.id.template);
        initShow();
        showInterstitial();
        progress();
    }

    /**
     * 监听Back键按下事件,方法1:
     * 注意:
     * super.onBackPressed()会自动调用finish()方法,关闭
     * 当前Activity.
     * 若要屏蔽Back键盘,注释该行代码即可
     */
    @Override
    public void onBackPressed() {
        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("type", "return");
        message.setData(bundle);
        message.what = 2;
        handler.sendMessage(message);
        suspend();
    }
    /**
     * 因为ui只能在主线程中修改，应该要用handler处理
     */
    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            Bundle data = null;
            data = msg.getData();
            String show = "";
            String type = "";
            switch (msg.what) {
                case 1://数据显示
                    show = (String) data.get("showinfo");
                    if (show != "" || show != null) {
                        showinfo.setText(show);
                    }

                    break;
                case 2://弹窗
                    type=(String) data.get("type");
                    if( type == "connect") {
                        isConnDialog();
                    }else if(type == "permission"){
                        isPermissionDialog();
                    }else if(type == "gpsOpen"){
                        isGPSOpenDialog();
                    }else if(type == "socket"){
                        isSocketConnectDialog();
                    }else if(type == "return"){
                        isReturn();
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };

    /**
     * android.os.NetworkOnMainThreadException是说不要在主线程中访问网络
     * android 中 主线程不能访问网络
     *            子线程不能修改ui
     * 这个是android3.0版本开始就强制程序不能在主线程中访问网络，要把访问网络放在独立的线程中。
     * 启动线程
     */
    public void progress() {
        if(thread==null) {
            thread = new beginThread();
            thread.start();
        }
    }

    /**
     * 自定义线程操作
     */
    class beginThread extends Thread {
        public void run() {
            Looper.prepare();//相当于该线程Looper的初始化
            try {
                while(threadLoop) {
                    getGPS();//获取gps信息
                    synchronized (this) {
                        while (suspendFlag||(!isConn("judge")||!isGPSOpen("judge")||!isPermission("judge"))) {
                            sleep(1000);//wait线程被暂停，需要notify 来释放
                        }
                    }
                }
            } catch (Exception e) {
                Message message=new Message();
                Bundle bundle=new Bundle();
                bundle.putString("showinfo",e.toString());
                message.setData(bundle);
                message.what=1;
                handler.sendMessage(message);
            }
            Looper.loop();//Looper开始执行，注意该语句执行后，这个线程的其他操作就被阻塞，只能响应事件了。
        }
    }
    /**
     * 线程暂停
     */
    public void suspend() {
        this.suspendFlag = true;
    }

    /**
     * 唤醒线程
     */
    public synchronized void resume() {
        this.suspendFlag = false;
        //notify();
    }
    /**
     * 停止线程运行。
     */
    public void stop() {
        if (thread != null){
            thread.interrupt();
            thread = null;
        }
    }

    /**
     * 获取gps信息 ，经度、纬度、速度、高度、方向
     */
    private void getGPS() {
        //创建连接 防止 运行一半的时候 服务器挂了，用户关掉数据
        if(netOpenDialog) {
            isConn("dialog");
            netOpenDialog=false;
        }
        if(socket==null) {
            socket = new MySocket();//
            socket.createSocket();
        }
        //系统服务
        location = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //获取定位服务，因为是系统的服务，因此不能new出来
        if(gpsOpenDialog) {
            isGPSOpen("dialog");
            gpsOpenDialog=false;
        }

        try {
            if (Build.VERSION.SDK_INT >= 23) { //CONTROLLO PER ANDROID 6.0 O SUPERIORE
                isPermission("dialog");
            } else {//android 6.0 以下
            }
        } catch (Exception ex) {}
        location.removeUpdates(onLocationChanged);
        if (location.isProviderEnabled(LocationManager.GPS_PROVIDER) && location.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null) {
            updateTextView("GPS_PROVIDER", location.getLastKnownLocation(LocationManager.GPS_PROVIDER));
            getSendData(location.getLastKnownLocation(LocationManager.GPS_PROVIDER));
        } else if (location.isProviderEnabled(LocationManager.NETWORK_PROVIDER) && location.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) != null) {
            updateTextView("NETWORK_PROVIDER", location.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
            getSendData(location.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
        } else {
            updateTextView("PASSIVE_PROVIDER", location.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER));
            getSendData(location.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER));
        }
        try {
            beginThread.sleep(2000);
        } catch (Exception ex) {}
    }
    /**
     * 外部定义定位监测器
     */
    LocationListener onLocationChanged= new LocationListener() {
        //当定位服务信息改变时
        @Override
        public void onLocationChanged(Location location) {
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
    };

    /**
     * 更新textview 上的显示
     * @param source
     * @param location
     */
    private void updateTextView(String source,Location location){
        sb=new StringBuilder();
        sb.append("来源"+source+"\n");
        if(location !=null){
            sb.append("当前定位服务信息：\n");
            sb.append("经度："+location.getLongitude()+"\n");//经度
            sb.append("纬度："+location.getLatitude()+"\n");//纬度
            sb.append("高度："+location.getAltitude()+"\n");//高度
            sb.append("速度："+location.getSpeed()+"\n");//速度
            sb.append("location方向："+location.getBearing()+"\n");//方向
            sb.append("定位精度："+location.getAccuracy()+"\n");//定位精度
            sb.append("时间："+timedate(location.getTime()+"")+"\n");//时间
            sb.append("步数："+count+"\n");//步数
            sb.append("orient方向："+orientNum+"\n");//orient方向
        }else{
            sb.append("数据为空");
        }
        //在线程中不能操作view
        Message message=new Message();
        Bundle bundle=new Bundle();
        bundle.putString("showinfo",sb.toString());
        message.setData(bundle);
        message.what=1;
        handler.sendMessage(message);
    }

    /**
     * 获取需要发送的信息数据 除了速度 坐标 其余都是假数据
     * 数据格式：00006经纬度坐标        |心率|速度|步数 |步频|体温|配速|步幅
     * 例如：    00006117.2323,24.23566|80  |60  |10023|10 |37.6|52  |68
     * @param location
     */
    private void getSendData(Location location){
        lastSend=new StringBuilder();
        int bp=0;
        if(location !=null){
            lastSend.append("$00003"+location.getLongitude()+","+location.getLatitude());//经纬度 √
            //安静状态下，成人正常心率为60～100次/分钟，理想心率应为55～70次/分钟（运动员的心率较普通成人偏慢，一般为50次/分钟左右
            lastSend.append("|"+((int)(location.getSpeed()/6)+60+new Random().nextInt(5)));//心率
            lastSend.append("|"+location.getSpeed());//速度
            lastSend.append("|"+count);//步数 √
            lastSend.append("|"+bp);//步频
            lastSend.append(String.format("|" + (37 + (location.getSpeed() / 600.0))));//体温
            lastSend.append("|"+52);//配速
            lastSend.append("|"+68);//步幅
        }else{
        }
        socket.writeData(lastSend.toString());
    }
    /**
     * 时间格式转换
     * @param time
     * @return
     */
    public static String timedate(String time) {
        SimpleDateFormat sdr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        @SuppressWarnings("unused")
        long lcc = Long.valueOf(time)+8*60*60*1000;//加8个小时 东八区
        String times = sdr.format(new Date(lcc));
        return times;
    }

    /**
     * 判断网络连接是否已开
     * true 已打开  false 未打开
     */
    public boolean isConn(String type){
        ConnectivityManager conManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo network = conManager.getActiveNetworkInfo();
        if(network==null&&type=="dialog") {
            Message message = new Message();
            Bundle bundle = new Bundle();
            bundle.putString("type", "connect");
            message.setData(bundle);
            message.what = 2;
            handler.sendMessage(message);
            suspend();//暂停线程
            return false;
        }else if(network==null&&type=="judge") {
            return false;
        }else{
            return true;
        }
    }
    public void isConnDialog() {
        AlertDialog.Builder connectDialog = new AlertDialog.Builder(ShowInfoActivity.this);
        connectDialog.setMessage("网络连接未打开，是否开启？");
        connectDialog.setTitle("Warning");
        connectDialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                boolean bisConnFlag=false;
                ConnectivityManager conManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo network = conManager.getActiveNetworkInfo();
                if(network==null){
                    //局域网
                    Intent intent=new Intent(Settings.ACTION_WIFI_SETTINGS);
                    startActivityForResult(intent,0);
                    bisConnFlag=conManager.getActiveNetworkInfo().isAvailable();
                    if(!bisConnFlag){//蜂窝网
                        intent=new Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS);
                        startActivityForResult(intent,0);
                    }
                }
                resume();
            }

        });
        connectDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //finish();
            }
        });
        // 显示
        connectDialog.show();
    }
    /**
     * 是否有权限
     */
    public boolean isPermission(String type){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) < 0) {//打开权限请求
            if(type=="dialog") {
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("type", "permission");
                message.setData(bundle);
                message.what = 2;
                handler.sendMessage(message);
                suspend();
                return false;
            }else{
                return false;
            }
        }else{
            return true;
        }

    }
    public void isPermissionDialog(){
        AlertDialog.Builder permissionDialog = new AlertDialog.Builder(ShowInfoActivity.this);
        permissionDialog.setMessage("应用的位置权限未打开，是否打开？");
        permissionDialog.setTitle("Warning");
        permissionDialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Uri packageURI = Uri.parse("package:" + getPackageName());
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                startActivity(intent);
                resume();
            }

        });
        permissionDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //finish();关闭当前页面
            }
        });
        // 显示
        permissionDialog.show();
    }
    /**
     * gps 是否打开
     */
    public boolean isGPSOpen(String type){
        //如果未启动gps服务 启动gps服务
        location = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(!location.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            if(type=="dialog") {
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("type", "gpsOpen");
                message.setData(bundle);
                message.what = 2;
                handler.sendMessage(message);
                suspend();
                return false;
            }else{
                return false;
            }
        }else{
            return true;
        }
    }
    public void isGPSOpenDialog(){
        AlertDialog.Builder gpsOpenDialog = new AlertDialog.Builder(ShowInfoActivity.this);
        gpsOpenDialog.setMessage("GPS服务未打开，是否打开以提高精度？");
        gpsOpenDialog.setTitle("Warning");
        gpsOpenDialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent intent=new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(intent,0);
                resume();//唤醒线程
            }
        });
        gpsOpenDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //finish();
                resume();//唤醒线程
            }
        });
        // 显示
        gpsOpenDialog.show();
    }
    /**
     * 服务器连接失败
     */
    public void isSocketConnectDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("服务器连接失败");
        builder.setTitle("Warning");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // finish();
                //resume();
            }
        });
        builder.show();
    }

    /**
     * 是否要退出
     */
    public void isReturn(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("是否要退出？");
        builder.setTitle("Warning");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //resume();//唤醒线程
                socket.writeData("00002");//向服务器发送终止
                stop();
                finish();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //finish();
                resume();//唤醒线程
            }
        });
        builder.show();
    }

    /**
     * 计步器
     */
    public void showInterstitial(){
        //SensorUtil.printAll(this); // 打印所有可用传感器

        // 开启计步监听
        stepSensor = new StepSensorPedometer(this, this);
        if (!stepSensor.registerStep()) {
            stepSensor = new StepSensorAcceleration(this, this);
            if (!stepSensor.registerStep()) {
                step.setText("加速度传感器不可用！");
            }
        }

        // 开启方向监听
        orientSensor = new OrientSensor(this, this);
        if (!orientSensor.registerOrient()) {
            orient.setText("方向传感器不可用！");
        }

        // 温度方向监听
        templateSensor = new TemplateSensor(this, this);
        if (!templateSensor.registerTemplate()) {
            template.setText("温度传感器不可用！");
        }
        //locationSensor=new LocationSensor(this,this);
    }
    @Override
    public void Step(int stepNum) {
        //  计步回调
        step.setText("步数:" + stepNum);
        count=stepNum;
    }
    @Override
    public void Orient(float o) {
        // 方向回调
        orient.setText("方向:" + (int) o);
        orientNum=(int)o;
    }
    @Override
    public void Location(Location local) {
       locationview.setText("坐标："+local.getLatitude()+","+local.getLongitude());
    }
    @Override
    public void Template(float t) {
        template.setText("温度："+t);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 注销传感器监听
        stepSensor.unregisterStep();
        orientSensor.unregisterOrient();
        templateSensor.unregisterTemplate();
    }

    /**
     * 退出后再进来
     */
    private void initShow(){
        step.setText("步数:" + count);
    }

}
