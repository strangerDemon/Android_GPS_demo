package com.example.administrator.helloworld;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.content.Intent;
import android.widget.EditText;
import android.widget.Toast;
import com.example.administrator.helloworld.Utils.GetServiceData;
import com.example.administrator.helloworld.Utils.GetServiceDataCallBack;
import com.example.administrator.helloworld.Utils.MyMessage;
import com.example.administrator.helloworld.Utils.MySocket;
/**
 *
 */
public class LoginActivity extends AppCompatActivity implements GetServiceDataCallBack{

    private EditText gameIdView;
    private EditText userIdView;

    private View focusView = null;//焦点view
    private boolean cancel=false;
    private static String gameId;
    private static String userId;

    public static boolean isOpenMain=false;//主页面时候已经打开

    private loginThread thread;
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

        gameIdView =(EditText)findViewById(R.id.gameId);
        userIdView=(EditText)findViewById(R.id.userId);

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
        gameId = gameIdView.getText().toString();
        userId = userIdView.getText().toString();
        //校验是否为null
        if (TextUtils.isEmpty(gameId)) {
            gameIdView.setError(getString(R.string.error_null));
            focusView = gameIdView;
            cancel = true;
        }
        if (TextUtils.isEmpty(userId)) {
            userIdView.setError(getString(R.string.error_null));
            focusView = userIdView;
            cancel = true;
        }

        if(cancel){
            focusView.requestFocus();
        }else {
            thread = new loginThread();
            thread.start();
        }
    }

    /**
     * 校验数据库时候有对应的比赛编号和用户
     * @param userId
     * @param gameId
     * @return
     */
    private void checkUser(String userId, String gameId) {
        try {
            MySocket socket = new MySocket();
            socket.reCreateSocket();
            new GetServiceData(this, this);
            socket.writeData("null" + userId);//少了这个服务器收不到下面的信息
            socket.writeData("$00001" + userId);
            MyMessage myMessage = new MyMessage(1, "text", "登录中...");
            handler.sendMessage(myMessage.getMessage());
        } catch (Exception ex) {
            MyMessage myMessage = new MyMessage(1, "text", "服务器连接失败");
            handler.sendMessage(myMessage.getMessage());
        }
    }
    /**
     * 主线程不能访问网络
     * 自定义线程操作
     */
    class loginThread extends Thread {
        public void run() {checkUser(userId,gameId);}
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
            MyMessage myMessage = new MyMessage(1, "text", "账号密码有误");
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

