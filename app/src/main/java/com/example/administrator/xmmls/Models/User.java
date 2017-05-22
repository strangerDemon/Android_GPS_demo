package com.example.administrator.xmmls.Models;

import org.json.JSONObject;

/**
 * Created by Administrator on 2017/5/17.
 */
public class User {
    private String Id;
    private String Code;
    private String Name;
    private int Age;
    private int Sex;
    private String Photo;
    private int Score;
    private String Pwd;
    private double Distance;
    private int StepNum;

    public User(){

    }

    public User(String id, String code, String name, int age, int sex, String photo, int score, String pwd, double distance, int stepNum) {
        Id = id;
        Code = code;
        Name = name;
        Age = age;
        Sex = sex;
        Photo = photo;
        Score = score;
        Pwd = pwd;
        Distance = distance;
        StepNum = stepNum;
    }

    /**
     *
     *
     * @param json
     * @param mlsJson
     * @return
     */
    public User(String json,String mlsJson){
        try {
            JSONObject jsonObject = new JSONObject(json);
            this.Id = jsonObject.getString("Id");
            this.Code = jsonObject.getString("Code");
            this.Name = jsonObject.getString("Name");
            this.Age = jsonObject.getInt("Age");
            this.Sex = jsonObject.getInt("Sex");
            this.Photo = jsonObject.getString("Photo");
            this.Score = jsonObject.getInt("Score");
            this.Pwd = jsonObject.getString("Pwd");
            this.Distance = 0;//以下2个用马拉松记录表中获取
            this.StepNum = 0;
            if(mlsJson!=null&&mlsJson!="null"&&mlsJson!="") {//以下2个用马拉松记录表中获取
                JSONObject mlsObject = new JSONObject(mlsJson);
                this.Distance = mlsObject.getDouble("Distance");
                this.StepNum = mlsObject.getInt("Bs");
            }
        }catch (Exception ex){
            //return null;
        }
    }



    public String getId() {
        return Id;
    }

    public String getCode() {
        return Code;
    }

    public String getName() {
        return Name;
    }

    public int getAge() {
        return Age;
    }

    public int getSex() {
        return Sex;
    }

    public String getPhoto() {
        return Photo;
    }

    public int getScore() {
        return Score;
    }

    public String getPwd() {
        return Pwd;
    }

    public double getDistance() {
        return Distance;
    }

    public int getStepNum() {
        return StepNum;
    }

    public void setId(String id) {
        Id = id;
    }

    public void setCode(String code) {
        Code = code;
    }

    public void setName(String name) {
        Name = name;
    }

    public void setAge(int age) {
        Age = age;
    }

    public void setSex(int sex) {
        Sex = sex;
    }

    public void setPhoto(String photo) {
        Photo = photo;
    }

    public void setScore(int score) {
        Score = score;
    }

    public void setPwd(String pwd) {
        Pwd = pwd;
    }

    public void setDistance(double distance) {
        Distance = distance;
    }

    public void setStepNum(int stepNum) {
        StepNum = stepNum;
    }
}
