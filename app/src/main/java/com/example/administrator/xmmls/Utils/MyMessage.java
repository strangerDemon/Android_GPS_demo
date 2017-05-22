package com.example.administrator.xmmls.Utils;

import android.os.Bundle;
import android.os.Message;

/**
 * Created by Administrator on 2017/4/26.
 */
public class MyMessage {

    private Message message;
    private Bundle bundle;

    /**
     * 无参构造函数
     */
    public MyMessage() {
        this.message=new Message();
        this.bundle = new Bundle();
    }

    /**
     * 有参数构造函数
     * @param message
     * @param bundle
     */
    public MyMessage(Message message, Bundle bundle){
        this.message=message;
        this.bundle=bundle;
    }

    /**
     * 有参数构造函数
     * @param what
     * @param title
     * @param text
     */
    public MyMessage(int what , String title, String text ){
        this.message=new Message();
        this.bundle = new Bundle();
        this.message.what=what;
        this.bundle.putString(title,text);
    }
    /**
     * 设置what
     * @param what
     */
    public void setMessage(int what){
        this.message.what=what;
    }

    /**
     * 设置message
     * @param what
     * @param title
     * @param text
     */
    public void setMessage(int what , String title, String text ){
        this.message.what=what;
        this.bundle.putString(title,text);
    }

    /**
     * 设置bundle
     * @param title
     * @param text
     */
    public void setBundle(String title, String text){
        this.bundle.putString(title,text);
    }


    public Message getMessage(){
        message.setData(bundle);
        return this.message;
    }
}
