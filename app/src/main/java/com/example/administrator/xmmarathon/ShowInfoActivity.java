package com.example.administrator.xmmarathon;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.GpsSatellite;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;
import com.baidu.mapapi.map.MyLocationData;
import com.example.administrator.xmmarathon.Sensors.location.BaiduLocation;
import com.example.administrator.xmmarathon.Sensors.location.LocationCallBack;
import com.example.administrator.xmmarathon.Sensors.location.LocationSensor;
import com.example.administrator.xmmarathon.Sensors.location.StatusCallBack;
import com.example.administrator.xmmarathon.Sensors.orient.OrientCallBack;
import com.example.administrator.xmmarathon.Sensors.orient.OrientSensor;
import com.example.administrator.xmmarathon.Sensors.step.*;
import com.example.administrator.xmmarathon.Sensors.template.TemplateCallBack;
import com.example.administrator.xmmarathon.Sensors.template.TemplateSensor;
import com.example.administrator.xmmarathon.Utils.MyMessage;
import com.example.administrator.xmmarathon.Utils.MySocket;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Created by Administrator on 2017/4/12.
 */
public class ShowInfoActivity extends AppCompatActivity implements StepCallBack, OrientCallBack ,LocationCallBack,TemplateCallBack ,StatusCallBack{
    public static ShowInfoActivity showInfoInstance = null;
    //gsp定位服务
    LocationManager location;
    Location local;
    Boolean isSend=false;//时候可以发送数据给服务端
    //数据
    public StringBuilder lastSend;//最后一次发送的信息
    public static int count=0;//步数
    public static int orientNum=0;//方向
    //网络socket
    public static MySocket socket;//
    //线程
    beginThread thread;
    //dialog提醒一次
    public boolean gpsOpenDialog=true;
    //计步器
    private TextView step;
    private TextView orient;
    private TextView locationview;
    private TextView template;
    private TextView status;

    private StepSensorBase stepSensor; // 计步传感器
    private OrientSensor orientSensor; // 方向传感器
    private LocationSensor locationSensor;//定位
    private BaiduLocation baiduLocation;//baidu location
    private TemplateSensor templateSensor;//温度
    //关闭线程
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showinfo);
        step = (TextView) findViewById(R.id.step);
        orient = (TextView) findViewById(R.id.orient);
        locationview=(TextView)findViewById(R.id.showinfo);
        template=(TextView)findViewById(R.id.template);
        status=(TextView)findViewById(R.id.status);

        initShow();
        showInterstitial();
        thread=new beginThread();
        thread.start();
        showInfoInstance=this;
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
        isReturn();
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
            String view = "";
            switch (msg.what) {
                case 1://数据显示
                    show = (String) data.get("text");
                    view=(String)data.get("view");
                    if("locationview".equals(view)) {
                        locationview.setText(show);
                    }
                    break;
                case 2://弹窗
                    break;
                case 3://toast
                    show = (String) data.get("text");
                    if (show != "" && show != null) {
                        Toast.makeText(ShowInfoActivity.this, show, Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };
    /**
     * 自定义线程操作
     */
   class beginThread extends Thread implements Runnable {
        public void run() {
            Looper.prepare();//相当于该线程Looper的初始化
            try {
                /*while(true) {
                    if(local!=null*//*&&isSend*//*) {
                        getSendData();
                    }
                    sleep(1000);//wait线程被暂停，需要notify 来释放
                }*/
            } catch (Exception e) {
                MyMessage myMessage=new MyMessage(1,"text", e.toString());
                handler.sendMessage(myMessage.getMessage());
            }
            Looper.loop();//Looper开始执行，注意该语句执行后，这个线程的其他操作就被阻塞，只能响应事件了。
        }
    }
    /**
     * 判断是否有连接网络，gps，权限
     */
    private Boolean check() {
        //创建连接 防止 运行一半的时候 服务器挂了，用户关掉数据
        if (!isConn()) {
            MyMessage myMessage=new MyMessage(3,"text", "网络连接失败");
            handler.sendMessage(myMessage.getMessage());
            return false;
        }else if (socket == null) {
            try {
                socket = new MySocket();//
                socket.createSocket();
            }catch(Exception ex){
                MyMessage myMessage=new MyMessage(3,"text", "服务器连接失败");
                handler.sendMessage(myMessage.getMessage());
                return false;
            }
        }else if (!isGPSOpen("judge")) {
            if (gpsOpenDialog) {//获取定位服务，因为是系统的服务，因此不能new出来
                isGPSOpen("dialog");
                gpsOpenDialog = false;
            }else {
                MyMessage myMessage = new MyMessage(3, "text", "GPS连接失败");
                handler.sendMessage(myMessage.getMessage());
                return false;
            }
        }else {
            if (Build.VERSION.SDK_INT >= 23) { //CONTROLLO PER ANDROID 6.0 O SUPERIORE
                isPermission("dialog");
            } else {//android 6.0 以下
            }
        }
        return true;
    }

    /**
     * 获取需要发送的信息数据 除了速度 坐标 其余都是假数据
     * 数据格式：00006经纬度坐标        |心率|速度|步数 |步频|体温|配速|步幅
     * 例如：    00006117.2323,24.23566|80  |60  |10023|10 |37.6|52  |68
     * y=1-1/(1+x)渐进函数
     */
    private void getSendData(){
        //locationSensor.getLastKnownLocation();//请求新的数据
        //baiduLocation.getLastKnownLocation();
        if(!check()){return;}
        lastSend=new StringBuilder();
        if(local !=null){
            lastSend.append("$00003"+local.getLongitude()+","+local.getLatitude());//经纬度 √
            //安静状态下，成人正常心率为60～100次/分钟，理想心率应为55～70次/分钟（运动员的心率较普通成人偏慢，一般为50次/分钟左右
            lastSend.append("|"+(40*(int)(1-10/(10+local.getSpeed()))+60+new Random().nextInt(3)));//心率
            lastSend.append("|"+local.getSpeed());//速度
            lastSend.append("|"+count);//步数 √
            lastSend.append("|"+5*(int)(1-10/(10+local.getSpeed())));//步频竞走步频可达3.5—3.7步/秒；短跑步频可达4.6—5.1步/秒
            lastSend.append(String.format("|" + (37 + (1-10/(10+local.getSpeed())))));//体温
            lastSend.append("|"+1000.0/(local.getSpeed()+1));//配速 你每跑一千米就需要7分30秒的时间。这也就是你的配速7m30s。
            lastSend.append("|"+(68+new Random().nextInt(5)));//步幅
            lastSend.append("|"+baiduLocation.getTotalTrip());//行程
        }else{
        }
        try {
            socket.writeData(lastSend.toString());
        }catch(Exception ex){
            MyMessage myMessage=new MyMessage(3,"text", "发送数据失败");
            handler.sendMessage(myMessage.getMessage());
            /**
             *  关闭之前的socket 重新创建socket
             */
            reLogin();
        }
    }
    /**
     *  重新登录
     *  关闭之前的socket 重新创建socket
     */
    public void reLogin(){
        try {
            socket.reCreateSocket();
            socket.writeData("null");//少了这个服务器可能收不到下面的信息
            socket.writeData("$00001" + LoginActivity.getUserId());
        }catch(Exception ex){
            MyMessage myMessage=new MyMessage(3,"text", "重新连接服务器失败");
            handler.sendMessage(myMessage.getMessage());
        }
    }
    /**
     * 时间格式转换
     * 如果是network获取得到的话需要加上东八区，测试出三星平板需要加，华为手机不要加
     * 如果是gps获取得到的数据 不需要加
     * @param time
     * @param source 源 ,network or gps
     * @return
     */
    public static String timedate(String time, String source) {
        SimpleDateFormat sdr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        /*long lcc =Long.valueOf(time);//加8个小时 东八区"network".equals(source)? Long.valueOf(time)+8*60*60*1000:Long.valueOf(time)
        String times = sdr.format(new Date(lcc));
        return times;*/
        //华为network获取的时间已经是东八区的时间，三星的不是东八区的 ，坑
        return sdr.format(new Date());
    }

    /**
     * 判断网络连接是否已开
     * true 已打开  false 未打开
     */
    public boolean isConn(){
        ConnectivityManager conManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo network = conManager.getActiveNetworkInfo();
        if(network==null) {
            return false;
        }else{
            return true;
        }
    }
    /**
     * 是否有权限
     */
    public boolean isPermission(String type){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) < 0) {//打开权限请求
            if(type=="dialog") {
                isPermissionDialog();
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
        permissionDialog.setMessage("应用的位置权限未打开，无法获取位置信息，是否打开？");
        permissionDialog.setTitle("Warning");
        permissionDialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Uri packageURI = Uri.parse("package:" + getPackageName());
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                startActivity(intent);
            }

        });
        permissionDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
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
                isGPSOpenDialog();
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
            }
        });
        gpsOpenDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        // 显示
        gpsOpenDialog.show();
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
                try {
                    socket.writeData("$00002");//向服务器发送终止
                }catch (Exception e){
                }
                LoginActivity.isOpenMain=false;
                local=null;
                handler.removeCallbacks(thread);
                finish();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    /**
     * 计步器
     */
    public void showInterstitial(){
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

        //Location定位服务
        /*locationSensor=new LocationSensor(this,this,this);
        if (!locationSensor.registerLocation()) {
            locationview.setText("定位服务不可用！");
        }*/
        try {
            baiduLocation = new BaiduLocation(this, this, this);
        }catch(Exception ex){
            ex.toString();
        }

    }
    //4个监听器的数据源监测数据回调
    //  计步回调
    @Override
    public void Step(int stepNum) {
        if((local!=null&&local.getSpeed()==0)||!isSend) {
        }else {
            step.setText("步数:" + stepNum);
            count = stepNum;
        }
    }
    // 方向回调
    @Override
    public void Orient(float o) {
        orient.setText("方向:" + (int) o);
        orientNum=(int)o;
    }
    //定位回调
    @Override
    public void Location(Location loc, Boolean isChange) {
        isSend=isChange;//从开始监测到gps数据变化开始才向服务器发送数据
        if(loc!=null) {
            String text = ("提供者："+loc.getProvider()+"\n经度：" +loc.getLongitude()  + "\n纬度：" + loc.getLatitude() + "\n速度：" + loc.getSpeed() + "\n精度："+loc.getAccuracy()+
                    "\n行程:"+baiduLocation.getTotalTrip()+"\n时间:" + timedate(loc.getTime() + "", loc.getProvider())).toString();
            MyMessage myMessage = new MyMessage(1, "text", text);
            myMessage.setBundle("view", "locationview");
            handler.sendMessage(myMessage.getMessage());
            this.local = loc;
            getSendData();
        }else{
            this.local=new Location("gps");
        }
    }
    @Override
    public void BaiduLocation(MyLocationData loc, Boolean isChange) {
        isSend=isChange;//从开始监测到gps数据变化开始才向服务器发送数据
        if(loc!=null) {
            String text = ("提供者：baidu   \n经度：" +loc.longitude  + "\n纬度：" + loc.latitude + "\n速度：" + loc.speed + "\n精度："+loc.accuracy+
                    "\n行程:"+baiduLocation.getTotalTrip()+"\n时间:" + timedate(new Date() + "", "baidu")).toString();
            MyMessage myMessage = new MyMessage(1, "text", text);
            myMessage.setBundle("view", "locationview");
            handler.sendMessage(myMessage.getMessage());
            this.local=new Location("gps");
            local.setAccuracy(loc.accuracy);
            local.setLatitude(loc.latitude);
            local.setLongitude(loc.longitude);
            local.setSpeed(loc.speed);
        }else{
            this.local=new Location("gps");
        }
    }
    @Override
    public void Status(List<GpsSatellite> numSatelliteList){
        status.setText("卫星数："+numSatelliteList.size());
    }
    @Override
    public void StatusNum(int num){
        status.setText("卫星数："+num);
    }
    //温度回调
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
        //locationSensor.unregisterLocation();
        baiduLocation.onDestroy();

    }

    /**
     * 退出后再进来
     */
    private void initShow(){
        step.setText("步数:" + count);
    }

}
