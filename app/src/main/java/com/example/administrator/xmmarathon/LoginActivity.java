package com.example.administrator.xmmarathon;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.content.Intent;
import com.example.administrator.xmmarathon.Datas.Grobal;
import com.example.administrator.xmmarathon.Models.User;
import com.example.administrator.xmmarathon.Sensors.location.BaiduLocation;
import com.example.administrator.xmmarathon.Utils.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class LoginActivity extends AppCompatActivity implements GetServiceDataCallBack{

    private Spinner gameIdView;
    private EditText userIdView;
    private EditText passwordView;

    private View focusView = null;//焦点view
    private boolean cancel=false;
    private static String gameId;
    private static String userId;
    private static String password;

    public static boolean isOpenMain=false;//主页面时候已经打开

    private LoginThread thread;

    private LoadThread loadThread;//加载比赛列表的线程
    private static String gameList="";//比赛列表
    List<MySpinnerItem> data_list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /*按钮点击事件*/
        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        gameIdView =(Spinner)findViewById(R.id.gameId);
        userIdView=(EditText)findViewById(R.id.userId);
        passwordView=(EditText)findViewById(R.id.password);

        loadThread=new LoadThread();
        loadThread.start();
    }
    /**
     * 因为ui只能在主线程中修改，应该要用handler处理
     */
    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            Bundle data = msg.getData();
            String show = "";
            String type = "";
            switch (msg.what) {
                case 1://数据显示
                    show = (String) data.get("text");
                    if (show != "" || show != null) {
                        Toast.makeText(LoginActivity.this, show, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 2://弹窗
                    break;
                case 3://load data
                        //适配器
                        ArrayAdapter<MySpinnerItem> arr_adapter = new ArrayAdapter<MySpinnerItem>(LoginActivity.this, android.R.layout.simple_spinner_dropdown_item, data_list);
                        //设置样式
                        arr_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        //加载适配器
                        gameIdView.setAdapter(arr_adapter);
                    break;
            }
            super.handleMessage(msg);
        }
    };
    /**
     * start
     */
    private void attemptLogin() {
        focusView = null;//焦点view
        cancel=false;
        gameId = ((MySpinnerItem) (gameIdView.getSelectedItem())).getValue();
        userId = userIdView.getText().toString();
        password=passwordView.getText().toString();

        //校验是否为null
        if (TextUtils.isEmpty(gameId)) {
            //gameIdView.set(getString(R.string.error_null));
            focusView = gameIdView;
            cancel = true;
        }
        if (TextUtils.isEmpty(userId)) {
            userIdView.setError(getString(R.string.error_null));
            focusView = userIdView;
            cancel = true;
        }
        if(TextUtils.isEmpty(password)){
            passwordView.setError(getString(R.string.error_null));
            focusView=passwordView;
            cancel=true;
        }
        if(cancel){
            focusView.requestFocus();
        }else {
            thread = new LoginThread();
            thread.start();
        }
    }

    /**
     * 校验数据库时候有对应的比赛编号和用户
     * @param userId
     * @param gameId
     * @return
     */
    private void checkUser(String userId, String gameId,String password) {
        try {
            //接口校验用户信息
            String loginCheck=GetApiData.postDownloadJson("/loginVerification","'mlsCode':'"+gameId+"','userCode':'"+userId+"','pwd':'"+password+"'");
            JSONObject jsonObject=new JSONObject(loginCheck);//{"Results":null,"RespDesc":"失败","RespCode":0};{"Results":null,"RespDesc":"成功","RespCode":1}
            if(jsonObject.getString("RespCode").equals("1")){//登录成功
                //保存用户信息
                String mlsData=GetApiData.postDownloadJson("/getUserLastMlsData","'userCode':'"+userId+"'");
                JSONObject MlsObject=new JSONObject(mlsData);
                Grobal.user=new User().JsonTOUser(jsonObject.getString("Results"),MlsObject.getString("Results"));
                //提醒
                MyMessage myMessage = new MyMessage(1, "text", Grobal.user.getName()+"欢迎使用");
                handler.sendMessage(myMessage.getMessage());
                //连接数据接收端
                MySocket socket = new MySocket();
                socket.reCreateSocket();
                new GetServiceData(this, this);
                socket.writeData("$00001" + userId+"|"+password+"|"+gameId);//少了这个服务器收不到下面的信息
                socket.writeData("$00001" + userId+"|"+password+"|"+gameId);
                myMessage = new MyMessage(1, "text", "服务端连接中...");
                handler.sendMessage(myMessage.getMessage());
            }else if(jsonObject.getString("RespCode").equals("0")){//登录失败
                MyMessage myMessage = new MyMessage(1, "text", "登录失败，请校验账号密码");
                handler.sendMessage(myMessage.getMessage());
            }else {//还要加一个赛事结束
                MyMessage myMessage = new MyMessage(1, "text", "赛事已结束！");
                handler.sendMessage(myMessage.getMessage());
            }
        } catch (Exception ex) {
            MyMessage myMessage = new MyMessage(1, "text", "服务端连接失败");
            handler.sendMessage(myMessage.getMessage());
        }
    }
    /**
     * 主线程不能访问网络
     * 自定义线程操作
     */
    class LoginThread extends Thread {
        public void run() {checkUser(userId,gameId,password);}
    }
    /**
     * 主线程不能访问网络
     * 自定义线程操作
     */
    class LoadThread extends Thread {
        public void run() {
            while(gameList==null||gameList=="") {
                gameList=GetApiData.postDownloadJson("/getMLSList","");
            }try {
                JSONObject jsonObject = new JSONObject(gameList);
                JSONArray jsonArray=jsonObject.getJSONArray("Results");
                data_list = new ArrayList<MySpinnerItem>();
                MySpinnerItem item;
                for(int i=0;i<jsonArray.length();i++){
                    JSONObject json = (JSONObject)jsonArray.opt(i);
                    item=new MySpinnerItem(json.getString("Name"),json.getString("Code"));
                    data_list.add(item);
                }
                MyMessage myMessage = new MyMessage(3, "text", "loadData");
                handler.sendMessage(myMessage.getMessage());
            }catch (Exception ex){
                ex.toString();
            }
        }
    }
    //setting getting
    public static String getUserId(){
        return userId;
    }
    public static String getGameId(){
        return gameId;
    }

    //callback
    @Override
    public void ServiceData(String callback) {
        if (callback.contains("000011")  && !isOpenMain) {
            isOpenMain = true;
            Intent intent = new Intent();
            intent.setClass(LoginActivity.this, ShowInfoActivity.class);
            LoginActivity.this.startActivity(intent);
        } else if (callback.contains("000010")){
            MyMessage myMessage = new MyMessage(1, "text", "服务端连接失败");
            handler.sendMessage(myMessage.getMessage());
        }else{
            MyMessage myMessage = new MyMessage(1, "text", callback);
            handler.sendMessage(myMessage.getMessage());
            if(callback.contains("ServerOff")){
                isOpenMain=false;
                ShowInfoActivity.showInfoInstance.finish();//类似于单例模式 关闭页面
                ShowInfoActivity.showInfoInstance.handler.removeCallbacks(ShowInfoActivity.showInfoInstance.thread);//关闭线程
                ShowInfoActivity.showInfoInstance.local=null;
            }
        }
    }
}

