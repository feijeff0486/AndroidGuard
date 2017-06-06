package com.jeff.cacheclear.dischangecounter;

import android.graphics.drawable.Drawable;

/**
 * Created by 小太阳jeff on 2017/6/4.
 */

public class AppTrafficInfo {
    private Drawable icon;//应用图标
    private String name;//应用名称

    @Override
    public String toString() {
        return "AppTrafficInfo{" +
                "icon=" + icon +
                ", name='" + name + '\'' +
                ", uid=" + uid +
                ", tx=" + tx +
                ", rx=" + rx +
                '}';
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    private int uid;
    private long tx;//应用上传的流量
    private long rx;//应用接收的流量

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTx() {
        return tx;
    }

    public void setTx(long tx) {
        this.tx = tx;
    }

    public long getRx() {
        return rx;
    }

    public void setRx(long rx) {
        this.rx = rx;
    }

}
