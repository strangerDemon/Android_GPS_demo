package com.example.administrator.helloworld.util;


import android.os.Bundle;
import android.os.Message;
import com.example.administrator.helloworld.ShowInfoActivity;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * 封装一个全局的socket
 * Created by Administrator on 2017/4/20.
 */
public class MySocket {
    private static Socket socket;
    private static String IP="183.250.160.124";//192.168.1.94
    private static int  PORT=8888;

    /**
     * 无参构造函数
     */
    public MySocket(){
        createSocket();
    }

    /**
     * 附带参数构造函数
     * @param ip
     * @param port
     */
    public MySocket(String ip,int port){
        this.IP=ip;
        this.PORT=port;
        createSocket();
    }

    /**
     * 创建socket
     *
     */
    private void createSocket(){
        try {
            if (socket == null) {
                socket = new Socket(IP, PORT);//
            }
        }catch (Exception ex){

        }
    }

    /**
     * 关闭socket连接
     */
    public  void closeSocket(){
        try {
            socket.close();
        }catch(Exception ex){

        }
    }

    /**
     * 获取socket
     * @return
     */
    public Socket getSocket(){
        return this.socket;
    }

    /**
     * 设置ip
     * @param ip
     */
    public void setIP(String ip){
        this.IP=ip;
    }

    /**
     * 设置端口号
     * @param port
     */
    public void setPORT(int port){
        this.PORT=port;
    }

    /**
     * 发送数据
     * @param data
     */
    public void writeData(String data){
        try {
            OutputStream os = socket.getOutputStream();//字节输出流
            PrintWriter pw = new PrintWriter(os);//将输出流包装为打印流
            pw.write(data);
            pw.flush();
        }catch (Exception ex){
            Message message=new Message();
            Bundle bundle=new Bundle();
            bundle.putString("showinfo","传输数据失败："+ex.toString());
            message.setData(bundle);
            message.what=1;
        }
    }

    /**
     * 读取socket的数据
     */
    public String  readData(){
        try {
            // serverSocket.isConnected 代表是否连接成功过
            if(true == socket.isConnected()) {

                InputStream isRead = socket.getInputStream(); // 客户端接收服务器端的响应，读取服务器端向客户端的输入流

                byte[] buffer = new byte[isRead.available()]; // 缓冲区

                isRead.read(buffer);// 读取缓冲区

                return new String(buffer); // 转换为字符串
            }
            return "socket is not connect";
        }
        catch (Exception e) {
            e.printStackTrace();
            return "Exception"+e.toString();
        }
    }
}
