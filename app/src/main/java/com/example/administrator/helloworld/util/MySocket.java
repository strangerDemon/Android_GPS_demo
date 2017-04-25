package com.example.administrator.helloworld.util;


import java.io.DataOutputStream;
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
    }

    /**
     * 附带参数构造函数
     * @param ip
     * @param port
     */
    public MySocket(String ip,int port){
        this.IP=ip;
        this.PORT=port;
    }

    /**
     * 创建socket
     *
     */
    public void createSocket() throws Exception {
        if (socket == null) {
            socket = new Socket(IP, PORT);
        }
    }
    /**
     * 重新创建socket
     *
     */
    public void reCreateSocket() throws Exception{
        socket = new Socket(IP, PORT);
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
    public void writeData(String data) throws Exception {
        /*OutputStream os = socket.getOutputStream();//字节输出流
        PrintWriter pw = new PrintWriter(os);//将输出流包装为打印流
        pw.write(data);
        pw.flush();*///这个不会报ioException
        DataOutputStream os = new DataOutputStream(socket.getOutputStream());
        os.write(data.getBytes());
        os.flush();//这个会报ioException
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
