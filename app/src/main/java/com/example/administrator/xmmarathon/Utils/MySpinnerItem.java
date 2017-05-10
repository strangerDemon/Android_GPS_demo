package com.example.administrator.xmmarathon.Utils;

/**
 * Created by Administrator on 2017/5/10.
 */
public class MySpinnerItem {
    public String key;
    public String value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public MySpinnerItem() {
    }

    public MySpinnerItem(String key,String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString() {
        return key;
    }
}
