package com.example.administrator.helloworld;

import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.content.Intent;
import android.widget.EditText;
import com.example.administrator.helloworld.util.MySocket;
/**
 *
 */
public class LoginActivity extends AppCompatActivity{

    private EditText gameIdView;
    private EditText userIdView;
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
     * start
     */
    private void attemptLogin() {
        View focusView = null;//焦点view
        boolean cancel=false;
        String gameId = gameIdView.getText().toString();
        String userId = userIdView.getText().toString();
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
        if (!checkUser(userId,gameId)) {
            userIdView.setError(getString(R.string.error_no_login));
            focusView = userIdView;
            cancel = true;
        }
        if(cancel){
            focusView.requestFocus();
        }else {
            Intent intent = new Intent();
            intent.setClass(LoginActivity.this, ShowInfoActivity.class);
            LoginActivity.this.startActivity(intent);
        }
    }

    /**
     * 校验数据库时候有对应的比赛编号和用户
     * @param userId
     * @param gameId
     * @return
     */
    private Boolean checkUser(String userId,String gameId){
        MySocket socket= new MySocket();
        socket.writeData("00001"+userId);
        String answer=socket.readData();
        if(answer=="000011"){
            return true;
        }else {
            return true;
            // return false;
        }
    }
}

