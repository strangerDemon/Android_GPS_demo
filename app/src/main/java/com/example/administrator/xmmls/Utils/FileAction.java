package com.example.administrator.xmmls.Utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

/**
 * 文件操作，存取马拉松数据到文件中
 * Created by Administrator on 2017/5/19.
 */
public class FileAction {

    private String FILENAME = "mlsData.txt";

    private FileInputStream fileInputStream;

    private FileOutputStream fileOutputStream;

    private PrintStream printStream;

    public FileAction(String fileName) {
        this.FILENAME = fileName;
    }

    /**
     *   单次存取 ，打开一次输入输出流，在关闭，效率
     */
    /**
     * 写入数据到本地
     *
     * @param data
     */
    public Boolean save(String data) {
        FileOutputStream out = null;
        PrintStream ps = null;
        try {
            out = new FileOutputStream(FILENAME);
            ps = new PrintStream(out);
            ps.println(data);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                    ps.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    /**
     * 读取文件中数据
     * 正常经常实时数据不在这里获取
     * 1.获取远动员数据回放（所有运动员数据）
     * 2.登录的时候读取比较服务器端数据（暂时未做）
     *
     * @return
     */
    public String read() {
        FileInputStream in = null;
        Scanner s = null;
        StringBuffer sb = new StringBuffer();
        try {
            in = new FileInputStream(FILENAME);
            s = new Scanner(in);
            while (s.hasNext()) {
                sb.append(s.next());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    /**
     * 长时间存取，不主动再一次执行后关闭/开启
     * 持久
     */
    public Boolean lastingSave(String data){
        try {
            this.printStream.println(data);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * setting/getting
     * @return
     */
    public String getFILENAME() {
        return FILENAME;
    }

    public void setFILENAME(String FILENAME) {
        this.FILENAME = FILENAME;
    }

    /**
     * 外部控制流的关闭与开启
     */
    public void openFileInputStream() {
        try {
            this.fileInputStream = new FileInputStream(FILENAME);
        } catch (Exception ex) {

        }
    }

    public void closeFileInputStream(){
        try {
            this.fileInputStream.close();
        } catch (Exception ex) {

        }

    }

    public void openFileOutputStream() {
        try {
            this.fileOutputStream = new FileOutputStream(FILENAME);
            this.printStream=new PrintStream(this.fileOutputStream);
        } catch (Exception ex) {

        }
    }

    public void closeFileOutputStream() {
        try {
            if(this.fileOutputStream!=null) {
                this.fileOutputStream.close();
                this.printStream.close();
            }
        } catch (Exception ex) {

        }
    }
}
